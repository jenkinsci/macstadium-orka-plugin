package io.jenkins.plugins.orka;

import com.cloudbees.plugins.credentials.common.PasswordCredentials;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.jenkins.plugins.orka.helpers.AESDecryptor;
import io.jenkins.plugins.orka.helpers.CredentialsHelper;
import io.jenkins.plugins.orka.helpers.SSHUtil;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class AESVerificationStrategy extends OrkaVerificationStrategy {
    private static final long serialVersionUID = -5429790217208997430L;

    private static final Logger logger = Logger.getLogger(AESVerificationStrategy.class.getName());

    private static String defaultRemotePath = "/tmp";
    private String aesKeyId;
    private String encryptionScript;
    private String remotePath;

    @DataBoundConstructor
    public AESVerificationStrategy(String aesKeyId, String encryptionScript, String remotePath) {
        this.aesKeyId = aesKeyId;
        this.encryptionScript = encryptionScript;
        this.remotePath = StringUtils.isNotBlank(remotePath) ? remotePath : defaultRemotePath;
    }

    public String getAesKeyId() {
        return this.aesKeyId;
    }

    public String getEncryptionScript() {
        return this.encryptionScript;
    }

    public String getRemotePath() {
        return this.remotePath;
    }

    public boolean verify(String host, int sshPort, StandardUsernameCredentials credentials, TaskListener listener) {
        String hostIdentity = "Host: " + host + ", port: " + sshPort;

        listener.getLogger().println("AES verification for host " + host + " on port " + sshPort);
        String token = this.generateSafeToken();
        this.logMessage("Random token: " + token, hostIdentity, listener);
        String scriptOutput = null;
        try {
            scriptOutput = SSHUtil.execute(host, sshPort, credentials, 300, this.encryptionScript, this.remotePath,
                    token);

            PasswordCredentials aesKey = CredentialsHelper.lookupSystemCredentials(this.aesKeyId,
                    PasswordCredentials.class);
            String decryptedToken = AESDecryptor.decrypt(scriptOutput, Secret.toString(aesKey.getPassword())).trim();
            this.logMessage("Decrypted token: " + decryptedToken, hostIdentity, listener);

            boolean verificationSuccessful = decryptedToken.equals(token);
            if (!verificationSuccessful) {
                this.logMessage("AES Verification failed. Script output: " + scriptOutput, hostIdentity, listener);
            }
            return verificationSuccessful;
        } catch (Exception e) {
            listener.getLogger().println("Exception during AES verification: " + e.toString());
            logger.log(Level.WARNING, "Exception during AES verification for " + hostIdentity, e);
            this.logMessage("Script output: " + scriptOutput, hostIdentity, listener);
        }
        return false;
    }

    private String generateSafeToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[128];
        random.nextBytes(bytes);
        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString(bytes);
    }

    private void logMessage(String message, String hostIdentity, TaskListener listener) {
        listener.getLogger().println(message);
        logger.fine(hostIdentity + " " + message);
    }

    @Extension
    public static final class DescriptorImpl extends OrkaVerificationStrategyDescriptor {
        @Override
        public String getDisplayName() {
            return "AES Verification Strategy";
        }

        public ListBoxModel doFillAesKeyIdItems() {
            return CredentialsHelper.getCredentials(PasswordCredentials.class);
        }

        public String getDefaultRemotePath() {
            return defaultRemotePath;
        }
    }
}
