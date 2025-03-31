package focandlol.api.study.service.join;

import static focandlol.common.exception.response.ErrorCode.ALREADY_CONFIRMED;
import static focandlol.common.exception.response.ErrorCode.ALREADY_JOINED_STUDY;
import static focandlol.common.exception.response.ErrorCode.CANCEL_PERMISSION_DENIED;
import static focandlol.common.exception.response.ErrorCode.NOT_A_STUDY_MEMBER;
import static focandlol.common.exception.response.ErrorCode.STUDY_FULL;
import static focandlol.common.exception.response.ErrorCode.STUDY_NOT_FOUND;
import static focandlol.common.exception.response.ErrorCode.TEAM_LEADER_ONLY_CONFIRMATION;
import static focandlol.common.exception.response.ErrorCode.USER_NOT_APPLIED;
import static focandlol.common.exception.response.ErrorCode.USER_NOT_FOUND;
import static focandlol.domain.type.NotiType.STUDY;
import static focandlol.domain.type.StudyMemberType.KICK;
import static focandlol.domain.type.StudyMemberType.LEADER;
import static focandlol.domain.type.StudyMemberType.NORMAL;
import static focandlol.domain.type.StudyMemberType.PENDING;
import static focandlol.domain.type.StudyStatus.PROGRESS;
import static focandlol.domain.type.StudyStatus.RECRUIT;

import focandlol.api.notification.service.NotificationService;
import focandlol.api.point.service.PointService;
import focandlol.common.exception.custom.CustomException;
import focandlol.domain.dto.study.join.JoinCreateDto;
import focandlol.domain.dto.study.join.JoinDetailsDto;
import focandlol.domain.dto.study.join.StudyMemberInfoDto;
import focandlol.domain.dto.study.join.StudyMemberListDto;
import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.repository.MemberRepository;
import focandlol.domain.repository.join.JoinRepository;
import focandlol.domain.repository.post.StudyRepository;
import focandlol.domain.type.StudyMemberType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JoinService {

  private final JoinRepository joinRepository;
  private final MemberRepository memberRepository;
  private final StudyRepository studyRepository;
  private final PointService pointService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final NotificationService notificationService;

  /**
   * 참가 신청
   *
   * @param memberId 로그인한 사용자 id
   * @param studyId  스터디 id
   */
  public void join(Long memberId, Long studyId, JoinCreateDto joinCreateDto) {
    MemberEntity memberEntity = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    Study studyEntity = studyRepository.findById(studyId)
        .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));

    // 참가 조건 검증
    validateJoin(memberEntity, studyEntity);

    // 참가 정보 저장
    StudyMemberEntity save = joinRepository.save(
        StudyMemberEntity.create(studyEntity, memberEntity));

    redisTemplate.opsForValue()
        .set("join" + save.getId(), joinCreateDto.getContent(), 7, TimeUnit.DAYS);

    // 추후 참가 신청 메일 발송 로직 추가

    List<StudyMemberEntity> find = joinRepository.findByStudyWithMembersInStatus(
        studyId, List.of(LEADER));

    StudyMemberEntity studyMemberEntity = find.get(0);

    notificationService.sendNotification(studyMemberEntity.getMember(),
        memberEntity.getNickName() + "님이 참가 신청했습니다.", null, STUDY);
  }

  /**
   * 참가 신청 확정 로그인 구현 이후 현재 로그인 한 사용자 정보도 받을 예정
   */
  public void confirmJoin(Long memberId, Long studyMemberId) {
    StudyMemberEntity studyMemberEntity = joinRepository.findWithStudyAndMemberById(studyMemberId)
        .orElseThrow(() -> new CustomException(USER_NOT_APPLIED));

    validateConfirmJoin(memberId, studyMemberEntity);

    studyMemberEntity.confirm();

    Study study = studyMemberEntity.getStudy();
    study.setParticipant(study.getParticipant() + 1);

    if (Objects.equals(study.getMaxParticipant(), study.getParticipant())) {
      study.setStudyStatus(PROGRESS);
    }

    /**
     * 포인트 확인 후 차감
     * 동시성 이슈로 인해 redisson lock 적용
     */
    pointService.usePoint(studyMemberEntity.getMember().getMemberId(),
        studyMemberEntity.getStudy().getStudyCount() * 5);
  }

  /**
   * 참가 신청 취소 로그인 구현 이후 현재 로그인 한 사용자 정보도 받을 예정
   *
   * @param studyMemberId
   */
  public void cancelJoin(Long memberId, Long studyMemberId) {
    StudyMemberEntity studyMemberEntity = joinRepository.findWithStudyAndMemberById(studyMemberId)
        .orElseThrow(() -> new CustomException(USER_NOT_APPLIED));

    validateCancelJoin(memberId, studyMemberEntity);

    joinRepository.deleteById(studyMemberId);
  }

  /**
   * 참가신청 세부사항 조회
   */
  public JoinDetailsDto getJoin(Long studyMemberId) {
    StudyMemberEntity studyMemberEntity = joinRepository.findWithSkillsById(studyMemberId)
        .orElseThrow(() -> new CustomException(USER_NOT_APPLIED));

    String content = (String) redisTemplate.opsForValue().get("join" + studyMemberId);

    return JoinDetailsDto.from(studyMemberEntity, content);
  }

  /**
   * 참여 신청자 리스트 조회
   *
   * @param studyId 스터디 id
   * @return 원하는 타입의 참여자 리스트 조회 팀장(LEADER), 일반 멤버(NORMAL), 참여 대기자(PENDING), 강퇴자(KICK)
   */
  public List<StudyMemberListDto> getStudyMember(Long studyId, List<StudyMemberType> types) {

    List<StudyMemberEntity> studyMemberList = joinRepository.findByStudyWithMembersInStatus(studyId,
        types);

    return getStudyMemberListDtos(studyMemberList);
  }

  /**
   * 피드백용 참여자 리스트 조회 자기 자신 제외
   */
  public List<StudyMemberListDto> getStudyMemberForFeedback(Long studyId, Long memberId) {

    List<StudyMemberEntity> list = joinRepository.findByStudyWithMembersInStatus(studyId,
        List.of(LEADER, NORMAL, KICK));

    List<StudyMemberEntity> studyMemberList = list.stream()
        .filter(studyMember -> !studyMember.getMember().getMemberId().equals(memberId))
        .collect(Collectors.toList());

    return getStudyMemberListDtos(studyMemberList);
  }

  public StudyMemberInfoDto getStudyMemberInfo(Long memberId, Long studyId) {
    StudyMemberEntity studyMemberEntity = joinRepository.findByMember_MemberIdAndStudy_StudyId(
            memberId, studyId)
        .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));

    return new StudyMemberInfoDto(studyMemberEntity.getMember().getNickName(),
        studyMemberEntity.getId(), studyMemberEntity.getMember().getProfileImageUrl());
  }

  /**
   * 로그인 구현 이후 현재 로그인한 사용자 정보 넘길 예정
   *
   * @param studyMemberEntity
   */
  private void validateCancelJoin(Long memberId, StudyMemberEntity studyMemberEntity) {
    if (!PENDING.equals(studyMemberEntity.getStatus())) {
      throw new CustomException(ALREADY_CONFIRMED);
    }

    //스터디 팀장 혹은 신청자 본인만 취소 가능
    StudyMemberEntity loginUser = joinRepository.findByMember_MemberIdAndStudy_StudyId(
            memberId, studyMemberEntity.getStudy().getStudyId())
        .orElseThrow(() -> new CustomException(NOT_A_STUDY_MEMBER));

    if (!studyMemberEntity.getMember().getMemberId().equals(loginUser.getMember().getMemberId())
        && !LEADER.equals(loginUser.getStatus())) {
      throw new CustomException(CANCEL_PERMISSION_DENIED);
    }

  }

  /**
   * 로그인 구현 이후 현재 로그인한 사용자 정보 넘길 예정
   *
   * @param studyMemberEntity
   */
  private void validateConfirmJoin(Long memberId, StudyMemberEntity studyMemberEntity) {
    if (!PENDING.equals(studyMemberEntity.getStatus())) {
      throw new CustomException(ALREADY_CONFIRMED);
    }

    //현재 로그인 한 사용자 정보 가져와서 해당 스터디 팀장인지 확인
    joinRepository.findByMember_MemberIdAndStudy_StudyIdAndStatusIn(memberId,
            studyMemberEntity.getStudy().getStudyId(), List.of(LEADER))
        .orElseThrow(() -> new CustomException(TEAM_LEADER_ONLY_CONFIRMATION));

    //스터디에 이미 참여인원이 꽉찼으면 신청 받지 않음
    if (studyMemberEntity.getStudy().getMaxParticipant() <= studyMemberEntity.getStudy()
        .getParticipant()) {
      throw new CustomException(STUDY_FULL);
    }
  }

  /**
   * 참가 신청 validation
   *
   * @param memberEntity
   * @param studyEntity
   */
  private void validateJoin(MemberEntity memberEntity, Study studyEntity) {
    if (!RECRUIT.equals(studyEntity.getStudyStatus())) {
      throw new CustomException(STUDY_FULL);
    }

    joinRepository.findByMemberAndStudy(memberEntity, studyEntity)
        .ifPresent(studyMember -> {
          throw new CustomException(ALREADY_JOINED_STUDY);
        });
  }

  private static List<StudyMemberListDto> getStudyMemberListDtos(
      List<StudyMemberEntity> studyMemberList) {
    return studyMemberList.stream()
        .map(studyMember -> StudyMemberListDto.fromEntity(studyMember))
        .collect(Collectors.toList());
  }

}

