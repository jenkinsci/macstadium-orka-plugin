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

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class OrkaAgent extends AbstractCloudSlave {
    private static final long serialVersionUID = 6363583313270146174L;

    public String orkaCredentialsId;
    public String orkaEndpoint;
    public String vmCredentialsId;
    private boolean useJenkinsProxySettings;
    private boolean createNewVMConfig;
    private String vm;
    private String node;
    private String configName;
    private String baseImage;
    private int numCPUs;

    @DataBoundConstructor
    public OrkaAgent(String name, String orkaCredentialsId, String orkaEndpoint, String vmCredentialsId, String vm,
            String node, String redirectHost, boolean createNewVMConfig, String configName, String baseImage,
            int numCPUs, int numExecutors, String host, int port, String remoteFS)
            throws Descriptor.FormException, IOException {

        super(name, remoteFS, new OrkaComputerLauncher(host, port, redirectHost));

        this.orkaCredentialsId = orkaCredentialsId;
        this.orkaEndpoint = orkaEndpoint;
        this.vmCredentialsId = vmCredentialsId;
        this.vm = vm;
        this.node = node;
        this.createNewVMConfig = createNewVMConfig;
        this.configName = configName;
        this.baseImage = baseImage;
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

    public boolean getUseJenkinsProxySettings() {
        return this.useJenkinsProxySettings;
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
                @QueryParameter boolean createNewVMConfig) {

            return this.formValidator.doCheckConfigName(configName, orkaEndpoint, orkaCredentialsId,
                    useJenkinsProxySettings, createNewVMConfig);
        }

        @POST
        public FormValidation doCheckNode(@QueryParameter String value, @QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter String vm, @QueryParameter boolean createNewVMConfig, @QueryParameter int numCPUs) {

            return this.formValidator.doCheckNode(value, orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings, vm,
                    createNewVMConfig, numCPUs);
        }

        public ListBoxModel doFillOrkaCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public ListBoxModel doFillNodeItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings) {

            return this.infoHelper.doFillNodeItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings);
        }

        public ListBoxModel doFillNumCPUsItems() {
            return this.infoHelper.doFillNumCPUsItems();
        }

        @POST
        public ListBoxModel doFillVmItems(@QueryParameter String orkaEndpoint, @QueryParameter String orkaCredentialsId,
                @QueryParameter boolean useJenkinsProxySettings, @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillVmItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    createNewVMConfig);
        }

        @POST
        public ListBoxModel doFillBaseImageItems(@QueryParameter String orkaEndpoint,
                @QueryParameter String orkaCredentialsId, @QueryParameter boolean useJenkinsProxySettings,
                @QueryParameter boolean createNewVMConfig) {

            return this.infoHelper.doFillBaseImageItems(orkaEndpoint, orkaCredentialsId, useJenkinsProxySettings,
                    createNewVMConfig);
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter String orkaCredentialsId,
                @QueryParameter String orkaEndpoint, @QueryParameter boolean useJenkinsProxySettings)
                throws IOException {

            return this.formValidator.doTestConnection(orkaCredentialsId, orkaEndpoint, useJenkinsProxySettings);
        }
    }
}
