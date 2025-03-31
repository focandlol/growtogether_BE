package focandlol.domain.dto.member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageBookmarksDto {
    private int studyCount;       // 내가 참여 중인 스터디 개수
    private int likedPostCount;   // 좋아요한 게시글 개수 (부트캠프 + 스터디 북마크)


}
