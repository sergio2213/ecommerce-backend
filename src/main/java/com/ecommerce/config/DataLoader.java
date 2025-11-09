package com.ecommerce.config;

import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.model.Role;
import com.ecommerce.repository.RoleRepository;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner hello() {
        return args -> {
            System.out.println("Hello");
        };
    }

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
            if (userRole.isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_USER"));
                System.out.println("Role USER created");
            }

            Optional<Role> adminRole = roleRepository.findByName("ADMIN_USER");
            if (adminRole.isEmpty()) {
                roleRepository.save(new Role(null, "ROLE_ADMIN"));
                System.out.println("Role ADMIN created");
            }
        };
    }
}
