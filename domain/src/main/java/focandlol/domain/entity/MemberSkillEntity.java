package focandlol.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Entity
@Table(name = "user_skill", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "skill_id"}) // memberId와 skill_id 조합을 유니크하게 설정
})
@Getter
@NoArgsConstructor
public class MemberSkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_skill_id")
    private Long userSkillId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity user;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private SkillEntity skill;

    public MemberSkillEntity(MemberEntity user, SkillEntity skill) {
        this.user = user;
        this.skill = skill;
    }
}