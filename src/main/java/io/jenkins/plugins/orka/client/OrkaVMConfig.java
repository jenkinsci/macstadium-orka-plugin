package io.jenkins.plugins.orka.client;

import com.google.gson.annotations.SerializedName;

public class OrkaVMConfig {

    @SerializedName("orka_vm_name")
    private String name;

    @SerializedName("orka_cpu_core")
    private int cpuCount;

    @SerializedName("orka_base_image")
    private String baseImage;

    private int memory;

    public OrkaVMConfig(String name, int cpuCount, String baseImage) {
        this(name, cpuCount, baseImage, 0);
    }

    public OrkaVMConfig(String name, int cpuCount, String baseImage, int memory) {
        this.name = name;
        this.cpuCount = cpuCount;
        this.baseImage = baseImage;
        if (memory > 0) {
            this.memory = memory;
        }
    }

    public String getName() {
        return this.name;
    }

    public int getCPUCount() {
        return this.cpuCount;
    }

    public String getBaseImage() {
        return this.baseImage;
    }

    public int getMemory() {
        return this.memory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseImage == null) ? 0 : baseImage.hashCode());
        result = prime * result + cpuCount;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        OrkaVMConfig other = (OrkaVMConfig) obj;
        if (baseImage == null) {
            if (other.baseImage != null) {
                return false;
            }
        } else if (!baseImage.equals(other.baseImage)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}