package io.jenkins.plugins.orka;

import com.google.common.annotations.VisibleForTesting;

import hudson.Extension;
import hudson.RelativePath;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.orka.helpers.OrkaClientFactory;
import io.jenkins.plugins.orka.helpers.OrkaInfoHelper;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

public class AndroidEmulatorTemplate extends AbstractDescribableImpl<AndroidEmulatorTemplate> {
    private final String config;

    @DataBoundConstructor
    public AndroidEmulatorTemplate(String config) {
        this.config = Util.fixEmptyAndTrim(config);
    }

    public String getConfig() {
        return this.config;
    }

    @Override
    public String toString() {
        return "AndroidEmulatorTemplate [config=" + config + "]";
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<AndroidEmulatorTemplate> {
        private OrkaInfoHelper infoHelper = new OrkaInfoHelper(new OrkaClientFactory());

        @VisibleForTesting
        void setClientFactory(OrkaClientFactory clientFactory) {
            this.infoHelper = new OrkaInfoHelper(clientFactory);
        }

        @Override
        public String getDisplayName() {
            return "Android Emulator";
        }

        @POST
        public ListBoxModel doFillConfigItems(@QueryParameter @RelativePath("../..") String endpoint,
                @QueryParameter @RelativePath("../..") String credentialsId,
                @QueryParameter @RelativePath("../..") Boolean useJenkinsProxySettings,
                @QueryParameter @RelativePath("../..") Boolean ignoreSSLErrors) {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return this.infoHelper.doFillEmulatorConfigItems(endpoint, credentialsId,
                    Boolean.TRUE.equals(useJenkinsProxySettings), Boolean.TRUE.equals(ignoreSSLErrors));
        }
    }
}
