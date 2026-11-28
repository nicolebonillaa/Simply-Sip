package com.simplysip.service;

import com.simplysip.dto.AuthResponse;
import com.simplysip.dto.LoginRequest;
import com.simplysip.dto.RegisterRequest;
import com.simplysip.model.User;
import com.simplysip.repository.UserRepository;
import com.simplysip.security.GoogleIdentity;
import com.simplysip.security.GoogleTokenVerifier;
import com.simplysip.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()));
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getUsername());
    }

    /**
     * Signs in (or signs up) with a Google ID token. The token is verified first
     * The person gets the same Simply Sip token as a normal login, so nothing else
     * in the app has to know the account came from Google.
     */
    public AuthResponse googleLogin(String idToken) {
        GoogleIdentity identity = googleTokenVerifier.verify(idToken);

        // Returning Google user: matched by Google's stable id, not by email.
        User user = userRepository.findByGoogleSubject(identity.subject())
                .orElseGet(() -> linkOrCreateGoogleUser(identity));

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getUsername());
    }

    private User linkOrCreateGoogleUser(GoogleIdentity identity) {
        // Never match or create an account from an email Google hasn't verified,
        // or someone could take over an existing account with an email they don't own.
        if (!identity.emailVerified()) {
            throw new IllegalArgumentException("Your Google email isn't verified, so we can't sign you in with it.");
        }
        String email = identity.email().trim().toLowerCase();

        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            // Same email as a password account: link Google to it. They keep their password.
            User user = existing.get();
            user.setGoogleSubject(identity.subject());
            return userRepository.save(user);
        }

        User user = new User();
        user.setName(identity.name() != null && !identity.name().isBlank()
                ? identity.name().trim()
                : email.substring(0, email.indexOf('@')));
        user.setEmail(email);
        // The password column is required, so store a random one nobody knows.
        // Google accounts can't log in with a password.
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setAuthProvider("GOOGLE");
        user.setGoogleSubject(identity.subject());
        return userRepository.save(user);
    }
}