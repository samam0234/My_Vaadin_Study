package study.vaadin.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.vaadin.api.ApiModels.Data;
import study.vaadin.api.ApiModels.Login;
import study.vaadin.api.ApiModels.LoginResult;
import study.vaadin.api.ApiModels.Signup;
import study.vaadin.api.ApiModels.UserPublic;
import study.vaadin.security.TokenService;
import study.vaadin.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final TokenService tokens;

    public AuthController(AuthService auth, TokenService tokens) {
        this.auth = auth;
        this.tokens = tokens;
    }

    @PostMapping("/signup")
    ResponseEntity<Data<UserPublic>> signup(@Valid @RequestBody Signup request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new Data<>(auth.signup(request)));
    }

    @PostMapping("/login")
    Data<LoginResult> login(@Valid @RequestBody Login request) {
        return new Data<>(auth.login(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            tokens.revoke(header.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    Data<UserPublic> me() {
        return new Data<>(auth.me());
    }
}
