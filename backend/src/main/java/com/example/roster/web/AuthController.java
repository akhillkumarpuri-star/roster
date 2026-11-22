package com.example.roster.web;

import com.example.roster.repo.UserRepository;
import com.example.roster.security.JwtService;
import com.example.roster.web.Dtos.LoginRequest;
import com.example.roster.web.Dtos.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final JwtService jwt;

    public AuthController(AuthenticationManager authManager, UserRepository users, JwtService jwt) {
        this.authManager = authManager;
        this.users = users;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        var user = users.findByUsername(req.username()).orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        return new LoginResponse(jwt.generate(user.getUsername(), user.getRole()), user.getUsername(),
                user.getFullName(), user.getRole(), jwt.expiresInSeconds());
    }
}
