package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.OrkaVMConfig;
import io.jenkins.plugins.orka.client.VMConfigResponse;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;

@RunWith(Parameterized.class)
public class VMItemsFillTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with createNewConfig={0}, endpoint={1}, credentials={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] { { false, "endpoint", "credentials", 2 }, { true, "endpoint", "credentials", 0 },
                        { false, null, "credentials", 0 }, { false, "endpoint", null, 0 }, });
    }

    private OrkaClientFactory clientFactory;
    private final boolean createNewConfig;
    private final String endpoint;
    private final String credentials;
    private final int resultSize;

    public VMItemsFillTest(boolean createNewConfig, String endpoint, String credentials, int resultSize) {
        this.createNewConfig = createNewConfig;
        this.endpoint = endpoint;
        this.credentials = credentials;
        this.resultSize = resultSize;
    }

    @Before
    public void initialize() throws IOException {
        OrkaVMConfig firstVM = new OrkaVMConfig("first", 12, "Mojave.img", 12);
        OrkaVMConfig secondVM = new OrkaVMConfig("second", 24, "Mojave.img", 15);
        VMConfigResponse configResponse = new VMConfigResponse(Arrays.asList(firstVM, secondVM), null);

        OrkaClient client = mock(OrkaClient.class);

        this.clientFactory = mock(OrkaClientFactory.class);
        when(clientFactory.getOrkaClient(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(client);
        when(client.getVMConfigs()).thenReturn(configResponse);
    }

    @Test
    public void when_fill_vm_items_in_orka_agent_should_return_correct_vm_size() throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        ListBoxModel vms = descriptor.doFillVmItems(this.endpoint, this.credentials, false, false,
                this.createNewConfig);

        assertEquals(this.resultSize, vms.size());
    }

    @Test
    public void when_fill_vm_items_in_agent_template_should_return_correct_vm_size() throws IOException {
        AgentTemplate.DescriptorImpl descriptor = new AgentTemplate.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        ListBoxModel vms = descriptor.doFillVmItems(this.endpoint, this.credentials, false, false,
                this.createNewConfig);

        assertEquals(this.resultSize, vms.size());
    }
}
