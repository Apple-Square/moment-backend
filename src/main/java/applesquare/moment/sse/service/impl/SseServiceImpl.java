package applesquare.moment.sse.service.impl;

import applesquare.moment.sse.dto.SseSendRequestDTO;
import applesquare.moment.sse.exception.SseEmitterNotFoundException;
import applesquare.moment.sse.exception.SseSendException;
import applesquare.moment.sse.repository.SseEmitterRepository;
import applesquare.moment.sse.service.SseCategory;
import applesquare.moment.sse.service.SseEvent;
import applesquare.moment.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {
    private final SseEmitterRepository sseEmitterRepository;


    /**
     * 특정 사용자의 SseEmitter를 반한한다.
     * 만약 없다면 새로 생성해서 반환한다.
     *
     * @param sseCategory SSE 유형
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    @Override
    public SseEmitter connect(SseCategory sseCategory, String userId){
        // 해당 유저의 SSE Emitter 객체가 이미 있는지 확인
        SseEmitter emitter=getEmitter(sseCategory, userId);

        if(emitter==null){
            // SseEmitter 생성
            emitter = createEmitter(sseCategory, userId);

            // 더미 데이터 전송
            // -> SSE 연결 생성하고 데이터를 하나도 보내지 않는다면, 타임아웃으로 인해 재연결 요청이 들어왔을 때 503 에러가 발생한다.
            // -> 이를 해결하기 위해서는 SSE 연결 생성 직후 더미 데이터를 전송해줘야 한다.
            SseSendRequestDTO sseSendRequestDTO=SseSendRequestDTO.builder()
                    .sseCategory(sseCategory)
                    .receiverId(userId)
                    .sseEvent(SseEvent.CONNECTION)
                    .data("SSE connected successfully!")
                    .build();
            sendEvent(sseSendRequestDTO);
        }

        return emitter;
    }

    /**
     * SSE 전송
     * @param sseSendRequestDTO 전송 요청 정보
     */
    @Override
    public void sendEvent(SseSendRequestDTO sseSendRequestDTO){
        SseCategory sseCategory=sseSendRequestDTO.getSseCategory();
        String receiverId=sseSendRequestDTO.getReceiverId();

        SseEmitter emitter=getEmitter(sseCategory, receiverId);
        if(emitter != null){
            try{
                // 이벤트 전송
                emitter.send(SseEmitter.event()
                        .id(sseSendRequestDTO.getLastEventId())
                        .name(sseSendRequestDTO.getSseEvent().getEventName())  // 이벤트 이름
                        .data(sseSendRequestDTO.getData())  // 전송할 데이터
                );
            } catch (IOException e){
                // 전송 실패 시 SseEmitter 제거
                sseEmitterRepository.remove(sseCategory, receiverId);
                e.printStackTrace();
                throw new SseSendException();
            }
        }
        else {
            throw new SseEmitterNotFoundException("해당 유저를 위한 Emitter 객체가 존재하지 않습니다. (id = "+receiverId+")");
        }
    }


    /**
     * SseEmitter 생성
     * @param sseCategory SSE 유형
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    private SseEmitter createEmitter(SseCategory sseCategory, String userId){
        // SseEmitter 객체 생성
        SseEmitter emitter=new SseEmitter(SSE_TIMEOUT_MS);

        // SseEmitter 콜백 함수 설정
        emitter.onCompletion(() -> {
            // Client가 연결을 정상적으로 종료하면, 기존 SseEmitter 객체 제거
            sseEmitterRepository.remove(sseCategory, userId);
        });

        emitter.onTimeout(() -> {
            try {
                // 명시적으로 스트림을 종료하기 위해 스트림 정상 종료 처리
                emitter.complete();
            } finally {
                // 지정된 시간 안에 Client와의 통신이 없어서 타임 아웃이 발생하면, 기존 SseEmitter 객체 제거
                sseEmitterRepository.remove(sseCategory, userId);
            }
        });

        emitter.onError((e) -> {
            try {
                // 명시적으로 스트림을 종료하기 위해 스트림 오류 종료 처리
                emitter.completeWithError(e);
            } finally {
                // Client와의 통신 중 요류가 발생하면, 기존 SseEmitter 객체 제거
                sseEmitterRepository.remove(sseCategory, userId);
            }
        });

        // SseEmitter 저장
        sseEmitterRepository.save(sseCategory, userId, emitter);

        return emitter;
    }

    /**
     * SSE Emitter 조회
     * @param sseCategory SSE 유형
     * @param userId 사용자 ID
     * @return SseEmitter
     */
    private SseEmitter getEmitter(SseCategory sseCategory, String userId){
        return sseEmitterRepository.findById(sseCategory, userId);
    }
}
