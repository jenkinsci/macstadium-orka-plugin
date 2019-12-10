package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class AddressMapper implements Describable<AddressMapper> {
    private String defaultHost;
    private String redirectHost;

    @DataBoundConstructor
    public AddressMapper(String defaultHost, String redirectHost) {
        this.defaultHost = defaultHost;
        this.redirectHost = redirectHost;
    }

    public String getDefaultHost() {
        return this.defaultHost;
    }

    public String getRedirectHost() {
        return this.redirectHost;
    }

    public void setDefaultHost(String defaultHost) {
        this.defaultHost = defaultHost;
    }

    public void setRedirectHost(String redirectHost) {
        this.redirectHost = redirectHost;
    }

    @Override
    public Descriptor<AddressMapper> getDescriptor() {
        return Jenkins.getInstance().getDescriptor(getClass());
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<AddressMapper> {
    }
}