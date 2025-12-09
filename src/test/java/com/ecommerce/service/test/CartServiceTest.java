package com.ecommerce.service.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @BeforeEach
    void setUp() {
        this.testCart = new Cart();
        this.testCart.setId(1L);

        this.availableProduct = new Product();
        this.availableProduct.setId(1L);
        this.availableProduct.setName("Laptop");
        this.availableProduct.setStock(10);
    }

    @Test
    void addProductToCart_Success_ExistingItem() {
        Long cartId = 1L;
        Long productId = 10L;
        Integer quantity = 5;

        // simulamos las llamadas a los repositorios y servicios
        when(this.cartRepository.findById(cartId)).thenReturn(Optional.of(this.testCart));
        when(this.productService.getProductById(productId)).thenReturn(Optional.of(availableProduct));
        when(this.cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty()); // no existe el ítem en el carrito
        
        // simulamos la conversión a DTO
        when(this.cartItemMapper.toCartItemDto(any())).thenReturn(new CartItemDTO());

        // llamamos al método a testear
        Optional<CartItemDTO> result = this.cartService.addProductToCart(cartId, productId, quantity);

        // verificamos el resultado
        assertTrue(result.isPresent());

        // verificamos que se haya llamado al repositorio para guardar el ítem solo una vez
        verify(this.cartItemRepository, times(1)).save(any());
    }

    @Test
    void addProductToCart_Failure_ProductOutOfStock() {
        Product productOutOfStock = new Product();
        productOutOfStock.setId(2L);
        productOutOfStock.setName("Phone");
        productOutOfStock.setStock(0);

        Long cartId = 1L;
        Long productId = 2L;
        Integer quantity = 1;

        when(this.cartRepository.findById(cartId)).thenReturn(Optional.of(this.testCart));
        when(this.productService.getProductById(productId)).thenReturn(Optional.of(productOutOfStock));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.cartService.addProductToCart(cartId, productId, quantity);
        });

        assertTrue(exception.getMessage().contains("out of stock"));

        verify(this.cartItemRepository, never()).save(any());
    }

    @Test
    void addProductToCart_Failure_InsufficientStock() {
        Integer quantity = 15; // mayor que el stock disponible
        Long cartId = 3L;
        Long productId = 1L;

        when(this.cartRepository.findById(cartId)).thenReturn(Optional.of(this.testCart));
        when(this.productService.getProductById(productId)).thenReturn(Optional.of(availableProduct));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.cartService.addProductToCart(cartId, productId, quantity);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));

        verify(this.cartItemRepository, never()).save(any());
    }
}
