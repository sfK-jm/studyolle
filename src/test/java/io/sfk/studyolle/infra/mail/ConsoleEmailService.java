package io.sfk.studyolle.infra.mail;

import io.sfk.studyolle.infra.mail.EmailMessage;
import io.sfk.studyolle.infra.mail.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        log.info("sent email: {}", emailMessage.getMessage());
    }
}
