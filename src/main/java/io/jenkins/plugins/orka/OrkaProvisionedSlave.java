package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.NodeProperty;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class OrkaProvisionedSlave extends AbstractCloudSlave {
    private static final long serialVersionUID = -2841785002270403074L;
    private static final Logger logger = Logger.getLogger(OrkaProvisionedSlave.class.getName());

    private String cloudId;
    private String vmId;
    private String node;
    private String host;
    private int sshPort;
    private String vmCredentialsId;
    private int idleTerminationMinutes;

    @DataBoundConstructor
    public OrkaProvisionedSlave(String cloudId, String vmId, String node, String host, int sshPort,
            String vmCredentialsId, int numExecutors, String remoteFS, Mode mode, String labelString,
            int idleTerminationMinutes, List<? extends NodeProperty<?>> nodeProperties)
            throws Descriptor.FormException, IOException {

        super(vmId, null, remoteFS, numExecutors, mode, labelString,
                new SSHLauncher(host, sshPort, vmCredentialsId, null, null, null, null, 300, 3, 30,
                        new NonVerifyingKeyVerificationStrategy()),
                new CloudRetentionStrategy(idleTerminationMinutes), nodeProperties);

        this.cloudId = cloudId;
        this.vmId = vmId;
        this.node = node;
        this.host = host;
        this.sshPort = sshPort;
        this.vmCredentialsId = vmCredentialsId;
        this.idleTerminationMinutes = idleTerminationMinutes;
    }

    public String getCloudId() {
        return this.cloudId;
    }

    public String getVmId() {
        return this.vmId;
    }

    public String getNode() {
        return this.node;
    }

    public String getHost() {
        return this.host;
    }

    public int getSshPort() {
        return this.sshPort;
    }

    public String getVmCredentialsId() {
        return this.vmCredentialsId;
    }

    public int getIdleTerminationMinutes() {
        return this.idleTerminationMinutes;
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        logger.info("Terminating slave ");

        this.getCloud().deleteVM(this.vmId, this.node);
    }

    @Override
    public AbstractCloudComputer createComputer() {
        return new AbstractCloudComputer(this);
    }

    @Extension
    public static final class DescriptorImpl extends SlaveDescriptor {
        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean isInstantiable() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Slave created in Orka";
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }
    }

    private OrkaCloud getCloud() {
        return (OrkaCloud) Jenkins.getInstance().getCloud(cloudId);
    }
}