package com.ecommerce.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.mapper.OrderMapper;
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
    private OrderMapper orderMapper;
    private OrderItemRepository orderItemRepository;

    public OrderService(UserService userService, CartService cartService, ProductService productService, OrderRepository orderRepository, OrderMapper orderMapper, OrderItemRepository orderItemRepository) {
        this.userService = userService;
        this.cartService = cartService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
    }

    @Transactional
    public Order placeOrder(Long userId) {
        User user = this.userService.getUserById(userId).orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));
        Cart cart = this.cartService.getCartByUserId(userId).orElseThrow(() -> new NoSuchElementException("Cart not found for user id: " + userId));
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot place order with empty cart for user id: " + userId);
        }
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }
        Order savedOrder = this.orderRepository.save(order);
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product id: " + product.getId());
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            this.orderItemRepository.save(orderItem);
            product.setStock(product.getStock() - cartItem.getQuantity());
            this.productService.saveProduct(product);
            savedOrder.getOrderItems().add(orderItem);
        }
        this.cartService.clearCartItems(cart.getId());
        cart.getCartItems().clear();
        return savedOrder; 
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return this.orderRepository.findByUserId(userId);
    }

    public OrderDTO toOrderDto(Order order) {
        return this.orderMapper.toOrderDto(order);
    }

    public List<OrderDTO> getOrdersDTOByUserId(Long userId) {
        return this.orderRepository.findByUserId(userId).stream()
            .map(order -> toOrderDto(order))
            .toList();
    }

    public List<OrderDTO> getOrdersDTOByUsername(String username) {
        return this.orderRepository.findByUserUsernameOrderByOrderDateDesc(username)
            .stream()
            .map(order -> toOrderDto(order))
            .toList();
    }
}
