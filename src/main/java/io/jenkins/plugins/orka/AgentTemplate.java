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
import hudson.slaves.RetentionStrategy;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.helpers.ClientFactory;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;
import io.jenkins.plugins.orka.helpers.OrkaRetentionStrategy;
import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class AgentTemplate implements Describable<AgentTemplate> {
    private static final Logger logger = Logger.getLogger(AgentTemplate.class.getName());
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
    private RetentionStrategy<?> retentionStrategy;
    private List<? extends NodeProperty<?>> nodeProperties;

    @Deprecated
    private transient int idleTerminationMinutes;

    private transient OrkaCloud parent;

    @DataBoundConstructor
    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, int numExecutors, String remoteFS, Mode mode, String labelString,
            RetentionStrategy<?> retentionStrategy, List<? extends NodeProperty<?>> nodeProperties) {
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
        this.retentionStrategy = retentionStrategy;
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

    public RetentionStrategy<?> getRetentionStrategy() {
        return this.retentionStrategy;
    }

    public List<? extends NodeProperty<?>> getNodeProperties() {
        return this.nodeProperties;
    }

    public Descriptor<AgentTemplate> getDescriptor() {
        return Jenkins.getInstance().getDescriptor(getClass());
    }

    public OrkaProvisionedAgent provision() throws IOException, FormException {
        this.ensureConfigurationExist();
        String vmName = this.createNewVMConfig ? this.configName : this.vm;

        logger.fine("Deploying VM with name " + vmName);
        DeploymentResponse response = this.parent.deployVM(vmName);

        logger.fine("Result deploying VM " + vmName + ":");
        logger.fine(response.toString());

        String host = this.parent.getRealHost(response.getHost());

        return new OrkaProvisionedAgent(this.parent.getDisplayName(), response.getId(), response.getHost(), host,
                response.getSSHPort(), this.vmCredentialsId, this.numExecutors, this.remoteFS, this.mode,
                this.labelString, this.retentionStrategy, this.nodeProperties);
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

    protected Object readResolve() {
        if (this.retentionStrategy == null) {
            this.retentionStrategy = new IdleTimeCloudRetentionStrategy(this.idleTerminationMinutes);
        }
        return this;
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

        public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() {
            return OrkaRetentionStrategy.getRetentionStrategyDescriptors();
        }
    }

    @Override
    public String toString() {
        return "AgentTemplate [baseImage=" + baseImage + ", configName=" + configName + ", createNewVMConfig="
                + createNewVMConfig + ", idleTerminationMinutes=" + idleTerminationMinutes + ", labelString="
                + labelString + ", mode=" + mode + ", nodeProperties=" + nodeProperties + ", numCPUs=" + numCPUs
                + ", numExecutors=" + numExecutors + ", parent=" + parent + ", remoteFS=" + remoteFS
                + ", retentionStrategy=" + retentionStrategy + ", vm=" + vm + ", vmCredentialsId=" + vmCredentialsId
                + "]";
    }
}