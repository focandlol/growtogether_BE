package focandlol.api.study.service.vote;

import focandlol.domain.entity.vote.VoteEntity;

public interface VoteProcessor {
  void processVote(VoteEntity voteEntity, Long votes, Long totalSize);
}
