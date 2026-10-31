package com.ecommerce.integration.test;

import com.ecommerce.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Test
    void register_RoleUserMissing_ReturnsInternalServerError() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("test_user");
        request.setPassword("password123");
        request.setEmail("test_user@test.com");
        ResponseEntity<String> response = this.restTemplate
                .exchange("/api/auth/register", HttpMethod.POST, new HttpEntity<>(request), String.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("An unexpected error occurred"));
    }
}
