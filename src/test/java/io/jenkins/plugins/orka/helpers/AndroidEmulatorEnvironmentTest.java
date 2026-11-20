package io.jenkins.plugins.orka.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import io.jenkins.plugins.orka.helpers.AndroidEmulatorEnvironment.Emulator;

public class AndroidEmulatorEnvironmentTest {
    @Test
    public void when_there_are_no_emulators_should_build_no_variables() {
        assertTrue(AndroidEmulatorEnvironment.build(Collections.emptyList()).isEmpty());
    }

    @Test
    public void when_there_are_emulators_should_build_indexed_and_default_variables() {
        Map<String, String> env = AndroidEmulatorEnvironment.build(Arrays.asList(
                new Emulator("vm-abcde-emu-1", "pixel9-api36", "192.168.64.1", 15556),
                new Emulator("vm-abcde-emu-2", "tablet-api35", "192.168.64.1", 15558)));

        assertEquals("2", env.get("ORKA_EMULATOR_COUNT"));
        assertEquals("192.168.64.1:15556", env.get("ORKA_EMULATOR_ADB"));
        assertEquals("192.168.64.1", env.get("ORKA_EMULATOR_ADB_HOST"));
        assertEquals("15556", env.get("ORKA_EMULATOR_ADB_PORT"));
        assertEquals("192.168.64.1:15556,192.168.64.1:15558", env.get("ORKA_EMULATOR_ADB_TARGETS"));

        assertEquals("192.168.64.1:15558", env.get("ORKA_EMULATOR_2_ADB"));
        assertEquals("192.168.64.1", env.get("ORKA_EMULATOR_2_ADB_HOST"));
        assertEquals("15558", env.get("ORKA_EMULATOR_2_ADB_PORT"));
        assertEquals("vm-abcde-emu-2", env.get("ORKA_EMULATOR_2_NAME"));
        assertEquals("tablet-api35", env.get("ORKA_EMULATOR_2_CONFIG"));

        assertEquals("192.168.64.1:15556", env.get("ORKA_EMULATOR_PIXEL9_API36_ADB"));
        assertEquals("vm-abcde-emu-2", env.get("ORKA_EMULATOR_TABLET_API35_NAME"));
    }

    @Test
    public void when_config_repeats_should_point_alias_to_first_emulator() {
        Map<String, String> env = AndroidEmulatorEnvironment.build(Arrays.asList(
                new Emulator("vm-abcde-emu-1", "pixel9-api36", "192.168.64.1", 15556),
                new Emulator("vm-abcde-emu-2", "pixel9-api36", "192.168.64.1", 15558)));

        assertEquals("vm-abcde-emu-1", env.get("ORKA_EMULATOR_PIXEL9_API36_NAME"));
        assertEquals("vm-abcde-emu-2", env.get("ORKA_EMULATOR_2_NAME"));
    }

    @Test
    public void when_configs_share_an_alias_should_keep_first_config() {
        Map<String, String> env = AndroidEmulatorEnvironment.build(Arrays.asList(
                new Emulator("vm-abcde-emu-1", "android-37.0", "192.168.64.1", 15556),
                new Emulator("vm-abcde-emu-2", "android-37-0", "192.168.64.1", 15558)));

        assertEquals("android-37.0", env.get("ORKA_EMULATOR_ANDROID_37_0_CONFIG"));
        assertEquals("vm-abcde-emu-1", env.get("ORKA_EMULATOR_ANDROID_37_0_NAME"));
    }

    @Test
    public void when_alias_is_numeric_should_not_shadow_indexed_variables() {
        Map<String, String> env = AndroidEmulatorEnvironment.build(Arrays.asList(
                new Emulator("vm-abcde-emu-1", "pixel9-api36", "192.168.64.1", 15556),
                new Emulator("vm-abcde-emu-2", "1", "192.168.64.1", 15558)));

        assertEquals("192.168.64.1:15556", env.get("ORKA_EMULATOR_1_ADB"));
        assertEquals("pixel9-api36", env.get("ORKA_EMULATOR_1_CONFIG"));
        assertEquals("vm-abcde-emu-1", env.get("ORKA_EMULATOR_1_NAME"));
    }
}
