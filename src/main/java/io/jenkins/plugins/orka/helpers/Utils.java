package io.jenkins.plugins.orka.helpers;

import java.util.Date;

public class Utils {
    public static String getTimestamp() {
        return String.format("[%1$tD %1$tT]", new Date());
    }
}