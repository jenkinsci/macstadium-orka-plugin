package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.RelativePath;
import hudson.Util;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Label;
import hudson.model.Node.Mode;
import hudson.model.Saveable;
import hudson.model.labels.LabelAtom;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.RetentionStrategy;
import hudson.util.DescribableList;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;
import io.jenkins.plugins.orka.helpers.OrkaRetentionStrategy;
import io.jenkins.plugins.orka.helpers.OrkaVerificationStrategyProvider;
import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
    private String namePrefix;
    private RetentionStrategy<?> retentionStrategy;
    private OrkaVerificationStrategy verificationStrategy;
    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties;
    private String jvmOptions;

    @Deprecated
    private transient int idleTerminationMinutes;

    private transient OrkaCloud parent;

    @DataBoundConstructor
    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, int numExecutors, String remoteFS, Mode mode, String labelString,
            String namePrefix, RetentionStrategy<?> retentionStrategy, OrkaVerificationStrategy verificationStrategy,
            List<? extends NodeProperty<?>> nodeProperties, String jvmOptions) {
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
        this.namePrefix = namePrefix;
        this.retentionStrategy = retentionStrategy;
        this.verificationStrategy = verificationStrategy;
        this.nodeProperties = new DescribableList<>(Saveable.NOOP, Util.fixNull(nodeProperties));
        this.jvmOptions = jvmOptions;
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

    public String getNamePrefix() {
        return this.namePrefix;
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

    public String getJvmOptions() {
        return this.jvmOptions;
    }

    public RetentionStrategy<?> getRetentionStrategy() {
        return this.retentionStrategy;
    }

    public OrkaVerificationStrategy getVerificationStrategy() {
        return this.verificationStrategy;
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
        return Objects.requireNonNull(this.nodeProperties);
    }

    public Descriptor<AgentTemplate> getDescriptor() {
        return Jenkins.get().getDescriptor(getClass());
    }

    public OrkaProvisionedAgent provision() throws IOException, FormException {
        ConfigurationResponse configurationResponse = this.ensureConfigurationExist();
        if (configurationResponse != null && !configurationResponse.isSuccessful()) {
            logger.warning("Creating VM configuration failed with: " + Utils.getErrorMessage(configurationResponse));
            return null;
        }

        String vmName = this.createNewVMConfig ? this.configName : this.vm;

        logger.fine("Deploying VM with name " + vmName);
        DeploymentResponse response = this.parent.deployVM(vmName);
        try {
            logger.fine("Result deploying VM " + vmName + ":");
            logger.fine(response.toString());

            if (!response.isSuccessful()) {
                logger.warning("Deploying VM failed with: " + Utils.getErrorMessage(response));
                return null;
            }

            String host = this.parent.getRealHost(response.getHost());
            String vmId = response.getId();

            return new OrkaProvisionedAgent(this.parent.getDisplayName(), this.namePrefix, vmId, response.getHost(), 
                    host, response.getSSHPort(), this.vmCredentialsId, this.numExecutors, this.remoteFS, this.mode,
                    this.labelString, this.retentionStrategy, this.verificationStrategy, 
                    this.nodeProperties, this.jvmOptions);
        } catch (Exception e) {
            logger.warning("Exception while creating provisioned agent. Deleting VM.");
            this.parent.deleteVM(response.getId());
            
            throw e;
        }
    }

    private ConfigurationResponse ensureConfigurationExist() throws IOException {
        if (this.createNewVMConfig) {
            boolean configExist = parent.getVMConfigs().stream()
                    .anyMatch(vm -> vm.getName().equalsIgnoreCase(configName));

            if (!configExist) {
                logger.fine("Creating config with name " + this.configName);
                return parent.createConfiguration(this.configName, this.configName, this.baseImage,
                        Constants.DEFAULT_CONFIG_NAME, this.numCPUs);
            }
        }
        return null;
    }

    void setParent(OrkaCloud parent) {
        this.parent = parent;
    }

    protected Object readResolve() {
        if (this.retentionStrategy == null) {
            this.retentionStrategy = new IdleTimeCloudRetentionStrategy(this.idleTerminationMinutes);
        }
        if (this.verificationStrategy == null) {
            this.verificationStrategy = new DefaultVerificationStrategy();
        }
        if (this.nodeProperties == null) {
            this.nodeProperties = new DescribableList<>(Saveable.NOOP, Collections.emptyList());
        }
        return this;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<AgentTemplate> {
        private OrkaClientProxyFactory clientProxyFactory = new OrkaClientProxyFactory();
        private FormValidator formValidator = new FormValidator(this.clientProxyFactory);
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(this.clientProxyFactory);

        @VisibleForTesting
        void setClientProxyFactory(OrkaClientProxyFactory clientProxyFactory) {
            this.clientProxyFactory = clientProxyFactory;
            this.formValidator = new FormValidator(this.clientProxyFactory);
            this.infoHelper = new OrkaInfoHelper(this.clientProxyFactory);
        }

        @POST
        public FormValidation doCheckConfigName(@QueryParameter String configName,
                @QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors,
                @QueryParameter boolean createNewVMConfig) {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return formValidator.doCheckConfigName(configName, endpoint, credentialsId, useJenkinsProxySettings,
            ignoreSSLErrors, createNewVMConfig);
        }

        public FormValidation doCheckNumExecutors(@QueryParameter String value) {
            return FormValidation.validatePositiveInteger(value);
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillNumCPUsItems() {
            return this.infoHelper.doFillNumCPUsItems();
        }

        @POST
        public ListBoxModel doFillVmItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors,
                @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillVmItems(endpoint, credentialsId, useJenkinsProxySettings, ignoreSSLErrors,
                    createNewVMConfig);
        }

        @POST
        public ListBoxModel doFillBaseImageItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors,
                @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillBaseImageItems(endpoint, credentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors, createNewVMConfig);
        }

        public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() {
            return OrkaRetentionStrategy.getRetentionStrategyDescriptors();
        }

        public static List<Descriptor<OrkaVerificationStrategy>> getVerificationStrategyDescriptors() {
            return OrkaVerificationStrategyProvider.getVerificationStrategyDescriptors();
        }

        public static Descriptor<OrkaVerificationStrategy> getDefaultVerificationDescriptor() {
            return OrkaVerificationStrategyProvider.getDefaultVerificationDescriptor();
        }

        public List<NodePropertyDescriptor> getNodePropertyDescriptors() {
            return NodePropertyDescriptor.for_(NodeProperty.all(), OrkaProvisionedAgent.class);
        }
    }

    @Override
    public String toString() {
        return "AgentTemplate [baseImage=" + baseImage + ", configName=" + configName + ", createNewVMConfig="
                + createNewVMConfig + ", idleTerminationMinutes=" + idleTerminationMinutes + ", labelString="
                + labelString + ", namePrefix=" + namePrefix + ", mode=" + mode + ", nodeProperties=" 
                + nodeProperties + ", numCPUs=" + numCPUs + ", numExecutors=" + numExecutors + ", parent=" + parent 
                + ", remoteFS=" + remoteFS + ", retentionStrategy=" + retentionStrategy + ", verificationStrategy=" 
                + verificationStrategy + ", vm=" + vm + ", vmCredentialsId=" + vmCredentialsId + "]";
    }
}