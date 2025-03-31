package focandlol.api.study.controller.attendance;

import focandlol.api.study.service.attendance.AttendanceService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.attendance.AttendanceDto;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study")
public class AttendanceController {

  private final AttendanceService attendanceService;

  /**
   * 로그인 구현되면 사용자 정보 넘길 예정
   * @param studyId
   */
  @PostMapping("/{studyId}/attendance")
  public void attendance(@AuthenticationPrincipal CustomUserDetails customUserDetails,  @PathVariable Long studyId){
    attendanceService.attendance(customUserDetails.getMemberId(), studyId);
  }

  @GetMapping("/{studyId}/attendance")
  public List<AttendanceDto> getAttendees(@PathVariable Long studyId, @RequestParam(required = false) String date){
    if(date == null){
      date = String.valueOf(YearMonth.now());
    }
    return attendanceService.getAttendee(studyId, date);
  }

}
