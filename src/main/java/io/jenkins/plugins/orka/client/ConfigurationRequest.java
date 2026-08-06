package io.jenkins.plugins.orka.client;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Util;
import java.util.Objects;

public class ConfigurationRequest {
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String name;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String image;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private int cpu;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private boolean netBoost;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private boolean gpuPassthrough;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String scheduler;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private float memory;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private String tag;

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    private Boolean tagRequired;

    public ConfigurationRequest(String name, String image, int cpu,
            boolean netBoost, boolean gpuPassthrough, String scheduler, String memory,
            String tag, Boolean tagRequired) {
        this.name = name;
        this.image = image;
        this.cpu = cpu;
        this.netBoost = netBoost;
        this.gpuPassthrough = gpuPassthrough;
        this.scheduler = Util.fixEmptyAndTrim(scheduler) != null ? scheduler : null;
        if (Util.fixEmptyAndTrim(memory) != null && !Objects.equals(memory, "auto") && Float.parseFloat(memory) > 0) {
            this.memory = Float.parseFloat(memory);
        }
        this.tag = Util.fixEmptyAndTrim(tag) != null && tag != null ? tag : null;
        this.tagRequired = tagRequired != null ? tagRequired : null;
    }
}
