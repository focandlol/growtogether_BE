package focandlol.domain.dto.study.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyFilter {
    private String studyType; // STUDY 또는 PROJECT
    private List<String> technologyStacks; // 기술 스택 리스트
    private LocalDate date; // 특정 날짜
    private SortBy sortBy; // 정렬 조건 (최신순, 조회순, 모집 마감 임박순)

    public enum SortBy {
        CREATED_AT,
        VIEW_COUNT,
        DEADLINE
    }
}

