package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.EnvVars;
import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.slaves.NodeProperty;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.EmulatorConfigResponse;
import io.jenkins.plugins.orka.client.EmulatorDeploymentResponse;
import io.jenkins.plugins.orka.client.HttpResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.OrkaEmulatorConfig;
import io.jenkins.plugins.orka.helpers.FormValidator;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
import jenkins.model.Jenkins;

public class AgentTemplateTest {
    private static final String ip = "127.0.0.1";
    private static final int sshPort = 2101;
    private static final String id = "vm-machine";
    private static final String adbPublicKey = "QAAAA" + StringUtils.repeat("A", 694) + "= jenkins@ci";

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void when_provision_should_get_agents() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        OrkaCloud cloud = this.getCloud();
        agentTemplate.setParent(cloud);

        OrkaProvisionedAgent provisionedAgent = agentTemplate.provision();

        assertEquals(agentTemplate.getLabelString(), provisionedAgent.getLabelString());
        assertEquals(agentTemplate.getMode(), provisionedAgent.getMode());
        assertEquals(agentTemplate.getNumExecutors(), provisionedAgent.getNumExecutors());
        assertEquals(agentTemplate.getRemoteFS(), provisionedAgent.getRemoteFS());
        assertEquals(agentTemplate.getVmCredentialsId(), provisionedAgent.getVmCredentialsId());
        assertEquals(ip, provisionedAgent.getHost());
        assertEquals(sshPort, provisionedAgent.getSshPort());
        assertEquals(id, provisionedAgent.getVmId());
        assertTrue(provisionedAgent.getEmulatorNames().isEmpty());
        assertNull(provisionedAgent.getNodeProperties().get(EnvironmentVariablesNodeProperty.class));
        verify(cloud, never()).deployEmulator(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    public void when_provision_with_emulators_should_attach_relay_variables() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate(Collections.singletonList(
                new EnvironmentVariablesNodeProperty(new EnvironmentVariablesNodeProperty.Entry("FOO", "bar"))));
        agentTemplate.setEmulators(Arrays.asList(new AndroidEmulatorTemplate("pixel9-api36"),
                new AndroidEmulatorTemplate("tablet-api35")));
        agentTemplate.setAdbPublicKey(adbPublicKey);
        OrkaCloud cloud = this.getCloud();
        when(cloud.deployEmulator(any(), any(), any(), any(), any(), anyInt())).thenAnswer(
                invocation -> new EmulatorDeploymentResponse(invocation.getArgument(3), "Running", "mini-arm-34",
                        "192.168.64.1", invocation.getArgument(3).toString().endsWith("1") ? 15556 : 15558, null));
        agentTemplate.setParent(cloud);

        OrkaProvisionedAgent provisionedAgent = agentTemplate.provision();

        verify(cloud).deployEmulator(Constants.DEFAULT_NAMESPACE, id, "pixel9-api36", id + "-emu-1", adbPublicKey, 10);
        verify(cloud).deployEmulator(Constants.DEFAULT_NAMESPACE, id, "tablet-api35", id + "-emu-2", adbPublicKey, 10);
        assertEquals(Arrays.asList(id + "-emu-1", id + "-emu-2"), provisionedAgent.getEmulatorNames());

        List<EnvironmentVariablesNodeProperty> envProperties = provisionedAgent.getNodeProperties()
                .getAll(EnvironmentVariablesNodeProperty.class);
        assertEquals(1, envProperties.size());
        EnvVars env = envProperties.get(0).getEnvVars();
        assertEquals("bar", env.get("FOO"));
        assertEquals("2", env.get("ORKA_EMULATOR_COUNT"));
        assertEquals("192.168.64.1:15556", env.get("ORKA_EMULATOR_ADB"));
        assertEquals("192.168.64.1:15558", env.get("ORKA_EMULATOR_TABLET_API35_ADB"));

        // The template's own property must not pick up one agent's emulators.
        assertEquals(Collections.singleton("FOO"), agentTemplate.getNodeProperties()
                .get(EnvironmentVariablesNodeProperty.class).getEnvVars().keySet());
    }

    @Test
    public void when_emulator_deploy_fails_should_delete_vm() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        agentTemplate.setEmulators(Collections.singletonList(new AndroidEmulatorTemplate("missing")));
        OrkaCloud cloud = this.getCloud();
        EmulatorDeploymentResponse failure = mock(EmulatorDeploymentResponse.class);
        when(failure.isSuccessful()).thenReturn(false);
        when(failure.getHttpResponse()).thenReturn(new HttpResponse("{}", 404, false));
        when(failure.getMessage()).thenReturn("emulator config \"missing\" not found");
        when(cloud.deployEmulator(any(), any(), any(), any(), any(), anyInt())).thenReturn(failure);
        agentTemplate.setParent(cloud);

        try {
            agentTemplate.provision();
            fail("Expected the provision to fail");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("emulator config \"missing\" not found"));
        }

        verify(cloud).deleteVM(id, Constants.DEFAULT_NAMESPACE);
    }

    @Test
    public void when_emulator_has_no_relay_should_delete_vm() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        agentTemplate.setEmulators(Collections.singletonList(new AndroidEmulatorTemplate("pixel9-api36")));
        OrkaCloud cloud = this.getCloud();
        when(cloud.deployEmulator(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new EmulatorDeploymentResponse(id + "-emu-1", "Running", "mini-arm-34", null, 0, null));
        agentTemplate.setParent(cloud);

        try {
            agentTemplate.provision();
            fail("Expected the provision to fail");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("no ADB relay address"));
        }

        verify(cloud).deleteVM(id, Constants.DEFAULT_NAMESPACE);
    }

    @Test
    public void when_cloud_is_serialized_should_keep_emulator_settings() {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        agentTemplate.setEmulators(Collections.singletonList(new AndroidEmulatorTemplate("pixel9-api36")));
        agentTemplate.setAdbPublicKey(adbPublicKey);
        agentTemplate.setEmulatorDeployTimeout(15);
        OrkaCloud cloud = new OrkaCloud("cloud", "credentialsId", "endpoint", null, 0, false, null,
                Collections.singletonList(agentTemplate));

        OrkaCloud reloaded = (OrkaCloud) Jenkins.XSTREAM2.fromXML(Jenkins.XSTREAM2.toXML(cloud));
        AgentTemplate reloadedTemplate = reloaded.getTemplates().get(0);

        assertEquals("pixel9-api36", reloadedTemplate.getEmulators().get(0).getConfig());
        assertEquals(adbPublicKey, reloadedTemplate.getAdbPublicKey());
        assertEquals(15, reloadedTemplate.getEmulatorDeployTimeout());
    }

    @Test
    public void when_template_has_no_emulator_settings_should_use_defaults() {
        AgentTemplate agentTemplate = this.getAgentTemplate();

        AgentTemplate reloaded = (AgentTemplate) Jenkins.XSTREAM2.fromXML(Jenkins.XSTREAM2.toXML(agentTemplate));

        assertTrue(reloaded.getEmulators().isEmpty());
        assertNull(reloaded.getAdbPublicKey());
        assertEquals(10, reloaded.getEmulatorDeployTimeout());
    }

    @Test
    public void when_checking_adb_public_key_should_accept_only_public_keys() {
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckAdbPublicKey("").kind);
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckAdbPublicKey(adbPublicKey).kind);
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckAdbPublicKey(" " + adbPublicKey + "\n").kind);
        assertEquals(FormValidation.Kind.OK,
                FormValidator.doCheckAdbPublicKey("QAAAA" + StringUtils.repeat("A", 694) + "=").kind);
        assertEquals(FormValidation.Kind.ERROR,
                FormValidator.doCheckAdbPublicKey("-----BEGIN PRIVATE KEY-----\nMIIE\n-----END PRIVATE KEY-----").kind);
        assertEquals(FormValidation.Kind.ERROR,
                FormValidator.doCheckAdbPublicKey("ssh-rsa AAAAB3NzaC1yc2E jenkins@ci").kind);
        assertEquals(FormValidation.Kind.ERROR,
                FormValidator.doCheckAdbPublicKey("QAAAA" + StringUtils.repeat("A", 693) + "= jenkins@ci").kind);
    }

    @Test
    public void when_filling_emulator_configs_should_list_cluster_configs() throws IOException {
        OrkaClient client = mock(OrkaClient.class);
        when(client.getEmulatorConfigs()).thenReturn(new EmulatorConfigResponse(Arrays.asList(
                new OrkaEmulatorConfig("pixel9-api36", "android-36", "google_apis", "pixel_9"),
                new OrkaEmulatorConfig("tablet-api35", "android-35", "google_apis_playstore", null)), null));
        OrkaClientFactory clientFactory = mock(OrkaClientFactory.class);
        when(clientFactory.getOrkaClient(eq("endpoint"), eq("credentials"), anyBoolean(), anyBoolean()))
                .thenReturn(client);
        AndroidEmulatorTemplate.DescriptorImpl descriptor = (AndroidEmulatorTemplate.DescriptorImpl) r.jenkins
                .getDescriptorOrDie(AndroidEmulatorTemplate.class);
        descriptor.setClientFactory(clientFactory);

        ListBoxModel items = descriptor.doFillConfigItems("endpoint", "credentials", false, false);

        assertEquals(Arrays.asList("pixel9-api36", "tablet-api35"),
                Arrays.asList(items.get(0).value, items.get(1).value));
        assertTrue(descriptor.doFillConfigItems(null, "credentials", false, false).isEmpty());
    }

    private OrkaCloud getCloud() throws IOException {
        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new DeploymentResponse(ip, sshPort, id, null));
        when(cloud.getRealHost(anyString())).thenReturn(ip);
        return cloud;
    }

    private AgentTemplate getAgentTemplate() {
        return this.getAgentTemplate(null);
    }

    private AgentTemplate getAgentTemplate(List<? extends NodeProperty<?>> nodeProperties) {
        return new AgentTemplate("vmCredentialsId", "orka3xOption", null, "baseImage", 12, null,
                Constants.DEFAULT_NAMESPACE, true, false, false, "default", null, null, null, null, null, false, 1500,
                2000, 100, null, null, 1, Mode.NORMAL, "remoteFS", "label", new IdleTimeCloudRetentionStrategy(5),
                nodeProperties, null, null);
    }
}
