
package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.common.PasswordCredentials;
import hudson.ProxyConfiguration;
import hudson.util.Secret;
import io.jenkins.plugins.orka.client.OrkaClient;

import java.io.IOException;
import java.net.Proxy;
import jenkins.model.Jenkins;

public class OrkaClientFactory {
    public OrkaClient getOrkaClient(String endpoint, String credentialsId, boolean useJenkinsProxySettings)
            throws IOException {
        return this.getOrkaClient(endpoint, credentialsId, useJenkinsProxySettings, false);
    }

    public OrkaClient getOrkaClient(String endpoint, String credentialsId, boolean useJenkinsProxySettings,
            boolean ignoreSSLErrors)
            throws IOException {
        return this.getOrkaClient(endpoint, credentialsId, 0, useJenkinsProxySettings, ignoreSSLErrors);
    }

    public OrkaClient getOrkaClient(String endpoint, String credentialsId, int httpClientTimeout,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors) throws IOException {

        Secret secret = CredentialsHelper.lookupTokenSecret(credentialsId);
        return new OrkaClient(endpoint, Secret.toString(secret), httpClientTimeout,
                this.getProxy(endpoint, useJenkinsProxySettings), ignoreSSLErrors);
    }

    private Proxy getProxy(String endpoint, boolean useJenkinsProxySettings) {
        if (useJenkinsProxySettings) {
            ProxyConfiguration proxyConfig = Jenkins.get().proxy;
            return proxyConfig == null ? Proxy.NO_PROXY : proxyConfig.createProxy(endpoint);
        }

        return Proxy.NO_PROXY;
    }
}
