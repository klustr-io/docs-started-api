package io.klustr.example;

import io.klustr.example.config.HealthCheck;
import io.klustr.example.config.StandardSecurityConfiguration;
import io.klustr.example.services.HelloWorldService;
import io.klustr.spring.oauth.OpaqueTokenSecurityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
        OpaqueTokenSecurityConfiguration.class,
        StandardSecurityConfiguration.class,
        Server.class,
        HealthCheck.class,

        // service api
        HelloWorldService.class,
})
@EnableScheduling
@ImportResource("classpath:spring.xml")
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        try {
            log.info("Starting application server...");
            SpringApplication.run(Server.class, args);
        } catch (Exception e) {
            log.error("Exception", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
