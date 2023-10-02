package io.jenkins.plugins.orka.helpers;

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.HealthCheckResponse;
import io.jenkins.plugins.orka.client.OrkaClient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

public class FormValidator {
    private static final Logger logger = Logger.getLogger(FormValidator.class.getName());

    private OrkaClientFactory clientFactory;

    public FormValidator(OrkaClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public FormValidation doCheckConfigName(String configName, String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, boolean createNewVMConfig) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        if (createNewVMConfig) {
            if (configName.length() < 5) {
                return FormValidation.error("Configuration name should NOT be shorter than 5 characters");
            }

            try {
                if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                    OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint,
                            orkaCredentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                    boolean alreadyInUse = client.getVMConfigs().getConfigs().stream()
                            .anyMatch(vmc -> vmc.getName().equalsIgnoreCase(configName));
                    if (alreadyInUse) {
                        return FormValidation.error("Configuration name is already in use");
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception in doCheckConfigName", e);
            }
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckMemory(String memory) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            if (StringUtils.isBlank(memory) || StringUtils.equals(memory, "auto")) {
                return FormValidation.ok();
            }
            if (Float.parseFloat(memory) <= 0) {
                return FormValidation.error("Memory should be greater than 0");
            }
            return FormValidation.ok();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doCheckMemory", e);
        }

        return FormValidation.error("Memory should be greater than 0");
    }

    public FormValidation doTestConnection(String credentialsId, String endpoint, boolean useJenkinsProxySettings,
            boolean ignoreSSLErrors)
            throws IOException {

        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            HealthCheckResponse response = new OrkaClientFactory()
                    .getOrkaClient(endpoint, credentialsId, useJenkinsProxySettings,
                            ignoreSSLErrors)
                    .getHealthCheck();
            if (!response.isSuccessful()) {
                return failedConnection(Utils.getErrorMessage(response));
            }
        } catch (IOException e) {
            return failedConnection(e.getMessage());
        }

        return FormValidation.ok("Connection Successful");
    }

    private static FormValidation failedConnection(String message) {
        return FormValidation.error("Connection failed with: " + message);
    }
}
