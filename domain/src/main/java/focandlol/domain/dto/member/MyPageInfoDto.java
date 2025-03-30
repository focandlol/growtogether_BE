package focandlol.domain.dto.member;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageInfoDto {

    private String email;
    private String nickName;
    private String profileImageUrl;
    private Integer points;
    private String githubUrl;
    private String phone;
    private List<String> skills;
    private int likedPostCount; // 좋아요한 게시글 개수
    private List<MyPageLikesDto> likedPosts; // 좋아요한 게시글 리스트

}
