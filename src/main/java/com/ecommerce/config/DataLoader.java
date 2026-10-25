package com.ecommerce.config;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.model.Role;
import com.ecommerce.repository.RoleRepository;

@Configuration
public class DataLoader {

    private static final List<String> INITIAL_ROLES = List.of("ROLE_USER", "ROLE_ADMIN");

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            INITIAL_ROLES.forEach(roleName -> {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    roleRepository.save(new Role(null, roleName));
                }
            });
        };
    }
}
