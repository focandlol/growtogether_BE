package focandlol.api.study.controller.vote;

import focandlol.api.study.service.vote.VoteService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.vote.VoteCreateDto;
import focandlol.domain.dto.study.vote.VoteDto;
import focandlol.domain.dto.study.vote.VotingDto;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/study")
public class VoteController {

  private final VoteService voteService;

  /**
   * 투표 로그인한 사용자 id 넘길 예정
   *
   * @param voteId    투표 id
   * @param votingDto 찬반
   */
  @PostMapping("/vote/{voteId}")
  public void vote(@AuthenticationPrincipal CustomUserDetails customUserDetails,
      @PathVariable Long voteId, @RequestBody @Valid VotingDto votingDto) {
    voteService.vote(customUserDetails.getMemberId(), voteId, votingDto);
  }

  /**
   * 강퇴 투표 시작 로그인한 사용자 id 넘길 예정
   */
  @PostMapping("/{studyId}/vote")
  public void createKickVote(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long studyId,
      @RequestBody @Valid VoteCreateDto voteCreateDto) {
    voteService.createKickVote(customUserDetails.getMemberId(), studyId, voteCreateDto);
  }

  /**
   * 투표 리스트 조회
   */
  @GetMapping("/{studyId}/vote")
  public ResponseEntity<List<VoteDto>> getPollsInProgress(@PathVariable Long studyId) {
    return ResponseEntity.ok(voteService.getVotes(studyId));
  }

  @GetMapping("/vote/{voteId}")
  public VoteDto getDetailsVote(@PathVariable Long voteId) {
    return voteService.getDetailVote(voteId);
  }

//  //임시 테스트용
//  @PostMapping("/{studyId}/change_vote")
//  public void createChangeVote(@PathVariable Long studyId,
//      @RequestBody @Valid ScheduleUpdateDto scheduleUpdateDto) {
//    voteService.createChangeVote(1L, studyId, 1L, scheduleUpdateDto);
//  }

}
