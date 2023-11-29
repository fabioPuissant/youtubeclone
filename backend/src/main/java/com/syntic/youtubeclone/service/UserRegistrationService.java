package com.syntic.youtubeclone.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syntic.youtubeclone.dto.UserInfoDto;
import com.syntic.youtubeclone.model.User;
import com.syntic.youtubeclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final UserRepository userRepository;

    @Value("${auth0.userinfo.endpoint}")
    private String userInfoEndpoint;

    public void registerUser(String tokenValue) {
        // Make a call to the user info endpoint
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(userInfoEndpoint))
                .setHeader("Authorization", String.format("Bearer %s", tokenValue))
                .build();
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        try {
            HttpResponse<String> rawResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = rawResponse.body();

            ObjectMapper objectMapper = new ObjectMapper();
            // Ignore the other properties that we did not configure in the UserInfoDto object but are received from the Issuer OPENID/Oauth
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            UserInfoDto dto = objectMapper.readValue(body, UserInfoDto.class);

            User user = new User();
            user.setFirstName(dto.getGivenName());
            user.setLastName(dto.getFamilyName());
            user.setFullName(dto.getName());
            user.setEmailAddress(dto.getEmail());
            user.setSub(dto.getSub());

            userRepository.save(user);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Exception occurred while registering user", e);
        }
        // fetch user details and save them to the database

    }
}
