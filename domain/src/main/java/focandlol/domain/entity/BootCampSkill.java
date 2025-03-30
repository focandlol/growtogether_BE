package focandlol.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="boot_camp_skill" , uniqueConstraints = @UniqueConstraint(columnNames = {"skill_id", "boot_camp_id"}))
public class BootCampSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boot_camp_skill_id")
    private Long bootCampSkillId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boot_camp_id",nullable = false)
    private BootCampReview bootCampReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id",nullable = false)
    private SkillEntity skill;

}
