package focandlol.domain.dto.study.post;

import focandlol.domain.entity.Study;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedStudyDTO {
    private int page;
    private int totalPages;
    private long totalElements;
    private List<StudyDTO> studyList;

    public static PagedStudyDTO from(Page<Study> studyPage, List<StudyDTO> studyList) {
        return PagedStudyDTO.builder()
                .studyList(studyList)
                .page(studyPage.getNumber()+1)
                .totalPages(studyPage.getTotalPages())
                .totalElements(studyPage.getTotalElements())
                .build();
    }
}
