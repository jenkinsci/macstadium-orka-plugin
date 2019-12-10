package io.jenkins.plugins.orka;

import hudson.model.TaskListener;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.plugins.sshslaves.verifiers.NonVerifyingKeyVerificationStrategy;
import hudson.slaves.ComputerLauncher;
import hudson.slaves.SlaveComputer;
import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.helpers.ClientFactory;
import io.jenkins.plugins.orka.helpers.Utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;

public final class OrkaComputerLauncher extends ComputerLauncher {
    private static String configurationErrorFormat = "%s: Creating configuration with configName: %s, image: %s, "
            + "baseImage: %s, template: %s and numCPUs: %s failed with an error: %s. Stopping creation.";
    private static String deploymentErrorFormat = "%s: Deploying vm with name: %s and node: %s "
            + "failed with an error: %s. Stopping creation.";

    private static String configurationSuccessFormat = "%s: Creating configuration returned result: %s";
    private static String deploymentSuccessFormat = "%s: Deploying vm returned result: %s";

    private static final String template = Constants.DEFAULT_CONFIG_NAME;

    private int launchWaitTime = 15;
    private int launchTimeoutSeconds = 300;
    private int maxRetries = 3;
    private int retryWaitTime = 30;

    private transient SSHLauncher launcher;
    private transient String redirectHost;
    private String host;
    private int port;

    public OrkaComputerLauncher(String host, int port, String redirectHost) {
        this.host = host;
        this.port = port;
        this.redirectHost = redirectHost;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
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

        OrkaClient client = new ClientFactory().getOrkaClient(agent.getOrkaEndpoint(), agent.getOrkaCredentialsId());
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
        Jenkins.getInstance().updateNode(slaveComputer.getNode());

        listener.getLogger().println("Waiting for VM to boot");

        Thread.sleep(TimeUnit.SECONDS.toMillis(this.launchWaitTime));

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

    private boolean createConfiguration(OrkaAgent agent, OrkaClient client, PrintStream logger) throws IOException {
        if (agent.getCreateNewVMConfig()) {
            String configName = agent.getConfigName();
            String image = configName;
            String baseImage = agent.getBaseImage();
            int numCPUs = agent.getNumCPUs();

            ConfigurationResponse configResponse = client.createConfiguration(configName, image, baseImage, template,
                    numCPUs);
            if (configResponse.hasErrors()) {
                logger.println(String.format(configurationErrorFormat, Utils.getTimestamp(), configName, image,
                        baseImage, template, numCPUs, Arrays.toString(configResponse.getErrors())));
                return false;
            }
            logger.println(
                    String.format(configurationSuccessFormat, Utils.getTimestamp(), configResponse.getMessage()));
        }
        return true;
    }

    private DeploymentResponse deployVM(OrkaAgent agent, OrkaClient client, PrintStream logger) throws IOException {
        String vmName = agent.getCreateNewVMConfig() ? agent.getConfigName() : agent.getVm();

        DeploymentResponse deploymentResponse = client.deployVM(vmName, agent.getNode());
        if (deploymentResponse.hasErrors()) {
            logger.println(String.format(deploymentErrorFormat, Utils.getTimestamp(), vmName, agent.getNode(),
                    Arrays.toString(deploymentResponse.getErrors())));
            return null;
        }
        logger.println(String.format(deploymentSuccessFormat, Utils.getTimestamp(), deploymentResponse.getMessage()));
        return deploymentResponse;
    }

    private SSHLauncher getLauncher(String vmCredentialsId) {
        String jvmOptions = null;
        String javaPath = null;
        String prefixStartSlaveCmd = null;
        String suffixStartSlaveCmd = null;

        return new SSHLauncher(this.host, this.port, vmCredentialsId, jvmOptions, javaPath, prefixStartSlaveCmd,
                suffixStartSlaveCmd, this.launchTimeoutSeconds, this.maxRetries, this.retryWaitTime,
                new NonVerifyingKeyVerificationStrategy());
    }
}
