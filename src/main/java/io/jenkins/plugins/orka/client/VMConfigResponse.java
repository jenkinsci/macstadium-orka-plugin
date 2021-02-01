package io.jenkins.plugins.orka.client;

import java.util.Collections;
import java.util.List;

public class VMConfigResponse extends ResponseBase {
    private List<OrkaVMConfig> configs;

    public VMConfigResponse(List<OrkaVMConfig> configs, String message, OrkaError[] errors) {
        super(message, errors);
        this.configs = configs;
    }

    public List<OrkaVMConfig> getConfigs() {
        return this.configs != null ? Collections.unmodifiableList(this.configs) : Collections.emptyList();
    }
}