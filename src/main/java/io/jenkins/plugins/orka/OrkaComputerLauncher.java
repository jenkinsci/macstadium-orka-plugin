package io.jenkins.plugins.orka;

import hudson.model.TaskListener;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
import io.jenkins.plugins.orka.helpers.SSHUtil;
import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.io.PrintStream;
import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;

public final class OrkaComputerLauncher extends ComputerLauncher {
    private static String deploymentErrorFormat = "%s: Deploying vm with name: %s, and node: %s"
            + "failed with an error: %s. Stopping creation.";

    private static String deploymentSuccessFormat = "%s: Deploying vm returned result: %s";

    private int launchTimeoutSeconds = 300;
    private int maxRetries = 3;
    private int retryWaitTime = 30;

    private transient SSHLauncher launcher;
    private transient String redirectHost;
    private String host;
    private int port;
    private String jvmOptions;

    public OrkaComputerLauncher(String host, int port, String redirectHost) {
        this(host, port, redirectHost, null);
    }

    public OrkaComputerLauncher(String host, int port, String redirectHost, String jvmOptions) {
        this.host = host;
        this.port = port;
        this.redirectHost = redirectHost;
        this.jvmOptions = jvmOptions;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getJvmOptions() {
        return this.jvmOptions;
    }

    @Override
    public void launch(SlaveComputer slaveComputer, TaskListener listener) throws IOException, InterruptedException {

        OrkaAgent agent = (OrkaAgent) slaveComputer.getNode();

        if (this.vmExists()) {
            if (this.launcher == null) {
                this.launcher = this.getLauncher(agent.getVmCredentialsId());
            }

            this.launcher.launch(slaveComputer, listener);
            return;
        }

        OrkaClient client = new OrkaClientFactory().getOrkaClient(agent.getOrkaEndpoint(),
                agent.getOrkaCredentialsId(), agent.getUseJenkinsProxySettings(), agent.getIgnoreSSLErrors());

        PrintStream logger = listener.getLogger();
        DeploymentResponse deploymentResponse = this.deployVM(agent, client, logger);
        if (deploymentResponse == null) {
            return;
        }

        this.host = StringUtils.isNotBlank(this.redirectHost) ? redirectHost : deploymentResponse.getIP();
        this.port = deploymentResponse.getSSH();
        this.launcher = this.getLauncher(agent.getVmCredentialsId());
        Jenkins.get().updateNode(slaveComputer.getNode());

        listener.getLogger().println("Waiting for VM to boot");
        SSHUtil.waitForSSH(this.host, this.port);

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

    private boolean vmExists() {
        return StringUtils.isNotBlank(this.host) && this.port != 0;
    }

    private DeploymentResponse deployVM(OrkaAgent agent, OrkaClient client, PrintStream logger)
            throws IOException {

        DeploymentResponse deploymentResponse = client.deployVM(null,
                agent.getNamespace(), agent.getNamePrefix(), agent.getImage(), agent.getCpu(), agent.getMemory(),
                agent.getNode(),
                null, agent.getTag(), agent.getTagRequired());

        if (!deploymentResponse.isSuccessful()) {
            logger.println(
                    String.format(deploymentErrorFormat, Utils.getTimestamp(), agent.getNamePrefix(),
                            agent.getNode(),
                            Utils.getErrorMessage(deploymentResponse)));
            return null;
        }
        logger.println(String.format(deploymentSuccessFormat, Utils.getTimestamp(),
                deploymentResponse.toString()));
        return deploymentResponse;
    }

    private SSHLauncher getLauncher(String vmCredentialsId) {
        String javaPath = null;
        String prefixStartSlaveCmd = null;
        String suffixStartSlaveCmd = null;

        return new SSHLauncher(this.host, this.port, vmCredentialsId, this.jvmOptions, javaPath, prefixStartSlaveCmd,
                suffixStartSlaveCmd, this.launchTimeoutSeconds, this.maxRetries, this.retryWaitTime,
                new NonVerifyingKeyVerificationStrategy());
    }
}
