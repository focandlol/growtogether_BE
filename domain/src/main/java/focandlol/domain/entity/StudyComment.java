package focandlol.domain.entity;

import focandlol.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StudyComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyCommentId;

    @Column(nullable = false)
    @Setter
    private String commentContent;

    @Column(nullable = false)
    private Long parentCommentId;

    @Column(nullable = false)
    private long studyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;
}