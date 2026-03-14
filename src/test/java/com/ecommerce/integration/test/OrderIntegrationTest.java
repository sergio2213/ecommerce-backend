package com.ecommerce.integration.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.dto.AuthRequestDTO;
import com.ecommerce.dto.AuthResponseDTO;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
public class OrderIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    private static final String CHECKOUT_URL = "/api/orders/checkout";
    private static final String CART_ADD_URL = "/api/carts/add-product";
    private static final String LOGIN_URL = "/api/auth/login";

    private final String USERNAME = "user_normal"; 
    private final String PASSWORD = "12345";

    private final int INITIAL_STOCK = 10;
    private final int PURCHASE_QUANTITY = 3;

    private String userToken;

    private void cleanDatabase() {
        this.orderRepository.deleteAllInBatch();
        this.cartItemRepository.deleteAllInBatch();
        this.cartRepository.deleteAllInBatch();
        this.productRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
        this.roleRepository.deleteAllInBatch();
    }

    private void createUserRoleAndUser() {
        Role userRole = this.roleRepository.findByName("ROLE_USER").orElseGet(
            () -> this.roleRepository.save(new Role(null, "ROLE_USER"))
        );
        User user = new User();
        user.setUsername(USERNAME);
        user.setPassword(this.passwordEncoder.encode(PASSWORD));
        user.setEmail("test@test.com");
        user.setRoles(Set.of(userRole));
        this.userRepository.save(user);
        
        Cart cart = new Cart();
        cart.setUser(user);
        this.cartRepository.save(cart);
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createUserRoleAndUser();
        this.userToken = loginAndGetToken(USERNAME, PASSWORD);
    }

    @Test
    void placeOrder_Success_CreatesOrderAndUpdatesStock() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        Product product = createTestProduct("Laptop", INITIAL_STOCK);
        Long productId = product.getId();
        addProductToCart(productId, PURCHASE_QUANTITY);
        ResponseEntity<OrderDTO> response = restTemplate
            .exchange(CHECKOUT_URL, HttpMethod.POST, new HttpEntity<>(headers), OrderDTO.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "El checkout exitoso debe resultar en 201 Created.");
        OrderDTO createdOrder = response.getBody();
        assertNotNull(createdOrder.getId(), "La order creada debe tener un ID");
        Product updatedProduct = this.productRepository.findById(productId).orElseThrow();
        int expectedStock = 7; // stock inicial 10 - cantidad comprada 3
        assertEquals(expectedStock, updatedProduct.getStock(), "El stock de la base de datos debe haberse actualizado correctamente.");
        User user = this.userRepository.findByUsername(USERNAME).orElseThrow();
        Cart userCart = this.cartRepository.findByUserId(user.getId()).orElseThrow();
        long itemsInCart = this.cartItemRepository.countByCartId(userCart.getId());
        assertEquals(0, itemsInCart, "El carrito debe estar vacío en la db");
    }

    @Test
    void placeOrder_Failure_InsufficientStockRollback() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        Product product = createTestProduct("Sony PlayStation 5", INITIAL_STOCK);
        Long productId = product.getId();
        addProductToCart(productId, 5);
        Product productToUpdate = this.productRepository.findById(productId).orElseThrow();
        productToUpdate.setStock(3);
        this.productRepository.save(productToUpdate);
        ResponseEntity<String> response = restTemplate
            .exchange(CHECKOUT_URL, HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(0, this.orderRepository.count(), "No debería haberse guardado ninguna orden debido al rollback");
        Product finalProduct = this.productRepository.findById(productId).orElseThrow();
        assertEquals(3, finalProduct.getStock(), "El stock debe permanecer en 3");
        User user = this.userRepository.findByUsername(USERNAME).orElseThrow();
        Cart userCart = this.cartRepository.findByUserId(user.getId()).orElseThrow();
        long itemsInCart = this.cartItemRepository.countByCartId(userCart.getId());
        assertEquals(1, itemsInCart, "El carrito debe seguir con los productos porque el checkout falló");
    }

    private Product createTestProduct(String name, int stock) {
        Product testProduct = new Product();
        testProduct.setName(name);
        testProduct.setDescription("Some description");
        testProduct.setStock(stock);
        testProduct.setPrice(new BigDecimal("100.00"));
        return this.productRepository.save(testProduct);
    }

    private void addProductToCart(Long productId, Integer quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);;
        Map<String, Number> params = new HashMap<>();
        params.put("productId", productId);
        params.put("quantity", quantity);
        ResponseEntity<String> response = restTemplate
            .exchange(
                CART_ADD_URL + "?productId={productId}&quantity={quantity}",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class,
                params
            );
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Debe poder añadir al carrito antes del checkout");
    }

    private String loginAndGetToken(String username, String password) {
        AuthRequestDTO authRequest = new AuthRequestDTO(username, password);
        ResponseEntity<AuthResponseDTO> authResponse = restTemplate.postForEntity(LOGIN_URL, authRequest, AuthResponseDTO.class);
        assertEquals(HttpStatus.OK, authResponse.getStatusCode());
        assertNotNull(authResponse.getBody().getToken());
        return authResponse.getBody().getToken();
    }
}
