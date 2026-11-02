package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;

import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.Descriptor.FormException;
import hudson.model.Node.Mode;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.util.FormValidation;
import io.jenkins.plugins.orka.client.DeploymentResponse;
import io.jenkins.plugins.orka.helpers.FormValidator;
import jenkins.model.Jenkins;

public class JavaPathTest {
    private static final String javaPath = "/opt/homebrew/opt/openjdk@25/bin/java";

    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Test
    public void when_template_has_java_path_should_pass_it_to_ssh_launcher() throws IOException, FormException {
        OrkaProvisionedAgent provisionedAgent = this.provision(this.getAgentTemplate(javaPath));

        assertEquals(javaPath, provisionedAgent.getJavaPath());
        assertEquals(javaPath, this.getSSHLauncher(provisionedAgent).javaPath);
    }

    @Test
    public void when_template_has_blank_java_path_should_use_null() throws IOException, FormException {
        AgentTemplate agentTemplate = this.getAgentTemplate("  ");
        OrkaProvisionedAgent provisionedAgent = this.provision(agentTemplate);

        assertNull(agentTemplate.getJavaPath());
        assertNull(provisionedAgent.getJavaPath());
        assertNull(this.getSSHLauncher(provisionedAgent).javaPath);
    }

    @Test
    public void when_static_agent_has_java_path_should_pass_it_to_ssh_launcher() throws IOException, FormException {
        OrkaAgent agent = this.getOrkaAgent(javaPath);
        OrkaComputerLauncher launcher = (OrkaComputerLauncher) agent.getLauncher();

        assertEquals(javaPath, agent.getJavaPath());
        assertEquals(javaPath, launcher.getJavaPath());
        assertEquals(javaPath, launcher.getLauncher("vmCredentialsId").javaPath);
    }

    @Test
    public void when_static_agent_has_blank_java_path_should_use_null() throws IOException, FormException {
        OrkaAgent agent = this.getOrkaAgent(" ");
        OrkaComputerLauncher launcher = (OrkaComputerLauncher) agent.getLauncher();

        assertNull(agent.getJavaPath());
        assertNull(launcher.getJavaPath());
        assertNull(launcher.getLauncher("vmCredentialsId").javaPath);
    }

    @Test
    public void when_cloud_config_roundtrip_should_keep_template_java_path() throws Exception {
        OrkaCloud cloud = new OrkaCloud("roundtrip-cloud", "credentialsId", "endpoint", null, 0, false, null,
                Collections.singletonList(this.getAgentTemplate(javaPath)));
        r.jenkins.clouds.add(cloud);
        try {
            r.configRoundtrip();

            OrkaCloud reloaded = (OrkaCloud) r.jenkins.getCloud("roundtrip-cloud");
            assertNotSame(cloud, reloaded);
            assertEquals(javaPath, reloaded.getTemplates().get(0).getJavaPath());
        } finally {
            r.jenkins.clouds.clear();
        }
    }

    @Test
    public void when_static_agent_config_roundtrip_should_keep_java_path() throws Exception {
        OrkaAgent agent = this.getOrkaAgent(javaPath);
        r.jenkins.addNode(agent);
        try {
            // r.configRoundtrip(agent) cannot be used as is: on this page the namespace validator's
            // @RelativePath("..") dependencies make hudson-behavior.js throw before the form behaviours run.
            JenkinsRule.WebClient webClient = r.createWebClient();
            new WebConnectionWrapper(webClient) {
                @Override
                public WebResponse getResponse(WebRequest request) throws IOException {
                    WebResponse response = super.getResponse(request);
                    if (!request.getUrl().getPath().endsWith("/configure")) {
                        return response;
                    }
                    String content = response.getContentAsString().replace(
                            "checkDependsOn=\"../orkaEndpoint ../orkaCredentialsId ../useJenkinsProxySettings "
                                    + "../ignoreSSLErrors\"",
                            "checkDependsOn=\"\"");
                    return new StringWebResponse(content, request.getUrl());
                }
            };
            r.submit(webClient.goTo("computer/" + agent.getNodeName() + "/configure").getFormByName("config"));
            OrkaAgent reloaded = (OrkaAgent) r.jenkins.getNode(agent.getNodeName());

            assertNotSame(agent, reloaded);
            assertEquals(javaPath, reloaded.getJavaPath());
            assertEquals(javaPath, ((OrkaComputerLauncher) reloaded.getLauncher()).getJavaPath());
        } finally {
            r.jenkins.removeNode(r.jenkins.getNode(agent.getNodeName()));
        }
    }

    @Test
    public void when_loading_template_config_without_java_path_should_use_null() {
        String xml = Jenkins.XSTREAM2.toXML(this.getAgentTemplate(javaPath)).replaceAll("\\s*<javaPath>.*</javaPath>",
                "");

        AgentTemplate loaded = (AgentTemplate) Jenkins.XSTREAM2.fromXML(xml);

        assertNull(loaded.getJavaPath());
    }

    @Test
    public void when_loading_static_agent_config_without_java_path_should_use_null()
            throws IOException, FormException {
        String xml = Jenkins.XSTREAM2.toXML(this.getOrkaAgent(javaPath)).replaceAll("\\s*<javaPath>.*</javaPath>",
                "");

        OrkaAgent loaded = (OrkaAgent) Jenkins.XSTREAM2.fromXML(xml);
        OrkaComputerLauncher launcher = (OrkaComputerLauncher) loaded.getLauncher();

        assertNull(loaded.getJavaPath());
        assertNull(launcher.getJavaPath());
        assertNull(launcher.getLauncher("vmCredentialsId").javaPath);
    }

    @Test
    public void when_java_path_is_blank_absolute_or_variable_should_be_ok() {
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckJavaPath(null).kind);
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckJavaPath(" ").kind);
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckJavaPath(javaPath).kind);
        assertEquals(FormValidation.Kind.OK, FormValidator.doCheckJavaPath("${JAVA_HOME}/bin/java").kind);
    }

    @Test
    public void when_java_path_is_relative_or_has_whitespace_should_warn() {
        assertEquals(FormValidation.Kind.WARNING, FormValidator.doCheckJavaPath("bin/java").kind);
        assertEquals(FormValidation.Kind.WARNING,
                FormValidator.doCheckJavaPath("/Library/Java/Zulu 21.jdk/bin/java").kind);
    }

    private OrkaProvisionedAgent provision(AgentTemplate agentTemplate) throws IOException, FormException {
        OrkaCloud cloud = mock(OrkaCloud.class);
        when(cloud.deployVM(any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new DeploymentResponse("127.0.0.1", 2101, "vm-machine", null));
        when(cloud.getRealHost(anyString())).thenReturn("127.0.0.1");
        agentTemplate.setParent(cloud);

        return agentTemplate.provision();
    }

    private SSHLauncher getSSHLauncher(OrkaProvisionedAgent provisionedAgent) {
        return ((WaitSSHLauncher) provisionedAgent.getLauncher()).getLauncher();
    }

    private AgentTemplate getAgentTemplate(String javaPath) {
        return new AgentTemplate("vmCredentialsId", "orka3xOption", null, "baseImage", 12, null,
                Constants.DEFAULT_NAMESPACE, true, false, false, "default", null, null, null, null, null, false,
                1500, 2000, 100, null, null, 1, Mode.NORMAL, "remoteFS", "label",
                new IdleTimeCloudRetentionStrategy(5), null, null, javaPath);
    }

    private OrkaAgent getOrkaAgent(String javaPath) throws IOException, FormException {
        return new OrkaAgent("name", "orkaCredentialsId", "orkaEndpoint", "vmCredentialsId", "node",
                Constants.DEFAULT_NAMESPACE, "namePrefix", null, "image", 12, false, false, false, null, null, null,
                1, "127.0.0.1", 2101, "remoteFS", false, false, null, null, null, null, null, javaPath);
    }
}
