package io.jenkins.plugins.orka.helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ImageRegexOCITest {

    @Parameterized.Parameters(name = "{index}: isValidOCI(\"{0}\") = {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {

            // VALID 

            { "registry.io/image:1.2.3", true },
            { "registry.io/image:tag", true },
            { "registry.io:5000/my/repo/image", true },
            { "registry.io/image@sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true },
            { "localhost/repo/image:tag", true },
            { "localhost:1234/repo/image:tag", true},

            // INVALID 
            { "", false },
            { " ", false },
            { null, false },
            { 7, false},
            { "Repo/Image", false },
            { "bad reference", false },
            { "image:", false },
            { "image@", false },
            { "image@sha256:1234", false },
            { "/leading/slash", false },
            { "repo//name", false },
            { "registry.io:/repo/image", false },
            { "image:tag with spaces", false },
            { "image", false },
            { "image:latest", false },
            { "repo/image", false },
            { "repo/my_image-name.1", false },
            { "registry.io:5403//image:tag", false},
            { "registry.io//:no-repository", false},
            { "registry.io:nonnumberport/repo/image:tag", false },

        });
    }

    private final String image;
    private final boolean expected;

    public ImageRegexOCITest(String image, boolean expected) {
        this.image = image;
        this.expected = expected;
    }

    @Test
    public void testImageRegexOCI() {
        boolean actual = ImageRegexOCI.isValidOCI(this.image);

        if (expected) {
            assertTrue("Expected valid OCI reference: " + image, actual);
        } else {
            assertFalse("Expected invalid OCI reference: " + image, actual);
        }
    }
}
