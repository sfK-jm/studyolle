package io.sfk.studyolle.modules.account;

import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class UserAccount extends User {

    private final Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}
