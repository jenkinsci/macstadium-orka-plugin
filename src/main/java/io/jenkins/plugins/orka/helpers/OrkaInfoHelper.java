package io.jenkins.plugins.orka.helpers;

import hudson.util.ListBoxModel;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public class OrkaInfoHelper {
    private static final Logger logger = Logger.getLogger(OrkaInfoHelper.class.getName());
    private OrkaClientProxyFactory clientProxyFactory;

    private static final String[] supportedCPUs = new String[] { "3", "4", "6", "8", "12", "24" };

    public OrkaInfoHelper(OrkaClientProxyFactory clientProxyFactory) {
        this.clientProxyFactory = clientProxyFactory;
    }

    public ListBoxModel doFillNodeItems(String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings) {

        ListBoxModel model = new ListBoxModel();

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                OrkaClientProxy clientProxy = this.clientProxyFactory.getOrkaClientProxy(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings);
                clientProxy.getNodes().stream().filter(ProvisioningHelper::canDeployOnNode)
                        .forEach(n -> model.add(n.getHostname()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillNodeItems", e);
        }

        return model;
    }

    public ListBoxModel doFillVmItems(String orkaEndpoint, String orkaCredentialsId, boolean useJenkinsProxySettings,
            boolean createNewVMConfig) {

        ListBoxModel model = new ListBoxModel();

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && !createNewVMConfig && orkaCredentialsId != null) {
                OrkaClientProxy clientProxy = this.clientProxyFactory.getOrkaClientProxy(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings);
                clientProxy.getVMs().forEach(vm -> model.add(vm.getVMName()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillVmItems", e);
        }

        return model;
    }

    public ListBoxModel doFillBaseImageItems(String orkaEndpoint, String orkaCredentialsId,
            boolean useJenkinsProxySettings, boolean createNewVMConfig) {

        ListBoxModel model = new ListBoxModel();
        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && createNewVMConfig && orkaCredentialsId != null) {
                OrkaClientProxy clientProxy = this.clientProxyFactory.getOrkaClientProxy(orkaEndpoint,
                        orkaCredentialsId, useJenkinsProxySettings);
                clientProxy.getImages().forEach(image -> model.add(image));
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
}