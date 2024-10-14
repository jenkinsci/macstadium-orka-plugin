package io.jenkins.plugins.orka;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

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
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;
import io.jenkins.plugins.orka.helpers.OrkaRetentionStrategy;
import io.jenkins.plugins.orka.helpers.Utils;
import jenkins.model.Jenkins;

public class AgentTemplate implements Describable<AgentTemplate> {
    private static final Logger logger = Logger.getLogger(AgentTemplate.class.getName());
    private static final String orka3xOption = "orka3xOption";
    private static final String orka2xOption = "orka2xOption";
    private String vmCredentialsId;

    private String namePrefix;
    private String image;
    private Integer cpu;
    private String memory;
    private String namespace;
    private boolean useNetBoost;
    private boolean useGpuPassthrough;
    private String scheduler;
    private String config;
    private String tag;
    private Boolean tagRequired;

    private String legacyConfigScheduler;
    private String legacyConfigTag;
    private Boolean legacyConfigTagRequired;

    private String deploymentOption;

    private int numExecutors;
    private Mode mode;
    private String remoteFS;
    private String labelString;

    private RetentionStrategy<?> retentionStrategy;

    private DescribableList<NodeProperty<?>, NodePropertyDescriptor> nodeProperties;
    private String jvmOptions;

    private transient OrkaCloud parent;

    private transient String vm;
    private transient String baseImage;
    private transient int numCPUs;
    private transient boolean createNewVMConfig;

    @Deprecated
    public AgentTemplate(String vmCredentialsId, String vm, boolean createNewVMConfig, String configName,
            String baseImage, int numCPUs, boolean useNetBoost, boolean useGpuPassthrough, int numExecutors,
            String remoteFS, Mode mode, String labelString, String namePrefix, RetentionStrategy<?> retentionStrategy,
            OrkaVerificationStrategy verificationStrategy, List<? extends NodeProperty<?>> nodeProperties,
            String jvmOptions, String scheduler, String memory, boolean overwriteTag, String tag,
            Boolean tagRequired) {

        this(vmCredentialsId, createNewVMConfig ? orka3xOption : orka2xOption, namePrefix, baseImage, numCPUs, memory,
                Constants.DEFAULT_NAMESPACE, useNetBoost,
                useGpuPassthrough,
                scheduler,
                tag,
                tagRequired,
                vm,
                scheduler,
                tag,
                tagRequired,
                numExecutors, mode, remoteFS, labelString, retentionStrategy,
                nodeProperties, jvmOptions);
    }

    @DataBoundConstructor
    public AgentTemplate(String vmCredentialsId, String deploymentOption, String namePrefix, String image, int cpu,
            String memory,
            String namespace, boolean useNetBoost, boolean useGpuPassthrough, String scheduler, String tag,
            Boolean tagRequired, String config, String legacyConfigScheduler,
            String legacyConfigTag, boolean legacyConfigTagRequired, int numExecutors, Mode mode,
            String remoteFS,
            String labelString, RetentionStrategy<?> retentionStrategy, List<? extends NodeProperty<?>> nodeProperties,
            String jvmOptions) {

        this.vmCredentialsId = vmCredentialsId;
        this.namePrefix = namePrefix;
        this.namespace = namespace;
        this.labelString = labelString;
        this.numExecutors = numExecutors;
        this.mode = mode;
        this.remoteFS = remoteFS;
        this.retentionStrategy = retentionStrategy;
        this.nodeProperties = new DescribableList<>(Saveable.NOOP, Util.fixNull(nodeProperties));
        this.jvmOptions = jvmOptions;
        this.deploymentOption = deploymentOption;

        this.config = config;
        this.legacyConfigScheduler = legacyConfigScheduler;
        this.legacyConfigTag = legacyConfigTag;
        this.legacyConfigTagRequired = legacyConfigTagRequired;

        this.image = image;
        this.cpu = cpu;
        this.memory = memory;
        this.useNetBoost = useNetBoost;
        this.useGpuPassthrough = useGpuPassthrough;
        this.scheduler = scheduler;
        this.tag = tag;
        this.tagRequired = tagRequired;
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

    public String getConfig() {
        return this.config;
    }

    public String getImage() {
        return this.image;
    }

    public Integer getCpu() {
        return this.cpu;
    }

    public String getMemory() {
        return this.memory;
    }

    public boolean isUseNetBoost() {
        return this.useNetBoost;
    }

    public boolean isUseGpuPassthrough() {
        return this.useGpuPassthrough;
    }

    public String getScheduler() {
        return this.scheduler;
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

    public String getDeploymentOption() {
        return this.deploymentOption;
    }

    public String getLegacyConfigScheduler() {
        return this.legacyConfigScheduler;
    }

    public String getLegacyConfigTag() {
        return this.legacyConfigTag;
    }

    public Boolean getLegacyConfigTagRequired() {
        return this.legacyConfigTagRequired;
    }

    public RetentionStrategy<?> getRetentionStrategy() {
        return this.retentionStrategy;
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
        return Objects.requireNonNull(this.nodeProperties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<AgentTemplate> getDescriptor() {
        return Jenkins.get().getDescriptor(getClass());
    }

    public OrkaProvisionedAgent provision() throws IOException, FormException {
        String vmDeployID = "[deployID=" + UUID.randomUUID().toString() + "] ";
        logger.log(Level.FINE, "Deploying VM for label {0}{1}", new Object[]{this.labelString, vmDeployID});
        DeploymentResponse response = this.deployVM(vmDeployID);

        try {
            logger.log(Level.FINE, "Result deploying VM with label {0}{1}:", new Object[]{this.labelString, vmDeployID});
            logger.fine(response.toString());

            if (!response.isSuccessful()) {
                logger.log(Level.WARNING, "Deploying VM failed with: {0}", Utils.getErrorMessage(response));
                return null;
            }

            String host = this.parent.getRealHost(response.getIP());
            String vmId = response.getName();

            return new OrkaProvisionedAgent(this.parent.getDisplayName(), vmId, response.getIP(),
                    host, response.getSSH(), this.namespace, this.vmCredentialsId, this.numExecutors, this.remoteFS,
                    this.mode,
                    this.labelString, this.retentionStrategy,
                    this.nodeProperties, this.jvmOptions);
        } catch (Exception e) {
            logger.warning("Exception while creating provisioned agent. Deleting VM.");
            this.parent.deleteVM(response.getName(), this.namespace);

            throw e;
        }
    }

    private DeploymentResponse deployVM(String vmDeployID) throws IOException {
        if (StringUtils.equals(deploymentOption, orka2xOption)) {
            logger.log(Level.FINE, "Using Orka 2x deployment for ID:{0}", vmDeployID);
            return this.parent.deployVM(this.namespace, this.namePrefix, this.config, null, null, null,
                    this.legacyConfigScheduler, this.legacyConfigTag, this.legacyConfigTagRequired);
        }
        logger.fine("Using Orka 3x deployment");
        return this.parent.deployVM(this.namespace, this.namePrefix, null, this.image,
                this.cpu, this.memory, this.scheduler, this.tag, this.tagRequired);
    }

    void setParent(OrkaCloud parent) {
        this.parent = parent;
    }

    protected Object readResolve() {
        if (this.retentionStrategy == null) {
            this.retentionStrategy = new IdleTimeCloudRetentionStrategy(30);
        }
        if (this.nodeProperties == null) {
            this.nodeProperties = new DescribableList<>(Saveable.NOOP, Collections.emptyList());
        }
        if (StringUtils.isBlank(this.namespace)) {
            this.namespace = Constants.DEFAULT_NAMESPACE;
        }
        if (this.createNewVMConfig && StringUtils.isNotBlank(this.baseImage)) {
            this.image = this.baseImage;
            this.cpu = this.numCPUs;
        }
        if (!this.createNewVMConfig && StringUtils.isNotBlank(vm)) {
            this.config = this.vm;
            this.legacyConfigTagRequired = tagRequired;
            this.legacyConfigTag = tag;
            this.legacyConfigScheduler = scheduler;
            this.deploymentOption = orka2xOption;
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
        public FormValidation doCheckMemory(@QueryParameter String value) {
            return this.formValidator.doCheckMemory(value);
        }

        public FormValidation doCheckNumExecutors(@QueryParameter String value) {
            return FormValidation.validatePositiveInteger(value);
        }

        @POST
        public FormValidation doCheckNamespace(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors, @QueryParameter String value) {
            return this.formValidator.doCheckNamespace(endpoint, credentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors, value);
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillSchedulerItems() {
            return this.infoHelper.doFillSchedulerItems();
        }

        @POST
        public ListBoxModel doFillConfigItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillVmItems(endpoint, credentialsId, useJenkinsProxySettings, ignoreSSLErrors);
        }

        @POST
        public ListBoxModel doFillImageItems(@QueryParameter @RelativePath("..") String endpoint,
                @QueryParameter @RelativePath("..") String credentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors,
                @QueryParameter String readonlyImage) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            ListBoxModel model = this.infoHelper.doFillBaseImageItems(endpoint, credentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors);

            if (StringUtils.isNotBlank(readonlyImage)) {
                if (!model.stream().filter(o -> readonlyImage.equals(o.value)).findAny().isPresent()) {
                    String sanitizedName = Utils.sanitizeK8sName(readonlyImage);
                    Optional<ListBoxModel.Option> existingImage = model.stream()
                            .filter(o -> sanitizedName.equals(o.value))
                            .findAny();
                    if (existingImage.isPresent()) {
                        existingImage.get().selected = true;
                    } else {
                        model.add(readonlyImage);
                    }
                }
            }

            return model;
        }

        public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() {
            return OrkaRetentionStrategy.getRetentionStrategyDescriptors();
        }

        public List<NodePropertyDescriptor> getNodePropertyDescriptors() {
            return NodePropertyDescriptor.for_(NodeProperty.all(), OrkaProvisionedAgent.class);
        }

        public String getDefaultNamespace() {
            return Constants.DEFAULT_NAMESPACE;
        }

        public String getOrka2xOption() {
            return AgentTemplate.orka2xOption;
        }

        public String getOrka3xOption() {
            return AgentTemplate.orka3xOption;
        }
    }

    @Override
    public String toString() {
        return "AgentTemplate [namePrefix=" + namePrefix + ", image=" + image + ", cpu=" + cpu + ", memory=" + memory
                + ", namespace=" + namespace + ", useNetBoost=" + useNetBoost + ", useGpuPassthrough="
                + useGpuPassthrough + ", scheduler=" + scheduler + ", config=" + config + ", tag=" + tag
                + ", tagRequired=" + tagRequired + ", legacyConfigScheduler=" + legacyConfigScheduler
                + ", legacyConfigTag=" + legacyConfigTag + ", legacyConfigTagRequired=" + legacyConfigTagRequired
                + ", deploymentOption=" + deploymentOption + ", numExecutors="
                + numExecutors + ", mode=" + mode + ", remoteFS=" + remoteFS + ", labelString=" + labelString
                + ", retentionStrategy=" + retentionStrategy + "]";
    }
}
