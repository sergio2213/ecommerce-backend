package com.ecommerce.service.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.mapper.CartItemMapper;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartItemMapper cartItemMapper;


    private Cart testCart;
    private Product availableProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        this.testCart = new Cart();
        this.testCart.setId(1L);

        this.availableProduct = new Product();
        this.availableProduct.setId(1L);
        this.availableProduct.setName("Laptop");
        this.availableProduct.setStock(10);

        this.testUser = new User();
        this.testUser.setId(3L);
        this.testUser.setUsername("test_user");
        this.testUser.setEmail("test_user@email.com");
        this.testUser.setPassword("encrypted password");
        this.testUser.setCart(testCart);
    }

    @Test
    void addProductToCart_Success_ExistingItem() {
        int quantity = 5;

        // simulamos las llamadas a los repositorios y servicios
        when(this.cartRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.of(this.testCart));
        when(this.productService.getProductById(this.availableProduct.getId())).thenReturn(Optional.of(availableProduct));
        when(this.cartItemRepository.findByCartIdAndProductId(this.testCart.getId(), this.availableProduct.getId())).thenReturn(Optional.empty()); // no existe el ítem en el carrito

        // simulamos la conversión a DTO
        when(this.cartItemMapper.toCartItemDto(any())).thenReturn(new CartItemDTO());

        // llamamos al método a testear
        Optional<CartItemDTO> result = this.cartService.addProductToCart(this.testUser.getId(), this.availableProduct.getId(), quantity);

        // verificamos el resultado
        assertTrue(result.isPresent());

        // verificamos que se haya llamado al repositorio para guardar el ítem solo una vez
        verify(this.cartItemRepository, times(1)).save(any());
    }

    @Test
    void addProductToCart_Failure_ProductOutOfStock() {
        Product productOutOfStock = new Product();
        productOutOfStock.setId(4L);
        productOutOfStock.setName("Phone");
        productOutOfStock.setStock(0);

        int quantity = 1;

        when(this.cartRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.of(this.testCart));
        when(this.productService.getProductById(productOutOfStock.getId())).thenReturn(Optional.of(productOutOfStock));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.cartService.addProductToCart(this.testUser.getId(), productOutOfStock.getId(), quantity);
        });

        assertTrue(exception.getMessage().contains("out of stock"));

        verify(this.cartItemRepository, never()).save(any());
    }

    @Test
    void addProductToCart_Failure_InsufficientStock() {
        Product productWithInsufficientStock = new Product();
        productWithInsufficientStock.setId(4L);
        productWithInsufficientStock.setName("Phone");
        productWithInsufficientStock.setStock(1);

        int quantity = 3; // mayor que el stock disponible

        when(this.cartRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.of(this.testCart));
        when(this.productService.getProductById(productWithInsufficientStock.getId())).thenReturn(Optional.of(productWithInsufficientStock));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.cartService.addProductToCart(this.testUser.getId(), productWithInsufficientStock.getId(), quantity);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));

        verify(this.cartItemRepository, never()).save(any());
    }

    @Test
    void getCartOrThrow_Success_ReturnsCart() {
        when(this.cartRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.of(this.testCart));
        Cart result = this.cartService.getCartOrThrow(this.testUser.getId());
        assertSame(this.testCart, result);
    }

    @Test
    void getCartOrThrow_Failure_CartNotFound() {
        when(this.cartRepository.findByUserId(this.testUser.getId())).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> this.cartService.getCartOrThrow(this.testUser.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -20})
    void addProductToCart_InvalidQuantity_ThrowsAnException(int quantity) {
        assertThrows(IllegalArgumentException.class, () -> this.cartService
                .addProductToCart(this.testUser.getId(), this.availableProduct.getId(), quantity));
        verify(this.cartRepository, never()).findByUserId(any(Long.class));
    }
}
