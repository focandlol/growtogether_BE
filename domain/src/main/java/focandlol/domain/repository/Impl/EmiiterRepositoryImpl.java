package focandlol.domain.repository.Impl;

import focandlol.domain.repository.EmitterRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmiiterRepositoryImpl implements EmitterRepository {

    private final Map<Long,List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    @Override
    public void save(Long userId, SseEmitter emitter) {
        sseEmitters.computeIfAbsent(userId , k -> new ArrayList<>()).add(emitter);
    }

    @Override
    public void delete(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = sseEmitters.get(userId);

        if(emitter != null){
            emitters.remove(emitter);
            if(emitters.isEmpty()){
                sseEmitters.remove(userId);
            }
        }
    }

    @Override
    public List<SseEmitter> findByUserId(Long userId) {
        return sseEmitters.getOrDefault(userId,new ArrayList<>());
    }

    @Override
    public void deleteAllByUserId(Long userId) {
        sseEmitters.remove(userId);
    }

    @Override
    public List<Long> getAllUserIds() {
        return new ArrayList<>(sseEmitters.keySet());
    }

}
