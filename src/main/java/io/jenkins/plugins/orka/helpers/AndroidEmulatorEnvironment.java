package io.jenkins.plugins.orka.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Builds the environment variables that tell a build where its emulators are. The names follow the job contract
 * shared with the GitHub Actions and Buildkite integrations.
 */
public class AndroidEmulatorEnvironment {
    private static final Logger logger = Logger.getLogger(AndroidEmulatorEnvironment.class.getName());
    private static final String PREFIX = "ORKA_EMULATOR_";

    public static Map<String, String> build(List<Emulator> emulators) {
        Map<String, String> env = new LinkedHashMap<>();
        if (emulators.isEmpty()) {
            return env;
        }

        env.put(PREFIX + "COUNT", String.valueOf(emulators.size()));

        List<String> targets = new ArrayList<>();
        for (int i = 0; i < emulators.size(); i++) {
            Emulator emulator = emulators.get(i);
            putEmulator(env, PREFIX + (i + 1) + "_", emulator);
            targets.add(emulator.getAdbTarget());
        }

        Emulator first = emulators.get(0);
        env.put(PREFIX + "ADB", first.getAdbTarget());
        env.put(PREFIX + "ADB_HOST", first.getHost());
        env.put(PREFIX + "ADB_PORT", String.valueOf(first.getPort()));
        env.put(PREFIX + "ADB_TARGETS", String.join(",", targets));

        // An alias points to the first emulator with its config. Two configs that sanitize to the same alias keep
        // the first one's, and an all-digit alias is skipped because it would shadow an indexed name.
        Map<String, String> aliasOwners = new HashMap<>();
        for (Emulator emulator : emulators) {
            String alias = toAlias(emulator.getConfig());
            if (alias.matches("\\d*")) {
                logger.warning("Not exporting " + PREFIX + alias + "_* for emulator config " + emulator.getConfig()
                        + ": the alias would shadow an indexed name");
                continue;
            }
            String owner = aliasOwners.putIfAbsent(alias, emulator.getConfig());
            if (owner == null) {
                putEmulator(env, PREFIX + alias + "_", emulator);
            } else if (!owner.equals(emulator.getConfig())) {
                logger.warning("Not exporting " + PREFIX + alias + "_* for emulator config " + emulator.getConfig()
                        + ": the alias is already used by emulator config " + owner);
            }
        }

        return env;
    }

    static String toAlias(String config) {
        return config == null ? "" : config.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
    }

    private static void putEmulator(Map<String, String> env, String prefix, Emulator emulator) {
        env.put(prefix + "ADB", emulator.getAdbTarget());
        env.put(prefix + "ADB_HOST", emulator.getHost());
        env.put(prefix + "ADB_PORT", String.valueOf(emulator.getPort()));
        env.put(prefix + "NAME", emulator.getName());
        env.put(prefix + "CONFIG", emulator.getConfig());
    }

    public static List<String> getNames(List<Emulator> emulators) {
        List<String> names = new ArrayList<>();
        emulators.forEach(e -> names.add(e.getName()));
        return Collections.unmodifiableList(names);
    }

    public static class Emulator {
        private final String name;
        private final String config;
        private final String host;
        private final int port;

        public Emulator(String name, String config, String host, int port) {
            this.name = name;
            this.config = config;
            this.host = host;
            this.port = port;
        }

        public String getName() {
            return this.name;
        }

        public String getConfig() {
            return this.config;
        }

        public String getHost() {
            return this.host;
        }

        public int getPort() {
            return this.port;
        }

        public String getAdbTarget() {
            return this.host + ":" + this.port;
        }
    }
}
