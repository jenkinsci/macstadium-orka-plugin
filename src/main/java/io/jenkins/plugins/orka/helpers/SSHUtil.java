package io.jenkins.plugins.orka.helpers;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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
}
