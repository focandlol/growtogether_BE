package focandlol.domain.repository;


import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 사용자 읽지 않는 알림 조회
    List<Notification> findByMemberAndIsCheckFalseOrderByCreatedAtDesc(MemberEntity member);
}
