package io.jenkins.plugins.orka;

import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;

public class OrkaComputer extends AbstractCloudComputer {
    public OrkaComputer(AbstractCloudSlave slave) {
        super(slave);
    }
}
