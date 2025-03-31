package focandlol.domain.entity.vote;

import static focandlol.domain.type.VoteStatus.PROGRESS;

import focandlol.domain.dto.study.schedule.ScheduleUpdateDto;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.entity.vote.VoteEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor
@DiscriminatorValue("CHANGE")
public class ChangeVoteEntity extends VoteEntity {

  //일정 관리 id
  //추가 예정
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id")
  private ScheduleEntity schedule;

  private String content;

  private LocalDateTime start;

  private LocalDateTime end;

  private Integer total;

  public static ChangeVoteEntity create(String title, StudyMemberEntity studyMemberEntity,
     ScheduleUpdateDto scheduleUpdateDto, ScheduleEntity scheduleEntity) {

    LocalDateTime start = LocalDateTime.of(scheduleUpdateDto.getStartDate(),
        scheduleUpdateDto.getStartTime());

    return ChangeVoteEntity.builder()
        .title(title)
        .schedule(scheduleEntity)
        .studyMember(studyMemberEntity)
        .study(studyMemberEntity.getStudy())
        .status(PROGRESS)
        .content(scheduleUpdateDto.getTitle())
        .start(start)
        .end(start.plusMinutes(scheduleUpdateDto.getTotalTime()))
        .total(scheduleUpdateDto.getTotalTime())
        .build();
  }
}
