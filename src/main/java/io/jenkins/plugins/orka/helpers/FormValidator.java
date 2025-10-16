package io.jenkins.plugins.orka.helpers;

import com.google.cloud.tools.jib.api.ImageReference;

import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.HealthCheckResponse;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;

public class FormValidator {
    private static final Logger logger = Logger.getLogger(FormValidator.class.getName());
    private static final int minDisplayWidth = 320;
    private static final int maxDisplayWidth = 3840;
    private static final int minDisplayHeight = 480;
    private static final int maxDisplayHeight = 2160;
    private static final int minDisplayDpi = 60;
    private static final int maxDisplayDpi = 320;

    private OrkaClientFactory clientFactory;

    public FormValidator(OrkaClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public FormValidation doCheckConfigName(String configName, String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, boolean createNewVMConfig) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        if (createNewVMConfig) {
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

    public FormValidation doCheckImage(String image) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            if (StringUtils.isBlank(image)) {
                return FormValidation.ok();
            }

            ImageReference imageReference = ImageReference.parse(image);

            return FormValidation.ok();

        } catch (InvalidImageReferenceException e) {
            return FormValidation.error("Not a valid image name");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exeption in doCheckImage", e);
        }
        return FormValidation.error("Not a valid image name");
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

    public FormValidation doCheckDisplayWidth(String displayWidth) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            if (StringUtils.isBlank(displayWidth)) {
                return FormValidation.ok();
            }
            Integer width = Integer.parseInt(displayWidth);

            if (width != 0 && (width < minDisplayWidth || width > maxDisplayWidth)) {
                return FormValidation.error(String.format(
                    "Display width shoud be 0 or between %d and %d", minDisplayWidth, maxDisplayWidth));
            }
            return FormValidation.ok();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doCheckDisplayWidth", e);
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckDisplayHeight(String displayHeight) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            if (StringUtils.isBlank(displayHeight)) {
                return FormValidation.ok();
            }
            Integer height = Integer.parseInt(displayHeight);

            if (height != 0 && (height < minDisplayHeight || height > maxDisplayHeight)) {
                return FormValidation.error(String.format(
                    "Display height shoud be 0 or between %d and %d", minDisplayHeight, maxDisplayHeight));
            }
            return FormValidation.ok();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doCheckDisplayHeight", e);
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckDisplayDpi(String displayDpi) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        try {
            if (StringUtils.isBlank(displayDpi)) {
                return FormValidation.ok();
            }
            Integer dpi = Integer.parseInt(displayDpi);

            if (dpi != 0 && (dpi < minDisplayDpi || dpi > maxDisplayDpi)) {
                return FormValidation.error(String.format(
                    "Display dpi shoud be 0 or between %d and %d", minDisplayDpi, maxDisplayDpi));
            }
            return FormValidation.ok();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doCheckDisplayDpi", e);
        }

        return FormValidation.ok();
    }

    public FormValidation doCheckNamespace(String endpoint, String credentialsId, boolean useJenkinsProxySettings,
            boolean ignoreSSLErrors, String namespace) {
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

        if (StringUtils.startsWith(namespace, "orka-")) {
            try {
                if (StringUtils.isNotBlank(endpoint) && StringUtils.isNotBlank(credentialsId)) {
                    OrkaClient client = this.clientFactory.getOrkaClient(endpoint,
                            credentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                    NodeResponse response = client.getNodes(namespace);
                    if (!response.getHttpResponse().getIsSuccessful()) {
                        if (response.getHttpResponse().getCode() == 403) {
                            return FormValidation.error(String.format(
                                    "The user or service account does not have access to namespace: %s", namespace));
                        }
                        logger.fine(String.format("Check namespace failed with %s", response.getMessage()));
                    }

                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception in doCheckNamespace", e);
            }

            return FormValidation.ok();
        }

        return FormValidation.error("Namespace must start with 'orka-'");
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
