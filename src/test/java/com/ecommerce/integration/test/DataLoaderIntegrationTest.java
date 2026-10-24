package com.ecommerce.integration.test;

import com.ecommerce.model.Role;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class DataLoaderIntegrationTest {

    @Autowired
    @Qualifier("initDatabase")
    private CommandLineRunner initDatabase;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private void cleanDatabase() {
        this.orderItemRepository.deleteAllInBatch();
        this.orderRepository.deleteAllInBatch();
        this.cartItemRepository.deleteAllInBatch();
        this.cartRepository.deleteAllInBatch();
        this.productRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
        this.roleRepository.deleteAllInBatch();
    }

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
