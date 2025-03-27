package com.campfiredev.growtogether.study.dto.vote;

import com.campfiredev.growtogether.study.entity.vote.ChangeVoteEntity;
import com.campfiredev.growtogether.study.type.VoteType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ChangeVoteDetailsDto extends VoteDto{

  private String content;

  private LocalDate prevStartDate;

  private LocalTime prevStartTime;

  private LocalDate prevEndDate;

  private LocalTime prevEndTime;

  private Integer prevTotalTime;

  private LocalDate changeStartDate;

  private LocalTime changeStartTime;

  private LocalDate changeEndDate;

  private LocalTime changeEndTime;

  private Integer changeTotalTime;

  public static ChangeVoteDetailsDto fromEntity(ChangeVoteEntity changeVoteEntity) {

    LocalDateTime start = changeVoteEntity.getSchedule().getStart();


    return ChangeVoteDetailsDto.builder()
        .voteId(changeVoteEntity.getId())
        .title(changeVoteEntity.getTitle())
        .voteType(VoteType.CHANGE)
        .content(changeVoteEntity.getContent())
        .prevStartDate(changeVoteEntity.getSchedule().getStart().toLocalDate())
        .prevStartTime(changeVoteEntity.getSchedule().getStart().toLocalTime())
        .prevEndDate(changeVoteEntity.getSchedule().getEnd().toLocalDate())
        .prevEndTime(changeVoteEntity.getSchedule().getEnd().toLocalTime())
        .prevTotalTime(changeVoteEntity.getSchedule().getTotalTime())
        .changeStartDate(changeVoteEntity.getStart().toLocalDate())
        .changeStartTime(changeVoteEntity.getStart().toLocalTime())
        .changeEndDate(changeVoteEntity.getEnd().toLocalDate())
        .changeEndTime(changeVoteEntity.getEnd().toLocalTime())
        .changeTotalTime(changeVoteEntity.getTotal())
        .build();

  }

}
