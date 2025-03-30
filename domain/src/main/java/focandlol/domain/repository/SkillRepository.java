package focandlol.domain.repository;

import focandlol.domain.entity.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<SkillEntity, Long> {
    List<SkillEntity> findBySkillNameIn(List<String> skillNames);

    @Query("SELECT DISTINCT s.category from SkillEntity s ORDER BY s.category ASC")
    List<String> findDistinctCategories();

    @Query("SELECT DISTINCT s.skillName from SkillEntity s ORDER BY s.skillName ASC")
    List<String> findDistinctSkillName();

    @Query("SELECT DISTINCT s.skill.skillName FROM MemberSkillEntity s WHERE s.user.memberId = :memberId ORDER BY s.skill.skillName ASC")
    List<String> findSkillNamesByMemberId(@Param("memberId") Long memberId);



}