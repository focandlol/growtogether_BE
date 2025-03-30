package focandlol.api.study.service.vote;

import focandlol.common.exception.custom.CustomException;
import focandlol.common.exception.response.ErrorCode;
import focandlol.domain.entity.vote.ChangeVoteEntity;
import focandlol.domain.entity.vote.KickVoteEntity;
import focandlol.domain.entity.vote.VoteEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VoteProcessorFactory {

  private final Map<Class<? extends VoteEntity>, VoteProcessor> processors = new HashMap<>();

  @Autowired
  public VoteProcessorFactory(List<VoteProcessor> processorList) {
    for (VoteProcessor processor : processorList) {
      if (processor instanceof KickVoteProcessor) {
        processors.put(KickVoteEntity.class, processor);
      } else if (processor instanceof ChangeVoteProcessor) {
        processors.put(ChangeVoteEntity.class, processor);
      }
    }
  }

  public VoteProcessor getProcessor(Class<? extends VoteEntity> voteClass) {
    return processors.getOrDefault(voteClass, (a,b,c) -> {
      throw new CustomException(ErrorCode.VOTE_NOT_FOUND);
    });
  }
}
