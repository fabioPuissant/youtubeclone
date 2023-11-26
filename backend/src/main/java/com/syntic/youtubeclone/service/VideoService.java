package com.syntic.youtubeclone.service;

import com.syntic.youtubeclone.dto.UploadVideoResponse;
import com.syntic.youtubeclone.dto.VideoDto;
import com.syntic.youtubeclone.model.Video;
import com.syntic.youtubeclone.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class VideoService {
    @Autowired private final S3Service s3Service;
    @Autowired private final VideoRepository videoRepository;
    public UploadVideoResponse uploadVideo(MultipartFile file) {
        String videoUrl = s3Service.uploadFile(file);
        var video = new Video();
        video.setVideoUrl(videoUrl);

       var savedVideo = videoRepository.save(video);

       var uploadedVideoResponse =  new UploadVideoResponse();
       uploadedVideoResponse.setVideoId(savedVideo.getId());
       uploadedVideoResponse.setVideoUrl(savedVideo.getVideoUrl());
       return uploadedVideoResponse;
    }

    public VideoDto editVideo(VideoDto dto) {
        Video savedVideo = getVideoById(dto.getId());

        // map the videoDto fields to video
        savedVideo.setTitle(dto.getTitle());
        savedVideo.setDescription(dto.getDescription());
        savedVideo.setVideoStatus(dto.getVideoStatus());
        savedVideo.setThumbnailUrl(dto.getThumbnailUrl());
        //savedVideo.setVideoUrl(dto.getVideoUrl());
        savedVideo.setTags(dto.getTags());

        //save the video to the database
        videoRepository.save(savedVideo);
        return dto;
    }

    public String uploadThumbnail(MultipartFile file, String videoId) {
        // Find the video by video_id
        Video savedVideo = getVideoById(videoId);

        String thumbnailUrl = s3Service.uploadFile(file);
        savedVideo.setThumbnailUrl(thumbnailUrl);
        this.videoRepository.save(savedVideo);
        return thumbnailUrl;
    }

    private Video getVideoById(String videoId) {
        // Find the video by video_id
        return this.videoRepository
                .findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find video by id: %s", videoId)));
    }

    public VideoDto getVideoDetails(String videoId) {
        Video savedVideo = this.getVideoById(videoId);
        return mapVideoToDto(savedVideo);
    }


    private VideoDto mapVideoToDto(Video video) {
        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setVideoStatus(video.getVideoStatus());
        dto.setTags(video.getTags());
        dto.setThumbnailUrl(video.getThumbnailUrl());
        dto.setVideoUrl(video.getVideoUrl());
        return dto;
    }
}
