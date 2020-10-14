package io.jenkins.plugins.orka.helpers;

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
}