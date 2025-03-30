package focandlol.api.study.service.vote;

import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public abstract class AbstractVoteJob implements Job {

  private final VoteService voteService;

  @Override
  public void execute(JobExecutionContext context) {
    Long id = context.getJobDetail().getJobDataMap().getLong("id");

    executeJob(voteService, id);
  }

  protected abstract void executeJob(VoteService voteService, Long id);

}
