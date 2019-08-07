
package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.util.FormValidation;

@RunWith(Parameterized.class)
public class IdleTimeTerminationMinutesTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with value={0}, validation={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { "12", FormValidation.Kind.OK }, { null, FormValidation.Kind.OK },
                { "", FormValidation.Kind.OK }, { "-27", FormValidation.Kind.ERROR },
                { "notANumber", FormValidation.Kind.ERROR } });
    }

    private final String idleValue;
    private final FormValidation.Kind validationKind;

    public IdleTimeTerminationMinutesTest(String idleValue, FormValidation.Kind validationKind) {
        this.idleValue = idleValue;
        this.validationKind = validationKind;
    }

    @Test
    public void when_check_idle_value_termination_minutes_should_return_correct_validation_kind() throws IOException {
        AgentTemplate.DescriptorImpl descriptor = new AgentTemplate.DescriptorImpl();

        FormValidation validation = descriptor.doCheckIdleTerminationMinutes(this.idleValue);

        assertEquals(this.validationKind, validation.kind);
    }
}