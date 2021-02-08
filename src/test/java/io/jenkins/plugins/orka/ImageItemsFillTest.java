package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.helpers.OrkaClientProxy;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

@RunWith(Parameterized.class)
public class ImageItemsFillTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with createNewConfig={0}, endpoint={1}, credentials={2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] { { true, "endpoint", "credentials", 2 }, { false, "endpoint", "credentials", 0 },
                        { true, null, "credentials", 0 }, { true, "endpoint", null, 0 }, });
    }

    private OrkaClientProxyFactory clientProxyFactory;
    private final boolean createNewConfig;
    private final String endpoint;
    private final String credentials;
    private final int resultSize;

    public ImageItemsFillTest(boolean createNewConfig, String endpoint, String credentials, int resultSize) {
        this.createNewConfig = createNewConfig;
        this.endpoint = endpoint;
        this.credentials = credentials;
        this.resultSize = resultSize;
    }

    @Before
    public void initialize() throws IOException {
        String[] response = { "Mojave.img", "SnowLeopard.img" };

        OrkaClientProxy clientProxy = mock(OrkaClientProxy.class);

        this.clientProxyFactory = mock(OrkaClientProxyFactory.class);
        when(clientProxyFactory.getOrkaClientProxy(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(clientProxy);
        when(clientProxy.getImages()).thenReturn(Arrays.asList((response)));
    }

    @Test
    public void when_fill_image_items_in_orka_agent_should_return_correct_result_size() throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        descriptor.setClientProxyFactory(this.clientProxyFactory);

        ListBoxModel baseImages = descriptor.doFillBaseImageItems(this.endpoint, this.credentials, false,
                false, this.createNewConfig);

        assertEquals(this.resultSize, baseImages.size());
    }

    @Test
    public void when_fill_image_items_in_agent_template_should_return_correct_result_size() throws IOException {
        AgentTemplate.DescriptorImpl descriptor = new AgentTemplate.DescriptorImpl();
        descriptor.setClientProxyFactory(this.clientProxyFactory);

        ListBoxModel baseImages = descriptor.doFillBaseImageItems(this.endpoint, this.credentials, false,
                false, this.createNewConfig);

        assertEquals(this.resultSize, baseImages.size());
    }
}