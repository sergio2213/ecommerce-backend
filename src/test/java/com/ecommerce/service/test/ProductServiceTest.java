package com.ecommerce.service.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ProductService;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Test
    void deleteProduct_Failure_ProductIdNotFound() {

        Long productId = 20L;

        when(this.productRepository.existsById(productId)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.productService.deleteProduct(productId));

        assertTrue(exception.getMessage().contains("Product not found"));

        verify(this.productRepository, never()).deleteById(productId);
    }

    @Test
    void createProduct_Success_MapsAndSaves() {

        Long productId = 25L;

        ProductInputDTO productInputDTO = new ProductInputDTO();
        productInputDTO.setName("Xbox 360");
        productInputDTO.setDescription("Xbox 360 console");
        productInputDTO.setPrice(new BigDecimal("150.00"));
        productInputDTO.setStock(10);

        Product productToSave = new Product();
        productToSave.setName(productInputDTO.getName());
        productToSave.setDescription(productInputDTO.getDescription());
        productToSave.setPrice(productInputDTO.getPrice());
        productToSave.setStock(productInputDTO.getStock());

        Product savedProduct = new Product();
        savedProduct.setName(productInputDTO.getName());
        savedProduct.setPrice(productInputDTO.getPrice());
        savedProduct.setId(productId);
        savedProduct.setStock(productInputDTO.getStock());

        when(this.productMapper.toEntity(productInputDTO)).thenReturn(productToSave);
        when(this.productRepository.save(productToSave)).thenReturn(savedProduct);

        Product result = this.productService.createProduct(productInputDTO);

        verify(this.productMapper, times(1)).toEntity(productInputDTO);

        verify(this.productRepository, times(1)).save(any(Product.class));

        assertNotNull(result);
        assertTrue(result.getId().equals(25L));
    }

    @Test
    void updateProduct_Failure_ProductIdNotFound() {
        Long productId = 50L;

        when(this.productRepository.findById(productId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> this.productService.updateProduct(productId, new ProductInputDTO()));

        assertTrue(exception.getMessage().contains("Product not found"));
        
        verify(this.productMapper, never()).updateEntityFromDto(any(ProductInputDTO.class), any(Product.class));

        verify(this.productRepository, never()).save(any(Product.class));
    }

    // test del método updateProduct con éxito
    @Test
    void updateProduct_Success_MapsAndSaves() {

        Long productId = 60L;

        ProductInputDTO productInputDTO = new ProductInputDTO();
        productInputDTO.setName("Nintendo Switch");
        productInputDTO.setDescription("New description");
        productInputDTO.setPrice(new BigDecimal("300.00"));
        productInputDTO.setStock(15);

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName(productInputDTO.getName());
        existingProduct.setDescription("Old description");
        existingProduct.setPrice(new BigDecimal("280.00"));
        existingProduct.setStock(10);

        Product updatedProduct = new Product();
        updatedProduct.setId(productId);
        updatedProduct.setName(productInputDTO.getName());
        updatedProduct.setDescription(productInputDTO.getDescription());
        updatedProduct.setPrice(productInputDTO.getPrice());
        updatedProduct.setStock(productInputDTO.getStock());

        when(this.productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));

        when(this.productRepository.save(existingProduct)).thenReturn(updatedProduct);

        Product result = this.productService.updateProduct(productId, productInputDTO);

        verify(this.productMapper, times(1)).updateEntityFromDto(any(ProductInputDTO.class), any(Product.class));

        verify(this.productRepository, times(1)).save(any(Product.class));

        assertNotNull(result);
        assertTrue(result.getId().equals(productId));
        assertTrue(result.getDescription().equals(productInputDTO.getDescription()));
        assertTrue(result.getName().equals(productInputDTO.getName()));
        assertTrue(result.getPrice().equals(productInputDTO.getPrice()));
        assertTrue(result.getStock().equals(productInputDTO.getStock()));
    }
}
