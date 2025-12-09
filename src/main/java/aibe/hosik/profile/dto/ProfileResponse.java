package aibe.hosik.profile.dto;

import aibe.hosik.profile.entity.Profile;

public record ProfileResponse(
        Long id,
        String name,
        String introduction,
        String image,
        String nickname,
        Long userId
) {
    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getName(),
                profile.getIntroduction(),
                profile.getImage(),
                profile.getNickname(),
                profile.getUser().getId()
        );
    }
}
