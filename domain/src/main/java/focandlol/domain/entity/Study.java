package focandlol.domain.entity;

import focandlol.common.entity.BaseEntity;
import focandlol.domain.dto.study.post.StudyDTO;
import focandlol.domain.dto.study.post.StudyScheduleDto;
import focandlol.domain.dto.study.schedule.MainScheduleDto;
import focandlol.domain.type.StudyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Study extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyId;

    private String title;

    private String description;

    private Long viewCount;

    private Integer maxParticipant;

    private LocalDateTime studyStartDate;

    private LocalDateTime studyEndDate;

    @Setter
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    @Setter
    private StudyStatus studyStatus;

    @Setter
    private Integer participant;

    private String type;

    private Integer studyCount;

    private LocalDateTime studyClosingDate;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL)
    private List<SkillStudy> skillStudies;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id", nullable = false)
    private MemberEntity member;

    public static Study fromDTO(StudyDTO dto) {
        List<LocalDateTime> scheduleList = StudyScheduleDto.formDto(dto.getMainScheduleList()).stream().map(
            MainScheduleDto::getStartTime).toList();

        LocalDateTime studyStartDate = Collections.min(scheduleList);
        LocalDateTime studyEndDate = Collections.max(scheduleList);

        return Study.builder()
                .title(dto.getTitle())
                .description(dto.getContent())
                .viewCount(0L)
                .maxParticipant(dto.getMaxParticipant())
                .studyStartDate(studyStartDate)
                .studyEndDate(studyEndDate)
                .isDeleted(false)
                .studyStatus(StudyStatus.RECRUIT)
                .participant(1)
                .type(dto.getType())
                .studyCount(dto.getMainScheduleList().getDates().size())
                .studyClosingDate(dto.getStudyClosingDate().atTime(LocalTime.MAX).withNano(0))
                .build();
    }

    public void addSkillStudies(List<SkillStudy> skillStudies) {
        this.skillStudies = skillStudies;
    }

    public void setAuthor(MemberEntity author) {
        this.member = author;
    }

    public void updateViewCount() {
        this.viewCount++;
    }
    public void updateFromDto(StudyDTO dto, List<SkillStudy> newSkillStudies) {
        this.title = dto.getTitle();
        this.description = dto.getContent();
        this.maxParticipant = dto.getMaxParticipant();
        this.studyStartDate = dto.getStudyStartDate();
        this.studyEndDate = dto.getStudyEndDate();
        this.type = dto.getType();
        this.skillStudies.addAll(newSkillStudies);
    }


}
