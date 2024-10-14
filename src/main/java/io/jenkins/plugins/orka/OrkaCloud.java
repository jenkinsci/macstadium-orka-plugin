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

import io.jenkins.plugins.orka.client.DeletionResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.OrkaVMConfig;
import io.jenkins.plugins.orka.helpers.CapacityHandler;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
import io.jenkins.plugins.orka.helpers.Utils;

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

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;




public class OrkaCloud extends Cloud {
    private static final Logger logger = Logger.getLogger(OrkaCloud.class.getName());
    private static final int recommendedMinTimeout = 30;
    private static final int defaultTimeout = 600;
    private static final int defaultHttpTimeout = 300;

    private String credentialsId;
    private String endpoint;
    private int instanceCap;
    private String instanceCapSetting;
    private int timeout;
    private int httpTimeout;
    private boolean useJenkinsProxySettings;
    private boolean ignoreSSLErrors;
    private boolean noDelayProvisioning;

    private List<? extends AddressMapper> mappings;
    private final List<? extends AgentTemplate> templates;
    private transient CapacityHandler capacityHandler;

    public OrkaCloud(String name, String credentialsId, String endpoint, String instanceCapSetting, int timeout,
            boolean useJenkinsProxySettings, List<? extends AddressMapper> mappings,
            List<? extends AgentTemplate> templates) {
        this(name, credentialsId, endpoint, instanceCapSetting, timeout, useJenkinsProxySettings, false, mappings,
                templates);
    }

    public OrkaCloud(String name, String credentialsId, String endpoint, String instanceCapSetting, int timeout,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, List<? extends AddressMapper> mappings,
            List<? extends AgentTemplate> templates) {
        this(name, credentialsId, endpoint, instanceCapSetting, timeout, defaultHttpTimeout,
                useJenkinsProxySettings, ignoreSSLErrors, mappings, templates);
    }

    public OrkaCloud(String name, String credentialsId, String endpoint, String instanceCapSetting, int timeout,
            int httpTimeout, boolean useJenkinsProxySettings, boolean ignoreSSLErrors,
            List<? extends AddressMapper> mappings, List<? extends AgentTemplate> templates) {
        this(name, credentialsId, endpoint, instanceCapSetting, timeout, defaultHttpTimeout,
                useJenkinsProxySettings, ignoreSSLErrors, false, mappings, templates);
    }

    @DataBoundConstructor
    public OrkaCloud(String name, String credentialsId, String endpoint, String instanceCapSetting, int timeout,
            int httpTimeout, boolean useJenkinsProxySettings, boolean ignoreSSLErrors, boolean noDelayProvisioning,
            List<? extends AddressMapper> mappings, List<? extends AgentTemplate> templates) {
        super(name);

        this.credentialsId = credentialsId;
        this.endpoint = endpoint;
        this.instanceCapSetting = instanceCapSetting;
        this.timeout = timeout;
        this.httpTimeout = httpTimeout;
        this.useJenkinsProxySettings = useJenkinsProxySettings;
        this.ignoreSSLErrors = ignoreSSLErrors;
        this.noDelayProvisioning = noDelayProvisioning;

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
        this.httpTimeout = this.httpTimeout > 0 ? this.httpTimeout : defaultHttpTimeout;

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

    public int getHttpTimeout() {
        return this.httpTimeout;
    }

    public List<? extends AddressMapper> getMappings() {
        return this.mappings;
    }

    public List<? extends AgentTemplate> getTemplates() {
        return this.templates;
    }

    public boolean getNoDelayProvisioning() {
        return this.noDelayProvisioning;
    }

    @Override
    public boolean canProvision(Label label) {
        return getTemplate(label) != null;
    }

    public AgentTemplate getTemplate(Label label) {
        return templates.stream().filter(t -> normalModeOrMatches(t, label) || exclusiveModeAndMatches(t, label))
                .findFirst().orElse(null);
    }

    public List<OrkaVMConfig> getVMConfigs() throws IOException {
        return new OrkaClientFactory()
                .getOrkaClient(this.endpoint, this.credentialsId, this.httpTimeout, this.useJenkinsProxySettings,
                        this.ignoreSSLErrors)
                .getVMConfigs().getConfigs();
    }

    public DeploymentResponse deployVM(String namespace, String namePrefix, String vmConfig, String image, Integer cpu,
            String memory, String scheduler,
            String tag,
            Boolean tagRequired) throws IOException {
        return new OrkaClientFactory()
                .getOrkaClient(this.endpoint, this.credentialsId, this.timeout, this.useJenkinsProxySettings,
                        this.ignoreSSLErrors)
                .deployVM(vmConfig, namespace, namePrefix, image, cpu, memory, null, scheduler, tag, tagRequired);
    }

    public void deleteVM(String name, String namespace) throws IOException {
        try {
            DeletionResponse deletionResponse = new OrkaClientFactory().getOrkaClient(this.endpoint,
                    this.credentialsId, this.httpTimeout, this.useJenkinsProxySettings, this.ignoreSSLErrors)
                    .deleteVM(name, namespace);

            if (deletionResponse.isSuccessful()) {
                logger.log(Level.INFO, "VM {0} is successfully deleted.", name);
                this.capacityHandler.removeRunningInstance();
            } else {
                logger.log(Level.WARNING, "Deleting VM {0} failed with: {1}", 
                    new Object[]{name, Utils.getErrorMessage(deletionResponse)});
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to delete a VM with name:" + name, ex);
        }
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
            logger.log(Level.INFO, "{0}Provisioning for label {1}. Workload: {2}", 
                new Object[]{provisionIdString, labelName, excessWorkload});

            AgentTemplate template = this.getTemplate(label);

            if (template == null) {
                logger.log(Level.FINE, "{0}Couldn''t find template for label {1}. Stopping provisioning.",
                    new Object[]{provisionIdString, labelName});
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

                logger.log(Level.FINE, "{0}Provisioning Node with template:", provisionIdString);
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
                    logger.log(Level.FINE, "{0}Adding Node to Jenkins:", provisionIdString);
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
        private OrkaClientFactory clientFactory = new OrkaClientFactory();
        private FormValidator formValidator = new FormValidator(this.clientFactory);

        @Override
        public String getDisplayName() {
            return "Orka Cloud";
        }

        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public int getDefaultHttpTimeout() {
            return defaultHttpTimeout;
        }

        public ListBoxModel doFillCredentialsIdItems() {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
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

        public FormValidation doCheckHttpTimeout(@QueryParameter String value) {
            try {
                int timeoutValue = Integer.parseInt(value);
                if (0 < timeoutValue && timeoutValue < recommendedMinTimeout) {
                    return FormValidation.warning(String.format(
                            "HTTP timeout less than %d seconds is not recommended.", recommendedMinTimeout));
                }

                if (timeoutValue <= 0) {
                    return FormValidation.error("HTTP timeout must be a positive number.");
                }

                return FormValidation.ok();
            } catch (NumberFormatException e) {
                return FormValidation.error("HTTP timeout must be a number.");
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
