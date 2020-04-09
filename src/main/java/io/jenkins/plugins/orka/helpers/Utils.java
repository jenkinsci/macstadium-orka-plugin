package io.jenkins.plugins.orka.helpers;

import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Utils {
    public static String getTimestamp() {
        return String.format("[%1$tD %1$tT]", new Date());
    }

    public static String getAsString(Object obj) {
        return ReflectionToStringBuilder.toString(obj, ToStringStyle.MULTI_LINE_STYLE);
    }
}