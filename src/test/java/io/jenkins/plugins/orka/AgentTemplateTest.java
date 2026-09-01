package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import hudson.util.Secret;
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
                any(), any(), any(), any(), any(), any(), any(), any()))
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

    @Test
    public void when_provision_with_userdata_should_pass_script_to_cloud() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        String script = "#!/bin/bash\necho hello";
        agentTemplate.setUserdata(Secret.fromString(script));

        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new DeploymentResponse("127.0.0.1", 2101, "vm-machine", null));
        when(cloud.getRealHost(anyString())).thenReturn("127.0.0.1");
        agentTemplate.setParent(cloud);

        agentTemplate.provision();

        verify(cloud).deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), eq(script));
    }

    @Test
    public void when_provision_with_blank_userdata_should_pass_null_to_cloud() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate();
        agentTemplate.setUserdata(Secret.fromString("   "));

        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new DeploymentResponse("127.0.0.1", 2101, "vm-machine", null));
        when(cloud.getRealHost(anyString())).thenReturn("127.0.0.1");
        agentTemplate.setParent(cloud);

        agentTemplate.provision();

        verify(cloud).deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), isNull());
    }

    private AgentTemplate getAgentTemplate() {  
        return new AgentTemplate("vmCredentialsId", "orka3xOption", null, "baseImage", 12, null, Constants.DEFAULT_NAMESPACE, true, false, false, "default", null, null, null, null, null, false, 1500, 2000, 100, 1, Mode.NORMAL, "remoteFS", "label", new IdleTimeCloudRetentionStrategy(5), null, null);
    }
}
