package io.jenkins.plugins.orka.helpers;

import hudson.model.Descriptor;
import io.jenkins.plugins.orka.DefaultVerificationStrategy;
import io.jenkins.plugins.orka.OrkaVerificationStrategy;
import java.util.List;
import jenkins.model.Jenkins;

public class OrkaVerificationStrategyProvider {
    public static List<Descriptor<OrkaVerificationStrategy>> getVerificationStrategyDescriptors() {
        return Jenkins.getInstance().getDescriptorList(OrkaVerificationStrategy.class);
    }

    public static Descriptor<OrkaVerificationStrategy> getDefaultVerificationDescriptor() {
        return OrkaVerificationStrategyProvider.getVerificationStrategyDescriptors().stream()
                .filter(x -> x.getClass().equals(DefaultVerificationStrategy.DescriptorImpl.class)).findFirst().get();
    }
}