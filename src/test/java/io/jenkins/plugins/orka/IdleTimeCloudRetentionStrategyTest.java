package io.jenkins.plugins.orka;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import antlr.ANTLRException;

@RunWith(Parameterized.class)
public class IdleTimeCloudRetentionStrategyTest {
    @ClassRule
    public static JenkinsRule r = new JenkinsRule();

    @Parameterized.Parameters(name = "{index}: Test with inputValue={0}, expected={1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] { { 5, 5 },
                { -5, IdleTimeCloudRetentionStrategy.RECOMMENDED_MIN_IDLE },
                { 0, IdleTimeCloudRetentionStrategy.RECOMMENDED_MIN_IDLE }
        });
    }

    private final int inputValue;
    private final int expected;

    public IdleTimeCloudRetentionStrategyTest(int passed, int expected) {
        this.inputValue = passed;
        this.expected = expected;
    }

    @Test
    public void when_passing_input_value_should_get_positive_integer() throws IOException, ANTLRException {
        IdleTimeCloudRetentionStrategy strat = new IdleTimeCloudRetentionStrategy(this.inputValue);

        assertEquals(this.expected, strat.getIdleMinutes());
    }

}
