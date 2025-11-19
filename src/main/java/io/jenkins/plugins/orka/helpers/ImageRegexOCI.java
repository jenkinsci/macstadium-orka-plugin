/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jenkins.plugins.orka.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class ImageRegexOCI {
    // The below regex and function were adapted from Jib:
    // https://github.com/GoogleContainerTools/jib/blob/master/jib-core/src/main/java/com/google/cloud/tools/jib/api/ImageReference.java
    private static final String REGISTRY_COMPONENT_REGEX = "(?:[a-zA-Z\\d]|(?:[a-zA-Z\\d][a-zA-Z\\d-]*[a-zA-Z\\d]))";
    private static final String REGISTRY_REGEX = String.format("%s(?:\\.%s)*(?::\\d+)?", REGISTRY_COMPONENT_REGEX,
            REGISTRY_COMPONENT_REGEX);

    private static final String REPOSITORY_COMPONENT_REGEX = "[a-z\\d]+(?:(?:[_.]|__|-+)[a-z\\d]+)*";
    private static final String REPOSITORY_REGEX = String.format("(?:%s/)*%s", REPOSITORY_COMPONENT_REGEX,
            REPOSITORY_COMPONENT_REGEX);

    private static final String TAG_REGEX = "[\\w][\\w.-]{0,127}";

    private static final int HASH_LENGTH = 64;
    private static final String HASH_REGEX = String.format("[a-f0-9]{%d}", HASH_LENGTH);
    private static final String DIGEST_PREFIX = "sha256:";
    static final String DIGEST_REGEX = DIGEST_PREFIX + HASH_REGEX;

    private static final String REFERENCE_REGEX = String.format(
            "^(?:(%s)/)?(%s)(?::(%s))?(?:@(%s))?$",
            REGISTRY_REGEX, REPOSITORY_REGEX, TAG_REGEX, DIGEST_REGEX);

    private static final Pattern REFERENCE_PATTERN = Pattern.compile(REFERENCE_REGEX);

    public static boolean isValidOCI(String reference) {
        Matcher matcher = REFERENCE_PATTERN.matcher(reference);

        if (!matcher.find() || matcher.groupCount() < 4) {
            return false;
        }

        String registry = matcher.group(1);
        String repository = matcher.group(2);

        if (StringUtils.isEmpty(registry)) {
            return false;
        }

        if (StringUtils.isEmpty(repository)) {
            return false;
        }

        if (!registry.contains(".") && !registry.contains(":") && !"localhost".equals(registry)) {
            return false;
        }

        return true;
    }
}
