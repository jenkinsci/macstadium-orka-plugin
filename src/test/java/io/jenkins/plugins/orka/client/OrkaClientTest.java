package io.jenkins.plugins.orka.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
    public void when_calling_deploy_vm_should_return_ip_and_ssh_port() throws IOException {
        String ip = "199.92.12.1";
        int sshPort = 9281;
        String body = "{ \"ip\": \"" + ip + "\", \"ssh\": \"" + sshPort + "\"}";
        HttpResponse response = new HttpResponse(body, 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(response);
        when(client.deployVM(any(), anyString(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenCallRealMethod();

        DeploymentResponse actualResponse = client.deployVM("newVm", "orka-default", null, "foo.img", 6, "12", null,
                null,
                null, false,false, false, false, null, 1500, 2000, 100, null);

        assertEquals(ip, actualResponse.getIP());
        assertEquals(sshPort, actualResponse.getSSH());
    }

    @Test
    public void when_calling_deploy_vm_with_userdata_should_send_base64_encoded_userdata() throws IOException {
        HttpResponse response = new HttpResponse("{ \"ip\": \"199.92.12.1\", \"ssh\": \"9281\"}", 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(response);
        when(client.deployVM(any(), anyString(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenCallRealMethod();

        client.deployVM("newVm", "orka-default", null, "foo.img", 6, "12", null, null,
                null, false, false, false, false, null, null, null, null, "echo hello");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(anyString(), bodyCaptor.capture());

        String expectedUserdata = Base64.getEncoder()
                .encodeToString("echo hello".getBytes(StandardCharsets.UTF_8));
        JsonObject body = new JsonParser().parse(bodyCaptor.getValue()).getAsJsonObject();
        assertEquals(expectedUserdata, body.get("userdata").getAsString());
    }

    @Test
    public void when_calling_deploy_vm_without_userdata_should_not_send_userdata() throws IOException {
        HttpResponse response = new HttpResponse("{ \"ip\": \"199.92.12.1\", \"ssh\": \"9281\"}", 200, true);

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(response);
        when(client.deployVM(any(), anyString(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenCallRealMethod();

        client.deployVM("newVm", "orka-default", null, "foo.img", 6, "12", null, null,
                null, false, false, false, false, null, null, null, null, null);

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(client).post(anyString(), bodyCaptor.capture());

        assertFalse(bodyCaptor.getValue().contains("userdata"));
    }
}
