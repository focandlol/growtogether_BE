package focandlol.domain.dto.study.notice;

import focandlol.domain.entity.notice.NoticeEntity;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDetailsDto {

  private Long id;

  private String title;

  private String content;

  private LocalDateTime createdAt;

  public static NoticeDetailsDto fromEntity(NoticeEntity noticeEntity) {
    return NoticeDetailsDto.builder()
        .id(noticeEntity.getId())
        .title(noticeEntity.getTitle())
        .content(noticeEntity.getContent())
        .createdAt(noticeEntity.getCreatedAt())
        .build();
  }

}
