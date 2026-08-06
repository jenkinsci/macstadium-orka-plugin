package io.jenkins.plugins.orka.helpers;

import hudson.Util;
import java.security.SecureRandom;

public class VMNameGenerator {
    
    private static final String charset = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int randomSuffixLength = 5;
    private static final SecureRandom random = new SecureRandom();
    private static final String defaultPrefix = "vm";
    
    public static String generateName(String prefix) {
        if (Util.fixEmptyAndTrim(prefix) == null) {
            prefix = defaultPrefix;
        }

        StringBuilder result = new StringBuilder(prefix.length() + 1 + randomSuffixLength);
        result.append(prefix).append("-");
        
        for (int i = 0; i < randomSuffixLength; i++) {
            result.append(charset.charAt(random.nextInt(charset.length())));
        }
        
        return result.toString();
    }
}
