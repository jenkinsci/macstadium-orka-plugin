package io.jenkins.plugins.orka.helpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.jenkins.plugins.orka.OrkaNode;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;

public class ProvisioningHelperTest {
    @Test
     public void when_getting_free_nodes_should_return_correct_count() throws IOException {
         NodeResponse firstNode = new NodeResponse("macpro-1", "127.0.0.1", 12, 12, "32Gi", "32Gi", "macpro-1", "ready");
         NodeResponse secondNode = new NodeResponse("macpro-2", "127.0.0.2", 24, 0, "32Gi", "0Gi", "macpro-2", "ready");
         NodeResponse thirdNode = new NodeResponse("macpro-3", "127.0.0.2", 24, 24, "32Gi", "10Gi", "macpro3", "ready");
         List<NodeResponse> nodes = Arrays.asList(firstNode, secondNode, thirdNode);

         OrkaClient client = mock(OrkaClient.class);
         when(client.getNodes()).thenReturn(nodes);

         ProvisioningHelper helper = new ProvisioningHelper(client);
         List<OrkaNode> reservedNodes = helper.getFreeNodes(1, 12);

         assertEquals(1, reservedNodes.size());
         assertEquals(firstNode.getHostname(), reservedNodes.get(0).getName());
         assertEquals(1, reservedNodes.get(0).getVmCapacity());

         List<NodeResponse> updatedNodes = Arrays.asList(secondNode, thirdNode);
         when(client.getNodes()).thenReturn(updatedNodes);

         reservedNodes = helper.getFreeNodes(5, 12);

         assertEquals(1, reservedNodes.size());
         assertEquals(thirdNode.getHostname(), reservedNodes.get(0).getName());
         assertEquals(2, reservedNodes.get(0).getVmCapacity());
    }

    @Test
    public void when_provisioning_one_vm_and_having_more_capacity_should_provision_one_vm() throws IOException {
        NodeResponse firstNode = new NodeResponse("macpro-1", "127.0.0.1", 24, 24, "32Gi", "32Gi", "macpro-1", "ready");

        ProvisioningHelper helper = this.getProvisioningHelper(Arrays.asList(firstNode));
        List<OrkaNode> reservedNodes = helper.getFreeNodes(1, 12);

        assertEquals(1, reservedNodes.size());
        assertEquals(firstNode.getHostname(), reservedNodes.get(0).getName());
        assertEquals(1, reservedNodes.get(0).getVmCapacity());
    }

    @Test
    public void when_provisioning_two_vms_and_having_more_capacity_should_provision_two_vms() throws IOException {
        NodeResponse firstNode = new NodeResponse("macpro-1", "127.0.0.1", 24, 24, "32Gi", "32Gi", "macpro-1", "ready");

        NodeResponse secondNode = new NodeResponse("macpro-2", "127.0.0.2", 48, 48, "32Gi", "0Gi", "macpro-2", "ready");

        ProvisioningHelper helper = this.getProvisioningHelper(Arrays.asList(firstNode, secondNode));
        List<OrkaNode> reservedNodes = helper.getFreeNodes(2, 24);

        assertEquals(2, reservedNodes.size());
        assertEquals(firstNode.getHostname(), reservedNodes.get(0).getName());
        assertEquals(1, reservedNodes.get(0).getVmCapacity());

        assertEquals(secondNode.getHostname(), reservedNodes.get(1).getName());
        assertEquals(1, reservedNodes.get(1).getVmCapacity());
    }

    private ProvisioningHelper getProvisioningHelper(List<NodeResponse> nodes) throws IOException {
        OrkaClient client = mock(OrkaClient.class);
        when(client.getNodes()).thenReturn(nodes);

        return new ProvisioningHelper(client);  
    }
}