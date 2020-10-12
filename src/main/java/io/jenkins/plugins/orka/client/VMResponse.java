package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class VMResponse extends ResponseBase {

    @SerializedName("virtual_machine_resources")
    private List<OrkaVM> vms;

    public VMResponse(List<OrkaVM> vms, String message, OrkaError[] errors) {
        super(message, errors);
        this.vms = vms;
    }

    public List<OrkaVM> getVMs() {
        return this.vms != null ? Collections.unmodifiableList(this.vms) : Collections.emptyList();
    }
}