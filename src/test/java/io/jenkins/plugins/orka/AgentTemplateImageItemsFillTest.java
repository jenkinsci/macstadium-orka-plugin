package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

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
public class AgentTemplateImageItemsFillTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with selectedImage={0}, resultSize={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] { { null, null, 5 }, { "sanitized-image", null, 5 },
                        { "SanitizedImage", null, 5 },
                        { "ventura-image", null, 5 },
                        { "Ventura_Image", "ventura-image", 5 },
                        { "missingImage", null, 6 } });
    }

    private OrkaClientFactory clientFactory;
    private final String currentImage;
    private final String selectedImage;
    private final int resultSize;

    public AgentTemplateImageItemsFillTest(String currentImage, String selectedImage, int resultSize) {
        this.currentImage = currentImage;
        this.selectedImage = selectedImage;
        this.resultSize = resultSize;
    }

    @Before
    public void initialize() throws IOException {
        Image[] images = { new Image("Mojave.img", "", "amd64"), new Image("SnowLeopard.img", "", "amd64"),
                new Image("sanitized-image", "", "amd64"), new Image("SanitizedImage", "", "amd64"),
                new Image("ventura-image", "", "arm64"),
        };

        OrkaClient client = mock(OrkaClient.class);

        this.clientFactory = mock(OrkaClientFactory.class);
        when(clientFactory.getOrkaClient(anyString(), anyString(), anyBoolean(), anyBoolean()))
                .thenReturn(client);
        when(client.getImages()).thenReturn(new ImageResponse(Arrays.asList(images), null));
    }

    @Test
    public void when_fill_image_items_in_agent_template_should_select_the_right_image() throws IOException {
        AgentTemplate.DescriptorImpl descriptor = new AgentTemplate.DescriptorImpl();
        descriptor.setclientFactory(this.clientFactory);

        ListBoxModel baseImages = descriptor.doFillBaseImageItems("http://10.221.188.100", "this.credentials", false,
                false, true, this.currentImage);

        assertEquals(this.resultSize, baseImages.size());
        Optional<ListBoxModel.Option> selectedOption = baseImages.stream().filter(o -> o.selected).findFirst();
        if (this.selectedImage != null) {
            assertEquals(this.selectedImage, selectedOption.get().value);
        } else {
            assertTrue(!selectedOption.isPresent());
        }
    }
}
