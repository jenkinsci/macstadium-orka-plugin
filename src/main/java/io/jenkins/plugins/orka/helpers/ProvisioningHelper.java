package io.jenkins.plugins.orka.helpers;

import com.google.common.annotations.VisibleForTesting;

import io.jenkins.plugins.orka.OrkaNode;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProvisioningHelper {
    private OrkaClient client;

    @VisibleForTesting
    ProvisioningHelper(OrkaClient client) {
        this.client = client;
    }

    public ProvisioningHelper(String endpoint, String credentialsId) throws IOException {
        this.client = new ClientFactory().getOrkaClient(endpoint, credentialsId);
    }

    public List<OrkaNode> getFreeNodes(int vms, int requiredCPU) throws IOException {
        List<OrkaNode> nodes = new ArrayList<OrkaNode>();
        int currentVMs = 0;

        List<NodeResponse> freeNodes = client.getNodes().stream().filter(n -> canDeployOnNode(n, requiredCPU))
                .collect(Collectors.toList());
        for (NodeResponse freeNode : freeNodes) {
            if (currentVMs >= vms) {
                break;
            }

            int deployableVMs = this.getDeployableVMs(freeNode, requiredCPU);
            int vmsLeft = vms - currentVMs;
            int vmsOnNode = Math.min(vmsLeft, deployableVMs);
            nodes.add(new OrkaNode(freeNode.getHostname(), vmsOnNode));
            currentVMs += deployableVMs;
        }

        return nodes;
    }

    public static boolean canDeployOnNode(NodeResponse node) {
        return canDeployOnNode(node, 1);
    }

    public static boolean canDeployOnNode(NodeResponse node, int requiredCPU) {
        return node.getAvailableCPU() >= requiredCPU && node.getState().equalsIgnoreCase("ready");
    }

    private int getDeployableVMs(NodeResponse node, int requiredCPU) {
        return node.getAvailableCPU() / requiredCPU;
    }
}