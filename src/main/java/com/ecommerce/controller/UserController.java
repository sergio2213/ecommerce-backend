package com.ecommerce.controller;

import com.ecommerce.dto.UserDTO;
import com.ecommerce.model.User;
import com.ecommerce.service.CartService;
import com.ecommerce.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CartService cartService;

    public UserController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        User savedUser = this.userService.saveUser(user);
        this.cartService.createCartForUser(savedUser);
        return new ResponseEntity<>(userService.convertToUserDTO(savedUser), HttpStatus.CREATED);
    }
}
