package com.ecommerce.integration.test;

import com.ecommerce.dto.AuthRequestDTO;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
public class JwtAuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private void createUser() {
        Role userRole = this.roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> this.roleRepository.save(new Role(null, "ROLE_USER")));
        User newUser = new User();
        newUser.setUsername("test_user");
        newUser.setPassword(this.passwordEncoder.encode("password123"));
        newUser.setEmail("test@test.com");
        newUser.setRoles(Set.of(userRole));
        this.userRepository.save(newUser);
    }

    private String generateToken() {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test_user")
                .password("password123")
                .authorities("ROLE_USER")
                .build();
        return this.jwtService.generateToken(userDetails);
    }

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Test
    void getProducts_MalformedToken_ReturnsUnauthorized(CapturedOutput output) {
        String token = "malformed_token";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(output.getOut().contains("Token JWT rechazado"));

    }

    @Test
    void getProducts_NoToken_ReturnsUnauthorized() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getProducts_ValidToken_ReturnsOk() {
        createUser();
        String token = generateToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void login_NoTokenInHeader_ReturnsOk() {
        createUser();
        AuthRequestDTO request = new AuthRequestDTO("test_user", "password123");
        ResponseEntity<String> response = this.restTemplate.postForEntity("/api/auth/login", request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void login_ExpiredTokenInHeader_ReturnsOk(CapturedOutput output) {
        createUser();
        JwtService service = new JwtService(
                jwtSecret,
                -1000
        );
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test_user")
                .password("password123")
                .authorities("ROLE_USER")
                .build();
        String token = service.generateToken(userDetails);
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("test_user");
        request.setPassword("password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/auth/login", HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(output.getOut().contains("Token JWT expirado"));

    }

    @Test
    void getProducts_TokenSignedWithAnotherSecret_ReturnsUnauthorized(CapturedOutput output) {
        createUser();
        JwtService service = new JwtService(
                "secreto_secreto_prueba_prueba_123456789",
                90000
        );
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test_user")
                .password("password123")
                .authorities("ROLE_USER")
                .build();
        String token = service.generateToken(userDetails);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(output.getOut().contains("Token JWT rechazado"));
    }

    @Test
    void getProducts_ExpiredToken_ReturnsUnauthorized(CapturedOutput output) {
        createUser();
        JwtService service = new JwtService(
                jwtSecret,
                -1000
        );
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test_user")
                .password("password123")
                .authorities("ROLE_USER")
                .build();
        String token = service.generateToken(userDetails);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(output.getOut().contains("Token JWT expirado"));
    }

    @Test
    void getProducts_TokenOfNonExistentUser_ReturnsUnauthorized(CapturedOutput output) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("usuario_inexistente")
                .password("password123")
                .authorities("ROLE_USER")
                .build();
        String token = this.jwtService.generateToken(userDetails);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(output.getOut().contains("Error al encontrar usuario"));
    }
}
