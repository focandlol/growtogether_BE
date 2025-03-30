package focandlol.api.study.controller.join;

import focandlol.api.study.service.join.JoinService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.join.JoinCreateDto;
import focandlol.domain.dto.study.join.JoinDetailsDto;
import focandlol.domain.dto.study.join.StudyMemberInfoDto;
import focandlol.domain.dto.study.join.StudyMemberListDto;
import focandlol.domain.type.StudyMemberType;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public class JoinController {

  private final JoinService joinService;

  /**
   * 스터디 참가 신청
   *
   * @param studyId 스터디 id
   */
  @PostMapping("{studyId}/join")
  public void join(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long studyId, @RequestBody JoinCreateDto joinCreateDto) {
    joinService.join(customUserDetails.getMemberId(), studyId, joinCreateDto);
  }

  /**
   * 스터디 참가 확정
   *
   * @param studyMemberId 스터디멤버 id
   */
  @PutMapping("/join/{studyMemberId}")
  public void confirmJoin(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long studyMemberId) {
    joinService.confirmJoin(customUserDetails.getMemberId(), studyMemberId);
  }

  /**
   * 스터디 참가 신청 취소
   *
   * @param studyMemberId 스터디멤버 id
   */
  @DeleteMapping("/join/{studyMemberId}")
  public void cancelJoin(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long studyMemberId) {
    joinService.cancelJoin(customUserDetails.getMemberId(), studyMemberId);
  }

  @GetMapping("/join/{studyMemberId}")
  public JoinDetailsDto getJoinDetails(@PathVariable Long studyMemberId) {
    return joinService.getJoin(studyMemberId);
  }

  /**
   * 스터디 참가자 리스트 조회
   *
   * @param studyId 스터디 id
   * @param types   참가자 타입
   * @return
   */
  @GetMapping("/{studyId}/studyMember")
  public List<StudyMemberListDto> studyMemberList(@PathVariable Long studyId,
      @RequestParam List<StudyMemberType> types) {
    return joinService.getStudyMember(studyId, types);
  }

  /**
   * 피드백용 스터디 참가자 리스트 조회 로그인 구현 이후
   */
  @GetMapping("/{studyId}/studyMember_feedback")
  public List<StudyMemberListDto> studyMemberListFeedback(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long studyId) {
    return joinService.getStudyMemberForFeedback(studyId, customUserDetails.getMemberId());
  }

  @GetMapping("/{studyId}/studyMember_info")
  public StudyMemberInfoDto getStudyMemberInfo(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long studyId){
    return joinService.getStudyMemberInfo(customUserDetails.getMemberId(),studyId);
  }
}

