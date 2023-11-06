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
                response.getMessage());
    }

    public static int normalizeIdleTime(int userValue, int recommended) {
        return userValue > 0 ? userValue : recommended;
    }

    public static String sanitizeK8sName(String name) {
        return name.toLowerCase().replace("_", "-");
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
        String firstVersionWithoutPreview = firstVersion.split("-")[0].replace(".", "");
        String secondVersionWithoutPreview = secondVersion.split("-")[0].replace(".", "");

        firstVersionWithoutPreview = firstVersionWithoutPreview.length() == 3 ? firstVersionWithoutPreview
                : firstVersionWithoutPreview + "0";
        secondVersionWithoutPreview = secondVersionWithoutPreview.length() == 3 ? secondVersionWithoutPreview
                : secondVersionWithoutPreview + "0";

        Integer v1 = Integer.parseInt(firstVersionWithoutPreview);
        Integer v2 = Integer.parseInt(secondVersionWithoutPreview);

        if (v1.intValue() == v2.intValue()) {
            Boolean isFirstVersionPreview = firstVersion.contains("-");
            Boolean isSecondVersionPreview = secondVersion.contains("-");

            if (isFirstVersionPreview && isSecondVersionPreview || !isFirstVersionPreview && !isSecondVersionPreview) {
                return 0;
            } else if (isFirstVersionPreview) {
                return -1;
            } else if (isSecondVersionPreview) {
                return 1;
            }
        }
        return v1.compareTo(v2);
    }
}
