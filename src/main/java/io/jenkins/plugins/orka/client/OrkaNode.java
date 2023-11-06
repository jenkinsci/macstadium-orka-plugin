package io.jenkins.plugins.orka.client;

public class OrkaNode {
    private String name;

    private String nodeIP;

    private int allocatableCpu;

    private int availableCpu;

    private String allocatableMemory;

    private String availableMemory;

    private String phase;

    public OrkaNode(String name, String nodeIP, int allocatableCpu, int availableCpu, String allocatableMemory,
            String availableMemory, String phase) {
        this.name = name;
        this.nodeIP = nodeIP;
        this.allocatableCpu = allocatableCpu;
        this.availableCpu = availableCpu;
        this.allocatableMemory = allocatableMemory;
        this.availableMemory = availableMemory;
        this.phase = phase;
    }

    public String getName() {
        return this.name;
    }

    public String getNodeIP() {
        return this.nodeIP;
    }

    public int getAllocatableCpu() {
        return this.allocatableCpu;
    }

    public int getAvailableCpu() {
        return this.availableCpu;
    }

    public String getAllocatableMemory() {
        return this.allocatableMemory;
    }

    public String getAvailableMemory() {
        return this.availableMemory;
    }

    public String getPhase() {
        return this.phase;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeIP == null) ? 0 : nodeIP.hashCode());
        result = prime * result + availableCpu;
        result = prime * result + ((availableMemory == null) ? 0 : availableMemory.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + allocatableCpu;
        result = prime * result + ((allocatableMemory == null) ? 0 : allocatableMemory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OrkaNode other = (OrkaNode) obj;
        if (nodeIP == null) {
            if (other.nodeIP != null) {
                return false;
            }
        } else if (!nodeIP.equals(other.nodeIP)) {
            return false;
        }
        if (allocatableCpu != other.allocatableCpu) {
            return false;
        }
        if (availableMemory == null) {
            if (other.availableMemory != null) {
                return false;
            }
        } else if (!availableMemory.equals(other.availableMemory)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (phase == null) {
            if (other.phase != null) {
                return false;
            }
        } else if (!phase.equals(other.phase)) {
            return false;
        }
        if (allocatableCpu != other.allocatableCpu) {
            return false;
        }
        if (allocatableMemory == null) {
            if (other.allocatableMemory != null) {
                return false;
            }
        } else if (!allocatableMemory.equals(other.allocatableMemory)) {
            return false;
        }
        return true;
    }
}
