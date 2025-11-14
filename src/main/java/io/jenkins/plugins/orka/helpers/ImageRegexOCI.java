package io.jenkins.plugins.orka.helpers;

import java.util.regex.Pattern;

public class ImageRegexOCI {
    final static String ALPHANUMERIC = "[a-z0-9]+";
    final static String SEPARATOR = "(?:[._]|__|[-]+)";
    final static String PATH_COMPONENT = ALPHANUMERIC + "(?:" + SEPARATOR + ALPHANUMERIC + ")*";
    final static String REMOTE_NAME = PATH_COMPONENT + "(?:/" + PATH_COMPONENT + ")*";

    final static String DOMAIN_NAME_COMPONENT = "(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])";
    final static String DOMAIN_NAME = DOMAIN_NAME_COMPONENT + "(?:\\." + DOMAIN_NAME_COMPONENT + ")*";
    final static String IPV6_ADDRESS = "\\[(?:[a-fA-F0-9:]+)\\]";
    final static String HOST = "(?:" + DOMAIN_NAME + "|" + IPV6_ADDRESS + ")";
    final static String OPTIONAL_PORT = "(?::[0-9]+)?";
    final static String DOMAIN_AND_PORT = HOST + OPTIONAL_PORT;

    final static String TAG = "[\\w][\\w.-]{0,127}";
    final static String DIGEST = "[A-Za-z][A-Za-z0-9]*(?:[-_+.][A-Za-z][A-Za-z0-9]*)*:[0-9A-Fa-f]{32,}";

    final static String NAME_PAT = "(?:(?:" + DOMAIN_AND_PORT + ")/)?" + REMOTE_NAME;

    // Full reference pattern:
    // ^(namePat)(?::(tag))?(?:@(digest))?$
    final static String REFERENCE_PAT =
        "^(" + NAME_PAT + ")(?::(" + TAG + "))?(?:@(" + DIGEST + "))?$";

    final static Pattern REFERENCE_REGEXP = Pattern.compile(REFERENCE_PAT);

    public static boolean isValidOCI(String image) {
        return REFERENCE_REGEXP.matcher(image).matches();
    }
}
