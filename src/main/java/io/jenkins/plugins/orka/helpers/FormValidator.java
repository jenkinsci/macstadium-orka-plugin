package io.jenkins.plugins.orka.helpers;

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

public class FormValidator {
    private static final Logger logger = Logger.getLogger(FormValidator.class.getName());
    private static final String NOT_ENOUGH_RESOURCES_FORMAT = 
        "Not enough resources on node. Required %s CPU, available %s";

    private ClientFactory clientFactory;

    public FormValidator(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public FormValidation doCheckConfigName(String configName, String orkaEndpoint, String orkaCredentialsId,
            boolean createNewVMConfig) {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);

        if (createNewVMConfig) {
            if (configName.length() < 5) {
                return FormValidation.error("Configuration name should NOT be shorter than 5 characters");
            }

            try {
                if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                    OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint, orkaCredentialsId);
                    boolean alreadyInUse = client.getVMs().stream()
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

    public FormValidation doCheckNode(String node, String orkaEndpoint, String orkaCredentialsId, String vm,
            boolean createNewConfig, int numCPUs) {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
    
        boolean hasAvailableNodes = true;
        boolean canDeployVM = true;
        int requiredCPU = 0;
        int availableCPU = 0;

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint, orkaCredentialsId);
                hasAvailableNodes = client.getNodes().stream().filter(ProvisioningHelper::canDeployOnNode)
                        .anyMatch(n -> true);

                if (hasAvailableNodes) {
                    requiredCPU = numCPUs;
                    if (!createNewConfig) {
                        requiredCPU = client.getVMs().stream().filter(v -> v.getVMName().equals(vm)).findFirst().get()
                                .getCPUCount();
                    }

                    NodeResponse nodeDetails = client.getNodes().stream().filter(n -> n.getHostname().equals(node))
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

    public FormValidation doCheckIdleTerminationMinutes(String value) {
        if (StringUtils.isBlank(value)) {
            return FormValidation.ok();
        }
        try {
            int val = Integer.parseInt(value);
            if (val > 0) {
                return FormValidation.ok();
            }
        } catch (NumberFormatException e) {
        }
        return FormValidation.error("Idle Termination time must be a greater than 0 (or empty)");
    }
}