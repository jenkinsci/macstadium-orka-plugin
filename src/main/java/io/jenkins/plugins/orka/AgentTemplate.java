package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Label;
import hudson.model.Node.Mode;
import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProperty;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.helpers.ClientFactory;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class AgentTemplate implements Describable<AgentTemplate> {
    private String vmCredentialsId;
    private boolean createNewVMConfig;
    private String vm;
    private String configName;
    private String baseImage;
    private int numCPUs;
    private int numExecutors;
    private Mode mode;
    private String remoteFS;
    private String labelString;
    private int idleTerminationMinutes;
    private List<? extends NodeProperty<?>> nodeProperties;

    private transient OrkaCloud parent;

    @DataBoundConstructor
    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, int numExecutors, String remoteFS, Mode mode,
            String labelString, int idleTerminationMinutes, List<? extends NodeProperty<?>> nodeProperties) {
        this.vmCredentialsId = vmCredentialsId;
        this.vm = vm;
        this.createNewVMConfig = createNewVMConfig;
        this.configName = configName;
        this.baseImage = baseImage;
        this.numCPUs = numCPUs;
        this.numExecutors = numExecutors;
        this.remoteFS = remoteFS;
        this.mode = mode;
        this.labelString = labelString;
        this.idleTerminationMinutes = idleTerminationMinutes;
        this.nodeProperties = nodeProperties;
    }

    public String getOrkaCredentialsId() {
        return this.parent.getCredentialsId();
    }

    public String getOrkaEndpoint() {
        return this.parent.getEndpoint();
    }

    public String getVmCredentialsId() {
        return this.vmCredentialsId;
    }

    public boolean getCreateNewVMConfig() {
        return this.createNewVMConfig;
    }

    public String getVm() {
        return this.vm;
    }

    public String getConfigName() {
        return this.configName;
    }

    public String getBaseImage() {
        return this.baseImage;
    }

    public int getNumCPUs() {
        return this.numCPUs;
    }

    public String getLabelString() {
        return this.labelString;
    }

    public Set<LabelAtom> getLabelSet() {
        return Label.parse(this.getLabelString());
    }

    public int getNumExecutors() {
        return this.numExecutors;
    }

    public Mode getMode() {
        return this.mode;
    }

    public String getRemoteFS() {
        return this.remoteFS;
    }

    public int getIdleTerminationMinutes() {
        return this.idleTerminationMinutes;
    }

    public List<? extends NodeProperty<?>> getNodeProperties() {
        return this.nodeProperties;
    }

    public Descriptor<AgentTemplate> getDescriptor() {
        return Jenkins.getInstance().getDescriptor(getClass());
    }

    public OrkaProvisionedAgent provision(String node) throws IOException, FormException {
        this.ensureConfigurationExist();
        String vmName = this.createNewVMConfig ? this.configName : this.vm;
        DeploymentResponse response = this.parent.deployVM(vmName, node);

        return new OrkaProvisionedAgent(this.parent.getDisplayName(), response.getId(), node, response.getHost(),
                response.getSSHPort(), this.vmCredentialsId, this.numExecutors, this.remoteFS, this.mode,
                this.labelString, this.idleTerminationMinutes, this.nodeProperties);
    }

    private void ensureConfigurationExist() throws IOException {
        if (this.createNewVMConfig) {
            boolean configExist = parent.getVMs().stream().anyMatch(vm -> vm.getVMName().equalsIgnoreCase(configName));

            if (!configExist) {
                parent.createConfiguration(this.configName, this.configName, this.baseImage, 
                    Constants.DEFAULT_CONFIG_NAME, this.numCPUs);
            }
        }
    }

    void setParent(OrkaCloud parent) {
        this.parent = parent;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<AgentTemplate> {
        private ClientFactory clientFactory = new ClientFactory();
        private FormValidator formValidator = new FormValidator(this.clientFactory);
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(this.clientFactory);

        @VisibleForTesting
        void setClientFactory(ClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            this.formValidator = new FormValidator(this.clientFactory);
            this.infoHelper = new OrkaInfoHelper(this.clientFactory);
        }

        @POST
        public FormValidation doCheckConfigName(@QueryParameter String configName,
                @QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId, @QueryParameter boolean createNewVMConfig) {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            return formValidator.doCheckConfigName(configName, endpoint, credentialsId, createNewVMConfig);
        }

        public FormValidation doCheckNumExecutors(@QueryParameter String value) {
            return FormValidation.validatePositiveInteger(value);
        }

        public FormValidation doCheckIdleTerminationMinutes(@QueryParameter String value) {
            return this.formValidator.doCheckIdleTerminationMinutes(value);
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        @POST
        public ListBoxModel doFillVmItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId, @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillVmItems(endpoint, credentialsId, createNewVMConfig);
        }

        @POST
        public ListBoxModel doFillBaseImageItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId, @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillBaseImageItems(endpoint, credentialsId, createNewVMConfig);
        }
    }
}