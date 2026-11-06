package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public List<ProductDTO> getAllProducts() {
        return this.productRepository.findAll().stream()
                .map(this.productMapper::toDto).toList();
    }

    public Optional<Product> getProductById(Long id) {
        return this.productRepository.findById(id);
    }

    public Optional<ProductDTO> getProductDTOById(Long id) {
        return getProductById(id).map(this.productMapper::toDto);
    }

    public void deleteProduct(Long id) {
        if (!this.productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product not found with ID: " + id);
        }
        this.productRepository.deleteById(id);
    }

    public ProductDTO createProduct(ProductInputDTO dto) {
        Product newProduct = this.productMapper.toEntity(dto);
        Product savedProduct = this.productRepository.save(newProduct);
        return this.productMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long productId, ProductInputDTO dto) {
        Product existingProduct = this.productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));
        this.productMapper.updateEntityFromDto(dto, existingProduct);
        Product savedProduct = this.productRepository.save(existingProduct);
        return this.productMapper.toDto(savedProduct);
    }
}
