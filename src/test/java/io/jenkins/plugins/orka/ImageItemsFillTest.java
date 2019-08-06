package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
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
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.helpers.ClientFactory;

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

    private ClientFactory factory;
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
        OrkaClient client = mock(OrkaClient.class);

        this.factory = mock(ClientFactory.class);
        when(factory.getOrkaClient(anyString(), anyString())).thenReturn(client);
        when(client.getImages()).thenReturn(Arrays.asList((response)));
    }

    @Test
    public void when_fill_image_items_in_orka_slave_should_return_correct_result_size() throws IOException {
        OrkaSlave.DescriptorImpl descriptor = new OrkaSlave.DescriptorImpl();
        descriptor.setClientFactory(this.factory);

        ListBoxModel baseImages = descriptor.doFillBaseImageItems(this.endpoint, this.credentials,
                this.createNewConfig);

        assertEquals(this.resultSize, baseImages.size());
    }

    @Test
    public void when_fill_image_items_in_slave_template_should_return_correct_result_size() throws IOException {
        SlaveTemplate.DescriptorImpl descriptor = new SlaveTemplate.DescriptorImpl();
        descriptor.setClientFactory(this.factory);

        ListBoxModel baseImages = descriptor.doFillBaseImageItems(this.endpoint, this.credentials,
                this.createNewConfig);

        assertEquals(this.resultSize, baseImages.size());
    }
}