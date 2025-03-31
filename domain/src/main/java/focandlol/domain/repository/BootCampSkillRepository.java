package focandlol.domain.repository;

import focandlol.domain.entity.BootCampReview;
import focandlol.domain.entity.BootCampSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BootCampSkillRepository extends JpaRepository<BootCampSkill, Long> {
    void deleteByBootCampReview(BootCampReview review);
}
