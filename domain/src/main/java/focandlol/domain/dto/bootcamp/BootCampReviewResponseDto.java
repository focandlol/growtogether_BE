package focandlol.domain.dto.bootcamp;

import focandlol.domain.entity.BootCampReview;
import focandlol.domain.type.ProgramCourse;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BootCampReviewResponseDto {

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String author;
        private String profileImageUrl;
        private String content;
        private String imageUrl;
        private String bootCampName;
        private LocalDate startdate;
        private LocalDate enddate;
        private int learningLevel;
        private int assistantSatisfaction;
        private int programSatisfaction;
        private int likeCount;
        private Long viewCount;
        private int commentCount;
        private ProgramCourse programCourse;
        private List<String> skillNames;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(BootCampReview review) {

            List<String> skillNames = review.getBootCampSkills().stream()
                    .map(skill -> skill.getSkill().getSkillName())
                    .toList();

            return Response.builder()
                    .id(review.getBootCampId())
                    .title(review.getTitle())
                    .author(review.getMember().getNickName())
                    .profileImageUrl(review.getMember().getProfileImageUrl())
                    .content(review.getContent())
                    .imageUrl(review.getImageUrl())
                    .bootCampName(review.getBootCampName())
                    .startdate(review.getBootCampStartDate())
                    .enddate(review.getBootCampEndDate())
                    .learningLevel(review.getLearningLevel())
                    .assistantSatisfaction(review.getAssistantSatisfaction())
                    .programSatisfaction(review.getProgramSatisfaction())
                    .likeCount(review.getLikeCount())
                    .viewCount(review.getViewCount())
                    .programCourse(review.getProgramCourse())
                    .commentCount(review.getCommentCount())
                    .skillNames(skillNames)
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<Response> reviews;
        private int totalPages;
        private int page;
        private long totalElements;
        private int size;

        public static PageResponse fromEntityPage(Page<BootCampReview> bootCampReviews){

            return PageResponse.builder()
                    .reviews(bootCampReviews.getContent().stream()
                            .map(Response::fromEntity)
                            .collect(Collectors.toList()))
                    .totalPages(bootCampReviews.getTotalPages())
                    .page(bootCampReviews.getNumber()+1)
                    .totalElements(bootCampReviews.getTotalElements())
                    .size(bootCampReviews.getSize())
                    .build();
        }
    }
}