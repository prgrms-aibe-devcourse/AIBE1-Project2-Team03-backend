package aibe.hosik.post.controller;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.dto.*;
import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;
import aibe.hosik.post.service.PostService;
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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "모집글 API") // Swagger Tag
public class PostController {
    private final PostService postService;

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "모집글 등록", description = "모집글을 등록합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("headCount") Integer headCount,
            @RequestParam("requirementPersonality") String requirementPersonality,
            @RequestParam("endedAt") String endedAt,
            @RequestParam("category") PostCategory postCategory,
            @RequestParam("type") PostType postType,
            @RequestParam("skills") List<String> skills,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User user
    ) {

        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        LocalDate endDate;

        try {
            endDate = LocalDate.parse(endedAt);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_DATA_FORMAT);
        }

        // RequestParam 값을 DTO로 변환
        PostCreateRequest dto = new PostCreateRequest(
                title,
                content,
                headCount,
                requirementPersonality,
                endDate,
                postCategory,
                postType,
                skills
        );

        PostResponse responseDTO = postService.createPost(dto, image, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }


    @Operation(summary = "모집글 조회", description = "모집글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @Operation(summary = "모집글 상세 조회", description = "모집글 게시글을 상세 조회합니다")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostDetail(postId));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "함께한 모집글 목록 조회", description = "함께한 게시글을 상세 조회합니다")
    @GetMapping("/{userId}/together")
    public ResponseEntity<List<PostTogetherResponse>> getAllPostsByTogether(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        return ResponseEntity.ok(postService.getAllPostsByTogether(userId, user));
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "모집글 삭제", description = "작성자는 모집글을 삭제합니다")
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, @AuthenticationPrincipal User user) {
        postService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "모집글 수정", description = "모집글을 수정합니다.")
    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestParam(value = "title", required = false) String title,
                                        @RequestParam(value = "content", required = false) String content,
                                        @RequestParam(value = "headCount", required = false) Integer headCount,
                                        @RequestParam(value = "requirementPersonality", required = false) String requirementPersonality,
                                        @RequestParam(value = "endedAt", required = false) String endedAt,
                                        @RequestParam(value = "category", required = false) PostCategory postCategory,
                                        @RequestParam(value = "type", required = false) PostType postType,
                                        @RequestParam(value = "skills", required = false) List<String> skills,
                                        @RequestParam(value = "image", required = false) MultipartFile image,
                                        @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        LocalDate endDate = null;
        if (endedAt != null) {
            try {
                endDate = LocalDate.parse(endedAt);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.INVALID_DATA_FORMAT);
            }
        }

        PostUpdateRequest dto = new PostUpdateRequest(
                title,
                content,
                headCount,
                requirementPersonality,
                endDate,
                postCategory,
                postType,
                skills
        );

        PostResponse responseDTO = postService.updatePost(postId, dto, image, user);
        return ResponseEntity.ok(responseDTO);
    }
}
