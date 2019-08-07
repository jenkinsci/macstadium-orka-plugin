package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import io.jenkins.plugins.orka.helpers.ClientFactory;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;

import java.io.IOException;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

public class OrkaAgent extends AbstractCloudSlave {
    private static final long serialVersionUID = 6363583313270146174L;

    public String orkaCredentialsId;
    public String orkaEndpoint;
    public String vmCredentialsId;
    private transient boolean createNewVMConfig;
    private String vm;
    private String node;
    private String configName;
    private transient String baseImage;
    private transient String image;
    private transient int numCPUs;

    @DataBoundConstructor
    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, boolean createNewVMConfig, String configName, String baseImage, String image, int numCPUs,
            int numExecutors, String host, int port, String remoteFS, Mode mode, String labelString,
            RetentionStrategy retentionStrategy, List<? extends NodeProperty<?>> nodeProperties)
            throws Descriptor.FormException, IOException {

        super(name, null, remoteFS, numExecutors, mode, labelString, new OrkaComputerLauncher(host, port),
                retentionStrategy, nodeProperties);

        this.orkaCredentialsId = orkaCredentialsId;
        this.orkaEndpoint = orkaEndpoint;
        this.vmCredentialsId = vmCredentialsId;
        this.vm = vm;
        this.node = node;
        this.createNewVMConfig = createNewVMConfig;
        this.configName = configName;
        this.baseImage = baseImage;
        this.image = image;
        this.numCPUs = numCPUs;
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

    public String getImage() {
        return this.image;
    }

    public int getNumCPUs() {
        return this.numCPUs;
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
        private ClientFactory clientFactory = new ClientFactory();
        private FormValidator formValidator = new FormValidator(this.clientFactory);
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(this.clientFactory);

        public DescriptorImpl() {
            load();
        }

        @VisibleForTesting
        void setClientFactory(ClientFactory clientFactory) {
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

        public FormValidation doCheckConfigName(@QueryParameter String configName, @QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean createNewVMConfig) {

            return this.formValidator.doCheckConfigName(configName, orkaEndpoint, orkaCredentialsId, createNewVMConfig);
        }

        public FormValidation doCheckNode(@QueryParameter String value, @QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter String vm,
                @QueryParameter boolean createNewVMConfig, @QueryParameter int numCPUs) {

            return this.formValidator.doCheckNode(value, orkaEndpoint, orkaCredentialsId, vm, createNewVMConfig,
                    numCPUs);
        }

        public ListBoxModel doFillOrkaCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillNodeItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId) {

            return this.infoHelper.doFillNodeItems(orkaEndpoint, orkaCredentialsId);
        }

        public ListBoxModel doFillVmItems(@QueryParameter String orkaEndpoint, @QueryParameter String orkaCredentialsId,
                @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillVmItems(orkaEndpoint, orkaCredentialsId, createNewVMConfig);
        }

        public ListBoxModel doFillBaseImageItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillBaseImageItems(orkaEndpoint, orkaCredentialsId, createNewVMConfig);
        }
    }
}
