package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class EmulatorConfigResponse extends ResponseBase {
    @SerializedName("items")
    private List<OrkaEmulatorConfig> configs;

    public EmulatorConfigResponse(List<OrkaEmulatorConfig> configs, String message) {
        super(message);
        this.configs = configs;
    }

    public List<OrkaEmulatorConfig> getConfigs() {
        return this.configs != null ? Collections.unmodifiableList(this.configs) : Collections.emptyList();
    }
}
