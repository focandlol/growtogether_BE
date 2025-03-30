package focandlol.domain.entity;

import focandlol.common.entity.BaseEntity;
import focandlol.domain.type.NotiType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notiId;

    @Column(nullable = false)
    private boolean isCheck;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotiType type;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    private MemberEntity member;

    public void markAsRead(){
        this.isCheck = true;
    }

}
