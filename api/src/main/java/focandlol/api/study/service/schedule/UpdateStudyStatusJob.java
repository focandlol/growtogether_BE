package focandlol.api.study.service.schedule;

import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class UpdateStudyStatusJob implements Job {

    private final ScheduleService scheduleService;

    @Override
    public void execute(JobExecutionContext context) {
        scheduleService.updateExpiredStudies();
    }
}

