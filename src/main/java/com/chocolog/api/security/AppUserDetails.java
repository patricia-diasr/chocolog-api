package com.chocolog.api.security;

import com.chocolog.api.model.Employee;
import com.chocolog.api.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class AppUserDetails implements UserDetails {

    private final Employee employee;
    private final GrantedAuthority authority;

    public AppUserDetails(Employee employee) {
        this.employee = employee;
        this.authority = new SimpleGrantedAuthority("ROLE_" + employee.getRole().name());
    }

    public Role getRole() {
        return this.employee.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(this.authority);
    }

    @Override
    public String getPassword() {
        return this.employee.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return this.employee.getLogin();
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
        return this.employee.isActive();
    }

    public Long getEmployeeId() {
        return this.employee.getId();
    }
}