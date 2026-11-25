package study.vaadin.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import study.vaadin.domain.UserAccount;
import study.vaadin.repo.UserAccountRepository;
import study.vaadin.service.ApiException;

@Component
public class SecurityCurrentUser implements CurrentUser {

    private final UserAccountRepository users;

    public SecurityCurrentUser(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public Optional<UserAccount> optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AccountUserDetails details) {
            return Optional.of(details.account());
        }
        if (principal instanceof String name && !"anonymousUser".equals(name)) {
            return users.findByEmail(name.toLowerCase());
        }
        return Optional.empty();
    }

    @Override
    public UserAccount require() {
        return optional().orElseThrow(() -> new ApiException(401, "UNAUTHORIZED", "로그인이 필요합니다."));
    }
}
