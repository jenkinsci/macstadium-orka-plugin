package io.jenkins.plugins.orka;

import io.jenkins.plugins.orka.helpers.Utils;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UtilsTest {
    @Test
    public void whrn_comparing_versions_should_return_correct_result() {
        assertTrue(Utils.compareVersions("1.0.0", "1.1.2") < 0);
        assertTrue(Utils.compareVersions("1.0.1", "1.10") < 0);
        assertTrue(Utils.compareVersions("1.1.2", "1.0.1") > 0);
        assertTrue(Utils.compareVersions("1.1.2", "1.2.0") < 0);
        assertTrue(Utils.compareVersions("1.3.0", "1.3") == 0);
        assertTrue(Utils.compareVersions("2.1.0", "2.1.1") < 0);
        assertTrue(Utils.compareVersions("2.1.1-preview-1", "2.1.1") < 0);
        assertTrue(Utils.compareVersions("2.1.1", "2.0") > 0);
    }
}