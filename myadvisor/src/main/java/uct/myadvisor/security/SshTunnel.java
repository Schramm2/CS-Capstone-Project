package uct.myadvisor.security;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Properties;

public class SshTunnel {

    public static void setupSshTunnel(String sshUser, String sshPassword, String sshHost, int sshPort,
                                      String remoteHost, int localPort, int remotePort) throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(sshUser, sshHost, sshPort);

        // SSH password authentication
        session.setPassword(sshPassword);

        // Avoid asking for key confirmation
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        // Connect to the SSH server
        session.connect();

        // Set up port forwarding (local port to remote server)
        session.setPortForwardingL(localPort, remoteHost, remotePort);
    }
}
