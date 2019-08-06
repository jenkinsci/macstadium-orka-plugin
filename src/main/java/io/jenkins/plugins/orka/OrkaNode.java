package io.jenkins.plugins.orka;

public class OrkaNode {
    private String name;
    private int vmCapacity;

    public OrkaNode(String name, int vmCapacity) {
        this.name = name;
        this.vmCapacity = vmCapacity;
    }

    public String getName() {
        return name;
    }

    public int getVmCapacity() {
        return vmCapacity;
    }
}