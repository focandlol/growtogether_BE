package focandlol.domain.repository.post;

import focandlol.domain.dto.study.post.StudyFilter;
import focandlol.domain.entity.Study;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyRepositoryCustom {
    Page<Study> findFilteredAndSortedStudies(StudyFilter filter, Pageable pageable);
    Page<Study> searchPostsByTitle(String title, Pageable pageable);
}
