package com.syntic.youtubeclone.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Document(value = "User")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    @Indexed(unique = true)
    private String emailAddress;
    private Set<String> subscribedToUsers = ConcurrentHashMap.newKeySet();;
    private Set<String> subscribers = ConcurrentHashMap.newKeySet();;
    private Set<String> videoHistory = ConcurrentHashMap.newKeySet();
    private Set<String> likedVideos = ConcurrentHashMap.newKeySet();
    private Set<String> dislikedVideos = ConcurrentHashMap.newKeySet();
    @Indexed(unique = true)
    private String sub;


    public void addToLikedVideos(String videoId) {
        this.likedVideos.add(videoId);
    }

    public void removeFromLikedVideos(String videoId) {
        this.likedVideos.remove(videoId);
    }

    public void removeFromDislikedVideos(String videoId) {
        this.dislikedVideos.remove(videoId);
    }

    public void addToDislikedVideos(String videoId) {
        this.dislikedVideos.add(videoId);
    }

    public void addToVideoHistory(String videoId) {
        this.videoHistory.add(videoId);
    }

    public void addSubscriptionToUser(String userId) {
        this.subscribedToUsers.add(userId);
    }

    public void addSubscriber(String userId) {
        this.subscribers.add(userId);
    }

    public void removeSubscriptionToUser(String userId) {
        this.subscribedToUsers.remove(userId);
    }

    public void removeSubscriber(String userId) {
        this.subscribers.remove(userId);
    }
}
