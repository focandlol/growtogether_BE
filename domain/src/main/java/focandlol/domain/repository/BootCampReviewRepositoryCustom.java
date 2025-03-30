package focandlol.domain.repository;

import focandlol.domain.entity.BootCampReview;
import focandlol.domain.strategy.WeightCalculateStrategy;
import focandlol.domain.type.ProgramCourse;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface BootCampReviewRepositoryCustom {
    List<BootCampReview> searchBootCamps(String bootCampName , String title, ProgramCourse programCourse , String skillName, Sort sort);

    List<BootCampReview> findTopRankedReviews(WeightCalculateStrategy strategy, int limit);
}
