package io.jenkins.plugins.orka.helpers;

import java.util.regex.Pattern;

public class ImageRegexOCI {
    static final String ALPHANUMERIC = "[a-z0-9]+";
    static final String SEPARATOR = "(?:[._]|__|[-]+)";
    static final String PATH_COMPONENT = ALPHANUMERIC + "(?:" + SEPARATOR + ALPHANUMERIC + ")*";
    static final String REMOTE_NAME = PATH_COMPONENT + "(?:/" + PATH_COMPONENT + ")*";

    static final String DOMAIN_NAME_COMPONENT = "(?:[a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])";
    static final String DOMAIN_NAME = DOMAIN_NAME_COMPONENT + "(?:\\." + DOMAIN_NAME_COMPONENT + ")*";
    static final String IPV6_ADDRESS = "\\[(?:[a-fA-F0-9:]+)\\]";
    static final String HOST = "(?:" + DOMAIN_NAME + "|" + IPV6_ADDRESS + ")";
    static final String OPTIONAL_PORT = "(?::[0-9]+)?";
    static final String DOMAIN_AND_PORT = HOST + OPTIONAL_PORT;

    static final String TAG = "[\\w][\\w.-]{0,127}";
    static final String DIGEST = "[A-Za-z][A-Za-z0-9]*(?:[-_+.][A-Za-z][A-Za-z0-9]*)*:[0-9A-Fa-f]{32,}";

    static final String NAME_PAT = "(?:(?:" + DOMAIN_AND_PORT + ")/)?" + REMOTE_NAME;

    // Full reference pattern:
    // ^(namePat)(?::(tag))?(?:@(digest))?$
    static final String REFERENCE_PAT =
        "^(" + NAME_PAT + ")(?::(" + TAG + "))?(?:@(" + DIGEST + "))?$";

    static final Pattern REFERENCE_REGEXP = Pattern.compile(REFERENCE_PAT);

    public static boolean isValidOCI(String image) {
        return REFERENCE_REGEXP.matcher(image).matches();
    }
}
