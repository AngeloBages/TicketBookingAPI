package com.ticket_booking.common.security;

import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.common.security.properties.AdminProperties;
import com.ticket_booking.domain.models.Role;
import com.ticket_booking.domain.models.User;
import com.ticket_booking.user.repositories.IRoleRepository;
import com.ticket_booking.user.repositories.IUserRepository;

@Component
public class AdminInitializer implements ApplicationRunner {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties properties;

    public AdminInitializer(
            IUserRepository userRepository,
            IRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AdminProperties properties) {
    	
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (userRepository.existsByEmail("admin@example.com")) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow();

        User admin = User.create(
                properties.name(),
                properties.email(),
                passwordEncoder.encode(properties.password()),
                Set.of(adminRole));

        userRepository.save(admin);
    }
}
