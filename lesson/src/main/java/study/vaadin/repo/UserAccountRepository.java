package study.vaadin.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import study.vaadin.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}
