package focandlol.domain.dto.study.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import focandlol.domain.entity.Study;
import focandlol.domain.type.StudyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyDTO {
    private Long studyId;

    @NotBlank(message = "제목은 반드시 입력되어야 합니다.")
    private String title;

    @NotBlank(message = "게시글의 내용은 반드시 입력되어야 합니다.")
    private String content;

    private Long viewCount;

    @NotNull(message = "최대정원을 반드시 입력해야 합니다.")
    @Min(value = 2, message = "최소인원은 2명이상 이여야 합니다.")
    private Integer maxParticipant;

    @NotNull(message = "스터디 모집 마감일정은 반드시 입력해야 합니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate studyClosingDate;

    private StudyScheduleDto mainScheduleList;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime studyStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime studyEndDate;

    private StudyStatus studyStatus;

    private Integer participant;

    @NotBlank(message = "어떤 형식의 스터디인지 입력해주세요.")
    private String type;

    private Integer studyCount;

    @NotEmpty(message = "스터디 진행시 사용할 기술스택을 입력해 주세요.")
    private List<String> skillNames;

    private String author;

    @Setter
    private Integer commentCount;

    @Setter
    private Integer likeCount;

    private String profileImageUrl;

    private LocalDateTime createdAt;

    public static StudyDTO fromEntity(Study study) {
        List<String> skillNames = study.getSkillStudies().stream()
                .map(skillStudy -> skillStudy.getSkill().getSkillName())
                .toList();

        return StudyDTO.builder()
                .studyId(study.getStudyId())
                .title(study.getTitle())
                .content(study.getDescription())
                .viewCount(study.getViewCount())
                .studyClosingDate(study.getStudyClosingDate().toLocalDate())
                .maxParticipant(study.getMaxParticipant())
                .studyStartDate(study.getStudyStartDate())
                .studyEndDate(study.getStudyEndDate())
                .studyStatus(study.getStudyStatus())
                .participant(study.getParticipant())
                .type(study.getType())
                .studyCount(study.getStudyCount())
                .skillNames(skillNames)
                .author(study.getMember().getNickName())
                .profileImageUrl(study.getMember().getProfileImageUrl())
                .createdAt(study.getCreatedAt())
                .build();
    }

}

