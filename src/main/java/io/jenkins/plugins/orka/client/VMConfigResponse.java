package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class VMConfigResponse extends ResponseBase {
    @SerializedName("items")
    private List<OrkaVMConfig> configs;

    public VMConfigResponse(List<OrkaVMConfig> configs, String message) {
        super(message);
        this.configs = configs;
    }

    public List<OrkaVMConfig> getConfigs() {
        return this.configs != null ? Collections.unmodifiableList(this.configs) : Collections.emptyList();
    }
}
