package focandlol.domain.dto.member;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageLikesDto {
    private Long postId;  // 게시글 ID
    private String title; // 게시글 제목
    private String type;  // 게시글 유형 (스터디 / 부트캠프 리뷰)
    private Integer people; // 총 모집예정 인원
    private List<String> skillName; // 기술스택
    private  String status;
    private List<String> bootcampSkillNames;
    private String programCourse;

}
