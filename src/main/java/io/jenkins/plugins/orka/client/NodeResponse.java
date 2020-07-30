package io.jenkins.plugins.orka.client;

import java.util.Collections;
import java.util.List;

public class NodeResponse extends ResponseBase {
    private List<OrkaNode> nodes;

    public NodeResponse(List<OrkaNode> nodes, String message, OrkaError[] errors) {
        super(message, errors);
        this.nodes = nodes;
    }

    public List<OrkaNode> getNodes() {
        return this.nodes != null ? Collections.unmodifiableList(this.nodes) : Collections.emptyList();
    }
}
