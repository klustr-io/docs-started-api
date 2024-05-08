package io.klustr.spring.oauth;

import io.klustr.spring.oauth.CacheOpaqueTokenIntrospector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * Enables the configuration of opaque tokens
 */
@Configuration
public class OpaqueTokenSecurityConfiguration {

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}")
    String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id:}")
    String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret:}")
    String clientSecret;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.api-key:}")
    String apiKey;

    @Bean
    public OpaqueTokenIntrospector introspector() {
        return new CacheOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret, apiKey);
    }

}