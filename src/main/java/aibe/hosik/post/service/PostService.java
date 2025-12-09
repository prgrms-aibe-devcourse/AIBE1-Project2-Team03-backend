package aibe.hosik.post.service;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.entity.PassStatus;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.dto.*;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.profile.entity.Profile;
import aibe.hosik.skill.entity.PostSkill;
import aibe.hosik.skill.entity.Skill;
import aibe.hosik.skill.repository.PostSkillRepository;
import aibe.hosik.skill.repository.SkillRepository;
import aibe.hosik.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final SkillRepository skillRepository;
    private final PostSkillRepository postSkillRepository;
    private final StorageService storageService;
    private final ApplyRepository applyRepository;

    /**
     * 모든 게시글을 조회하는 메서드입니다.
     *
     * @return 게시글 정보와 관련된 스킬 정보 및 현재 참여자 수를 포함한 PostResponse 객체 리스트
     */
    public List<PostResponse> getAllPosts() {
        // findAllWithSkills로 한 번에 fetch(post, postSkills, skill)
        List<Post> posts = postRepository.findAllWithSkills();

        // 모든 게시글의 선택된 지원자 수를 한 번에 조회
        List<Map<String, Object>> applyCounts = applyRepository.countSelectedAppliesByPostId(PassStatus.PASS);

        // postId -> count 매핑을 생성
        Map<Long, Integer> postIdToCountMap = applyCounts.stream()
                .collect(Collectors.toMap(
                        map -> ((Number) map.get("postId")).longValue(),
                        map -> ((Number) map.get("count")).intValue()
                ));

        return posts.stream()
                .map(post -> {
                    // 이미 로딩된 postSkills에서 skill 이름 추출
                    List<String> skills = post.getPostSkills().stream()
                            .map(ps -> ps.getSkill().getName())
                            .collect(Collectors.toList());

                    int currentCount = postIdToCountMap.getOrDefault(post.getId(), 0);

                    return PostResponse.from(post, skills, currentCount);
                }).collect(Collectors.toList());

    }

    public List<PostResponse> getAllPostsCreatedByAuthor(Long userId) {
        List<Post> posts = postRepository.findAllWithSkills();

        return posts.stream()
                .filter(post -> post.getUser().getId().equals(userId))
                .map(post -> {
                    // fetch된 postSkills에서 skill 추출
                    List<String> skills = skillRepository.findByPostId(post.getId());

                    int currentCount = applyRepository.countByPostIdAndIsSelected(post.getId(), PassStatus.PASS);

                    // DTO 정적 팩토리 메서드 활용
                    return PostResponse.from(post, skills, currentCount);
                }).collect(Collectors.toList());
    }

    public List<PostResponse> getAllPostsJoinedByUser(Long userId) {
        List<Post> posts = postRepository.findAllJoinedByUser(userId, PassStatus.PASS);

        return posts.stream()
                .map(post -> {
                    // fetch된 postSkills에서 skill 추출
                    List<String> skills = skillRepository.findByPostId(post.getId());

                    int currentCount = applyRepository.countByPostIdAndIsSelected(post.getId(), PassStatus.PASS);

                    // DTO 정적 팩토리 메서드 활용
                    return PostResponse.from(post, skills, currentCount);
                }).collect(Collectors.toList());
    }

    public List<PostTogetherResponse> getAllPostsByTogether(Long revieweeId, User user) {
        return postRepository.getAllPostsByTogether(revieweeId, user.getId(), PassStatus.PASS)
                .stream()
                .map(post -> new PostTogetherResponse(post.getId(), post.getTitle()))
                .toList();
    }

    /**
     * 새로운 게시글을 생성하고 저장하는 메서드입니다.
     *
     * @param dto   게시글 생성을 위한 요청 데이터를 담은 DTO
     * @param image 게시글 이미지 파일 (선택 사항)
     * @param user  게시글 작성자 정보
     * @return 생성된 게시글 정보와 관련 데이터가 담긴 응답 DTO
     * @throws RuntimeException 이미지 업로드 실패 시 발생하는 예외
     */
    @Transactional
    public PostResponse createPost(PostCreateRequest dto, MultipartFile image, User user) {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = storageService.upload(image); // Supabase에 업로드 후 URL 획득
        }

        // toEntity 사용해서 Post 객체 생성
        Post post = dto.toEntity(user, imageUrl);
        // 생성한 객체 Post 저장
        Post savePost = postRepository.save(post);

        // 연관된 skill 정보 저장
        List<String> skills = new ArrayList<>();
        // Skill 찾거나 생성
        for (String skillName : dto.skills()) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

            PostSkill postSkill = PostSkill.builder()
                    .post(savePost)
                    .skill(skill)
                    .build();

            // post-skill 연관관계 추가
            postSkillRepository.save(postSkill);
            // 응답 DTO 스킬 저장
            skills.add(skill.getName());
        }

        // 추후 Apply > is_selected 될 때 변경
        int currentCount = 0;
        return PostResponse.from(savePost, skills, currentCount);
    }

    /**
     * 특정 게시글의 상세 정보를 조회하는 메서드입니다.
     *
     * @param postId 상세 정보를 조회하려는 게시글의 ID
     * @return 게시글 정보, 관련된 스킬 정보, 매칭 사용자 정보를 포함한 PostDetailResponse 객체
     */
    public PostDetailResponse getPostDetail(Long postId) {
        // 게시글 정보 조회
        Post post = postRepository.findByIdWithSkills(postId)
                .orElseThrow();

        // 스킬 이름 조회
        List<String> skills = post.getPostSkills().stream()
                .map(s -> s.getSkill().getName())
                .collect(Collectors.toList());

        // 매칭 사용자 정보 조회
        List<MatchedUserResponse> matchedUsers = findMatchedUsers(postId);
        int currentCount = matchedUsers.size();

        return PostDetailResponse.from(post, skills, matchedUsers, currentCount);
    }

    /**
     * 특정 게시글을 삭제하는 메서드입니다.
     *
     * @param postId 삭제하려는 게시글의 ID
     * @throws ResponseStatusException 작성자가 아닐 경우, HTTP 상태 코드 FORBIDDEN과 함께 예외 발생
     */
    @Transactional
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow();
        if (post.getUser() == null || !post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
        postRepository.delete(post);
    }

    /**
     * 주어진 게시글 ID와 수정 데이터를 기반으로 게시글을 업데이트합니다.
     *
     * @param postId 업데이트할 게시글의 ID
     * @param dto    수정할 게시글 데이터를 포함하는 객체
     * @param image  게시글과 함께 업로드할 이미지 파일
     * @param user   요청을 보낸 사용자 객체
     * @return 수정된 게시글 정보를 포함하는 PostResponse 객체
     * @throws ResponseStatusException 작성자가 아닌 사용자가 요청한 경우 FORBIDDEN 상태 코드 예외를 발생시킴
     * @throws RuntimeException        이미지 업로드 실패 시 발생
     */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest dto, MultipartFile image, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow();
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = storageService.upload(image);
            post.setImage(imageUrl);
        }

        // 엔티티 메서드 이용해서 수정
        post.updatePatch(dto);

        List<String> skills = new ArrayList<>();
        if (dto.skills() != null) {
            postSkillRepository.deleteByPostId(postId);

            for (String skillName : dto.skills()) {
                Skill skill = skillRepository.findByName(skillName)
                        .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

                PostSkill postSkill = PostSkill.builder()
                        .post(post)
                        .skill(skill)
                        .build();

                // post-skill 연관관계 추가
                postSkillRepository.save(postSkill);
                skills.add(skill.getName());
            }
        } else {
            skills = post.getPostSkills().stream()
                    .map(s -> s.getSkill().getName())
                    .collect(Collectors.toList());
        }

        // 수정 X
        int currentCount = applyRepository.countByPostIdAndIsSelected(postId, PassStatus.PASS);
        return PostResponse.from(post, skills, currentCount);
    }

    // 현재 매칭된 사람 정보 조회
    private List<MatchedUserResponse> findMatchedUsers(Long postId) {
        List<Apply> applies = applyRepository.findWithUserAndProfileByPostId(postId, PassStatus.PASS);

        return applies.stream()
                .map(apply -> {
                    User user = apply.getUser();
                    Profile profile = user.getProfile();

                    return new MatchedUserResponse(
                            user.getId(),
                            profile.getNickname(),
                            profile.getImage(),
                            profile.getIntroduction()
                    );
                }).toList();
    }
}
