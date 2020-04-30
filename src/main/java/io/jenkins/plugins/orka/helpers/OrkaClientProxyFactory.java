
package io.jenkins.plugins.orka.helpers;

import java.io.IOException;

public class OrkaClientProxyFactory {
    public OrkaClientProxy getOrkaClientProxy(String endpoint, String credentialsId) throws IOException {
        return new OrkaClientProxy(endpoint, credentialsId);
    }
}