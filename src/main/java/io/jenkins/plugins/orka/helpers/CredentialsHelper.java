package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import hudson.security.ACL;
import hudson.util.ListBoxModel;

import java.util.Collections;
import jenkins.model.Jenkins;

public final class CredentialsHelper {
    public static StandardUsernamePasswordCredentials lookupSystemCredentials(final String credentialsId) {
        return lookupSystemCredentials(credentialsId, StandardUsernamePasswordCredentials.class);
    }

    public static <C extends Credentials> C lookupSystemCredentials(final String credentialsId, final Class<C> type) {
        return CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(type, Jenkins.get(), ACL.SYSTEM, Collections.emptyList()),
                CredentialsMatchers.withId(credentialsId));
    }

    public static ListBoxModel getCredentials(Class type) {
        return new StandardListBoxModel().includeEmptyValue().includeMatchingAs(ACL.SYSTEM, Jenkins.get(), type,
                Collections.emptyList(), CredentialsMatchers.always());
    }
}
