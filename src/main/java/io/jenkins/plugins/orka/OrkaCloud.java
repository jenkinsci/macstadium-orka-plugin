package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner.PlannedNode;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.VMResponse;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.OrkaClientProxy;
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

import org.kohsuke.stapler.DataBoundConstructor;

public class OrkaCloud extends Cloud {
    private static final Logger logger = Logger.getLogger(OrkaCloud.class.getName());

    private String credentialsId;
    private String endpoint;
    private OrkaClientProxy client;

    private List<? extends AddressMapper> mappings;
    private final List<? extends AgentTemplate> templates;

    @VisibleForTesting
    public OrkaCloud(String name, List<? extends AgentTemplate> templates) throws IOException {
        super(name);
        this.templates = templates == null ? Collections.emptyList() : templates;
    }

    @DataBoundConstructor
    public OrkaCloud(String name, String credentialsId, String endpoint, List<? extends AddressMapper> mappings,
            List<? extends AgentTemplate> templates) throws IOException {
        super(name);

        this.credentialsId = credentialsId;
        this.endpoint = endpoint;

        this.mappings = mappings;
        this.templates = templates == null ? Collections.emptyList() : templates;

        readResolve();
    }

    protected Object readResolve() throws IOException {
        this.templates.forEach(t -> t.setParent(this));
        this.mappings = this.mappings == null ? Collections.emptyList() : this.mappings;
        if (this.client == null) {
            this.client = new OrkaClientProxyFactory().getOrkaClientProxy(this.endpoint, this.credentialsId);
        }

        return this;
    }

    public String getCredentialsId() {
        return this.credentialsId;
    }

    public String getEndpoint() {
        return this.endpoint;
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

    public VMResponse getVMs() throws IOException {
        return this.client.getVMs();
    }

    public ConfigurationResponse createConfiguration(String name, String image, String baseImage, String configTemplate,
            int cpuCount) throws IOException {
        return this.client.createConfiguration(name, image, baseImage, configTemplate, cpuCount);
    }

    public DeploymentResponse deployVM(String name) throws IOException {
        return this.client.deployVM(name);
    }

    public void deleteVM(String name) throws IOException {
        this.client.deleteVM(name);
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
            logger.fine(provisionIdString + "VMs to provision: " + vmsToProvision);

            return IntStream.range(0, vmsToProvision).mapToObj(i -> {
                String nodeName = UUID.randomUUID().toString();
                Callable<Node> provisionNodeCallable = this.provisionNode(template, provisionIdString);
                Future<Node> provisionNodeTask = Computer.threadPoolForRemoting.submit(provisionNodeCallable);

                return new PlannedNode(nodeName, provisionNodeTask, template.getNumExecutors());
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.WARNING, provisionIdString + "Exception during provisioning", e);
        }

        return Collections.emptyList();
    }

    private Callable<Node> provisionNode(AgentTemplate template, String provisionIdString) {
        return new Callable<Node>() {
            @Override
            public Node call() throws Exception {

                logger.fine(provisionIdString + "Provisioning Node with template:");
                logger.fine(template.toString());

                OrkaProvisionedAgent agent = template.provision();
                if (agent != null) {
                    logger.fine(provisionIdString + "Adding Node to Jenkins:");
                    logger.fine(agent.toString());
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
        @Override
        public String getDisplayName() {
            return "Orka Cloud";
        }

        public ListBoxModel doFillCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }
    }
}
