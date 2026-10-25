package com.ecommerce.integration.test;

import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
public abstract class BaseIntegrationTest {

    @Autowired
    protected OrderItemRepository orderItemRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected CartRepository cartRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    protected void cleanDatabase() {
        this.orderItemRepository.deleteAllInBatch();
        this.orderRepository.deleteAllInBatch();
        this.cartItemRepository.deleteAllInBatch();
        this.cartRepository.deleteAllInBatch();
        this.productRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
        this.roleRepository.deleteAllInBatch();
    }
}
