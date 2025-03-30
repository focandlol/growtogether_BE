package focandlol.api.skill.controller;

import focandlol.domain.dto.skill.SkillDto;
import focandlol.domain.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepository;

    @GetMapping
    public ResponseEntity<List<SkillDto>> getAllSkills() {
        List<SkillDto> skills = skillRepository.findAll()
                .stream()
                .map(SkillDto::fromEntity) // 엔티티 -> DTO 변환
                .collect(Collectors.toList());
        return ResponseEntity.ok(skills);
    }
}
