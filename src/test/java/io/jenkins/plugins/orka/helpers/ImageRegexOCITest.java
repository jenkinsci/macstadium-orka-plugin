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

            { "registry.io/myimage:1.2.3", true },
            { "registry.io:5000/my/repo/image", true },
            { "[2001:db8::1]/repo/image", true },
            { "registry.io/alpine@sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true },

            // INVALID 
            { "", false },
            { " ", false },
            { "MyRepo/Image", false },                    
            { "bad reference", false },
            { "alpine:", false },
            { "alpine@", false },
            { "alpine@sha256:1234", false },
            { "/leading/slash", false },
            { "repo//name", false },
            { "registry.io:/repo/image", false },
            { "alpine:tag with spaces", false },
            { "alpine", false },
            { "alpine:latest", false },
            { "myrepo/myimage", false },
            { "my-repo/my_image-name.1", false },

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
