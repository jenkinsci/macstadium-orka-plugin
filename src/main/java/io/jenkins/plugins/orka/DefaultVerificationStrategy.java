package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;

import hudson.Extension;
import hudson.model.TaskListener;

import org.kohsuke.stapler.DataBoundConstructor;

public class DefaultVerificationStrategy extends OrkaVerificationStrategy {
    private static final long serialVersionUID = -582491481515659003L;

    @DataBoundConstructor
    public DefaultVerificationStrategy() {
    }

    public boolean verify(String host, int sshPort, StandardUsernameCredentials credentials, TaskListener listener) {
        listener.getLogger().println("Default verification for host " + host + " on port " + sshPort);
        return true;
    }

    @Extension
    public static final class DescriptorImpl extends OrkaVerificationStrategyDescriptor {
        @Override
        public String getDisplayName() {
            return "Default Verification Strategy";
        }
    }
}
