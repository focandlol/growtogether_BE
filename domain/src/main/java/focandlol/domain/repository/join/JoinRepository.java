package focandlol.domain.repository.join;


import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Study;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.type.StudyMemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JoinRepository extends JpaRepository<StudyMemberEntity, Long> {

  Optional<StudyMemberEntity> findByMemberAndStudy(MemberEntity member, Study study);

  @Query("SELECT sm FROM StudyMemberEntity sm " +
      "JOIN FETCH sm.study s " +
      "JOIN FETCH sm.member u " +
      "WHERE sm.id = :studyMemberId")
  Optional<StudyMemberEntity> findWithStudyAndMemberById(
      @Param("studyMemberId") Long studyMemberId);

  @Query("SELECT sm FROM StudyMemberEntity sm " +
      "LEFT JOIN FETCH sm.member u " +
      "LEFT JOIN FETCH u.userSkills us " +
      "LEFT JOIN FETCH us.skill sk " +
      "WHERE sm.id = :studyMemberId")
  Optional<StudyMemberEntity> findWithSkillsById(
      @Param("studyMemberId") Long studyMemberId);

  @Query("SELECT sm FROM StudyMemberEntity sm " +
      "JOIN FETCH sm.member " +
      "WHERE sm.study.studyId = :studyId AND sm.status IN :statuses")
  List<StudyMemberEntity> findByStudyWithMembersInStatus(@Param("studyId") Long studyId,
      @Param("statuses") List<StudyMemberType> statuses);

  List<StudyMemberEntity> findByStudyAndStatusIn(Study study, List<StudyMemberType> statuses);

  @Query("SELECT sm FROM StudyMemberEntity sm " +
      "JOIN FETCH sm.study " +
      "WHERE sm.study.studyId = :studyId AND sm.member.memberId = :memberId AND sm.status IN :statuses")
  Optional<StudyMemberEntity> findByStudyAndMemberWithStudyInStatus(@Param("studyId") Long studyId,
      @Param("memberId") Long memberId,
      @Param("statuses") List<StudyMemberType> statuses);


  Optional<StudyMemberEntity> findByMember_MemberIdAndStudy_StudyIdAndStatusIn(Long memberId,
      Long studyId, List<StudyMemberType> statuses);

  long countByStudy_StudyIdAndStatusIn(Long studyId, List<StudyMemberType> statuses);

  @Query("SELECT sm FROM StudyMemberEntity sm "
      + "JOIN FETCH sm.member "
      + "WHERE sm.id IN :ids")
  List<StudyMemberEntity> findAllWithMembersInIds(@Param("ids") List<Long> ids);

  Optional<StudyMemberEntity> findByMember_MemberIdAndStudy_StudyId(Long memberId, Long studyId);

  List<StudyMemberEntity> findAllByStudy_StudyId(Long studyId);

 // List<StudyMemberEntity> findByMember_MemberId(Long memberId);



}

