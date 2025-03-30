package focandlol.api.study.service.notice;

import static focandlol.common.exception.response.ErrorCode.NOTICE_NOT_FOUND;
import static focandlol.common.exception.response.ErrorCode.NOT_A_STUDY_LEADER;
import static focandlol.common.exception.response.ErrorCode.NOT_A_STUDY_MEMBER;
import static focandlol.common.exception.response.ErrorCode.STUDY_NOT_FOUND;
import static focandlol.domain.type.StudyMemberType.LEADER;

import focandlol.common.exception.custom.CustomException;
import focandlol.domain.dto.study.notice.NoticeCreateDto;
import focandlol.domain.dto.study.notice.NoticeDetailsDto;
import focandlol.domain.dto.study.notice.NoticeListDto;
import focandlol.domain.dto.study.notice.NoticeUpdateDto;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.notice.NoticeEntity;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.notice.NoticeRepository;
import focandlol.domain.repository.post.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NoticeService {

  private final NoticeRepository noticeRepository;
  private final StudyRepository studyRepository;
  private final JoinRepository joinRepository;

  /**
   * 공지 리스트 조회
   *
   * @param studyId  스터디 id
   * @param pageable pageable 객체
   * @return NoticeListDto
   */
  public NoticeListDto getNotices(Long studyId, Pageable pageable) {
    return NoticeListDto.fromEntityPage(noticeRepository.findByStudy_StudyId(studyId, pageable));
  }

  /**
   * 공지사항 내부
   *
   * @param noticeId 공지사항 ID
   * @return NoticeDetailsDto
   */
  public NoticeDetailsDto getNotice(Long noticeId) {
    return noticeRepository.findById(noticeId)
        .map(entity -> NoticeDetailsDto.fromEntity(entity))
        .orElseThrow(() -> new CustomException(NOTICE_NOT_FOUND));
  }

  /**
   * 공지 생성
   *
   * 로그인 개발 후 Long userId 파라미터 추가 예정 StudyEntity 개발 후 validateStudyLeader 메서드 추가 예정
   */
  public NoticeCreateDto.Response createNotice(Long memberId, Long studyId, NoticeCreateDto.Request request) {
    Study study = studyRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

    validateStudyLeader(memberId, studyId);

    return NoticeCreateDto.Response.fromEntity(noticeRepository.save(request.toEntity(study)));
  }

  /**
   * 공지 수정
   *
   * 로그인 개발 후 Long userId 파라미터 추가 예정 StudyEntity 개발 후 validateStudyLeader 메서드 추가 예정
   */
  public NoticeUpdateDto.Response updateNotice(Long memberId, Long noticeId, NoticeUpdateDto.Request request) {
    NoticeEntity noticeEntity = noticeRepository.findById(noticeId)
        .orElseThrow(() -> new CustomException(NOTICE_NOT_FOUND));

    //로그인 후 추가 예정
    validateStudyLeader(memberId, noticeEntity.getStudy().getStudyId());

    noticeEntity.setTitle(request.getTitle());
    noticeEntity.setContent(request.getContent());

    return NoticeUpdateDto.Response.fromEntity(noticeEntity);
  }

  /**
   * 공지 삭제
   *
   * 로그인 개발 후 Long userId 파라미터 추가 예정 StudyEntity 개발 후 validateStudyLeader 메서드 추가 예정
   */
  public void deleteNotice(Long memberId, Long noticeId){
    NoticeEntity noticeEntity = noticeRepository.findById(noticeId)
        .orElseThrow(() -> new CustomException(NOTICE_NOT_FOUND));

    validateStudyLeader(memberId, noticeEntity.getStudy().getStudyId());

    noticeRepository.deleteById(noticeId);
  }

  /**
   * 스터디 팀장인지 확인
   * StudyEntity 개발 후 추가 예정
   */
  private void validateStudyLeader(Long memberId, Long studyId) {
    StudyMemberEntity studyMemberEntity = joinRepository.findByMember_MemberIdAndStudy_StudyId(memberId, studyId)
        .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));

    if(!LEADER.equals(studyMemberEntity.getStatus())){
      throw new CustomException(NOT_A_STUDY_LEADER);
    }
  }

}
