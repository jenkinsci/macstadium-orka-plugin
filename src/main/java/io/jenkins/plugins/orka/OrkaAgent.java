package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.RelativePath;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
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
    private String node;
    private String namespace;
    private String image;
    private Integer cpu;
    private boolean useNetBoost;
    private boolean useGpuPassthrough;
    private String memory;
    private String tag;
    private Boolean tagRequired;
    private String namePrefix;
    private String jvmOptions;

    @DataBoundConstructor
    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId,
            String node, String namespace, String namePrefix, String redirectHost, String image,
            Integer cpu, boolean useNetBoost, boolean useGpuPassthrough, int numExecutors, String host,
            int port, String remoteFS, boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String jvmOptions,
            String memory, String tag, Boolean tagRequired)
            throws Descriptor.FormException, IOException {
        super(name, remoteFS, new OrkaComputerLauncher(host, port, redirectHost, jvmOptions));

        this.orkaCredentialsId = orkaCredentialsId;
        this.orkaEndpoint = orkaEndpoint;
        this.vmCredentialsId = vmCredentialsId;
        this.namespace = namespace;
        this.namePrefix = namePrefix;
        this.node = node;
        this.image = image;
        this.cpu = cpu;
        this.useNetBoost = useNetBoost;
        this.useGpuPassthrough = useGpuPassthrough;
        this.useJenkinsProxySettings = useJenkinsProxySettings;
        this.ignoreSSLErrors = ignoreSSLErrors;
        this.jvmOptions = jvmOptions;
        this.memory = memory;
        this.tag = tag;
        this.tagRequired = tagRequired;
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

    public String getNamespace() {
        return this.namespace;
    }

    public String getNamePrefix() {
        return this.namePrefix;
    }

    public String getNode() {
        return this.node;
    }

    public String getImage() {
        return this.image;
    }

    public int getCpu() {
        return this.cpu;
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
        private OrkaClientFactory clientFactory = new OrkaClientFactory();
        private FormValidator formValidator = new FormValidator(clientFactory);
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(clientFactory);

        public DescriptorImpl() {
            load();
        }

        @VisibleForTesting
        void setclientFactory(OrkaClientFactory clientFactory) {
            this.clientFactory = clientFactory;
            this.formValidator = new FormValidator(this.clientFactory);
            this.infoHelper = new OrkaInfoHelper(this.clientFactory);
        }

        public String getDisplayName() {
            return "Agent running under Orka by MacStadium";
        }

        @Override
        public boolean isInstantiable() {
            return true;
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
                @QueryParameter String orkaCredentialsId, @QueryParameter String namespace,
                @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillNodeItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    namespace,
                    ignoreSSLErrors);
        }

        @POST
        public FormValidation doCheckNamespace(@QueryParameter @RelativePath("..") String orkaEndpoint,
                @QueryParameter @RelativePath("..") String orkaCredentialsId,
                @QueryParameter @RelativePath("..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("..") Boolean ignoreSSLErrors, @QueryParameter String value) {
            return this.formValidator.doCheckNamespace(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors, value);
        }

        public ListBoxModel doFillNumCPUsItems() {
            return this.infoHelper.doFillNumCPUsItems();
        }

        @POST
        public ListBoxModel doFillImageItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillBaseImageItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    ignoreSSLErrors);
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter String orkaCredentialsId,
                @QueryParameter String orkaEndpoint, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean ignoreSSLErrors)
                throws IOException {

            return this.formValidator.doTestConnection(orkaCredentialsId, orkaEndpoint, useJenkinsProxySettings,
                    ignoreSSLErrors);
        }

        public String getDefaultNamespace() {
            return Constants.DEFAULT_NAMESPACE;
        }
    }
}
