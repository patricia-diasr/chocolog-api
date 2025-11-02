package com.chocolog.api.audit;

import com.chocolog.api.model.Employee;
import com.chocolog.api.security.AppUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<Employee> {

    @Override
    public Optional<Employee> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal() instanceof String) {

            return Optional.empty();
        }

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        return Optional.of(userDetails.getEmployee());
    }
}