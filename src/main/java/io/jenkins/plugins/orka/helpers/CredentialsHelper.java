package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

public final class CredentialsHelper {
    public static StandardUsernamePasswordCredentials lookupSystemCredentials(final String credentialsId) {
        return lookupSystemCredentials(credentialsId, StandardUsernamePasswordCredentials.class);
    }

    public static <C extends Credentials> C lookupSystemCredentials(final String credentialsId, final Class<C> type) {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        return CredentialsMatchers.firstOrNull(CredentialsProvider.lookupCredentials(type, Jenkins.getInstance()),
                CredentialsMatchers.withId(credentialsId));
    }

    public static ListBoxModel getCredentials(Class type) {
        Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
        return new StandardListBoxModel().includeEmptyValue().withMatching(CredentialsMatchers.always(),
                CredentialsProvider.lookupCredentials(type, Jenkins.getInstance()));
    }
}
