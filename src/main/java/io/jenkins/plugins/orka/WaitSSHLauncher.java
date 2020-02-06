package io.jenkins.plugins.orka;

import hudson.model.TaskListener;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.orka.helpers.SSHUtil;

import java.io.IOException;

public final class WaitSSHLauncher extends ComputerLauncher {
    private SSHLauncher launcher;

    public WaitSSHLauncher(String host, int sshPort, String vmCredentialsId) {
        String jvmOptions = null;
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
        int maxRetries = 12;
        int retryWaitTime = 15;
        listener.getLogger().println("Waiting for SSH to be enabled");
        SSHUtil.waitForSSH(this.launcher.getHost(), this.launcher.getPort(), maxRetries, retryWaitTime);
        listener.getLogger().println("SSH enabled");

        this.launcher.launch(slaveComputer, listener);
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
