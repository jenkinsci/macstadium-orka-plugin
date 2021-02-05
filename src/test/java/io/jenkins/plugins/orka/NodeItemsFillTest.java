package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.OrkaNode;
import io.jenkins.plugins.orka.helpers.OrkaClientProxy;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

@RunWith(Parameterized.class)
public class NodeItemsFillTest {
    private final static int NO_CPU_LEFT = 0;
    private final static int HAS_CPU_LEFT = 12;

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with firstNodeAvailableCPU={0}, secondNodeAvailableCPU={1}, thirdNodeAvailableCPU={2}, endpoint={3}, credentials={4}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { HAS_CPU_LEFT, NO_CPU_LEFT, HAS_CPU_LEFT, "endpoint", "credentials", 2 },
                { NO_CPU_LEFT, NO_CPU_LEFT, NO_CPU_LEFT, "endpoint", "credentials", 0 },
                { HAS_CPU_LEFT, NO_CPU_LEFT, NO_CPU_LEFT, null, "credentials", 0 },
                { NO_CPU_LEFT, NO_CPU_LEFT, HAS_CPU_LEFT, "endpoint", null, 0 }, });
    }

    private final int firstNodeAvailableCPU;
    private final int secondNodeAvailableCPU;
    private final int thirdNodeAvailableCPU;
    private final String endpoint;
    private final String credentials;
    private final int resultSize;

    public NodeItemsFillTest(int firstNodeAvailableCPU, int secondNodeAvailableCPU, int thirdNodeAvailableCPU,
            String endpoint, String credentials, int resultSize) {
        this.firstNodeAvailableCPU = firstNodeAvailableCPU;
        this.secondNodeAvailableCPU = secondNodeAvailableCPU;
        this.thirdNodeAvailableCPU = thirdNodeAvailableCPU;
        this.endpoint = endpoint;
        this.credentials = credentials;
        this.resultSize = resultSize;
    }

    @Test
    public void when_fill_node_items_should_return_correct_size() throws IOException {
        OrkaNode firstNode = new OrkaNode("macpro-1", "127.0.0.1", 12, this.firstNodeAvailableCPU, "32Gi", "20Gi",
                "macpro-1", "ready");
        OrkaNode secondNode = new OrkaNode("macpro-2", "127.0.0.2", 24, this.secondNodeAvailableCPU, "64Gi", "32Gi",
                "macpro2", "ready");
        OrkaNode thirdNode = new OrkaNode("macpro-3", "127.0.0.3", 24, this.thirdNodeAvailableCPU, "64Gi", "32Gi",
                "macpro-3", "ready");
        List<OrkaNode> response = Arrays.asList(firstNode, secondNode, thirdNode);

        OrkaClientProxyFactory clientProxyFactory = mock(OrkaClientProxyFactory.class);
        OrkaClientProxy clientProxy = mock(OrkaClientProxy.class);

        when(clientProxyFactory.getOrkaClientProxy(anyString(), anyString(), anyBoolean(), anyBoolean()))
                        .thenReturn(clientProxy);
        when(clientProxy.getNodes()).thenReturn(response);

        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        descriptor.setClientProxyFactory(clientProxyFactory);

        ListBoxModel nodes = descriptor.doFillNodeItems(this.endpoint, this.credentials, false, false);

        assertEquals(this.resultSize, nodes.size());
    }
}