package com.ecommerce.integration.test;

import com.ecommerce.dto.AuthRequestDTO;
import com.ecommerce.dto.AuthResponseDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.RegisterRequestDTO;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.profiles.active=test"})
public class CartIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

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

    @BeforeEach
    void setUp() {
        cleanDatabase();
        this.roleRepository.save(new Role(null, "ROLE_USER"));
    }

    private ResponseEntity<String> register(String username, String email) {
        RegisterRequestDTO body = new RegisterRequestDTO(username, "12345", email);
        return restTemplate.postForEntity("/api/auth/register", body, String.class);
    }

    private Product createTestProduct() {
        Product testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Some description");
        testProduct.setStock(10);
        testProduct.setPrice(new BigDecimal("100.00"));
        return this.productRepository.save(testProduct);
    }

    private String loginAndGetToken(String username, String password) {
        AuthRequestDTO authRequest = new AuthRequestDTO(username, password);
        ResponseEntity<AuthResponseDTO> authResponse = restTemplate.postForEntity("/api/auth/login", authRequest, AuthResponseDTO.class);
        assertEquals(HttpStatus.OK, authResponse.getStatusCode());
        assertNotNull(authResponse.getBody());
        return authResponse.getBody().getToken();
    }

    private long countItemsOf(String username) {
        User user = this.userRepository.findByUsername(username).orElseThrow();
        Cart cart = this.cartRepository.findByUserId(user.getId()).orElseThrow();
        return this.cartItemRepository.countByCartId(cart.getId());
    }

    @Test
    void addToCart_AfterFailedRegistration_UsesTheCorrectCart() {
        ResponseEntity<String> responseUserA = register("usuario_a", "a@test.com");
        assertEquals(HttpStatus.CREATED, responseUserA.getStatusCode());
        ResponseEntity<String> duplicateRegisterResponse = register("usuario_a", "otro@test.com");
        assertEquals(HttpStatus.CONFLICT, duplicateRegisterResponse.getStatusCode());
        ResponseEntity<String> responseUserB = register("usuario_b", "b@test.com");
        assertEquals(HttpStatus.CREATED, responseUserB.getStatusCode());

        String userToken = loginAndGetToken("usuario_b", "12345");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        Product productTest = createTestProduct();
        ResponseEntity<CartItemDTO> response = this.restTemplate.
                exchange("/api/carts/add-product?productId={productId}&quantity={quantity}", HttpMethod.POST, new HttpEntity<>(headers), CartItemDTO.class, productTest.getId(), 5);


        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertEquals(0, countItemsOf("usuario_a"));
        assertEquals(1, countItemsOf("usuario_b"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -20})
    void addToCart_InvalidQuantity_ReturnsBadRequest(int quantity) {
        ResponseEntity<String> registrationResponse = register("usuario_test", "usuario@test.com");
        assertEquals(HttpStatus.CREATED, registrationResponse.getStatusCode());
        String userToken = loginAndGetToken("usuario_test", "12345");
        assertNotNull(userToken);
        Product availableProduct = createTestProduct();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);

        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/carts/add-product?productId={productId}&quantity={quantity}", HttpMethod.POST, new HttpEntity<>(headers), String.class, availableProduct.getId(), quantity);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, countItemsOf("usuario_test"));
    }
}
