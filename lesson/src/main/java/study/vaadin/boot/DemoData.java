package study.vaadin.boot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import study.vaadin.domain.Post;
import study.vaadin.domain.UserAccount;
import study.vaadin.repo.PostRepository;
import study.vaadin.repo.UserAccountRepository;

@Component
public class DemoData implements CommandLineRunner {

    private final UserAccountRepository users;
    private final PostRepository posts;
    private final PasswordEncoder passwordEncoder;

    public DemoData(UserAccountRepository users, PostRepository posts, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.posts = posts;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        UserAccount mina = ensure("mina@example.com", "mina", "USER");
        ensure("admin@example.com", "관리자", "ADMIN");
        if (posts.count() == 0) {
            Post post = new Post();
            post.setTitle("첫 글");
            post.setBody("Vaadin Flow 게시판에 오신 것을 환영합니다.");
            post.setAuthor(mina);
            post.getTags().add("intro");
            posts.save(post);
        }
    }

    private UserAccount ensure(String email, String nickname, String role) {
        return users.findByEmail(email).orElseGet(() -> {
            UserAccount account = new UserAccount();
            account.setEmail(email);
            account.setNickname(nickname);
            account.setRole(role);
            account.setPasswordHash(passwordEncoder.encode("password123"));
            return users.save(account);
        });
    }
}
