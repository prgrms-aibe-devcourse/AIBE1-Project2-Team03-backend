package aibe.hosik.resume.controller;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.resume.dto.ResumeDetailResponse;
import aibe.hosik.resume.dto.ResumeRequest;
import aibe.hosik.resume.dto.ResumeResponse;
import aibe.hosik.resume.service.ResumeService;
import aibe.hosik.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume", description = "자기소개서 API") // Swagger Tag
public class ResumeController {
    private final ResumeService resumeService;

    @GetMapping
    @Operation(summary = "자기소개서 목록 조회")
    public ResponseEntity<List<ResumeDetailResponse>> getAllResumes(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(resumeService.getAllResumesByUserId(user.getId()));
    }

    @GetMapping("main")
    @Operation(summary = "대표 자기소개서 목록 조회")
    public ResponseEntity<List<ResumeResponse>> getAllMainResumes() {
        return ResponseEntity.ok(resumeService.getAllMainResumes());
    }

    @GetMapping("{id}")
    @Operation(summary = "자기소개서 조회")
    public ResponseEntity<ResumeDetailResponse> getResume(@PathVariable("id") Long resumeId) {
        return ResponseEntity.ok(resumeService.getResume(resumeId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "자기소개서 생성")
    @ResponseStatus(HttpStatus.CREATED)
    public void createResume(
            @RequestPart ResumeRequest request,
            @RequestParam(required = false) MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        resumeService.createResume(request, file, user);
    }

    @PatchMapping(value = "{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "자기소개서 수정")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateResume(
            @PathVariable("id") Long resumeId,
            @RequestPart ResumeRequest request,
            @RequestParam(required = false) MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        resumeService.updateResume(resumeId, request, file, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "자기소개서 삭제")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResume(
            @PathVariable("id") Long resumeId,
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }

        resumeService.deleteResume(resumeId, user);
    }
}