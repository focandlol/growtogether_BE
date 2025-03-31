package focandlol.domain.entity.attendance;

import focandlol.common.entity.BaseEntity;
import focandlol.domain.entity.join.StudyMemberEntity;
import focandlol.domain.entity.schedule.ScheduleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(name = "unique_study_member_schedule", columnNames = {"study_member_id",
            "schedule_id"})
    })
public class AttendanceEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "attendance_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_member_id", nullable = false)
  private StudyMemberEntity studyMember;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", nullable = false)
  private ScheduleEntity schedule;

  public static AttendanceEntity create(StudyMemberEntity studyMember, ScheduleEntity schedule) {
    return AttendanceEntity
        .builder()
        .studyMember(studyMember)
        .schedule(schedule)
        .build();
  }
}
