package focandlol.domain.entity.notice;

import focandlol.common.entity.BaseEntity;
import focandlol.domain.entity.Study;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "study_notice")
//BaseEntity 상속 예정
public class NoticeEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "notice_id")
  private Long id;

  @Column(nullable = false)
  @Setter
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  @Setter
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "study_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Study study;
}
