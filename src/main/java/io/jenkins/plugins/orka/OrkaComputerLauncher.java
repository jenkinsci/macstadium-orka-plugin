package io.jenkins.plugins.orka;

import hudson.model.TaskListener;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.helpers.OrkaClientProxy;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;
import io.jenkins.plugins.orka.helpers.SSHUtil;
import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.io.PrintStream;
import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;

public final class OrkaComputerLauncher extends ComputerLauncher {
    private static String configurationErrorFormat = "%s: Creating configuration with configName: %s, image: %s, "
            + "baseImage: %s, template: %s, numCPUs: %s, and memory: %s"
            + "failed with an error: %s. Stopping creation.";
    private static String deploymentErrorFormat = "%s: Deploying vm with name: %s, and node: %s"
            + "failed with an error: %s. Stopping creation.";

    private static String configurationSuccessFormat = "%s: Creating configuration returned result: %s";
    private static String deploymentSuccessFormat = "%s: Deploying vm returned result: %s";

    private static final String template = Constants.DEFAULT_CONFIG_NAME;

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

        OrkaClientProxy client = new OrkaClientProxyFactory().getOrkaClientProxy(agent.getOrkaEndpoint(),
                agent.getOrkaCredentialsId(), agent.getUseJenkinsProxySettings(), agent.getIgnoreSSLErrors());

        PrintStream logger = listener.getLogger();

        if (!createConfiguration(agent, client, logger)) {
            return;
        }

        DeploymentResponse deploymentResponse = this.deployVM(agent, client, logger);
        if (deploymentResponse == null) {
            return;
        }

        this.host = StringUtils.isNotBlank(this.redirectHost) ? redirectHost : deploymentResponse.getHost();
        this.port = deploymentResponse.getSSHPort();
        this.launcher = this.getLauncher(agent.getVmCredentialsId());
        Jenkins.get().updateNode(slaveComputer.getNode());

        listener.getLogger().println("Waiting for VM to boot");
        this.waitForVM(this.host, this.port);

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

    private void waitForVM(String host, int sshPort) throws InterruptedException, IOException {
        int retries = 12;
        int secondsBetweenRetries = 15;
        SSHUtil.waitForSSH(host, sshPort, retries, secondsBetweenRetries);
    }

    private boolean vmExists() {
        return StringUtils.isNotBlank(this.host) && this.port != 0;
    }

    private boolean createConfiguration(OrkaAgent agent, OrkaClientProxy clientProxy, PrintStream logger)
            throws IOException {
        if (agent.getCreateNewVMConfig()) {
            String configName = agent.getConfigName();
            String image = configName;
            String baseImage = agent.getBaseImage();
            int numCPUs = agent.getNumCPUs();
            String memory = agent.getMemory();

            ConfigurationResponse configResponse = clientProxy.createConfiguration(configName, image, baseImage,
                    template, numCPUs, null, memory);
            if (!configResponse.isSuccessful()) {
                logger.println(String.format(configurationErrorFormat, Utils.getTimestamp(), configName, image,
                        baseImage, template, numCPUs, memory, Utils.getErrorMessage(configResponse)));
                return false;
            }
            logger.println(
                    String.format(configurationSuccessFormat, Utils.getTimestamp(), configResponse.getMessage()));
        }
        return true;
    }

    private DeploymentResponse deployVM(OrkaAgent agent, OrkaClientProxy clientProxy, PrintStream logger)
            throws IOException {
        String vmName = agent.getCreateNewVMConfig() ? agent.getConfigName() : agent.getVm();

        DeploymentResponse deploymentResponse = clientProxy.deployVM(vmName, agent.getNode());
        if (!deploymentResponse.isSuccessful()) {
            logger.println(
                    String.format(deploymentErrorFormat, Utils.getTimestamp(), vmName, agent.getNode(),
                            Utils.getErrorMessage(deploymentResponse)));
            return null;
        }
        logger.println(String.format(deploymentSuccessFormat, Utils.getTimestamp(), deploymentResponse.getMessage()));
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
