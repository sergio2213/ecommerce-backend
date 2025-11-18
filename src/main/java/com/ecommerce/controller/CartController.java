package com.ecommerce.controller;

import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/carts")
@Tag(
    name = "Carrito",
    description = "Gestión del carrito de compra del usuario autenticado"
)
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @Operation(
        summary = "Añadir un producto al carrito",
        description = "Añade una cantidad específica de un producto al carrito del usuario autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto añadido al carrito exitosamente."),
        @ApiResponse(responseCode = "400", description = "Fallo de lógica de negocio (ej. producto no existe, stock insuficiente)."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/add-product")
    public ResponseEntity<CartItemDTO> addProductToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, Principal principal) {
        Long userId = this.userService.getUserByUsername(principal.getName()).orElseThrow(() -> new NoSuchElementException("User not found")).getId();
        Optional<CartItemDTO> result = this.cartService.addProductToCart(userId, productId, quantity);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.internalServerError().build());
    }

    @Operation(
        summary = "Obtener el carrito del usuario autenticado",
        description = "Devuelve el contenido del carrito (incluyendo ítems y totales) del usuario actualmente autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito obtenido exitosamente."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado para el usuario."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public ResponseEntity<CartDTO> getCartByUserId(Principal principal) {
        Optional<Long> userIdOptional = this.userService.getUserByUsername(principal.getName()).map(user -> user.getId());
        if(userIdOptional.isPresent()) {
            Long userId = userIdOptional.get();
            return this.cartService.getCartDTOByUserId(userId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }
        return ResponseEntity.notFound().build();
    }
}
