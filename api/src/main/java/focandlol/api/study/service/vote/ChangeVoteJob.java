package focandlol.api.study.service.vote;

import org.springframework.stereotype.Component;

@Component
public class ChangeVoteJob extends AbstractVoteJob {

  public ChangeVoteJob(VoteService voteService) {
    super(voteService);
  }

  @Override
  protected void executeJob(VoteService voteService, Long id) {
    voteService.sumKickVote(id);
  }
}
