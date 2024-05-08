package io.klustr.example.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck implements HealthIndicator {

    public HealthCheck() {

    }

    @Override
    public Health health() {
        return Health.up().build();
    }
}
