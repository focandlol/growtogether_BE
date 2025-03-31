package focandlol.domain.dto.study.notice;

import focandlol.domain.entity.notice.NoticeEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeListDto {
  private List<Summary> notices;
  private int page;
  private int totalPages;
  private int size;
  private long totalElements;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Summary {
    private Long noticeId;
    private String title;
    private LocalDateTime createdAt;
  }

  public static NoticeListDto fromEntityPage(Page<NoticeEntity> noticePage) {
    List<Summary> summaries = noticePage.getContent().stream()
        .map(notice -> Summary.builder()
            .noticeId(notice.getId())
            .title(notice.getTitle())
            .createdAt(notice.getCreatedAt())
            .build())
        .collect(Collectors.toList());

    return NoticeListDto.builder()
        .notices(summaries)
        .page(noticePage.getNumber())
        .totalPages(noticePage.getTotalPages())
        .size(noticePage.getSize())
        .totalElements(noticePage.getTotalElements())
        .build();
  }
}

