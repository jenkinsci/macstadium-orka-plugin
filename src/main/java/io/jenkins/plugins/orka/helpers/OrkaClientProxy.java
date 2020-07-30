package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.util.Secret;

import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeletionResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.ImageResponse;
import io.jenkins.plugins.orka.client.NodeResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.TokenResponse;
import io.jenkins.plugins.orka.client.VMResponse;

import java.io.IOException;

public class OrkaClientProxy implements AutoCloseable {
    private transient String credentialsId;
    private String endpoint;
    private Secret token;
    private transient OrkaClient client;

    public OrkaClientProxy(String endpoint, String credentialsId) throws IOException {
        this.credentialsId = credentialsId;
        this.endpoint = endpoint;
        this.client = new OrkaClient(this.endpoint);

        StandardUsernamePasswordCredentials credentials = CredentialsHelper.lookupSystemCredentials(this.credentialsId);
        this.token = Secret.fromString(this.getToken(credentials).getToken());
        this.client.setToken(Secret.toString(this.token));
    }

    protected Object readResolve() throws IOException {
        this.client = new OrkaClient(this.endpoint);
        this.client.setToken(Secret.toString(this.token));

        return this;
    }

    public VMResponse getVMs() throws IOException {
        return this.client.getVMs();
    }

    public NodeResponse getNodes() throws IOException {
        return this.client.getNodes();
    }

    public ImageResponse getImages() throws IOException {
        return this.client.getImages();
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount) throws IOException {
        return this.client.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount);
    }

    public DeploymentResponse deployVM(String vmName) throws IOException {
        return this.client.deployVM(vmName);
    }

    public DeploymentResponse deployVM(String vmName, String node) throws IOException {
        return this.client.deployVM(vmName, node);
    }

    public DeletionResponse deleteVM(String vmName) throws IOException {
        return this.client.deleteVM(vmName);
    }

    public DeletionResponse deleteVM(String vmName, String node) throws IOException {
        return this.client.deleteVM(vmName, node);
    }

    private TokenResponse getToken(StandardUsernamePasswordCredentials credentials) throws IOException {
        return this.client.requestToken(credentials.getUsername(), Secret.toString(credentials.getPassword()));
    }

    @Override
    public void close() throws IOException {
        if (this.client != null) {
            this.client.close();
            this.client = null;
            this.token = null;
        }
    }
}