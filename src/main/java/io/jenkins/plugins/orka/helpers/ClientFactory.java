
package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;
import io.jenkins.plugins.orka.client.OrkaClient;
import java.io.IOException;

public class ClientFactory {
    public OrkaClient getOrkaClient(String endpoint, String credentialsId) throws IOException {
        StandardUsernamePasswordCredentials credentials = CredentialsHelper.lookupSystemCredentials(credentialsId);
        return new OrkaClient(endpoint, credentials.getUsername(), Secret.toString(credentials.getPassword()));
    }
}