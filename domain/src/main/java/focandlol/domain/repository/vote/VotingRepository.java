package focandlol.domain.repository.vote;


import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.vote.VoteEntity;
import focandlol.domain.entity.vote.VotingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingRepository extends JpaRepository<VotingEntity, Long> {

  boolean existsByVoteAndStudyMember(VoteEntity vote, StudyMemberEntity studyMember);
}
