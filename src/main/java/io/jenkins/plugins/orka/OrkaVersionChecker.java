package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.slaves.Cloud;

import io.jenkins.plugins.orka.client.HealthCheckResponse;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

import java.util.logging.Logger;
import jenkins.model.Jenkins;

@Extension
public class OrkaVersionChecker {
    private static final Logger logger = Logger.getLogger(OrkaVersionChecker.class.getName());
    private static OrkaClientProxyFactory clientFactory = new OrkaClientProxyFactory();

    @Initializer(after = InitMilestone.JOB_LOADED)
    public void init() {
        OrkaVersionChecker.updateOrkaVersion();
    }

    public static void updateOrkaVersion() {
        logger.fine("Checking Orka version...");

        Jenkins jenkinsInstance = Jenkins.get();
        for (Cloud cloud : jenkinsInstance.clouds) {
            if ((cloud instanceof OrkaCloud)) {
                OrkaCloud orka = (OrkaCloud) cloud;
                updateOrkaVersion(orka.getEndpoint(), orka.getCredentialsId(), orka.getHttpTimeout(),
                        orka.getUseJenkinsProxySettings(), orka.getIgnoreSSLErrors());
            }
        }
    }

    public static void updateOrkaVersion(String endpoint, String credentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors) {
        logger.fine("Checking Orka version for endpoint: " + endpoint);

        updateOrkaVersion(endpoint, credentialsId, 0, useJenkinsProxySettings, ignoreSSLErrors);
    }

    private static void updateOrkaVersion(String endpoint, String credentialsId, int httpTimeout,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors) {
        logger.fine("Checking Orka version for endpoint: " + endpoint);

        try {
            HealthCheckResponse healthCheck = clientFactory
                    .getOrkaClientProxy(endpoint, credentialsId,
                            useJenkinsProxySettings, ignoreSSLErrors)
                    .getHealthCheck();

            logger.fine("Server: " + endpoint + ". Version: " + healthCheck.getApiVersion());
            OrkaClientProxyFactory.setServerVersion(endpoint, healthCheck.getApiVersion());
        } catch (Exception e) {
            logger.warning("Error while getting Orka version: " + e.getMessage());
        }
    }
}
