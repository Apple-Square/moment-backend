package applesquare.moment.email.service.impl;

import applesquare.moment.common.exception.StateExpiredException;
import applesquare.moment.email.dto.MailDTO;
import applesquare.moment.email.exception.EmailValidationException;
import applesquare.moment.email.service.EmailSendService;
import applesquare.moment.email.service.EmailValidationService;
import applesquare.moment.redis.model.RedisKeyType;
import applesquare.moment.redis.repository.RedisRepository;
import applesquare.moment.util.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailValidationServiceImpl implements EmailValidationService {
    private final EmailSendService emailSendService;
    private final RedisRepository redisRepository;

    /**
     * 이메일 인증 코드 요청 메서드
     * 1 ) 인증 코드를 발급하고,
     * 2 ) 이메일로 인증 코드를 전송한 뒤,
     * 3 ) Redis에 상태를 저장하는 메서드
     *
     * @param email 이메일
     */
    @Override
    public void storeAndSendEmailCode(String email){
        // 랜덤 인증 코드 발급
        String code= StringUtil.generateRandomString(StringUtil.NUMBERS,  EMAIL_CODE_LEN);

        // 이메일로 인증 코드를 전송하는 메서드
        MailDTO mailDTO=MailDTO.builder()
                .toEmail(email)
                .title("[MOMENT] 이메일 인증 코드")
                .message("인증 코드 : "+code)
                .build();
        emailSendService.send(mailDTO);

        // Redis에 상태 저장
        redisRepository.saveWithTTL(RedisKeyType.EMAIL_CODE, email, code, EMAIL_CODE_TTL_MINUTE, TimeUnit.MINUTES);
    }

    /**
     * 이메일에 대해 인증 코드가 유효한지 검사
     * @param email 이메일
     * @param code 인증 코드
     */
    @Override
    public void validateEmailCode(String email, String code){
        // Redis에서 인증 상태 검색
        String originalCode=(String)redisRepository.get(RedisKeyType.EMAIL_CODE, email);
        if(code==null){
            throw new IllegalArgumentException("인증코드를 입력해주세요.");
        }
        if(originalCode==null){
            // Redis에 인증 코드가 존재하지 않는다면, 에러 던지기
            throw new StateExpiredException("만료된 인증 코드입니다.");
        }
        if(!code.equals(originalCode)) {
            throw new EmailValidationException("잘못된 인증 코드입니다.");
        }
    }

    /**
     * Redis에서 특정 이메일에 대한 인증 코드를 제거한다
     * @param email 이메일
     */
    @Override
    public void removeEmailCode(String email){
        redisRepository.delete(RedisKeyType.EMAIL_CODE, email);
    }
}
