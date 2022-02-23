package io.jenkins.plugins.orka.helpers;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import hudson.ProxyConfiguration;
import hudson.util.Secret;

import io.jenkins.plugins.orka.client.ConfigurationResponse;
import io.jenkins.plugins.orka.client.DeletionResponse;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.client.OrkaClient;
import io.jenkins.plugins.orka.client.OrkaNode;
import io.jenkins.plugins.orka.client.OrkaVM;
import io.jenkins.plugins.orka.client.OrkaVMConfig;
import io.jenkins.plugins.orka.client.TokenStatusResponse;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

import jenkins.model.Jenkins;

public class OrkaClientProxy {
    private StandardUsernamePasswordCredentials credentials;
    private String endpoint;
    private int httpClientTimeout;
    private boolean ignoreSSLErrors;
    private Proxy proxy;

    public OrkaClientProxy(String endpoint, String credentialsId, int httpClientTimeout,
            boolean useJenkinsProxySettings) {
        this(endpoint, credentialsId, httpClientTimeout, useJenkinsProxySettings, false);
    }

    public OrkaClientProxy(String endpoint, String credentialsId, int httpClientTimeout,
            boolean useJenkinsProxySettings, boolean ignoreSSLErrors) {
        this.credentials = CredentialsHelper.lookupSystemCredentials(credentialsId);
        this.endpoint = endpoint;
        this.httpClientTimeout = httpClientTimeout;
        this.proxy = this.getProxy(useJenkinsProxySettings);
        this.ignoreSSLErrors = ignoreSSLErrors;
    }

    public List<OrkaVM> getVMs() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getVMs().getVMs();
        }
    }

    public List<OrkaVMConfig> getVMConfigs() throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.getVMConfigs().getConfigs();
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
        return this.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount, null);
    }

    public ConfigurationResponse createConfiguration(String vmName, String image, String baseImage,
            String configTemplate, int cpuCount, String scheduler) throws IOException {

        try (OrkaClient client = getOrkaClient()) {
            return client.createConfiguration(vmName, image, baseImage, configTemplate, cpuCount, scheduler);
        }
    }

    public DeploymentResponse deployVM(String vmName) throws IOException {
        return this.deployVM(vmName, null);
    }

    public DeploymentResponse deployVM(String vmName, String node) throws IOException {
        return this.deployVM(vmName, node, null);
    }

    public DeploymentResponse deployVM(String vmName, String node, String scheduler) throws IOException {
        try (OrkaClient client = getOrkaClient()) {
            return client.deployVM(vmName, node, scheduler);
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
                this.httpClientTimeout, this.proxy, this.ignoreSSLErrors);
    }

    private Proxy getProxy(boolean useJenkinsProxySettings) {
        if (useJenkinsProxySettings) {
            ProxyConfiguration proxyConfig = Jenkins.get().proxy;
            return proxyConfig == null ? Proxy.NO_PROXY : proxyConfig.createProxy(this.endpoint);
        }

        return Proxy.NO_PROXY;
    }
}