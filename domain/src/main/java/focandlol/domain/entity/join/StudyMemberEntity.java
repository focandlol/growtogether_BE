package focandlol.domain.entity.join;

import static focandlol.domain.type.StudyMemberType.NORMAL;
import static focandlol.domain.type.StudyMemberType.PENDING;

import focandlol.common.entity.BaseEntity;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Study;
import focandlol.domain.type.StudyMemberType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "study_member",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_study_user", columnNames = {"study_id", "member_id"})
    }
)
public class StudyMemberEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "study_member_id")
  private Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StudyMemberType status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Study study;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private MemberEntity member;

  public static StudyMemberEntity create(Study study, MemberEntity member){
    return StudyMemberEntity.builder()
        .status(PENDING)
        .study(study)
        .member(member)
        .build();
  }

  public void confirm(){
    status = NORMAL;
  }

}

