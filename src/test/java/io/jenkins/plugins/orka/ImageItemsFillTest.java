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
import io.jenkins.plugins.orka.client.Image;
import io.jenkins.plugins.orka.client.ImageResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;

@RunWith(Parameterized.class)
public class ImageItemsFillTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with endpoint={0}, credentials={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] { { "endpoint", "credentials", 2 }, { "endpoint", "credentials", 2 },
                        { null, "credentials", 0 }, { "endpoint", null, 0 }, });
    }

    private OrkaClientFactory clientFactory;
    private final String endpoint;
    private final String credentials;
    private final int resultSize;

    public ImageItemsFillTest(String endpoint, String credentials, int resultSize) {
        this.endpoint = endpoint;
        this.credentials = credentials;
        this.resultSize = resultSize;
    }

    @Before
    public void initialize() throws IOException {
        Image[] images = { new Image("Mojave.img", "", "amd64"), new Image("SnowLeopard.img", "", "amd64") };

        OrkaClient client = mock(OrkaClient.class);

        this.clientFactory = mock(OrkaClientFactory.class);
        when(clientFactory.getOrkaClient(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(client);
        when(client.getImages()).thenReturn(new ImageResponse(Arrays.asList(images), ""));
    }

    @Test
    public void when_fill_image_items_in_orka_agent_should_return_correct_result_size() throws IOException {
        OrkaAgent.DescriptorImpl descriptor = new OrkaAgent.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        ListBoxModel baseImages = descriptor.doFillImageItems(this.endpoint, this.credentials, false,
                false);

        assertEquals(this.resultSize, baseImages.size());
    }

    @Test
    public void when_fill_image_items_in_agent_template_should_return_correct_result_size() throws IOException {
        AgentTemplate.DescriptorImpl descriptor = new AgentTemplate.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        ListBoxModel baseImages = descriptor.doFillImageItems(this.endpoint, this.credentials, false,
                false, "");

        assertEquals(this.resultSize, baseImages.size());
    }
}
