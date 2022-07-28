package io.jenkins.plugins.orka.helpers;

import hudson.util.FormValidation;

import io.jenkins.plugins.orka.client.ResponseBase;

import java.util.Date;

public class Utils {
    public static String getTimestamp() {
        return String.format("[%1$tD %1$tT]", new Date());
    }

    public static String getErrorMessage(ResponseBase response) {
        return String.format("HTTP Code: %s, Error: %s", response.getHttpResponse().getCode(),
                response.getErrorMessage());
    }

    public static int normalizeIdleTime(int userValue, int recommended) {
        return userValue > 0 ? userValue : recommended;
    }

    public static FormValidation checkInputValue(String userInput) {
        try {
            int idleMinutesValue = Integer.parseInt(userInput);

            if (idleMinutesValue <= 0) {
                return FormValidation.error("Idle timeout must be a positive number.");
            }

            return FormValidation.ok();
        } catch (NumberFormatException e) {
            return FormValidation.error("Idle timeout must be a positive number.");
        }
    }

    public static int compareVersions(String firstVersion, String secondVersion) {
        int comparisonResult = 0;

        String[] firstVersionSplits = firstVersion.split("\\.");
        String[] secondVersionSplits = secondVersion.split("\\.");
        int maxLengthOfVersionSplits = Math.max(firstVersionSplits.length, secondVersionSplits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer v1 = i < firstVersionSplits.length ? Integer.parseInt(firstVersionSplits[i]) : 0;
            Integer v2 = i < secondVersionSplits.length ? Integer.parseInt(secondVersionSplits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }
}
