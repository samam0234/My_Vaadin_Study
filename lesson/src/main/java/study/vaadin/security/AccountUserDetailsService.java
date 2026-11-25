package study.vaadin.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import study.vaadin.repo.UserAccountRepository;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserAccountRepository users;

    public AccountUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return users.findByEmail(username.toLowerCase())
                .map(AccountUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
