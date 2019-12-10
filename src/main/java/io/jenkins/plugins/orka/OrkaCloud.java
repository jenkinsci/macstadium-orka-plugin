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
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.VMResponse;
import io.jenkins.plugins.orka.helpers.ClientFactory;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.ProvisioningHelper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class OrkaCloud extends Cloud {
    private static final Logger logger = Logger.getLogger(OrkaCloud.class.getName());
    private static final int launchWaitTime = 15;

    private String credentialsId;
    private String endpoint;

    private List<? extends AddressMapper> mappings;
    private final List<? extends AgentTemplate> templates;

    @DataBoundConstructor
    public OrkaCloud(String name, String credentialsId, String endpoint, List<? extends AddressMapper> mappings,
            List<? extends AgentTemplate> templates) {
        super(name);

        this.credentialsId = credentialsId;
        this.endpoint = endpoint;

        this.mappings = mappings;
        this.templates = templates == null ? Collections.emptyList() : templates;

        readResolve();
    }

    protected Object readResolve() {
        this.templates.forEach(t -> t.setParent(this));
        this.mappings = this.mappings == null ? Collections.emptyList() : this.mappings;

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

    public List<VMResponse> getVMs() throws IOException {
        OrkaClient client = new ClientFactory().getOrkaClient(this.endpoint, this.credentialsId);
        return client.getVMs();
    }

    public ConfigurationResponse createConfiguration(String name, String image, String baseImage, String configTemplate,
            int cpuCount) throws IOException {
        OrkaClient client = new ClientFactory().getOrkaClient(this.endpoint, this.credentialsId);
        return client.createConfiguration(name, image, baseImage, configTemplate, cpuCount);
    }

    public DeploymentResponse deployVM(String name, String node) throws IOException {
        OrkaClient client = new ClientFactory().getOrkaClient(this.endpoint, this.credentialsId);
        return client.deployVM(name, node);
    }

    public void deleteVM(String name, String node) throws IOException {
        OrkaClient client = new ClientFactory().getOrkaClient(this.endpoint, this.credentialsId);
        client.deleteVM(name, node);
    }

    public String getRealHost(String host) {
        return this.mappings.stream().filter(m -> m.getDefaultHost().equalsIgnoreCase(host)).findFirst()
                .map(m -> m.getRedirectHost()).orElse(host);
    }

    @Override
    public Collection<PlannedNode> provision(final Label label, int excessWorkload) {
        try {
            logger.info("Provisioning for label " + label.getName() + ". Workload: " + excessWorkload);

            AgentTemplate template = this.getTemplate(label);

            if (template == null) {
                logger.log(Level.INFO,
                        "Couldn't find template for label " + label.getName() + ". Stopping provisioning.");
                return Collections.emptyList();
            }

            int vmsToProvision = Math.max(excessWorkload / template.getNumExecutors(), 1);

            List<OrkaNode> freeNodes = getFreeNodes(vmsToProvision, template.getNumCPUs(), this.endpoint,
                    this.credentialsId);

            int possibleVMsToProvision = freeNodes.stream().mapToInt(n -> n.getVmCapacity()).sum();

            if (possibleVMsToProvision < vmsToProvision) {
                logger.info("There are not enough free nodes. Provisioning " + possibleVMsToProvision + " agents");
            }

            return freeNodes.stream().flatMap(node -> {
                return IntStream.range(0, node.getVmCapacity()).mapToObj(i -> {
                    Callable<Node> provisionNodeCallable = this.provisionNode(template, node);
                    Future<Node> provisionNodeTask = Computer.threadPoolForRemoting.submit(provisionNodeCallable);
                    return new PlannedNode(node.getName(), provisionNodeTask, template.getNumExecutors());
                });
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception during provisioning", e);
        }

        return Collections.emptyList();
    }

    private Callable<Node> provisionNode(AgentTemplate template, OrkaNode node) {
        return new Callable<Node>() {
            @Override
            public Node call() throws Exception {
                OrkaProvisionedAgent agent = template.provision(node.getName());
                Thread.sleep(TimeUnit.SECONDS.toMillis(launchWaitTime));
                Jenkins.getInstance().addNode(agent);
                return agent;
            }
        };
    }

    @VisibleForTesting
    List<OrkaNode> getFreeNodes(int vmsToProvision, int requiredCPU, String endpoint, String credentialsId) {
        try {
            ProvisioningHelper provisioningHelper = new ProvisioningHelper(endpoint, credentialsId);
            return provisioningHelper.getFreeNodes(vmsToProvision, requiredCPU);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't create provisioning helper. No free nodes found.", e);
        }
        return Collections.emptyList();
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
