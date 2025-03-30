package focandlol.domain.entity;

import focandlol.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.List;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="boot_camp_comment")
public class BootCampComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bootCampCommentId;

    @Column(nullable = false)
    private String commentContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private BootCampComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL,orphanRemoval = true)
    @OrderBy("bootCampCommentId DESC")
    @BatchSize(size = 10)
    private List<BootCampComment> childComments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boot_camp_id",nullable = false)
    private BootCampReview bootCampReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id",nullable = false)
    private MemberEntity member;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private int depth;
}
