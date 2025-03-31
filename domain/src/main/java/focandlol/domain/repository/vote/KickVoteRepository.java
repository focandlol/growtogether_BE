package focandlol.domain.repository.vote;

import focandlol.domain.entity.vote.KickVoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KickVoteRepository extends JpaRepository<KickVoteEntity, Long> {

}
