package com.ecommerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api/orders")
@Tag(
    name = "Pedidos",
    description = "Gestión y consulta de los pedidos realizados por el usuario autenticado"
)
public class OrderController {

    private UserService userService;
    private OrderService orderService;

    public OrderController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }
    
    @Operation(
        summary = "Finalizar la compra (checkout) del carrito del usuario autenticado",
        description = "Crea un nuevo pedido a partir del carrito actual. Proceso transaccional que actualiza el stock y limpia el carrito."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Fallo de lógica de negocio (ej. carrito vacío, stock insuficiente)."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> placeOrder(Principal principal) {
        Long userId = this.userService.getUserByUsername(principal.getName())
            .orElseThrow(() -> new NoSuchElementException("User not found")).getId();
        Order newOrder = this.orderService.placeOrder(userId);
        OrderDTO newOrderDTO = this.orderService.toOrderDto(newOrder);
        return new ResponseEntity<>(newOrderDTO, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Obtener los pedidos realizados por el usuario autenticado",
        description = "Devuelve una lista de todos los pedidos que ha realizado el usuario actualmente autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrdersByCurrentUser(Principal principal) {
        Long userId = this.userService.getUserByUsername(principal.getName())
            .orElseThrow(() -> new NoSuchElementException("User not found")).getId();
        List<OrderDTO> orders = this.orderService.getOrdersDTOByUserId(userId);
        return ResponseEntity.ok(orders);
    }
    
}
