package com.ecommerce.integration.test;

import com.ecommerce.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLoaderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    @Qualifier("initDatabase")
    private CommandLineRunner initDatabase;

    @Test
    void initDatabase_SecondRun_DoesNotFailNorDuplicateRoles() {
        cleanDatabase();
        assertDoesNotThrow(() -> initDatabase.run());
        assertDoesNotThrow(() -> initDatabase.run());
        Optional<Role> roleUser = this.roleRepository.findByName("ROLE_USER");
        Optional<Role> roleAdmin = this.roleRepository.findByName("ROLE_ADMIN");

        assertTrue(roleUser.isPresent());
        assertTrue(roleAdmin.isPresent());
        assertEquals(2, this.roleRepository.count());
    }
}
