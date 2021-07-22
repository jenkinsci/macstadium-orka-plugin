package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner.PlannedNode;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.OrkaVM;
import io.jenkins.plugins.orka.client.OrkaVMConfig;
import io.jenkins.plugins.orka.helpers.CapacityHandler;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class OrkaCloud extends Cloud {
    private static final Logger logger = Logger.getLogger(OrkaCloud.class.getName());
    private static final int recommendedMinTimeout = 30;
    private static final int defaultTimeout = 300;

    private String credentialsId;
    private String endpoint;
    private int instanceCap;
    private String instanceCapSetting;
    private int timeout;
    private boolean useJenkinsProxySettings;
    private boolean ignoreSSLErrors;

    private List<? extends AddressMapper> mappings;
    private final List<? extends AgentTemplate> templates;
    private transient CapacityHandler capacityHandler;
    
    public OrkaCloud(String name, String credentialsId, String endpoint, String instanceCapSetting, int timeout,
            boolean useJenkinsProxySettings, List<? extends AddressMapper> mappings,
            List<? extends AgentTemplate> templates) {
        this(name, credentialsId, endpoint, instanceCapSetting, timeout, useJenkinsProxySettings, false, mappings,
                templates);
    }

    @DataBoundConstructor
    public OrkaCloud(String name, String credentialsId, String endpoint, String instanceCapSetting, int timeout,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, List<? extends AddressMapper> mappings,
            List<? extends AgentTemplate> templates) {
        super(name);

        this.credentialsId = credentialsId;
        this.endpoint = endpoint;
        this.instanceCapSetting = instanceCapSetting;
        this.timeout = timeout;
        this.useJenkinsProxySettings = useJenkinsProxySettings;
        this.ignoreSSLErrors = ignoreSSLErrors;

        this.mappings = mappings;
        this.templates = templates == null ? Collections.emptyList() : templates;

        readResolve();
    }

    protected Object readResolve() {
        this.templates.forEach(t -> t.setParent(this));
        this.mappings = this.mappings == null ? Collections.emptyList() : this.mappings;

        if (StringUtils.isEmpty(this.instanceCapSetting)) {
            this.instanceCap = Integer.MAX_VALUE;
        } else {
            this.instanceCap = Integer.parseInt(this.instanceCapSetting);
        }

        this.capacityHandler = new CapacityHandler(this.name, this.instanceCap);

        this.timeout = this.timeout > 0 ? this.timeout : defaultTimeout;

        return this;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public boolean getUseJenkinsProxySettings() {
        return this.useJenkinsProxySettings;
    }
    
    public boolean getIgnoreSSLErrors() {
        return this.ignoreSSLErrors;
    }

    public String getInstanceCapSetting() {
        return this.instanceCap == Integer.MAX_VALUE ? "" : String.valueOf(this.instanceCap);
    }

    public int getTimeout() {
        return this.timeout;
    }

    public List<? extends AddressMapper> getMappings() {
        return this.mappings;
    }

    public List<? extends AgentTemplate> getTemplates() {
        return this.templates;
    }

    @Override
    public boolean canProvision(Label label) {
        return getTemplate(label) != null;
    }

    public AgentTemplate getTemplate(Label label) {
        return templates.stream().filter(t -> normalModeOrMatches(t, label) || exclusiveModeAndMatches(t, label))
                .findFirst().orElse(null);
    }

    public List<OrkaVM> getVMs() throws IOException {
        return new OrkaClientProxyFactory()
                .getOrkaClientProxy(this.endpoint, this.credentialsId, this.useJenkinsProxySettings, 
                        this.ignoreSSLErrors)
                .getVMs();
    }
    
    public List<OrkaVMConfig> getVMConfigs() throws IOException {
        return new OrkaClientProxyFactory()
                .getOrkaClientProxy(this.endpoint, this.credentialsId, this.useJenkinsProxySettings, 
                        this.ignoreSSLErrors)
                .getVMConfigs();
    }

    public ConfigurationResponse createConfiguration(String name, String image, String baseImage, String configTemplate,
            int cpuCount) throws IOException {
        return new OrkaClientProxyFactory()
                .getOrkaClientProxy(this.endpoint, this.credentialsId, this.useJenkinsProxySettings, 
                        this.ignoreSSLErrors)
                .createConfiguration(name, image, baseImage, configTemplate, cpuCount);
    }

    public DeploymentResponse deployVM(String name) throws IOException {
        return new OrkaClientProxyFactory()
                .getOrkaClientProxy(this.endpoint, this.credentialsId, this.timeout, this.useJenkinsProxySettings, 
                        this.ignoreSSLErrors)
                .deployVM(name);
    }

    public void deleteVM(String name) throws IOException {
        DeletionResponse deletionResponse = new OrkaClientProxyFactory().getOrkaClientProxy(this.endpoint, this.credentialsId, this.useJenkinsProxySettings,
                this.ignoreSSLErrors)
                .deleteVM(name);
        
        if (!deletionResponse.isSuccessful()) {
            logger.warning("Deleting VM failed with: " + Utils.getErrorMessage(deletionResponse));
            return null;
        }

        this.capacityHandler.removeRunningInstance();
    }

    public String getRealHost(String host) {
        return this.mappings.stream().filter(m -> m.getDefaultHost().equalsIgnoreCase(host)).findFirst()
                .map(m -> m.getRedirectHost()).orElse(host);
    }

    @Override
    public Collection<PlannedNode> provision(final Label label, int excessWorkload) {
        String provisionIdString = "[provisionId=" + UUID.randomUUID().toString() + "] ";

        try {
            String labelName = label != null ? label.getName() : "";
            logger.info(provisionIdString + "Provisioning for label " + labelName + ". Workload: " + excessWorkload);

            AgentTemplate template = this.getTemplate(label);

            if (template == null) {
                logger.fine(provisionIdString + "Couldn't find template for label " + labelName
                        + ". Stopping provisioning.");
                return Collections.emptyList();
            }

            int vmsToProvision = Math.max(excessWorkload / template.getNumExecutors(), 1);
            int possibleVMsToProvision = this.capacityHandler.reserveCapacity(vmsToProvision, provisionIdString);
            logger.fine(String.format("%s. Asked for %s VMs and got %s", provisionIdString, vmsToProvision,
                    possibleVMsToProvision));

            return IntStream.range(0, possibleVMsToProvision).mapToObj(i -> {
                String nodeName = UUID.randomUUID().toString();
                Callable<Node> provisionNodeCallable = this.provisionNode(template, provisionIdString,
                        this.capacityHandler);
                Future<Node> provisionNodeTask = Computer.threadPoolForRemoting.submit(provisionNodeCallable);

                return new PlannedNode(nodeName, provisionNodeTask, template.getNumExecutors());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.WARNING, provisionIdString + "Exception during provisioning", e);
        }

        return Collections.emptyList();
    }

    private Callable<Node> provisionNode(AgentTemplate template, String provisionIdString,
            CapacityHandler capacityHandler) {
        return new Callable<Node>() {
            @Override
            public Node call() throws Exception {

                logger.fine(provisionIdString + "Provisioning Node with template:");
                logger.fine(template.toString());
                OrkaProvisionedAgent agent = null;

                try {
                    agent = template.provision();
                } catch (Exception e) {
                    capacityHandler.removeFailedPlannedInstance();
                    logger.log(Level.WARNING, "Exception during provision", e);
                    throw e;
                }

                if (agent != null) {
                    logger.fine(provisionIdString + "Adding Node to Jenkins:");
                    logger.fine(agent.toString());
                    capacityHandler.addRunningInstance();
                } else {
                    capacityHandler.removeFailedPlannedInstance();
                }

                return agent;
            }
        };
    }

    private boolean normalModeOrMatches(AgentTemplate template, Label label) {
        return template.getMode() == Node.Mode.NORMAL && (label == null || label.matches(template.getLabelSet()));
    }

    private boolean exclusiveModeAndMatches(AgentTemplate template, Label label) {
        return template.getMode() == Node.Mode.EXCLUSIVE && (label != null && label.matches(template.getLabelSet()));
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Cloud> {
        private OrkaClientProxyFactory clientProxyFactory = new OrkaClientProxyFactory();
        private FormValidator formValidator = new FormValidator(this.clientProxyFactory);

        @Override
        public String getDisplayName() {
            return "Orka Cloud";
        }

        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public ListBoxModel doFillCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public FormValidation doCheckTimeout(@QueryParameter String value) {
            try {
                int timeoutValue = Integer.parseInt(value);
                if (0 < timeoutValue && timeoutValue < recommendedMinTimeout) {
                    return FormValidation.warning(String.format(
                            "Deployment timeout less than %d seconds is not recommended.", recommendedMinTimeout));
                }

                if (timeoutValue <= 0) {
                    return FormValidation.error("Deployment timeout must be a positive number.");
                }

                return FormValidation.ok();
            } catch (NumberFormatException e) {
                return FormValidation.error("Deployment timeout must be a number.");
            }
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter String credentialsId, @QueryParameter String endpoint,
                @QueryParameter boolean useJenkinsProxySettings, 
                @QueryParameter boolean ignoreSSLErrors) throws IOException {

            return this.formValidator.doTestConnection(credentialsId, endpoint, useJenkinsProxySettings,
                    ignoreSSLErrors);
        }
    }
}
