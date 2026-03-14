package com.ecommerce.integration.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
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
import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UserRepository;

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {"spring.profiles.active=test"}
)
public class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static final String ADMIN_URL = "/api/products/admin";
    private static final String USER_URL = "/api/products";
    private static final String LOGIN_URL = "/api/auth/login";

    private static final String USER = "user_normal";
    private static final String ADMIN = "user_admin";

    private static final String PASSWORD = "12345";

    private String userToken;
    private String adminToken;

    private void cleanDatabase() {
        this.orderItemRepository.deleteAllInBatch();
        this.orderRepository.deleteAllInBatch();
        this.cartItemRepository.deleteAllInBatch();
        this.cartRepository.deleteAllInBatch();
        this.productRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
        this.roleRepository.deleteAllInBatch();
    }

    private void createRolesAndUsers() {
        Role userRole = this.roleRepository.save(new Role(null, "ROLE_USER"));
        Role adminRole = this.roleRepository.save(new Role(null, "ROLE_ADMIN"));

        // user + carrito
        User user = new User();
        user.setUsername(USER);
        user.setPassword(this.passwordEncoder.encode(PASSWORD));
        user.setEmail("user@email.com");
        user.setRoles(Set.of(userRole));
        this.userRepository.save(user);
        
        Cart userCart = new Cart();
        userCart.setUser(user);
        this.cartRepository.save(userCart);

        // admin + carrito
        User admin = new User();
        admin.setUsername(ADMIN);
        admin.setPassword(this.passwordEncoder.encode(PASSWORD));
        admin.setEmail("admin@email.com");
        admin.setRoles(Set.of(adminRole));
        this.userRepository.save(admin);

        Cart adminCart = new Cart();
        adminCart.setUser(admin);
        this.cartRepository.save(adminCart);
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();
        createRolesAndUsers();
        this.userToken = loginAndGetToken(USER, PASSWORD);
        this.adminToken = loginAndGetToken(ADMIN, PASSWORD);
    }

    @Test
    void createProduct_Failure_RequiresAdminRole() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        ProductInputDTO dto = createProductDTO("Laptop", 10);
        ResponseEntity<Product> response = restTemplate
            .exchange(ADMIN_URL + "/new", HttpMethod.POST, new HttpEntity<>(dto, headers), Product.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "El acceso sin rol ADMIN debe resultar en 403 Forbidden.");
    }

    @Test
    void createProduct_Success_WithAdminRole() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.adminToken);
        ProductInputDTO dto = createProductDTO("Laptop", 10);
        ResponseEntity<Product> response = restTemplate
            .exchange(ADMIN_URL + "/new", HttpMethod.POST, new HttpEntity<>(dto, headers), Product.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Product createdProduct = response.getBody();
        assertAll("Verificación del producto creado",
            () -> assertNotNull(createdProduct.getId(), "El producto creado debe tener un ID asignado"),
            () -> assertEquals("Laptop", createdProduct.getName(), "El nombre del producto creado debe coincidir"),
            () -> assertEquals(10, createdProduct.getStock(), "El stock del producto creado debe coincidir")
        );
    }

    @Test
    void createProduct_Failure_MissingFields() {
        ProductInputDTO dto = new ProductInputDTO();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.adminToken);
        ResponseEntity<String> response = restTemplate
            .exchange(ADMIN_URL + "/new", HttpMethod.POST, new HttpEntity<>(dto, headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getProductById_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        Product product = createProduct("iPhone", 10);
        ResponseEntity<Product> response = restTemplate
            .exchange(USER_URL + "/" + product.getId(), HttpMethod.GET, new HttpEntity<>(headers), Product.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("iPhone", product.getName());
    }

    @Test
    void getAllProducts_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        createProduct("A", 1);
        createProduct("B", 1);
        ResponseEntity<Product[]> response = restTemplate
            .exchange(USER_URL, HttpMethod.GET, new HttpEntity<>(headers), Product[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "La obtención de la lista de productos debe resultar en 200 OK.");
        Product[] products = response.getBody();
        List<String> productNames = Arrays.stream(products)
            .map(p -> p.getName())
            .toList();
        assertTrue(products.length == 2, "La lista de productos debe contener dos elementos.");
        assertTrue(productNames.contains("A"));
        assertTrue(productNames.contains("B"));
    }

    @Test
    void updateProduct_Failure_RequiresAdminRole() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        Product product = createProduct("Laptop", 10);
        long productIdToUpdate = product.getId();
        ProductInputDTO dto = new ProductInputDTO();
        dto.setName("Laptop Asus");
        dto.setDescription("Some description");
        dto.setPrice(new BigDecimal("20.99"));
        dto.setStock(20);
        ResponseEntity<String> response = restTemplate
            .exchange(ADMIN_URL + "/" + productIdToUpdate, HttpMethod.PUT, new HttpEntity<>(dto, headers), String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "El acceso sin rol ADMIN debe resultar en 403 Forbidden.");
    }

    @Test
    void deleteProduct_Success_WithAdminRole() {
        Product productToDelete = createProduct("Product To Delete", 20);
        Long productIdToDelete = productToDelete.getId();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.adminToken);
        ResponseEntity<Product> response = restTemplate
            .exchange(ADMIN_URL + "/" + productIdToDelete, HttpMethod.DELETE, new HttpEntity<>(headers), Product.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "La eliminación con rol ADMIN debe resultar en 204 No Content.");
        Optional<Product> optionalProduct = this.productRepository.findById(productIdToDelete);
        assertTrue(optionalProduct.isEmpty());
    }

    @Test
    void getProductById_Failure_NonExistentId() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(this.userToken);
        Long nonExistentProductId = 9999L;
        ResponseEntity<Product> response = restTemplate
            .exchange(USER_URL + "/" + nonExistentProductId, HttpMethod.GET, new HttpEntity<>(headers), Product.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "La obtención de un producto inexistente debe resultar en 404 Not Found.");
    }

    private Product createProduct(String name, int stock) {
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setStock(stock);
        newProduct.setPrice(new BigDecimal("10.99"));
        newProduct.setDescription("Some description");
        return this.productRepository.save(newProduct);
    }

    private ProductInputDTO createProductDTO(String name, int stock) {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setName(name);
        dto.setStock(stock);
        dto.setPrice(new BigDecimal("10.99"));
        dto.setDescription("Some description");
        return dto;
    }

    private String loginAndGetToken(String username, String password) {
        AuthRequestDTO authRequest = new AuthRequestDTO(username, password);
        ResponseEntity<AuthResponseDTO> authResponse = restTemplate.postForEntity(LOGIN_URL, authRequest, AuthResponseDTO.class);
        assertEquals(HttpStatus.OK, authResponse.getStatusCode());
        assertNotNull(authResponse.getBody().getToken());
        return authResponse.getBody().getToken();
    }
}
