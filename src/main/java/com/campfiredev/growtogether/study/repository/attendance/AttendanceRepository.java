package com.campfiredev.growtogether.study.repository.attendance;

import com.campfiredev.growtogether.study.entity.attendance.AttendanceEntity;
import com.campfiredev.growtogether.study.entity.schedule.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Long> {

  boolean existsByStudyMemberIdAndScheduleId(Long studyMemberId, Long scheduleId);

  List<AttendanceEntity> findByScheduleIn(List<ScheduleEntity> scheduleIds);

  @Query("SELECT DISTINCT a FROM AttendanceEntity a "
      + "LEFT JOIN FETCH a.studyMember sm "
      + "LEFT JOIN FETCH sm.member attendee "
      + "WHERE a.schedule IN :schedules")
  List<AttendanceEntity> findAttendancesBySchedules(
      @Param("schedules") List<ScheduleEntity> schedules);

  @Query("SELECT a FROM AttendanceEntity a "
      + "JOIN FETCH a.studyMember sm "
      + "JOIN FETCH sm.member m "
      + "JOIN FETCH a.schedule s "
      + "WHERE sm.study.studyId = :studyId AND s.start BETWEEN :start AND :end")
  List<AttendanceEntity> findAttendancesBetween(@Param("studyId") Long studyId, @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
