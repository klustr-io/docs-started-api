package io.klustr.example.openapi;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
@OpenAPIDefinition
public class EveryApiDocumentation {

    @Bean
    public GroupedOpenApi everyApi() {
        return GroupedOpenApi.builder()
                .group("everything")
                .pathsToMatch(
                        "/**",
                        "/**/**")

                .displayName("Example API")
                .addOperationCustomizer(ApiDocumentation.oauthExpected())
                .addOperationCustomizer(ApiDocumentation.serviceAccountExpected())
                .addOperationCustomizer(ApiDocumentation.permissionCheck())
                .addOpenApiCustomizer(new OpenApiCustomizer() {
                    @Override
                    public void customise(OpenAPI openApi) {
                        openApi.info(new Info()
                                        .description(
                                                """
                                                        ## Hello!

                                                        This is a simple bootstrap of a Java API server.
                                                                                  
                                                        """
                                        )
                                        .title("Example API")
                                        .version("1.0")
                                )
                                .servers(Lists.newArrayList(
                                        new Server().url("https://gateway.dev.klustr.io").description("Production")
                                ));
                    }
                })
                .build();
    }
}
