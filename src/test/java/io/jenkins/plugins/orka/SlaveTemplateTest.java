package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import io.jenkins.plugins.orka.client.DeploymentResponse;

public class SlaveTemplateTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Test
    public void when_provision_should_get_slaves() throws IOException, FormException {
        SlaveTemplate slaveTemplate = this.getSlaveTemplate();
        String ip = "127.0.0.1";
        int sshPort = 2101;
        String id = "vm-machine";

        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(anyString(), anyString())).thenReturn(new DeploymentResponse(ip, sshPort, id, null, null));
        slaveTemplate.setParent(cloud);

        String node = "macpro-1";

        OrkaProvisionedSlave provisionedSlave = slaveTemplate.provision(node);

        assertEquals(slaveTemplate.getLabelString(), provisionedSlave.getLabelString());
        assertEquals(slaveTemplate.getMode(), provisionedSlave.getMode());
        assertEquals(slaveTemplate.getNumExecutors(), provisionedSlave.getNumExecutors());
        assertEquals(slaveTemplate.getRemoteFS(), provisionedSlave.getRemoteFS());
        assertEquals(slaveTemplate.getVmCredentialsId(), provisionedSlave.getVmCredentialsId());
        assertEquals(ip, provisionedSlave.getHost());
        assertEquals(sshPort, provisionedSlave.getSshPort());
        assertEquals(id, provisionedSlave.getVmId());
        assertEquals(node, provisionedSlave.getNode());
    }

    private SlaveTemplate getSlaveTemplate() {
        return new SlaveTemplate("vmCredentialsId", "my-vm", false, "configName", "baseImage", "image", 12, 1,
                "remoteFS", Mode.NORMAL, "label", 5, Collections.emptyList());
    }
}