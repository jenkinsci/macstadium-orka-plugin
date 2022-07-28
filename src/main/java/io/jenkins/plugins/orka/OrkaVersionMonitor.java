package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.model.PeriodicWork;
import hudson.slaves.Cloud;
import io.jenkins.plugins.orka.client.HealthCheckResponse;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

@Extension
public class OrkaVersionMonitor extends PeriodicWork {
    private static final Logger logger = Logger.getLogger(OrkaVersionMonitor.class.getName());
    OrkaClientProxyFactory clientFactory = new OrkaClientProxyFactory();

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit.HOURS.toMillis(1);
    }

    @Override
    protected void doRun() throws Exception {
        Jenkins jenkinsInstance = Jenkins.get();
        for (Cloud cloud : jenkinsInstance.clouds) {
            if ((cloud instanceof OrkaCloud)) {
                OrkaCloud orka = (OrkaCloud) cloud;
                try {
                    HealthCheckResponse healthCheck = clientFactory
                            .getOrkaClientProxy(orka.getEndpoint(), orka.getCredentialsId(), orka.getHttpTimeout(),
                                    orka.getUseJenkinsProxySettings(), orka.getIgnoreSSLErrors())
                            .getHealthCheck();

                    OrkaClientProxyFactory.setServerVersion(orka.getEndpoint(), healthCheck.getApiVersion());
                } catch (Exception e) {
                    logger.warning("Error while getting Orka version: " + e.getMessage());
                }
            }
        }
    }

}
