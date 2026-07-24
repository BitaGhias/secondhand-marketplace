package com.secondhand.backend.security;

import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Configuration class: "custom user details service".
 * <p>
 * This class is part of the application security configuration and is loaded by Spring at startup.
 * </p>
 *
 * @author Bita Ghiasvand Jozani
 * @author Ata Torkamani Zadeh Alamdari
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads user by username.
     *
     * @param username the username
     * @return the resulting {@code UserDetails} instance
     * @throws UsernameNotFoundException if an error occurs
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // FIX: جستجوی نام کاربری بدون توجه به بزرگی/کوچکی حروف
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("کاربر یافت نشد: " + username));

        // اگه کاربر مسدود شده باشه، نمیذاریم لاگین کنه
        if (user.isBlocked()) {
            throw new UsernameNotFoundException("حساب کاربری شما مسدود شده است");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
/*نقش کاربر رو (مثلاً ADMIN)
 به فرمتی که Spring Security میفهمه (ROLE_ADMIN)
  تبدیل کن و توی یه لیست بذار
 */