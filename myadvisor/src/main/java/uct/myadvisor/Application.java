package uct.myadvisor;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import uct.myadvisor.security.SshTunnel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.sql.DataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;

import uct.myadvisor.data.Message;
import uct.myadvisor.data.UserRepository;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@Push
@Theme(value = "my-advisor")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        // Setup SSH tunnel if enabled
        String sshEnabled = System.getenv("SSH_TUNNEL_ENABLED");
        if ("true".equalsIgnoreCase(sshEnabled)) {
            try {
                String sshUser = System.getenv("SSH_USER");
                String sshPassword = System.getenv("SSH_PASSWORD");
                String sshHost = System.getenv("SSH_HOST");
                String sshPortStr = System.getenv("SSH_PORT");
                int sshPort = (sshPortStr != null && !sshPortStr.isEmpty()) ? Integer.parseInt(sshPortStr) : 22;
                String remoteHost = System.getenv("SSH_REMOTE_HOST");
                if (remoteHost == null || remoteHost.isEmpty()) {
                    remoteHost = "localhost";
                }
                String localPortStr = System.getenv("SSH_LOCAL_PORT");
                int localPort = (localPortStr != null && !localPortStr.isEmpty()) ? Integer.parseInt(localPortStr) : 3306;
                String remotePortStr = System.getenv("SSH_REMOTE_PORT");
                int remotePort = (remotePortStr != null && !remotePortStr.isEmpty()) ? Integer.parseInt(remotePortStr) : 3306;

                System.out.println("Initializing SSH Tunnel to " + sshHost + "...");
                SshTunnel.setupSshTunnel(
                    sshUser,
                    sshPassword,
                    sshHost,
                    sshPort,
                    remoteHost,
                    localPort,
                    remotePort
                );
                System.out.println("SSH Tunnel established successfully.");
            } catch (Exception e) {
                System.err.println("Failed to establish SSH Tunnel: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("SSH Tunnel disabled (enable by setting SSH_TUNNEL_ENABLED=true).");
        }

        SpringApplication.run(Application.class, args);
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
            SqlInitializationProperties properties, UserRepository repository) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }

    // Method to add Favicon
    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "/icons/favicon.png", "192x192");
    }
}
