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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private static final String USER = "user_normal";
    private static final String ADMIN = "user_admin";

    private static final String RAW_PASSWORD = "12345";

    @BeforeEach
    void clear() {
        this.orderItemRepository.deleteAllInBatch();
        this.orderRepository.deleteAllInBatch();
        this.cartItemRepository.deleteAllInBatch();
        this.cartRepository.deleteAllInBatch();
        this.productRepository.deleteAllInBatch();
        this.userRepository.deleteAllInBatch();
        this.roleRepository.deleteAllInBatch();


        Role userRole = this.roleRepository.save(new Role(null, "ROLE_USER"));
        Role adminRole = this.roleRepository.save(new Role(null, "ROLE_ADMIN"));

        // user + carrito
        User user = new User();
        user.setUsername(USER);
        user.setPassword(this.passwordEncoder.encode(RAW_PASSWORD));
        user.setEmail("user@email.com");
        user.setRoles(Set.of(userRole));
        this.userRepository.save(user);

        Cart userCart = new Cart();
        userCart.setUser(user);
        this.cartRepository.save(userCart);

        // admin + carrito
        User admin = new User();
        admin.setUsername(ADMIN);
        admin.setPassword(this.passwordEncoder.encode(RAW_PASSWORD));
        admin.setEmail("admin@email.com");
        admin.setRoles(Set.of(adminRole));
        this.userRepository.save(admin);

        Cart adminCart = new Cart();
        adminCart.setUser(admin);
        this.cartRepository.save(adminCart);
    }

    @Test
    void createProduct_Failure_RequiresAdminRole() {
        ProductInputDTO dto = createProductDTO("Name", 10);
        HttpEntity<ProductInputDTO> request = new HttpEntity<>(dto);

        // intentar crear un producto con credenciales de user normal debería resultar en FORBIDDEN
        ResponseEntity<Product> response = restTemplate.withBasicAuth(USER, RAW_PASSWORD)
            .exchange(ADMIN_URL + "/new", HttpMethod.POST, request, Product.class);
            // no hace falta el host porque TestRestTemplate ya lo añade
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "El acceso sin rol ADMIN debe resultar en 403 Forbidden.");
    }

    // prueba para crear un producto con rol de administrador
    @Test
    void createProduct_Success_WithAdminRole() {
        ProductInputDTO dto = createProductDTO("Name", 10);

        HttpEntity<ProductInputDTO> request = new HttpEntity<>(dto);

        ResponseEntity<Product> response = restTemplate.withBasicAuth(ADMIN, RAW_PASSWORD)
            .exchange(ADMIN_URL + "/new", HttpMethod.POST, request, Product.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "El acceso con rol ADMIN debe resultar en 201 OK.");
        Product createdProduct = response.getBody();
        assertAll("Verificación del producto creado",
            () -> assertNotNull(createdProduct.getId(), "El producto creado debe tener un ID asignado"),
            () -> assertEquals("Name", createdProduct.getName(), "El nombre del producto creado debe coincidir"),
            () -> assertEquals(10, createdProduct.getStock(), "El stock del producto creado debe coincidir")
        );
    }

    @Test
    void createProduct_Failure_MissingFields() {
        ProductInputDTO dto = new ProductInputDTO();
        HttpEntity<ProductInputDTO> request = new HttpEntity<>(dto);
        ResponseEntity<String> response = restTemplate.withBasicAuth(ADMIN, RAW_PASSWORD)
            .exchange(ADMIN_URL + "/new", HttpMethod.POST, request, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "La falta de campos obligatorios, debe resultar en 400 Bad Request.");
    }

    @Test
    void getProductById_Success() {
        Product product = createProduct("New Product", 10);
        ResponseEntity<Product> response = restTemplate.withBasicAuth(USER, RAW_PASSWORD)
            .getForEntity(USER_URL + "/" + product.getId(), Product.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "La obtención de un producto existente debe resultar en 200 OK.");
        assertEquals("New Product", product.getName(), "El nombre del producto obtenido debe coincidir.");
    }

    @Test
    void getAllProducts_Success() {
        // creamos dos productos de prueba
        createProduct("A", 1);
        createProduct("B", 1);
        ResponseEntity<Product[]> response = restTemplate.withBasicAuth(USER, RAW_PASSWORD)
            .getForEntity(USER_URL, Product[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "La obtención de la lista de productos debe resultar en 200 OK.");
        Product[] products = response.getBody();
        List<String> productNames = Arrays.stream(products)
            .map(p -> p.getName())
            .toList();
        assertTrue(products.length == 2, "La lista de productos debe contener dos elementos.");
        assertTrue(productNames.contains("A"));
        assertTrue(productNames.contains("B"));
    }

    // verificar que solo un ADMIN puede actualizar un producto
    @Test
    void updateProduct_Failure_RequiresAdminRole() {
        Product product = createProduct("New Product", 10);
        long productIdToUpdate = product.getId();

        ProductInputDTO dto = new ProductInputDTO();
        dto.setName("New Awesome Product");
        dto.setDescription("Description");
        dto.setPrice(new BigDecimal("20.99"));
        dto.setStock(20);

        HttpEntity<ProductInputDTO> request = new HttpEntity<>(dto);

        ResponseEntity<String> response = restTemplate.withBasicAuth(USER, RAW_PASSWORD)
            .exchange(ADMIN_URL + "/" + productIdToUpdate, HttpMethod.PUT, request, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode(), "El acceso sin rol ADMIN debe resultar en 403 Forbidden.");
    }

    // verificar que la eliminación ocurre y el producto ya no existe
    @Test
    void deleteProduct_Success_WithAdminRole() {
        Product productToDelete = createProduct("Product To Delete", 20);
        Long productIdToDelete = productToDelete.getId();

        ResponseEntity<Product> response = restTemplate.withBasicAuth(ADMIN, RAW_PASSWORD)
            .exchange(ADMIN_URL + "/" + productIdToDelete, HttpMethod.DELETE, null, Product.class);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "La eliminación con rol ADMIN debe resultar en 204 No Content.");

        // Verificar que el producto ya no existe
        Optional<Product> optionalProduct = this.productRepository.findById(productIdToDelete);
        assertTrue(optionalProduct.isEmpty());
    }

    // verificar que un ID de producto inexistente devuelve 404
    @Test
    void getProductById_Failure_NonExistentId() {
        Long nonExistentProductId = 9999L;
        ResponseEntity<Product> response = restTemplate.withBasicAuth(USER, RAW_PASSWORD)
            .getForEntity(USER_URL + "/" + nonExistentProductId, Product.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "La obtención de un producto inexistente debe resultar en 404 Not Found.");
    }

    private Product createProduct(String name, int stock) {
        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setStock(stock);
        newProduct.setPrice(new BigDecimal("10.99"));
        newProduct.setDescription("Description");
        return this.productRepository.save(newProduct);
    }

    private ProductInputDTO createProductDTO(String name, int stock) {
        ProductInputDTO dto = new ProductInputDTO();
        dto.setName(name);
        dto.setStock(stock);
        dto.setPrice(new BigDecimal("10.99"));
        dto.setDescription("Description");
        return dto;
    }
}
