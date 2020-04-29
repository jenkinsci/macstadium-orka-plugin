package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;

import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeletionResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.VMResponse;

import java.io.IOException;
import java.util.List;

public class OrkaClientProxy {

    private StandardUsernamePasswordCredentials credentials;
    private String endpoint;

    public OrkaClientProxy() {
    }

    public OrkaClientProxy(String endpoint, String credentialsId) {
        this.setData(endpoint, credentialsId);
    }

    public void setData(String endpoint, String credentialsId) {
        this.credentials = CredentialsHelper.lookupSystemCredentials(credentialsId);
        this.endpoint = endpoint;
    }

    public List<VMResponse> getVMs() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getVMs();
        }
    }

    public List<NodeResponse> getNodes() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getNodes();
        }
    }

    public List<String> getImages() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getImages();
        }
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount) throws IOException {

        try (OrkaClient client = getOrkaClient()) {
            return client.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount);
        }
    }

    public DeploymentResponse deployVM(String vmName) throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.deployVM(vmName);
        }
    }

    public DeploymentResponse deployVM(String vmName, String node) throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.deployVM(vmName, node);
        }
    }

    public DeletionResponse deleteVM(String vmName) throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.deleteVM(vmName);
        }
    }

    public DeletionResponse deleteVM(String vmName, String node) throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.deleteVM(vmName, node);
        }
    }

    private OrkaClient getOrkaClient() throws IOException {
        return new OrkaClient(this.endpoint, this.credentials.getUsername(),
                Secret.toString(credentials.getPassword()));
    }
}