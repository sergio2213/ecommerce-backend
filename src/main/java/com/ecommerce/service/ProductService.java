package com.ecommerce.service;

import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

    public Product updateProduct(Long id, ProductInputDTO dto) {
        Product existingProduct = this.productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found with ID: " + id));
        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setStock(dto.getStock());
        return this.productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        if (!this.productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found with ID: " + id);
        }
        this.productRepository.deleteById(id);
    }

    public Product convertoToEntity(ProductInputDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        return product;
    }
}
