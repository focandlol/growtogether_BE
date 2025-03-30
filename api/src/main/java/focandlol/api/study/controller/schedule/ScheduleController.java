package focandlol.api.study.controller.schedule;

import focandlol.api.study.service.schedule.ScheduleService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.schedule.ScheduleAttendeeMonthDto;
import focandlol.domain.dto.study.schedule.ScheduleCreateDto;
import focandlol.domain.dto.study.schedule.ScheduleDto;
import focandlol.domain.dto.study.schedule.ScheduleMonthDto;
import focandlol.domain.dto.study.schedule.ScheduleUpdateDto;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study")
public class ScheduleController {

  private final ScheduleService scheduleService;

  /**
   * 일정 추가
   */
  @PostMapping("/{studyId}/schedule")
  public void createSchedule(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long studyId,
      @RequestBody @Valid ScheduleCreateDto scheduleCreateDto) {
    scheduleService.createSchedule(studyId, customUserDetails.getMemberId(), scheduleCreateDto);
  }

  /**
   * 일정 수정
   */
  @PutMapping("/schedule/{scheduleId}")
  public void updateSchedule(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long scheduleId,
      @RequestBody @Valid ScheduleUpdateDto scheduleUpdateDto) {
    scheduleService.updateSchedule(customUserDetails.getMemberId(), scheduleId, scheduleUpdateDto);
  }

  /**
   * 일정 삭제 로그인 이후 사용자 id도 넘길 예정
   */
  @DeleteMapping("/schedule/{scheduleId}")
  public void deleteSchedule(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long scheduleId) {
    scheduleService.deleteSchedule(customUserDetails.getMemberId(), scheduleId);
  }

  /**
   * 해당일의 일정 리스트 조회
   */
  @GetMapping("/{studyId}/schedule")
  public ResponseEntity<List<ScheduleDto>> getSchedule(@PathVariable Long studyId,
      @RequestParam(required = false) LocalDate date) {
    if (date == null) {
      date = LocalDate.now();
    }
    return ResponseEntity.ok(scheduleService.getSchedules(studyId, date));
  }

  /**
   * 해당 달의 일정 리스트 조회
   */
  @GetMapping("/{studyId}/schedules")
  public ResponseEntity<ScheduleMonthDto> getSchedules(@PathVariable Long studyId,
      @RequestParam(required = false) String date) {
    if (date == null) {
      date = String.valueOf(YearMonth.now());
    }
    return ResponseEntity.ok(scheduleService.getMonthSchedules(studyId, date));
  }

  /**
   * 해당 달의 일정 + 일정별 출석자 같이 조회
   *
   * @param studyId
   * @param date
   * @return
   */
  @GetMapping("/{studyId}/schedules_attendee")
  public ResponseEntity<ScheduleAttendeeMonthDto> getScheduleAttendee(@PathVariable Long studyId,
      @RequestParam(required = false) String date) {
    if (date == null) {
      date = String.valueOf(YearMonth.now());
    }
    return ResponseEntity.ok(scheduleService.getMonthSchedulesAttendees(studyId, date));
  }
}
