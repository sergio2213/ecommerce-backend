package com.ecommerce.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private UserService userService;
    private CartService cartService;
    private ProductService productService;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;

    public OrderService(UserService userService, CartService cartService, ProductService productService, OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public Order placeOrder(Long userId) {
        // obtener el usuario
        User user = this.userService.getUserById(userId).orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        // obtener el carrito del usuario
        Cart cart = this.cartService.getCartByUserId(userId).orElseThrow(() -> new NoSuchElementException("Cart not found for user id: " + userId));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart for user id: " + userId);
        }

        // crear la order
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }
        // no debería estar acá
        Order savedOrder = this.orderRepository.save(order);
        // crear los items de la order a partir de los items del carrito, actualizar el stock y limpiar el carrito
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            // verificar stock
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product id: " + product.getId());
            }
            // crear OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            this.orderItemRepository.save(orderItem);
            // actualizar stock del producto
            product.setStock(product.getStock() - cartItem.getQuantity());
            this.productService.saveProduct(product);
            // guardar el orderItem en la lista
            savedOrder.getOrderItems().add(orderItem);
        }
        // limpiar el carrito
        this.cartService.clearCartItems(cart.getId());
        // sincronizar el carrito en memoria
        cart.getCartItems().clear();
        return savedOrder; 
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return this.orderRepository.findByUserId(userId);
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        return dto;
    }

    public OrderDTO convertToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        List<OrderItemDTO> itemsDTO = order.getOrderItems().stream()
            .map(item -> convertToOrderItemDTO(item))
            .toList();
        dto.setItems(itemsDTO);
        BigDecimal totalAmount = order.getOrderItems().stream()
            .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, (acc, elem) -> acc.add(elem));
        dto.setTotalAmount(totalAmount);
        return dto;
    }

    public List<OrderDTO> getOrdersDTOByUserId(Long userId) {
        return this.orderRepository.findByUserId(userId).stream()
            .map(order -> convertToOrderDTO(order))
            .toList();
    }
}
