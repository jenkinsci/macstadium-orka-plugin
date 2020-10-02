package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;

import hudson.ExtensionPoint;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.TaskListener;

import java.io.Serializable;

public abstract class OrkaVerificationStrategy extends AbstractDescribableImpl<OrkaVerificationStrategy>
        implements ExtensionPoint, Serializable {

    private static final long serialVersionUID = 8557164621833739416L;

    public boolean verify(String host, int sshPort, StandardUsernameCredentials credentials, TaskListener listener) {
        return true;
    }

    public abstract static class OrkaVerificationStrategyDescriptor extends Descriptor<OrkaVerificationStrategy> {
    }
}
