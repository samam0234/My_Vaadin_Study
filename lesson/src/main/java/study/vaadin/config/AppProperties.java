package study.vaadin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String corsOrigin,
        String tokenSecret,
        int tokenTtlHours,
        String uploadDir,
        long uploadMaxBytes,
        String aiProvider,
        String aiUrl,
        String aiModel,
        String aiKey,
        String paymentSecret,
        String mailFrom,
        boolean mailEnabled
) {
}
