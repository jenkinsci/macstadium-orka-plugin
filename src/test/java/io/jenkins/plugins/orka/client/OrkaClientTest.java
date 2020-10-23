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

import com.google.gson.Gson;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrkaClientTest {
    @Test
    public void when_creating_client_should_get_correct_token() throws IOException {
        String token = "private-token";
        String body = "{\"token\": \"" + token + "\"}";
        HttpResponse tokenResponse = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), anyString())).thenReturn(tokenResponse);
        when(client.getToken(anyString(), anyString())).thenCallRealMethod();

        String actualToken = client.getToken("email", "password").getToken();

        assertEquals(token, actualToken);
    }

    @Test
    public void when_calling_getvms_should_get_all_vms() throws IOException {
        OrkaVM firstVM = new OrkaVM("first", "deployed", 12, "Mojave.img", "firstImage", "default");
        OrkaVM secondVM = new OrkaVM("second", "not deployed", 24, "Mojave.img", "secondImage", "default");
        OrkaVM[] vms = { firstVM, secondVM };
        String arrayJson = new Gson().toJson(vms);
        String body = "{ \"virtual_machine_resources\": " + arrayJson + "}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getVMs()).thenCallRealMethod();

        VMResponse actualResponse = client.getVMs();

        assertArrayEquals(vms, actualResponse.getVMs().toArray());
    }

    @Test
    public void when_calling_getnodes_should_get_all_nodes() throws IOException {
        OrkaNode firstNode = new OrkaNode("macpro-1", "127.0.0.1", 12, 12, "66Gi", "66Gi", "macpro-1", "ready");
        OrkaNode secondNode = new OrkaNode("macpro-2", "127.0.0.2", 24, 24, "66Gi", "33Gi", "macpro2", "ready");
        OrkaNode[] nodes = { firstNode, secondNode };
        String arrayJson = new Gson().toJson(nodes);
        String body = "{ \"nodes\": " + arrayJson + "}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getNodes()).thenCallRealMethod();

        NodeResponse actualResponse = client.getNodes();

        assertArrayEquals(nodes, actualResponse.getNodes().toArray());
    }

    @Test
    public void when_calling_getimages_should_get_all_images() throws IOException {
        String[] images = { "Mojave.img", "SnowLeopard.img" };
        String arrayJson = new Gson().toJson(images);
        String body = "{ \"images\": " + arrayJson + "}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getImages()).thenCallRealMethod();

        ImageResponse actualResponse = client.getImages();

        assertArrayEquals(images, actualResponse.getImages().toArray());
    }

    @Test
    public void when_calling_create_configuration_should_return_success() throws IOException {
        String message = "Succesfully Created";
        String body = "{ \"message\": \"" + message + "\"}";
        HttpResponse response = new HttpResponse(body, 200, true);

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
        String body = "{ \"ip\": \"" + ip + "\", \"ssh_port\": \"" + sshPort + "\"}";
        HttpResponse response = new HttpResponse(body, 200, true);

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
        String body = "{ \"message\": \"" + message + "\" }";
        HttpResponse response = new HttpResponse(body, 200, true);

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