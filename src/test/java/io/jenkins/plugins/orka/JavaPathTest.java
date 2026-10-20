package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.Util;
import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import hudson.plugins.sshslaves.SSHLauncher;
import io.jenkins.plugins.orka.client.DeploymentResponse;

public class JavaPathTest {
    private static final String javaPath = "/opt/homebrew/opt/openjdk@25/bin/java";

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void when_template_has_java_path_should_pass_it_to_ssh_launcher() throws IOException, FormException {
        OrkaProvisionedAgent provisionedAgent = this.provision(this.getAgentTemplate(javaPath));

        assertEquals(javaPath, provisionedAgent.getJavaPath());
        assertEquals(javaPath, this.getSSHLauncher(provisionedAgent).getJavaPath());
    }

    @Test
    public void when_template_has_blank_java_path_should_use_null() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate("  ");
        OrkaProvisionedAgent provisionedAgent = this.provision(agentTemplate);

        assertNull(agentTemplate.getJavaPath());
        assertNull(provisionedAgent.getJavaPath());
        assertNull(Util.fixEmpty(this.getSSHLauncher(provisionedAgent).getJavaPath()));
    }

    @Test
    public void when_static_agent_has_java_path_should_pass_it_to_ssh_launcher() throws IOException, FormException {
        OrkaAgent agent = this.getOrkaAgent(javaPath);
        OrkaComputerLauncher launcher = (OrkaComputerLauncher) agent.getLauncher();

        assertEquals(javaPath, agent.getJavaPath());
        assertEquals(javaPath, launcher.getJavaPath());
        assertEquals(javaPath, launcher.getLauncher("vmCredentialsId").getJavaPath());
    }

    @Test
    public void when_static_agent_has_blank_java_path_should_use_null() throws IOException, FormException {
        OrkaAgent agent = this.getOrkaAgent(" ");
        OrkaComputerLauncher launcher = (OrkaComputerLauncher) agent.getLauncher();

        assertNull(agent.getJavaPath());
        assertNull(launcher.getJavaPath());
        assertNull(Util.fixEmpty(launcher.getLauncher("vmCredentialsId").getJavaPath()));
    }

    private OrkaProvisionedAgent provision(AgentTemplate agentTemplate) throws IOException, FormException {
        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new DeploymentResponse("127.0.0.1", 2101, "vm-machine", null));
        when(cloud.getRealHost(anyString())).thenReturn("127.0.0.1");
        agentTemplate.setParent(cloud);

        return agentTemplate.provision();
    }

    private SSHLauncher getSSHLauncher(OrkaProvisionedAgent provisionedAgent) {
        return ((WaitSSHLauncher) provisionedAgent.getLauncher()).getLauncher();
    }

    private AgentTemplate getAgentTemplate(String javaPath) {
        return new AgentTemplate("vmCredentialsId", "orka3xOption", null, "baseImage", 12, null,
                Constants.DEFAULT_NAMESPACE, true, false, false, "default", null, null, null, null, null, false,
                1500, 2000, 100, null, null, 1, Mode.NORMAL, "remoteFS", "label",
                new IdleTimeCloudRetentionStrategy(5), null, null, javaPath);
    }

    private OrkaAgent getOrkaAgent(String javaPath) throws IOException, FormException {
        return new OrkaAgent("name", "orkaCredentialsId", "orkaEndpoint", "vmCredentialsId", "node",
                Constants.DEFAULT_NAMESPACE, "namePrefix", null, "image", 12, false, false, false, null, null, null,
                1, "127.0.0.1", 2101, "remoteFS", false, false, null, javaPath, null, null, null, null);
    }
}
