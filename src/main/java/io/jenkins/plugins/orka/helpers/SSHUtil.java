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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SSHUtil {
    private static final Logger logger = Logger.getLogger(SSHUtil.class.getName());
    private static final long timeout = TimeUnit.SECONDS.toMillis(10);
    private static final int connectionRetries = 12;

    public static boolean waitForSSH(String host, int sshPort)
            throws IOException, InterruptedException {
        int attempts = 0;
        while (attempts < connectionRetries) {
            attempts++;
            try (Socket s = new Socket()) {
                s.setSoTimeout((int) timeout);
                s.connect(new InetSocketAddress(host, sshPort), (int) timeout);
                return true;
            } catch (IOException ex) {
                if (attempts == connectionRetries) {
                    throw ex;
                }
            }

            Thread.sleep(timeout);
        }

        return false;
    }

    public static String execute(String host, int sshPort, StandardUsernameCredentials credentials,
            int launchTimeoutSeconds, String script, String remoteLocation, String args)
            throws IOException, InterruptedException {

        Connection connection = new Connection(host, sshPort);
        try {
            long launchTimeoutMilliseconds = TimeUnit.SECONDS.toMillis(launchTimeoutSeconds);
            int attempts = 0;
            while (attempts < connectionRetries) {
                attempts++;
                try {
                    connection.connect(new ServerHostKeyVerifier() {
                        public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm,
                                byte[] serverHostKey) throws Exception {
                            return true;
                        }
                    }, (int) launchTimeoutMilliseconds, 0,
                            (int) (launchTimeoutMilliseconds + timeout));
                    break;
                } catch (Exception e) {
                    logger.log(Level.WARNING,
                            "Exception when connecting via SSH for host " + host + " on port " + sshPort, e);
                    if (attempts == connectionRetries) {
                        throw e;
                    }
                }
                Thread.sleep(timeout);
            }
            logger.log(Level.FINE, "Connected via SSH for host " + host + " on port " + sshPort);
            if (SSHAuthenticator.newInstance(connection, credentials).authenticate(TaskListener.NULL)) {
                SCPClient scp = connection.createSCPClient();
                String scriptName = "orka_handshake.sh";
                scp.put(script.getBytes("UTF-8"), scriptName, remoteLocation, "0700");
                logger.log(Level.FINE, "File copied for host " + host + " on port " + sshPort);

                Session session = connection.openSession();
                session.requestDumbPTY();
                session.execCommand(String.format("%s/%s %s", remoteLocation, scriptName, args));
                session.getStdin().close();
                session.getStderr().close();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(session.getStdout(), StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                logger.log(Level.SEVERE, "Could not authenticate to SSH for host " + host + " on port " + sshPort);
            }
        } finally {
            connection.close();
        }

        return null;
    }
}
