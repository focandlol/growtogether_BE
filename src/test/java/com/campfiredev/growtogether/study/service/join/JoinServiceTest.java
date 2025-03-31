//package com.campfiredev.growtogether.study.service.join;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyList;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import com.campfiredev.growtogether.exception.custom.CustomException;
//import focandlol.domain.entity.MemberEntity;
//import focandlol.domain.repository.MemberRepository;
//import com.campfiredev.growtogether.point.service.PointService;
//import com.campfiredev.growtogether.study.dto.join.StudyMemberListDto;
//import com.campfiredev.growtogether.study.entity.Study;
//import focandlol.domain.type.StudyStatus;
//import com.campfiredev.growtogether.study.entity.join.StudyMemberEntity;
//import com.campfiredev.growtogether.study.repository.post.StudyRepository;
//import com.campfiredev.growtogether.study.repository.join.JoinRepository;
//import focandlol.domain.type.StudyMemberType;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class JoinServiceTest {
//
//  @InjectMocks
//  private JoinService joinService;
//
//  @Mock
//  private JoinRepository joinRepository;
//
//  @Mock
//  private MemberRepository memberRepository;
//
//  @Mock
//  private StudyRepository studyRepository;
//
//  @Mock
//  private PointService pointService;
//
//  private MemberEntity member;
//  private Study study;
//  private StudyMemberEntity studyMember;
//
//  @BeforeEach
//  void setUp() {
//    member = MemberEntity.builder()
//        .memberId(1L)
//        .build();
//
//    study = Study.builder()
//        .studyId(1L)
//        .studyStatus(StudyStatus.PROGRESS)  // 예제 스터디 상태
//        .studyCount(10)
//        .build();
//
//    studyMember = StudyMemberEntity.builder()
//        .id(1L)
//        .study(study)
//        .member(member)
//        .status(StudyMemberType.PENDING)
//        .build();
//  }
//
//  /**
//   * 참가 신청 테스트
//   */
//  @Test
//  void join_Success() {
//    // given
//    given(memberRepository.findById(1L)).willReturn(Optional.of(member));
//    given(studyRepository.findById(1L)).willReturn(Optional.of(study));
//    given(joinRepository.findByMemberAndStudy(member, study)).willReturn(Optional.empty());
//
//    // when
//    joinService.join(1L, 1L);
//
//    // then
//    verify(joinRepository, times(1)).save(any(StudyMemberEntity.class));
//  }
//
//  /**
//   * 참가 신청 실패 - 이미 신청된 경우
//   */
//  @Test
//  void join_Fail_AlreadyJoined() {
//    // given
//    given(memberRepository.findById(1L)).willReturn(Optional.of(member));
//    given(studyRepository.findById(1L)).willReturn(Optional.of(study));
//    given(joinRepository.findByMemberAndStudy(member, study)).willReturn(Optional.of(studyMember));
//
//    // when & then
//    assertThrows(CustomException.class, () -> joinService.join(1L, 1L));
//  }
//
//  /**
//   * 참가 신청 확정 테스트
//   */
//  @Test
//  void confirmJoin_Success() {
//    given(joinRepository.findWithStudyAndMemberById(1L)).willReturn(Optional.of(studyMember));
//
//    // when
//    joinService.confirmJoin(1L);
//
//    // then
//    verify(pointService, times(1)).usePoint(anyLong(), anyInt());
//  }
//
//  /**
//   * 참가 신청 확정 실패 - 이미 확정된 경우
//   */
//  @Test
//  void confirmJoin_Fail_AlreadyConfirmed() {
//    // given
//    studyMember.setStatus(StudyMemberType.NORMAL);
//    given(joinRepository.findWithStudyAndMemberById(1L)).willReturn(Optional.of(studyMember));
//
//    // when & then
//    assertThrows(CustomException.class, () -> joinService.confirmJoin(1L));
//  }
//
//  /**
//   * 참가 신청 취소 테스트
//   */
//  @Test
//  void cancelJoin_Success() {
//    // given
//    given(joinRepository.findWithStudyAndMemberById(1L)).willReturn(Optional.of(studyMember));
//
//    // when
//    joinService.cancelJoin(1L);
//
//    // then
//    verify(joinRepository, times(1)).deleteById(1L);
//  }
//
//  /**
//   * 대기중인 참여자 리스트 조회 테스트
//   */
//  @Test
//  void getPendingList_Success() {
//    // given
//    List<StudyMemberEntity> pendingList = List.of(studyMember);
//    given(joinRepository.findByStudyWithMembersInStatus(1L, List.of(StudyMemberType.PENDING)))
//        .willReturn(pendingList);
//
//    // when
//    StudyMemberListDto result = joinService.getPendingList(1L);
//
//    // then
//    assertNotNull(result);
//    verify(joinRepository, times(1)).findByStudyWithMembersInStatus(anyLong(), anyList());
//  }
//
//  /**
//   * 참여자 리스트 조회 테스트 (NORMAL, LEADER, KICK)
//   */
//  @Test
//  void getJoinList_Success() {
//    // given
//    List<StudyMemberEntity> joinList = List.of(studyMember);
//    given(joinRepository.findByStudyWithMembersInStatus(1L, List.of(StudyMemberType.NORMAL, StudyMemberType.LEADER, StudyMemberType.KICK)))
//        .willReturn(joinList);
//
//    // when
//    StudyMemberListDto result = joinService.getJoinList(1L);
//
//    // then
//    assertNotNull(result);
//    verify(joinRepository, times(1)).findByStudyWithMembersInStatus(anyLong(), anyList());
//  }
//}
