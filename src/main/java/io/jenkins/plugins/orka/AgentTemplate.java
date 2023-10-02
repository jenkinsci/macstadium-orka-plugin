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
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;
import io.jenkins.plugins.orka.helpers.OrkaRetentionStrategy;
import io.jenkins.plugins.orka.helpers.OrkaVerificationStrategyProvider;
import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;
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
    private boolean useNetBoost;
    private boolean useGpuPassthrough;
    private String memory;
    private boolean overwriteTag;
    private String tag;
    private Boolean tagRequired;
    private int numExecutors;
    private Mode mode;
    private String remoteFS;
    private String labelString;
    private String namePrefix;
    private RetentionStrategy<?> retentionStrategy;
    private OrkaVerificationStrategy verificationStrategy;
    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties;
    private String jvmOptions;
    private String scheduler;

    private transient OrkaCloud parent;

    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, int numExecutors, String remoteFS, Mode mode, String labelString,
            String namePrefix, RetentionStrategy<?> retentionStrategy, OrkaVerificationStrategy verificationStrategy,
            List<? extends NodeProperty<?>> nodeProperties, String jvmOptions, String scheduler, String memory) {
        this(vmCredentialsId, vm, createNewVMConfig, configName, baseImage, numCPUs, false, false,
                numExecutors, remoteFS, mode, labelString, namePrefix, retentionStrategy, verificationStrategy,
                nodeProperties, jvmOptions, scheduler, memory, false, null, null);
    }

    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, boolean useNetBoost, int numExecutors,
            String remoteFS, Mode mode, String labelString, String namePrefix, RetentionStrategy<?> retentionStrategy,
            OrkaVerificationStrategy verificationStrategy, List<? extends NodeProperty<?>> nodeProperties,
            String jvmOptions, String scheduler, String memory) {
        this(vmCredentialsId, vm, createNewVMConfig, configName, baseImage, numCPUs, useNetBoost, false,
                numExecutors, remoteFS, mode, labelString, namePrefix, retentionStrategy, verificationStrategy,
                nodeProperties, jvmOptions, scheduler, memory, false, null, null);
    }

    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, boolean useNetBoost, int numExecutors,
            String remoteFS, Mode mode, String labelString, String namePrefix, RetentionStrategy<?> retentionStrategy,
            OrkaVerificationStrategy verificationStrategy, List<? extends NodeProperty<?>> nodeProperties,
            String jvmOptions, String scheduler, String memory, boolean overwriteTag, String tag,
            Boolean tagRequired) {
        this(vmCredentialsId, vm, createNewVMConfig, configName, baseImage, numCPUs, useNetBoost, false,
                numExecutors, remoteFS, mode, labelString, namePrefix, retentionStrategy, verificationStrategy,
                nodeProperties, jvmOptions, scheduler, memory, overwriteTag, tag, tagRequired);
    }

    @DataBoundConstructor
    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, boolean useNetBoost, boolean useGpuPassthrough, int numExecutors,
            String remoteFS, Mode mode, String labelString, String namePrefix, RetentionStrategy<?> retentionStrategy,
            OrkaVerificationStrategy verificationStrategy, List<? extends NodeProperty<?>> nodeProperties,
            String jvmOptions, String scheduler, String memory, boolean overwriteTag, String tag,
            Boolean tagRequired) {
        this.vmCredentialsId = vmCredentialsId;
        this.vm = vm;
        this.createNewVMConfig = createNewVMConfig;
        this.configName = configName;
        this.baseImage = baseImage;
        this.numCPUs = numCPUs;
        this.useNetBoost = useNetBoost;
        this.useGpuPassthrough = useGpuPassthrough;
        this.numExecutors = numExecutors;
        this.remoteFS = remoteFS;
        this.mode = mode;
        this.labelString = labelString;
        this.namePrefix = namePrefix;
        this.retentionStrategy = retentionStrategy;
        this.verificationStrategy = verificationStrategy;
        this.nodeProperties = new DescribableList<>(Saveable.NOOP, Util.fixNull(nodeProperties));
        this.jvmOptions = jvmOptions;
        this.scheduler = scheduler;
        this.memory = memory;
        this.overwriteTag = overwriteTag;
        this.tag = this.overwriteTag ? tag : null;
        this.tagRequired = this.overwriteTag ? tagRequired : null;
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

    public boolean getUseNetBoost() {
        return this.useNetBoost;
    }

    public boolean getUseGpuPassthrough() {
        return this.useGpuPassthrough;
    }

    public String getMemory() {
        return this.memory;
    }

    public boolean getOverwriteTag() {
        return this.overwriteTag;
    }

    public String getTag() {
        return this.tag;
    }

    public Boolean getTagRequired() {
        return this.tagRequired;
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

    public String getScheduler() {
        return this.scheduler;
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
        DeploymentResponse response = this.parent.deployVM(vmName, this.getScheduler(), this.getTag(),
                this.getTagRequired());

        try {
            logger.fine("Result deploying VM " + vmName + ":");
            logger.fine(response.toString());

            if (!response.isSuccessful()) {
                logger.warning("Deploying VM failed with: " + Utils.getErrorMessage(response));
                return null;
            }

            String host = this.parent.getRealHost(response.getIP());
            String vmId = response.getName();

            return new OrkaProvisionedAgent(this.parent.getDisplayName(), this.namePrefix, vmId, response.getIP(),
                    host, response.getSSH(), this.vmCredentialsId, this.numExecutors, this.remoteFS, this.mode,
                    this.labelString, this.retentionStrategy, this.verificationStrategy,
                    this.nodeProperties, this.jvmOptions);
        } catch (Exception e) {
            logger.warning("Exception while creating provisioned agent. Deleting VM.");
            this.parent.deleteVM(response.getName());

            throw e;
        }
    }

    private ConfigurationResponse ensureConfigurationExist() throws IOException {
        if (this.createNewVMConfig) {
            boolean configExist = parent.getVMConfigs().stream()
                    .anyMatch(vm -> vm.getName().equalsIgnoreCase(configName));

            if (!configExist) {
                logger.fine("Creating config with name " + this.configName);
                return parent.createConfiguration(this.configName, this.baseImage,
                        this.numCPUs, this.useNetBoost, this.useGpuPassthrough,
                        this.scheduler, this.memory, this.tag, this.tagRequired);
            }
        }
        return null;
    }

    void setParent(OrkaCloud parent) {
        this.parent = parent;
    }

    protected Object readResolve() {
        if (this.retentionStrategy == null) {
            this.retentionStrategy = new IdleTimeCloudRetentionStrategy(30);
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
        private OrkaClientFactory clientFactory = new OrkaClientFactory();
        private FormValidator formValidator = new FormValidator(this.clientFactory);
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(this.clientFactory);

        @VisibleForTesting
        void setclientFactory(OrkaClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            this.formValidator = new FormValidator(this.clientFactory);
            this.infoHelper = new OrkaInfoHelper(this.clientFactory);
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

        @POST
        public FormValidation doCheckMemory(@QueryParameter String memory) {
            return this.formValidator.doCheckMemory(memory);
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

        public ListBoxModel doFillSchedulerItems() {
            return this.infoHelper.doFillSchedulerItems();
        }

        @POST
        public ListBoxModel doFillVmItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors,
                @QueryParameter boolean createNewVMConfig) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillVmItems(endpoint, credentialsId, useJenkinsProxySettings, ignoreSSLErrors,
                    createNewVMConfig);
        }

        @POST
        public ListBoxModel doFillBaseImageItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors,
                @QueryParameter boolean createNewVMConfig, @QueryParameter String readonlyBaseImage) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            ListBoxModel model = this.infoHelper.doFillBaseImageItems(endpoint, credentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors, createNewVMConfig);

            if (StringUtils.isNotBlank(readonlyBaseImage)) {
                if (!model.stream().filter(o -> o.value.equals(readonlyBaseImage)).findAny().isPresent()) {
                    String sanitizedName = Utils.sanitizeK8sName(readonlyBaseImage);
                    Optional<ListBoxModel.Option> existingImage = model.stream()
                            .filter(o -> o.value.equals(sanitizedName))
                            .findAny();
                    if (existingImage.isPresent()) {
                        existingImage.get().selected = true;
                    } else {
                        model.add(readonlyBaseImage);
                    }
                }
            }

            return model;
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
                + createNewVMConfig + ", labelString="
                + labelString + ", namePrefix=" + namePrefix + ", mode=" + mode + ", nodeProperties="
                + nodeProperties + ", numCPUs=" + numCPUs + ", memory=" + memory + ", overwriteTag="
                + overwriteTag + ", tag=" + tag + ", tagRequired=" + tagRequired + ", numExecutors="
                + numExecutors + ", parent=" + parent + ", remoteFS=" + remoteFS + ", retentionStrategy="
                + retentionStrategy + ", verificationStrategy=" + verificationStrategy + ", vm=" + vm
                + ", vmCredentialsId=" + vmCredentialsId + " scheduler=" + scheduler + "]";
    }
}
