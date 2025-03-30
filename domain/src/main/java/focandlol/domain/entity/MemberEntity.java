package focandlol.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "nick_name"),
        @UniqueConstraint(columnNames = "phone_number")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "member_Id")
  private Long memberId;

  // 카카오 로그인 회원
  @Column(name = "kakao_id", nullable = true, length = 30)
  private String kakaoId;

  @Setter
  @Column(name = "nick_name", nullable = false, length = 20)
  private String nickName;

  @Setter
  @Column(name = "phone_number", nullable = true, length = 20)
  private String phone;

  // 카카오 로그인 경우 null
  @Setter
  @Column(name = "password", nullable = true, length = 100)
  private String password;

  // 추후 kakao에서 email 수집 확인 후 email 핸들링 로직 필요
  @Column(name = "email", nullable = false, length = 30)
  private String email;

  @Setter
  @Builder.Default
  private Integer points = 100;

  @Setter
  @Column(name = "github_url")
  private String githubUrl;

  @Setter
  @Column(name = "profile_image_url")
  private String profileImageUrl;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // 사용자 기술 매핑
  @Setter
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MemberSkillEntity> userSkills;
  //@Column(nullable = false)
  @Setter
  @Column(nullable = false)
  private Double rating;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<StudyComment> comments;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Bookmark> bookmarkList;

  @Builder
  public MemberEntity(Long memberId, String kakaoId, String nickName, String phone, String password,
                      String email, Integer points, String githubUrl, String profileImageUrl,
                      LocalDateTime createdAt, LocalDateTime updatedAt, List<MemberSkillEntity> userSkills, Double rating, List<StudyComment> comments) {
    this.memberId = memberId;
    this.kakaoId = kakaoId;
    this.nickName = nickName;
    this.phone = phone;
    this.password = password;
    this.email = email;
    this.points = points;
    this.githubUrl = githubUrl;
    this.profileImageUrl = profileImageUrl;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.userSkills = userSkills;
    this.rating = rating;
    this.comments = comments;
  }

  public void usePoints(int amount) {
    points -= amount;
  }
}