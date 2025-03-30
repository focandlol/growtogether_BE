package focandlol.api.study.service.vote;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchedulerService {

  private final Scheduler scheduler;

  public void scheduleJob(Class<? extends Job> jobClass, String jobName, String jobGroup,
      long delayMinutes, Map<String, Object> jobDataMap){

    JobDetail jobDetail = JobBuilder.newJob(jobClass)
        .withIdentity(jobName, jobGroup)
        .storeDurably(false)
        .build();

    jobDataMap.forEach(jobDetail.getJobDataMap()::put);

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(jobName + "Tri", jobGroup)
        .startAt(Date.from(Instant.now().plusSeconds(delayMinutes * 60)))
        .build();

    try {
      scheduler.scheduleJob(jobDetail, trigger);
    } catch (SchedulerException e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}
