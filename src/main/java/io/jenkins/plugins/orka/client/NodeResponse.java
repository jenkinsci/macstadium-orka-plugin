package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class NodeResponse extends ResponseBase {
    @SerializedName("items")
    private List<OrkaNode> nodes;

    public NodeResponse(List<OrkaNode> nodes, String message) {
        super(message);
        this.nodes = nodes;
    }

    public List<OrkaNode> getNodes() {
        return this.nodes != null ? Collections.unmodifiableList(this.nodes) : Collections.emptyList();
    }
}
