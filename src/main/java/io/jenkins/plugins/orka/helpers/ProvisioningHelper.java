package io.jenkins.plugins.orka.helpers;

import io.jenkins.plugins.orka.client.NodeResponse;

public class ProvisioningHelper {
    public static boolean canDeployOnNode(NodeResponse node) {
        return canDeployOnNode(node, 1);
    }

    public static boolean canDeployOnNode(NodeResponse node, int requiredCPU) {
        return node.getAvailableCPU() >= requiredCPU && node.getState().equalsIgnoreCase("ready");
    }
}