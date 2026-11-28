package com.simplysip.security;

/**
 * Who Google says the person is, taken from a verified ID token.
 *
 * @param subject       Google's stable id for the person (never changes, unlike email)
 * @param email         the Google account's email
 * @param emailVerified whether Google has verified that email
 * @param name          the person's display name, or null if the token has none
 */
public record GoogleIdentity(String subject, String email, boolean emailVerified, String name) {
}