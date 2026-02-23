package com.ecommerce.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.ecommerce.dto.AuthRequestDTO;
import com.ecommerce.dto.AuthResponseDTO;
import com.ecommerce.dto.RegisterRequestDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.model.User;
import com.ecommerce.security.JpaUserDetailsService;
import com.ecommerce.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JpaUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;
    private final CartService cartService;

    public AuthResponseDTO login(AuthRequestDTO request) {
        this.authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails user = this.userDetailsService.loadUserByUsername(request.getUsername());
        String token = this.jwtService.generateToken(user);
        return AuthResponseDTO.builder()
            .token(token)
            .build();
    }

    public UserDTO register(RegisterRequestDTO request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        User savedUser = this.userService.saveUser(user);
        this.cartService.createCartForUser(savedUser);
        return this.userService.toUserDto(savedUser);
    }
}
