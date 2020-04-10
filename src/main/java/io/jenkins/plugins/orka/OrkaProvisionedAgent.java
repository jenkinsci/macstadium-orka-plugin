package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardCredentials;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.NodeProperty;
import hudson.slaves.RetentionStrategy;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.OrkaRetentionStrategy;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class OrkaProvisionedAgent extends AbstractCloudSlave {
    private static final long serialVersionUID = -2841785002270403074L;
    private static final Logger logger = Logger.getLogger(OrkaProvisionedAgent.class.getName());

    private String cloudId;
    private String vmId;
    private String node;
    private String host;
    private int sshPort;
    private String vmCredentialsId;

    @DataBoundConstructor
    public OrkaProvisionedAgent(String cloudId, String vmId, String node, String host, int sshPort,
            String vmCredentialsId, int numExecutors, String remoteFS, Mode mode, String labelString,
            RetentionStrategy<?> retentionStrategy, List<? extends NodeProperty<?>> nodeProperties)
            throws Descriptor.FormException, IOException {

        super(vmId, null, remoteFS, numExecutors, mode, labelString,
                new WaitSSHLauncher(host, sshPort, vmCredentialsId), retentionStrategy, nodeProperties);

        this.cloudId = cloudId;
        this.vmId = vmId;
        this.node = node;
        this.host = host;
        this.sshPort = sshPort;
        this.vmCredentialsId = vmCredentialsId;
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

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        logger.info("Terminating agent. VM id: " + this.vmId);

        this.getCloud().deleteVM(this.vmId);
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
            return "Agent created in Orka";
        }

        public ListBoxModel doFillVmCredentialsIdItems() {
            return CredentialsHelper.getCredentials(StandardCredentials.class);
        }

        public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() {
            return OrkaRetentionStrategy.getRetentionStrategyDescriptors();
        }
    }

    private OrkaCloud getCloud() {
        return (OrkaCloud) Jenkins.getInstance().getCloud(cloudId);
    }
}