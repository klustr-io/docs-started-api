package io.klustr.example.services;

import com.google.common.collect.Lists;
import io.klustr.spring.U;
import io.klustr.spring.oauth.OAuthCredentialType;
import io.klustr.spring.oauth.PrincipleUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Component
@RequestMapping("/example")
@Tag(name = "Example API", description = "Simple API example in Java.")
public class HelloWorldService {

    public HelloWorldService() {

    }

    @GetMapping("/hello")
    @Operation(summary = "Say Hello",
            operationId = "example1",
            description = "Will say hello given the users credentials.",
            security = @SecurityRequirement(name = OAuthCredentialType.USER_TO_SERVICE)
    )
    // user must have consent for this API to access the user first and last name
    @PreAuthorize("hasAuthority('SCOPE_profile')")
    public String sayHello(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal oauth) {
        String givenName = PrincipleUtils.tryGetField("given_name", oauth);
        String familyName = PrincipleUtils.tryGetField("family_name", oauth);
        return "Hello, " + givenName + " " + familyName;
    }

    @GetMapping("/heartrate")
    @Operation(summary = "Get Heart Rate",
            operationId = "example2",
            description = "Will return a mock heart rate collection.",
            security = @SecurityRequirement(name = OAuthCredentialType.USER_TO_SERVICE)
    )
    // user must have consent for this API to access their heart rate information
    @PreAuthorize("hasAuthority('SCOPE_fitness.heart_rate.read')")
    public List<Integer> getUserHeartRate(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal oauth) {
        // will only be allowed to be called if the user gave consent
        String userId = oauth.getName();
        List<Integer> hbs = Lists.newArrayList();
        for (int i=0;i<=60;i++) {
            hbs.add(U.randomNumber(50,80));
        }
        return hbs;
    }
}
