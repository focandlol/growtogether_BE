package focandlol.api.study.controller.post;

import focandlol.api.study.service.post.StudyService;
import focandlol.common.auth.CustomUserDetails;
import focandlol.domain.dto.study.post.PagedStudyDTO;
import focandlol.domain.dto.study.post.StudyDTO;
import focandlol.domain.dto.study.post.StudyFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/study")
public class StudyController {
    private final StudyService studyService;

    @PostMapping
    public StudyDTO createStudy(@Valid @RequestBody StudyDTO dto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return studyService.createStudy(dto, customUserDetails.getMemberId());
    }

    @GetMapping
    public PagedStudyDTO getAllStudies(
            @RequestParam(required = false) String studyType,
            @RequestParam(required = false) List<String> skillStacks,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "CREATED_AT") StudyFilter.SortBy sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {

        StudyFilter filter = new StudyFilter(studyType, skillStacks, date, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size);

        return studyService.getFilteredAndSortedStudies(filter, pageable);
    }

    @GetMapping("/{studyId}")
    public StudyDTO getStudyById(@PathVariable Long studyId) {
        return studyService.getStudyById(studyId);
    }

    @PutMapping("/{studyId}")
    public StudyDTO updateStudy(@PathVariable Long studyId, @Valid @RequestBody StudyDTO dto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return studyService.updateStudy(studyId, dto, customUserDetails.getMemberId());
    }

    @DeleteMapping("/{studyId}")
    public void deleteStudy(@PathVariable Long studyId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        studyService.deleteStudy(studyId, customUserDetails.getMemberId());
    }

    @GetMapping("/popular")
    public List<StudyDTO> getPopularStudies() {
        return studyService.getPopularStudies();
    }

    @GetMapping("/search")
    public PagedStudyDTO searchPostsByTitle(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return studyService.searchStudies(title, pageable);
    }
}


