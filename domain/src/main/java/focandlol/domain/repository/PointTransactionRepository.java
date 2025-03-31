package focandlol.domain.repository;

import focandlol.domain.entity.MemberEntity;
import focandlol.domain.entity.PointTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByMemberOrderByDateDesc(MemberEntity member);
}
