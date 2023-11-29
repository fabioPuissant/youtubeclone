package com.syntic.youtubeclone.service;

import com.syntic.youtubeclone.model.User;
import com.syntic.youtubeclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        String sub = ((Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getClaim("sub");
        return userRepository.findBySub(sub).orElseThrow(()->
                new IllegalArgumentException(String.format("Cannot find sub %s", sub)));
    }

    public void addToLikedVideos(String videoId) {
        User currentUser= getCurrentUser();
        currentUser.addToLikedVideos(videoId);
        userRepository.save(currentUser);
    }

    public boolean ifLikedVideo(String videoId) {
        return getCurrentUser().getLikedVideos().stream().anyMatch(s-> s.equals(videoId));
    }

    public boolean ifDislikedVideo(String videoId) {
        return getCurrentUser().getDislikedVideos().stream().anyMatch(s-> s.equals(videoId));
    }

    public void removeFromLikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.removeFromLikedVideos(videoId);
        userRepository.save(currentUser);
    }

    public void removeFromDislikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.removeFromDislikedVideos(videoId);
        userRepository.save(currentUser);
    }

    public void addTolDislikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.addToDislikedVideos(videoId);
        userRepository.save(currentUser);
    }

    public void addVideoToHistory(String videoId) {
        User currentUser = getCurrentUser();
        currentUser.addToVideoHistory(videoId);
        userRepository.save(currentUser);
    }

    public void subscribeUser(String userId) {
        // retrieve current user and add user id to the subscribed to users set
        // retrieve the target user and add the current user to the subscribers set

        User currentUser = getCurrentUser();
        User userToSubscribeTo = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("No user found with id "+ userId));

        currentUser.addSubscriptionToUser(userToSubscribeTo.getId());
        userToSubscribeTo.addSubscriber(currentUser.getId());

        this.userRepository.saveAll(Arrays.asList(currentUser, userToSubscribeTo));
    }

    public void unsubscribeUser(String userId) {
        // retrieve current user and add user id to the subscribed to users set
        // retrieve the target user and add the current user to the subscribers set

        User currentUser = getCurrentUser();
        User userToUnsubscribeFrom = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("No user found with id "+ userId));

        currentUser.removeSubscriptionToUser(userToUnsubscribeFrom.getId());
        userToUnsubscribeFrom.removeSubscriber(currentUser.getId());

        this.userRepository.saveAll(Arrays.asList(currentUser, userToUnsubscribeFrom));
    }

    public Set<String> getUserHistory(String userId) {
        User savedUser = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("No user found with id "+ userId));

        return savedUser.getVideoHistory();
    }
}
