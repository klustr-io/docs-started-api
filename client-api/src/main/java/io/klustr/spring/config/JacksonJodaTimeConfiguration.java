package io.klustr.spring.config;

import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Injection of date time formatting for Jackson serialization within sprint.
 */
@Configuration
public class JacksonJodaTimeConfiguration {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new JodaModule());
    }
}