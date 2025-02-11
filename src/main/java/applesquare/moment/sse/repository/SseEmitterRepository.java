package applesquare.moment.sse.repository;

import applesquare.moment.sse.service.SseCategory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRepository {
    private final Map<SseCategory, Map<String, SseEmitter>> emitterMap;  // <카테고리, <사용자 ID, SseEmitter>>

    public SseEmitterRepository(){
        // thread-safe한 자료구조 사용
        emitterMap=new ConcurrentHashMap<>();
    }

    /**
     * SseEmitter 저장
     * @param sseCategory SSE 유형
     * @param userId 사용자 ID
     * @param emitter SseEmitter 객체
     */
    public void save(SseCategory sseCategory, String userId, SseEmitter emitter){
        emitterMap
                .computeIfAbsent(sseCategory, key->new ConcurrentHashMap<>())
                .put(userId, emitter);
    }

    /**
     * 사용자 ID로 SseEmitter 검색
     * @param sseCategory SSE 유형
     * @param userId 사용자 ID
     * @return SseEmitter 객체 (없으면 null)
     */
    public SseEmitter findById(SseCategory sseCategory, String userId){
        return emitterMap
                .getOrDefault(sseCategory, Collections.emptyMap())
                .get(userId);
    }

    /**
     * 사용자 ID로 SseEmitter 삭제
     * @param sseCategory SSE 유형
     * @param userId 사용자 ID
     */
    public void remove(SseCategory sseCategory, String userId){
        Map<String, SseEmitter> categoryEmitterMap=emitterMap.get(sseCategory);
        if(categoryEmitterMap!=null){
            categoryEmitterMap.remove(userId);
        }
    }
}
