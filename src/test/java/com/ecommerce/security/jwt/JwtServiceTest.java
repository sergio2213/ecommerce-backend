package com.ecommerce.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtServiceTest {

    private static final String VALID_SECRET = "secreto_secreto_prueba_prueba_123456789";

    private static final String OTHER_SECRET = "secreto_secreto_prueba_prueba_dos_dos_123456789";

    private UserDetails testUser() {
        return testUser("test_user");
    }

    private UserDetails testUser(String username) {
        return User
                .withUsername(username)
                .password("test_password")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generateToken_ValidUser_TokenIsValidForThatUser() {
        JwtService jwtService = new JwtService(
                VALID_SECRET,
                86400000
        );
        UserDetails userDetails = testUser();
        String token = jwtService.generateToken(userDetails);

        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertEquals("test_user", jwtService.extractUsername(token));
    }

    @Test
    void extractUsername_TokenSignedWithAnotherSecret_ThrowsException() {
        JwtService jwtService1 = new JwtService(
                VALID_SECRET,
                86400000
        );
        JwtService jwtService2 = new JwtService(
                OTHER_SECRET,
                86400000
        );
        UserDetails userDetails = testUser();
        String token = jwtService1.generateToken(userDetails);

        assertThrows(SignatureException.class, () -> jwtService2.extractUsername(token));
    }

    @Test
    void constructor_SecretTooShort_ThrowsException() {
        assertThrows(WeakKeyException.class, () -> new JwtService(
                "secreto_corto",
                86400000
        ));
    }

    @Test
    void generateToken_ConfiguredExpiration_ExpiresAtExpectedTime() {
        JwtService jwtService = new JwtService(
                VALID_SECRET,
                5000
        );
        UserDetails userDetails = testUser();
        String token = jwtService.generateToken(userDetails);
        Claims claims = jwtService.extractAllClaims(token);
        long actual = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(5000, actual, 500);
    }

    @Test
    void isTokenExpired_NegativeExpiration_ThrowsExpiredJwtException() {
        JwtService jwtService = new JwtService(
                VALID_SECRET,
                -1000
        );
        UserDetails userDetails = testUser();
        String token = jwtService.generateToken(userDetails);
        assertThrows(ExpiredJwtException.class, () -> jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenValid_TokenForAnotherUser_ReturnsFalse() {
        JwtService jwtService = new JwtService(
                VALID_SECRET,
                90000
        );
        UserDetails userDetails1 = testUser("user1");
        UserDetails userDetails2 = testUser("user2");
        String tokenUser1 = jwtService.generateToken(userDetails1);
        assertFalse(jwtService.isTokenValid(tokenUser1, userDetails2));
    }
}
