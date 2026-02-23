package com.ecommerce.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.AuthRequestDTO;
import com.ecommerce.dto.AuthResponseDTO;
import com.ecommerce.dto.RegisterRequestDTO;
import com.ecommerce.dto.UserDTO;
import com.ecommerce.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
        summary = "Autenticar un usuario",
        description = "Genera un token JWT para el usuario autenticado. Requiere username y password."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa, token JWT generado"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(this.authService.login(request));
    }

    @Operation(
        summary = "Registrar un nuevo usuario",
        description = "Crea un nuevo usuario con rol ROLE_USER y un carrito asociado. No requiere autenticación."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return new ResponseEntity<>(this.authService.register(request), HttpStatus.CREATED);
    }
}
