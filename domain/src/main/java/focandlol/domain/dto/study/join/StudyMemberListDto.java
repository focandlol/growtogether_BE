package focandlol.domain.dto.study.join;

import focandlol.domain.entity.join.StudyMemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyMemberListDto {
  private String nickname;

  private Long studyMemberId;

  private String profileImageUrl;

  public static StudyMemberListDto fromEntity(StudyMemberEntity studyMemberEntity){

    return StudyMemberListDto.builder()
        .nickname(studyMemberEntity.getMember().getNickName())
        .studyMemberId(studyMemberEntity.getId())
        .profileImageUrl(studyMemberEntity.getMember().getProfileImageUrl())
        .build();
  }
}

