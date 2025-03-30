package focandlol.domain.dto.study.join;


import focandlol.domain.entity.join.StudyMemberEntity;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinDetailsDto {

  private Long studyMemberId;

  private String nickname;

  private List<String> skillNames;

  private String content;

  /**
   *  points -> rating으로 변경
   */
  private double rating;

  public static JoinDetailsDto from(StudyMemberEntity studyMemberEntity, String content) {
    return JoinDetailsDto.builder()
        .studyMemberId(studyMemberEntity.getId())
        .nickname(studyMemberEntity.getMember().getNickName())
        .rating(studyMemberEntity.getMember().getRating())
        .content(content)
        .skillNames(studyMemberEntity.getMember().getUserSkills().stream()
            .map(skill -> skill.getSkill().getSkillName())
            .collect(Collectors.toList()))
        .build();
  }

}
