package com.ecommerce.controller.test;

import com.ecommerce.controller.OrderController;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @InjectMocks
    private OrderController orderController;

    @Mock
    private UserService userService;

    @Mock
    private OrderService orderService;

    @Test
    void placeOrder_UserNotFound_ThrowsUserNotFoundException() {
        Principal principal = () -> "usuario_inexistente";
        when(this.userService.getUserByUsername("usuario_inexistente")).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> this.orderController.placeOrder(principal)
        );
        assertTrue(exception.getMessage().contains("User not found"));
        verify(this.orderService, never()).placeOrder(any());

    }
}
