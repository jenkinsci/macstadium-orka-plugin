package io.jenkins.plugins.orka.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenCallRealMethod();

        DeploymentResponse actualResponse = client.deployVM("newVm", "orka-default", null, "foo.img", 6, "12", null,
                null,
                null, false,false, false, false, null, 1500, 2000, 100);

        assertEquals(ip, actualResponse.getIP());
        assertEquals(sshPort, actualResponse.getSSH());
    }

    @Test
    public void when_calling_get_emulator_configs_should_get_all_configs() throws IOException {
        OrkaEmulatorConfig[] configs = { new OrkaEmulatorConfig("pixel9-api36", "android-36", "google_apis", "pixel_9"),
                new OrkaEmulatorConfig("tablet-api35", "android-35", "google_apis_playstore", null) };
        String body = "{ \"items\": " + new Gson().toJson(configs) + "}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.get(anyString())).thenReturn(new HttpResponse(body, 200, true));
        when(client.getEmulatorConfigs()).thenCallRealMethod();

        EmulatorConfigResponse actualResponse = client.getEmulatorConfigs();

        assertArrayEquals(configs, actualResponse.getConfigs().toArray());
        verify(client).get(endsWith("/api/v1/namespaces/orka-default/emulatorconfigs/android"));
    }

    @Test
    public void when_calling_deploy_emulator_should_return_relay_address() throws IOException {
        String body = "{\"name\":\"vm-abcde-emu-1\",\"status\":\"Running\",\"nodeName\":\"mini-arm-34\","
                + "\"relayIP\":\"192.168.64.1\",\"relayPort\":15556}";

        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(new HttpResponse(body, 200, true));
        when(client.deployEmulator(any(), any(), any(), any(), any(), any())).thenCallRealMethod();

        EmulatorDeploymentResponse actualResponse = client.deployEmulator("orka-default", "vm-abcde", "pixel9-api36",
                "vm-abcde-emu-1", null, 10);

        assertTrue(actualResponse.isSuccessful());
        assertEquals("vm-abcde-emu-1", actualResponse.getName());
        assertEquals("192.168.64.1", actualResponse.getRelayIP());
        assertEquals(15556, actualResponse.getRelayPort());
        verify(client).post(endsWith("/api/v1/namespaces/orka-default/emulators/android"), anyString());
    }

    @Test
    public void when_deploying_emulator_without_key_should_omit_key() throws IOException {
        JsonObject request = this.captureEmulatorDeployRequest("  ");

        assertEquals("vm-abcde", request.get("vmName").getAsString());
        assertEquals("pixel9-api36", request.get("config").getAsString());
        assertEquals("vm-abcde-emu-1", request.get("name").getAsString());
        assertEquals(10, request.get("timeout").getAsInt());
        assertFalse(request.has("adbPublicKey"));
        assertFalse(request.has("shouldGenerateName"));
    }

    @Test
    public void when_deploying_emulator_with_key_should_send_trimmed_key() throws IOException {
        JsonObject request = this.captureEmulatorDeployRequest("  QAAAAkey= jenkins@ci\n");

        assertEquals("QAAAAkey= jenkins@ci", request.get("adbPublicKey").getAsString());
    }

    @Test
    public void when_cluster_has_no_emulator_routes_should_report_unsupported() throws IOException {
        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(new HttpResponse("404 page not found", 404, false));
        when(client.deployEmulator(any(), any(), any(), any(), any(), any())).thenCallRealMethod();

        EmulatorDeploymentResponse actualResponse = client.deployEmulator("orka-default", "vm-abcde", "pixel9-api36",
                "vm-abcde-emu-1", null, 10);

        assertFalse(actualResponse.isSuccessful());
        assertEquals("Android emulators require Orka 4.0.0 or later", actualResponse.getMessage());
    }

    @Test
    public void when_emulator_config_is_missing_should_report_api_message() throws IOException {
        String body = "{\"kind\":\"Status\",\"status\":\"Failure\","
                + "\"message\":\"emulator config \\\"missing\\\" not found\",\"code\":404}";
        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(new HttpResponse(body, 404, false));
        when(client.deployEmulator(any(), any(), any(), any(), any(), any())).thenCallRealMethod();

        EmulatorDeploymentResponse actualResponse = client.deployEmulator("orka-default", "vm-abcde", "missing",
                "vm-abcde-emu-1", null, 10);

        assertFalse(actualResponse.isSuccessful());
        assertEquals("emulator config \"missing\" not found", actualResponse.getMessage());
    }

    @Test
    public void when_deleting_emulator_should_accept_empty_body() throws IOException {
        OrkaClient client = mock(OrkaClient.class);
        when(client.delete(anyString())).thenReturn(new HttpResponse("", 200, true));
        when(client.deleteEmulator(anyString(), anyString())).thenCallRealMethod();

        DeletionResponse actualResponse = client.deleteEmulator("vm-abcde-emu-1", "orka-default");

        assertTrue(actualResponse.isSuccessful());
        verify(client).delete(endsWith("/api/v1/namespaces/orka-default/emulators/android/vm-abcde-emu-1"));
    }

    private JsonObject captureEmulatorDeployRequest(String adbPublicKey) throws IOException {
        String body = "{\"name\":\"vm-abcde-emu-1\",\"relayIP\":\"192.168.64.1\",\"relayPort\":15556}";
        OrkaClient client = mock(OrkaClient.class);
        when(client.post(anyString(), any())).thenReturn(new HttpResponse(body, 200, true));
        when(client.deployEmulator(any(), any(), any(), any(), any(), any())).thenCallRealMethod();

        client.deployEmulator("orka-default", "vm-abcde", "pixel9-api36", "vm-abcde-emu-1", adbPublicKey, 10);

        ArgumentCaptor<String> requestBody = ArgumentCaptor.forClass(String.class);
        verify(client).post(anyString(), requestBody.capture());
        return new Gson().fromJson(requestBody.getValue(), JsonObject.class);
    }
}
