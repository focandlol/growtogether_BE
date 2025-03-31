package focandlol.domain.dto.member;

import focandlol.domain.entity.MemberEntity;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MemberResponseDto {

    private Long memberId;
    private String nickName;
    private String email;
    private String phone;
    private String githubUrl;
    private String profileImageUrl;
    private Integer points;
    private Double rating;
    private List<String> skills;

    public MemberResponseDto(MemberEntity member) {
        this.memberId = member.getMemberId();
        this.nickName = member.getNickName();
        this.email = member.getEmail();
        this.phone = member.getPhone();
        this.githubUrl = member.getGithubUrl();
        this.profileImageUrl = member.getProfileImageUrl();
        this.points = member.getPoints();
        this.rating = member.getRating();

        this.skills = member.getUserSkills() == null ? List.of()
                : member.getUserSkills().stream()
                .map(memberSkill -> memberSkill.getSkill().getSkillName())
                .collect(Collectors.toList());
    }
}
