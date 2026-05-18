package io.jenkins.plugins.orka;

import hudson.model.TaskListener;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.orka.helpers.SSHUtil;

import java.io.IOException;
import java.util.logging.Logger;

public final class WaitSSHLauncher extends ComputerLauncher {
    private static final Logger logger = Logger.getLogger(WaitSSHLauncher.class.getName());

    private SSHLauncher launcher;

    public WaitSSHLauncher(String host, int sshPort, String vmCredentialsId, String jvmOptions) {
        String javaPath = null;
        String prefixStartSlaveCmd = null;
        String suffixStartSlaveCmd = null;
        int launchTimeoutSeconds = 300;
        int maxNumRetries = 3;
        int retryWaitTime = 30;

        this.launcher = new SSHLauncher(host, sshPort, vmCredentialsId, jvmOptions, javaPath, prefixStartSlaveCmd,
                suffixStartSlaveCmd, launchTimeoutSeconds, maxNumRetries, retryWaitTime,
                new NonVerifyingKeyVerificationStrategy());
    }

    @Override
    public void launch(SlaveComputer slaveComputer, TaskListener listener) throws IOException, InterruptedException {
        String host = launcher.getHost();
        int port = launcher.getPort();

        listener.getLogger().println("Waiting for SSH to be enabled");
        logger.fine("Waiting for SSH to be enabled on host  " + host + " on port " + port);

        try {
            SSHUtil.waitForSSH(host, port);
        } catch (IOException ex) {
            listener.getLogger().println("SSH connection failed with: " + ex);
            logger.fine("SSH connection failed for host " + host + " on port " + port + "with: " + ex);
            this.deleteAgent(slaveComputer);
            throw ex;
        }

        listener.getLogger().println("SSH enabled");
        logger.fine("SSH enabled on host " + host + " on port " + port);

        this.launcher.launch(slaveComputer, listener);
    }

    private void deleteAgent(SlaveComputer slaveComputer) throws InterruptedException, IOException {
        AbstractCloudSlave node = ((AbstractCloudSlave) slaveComputer.getNode());
        if (node != null) {
            node.terminate();
        } else {
            slaveComputer.doDoDelete();
        }
    }

    @Override
    public void afterDisconnect(SlaveComputer computer, TaskListener listener) {
        if (launcher != null) {
            this.launcher.afterDisconnect(computer, listener);
        }
    }

    @Override
    public void beforeDisconnect(SlaveComputer computer, TaskListener listener) {
        if (launcher != null) {
            this.launcher.beforeDisconnect(computer, listener);
        }
    }
}
