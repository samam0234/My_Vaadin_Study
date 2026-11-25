package study.vaadin.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import study.vaadin.config.AppProperties;
import study.vaadin.repo.UserAccountRepository;

@Service
public class TokenService {

    private final AppProperties properties;
    private final UserAccountRepository users;
    private final Set<String> revoked = ConcurrentHashMap.newKeySet();

    public TokenService(AppProperties properties, UserAccountRepository users) {
        this.properties = properties;
        this.users = users;
    }

    public String issue(long userId) {
        long expiry = Instant.now().plusSeconds(properties.tokenTtlHours() * 3600L).getEpochSecond();
        String payload = userId + "." + expiry;
        return payload + "." + sign(payload);
    }

    public Optional<Authentication> authenticate(String token) {
        if (token == null || token.isBlank() || revoked.contains(token)) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3 || !MessageEqual(sign(parts[0] + "." + parts[1]), parts[2])) {
            return Optional.empty();
        }
        long expiry;
        long userId;
        try {
            userId = Long.parseLong(parts[0]);
            expiry = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
        if (expiry < Instant.now().getEpochSecond()) {
            return Optional.empty();
        }
        return users.findById(userId).map(account -> {
            AccountUserDetails details = new AccountUserDetails(account);
            return new UsernamePasswordAuthenticationToken(details, token, details.getAuthorities());
        });
    }

    public void revoke(String token) {
        if (token != null && !token.isBlank()) {
            revoked.add(token);
        }
    }

    public int purgeExpired() {
        int before = revoked.size();
        long now = Instant.now().getEpochSecond();
        revoked.removeIf(token -> {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return true;
            }
            try {
                return Long.parseLong(parts[1]) < now;
            } catch (NumberFormatException ex) {
                return true;
            }
        });
        return before - revoked.size();
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.tokenSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("토큰 서명에 실패했습니다.", ex);
        }
    }

    private static boolean MessageEqual(String left, String right) {
        return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
