package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.helpers.ClientFactory;

@RunWith(Parameterized.class)
public class NodeNameCheckTest {
    private final static int NO_CPU_LEFT = 0;
    private final static int HAS_CPU_LEFT = 12;

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with firstNodeAvailableCPU={0}, secondNodeAvailableCPU={1}, thirdNodeAvailableCPU={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { HAS_CPU_LEFT, NO_CPU_LEFT, HAS_CPU_LEFT, FormValidation.Kind.OK },
            { NO_CPU_LEFT, NO_CPU_LEFT, NO_CPU_LEFT, FormValidation.Kind.ERROR },
        });
    }

    private final int firstNodeAvailableCPU;
    private final int secondNodeAvailableCPU;
    private final int thirdNodeAvailableCPU;
    private final FormValidation.Kind validationKind;
    
    public NodeNameCheckTest(int firstNodeAvailableCPU, int secondNodeAvailableCPU, int thirdNodeAvailableCPU, FormValidation.Kind validationKind) {
        this.firstNodeAvailableCPU = firstNodeAvailableCPU;
        this.secondNodeAvailableCPU = secondNodeAvailableCPU;
        this.thirdNodeAvailableCPU = thirdNodeAvailableCPU;
        this.validationKind = validationKind;
    }

    @Test
    public void when_check_node_should_return_correct_validation_type() throws IOException {
        NodeResponse firstNode = new NodeResponse("macpro-1", "127.0.0.1", 12, this.firstNodeAvailableCPU, "32Gi", "20Gi", "macpro-1", "ready");
        NodeResponse secondNode = new NodeResponse("macpro-2", "127.0.0.2", 24, this.secondNodeAvailableCPU, "64Gi", "32Gi", "macpro2", "ready");
        NodeResponse thirdNode = new NodeResponse("macpro-3", "127.0.0.3", 24, this.thirdNodeAvailableCPU, "64Gi", "32Gi", "macpro-3", "ready");
        List<NodeResponse> response = Arrays.asList(firstNode, secondNode, thirdNode);

        ClientFactory factory = mock(ClientFactory.class);
        OrkaClient client = mock(OrkaClient.class);

        when(factory.getOrkaClient(anyString(), anyString())).thenReturn(client);
        when(client.getNodes()).thenReturn(response);

        OrkaSlave.DescriptorImpl descriptor = new OrkaSlave.DescriptorImpl();
        descriptor.setClientFactory(factory);
        
        FormValidation validation = descriptor.doCheckNode(null, "endpoint", "credentialsId", null, false, 12);

        assertEquals(this.validationKind, validation.kind);
    }
}