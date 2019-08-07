package io.jenkins.plugins.orka.helpers;

import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.client.OrkaClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

public class OrkaInfoHelper {
    private static final Logger logger = Logger.getLogger(OrkaInfoHelper.class.getName());
    private ClientFactory clientFactory;

    public OrkaInfoHelper(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public ListBoxModel doFillNodeItems(String orkaEndpoint, String orkaCredentialsId) {

        ListBoxModel model = new ListBoxModel();

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint, orkaCredentialsId);
                client.getNodes().stream().filter(ProvisioningHelper::canDeployOnNode)
                        .forEach(n -> model.add(n.getHostname()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillNodeItems", e);
        }

        return model;
    }

    public ListBoxModel doFillVmItems(String orkaEndpoint, String orkaCredentialsId, boolean createNewVMConfig) {

        ListBoxModel model = new ListBoxModel();

        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && !createNewVMConfig && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint, orkaCredentialsId);
                client.getVMs().forEach(vm -> model.add(vm.getVMName()));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillVmItems", e);
        }

        return model;
    }

    public ListBoxModel doFillBaseImageItems(String orkaEndpoint, String orkaCredentialsId, boolean createNewVMConfig) {

        ListBoxModel model = new ListBoxModel();
        try {
            if (StringUtils.isNotBlank(orkaEndpoint) && createNewVMConfig && orkaCredentialsId != null) {
                OrkaClient client = this.clientFactory.getOrkaClient(orkaEndpoint, orkaCredentialsId);
                client.getImages().forEach(image -> model.add(image));
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception in doFillBaseImageItems", e);
        }

        return model;
    }
}