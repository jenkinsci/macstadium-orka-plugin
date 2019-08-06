package io.jenkins.plugins.orka.helpers;

public class StringHelper {
    public static boolean nullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}