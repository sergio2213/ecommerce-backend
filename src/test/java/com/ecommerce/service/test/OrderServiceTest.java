package com.ecommerce.service.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.ecommerce.service.UserService;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    
    @InjectMocks
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductService productService;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        this.testUser = new User();
        testUser.setId(1L);
        
        this.testCart = new Cart();
        testCart.setId(2L);
        testCart.setUser(testUser);
    }

    @Test
    // se deben realizar todas las operaciones necesarias para colocar una orden correctamente
    void placeOrder_Success_ClearsCartAndUpdatesStock() {

        // creamos algunos productos
        Product product1 = new Product(300L, "iPad", "Apple Tablet", new BigDecimal(300.00), 20);
        Product product2 =  new Product(301L, "Keyboard", "Teclado Logitech", new BigDecimal(200.00), 10);

        // creamos algunos ítems
        CartItem cartItem1 = new CartItem(20L, this.testCart, product1, 1);
        CartItem cartItem2 = new CartItem(21L, this.testCart, product2, 5);

        List<CartItem> items = new ArrayList<>();
        items.add(cartItem1);
        items.add(cartItem2);
        this.testCart.setCartItems(items);

        when(this.userService.getUserById(this.testUser.getId())).thenReturn(Optional.of(this.testUser));
        when(this.cartService.getCartByUserId(this.testUser.getId())).thenReturn(Optional.of(this.testCart));
        // mockeamos el guardado de la orden
        when(this.orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(300L); // simulamos que genera un ID
            return savedOrder;
        });

        // mockeamos el guardado del item, de los productos y la limpieza
        when(this.orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.productService.saveProduct(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(this.cartService).clearCartItems(this.testCart.getId());

        Order resultOrder = this.orderService.placeOrder(this.testUser.getId());

        assertNotNull(resultOrder.getId(), "La ordern debe tener un ID asignado.");
        assertEquals(2, resultOrder.getOrderItems().size(), "La orden debe tener 2 ítems.");

        // guardado inicial de la orden (antes de entrar al bucle)
        verify(this.orderRepository, times(1)).save(any(Order.class));

        // guardado de los 2 ítems de la orden (dentro del bucle)
        verify(this.orderItemRepository, times(2)).save(any(OrderItem.class));

        // actualización de stock de los 2 productos
        verify(this.productService, times(1)).saveProduct(argThat(
            p -> p.getId().equals(300L) && p.getStock() == (20 - 1)
        ));
        verify(this.productService, times(1)).saveProduct(argThat(
            p -> p.getId().equals(301L) && p.getStock() == (10 - 5)
        ));

        // limpieza del carrito
        verify(this.cartService, times(1)).clearCartItems(this.testCart.getId());
    }

    @Test
    // debe realizarse el rollback si hay stock insuficiente
    void placeOrder_Failure_InsufficientStock_RollsBack() {

        Product product1 = new Product();
        product1.setId(5L);
        product1.setName("Play Station 3");
        product1.setPrice(new BigDecimal(200.00));
        product1.setStock(1); // stock: 1

        CartItem cartItem1 = new CartItem();
        cartItem1.setId(10L);
        cartItem1.setProduct(product1);
        cartItem1.setQuantity(3); // cantidad: 3 (stock insuficiente)
        
        List<CartItem> items = new ArrayList<>();
        items.add(cartItem1);
        this.testCart.setCartItems(items);

        when(this.userService.getUserById(this.testUser.getId())).thenReturn(Optional.of(this.testUser));
        
        when(this.cartService.getCartByUserId(this.testUser.getId())).thenReturn(Optional.of(this.testCart));

        when(this.orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(200L);
            return savedOrder;
        });

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> this.orderService.placeOrder(this.testUser.getId()));

        assertTrue(exception.getMessage().contains("Insufficient stock"));

        // controlar que el rollback se haya realizado
        verify(this.orderRepository, times(1)).save(any(Order.class)); // se llama solo una vez, aunque por la anotación @Transactional se revierte

        verify(this.orderItemRepository, never()).save(any());

        verify(this.productService, never()).saveProduct(any());

        verify(this.cartService, never()).clearCartItems(anyLong());
    }

}
