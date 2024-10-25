package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import antlr.ANTLRException;
import hudson.model.Label;
import hudson.model.Node.Mode;

@RunWith(Parameterized.class)
public class GetTemplateTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with mode={0}, label={1}, labelToLookFor={2}, expectedResult={3}, expectedCanProvision={4}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { Mode.NORMAL, "myLabel", null, "myLabel", true },
                { Mode.NORMAL, "myLabel", "myLabel", "myLabel", true },
                { Mode.NORMAL, "myLabel", "anotherLabel", null, false },
                { Mode.EXCLUSIVE, "myLabel", null, null, false },
                { Mode.EXCLUSIVE, "myLabel", "myLabel", "myLabel", true },
                { Mode.EXCLUSIVE, "myLabel", "anotherLabel", null, false } });
    }

    private final Mode mode;
    private final String label;
    private final String labelToLookFor;
    private final String expectedResult;
    private final boolean expectedCanProvision;

    public GetTemplateTest(Mode mode, String label, String labelToLookFor, String expectedResult,
            boolean expectedCanProvision) {
        this.mode = mode;
        this.label = label;
        this.labelToLookFor = labelToLookFor;
        this.expectedResult = expectedResult;
        this.expectedCanProvision = expectedCanProvision;
    }

    @Test
    public void when_get_template_should_find_template_with_label() throws IOException, ANTLRException {
        AgentTemplate AgentTemplate = this.getAgentTemplate(this.mode, this.label);
        OrkaCloud cloud = new OrkaCloud("cloud", "credentialsId", "endpoint", null, 0, false, null,
                Arrays.asList(AgentTemplate));

        Label label = this.labelToLookFor != null ? Label.parseExpression(this.labelToLookFor) : null;
        AgentTemplate resultTemplate = cloud.getTemplate(label);

        String result = resultTemplate == null ? null : resultTemplate.getLabelString();
        boolean canProvisionResult = cloud.canProvision(label);

        assertEquals(this.expectedResult, result);
        assertEquals(this.expectedCanProvision, canProvisionResult);
    }

    private AgentTemplate getAgentTemplate(Mode mode, String label) {
        return new AgentTemplate("vmCredentialsId", "name", false, "configName", "baseImage", 12, true,
                false, false, 1,
                "remoteFS",
                this.mode, this.label, "prefix", new IdleTimeCloudRetentionStrategy(5),
                null,
                Collections.emptyList(), null, "default", "", false, null, false);
    }
}
