package com.syntic.youtubeclone.controller;

import com.syntic.youtubeclone.service.UserRegistrationService;
import com.syntic.youtubeclone.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
public class UserController {
    private final UserRegistrationService userRegistrationService;
    private final UserService userService;
    @GetMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public String register(Authentication authentication) {
        Jwt jwt = (Jwt)authentication.getPrincipal();
        userRegistrationService.registerUser(jwt.getTokenValue());
        return "User registration successful";
    }

    @PostMapping("/subscribe/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean subscribeUser(@PathVariable String userId) {
        userService.subscribeUser(userId);
        return true;
    }

    @PostMapping("/unsubscribe/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean unsubscribeUser(@PathVariable String userId) {
        userService.unsubscribeUser(userId);
        return true;
    }

    @GetMapping("/{userId}/history")
    @ResponseStatus(HttpStatus.OK)
    public Set<String> getUserHistory(@PathVariable String userId) {
        return this.userService.getUserHistory(userId);
    }
}