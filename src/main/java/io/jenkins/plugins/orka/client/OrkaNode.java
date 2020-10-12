package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

public class OrkaNode {
    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    @SerializedName("total_cpu")
    private int totalCPU;

    @SerializedName("available_cpu")
    private int availableCPU;

    @SerializedName("host_name")
    private String hostname;

    @SerializedName("total_memory")
    private String totalMemory;

    @SerializedName("available_memory")
    private String availableMemory;

    @SerializedName("state")
    private String state;

    public OrkaNode(String name, String address, int totalCPU, int availableCPU, String totalMemory,
            String availableMemory, String hostname, String state) {
        this.name = name;
        this.address = address;
        this.totalCPU = totalCPU;
        this.availableCPU = availableCPU;
        this.totalMemory = totalMemory;
        this.availableMemory = availableMemory;
        this.hostname = hostname;
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public String getAddress() {
        return this.address;
    }

    public int getTotalCPU() {
        return this.totalCPU;
    }

    public int getAvailableCPU() {
        return this.availableCPU;
    }

    public String getTotalMemory() {
        return this.totalMemory;
    }

    public String getAvailableMemory() {
        return this.availableMemory;
    }

    public String getHostname() {
        return this.hostname;
    }

    public String getState() {
        return this.state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + availableCPU;
        result = prime * result + ((availableMemory == null) ? 0 : availableMemory.hashCode());
        result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + totalCPU;
        result = prime * result + ((totalMemory == null) ? 0 : totalMemory.hashCode());
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
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (availableCPU != other.availableCPU) {
            return false;
        }
        if (availableMemory == null) {
            if (other.availableMemory != null) {
                return false;
            }
        } else if (!availableMemory.equals(other.availableMemory)) {
            return false;
        }
        if (hostname == null) {
            if (other.hostname != null) {
                return false;
            }
        } else if (!hostname.equals(other.hostname)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (totalCPU != other.totalCPU) {
            return false;
        }
        if (totalMemory == null) {
            if (other.totalMemory != null) {
                return false;
            }
        } else if (!totalMemory.equals(other.totalMemory)) {
            return false;
        }
        return true;
    }
}