package focandlol.domain.dto.study.vote;

import focandlol.domain.entity.vote.ChangeVoteEntity;
import focandlol.domain.entity.vote.KickVoteEntity;
import focandlol.domain.entity.vote.VoteEntity;
import focandlol.domain.type.VoteType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VoteDto {

  private Long voteId;

  private String title;

  private VoteType voteType;

  public static VoteDto fromEntity(VoteEntity voteEntity) {
    return VoteDto.builder()
        .voteId(voteEntity.getId())
        .title(voteEntity.getTitle())
        .voteType(determineVoteType(voteEntity))
        .build();
  }

  private static VoteType determineVoteType(VoteEntity voteEntity) {
    if (voteEntity instanceof KickVoteEntity) {
      return VoteType.KICK;
    } else if (voteEntity instanceof ChangeVoteEntity) {
      return VoteType.CHANGE;
    } else {
      throw new IllegalArgumentException("Unknown vote type: " + voteEntity.getClass());
    }
  }
}
