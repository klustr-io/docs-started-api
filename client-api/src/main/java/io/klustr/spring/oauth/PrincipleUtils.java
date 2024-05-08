package io.klustr.spring.oauth;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Map;

public class PrincipleUtils {
    public static String tryGetField(String field, OAuth2AuthenticatedPrincipal user) {
        Object ext = user.getAttribute("ext");
        if (ext != null) {
            Map<?, ?> ext_map = (Map<?, ?>) ext;
            if (ext_map.containsKey(field)) {
                return ext_map.get(field).toString();
            }
        }
        return null;
    }
}
