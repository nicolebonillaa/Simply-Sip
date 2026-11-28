package com.simplysip.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Checks the ID token the app gets from Google, so the server never has to trust
 * an email or name the app merely claims. A token is accepted only if:
 * - it is signed by Google (checked against Google's public keys),
 * - it was issued by Google,
 * - it was issued for one of this app's client IDs (app.google.client-ids),
 * - it has not expired.
 */
@Service
public class GoogleTokenVerifier {

    private static final String GOOGLE_KEYS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final List<String> GOOGLE_ISSUERS =
            List.of("https://accounts.google.com", "accounts.google.com");

    private final List<String> clientIds;
    private final JwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-ids:}") String clientIdSetting) {
        List<String> ids = Arrays.stream(clientIdSetting.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
        this.clientIds = ids;

        OAuth2TokenValidator<Jwt> issuer = jwt -> GOOGLE_ISSUERS.contains(jwt.getClaimAsString("iss"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token was not issued by Google", null));

        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience() != null
                && jwt.getAudience().stream().anyMatch(ids::contains)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token was not issued for this app", null));

        NimbusJwtDecoder nimbus = NimbusJwtDecoder.withJwkSetUri(GOOGLE_KEYS_URI).build();
        nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefault(), issuer, audience));
        this.decoder = nimbus;
    }

    /**
     * Verifies the token and returns who it says the person is.
     *
     * @throws IllegalStateException if no Google client IDs are configured
     * @throws org.springframework.security.oauth2.jwt.JwtException if the token is invalid,
     *         expired, or not meant for this app
     */
    public GoogleIdentity verify(String idToken) {
        if (clientIds.isEmpty()) {
            throw new IllegalStateException("Google sign-in is not configured");
        }
        Jwt jwt = decoder.decode(idToken);
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new BadJwtException("Token has no email");
        }
        boolean emailVerified = Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"));
        return new GoogleIdentity(jwt.getSubject(), email, emailVerified, jwt.getClaimAsString("name"));
    }
}