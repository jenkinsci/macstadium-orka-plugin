package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.RetentionStrategy;
import hudson.util.FormValidation;
import io.jenkins.plugins.orka.helpers.Utils;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;


public class IdleTimeCloudRetentionStrategy extends CloudRetentionStrategy {
    private int idleMinutes;
    public static final int RECOMMENDED_MIN_IDLE = 30;
    
    @DataBoundConstructor
    public IdleTimeCloudRetentionStrategy(int idleMinutes) {
        super(Utils.normalizeIdleTime(idleMinutes, RECOMMENDED_MIN_IDLE));
        
        this.idleMinutes = Utils.normalizeIdleTime(idleMinutes, RECOMMENDED_MIN_IDLE);
    }

    public int getIdleMinutes() {
        return idleMinutes;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends Descriptor<RetentionStrategy<?>> {
        @Override
        public String getDisplayName() {
            return "Keep until idle time expires";
        }
        
        public FormValidation doCheckIdleMinutes(@QueryParameter String value) {
            return Utils.checkInputValue(value);
        }
    }

    private Object readResolve() {
        this.idleMinutes = Utils.normalizeIdleTime(this.idleMinutes, RECOMMENDED_MIN_IDLE);

        return new IdleTimeCloudRetentionStrategy(idleMinutes);
    }
}
