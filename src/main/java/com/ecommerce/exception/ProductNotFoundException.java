package com.ecommerce.exception;

public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}
