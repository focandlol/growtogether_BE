package focandlol.domain.dto.bootcamp;

import focandlol.domain.entity.BootCampComment;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto {

    private Long commentId;
    private String content;
    private Long userId;
    private String author;
    private Boolean isDeleted;
    private List<CommentResponseDto> childComments;
    private LocalDateTime createdAt;

    public static CommentResponseDto fromEntity(BootCampComment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getBootCampCommentId())
                .content(comment.getCommentContent())
                .userId(comment.getMember().getMemberId())
                .author(comment.getMember().getNickName())
                .isDeleted(comment.getIsDeleted())
                .createdAt(comment.getCreatedAt())
                .childComments(Optional.ofNullable(comment.getChildComments())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(CommentResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<CommentResponseDto> comments;
        private int totalPages;
        private int page;
        private int totalElements;
        private int size;

        public static PageResponse fromEntityPage(Page<BootCampComment> bootCampComments) {
            return PageResponse.builder()
                    .comments(bootCampComments.getContent().stream()
                            .map(CommentResponseDto::fromEntity)
                            .collect(Collectors.toList()))
                    .totalPages(bootCampComments.getTotalPages())
                    .page(bootCampComments.getNumber())
                    .totalElements((int)bootCampComments.getTotalElements())
                    .size(9)
                    .build();
        }
    }
}
