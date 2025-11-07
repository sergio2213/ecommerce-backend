package com.ecommerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private UserService userService;
    private OrderService orderService;

    public OrderController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }
    
    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> placeOrder(Principal principal) {
        Long userId = this.userService.getUserByUsername(principal.getName())
            .orElseThrow(() -> new NoSuchElementException("User not found")).getId();
        Order newOrder = this.orderService.placeOrder(userId);
        OrderDTO newOrderDTO = this.orderService.convertToOrderDTO(newOrder);
        return new ResponseEntity<>(newOrderDTO, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrdersByCurrentUser(Principal principal) {
        Long userId = this.userService.getUserByUsername(principal.getName())
            .orElseThrow(() -> new NoSuchElementException("User not found")).getId();
        List<OrderDTO> orders = this.orderService.getOrdersDTOByUserId(userId);
        return ResponseEntity.ok(orders);
    }
    
}
