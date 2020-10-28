
package io.jenkins.plugins.orka.helpers;

import java.io.IOException;

public class OrkaClientProxyFactory {
    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId, boolean useJenkinsProxySettings)
            throws IOException {
        return new OrkaClientProxy(endpoint, credentialsId, 0, useJenkinsProxySettings);
    }

    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId, int httpClientTimeout,
            boolean useJenkinsProxySettings) throws IOException {
        return new OrkaClientProxy(endpoint, credentialsId, httpClientTimeout, useJenkinsProxySettings);
    }
}