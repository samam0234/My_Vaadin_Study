package study.vaadin.security;

import java.util.Optional;
import study.vaadin.domain.UserAccount;

public interface CurrentUser {

    Optional<UserAccount> optional();

    UserAccount require();
}
