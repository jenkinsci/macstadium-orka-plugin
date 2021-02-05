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

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.OrkaNode;
import io.jenkins.plugins.orka.client.OrkaVM;
import io.jenkins.plugins.orka.helpers.OrkaClientProxy;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

@RunWith(Parameterized.class)
public class NodeResourcesCheckTest {
    private final static String FIRST_NODE = "macpro-1";
    private final static String SECOND_NODE = "macpro-2";
    private final static String THIRD_NODE = "macpro-3";

    private final static String SMALL_VM = "small-vm";
    private final static String BIG_VM = "big-vm";

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with firstNodeAvailableCPU={0}, secondNodeAvailableCPU={1}, thirdNodeAvailableCPU={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { FIRST_NODE, SMALL_VM, false, 0, FormValidation.Kind.OK },
                { FIRST_NODE, BIG_VM, false, 0, FormValidation.Kind.ERROR },
                { SECOND_NODE, SMALL_VM, false, 0, FormValidation.Kind.OK },
                { SECOND_NODE, BIG_VM, false, 0, FormValidation.Kind.ERROR },
                { THIRD_NODE, BIG_VM, false, 0, FormValidation.Kind.ERROR },
                { THIRD_NODE, BIG_VM, false, 0, FormValidation.Kind.ERROR },
                { FIRST_NODE, null, true, 12, FormValidation.Kind.OK },
                { FIRST_NODE, null, true, 24, FormValidation.Kind.ERROR },
                { SECOND_NODE, null, true, 12, FormValidation.Kind.OK },
                { SECOND_NODE, null, true, 24, FormValidation.Kind.ERROR },
                { THIRD_NODE, null, true, 24, FormValidation.Kind.ERROR },
                { THIRD_NODE, null, true, 24, FormValidation.Kind.ERROR }, });
    }

    private final String selectedNode;
    private final String vm;
    private final boolean createNewVMConfig;
    private final int requiredCPU;
    private final FormValidation.Kind validationKind;

    public NodeResourcesCheckTest(String selectedNode, String vm, boolean createNewVMConfig, int requiredCPU,
            FormValidation.Kind validationKind) {
        this.selectedNode = selectedNode;
        this.vm = vm;
        this.createNewVMConfig = createNewVMConfig;
        this.requiredCPU = requiredCPU;
        this.validationKind = validationKind;
    }

    @Test
    public void when_check_node_should_return_correct_validation_type() throws IOException {
        OrkaNode firstNode = new OrkaNode(FIRST_NODE, "127.0.0.1", 12, 12, "32Gi", "20Gi", FIRST_NODE, "ready");
        OrkaNode secondNode = new OrkaNode(SECOND_NODE, "127.0.0.2", 24, 12, "64Gi", "32Gi", SECOND_NODE, "ready");
        OrkaNode thirdNode = new OrkaNode(THIRD_NODE, "127.0.0.3", 24, 0, "64Gi", "32Gi", THIRD_NODE, "ready");
        List<OrkaNode> response = Arrays.asList(firstNode, secondNode, thirdNode);

        OrkaVM firstVM = new OrkaVM(BIG_VM, "not deployed", 24, "Mojave.img", "firstImage", "default");
        OrkaVM secondVM = new OrkaVM(SMALL_VM, "not deployed", 12, "Mojave.img", "secondImage", "default");

        List<OrkaVM> vmResponse = Arrays.asList(firstVM, secondVM);

        OrkaClientProxyFactory factory = mock(OrkaClientProxyFactory.class);
        OrkaClientProxy clientProxy = mock(OrkaClientProxy.class);

        when(factory.getOrkaClientProxy(anyString(), anyString(), anyBoolean(), anyBoolean())).thenReturn(clientProxy);
        when(clientProxy.getNodes()).thenReturn(response);
        when(clientProxy.getVMs()).thenReturn(vmResponse);

        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        descriptor.setClientProxyFactory(factory);

        FormValidation validation = descriptor.doCheckNode(this.selectedNode, "endpoint", "credentialsId", false, false,
                this.vm, this.createNewVMConfig, this.requiredCPU);

        assertEquals(this.validationKind, validation.kind);
    }
}