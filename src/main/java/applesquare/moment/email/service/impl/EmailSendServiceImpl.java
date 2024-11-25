package applesquare.moment.email.service.impl;

import applesquare.moment.email.dto.MailDTO;
import applesquare.moment.email.exception.MailSendException;
import applesquare.moment.email.service.EmailSendService;
import applesquare.moment.email.service.MailHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSendServiceImpl implements EmailSendService {
    private final JavaMailSender javaMailSender;


    /**
     * 이메일 전송
     * @param mailDTO 전송할 메일 정보
     */
    public void send(MailDTO mailDTO){
        try {
            MailHandler mailHandler = new MailHandler(javaMailSender);

            // 메일 처리기 설정
            mailHandler.setTo(mailDTO.getToEmail());  // 받는 사람 이메일 주소
            mailHandler.setSubject(mailDTO.getTitle());  // 메일 제목
            mailHandler.setText(mailDTO.getMessage(), mailDTO.isUseHtml());  // 메일 내용

            // 메일 전송
            mailHandler.send();
        }
        catch(Exception e){
            e.printStackTrace();
            throw new MailSendException(e.getMessage());
        }
    }
}
