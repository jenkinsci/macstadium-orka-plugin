package io.jenkins.plugins.orka.helpers;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.regex.Pattern;

public class VMNameGeneratorTest {
    private static final Pattern VALID_CHARSET = Pattern.compile("^[a-z0-9]+$");
    private static final int EXPECTED_SUFFIX_LENGTH = 5;
    
    @Test
    public void testBasicFunctionality() {
        String prefix = "test";
        String result = VMNameGenerator.generateName(prefix);
        
        assertTrue(result.startsWith("test-"));
        
        assertEquals(prefix.length() + 1 + EXPECTED_SUFFIX_LENGTH, result.length());
        
        String suffix = result.substring(prefix.length() + 1);
        assertTrue(VALID_CHARSET.matcher(suffix).matches());
    }

    @Test
    public void testNoPrefix() {
        String result = VMNameGenerator.generateName(null);
        String expectedPrefix = "vm";
        
        assertTrue(result.startsWith(expectedPrefix + "-"));
        
        assertEquals(expectedPrefix.length() + 1 + EXPECTED_SUFFIX_LENGTH, result.length());
        
        String suffix = result.substring(expectedPrefix.length() + 1);
        assertTrue(VALID_CHARSET.matcher(suffix).matches());
    }
    
    @Test
    public void testWithDifferentPrefixes() {
        String[] prefixes = {"nginx", "my-pod", "web-server", "app123", "a"};
        
        for (String prefix : prefixes) {
            String result = VMNameGenerator.generateName(prefix);
            
            assertTrue(result.startsWith(prefix + "-"));
            assertEquals(prefix.length() + 1 + EXPECTED_SUFFIX_LENGTH, result.length());
            
            String suffix = result.substring(prefix.length() + 1);
            assertTrue(VALID_CHARSET.matcher(suffix).matches());
        }
    }
    
}
