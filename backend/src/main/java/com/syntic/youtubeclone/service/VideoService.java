package com.syntic.youtubeclone.service;

import com.syntic.youtubeclone.dto.CommentDto;
import com.syntic.youtubeclone.dto.UploadVideoResponse;
import com.syntic.youtubeclone.dto.VideoDto;
import com.syntic.youtubeclone.model.Comment;
import com.syntic.youtubeclone.model.User;
import com.syntic.youtubeclone.model.Video;
import com.syntic.youtubeclone.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {
    @Autowired private final S3Service s3Service;
    @Autowired private final VideoRepository videoRepository;
    @Autowired private final UserService userService;
    public UploadVideoResponse uploadVideo(Authentication authentication, MultipartFile file) {
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
        incrementVideoViewCount(savedVideo);
        userService.addVideoToHistory(videoId);
        return mapVideoToDto(savedVideo);
    }

    private void incrementVideoViewCount(Video video) {
        video.incrementViewCount();
        videoRepository.save(video);
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
        dto.setLikeCount(video.getLikes().get());
        dto.setDislikeCount(video.getDisLikes().get());
        dto.setViewCount(video.getViewCount().get());
        return dto;
    }

    public VideoDto likeVideo(String videoId) {
        // retrieve the video
        Video video = getVideoById(videoId);

        // increment Like Count
        // if user already liked the video, then decrement like count
        if (userService.ifLikedVideo(videoId)) {
            video.decrementLikes();
            userService.removeFromLikedVideos(videoId);
        } else if(userService.ifDislikedVideo(videoId)) {
            video.decrementDisLikes();
            userService.removeFromDislikedVideos(videoId);
            video.incrementLikes();
            userService.addToLikedVideos(videoId);
        } else {
            video.incrementLikes();
            userService.addToLikedVideos(videoId);
        }

        Video saved = videoRepository.save(video);

        return mapVideoToDto(saved);
    }


    public VideoDto dislikeVideo(String videoId) {
        Video video = getVideoById(videoId);

        if(userService.ifDislikedVideo(videoId)) {
            userService.removeFromDislikedVideos(videoId);
            video.decrementDisLikes();
        } else if(userService.ifLikedVideo(videoId)) {
            userService.removeFromLikedVideos(videoId);
            video.decrementLikes();
            userService.addTolDislikedVideos(videoId);
            video.incrementDisLikes();
        } else {
            userService.addTolDislikedVideos(videoId);
            video.incrementDisLikes();
        }

        Video saved = videoRepository.save(video);
        return mapVideoToDto(saved);
    }

    public VideoDto addComment(String videoId, CommentDto commentDto) {
        Video savedVideo = videoRepository.findById(videoId)
                .orElseThrow(()-> new IllegalArgumentException(String.format("No video found with id %s", videoId)));

        Comment comment = new Comment();
        comment.setText(commentDto.getCommentText());

        User currentUser = userService.getCurrentUser();
        if(!commentDto.getAuthorId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Not the same authorId as current logged in user!");
        }
        comment.setAuthorId(currentUser.getId());
        savedVideo.addComment(comment);

        return  mapVideoToDto(videoRepository.save(savedVideo));
    }

    public List<CommentDto> getCommentsOfVideo(String videoId) {
        Video savedVideo = videoRepository.findById(videoId)
                .orElseThrow(()-> new IllegalArgumentException(String.format("No video found with id %s", videoId)));
        return savedVideo.getCommentList().stream().map(this::mapToCommentDto).toList();
    }

    private CommentDto mapToCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setAuthorId(comment.getAuthorId());
        dto.setCommentText(comment.getText());
        return dto;
    }

    public List<VideoDto> getAllVideos() {
        return videoRepository.findAll().stream().map(this::mapVideoToDto).toList();
    }
}
