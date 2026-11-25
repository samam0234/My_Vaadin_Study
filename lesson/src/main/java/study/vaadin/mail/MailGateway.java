package study.vaadin.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import study.vaadin.config.AppProperties;

@Service
public class MailGateway {

    private static final Logger log = LoggerFactory.getLogger(MailGateway.class);

    private final ObjectProvider<JavaMailSender> senders;
    private final AppProperties properties;

    public MailGateway(ObjectProvider<JavaMailSender> senders, AppProperties properties) {
        this.senders = senders;
        this.properties = properties;
    }

    @Async
    public void notifyNewPost(String title) {
        String text = "새 글이 등록되었습니다: " + title;
        if (!properties.mailEnabled()) {
            log.info("메일 대신 기록: {}", text);
            return;
        }
        JavaMailSender sender = senders.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender가 없어 메일을 보내지 못했습니다.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.mailFrom());
        message.setTo(properties.mailFrom());
        message.setSubject("새 글");
        message.setText(text);
        sender.send(message);
    }
}
