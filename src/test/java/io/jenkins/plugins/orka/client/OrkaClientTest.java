package io.jenkins.plugins.orka.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.google.gson.Gson;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrkaClientTest {
    @Test
    public void when_calling_getvms_should_get_all_vms() throws IOException {
        OrkaVMConfig firstVM = new OrkaVMConfig("first", 12, "Mojave.img", 12);
        OrkaVMConfig secondVM = new OrkaVMConfig("second", 24, "Mojave.img", 15);

        OrkaVMConfig[] vms = { firstVM, secondVM };
        String arrayJson = new Gson().toJson(vms);
        String body = "{ \"items\": " + arrayJson + "}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getVMConfigs()).thenCallRealMethod();

        VMConfigResponse actualResponse = client.getVMConfigs();

        assertArrayEquals(vms, actualResponse.getConfigs().toArray());
    }

    @Test
    public void when_calling_getnodes_should_get_all_nodes() throws IOException {
        OrkaNode firstNode = new OrkaNode("macpro-1", "127.0.0.1", 12, 12, "66Gi", "66Gi", "ready");
        OrkaNode secondNode = new OrkaNode("macpro-2", "127.0.0.2", 24, 24, "66Gi", "33Gi", "ready");
        OrkaNode[] nodes = { firstNode, secondNode };
        String arrayJson = new Gson().toJson(nodes);
        String body = "{ \"items\": " + arrayJson + "}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(response);
        when(client.getNodes("orka-default")).thenCallRealMethod();

        NodeResponse actualResponse = client.getNodes("orka-default");

        assertArrayEquals(nodes, actualResponse.getNodes().toArray());
    }

    @Test
    public void when_calling_getimages_should_get_all_images() throws IOException {
        Image[] images = { new Image("Mojave.img", "", "amd64"), new Image("SnowLeopard.img", "", "amd64") };
        String arrayJson = new Gson().toJson(images);
        String body = "{ \"items\": " + arrayJson + "}";
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
        when(client.createConfiguration(anyString(), anyString(), anyInt(), anyBoolean(),
                anyBoolean(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenCallRealMethod();

        ConfigurationResponse actualResponse = client.createConfiguration(
                "newVm", "image", 24,
                false, false, "most-allocated", "10", "testTag", true);

        assertEquals(message, actualResponse.getMessage());
    }

    @Test
    public void when_calling_deploy_vm_should_return_ip_and_ssh_port() throws IOException {
        String ip = "199.92.12.1";
        int sshPort = 9281;
        String body = "{ \"ip\": \"" + ip + "\", \"ssh\": \"" + sshPort + "\"}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), anyString())).thenReturn(response);
        when(client.deployVM(anyString(), anyString(), anyString(), any(), any(), any())).thenCallRealMethod();

        DeploymentResponse actualResponse = client.deployVM("newVm", "orka-default", "macpro-2", null, null, null);

        assertEquals(ip, actualResponse.getIP());
        assertEquals(sshPort, actualResponse.getSSH());
    }
}
