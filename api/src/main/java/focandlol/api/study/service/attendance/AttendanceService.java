package focandlol.api.study.service.attendance;

import static focandlol.domain.type.ScheduleType.*;
import static focandlol.domain.type.StudyMemberType.*;
import static focandlol.domain.type.StudyMemberType.NORMAL;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.dto.study.attendance.AttendanceDto;
import focandlol.domain.entity.attendance.AttendanceEntity;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.schedule.ScheduleEntity;
import focandlol.domain.repository.attendance.AttendanceRepository;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.schedule.ScheduleRepository;
import focandlol.domain.type.ScheduleType;
import focandlol.domain.type.StudyMemberType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

  private final AttendanceRepository attendanceRepository;
  private final ScheduleRepository scheduleRepository;
  private final JoinRepository joinRepository;

  public void attendance(Long memberId, Long studyId) {

    StudyMemberEntity studyMemberEntity = joinRepository.findByMember_MemberIdAndStudy_StudyIdAndStatusIn(memberId,
            studyId, List.of(NORMAL, LEADER))
        .orElseThrow(() -> new CustomException(ErrorCode.NOT_A_STUDY_MEMBER));

    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));


    ScheduleEntity scheduleEntity = scheduleRepository.findFirstByTypeAndStudy_StudyIdAndStartBetween(
            MAIN, studyId, now.minusMinutes(10), now.plusMinutes(10))
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ATTENDANCE_TIME));

    if (attendanceRepository.existsByStudyMemberIdAndScheduleId(studyMemberEntity.getId(),
        scheduleEntity.getId())) {
      throw new CustomException(ErrorCode.ALREADY_ATTENDANCE);
    }

    attendanceRepository.save(AttendanceEntity.create(studyMemberEntity, scheduleEntity));
  }

  public List<AttendanceDto> getAttendee(Long studyId, String date) {
    YearMonth yearMonth = YearMonth.parse(date);

    LocalDate startDate = yearMonth.atDay(1);
    LocalDate endDate = yearMonth.atEndOfMonth();

    List<AttendanceEntity> attendees = attendanceRepository.findAttendancesBetween(
        studyId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

    Map<Long, List<String>> collect = attendees.stream()
        .collect(
            Collectors.groupingBy(attendee -> attendee.getSchedule().getId(),
                Collectors.mapping(attendee -> attendee.getStudyMember().getMember().getNickName(),
                    Collectors.toList())));

    return collect.entrySet().stream()
        .map(entry -> {
          ScheduleEntity scheduleEntity = attendees.stream()
              .filter(attendee -> attendee.getSchedule().getId().equals(entry.getKey()))
              .findFirst().map(attendee -> attendee.getSchedule()).orElseThrow();

          return AttendanceDto.builder()
              .scheduleId(scheduleEntity.getId())
              .date(scheduleEntity.getStart())
              .attendees(entry.getValue())
              .build();
        }).collect(Collectors.toList());
  }


}
