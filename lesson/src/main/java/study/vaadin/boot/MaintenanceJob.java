package study.vaadin.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import study.vaadin.security.TokenService;

@Component
public class MaintenanceJob {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceJob.class);
    private final TokenService tokens;

    public MaintenanceJob(TokenService tokens) {
        this.tokens = tokens;
    }

    @Scheduled(fixedDelay = 60_000)
    public void purgeRevokedTokens() {
        int removed = tokens.purgeExpired();
        if (removed > 0) {
            log.info("만료된 폐기 토큰 {}건을 지웠습니다.", removed);
        }
    }
}
