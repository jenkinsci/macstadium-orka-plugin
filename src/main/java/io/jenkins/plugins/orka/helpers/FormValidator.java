package io.jenkins.plugins.orka.helpers;

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.OrkaNode;
import io.jenkins.plugins.orka.client.TokenStatusResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

public class FormValidator {
    private static final Logger logger = Logger.getLogger(FormValidator.class.getName());
    private static final String NOT_ENOUGH_RESOURCES_FORMAT = "Not enough resources on node. "
            + "Required %s CPU, available %s";

    private OrkaClientProxyFactory clientProxyFactory;

    public FormValidator(OrkaClientProxyFactory clientProxyFactory) {
        this.clientProxyFactory = clientProxyFactory;
    }

    public FormValidation doCheckConfigName(String configName, String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, boolean createNewVMConfig) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        if (createNewVMConfig) {
            if (configName.length() < 5) {
                return FormValidation.error("Configuration name should NOT be shorter than 5 characters");
            }

            try {
                if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                    OrkaClientProxy clientProxy = this.clientProxyFactory.getOrkaClientProxy(orkaEndpoint,
                            orkaCredentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                    boolean alreadyInUse = clientProxy.getVMs().stream()
                            .anyMatch(vm -> vm.getVMName().equalsIgnoreCase(configName));
                    if (alreadyInUse) {
                        return FormValidation.error("Configuration name is already in use");
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception in doCheckConfigName", e);
            }
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckNode(String node, String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, String vm, boolean createNewConfig, int numCPUs) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        boolean hasAvailableNodes = true;
        boolean canDeployVM = true;
        int requiredCPU = 0;
        int availableCPU = 0;

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                OrkaClientProxy clientProxy = this.clientProxyFactory.getOrkaClientProxy(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                hasAvailableNodes = clientProxy.getNodes().stream().filter(ProvisioningHelper::canDeployOnNode)
                        .anyMatch(n -> true);

                if (hasAvailableNodes) {
                    requiredCPU = numCPUs;
                    if (!createNewConfig) {
                        requiredCPU = clientProxy.getVMs().stream().filter(v -> v.getVMName().equals(vm)).findFirst()
                                .get().getCPUCount();
                    }

                    OrkaNode nodeDetails = clientProxy.getNodes().stream().filter(n -> n.getHostname().equals(node))
                            .findFirst().get();
                    canDeployVM = ProvisioningHelper.canDeployOnNode(nodeDetails, requiredCPU);
                    availableCPU = nodeDetails.getAvailableCPU();
                }

            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doCheckNode", e);
        }

        if (hasAvailableNodes) {
            return canDeployVM ? FormValidation.ok()
                    : FormValidation.error(String.format(NOT_ENOUGH_RESOURCES_FORMAT, requiredCPU, availableCPU));
        }

        return FormValidation.error("There are no available nodes");
    }

    public FormValidation doCheckMemory(String memory) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        System.out.println(memory);
        try {
            if (StringUtils.isBlank(memory) || StringUtils.equals(memory, "auto")) {
                return FormValidation.ok();
            }
            if (Integer.parseInt(memory) < 1) {
                return FormValidation.error("Memory should be greater than 0");
            }
            return FormValidation.ok();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doCheckMemory", e);
        }

        return FormValidation.error("Invalid Memory");
    }

    public FormValidation doTestConnection(String credentialsId, String endpoint, boolean useJenkinsProxySettings,
            boolean ignoreSSLErrors)
            throws IOException {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            TokenStatusResponse response = new OrkaClientProxyFactory()
                    .getOrkaClientProxy(endpoint, credentialsId, useJenkinsProxySettings,
                            ignoreSSLErrors)
                    .getTokenStatus();
            if (!response.isSuccessful() || !response.getIsValid()) {
                return failedConnection(Utils.getErrorMessage(response));
            }
        } catch (IOException e) {
            return failedConnection(e.getMessage());
        }

        return FormValidation.ok("Connection Successful");
    }

    private static FormValidation failedConnection(String message) {
        return FormValidation.error("Connection failed with: " + message);
    }
}