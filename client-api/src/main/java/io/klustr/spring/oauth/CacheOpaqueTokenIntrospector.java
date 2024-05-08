package io.klustr.spring.oauth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * When using token introspection it is important under heavy loads
 * not to hammer the token introspection endpoint. We cache the token
 * for a limited amount of time to reduce the load on the introspection
 * endpoint.
 */
public class CacheOpaqueTokenIntrospector implements OpaqueTokenIntrospector {
    private static final Logger log = LoggerFactory.getLogger(CacheOpaqueTokenIntrospector.class);

    private final OpaqueTokenIntrospector introspector;

    // cache access tokens for at least 1 minute to avoid saturating
    // the token validation endpoint, this is a balance between asking
    // for information or otherwise invalidating a 'bad actor' and being
    // exposed for up to 5 minute.
    private final Cache<String, OAuth2AuthenticatedPrincipal> accessTokens;

    /**
     * Creates a new token introspector with a default of 5 minutes (300 seconds)
     *
     * @param uri          The URI of the token introspection endpoint
     * @param clientId     The client ID to use when asking for introspection
     * @param clientSecret THe client secret to use when asking for introspection.
     */
    public CacheOpaqueTokenIntrospector(String uri, String clientId, String clientSecret, String apiKey) {
        this(uri, clientId, clientSecret, apiKey,10);
    }

    /**
     * Creates a new token introspector with a default of 5 minutes (300 seconds)
     *
     * @param uri             The URI of the token introspection endpoint
     * @param clientId        The client ID to use when asking for introspection
     * @param clientSecret    THe client secret to use when asking for introspection.
     * @param expiryInSeconds The number of seconds we should cache the token introspection result.
     */
    public CacheOpaqueTokenIntrospector(String uri, String clientId, String clientSecret, String apiKey, int expiryInSeconds) {
        RestTemplate restTemplate = new RestTemplate();
        if (StringUtils.isNotBlank(clientId)) {
            restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        }
        if (StringUtils.isNotBlank(apiKey)) {
            restTemplate.getInterceptors().add(new ApiKeyInterceptor(apiKey));
        }

        this.introspector = new NimbusOpaqueTokenIntrospector(uri, restTemplate);
        this.accessTokens = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(expiryInSeconds))
                .recordStats()
                .build();
    }

    private static class ApiKeyInterceptor implements ClientHttpRequestInterceptor {

        private final String apiKey;

        public ApiKeyInterceptor(String apiKey) {
            this.apiKey = apiKey;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            if (!request.getHeaders().containsKey("X-API-Key")) {
                request.getHeaders().set("X-API-Key", apiKey);
            }
            return execution.execute(request, body);
        }
    }

    public @Override OAuth2AuthenticatedPrincipal introspect(String token) {
        try {
            OAuth2AuthenticatedPrincipal principal = accessTokens.getIfPresent(token);
            if (principal == null) {
                principal = introspector.introspect(token);
                if (principal != null) {
                    accessTokens.put(token, principal);
                } else {
                    return null;
                }
            }

            Object expiry_obj = principal.getAttribute("exp");
            if (expiry_obj != null) {
                Instant expiry = (Instant) expiry_obj;
                if (expiry.isBefore(Instant.now())) {
                    accessTokens.invalidate(token);
                    principal = introspector.introspect(token);
                }
            }

            return new OAuth2IntrospectionAuthenticatedPrincipal(principal.getAttributes(), new PermissionExtractor(principal).getAuthorities());
        } catch (BadOpaqueTokenException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static class PermissionExtractor {

        private final ImmutableCollection<GrantedAuthority> authorities;

        public PermissionExtractor(OAuth2AuthenticatedPrincipal wrap) {
            // extensions has permissions
            List<GrantedAuthority> auth = Lists.newArrayList();
            Object ext = wrap.getAttribute("ext");
            if (ext != null) {
                ;
                net.minidev.json.JSONObject map = (net.minidev.json.JSONObject) ext;
                if (map.get("permissions") != null) {
                    JSONArray permissions = (JSONArray) map.get("permissions");
                    List<SimpleGrantedAuthority> list = permissions.stream().map(x -> {
                        return new SimpleGrantedAuthority(x.toString());
                    }).toList();
                    auth.addAll(list);
                }
            }
            auth.addAll(wrap.getAuthorities());
            this.authorities = ImmutableList.copyOf(auth);
        }

        public Collection<GrantedAuthority> getAuthorities() {
            return this.authorities;
        }
    }
}