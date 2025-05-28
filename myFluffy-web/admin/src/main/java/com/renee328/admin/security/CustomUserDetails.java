package com.renee328.admin.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String loginId;
    private final String password;
    private final Boolean isActive;
    private final Boolean isInitialized;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId, String loginId, String password, Boolean isActive, Boolean isInitialized, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.loginId = loginId;
        this.password = password;
        this.isActive = isActive;
        this.isInitialized = isInitialized;
        this.authorities = authorities;
    }

    public Long getUserId() {  // user_id 반환
        return userId;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public Boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    public String getRoleId() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

}
