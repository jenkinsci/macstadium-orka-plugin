package io.jenkins.plugins.orka.helpers;

import io.jenkins.plugins.orka.client.OrkaNode;

public class ProvisioningHelper {
    public static boolean canDeployOnNode(OrkaNode node) {
        return canDeployOnNode(node, 1);
    }

    public static boolean canDeployOnNode(OrkaNode node, int requiredCPU) {
        return node.getAvailableCPU() >= requiredCPU && node.getState().equalsIgnoreCase("ready");
    }
}