package io.jenkins.plugins.orka;

import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;

@SuppressWarnings("rawtypes")
public class OrkaComputer extends AbstractCloudComputer {
    @SuppressWarnings("unchecked")
    public OrkaComputer(AbstractCloudSlave slave) {
        super(slave);
    }
}
