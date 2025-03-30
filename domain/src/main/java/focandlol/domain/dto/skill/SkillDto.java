package focandlol.domain.dto.skill;

import focandlol.domain.entity.SkillEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkillDto {
    private Long id;
    private String name;
    private String category;
    private String imageUrl;

    public static SkillDto fromEntity(SkillEntity entity) {
        return new SkillDto(
                entity.getSkillId(),
                entity.getSkillName(),
                entity.getCategory(),
                entity.getSkillImgUrl()
        );
    }
}
