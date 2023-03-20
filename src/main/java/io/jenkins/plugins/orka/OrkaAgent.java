package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;

import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class OrkaAgent extends AbstractCloudSlave {
    private static final long serialVersionUID = 6363583313270146174L;

    public String orkaCredentialsId;
    public String orkaEndpoint;
    public String vmCredentialsId;
    private boolean useJenkinsProxySettings;
    private boolean ignoreSSLErrors;
    private boolean createNewVMConfig;
    private String vm;
    private String node;
    private String configName;
    private String baseImage;
    private int numCPUs;
    private boolean useNetBoost;
    private boolean useGpuPassthrough;
    private String memory;
    private boolean overwriteTag;
    private String tag;
    private Boolean tagRequired;
    private String jvmOptions;

    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, int numExecutors, String host, int port, String remoteFS)
            throws Descriptor.FormException, IOException {

        this(name, orkaCredentialsId, orkaEndpoint, vmCredentialsId, vm, node, redirectHost, createNewVMConfig,
                configName, baseImage, numCPUs, numExecutors, host, port, remoteFS, false, false, null);
    }

    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, int numExecutors, String host, int port, String remoteFS,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors)
            throws Descriptor.FormException, IOException {

        this(name, orkaCredentialsId, orkaEndpoint, vmCredentialsId, vm, node, redirectHost, createNewVMConfig,
                configName, baseImage, numCPUs, numExecutors, host, port, remoteFS,
                useJenkinsProxySettings, ignoreSSLErrors, null);
    }

    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, int numExecutors, String host, int port, String remoteFS,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String jvmOptions)
            throws Descriptor.FormException, IOException {

        this(name, orkaCredentialsId, orkaEndpoint, vmCredentialsId, vm, node, redirectHost, createNewVMConfig,
                configName, baseImage, numCPUs, numExecutors, host, port, remoteFS,
                useJenkinsProxySettings, ignoreSSLErrors, jvmOptions, "auto");
    }

    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, int numExecutors, String host, int port, String remoteFS,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String jvmOptions, String memory)
            throws Descriptor.FormException, IOException {
        this(name, orkaCredentialsId, orkaEndpoint, vmCredentialsId, vm, node, redirectHost, createNewVMConfig,
                configName, baseImage, numCPUs, false, false, numExecutors, host, port, remoteFS,
                useJenkinsProxySettings, ignoreSSLErrors, jvmOptions, memory);
    }

    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, boolean useNetBoost, int numExecutors, String host, 
            int port, String remoteFS, boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String jvmOptions, 
            String memory)
            throws Descriptor.FormException, IOException {
        this(name, orkaCredentialsId, orkaEndpoint, vmCredentialsId, vm, node, redirectHost, createNewVMConfig,
                configName, baseImage, numCPUs, useNetBoost, false, numExecutors, host, port, remoteFS,
                useJenkinsProxySettings, ignoreSSLErrors, jvmOptions, memory, false, null, null);
    }

    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, boolean useNetBoost, boolean useGpuPassthrough, int numExecutors, String host, 
            int port, String remoteFS, boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String jvmOptions, 
            String memory)
            throws Descriptor.FormException, IOException {
        this(name, orkaCredentialsId, orkaEndpoint, vmCredentialsId, vm, node, redirectHost, createNewVMConfig,
                configName, baseImage, numCPUs, useNetBoost, useGpuPassthrough, numExecutors, host, port, remoteFS,
                useJenkinsProxySettings, ignoreSSLErrors, jvmOptions, memory, false, null, null);
    }

    @DataBoundConstructor
    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, boolean useNetBoost, boolean useGpuPassthrough, int numExecutors, String host, 
            int port, String remoteFS, boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String jvmOptions, 
            String memory, boolean overwriteTag, String tag, Boolean tagRequired)
            throws Descriptor.FormException, IOException {
        super(name, remoteFS, new OrkaComputerLauncher(host, port, redirectHost, jvmOptions));

        this.orkaCredentialsId = orkaCredentialsId;
        this.orkaEndpoint = orkaEndpoint;
        this.vmCredentialsId = vmCredentialsId;
        this.vm = vm;
        this.node = node;
        this.createNewVMConfig = createNewVMConfig;
        this.configName = configName;
        this.baseImage = baseImage;
        this.numCPUs = numCPUs;
        this.useNetBoost = useNetBoost;
        this.useGpuPassthrough = useGpuPassthrough;
        this.useJenkinsProxySettings = useJenkinsProxySettings;
        this.ignoreSSLErrors = ignoreSSLErrors;
        this.jvmOptions = jvmOptions;
        this.memory = memory;
        this.overwriteTag = overwriteTag;
        this.tag = this.overwriteTag ? tag : null;
        this.tagRequired = this.overwriteTag ? tagRequired : null;
        this.setNumExecutors(numExecutors);
    }

    public String getOrkaCredentialsId() {
        return this.orkaCredentialsId;
    }

    public String getOrkaEndpoint() {
        return this.orkaEndpoint;
    }

    public String getVmCredentialsId() {
        return this.vmCredentialsId;
    }

    public boolean getUseJenkinsProxySettings() {
        return this.useJenkinsProxySettings;
    }

    public boolean getIgnoreSSLErrors() {
        return this.ignoreSSLErrors;
    }

    public boolean getCreateNewVMConfig() {
        return this.createNewVMConfig;
    }

    public String getVm() {
        return this.vm;
    }

    public String getNode() {
        return this.node;
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

    public String getJvmOptions() {
        return this.jvmOptions;
    }

    @Override
    public AbstractCloudComputer createComputer() {
        return new OrkaComputer(this);
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {
        private OrkaClientProxyFactory clientProxyFactory = new OrkaClientProxyFactory();
        private FormValidator formValidator = new FormValidator(clientProxyFactory);
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(clientProxyFactory);

        public DescriptorImpl() {
            load();
        }

        @VisibleForTesting
        void setClientProxyFactory(OrkaClientProxyFactory clientProxyFactory) {
            this.clientProxyFactory = clientProxyFactory;
            this.formValidator = new FormValidator(this.clientProxyFactory);
            this.infoHelper = new OrkaInfoHelper(this.clientProxyFactory);
        }

        public String getDisplayName() {
            return "Agent running under Orka by MacStadium";
        }

        @Override
        public boolean isInstantiable() {
            return true;
        }

        @POST
        public FormValidation doCheckConfigName(@QueryParameter String configName, @QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors, @QueryParameter boolean createNewVMConfig) {

            return this.formValidator.doCheckConfigName(configName, orkaEndpoint, orkaCredentialsId,
                    useJenkinsProxySettings, ignoreSSLErrors, createNewVMConfig);
        }

        @POST
        public FormValidation doCheckNode(@QueryParameter String value, @QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors, @QueryParameter String vm,
                @QueryParameter boolean createNewVMConfig, @QueryParameter int numCPUs) {

            return this.formValidator.doCheckNode(value, orkaEndpoint, orkaCredentialsId,
                    useJenkinsProxySettings, ignoreSSLErrors, vm, createNewVMConfig, numCPUs);
        }

        @POST
        public FormValidation doCheckMemory(@QueryParameter String memory) {
            return this.formValidator.doCheckMemory(memory);
        }

        public ListBoxModel doFillOrkaCredentialsIdItems() {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        @POST
        public ListBoxModel doFillNodeItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors) {

            return this.infoHelper.doFillNodeItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors);
        }

        public ListBoxModel doFillNumCPUsItems() {
            return this.infoHelper.doFillNumCPUsItems();
        }

        @POST
        public ListBoxModel doFillVmItems(@QueryParameter String orkaEndpoint, @QueryParameter String orkaCredentialsId,
                @QueryParameter boolean useJenkinsProxySettings, @QueryParameter boolean ignoreSSLErrors,
                @QueryParameter boolean createNewVMConfig) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillVmItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors, createNewVMConfig);
        }

        @POST
        public ListBoxModel doFillBaseImageItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors, @QueryParameter boolean createNewVMConfig) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillBaseImageItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors, createNewVMConfig);
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter String orkaCredentialsId,
                @QueryParameter String orkaEndpoint, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors)
                throws IOException {

            return this.formValidator.doTestConnection(orkaCredentialsId, orkaEndpoint, useJenkinsProxySettings,
                    ignoreSSLErrors);
        }
    }
}
