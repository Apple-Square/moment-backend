package applesquare.moment.email.service;

import applesquare.moment.email.dto.MailDTO;

public interface EmailSendService {
    void send(MailDTO mailDTO);
}
