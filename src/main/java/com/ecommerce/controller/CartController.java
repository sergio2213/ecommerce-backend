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

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    @PostMapping("/add-product")
    public ResponseEntity<CartItemDTO> addProductToCart(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity, Principal principal) {
        Long userId = this.userService.getUserByUsername(principal.getName()).orElseThrow(() -> new NoSuchElementException("User not found")).getId();
        Optional<CartItemDTO> result = this.cartService.addProductToCart(userId, productId, quantity);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.internalServerError().build());
    }

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
