package io.jenkins.plugins.orka;

import io.jenkins.plugins.orka.helpers.Utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UtilsTest {
    @Test
    public void when_comparing_versions_should_return_correct_result() {
        assertTrue(Utils.compareVersions("1.0.0", "1.1.2") < 0);
        assertTrue(Utils.compareVersions("1.0.1", "1.10") < 0);
        assertTrue(Utils.compareVersions("1.1.2", "1.0.1") > 0);
        assertTrue(Utils.compareVersions("1.1.2", "1.2.0") < 0);
        assertTrue(Utils.compareVersions("1.3.0", "1.3") == 0);
        assertTrue(Utils.compareVersions("2.1.0", "2.1.1") < 0);
        assertTrue(Utils.compareVersions("2.1.1-preview-1", "2.1.1") < 0);
        assertTrue(Utils.compareVersions("2.1.1-preview-1", "2.1.1-preview-2") == 0);
        assertTrue(Utils.compareVersions("2.1.1", "2.0") > 0);
        assertTrue(Utils.compareVersions("2.1.1", "2.1.1") == 0);
    }

    @Test
    public void when_sanitizing_name_should_return_correct_name() {
        assertEquals(Utils.sanitizeK8sName("Mojave"), "mojave");
        assertEquals(Utils.sanitizeK8sName("90GBCatalina"), "90gbcatalina");
        assertEquals(Utils.sanitizeK8sName("ventura"), "ventura");
        assertEquals(Utils.sanitizeK8sName("ventura-new"), "ventura-new");
        assertEquals(Utils.sanitizeK8sName("ventura_invalid"), "ventura-invalid");
        assertEquals(Utils.sanitizeK8sName("Mojave_Invalid"), "mojave-invalid");
    }
}
