package io.jenkins.plugins.orka.helpers;

import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.OrkaClient;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public class OrkaInfoHelper {
    private static final Logger logger = Logger.getLogger(OrkaInfoHelper.class.getName());
    private OrkaClientFactory clientFactory;

    private static final String[] supportedCPUs = new String[] { "3", "4", "6", "8", "12", "24" };
    private static final String[] supportedSchedulers = new String[] { "default", "most-allocated" };
    private static final String defaultNamespace = "orka-default";

    public OrkaInfoHelper(OrkaClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public ListBoxModel doFillNodeItems(String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors) {

        ListBoxModel model = new ListBoxModel();

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                client.getNodes(defaultNamespace).getNodes().forEach(n -> model.add(n.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillNodeItems", e);
        }

        return model;
    }

    public ListBoxModel doFillVmItems(String orkaEndpoint, String orkaCredentialsId, boolean useJenkinsProxySettings,
            boolean ignoreSSLErrors, boolean createNewVMConfig) {

        ListBoxModel model = new ListBoxModel();

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && !createNewVMConfig && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                client.getVMConfigs().getConfigs().forEach(vm -> model.add(vm.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillVmItems", e);
        }

        return model;
    }

    public ListBoxModel doFillBaseImageItems(String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors, boolean createNewVMConfig) {

        ListBoxModel model = new ListBoxModel();
        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && createNewVMConfig && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings, ignoreSSLErrors);
                client.getImages().getImages().forEach(image -> model.add(image.getName()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillBaseImageItems", e);
        }

        return model;
    }

    public ListBoxModel doFillNumCPUsItems() {
        ListBoxModel model = new ListBoxModel();
        Arrays.stream(supportedCPUs).forEach(cpu -> model.add(cpu));
        return model;
    }

    public ListBoxModel doFillSchedulerItems() {
        ListBoxModel model = new ListBoxModel();
        model.add("config-default", "");
        Arrays.stream(supportedSchedulers).forEach(scheduler -> model.add(scheduler));
        return model;
    }
}
