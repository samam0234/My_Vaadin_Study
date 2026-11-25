package study.vaadin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.vaadin.api.ApiModels.Login;
import study.vaadin.api.ApiModels.LoginResult;
import study.vaadin.api.ApiModels.Signup;
import study.vaadin.api.ApiModels.UserPublic;
import study.vaadin.domain.UserAccount;
import study.vaadin.repo.UserAccountRepository;
import study.vaadin.security.CurrentUser;
import study.vaadin.security.TokenService;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final CurrentUser currentUser;

    public AuthService(UserAccountRepository users, PasswordEncoder passwordEncoder, TokenService tokens, CurrentUser currentUser) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.currentUser = currentUser;
    }

    @Transactional
    public UserPublic signup(Signup request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(409, "EMAIL_TAKEN", "이미 가입한 이메일입니다.");
        }
        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setNickname(request.nickname().trim());
        account.setRole("USER");
        users.save(account);
        return publicUser(account);
    }

    @Transactional(readOnly = true)
    public LoginResult login(Login request) {
        UserAccount account = users.findByEmail(request.email().trim().toLowerCase())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ApiException(401, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
        return new LoginResult(publicUser(account), tokens.issue(account.getId()));
    }

    @Transactional(readOnly = true)
    public UserPublic me() {
        return publicUser(currentUser.require());
    }

    public static UserPublic publicUser(UserAccount account) {
        return new UserPublic(account.getId(), account.getEmail(), account.getNickname());
    }
}
