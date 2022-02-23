
package io.jenkins.plugins.orka.helpers;

import java.io.IOException;

public class OrkaClientProxyFactory {
    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId, boolean useJenkinsProxySettings)
            throws IOException {
        return this.getOrkaClientProxy(endpoint, credentialsId, useJenkinsProxySettings, false);
    }

    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId, boolean useJenkinsProxySettings,
            boolean ignoreSSLErrors)
            throws IOException {
        return new OrkaClientProxy(endpoint, credentialsId, 0, useJenkinsProxySettings, ignoreSSLErrors);
    }

    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId, int httpClientTimeout,
            boolean useJenkinsProxySettings) throws IOException {
        return this.getOrkaClientProxy(endpoint, credentialsId, httpClientTimeout, useJenkinsProxySettings,
                false);
    }

    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId, int httpClientTimeout,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors) throws IOException {
        return new OrkaClientProxy(endpoint, credentialsId, httpClientTimeout, useJenkinsProxySettings,
                ignoreSSLErrors);
    }
}