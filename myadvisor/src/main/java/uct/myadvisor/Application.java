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
    // Setup SSH tunnel
    try {
        SshTunnel.setupSshTunnel(
            "thmcon004",          // SSH user
            "404Go4mhY",          // SSH password
            "nightmare.cs.uct.ac.za", // SSH host
            22,                   // SSH port
            "localhost",          // Remote host (DB server host)
            3306,                 // Local port (local port for SSH tunnel)
            3306                  // Remote port (database port on the remote server)
        );
    } catch (Exception e) {
        e.printStackTrace();
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
