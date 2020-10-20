package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;

import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeletionResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.OrkaNode;
import io.jenkins.plugins.orka.client.OrkaVM;
import io.jenkins.plugins.orka.client.TokenStatusResponse;

import java.io.IOException;
import java.util.List;

public class OrkaClientProxy {
    private StandardUsernamePasswordCredentials credentials;
    private String endpoint;
    private int httpClientTimeout;

    public OrkaClientProxy() {
    }

    public OrkaClientProxy(String endpoint, String credentialsId) {
        this.setData(endpoint, credentialsId);
        this.httpClientTimeout = 0;
    }

    public OrkaClientProxy(String endpoint, String credentialsId, int httpClientTimeout) {
        this.setData(endpoint, credentialsId);
        this.httpClientTimeout = httpClientTimeout;
    }

    public void setData(String endpoint, String credentialsId) {
        this.credentials = CredentialsHelper.lookupSystemCredentials(credentialsId);
        this.endpoint = endpoint;
    }

    public List<OrkaVM> getVMs() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getVMs().getVMs();
        }
    }

    public List<OrkaNode> getNodes() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getNodes().getNodes();
        }
    }

    public List<String> getImages() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getImages().getImages();
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

    public TokenStatusResponse getTokenStatus() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getTokenStatus();
        }
    }

    private OrkaClient getOrkaClient() throws IOException {
        return new OrkaClient(this.endpoint, this.credentials.getUsername(), Secret.toString(credentials.getPassword()),
                this.httpClientTimeout);
    }
}