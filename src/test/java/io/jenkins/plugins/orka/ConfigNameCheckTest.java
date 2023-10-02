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

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.OrkaVMConfig;
import io.jenkins.plugins.orka.client.VMConfigResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;

@RunWith(Parameterized.class)
public class ConfigNameCheckTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with name={0}, configName={1}, validation={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { "second", "uniqueName", FormValidation.Kind.OK },
                { "second", "second", FormValidation.Kind.ERROR }, { "second", "s", FormValidation.Kind.ERROR }, });
    }

    private OrkaClientFactory clientFactory;
    private final String configName;
    private final FormValidation.Kind validationKind;

    public ConfigNameCheckTest(String vmName, String newConfigName, FormValidation.Kind validationKind) {
        this.configName = newConfigName;
        this.validationKind = validationKind;
    }

    @Before
    public void initialize() throws IOException {
        OrkaVMConfig firstVM = new OrkaVMConfig("first", 12, "Mojave.img", 12);
        OrkaVMConfig secondVM = new OrkaVMConfig("second", 24, "Mojave.img", 15);

        List<OrkaVMConfig> response = Arrays.asList(firstVM, secondVM);

        OrkaClient client = mock(OrkaClient.class);

        this.clientFactory = mock(OrkaClientFactory.class);
        when(clientFactory.getOrkaClient(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(client);
        when(client.getVMConfigs()).thenReturn(new VMConfigResponse(response, null));
    }

    @Test
    public void when_check_config_name_in_orka_agent_should_return_correct_validation_kind() throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        FormValidation validation = descriptor.doCheckConfigName(this.configName, "127.0.0.1", "credentialsId", false,
                false, true);

        assertEquals(this.validationKind, validation.kind);
    }

    @Test
    public void when_check_config_name_in_agent_template_should_return_correct_validation_kind() throws IOException {
        AgentTemplate.DescriptorImpl descriptor = new AgentTemplate.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        FormValidation validation = descriptor.doCheckConfigName(this.configName, "127.0.0.1", "credentialsId", false,
                false, true);

        assertEquals(this.validationKind, validation.kind);
    }
}
