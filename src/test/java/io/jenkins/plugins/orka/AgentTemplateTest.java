package io.jenkins.plugins.orka;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import io.jenkins.plugins.orka.client.DeploymentResponse;

public class AgentTemplateTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void when_provision_should_get_agents() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        String ip = "127.0.0.1";
        int sshPort = 2101;
        String id = "vm-machine";

        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any()))
                .thenReturn(new DeploymentResponse(ip, sshPort, id, null));
        when(cloud.getRealHost(anyString())).thenReturn(ip);
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
    }

    @SuppressWarnings("deprecation")
    private AgentTemplate getAgentTemplate() {
        return new AgentTemplate("vmCredentialsId", "my-vm", false, "configName", "baseImage", 12, true,
                false, 1,
                "remoteFS",
                Mode.NORMAL, "label", "prefix", new IdleTimeCloudRetentionStrategy(5),
                null,
                Collections.emptyList(), null, "default", "", false, null, false);
    }
}
