package io.jenkins.plugins.orka.helpers;

import com.cloudbees.jenkins.plugins.sshcredentials.SSHAuthenticator;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.trilead.ssh2.Session;

import hudson.model.TaskListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SSHUtil {
    public static boolean waitForSSH(String host, int sshPort, int retries, int secondsBetweenRetries)
            throws IOException, InterruptedException {
        int attempts = 0;
        while (attempts < retries) {
            attempts++;
            try (Socket s = new Socket(host, sshPort)) {
                return true;
            } catch (IOException ex) {
                if (attempts == retries) {
                    throw ex;
                }
            }

            Thread.sleep(TimeUnit.SECONDS.toMillis(secondsBetweenRetries));
        }

        return false;
    }

    public static String execute(String host, int sshPort, StandardUsernameCredentials credentials,
            int launchTimeoutSeconds, String script, String remoteLocation, String args)
            throws IOException, InterruptedException {

        Connection connection = new Connection(host, sshPort);
        try {
            long launchTimeoutMilliseconds = TimeUnit.SECONDS.toMillis(launchTimeoutSeconds);
            connection.connect(new ServerHostKeyVerifier() {
                public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm,
                        byte[] serverHostKey) throws Exception {
                    return true;
                }
            }, (int) launchTimeoutMilliseconds, 0, (int) (launchTimeoutMilliseconds + TimeUnit.SECONDS.toMillis(5)));

            if (SSHAuthenticator.newInstance(connection, credentials).authenticate(TaskListener.NULL)) {

                SCPClient scp = connection.createSCPClient();
                String scriptName = "orka_handshake.sh";
                scp.put(script.getBytes("UTF-8"), scriptName, remoteLocation, "0700");

                Session session = connection.openSession();
                session.requestDumbPTY();
                session.execCommand(String.format("%s/%s %s", remoteLocation, scriptName, args));
                session.getStdin().close();
                session.getStderr().close();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(session.getStdout(), StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            }
        } finally {
            connection.close();
        }

        return null;
    }
}
