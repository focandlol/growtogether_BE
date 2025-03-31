package focandlol.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "point_transaction")
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @Column(nullable = false)
    private LocalDateTime date; // 발생일자

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type; // 거래 유형 (적립, 사용, 충전)

    @Column(nullable = false)
    private Integer amount; // 포인트 금액

    // 거래 유형 Enum
    public enum TransactionType {
        REWARD, USE,CHARGE
    }
}
