package aibe.hosik.profile.controller;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.dto.PostResponse;
import aibe.hosik.post.service.PostService;
import aibe.hosik.profile.dto.ProfileDetailResponse;
import aibe.hosik.profile.dto.ProfileRequest;
import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.profile.service.ProfileService;
import aibe.hosik.resume.dto.ResumeDetailResponse;
import aibe.hosik.resume.service.ResumeService;
import aibe.hosik.review.dto.ReviewResponse;
import aibe.hosik.review.service.ReviewService;
import aibe.hosik.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "프로필 API") // Swagger Tag
public class ProfileController {
    private final ProfileService profileService;
    private final PostService postService;
    private final ReviewService reviewService;
    private final ResumeService resumeService;

    /**
     * 현재 로그인한 사용자의 프로필 조회 (마이페이지)
     */
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "마이프로필 조회")
    @GetMapping("me")
    public ResponseEntity<ProfileDetailResponse> getMyProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        Long userId = user.getId();

        ProfileResponse profile = profileService.getProfileByUserId(userId);
        List<PostResponse> authorPosts = postService.getAllPostsCreatedByAuthor(userId);
        List<PostResponse> joinedPosts = postService.getAllPostsJoinedByUser(userId);
        List<ReviewResponse> reviews = reviewService.getAllReviewsByUserId(userId);
        List<ResumeDetailResponse> resumes = resumeService.getAllResumesByUserId(userId);

        ProfileDetailResponse response = ProfileDetailResponse.from(profile, authorPosts, joinedPosts, reviews, resumes);

        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "다른 사용자 프로필 조회")
    @GetMapping("{id}")
    public ResponseEntity<ProfileDetailResponse> getProfile(
            @PathVariable("id") Long userId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        ProfileResponse profile = profileService.getProfileByUserId(userId);
        List<PostResponse> authorPosts = postService.getAllPostsCreatedByAuthor(userId);
        List<PostResponse> joinedPosts = postService.getAllPostsJoinedByUser(userId);
        List<ReviewResponse> reviews = reviewService.getAllReviewsByUserId(userId);
        List<ResumeDetailResponse> resumes = resumeService.getAllResumesByUserId(userId);

        ProfileDetailResponse response = ProfileDetailResponse.from(profile, authorPosts, joinedPosts, reviews, resumes);

        return ResponseEntity.ok(response);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "프로필 수정")
    @PatchMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfile(
            @RequestPart ProfileRequest request,
            @RequestPart(required = false) MultipartFile image,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        profileService.updateProfile(request, image, user.getProfile().getId());
    }
}