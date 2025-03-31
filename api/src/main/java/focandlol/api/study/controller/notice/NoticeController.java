package focandlol.api.study.controller.notice;

import focandlol.api.study.service.notice.NoticeService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.notice.NoticeCreateDto;
import focandlol.domain.dto.study.notice.NoticeDetailsDto;
import focandlol.domain.dto.study.notice.NoticeListDto;
import focandlol.domain.dto.study.notice.NoticeUpdateDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study")
public class NoticeController {

  private final NoticeService noticeService;

  /**
   * 공지 리스트 조회
   *
   * @param studyId  스더디 id
   * @param pageable pageable 객체
   * @return NoticeListDto
   */
  @GetMapping("{studyId}/notice")
  public ResponseEntity<NoticeListDto> getNoticesPage(@PathVariable Long studyId,
      Pageable pageable) {
    return ResponseEntity.ok(noticeService.getNotices(studyId, pageable));
  }

  /**
   * 공지 세부 조회
   *
   * @param noticeId 공지사항 id
   * @return NoticeDetailsDto
   */
  @GetMapping("/notice/{noticeId}")
  public ResponseEntity<NoticeDetailsDto> getNoticeDetails(@PathVariable Long noticeId) {
    return ResponseEntity.ok(noticeService.getNotice(noticeId));
  }

  /**
   * 공지 생성
   */
  @PostMapping("{studyId}/notice")
  public ResponseEntity<NoticeCreateDto.Response> addNotice(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long studyId,
      @RequestBody @Valid NoticeCreateDto.Request request) {
    return ResponseEntity.ok(
        noticeService.createNotice(customUserDetails.getMemberId(), studyId, request));
  }

  /**
   * 공지 수정
   */
  @PutMapping("/notice/{noticeId}")
  public ResponseEntity<NoticeUpdateDto.Response> updateNotice(
      @AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long noticeId,
      @RequestBody @Valid NoticeUpdateDto.Request request) {
    return ResponseEntity.ok(
        noticeService.updateNotice(customUserDetails.getMemberId(), noticeId, request));
  }

  /**
   * 공지 삭제
   */
  @DeleteMapping("/notice/{noticeId}")
  public void deleteNotice(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long noticeId) {
    noticeService.deleteNotice(customUserDetails.getMemberId(), noticeId);
  }
}
