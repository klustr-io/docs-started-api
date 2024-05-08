package io.klustr.example.openapi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.klustr.spring.oauth.OAuthCredentialType;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class ApiDocumentation {

    public static OperationCustomizer serviceAccountExpected() {
        return (operation, handlerMethod) -> {
            Optional<Operation> op = Optional.ofNullable(handlerMethod.getMethodAnnotation(Operation.class));
            StringBuilder sb = new StringBuilder();
            if (operation.getDescription() != null) {
                sb.append(operation.getDescription());
            }

            if (op.isPresent()) {
                SecurityRequirement[] security = op.get().security();
                if (security != null && security.length > 0) {
                    Optional<SecurityRequirement> accessToken = Arrays.stream(security).filter(x -> x.name().equalsIgnoreCase(OAuthCredentialType.SERVICE_TO_SERVICE)).findFirst();
                    if (accessToken.isPresent()) {
                        sb.append("\r\n");
                        sb.append("""

                                <div class="callout callout-info" style="margin: 1em 0em 1em 0em">
                                <p>
                                💻 <strong>Service Credentials</strong> is required to access this service.
                                </p>
                                </div>

                                """);
                    }
                }
            }

            operation.setDescription(sb.toString());
            return operation;
        };
    }

    public static OperationCustomizer oauthExpected() {
        return (operation, handlerMethod) -> {
            Optional<Operation> op = Optional.ofNullable(handlerMethod.getMethodAnnotation(Operation.class));
            StringBuilder sb = new StringBuilder();
            if (operation.getDescription() != null) {
                sb.append(operation.getDescription());
            }

            if (op.isPresent()) {
                SecurityRequirement[] security = op.get().security();
                if (security != null && security.length > 0) {
                    Optional<SecurityRequirement> accessToken = Arrays.stream(security).filter(x -> x.name().equalsIgnoreCase(OAuthCredentialType.USER_TO_SERVICE)).findFirst();
                    if (accessToken.isPresent()) {
                        sb.append("\r\n");
                        sb.append("""

                                <div class="callout callout-info" style="margin: 1em 0em 1em 0em">
                                <p>
                                👨🏻‍🦱 <strong>User Credential</strong> is needed to access this service. You must
                                login and authenticate a user with OIDC and use an access token retrieved
                                to invoke this service.
                                </p>
                                </div>

                                """);
                    }
                }
            }

            if (operation.getSecurity() == null) {
                operation.setSecurity(Lists.newArrayList());
            }

            operation.getSecurity().add(new io.swagger.v3.oas.models.security.SecurityRequirement().addList(SecuritySchemeType.APIKEY.name()));

            operation.setDescription(sb.toString());
            return operation;
        };
    }

    public static OperationCustomizer apikey() {
        return (operation, handlerMethod) -> operation.addParametersItem(
                new Parameter()
                        .in("header")
                        .required(false)
                        .description("API key for use when using api gateways.")
                        .name("x-api-key"));
    }

    public static OperationCustomizer permissionCheck() {
        return (operation, handlerMethod) -> {
            Optional<PreAuthorize> preAuthorizeAnnotation = Optional.ofNullable(handlerMethod.getMethodAnnotation(PreAuthorize.class));
            StringBuilder sb = new StringBuilder();
            if (operation.getDescription() != null) {
                sb.append(operation.getDescription());
            }

            if (preAuthorizeAnnotation.isPresent()) {
                String perms = (preAuthorizeAnnotation.get()).value().replaceAll("hasAuthority|\\(|\\)|\\'", "");

                boolean isScope = false;
                if (perms.startsWith("SCOPE_")) {
                    perms = perms.replace("SCOPE_","");
                    isScope = true;
                }

                if (isScope) {
                    sb.append("""
                                
                            <div class="callout callout-warning" style="margin: 1em 0em 1em 0em">
                            <p>
                            🔒 <strong>Consent: <span style="font-family: monospace">%s</span></strong> is required in order for the call to succeed. Ensure that you have the consent
                            approval for the user or this call will fail.
                            </p>
                            </div>
                                
                            """.formatted(perms));
                } else {
                    sb.append("""
                                
                            <div class="callout callout-warning" style="margin: 1em 0em 1em 0em">
                            <p>
                            🔒 <strong>Permission: <span style="font-family: monospace">%s</span></strong> is required in order for the call to succeed. Configure the OIDC client permissions to add these permissions.
                            </p>
                            </div>
                                
                            """.formatted(perms));
                }


                // add the extension for metadata extraction
                Map<String, Object> extensions = operation.getExtensions();
                if (extensions == null) {
                    operation.setExtensions(Maps.newHashMap());
                }
                operation.getExtensions().put("permissions", perms);
            }

            operation.setDescription(sb.toString());
            return operation;
        };
    }
}
