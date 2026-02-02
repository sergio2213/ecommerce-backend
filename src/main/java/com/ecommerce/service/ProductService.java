package com.ecommerce.service;

import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public Product saveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return this.productRepository.findById(id);
    }

    public void deleteProduct(Long id) {
        if (!this.productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found with ID: " + id);
        }
        this.productRepository.deleteById(id);
    }

    public Product createProduct(ProductInputDTO dto) {
        Product newProduct = this.productMapper.toEntity(dto);
        return this.productRepository.save(newProduct);
    }

    @Transactional
    public Product updateProduct(Long productId, ProductInputDTO dto) {
        Product existingProduct = this.productRepository.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + productId));
        this.productMapper.updateEntityFromDto(dto, existingProduct);
        return this.productRepository.save(existingProduct);
    }
}
