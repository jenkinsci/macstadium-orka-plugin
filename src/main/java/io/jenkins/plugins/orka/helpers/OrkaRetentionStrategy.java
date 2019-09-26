package io.jenkins.plugins.orka.helpers;

import hudson.model.Descriptor;
import hudson.slaves.RetentionStrategy;

import io.jenkins.plugins.orka.IdleTimeCloudRetentionStrategy;
import io.jenkins.plugins.orka.RunOnceCloudRetentionStrategy;

import java.util.Arrays;
import java.util.List;

public class OrkaRetentionStrategy {
    public static List<Descriptor<RetentionStrategy<?>>> getRetentionStrategyDescriptors() {
        return Arrays.asList(IdleTimeCloudRetentionStrategy.DESCRIPTOR, RunOnceCloudRetentionStrategy.DESCRIPTOR);
    }
}