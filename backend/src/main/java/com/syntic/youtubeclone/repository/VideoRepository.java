package com.syntic.youtubeclone.repository;

import com.syntic.youtubeclone.model.Video;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface VideoRepository extends MongoRepository<Video, String> {
}
