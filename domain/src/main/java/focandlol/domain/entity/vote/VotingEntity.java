package focandlol.domain.entity.vote;

import focandlol.common.entity.BaseEntity;
import focandlol.domain.entity.join.StudyMemberEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "voting",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_vote_study_member", columnNames = {"vote_id", "study_member_id"})
    })
public class VotingEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "voting_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id", nullable = false)
  private VoteEntity vote;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_member_id", nullable = false)
  private StudyMemberEntity studyMember;

  public static VotingEntity create(VoteEntity vote, StudyMemberEntity studyMember) {
    return VotingEntity.builder()
        .vote(vote)
        .studyMember(studyMember)
        .build();
  }

}
