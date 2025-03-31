package focandlol.domain.repository.chat;

import focandlol.domain.entity.chat.ChatEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
  List<ChatEntity> findByStudy_StudyIdAndDateBefore(Long studyId, LocalDateTime lastDate, Pageable pageable);

  List<ChatEntity> findByStudy_StudyIdAndIdLessThanOrderByIdDesc(Long studyId, Long lastIndex, Pageable pageable);


}
