package io.jenkins.plugins.orka.client;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrkaClientTest {
    @Test
    public void when_creating_client_should_get_correct_token() throws IOException {
        String token = "private-token";
        String tokenResponse = "{\"token\": \"" + token + "\"}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), anyString())).thenReturn(tokenResponse);
        when(client.getToken(anyString(), anyString())).thenCallRealMethod();

        String actualToken = client.getToken("email", "password");

        assertEquals(token, actualToken);
    }

    @Test
    public void when_calling_getvms_should_get_all_vms() throws IOException {
        VMResponse firstVM = new VMResponse("first", "deployed", 12, "Mojave.img", "firstImage", "default");
        VMResponse secondVM = new VMResponse("second", "not deployed", 24, "Mojave.img", "secondImage", "default");
        VMResponse[] vms = { firstVM, secondVM };
        String arrayJson = new Gson().toJson(vms);
        String response = "{ \"virtual_machine_resources\": " + arrayJson + "}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getVMs()).thenCallRealMethod();

        List<VMResponse> actualResponse = client.getVMs();

        assertArrayEquals(vms, actualResponse.toArray());
    }

    @Test
    public void when_calling_getnodes_should_get_all_nodes() throws IOException {
        NodeResponse firstNode = new NodeResponse("macpro-1", "127.0.0.1", 12, 12, "66Gi", "66Gi", "macpro-1", "ready");
        NodeResponse secondNode = new NodeResponse("macpro-2", "127.0.0.2", 24, 24, "66Gi", "33Gi", "macpro2","ready");
        NodeResponse[] nodes = { firstNode, secondNode };
        String arrayJson = new Gson().toJson(nodes);
        String response = "{ \"nodes\": " + arrayJson + "}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getNodes()).thenCallRealMethod();

        List<NodeResponse> actualResponse = client.getNodes();

        assertArrayEquals(nodes, actualResponse.toArray());
    }

    @Test
    public void when_calling_getimages_should_get_all_images() throws IOException {
        String[] images = { "Mojave.img", "SnowLeopard.img" };
        String arrayJson = new Gson().toJson(images);
        String response = "{ \"images\": " + arrayJson + "}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getImages()).thenCallRealMethod();

        List<String> actualResponse = client.getImages();

        assertArrayEquals(images, actualResponse.toArray());
    }

    @Test
    public void when_calling_create_configuration_should_return_success() throws IOException {
        String message = "Succesfully Created";
        String response = "{ \"message\": \"" + message + "\"}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), anyString())).thenReturn(response);
        when(client.createConfiguration(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenCallRealMethod();

        ConfigurationResponse actualResponse = client.createConfiguration("newVm", "image", "baseImage", "default", 24);

        assertEquals(message, actualResponse.getMessage());
    }

    @Test
    public void when_calling_deploy_vm_should_return_ip_and_ssh_port() throws IOException {
        String ip = "199.92.12.1";
        int sshPort = 9281;
        String response = "{ \"ip\": \"" + ip + "\", \"ssh_port\": \"" + sshPort + "\"}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), anyString())).thenReturn(response);
        when(client.deployVM(anyString(), anyString())).thenCallRealMethod();

        DeploymentResponse actualResponse = client.deployVM("newVm", "macpro-2");

        assertEquals(ip, actualResponse.getHost());
        assertEquals(sshPort, actualResponse.getSSHPort());
    }

    @Test
    public void when_calling_delete_vm_should_return_status() throws IOException {
        String message = "Success";
        String response = "{ \"message\": \"" + message + "\" }";

        OrkaClient client = mock(OrkaClient.class);
        when(client.delete(anyString(), anyString())).thenReturn(response);
        when(client.deleteVM(anyString(), anyString())).thenCallRealMethod();

        DeletionResponse actualResponse = client.deleteVM("newVm", "macpro-2");

        assertEquals(message, actualResponse.getMessage());
    }

    @Test
    public void when_calling_close_should_call_delete() throws IOException {
        OrkaClient client = mock(OrkaClient.class);

        doCallRealMethod().when(client).close();

        client.close();

        verify(client, times(1)).delete(anyString(), anyString());
        verify(client, times(1)).delete(endsWith("/token"), eq(""));
    }
}