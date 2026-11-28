package com.simplysip.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    /** The ID token (a signed JWT) the app got from Google after the user signed in there. */
    @NotBlank
    private String idToken;
}