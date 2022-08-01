package io.jenkins.plugins.orka;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.TaskListener;
import hudson.slaves.Cloud;
import io.jenkins.plugins.orka.client.HealthCheckResponse;
import io.jenkins.plugins.orka.helpers.OrkaClientProxyFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

@Extension
public class OrkaVersionMonitor extends AsyncPeriodicWork {
    private static final Logger logger = Logger.getLogger(OrkaVersionMonitor.class.getName());
    OrkaClientProxyFactory clientFactory = new OrkaClientProxyFactory();

    public OrkaVersionMonitor() {
        super("Orka version monitor");
    }

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit.HOURS.toMillis(1);
    }

    @Override
    protected void execute(TaskListener listener) throws IOException, InterruptedException {
        logger.fine("Running Orka version monitor");

        Jenkins jenkinsInstance = Jenkins.get();
        for (Cloud cloud : jenkinsInstance.clouds) {
            if ((cloud instanceof OrkaCloud)) {
                OrkaCloud orka = (OrkaCloud) cloud;
                try {
                    HealthCheckResponse healthCheck = clientFactory
                            .getOrkaClientProxy(orka.getEndpoint(), orka.getCredentialsId(), orka.getHttpTimeout(),
                                    orka.getUseJenkinsProxySettings(), orka.getIgnoreSSLErrors())
                            .getHealthCheck();

                    logger.fine("Server: " + orka.getEndpoint() + ". Version: " + healthCheck.getApiVersion());
                    OrkaClientProxyFactory.setServerVersion(orka.getEndpoint(), healthCheck.getApiVersion());
                } catch (Exception e) {
                    logger.warning("Error while getting Orka version: " + e.getMessage());
                }
            }
        }
    }
}
