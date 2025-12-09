package aibe.hosik.post.dto;

public record MatchedUserResponse(
        Long userId,
        String nickname,
        String image,
        String introduction
) {

}
