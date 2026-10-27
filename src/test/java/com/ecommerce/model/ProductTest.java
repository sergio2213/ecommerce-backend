package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductTest {

    @Test
    void equals_SameInstance_ReturnsTrue() {
        Product product = new Product();
        product.setId(1L);
        assertTrue(product.equals(product));
    }

    @Test
    void equals_DifferentInstancesWithSameId_ReturnsTrue() {
        Product p1 = new Product();
        p1.setId(2L);
        Product p2 = new Product();
        p2.setId(2L);
        assertTrue(p1.equals(p2));
    }

    @Test
    void equals_DifferentIds_ReturnsFalse() {
        Product p1 = new Product();
        p1.setId(2L);
        Product p2 = new Product();
        p2.setId(3L);
        assertFalse(p1.equals(p2));
    }

    @Test
    void equals_ThisWithoutId_ReturnsFalse() {
        Product p1 = new Product();
        Product p2 = new Product();
        p2.setId(4L);
        assertFalse(p1.equals(p2));
    }

    @Test
    void equals_BothWithoutId_ReturnsFalse() {
        Product p1 = new Product();
        Product p2 = new Product();
        assertFalse(p1.equals(p2));
    }

    @Test
    void equals_Null_ReturnsFalse() {
        Product product = new Product();
        assertFalse(product.equals(null));
    }

    @Test
    void equals_DifferentType_ReturnsFalse() {
        Product product = new Product();
        String string = "string";
        assertFalse(product.equals(string));
    }

    @Test
    void equals_OtherWithoutId_ReturnsFalse() {
        Product p1 = new Product();
        p1.setId(2L);
        Product p2 = new Product();
        assertFalse(p1.equals(p2));
    }

    @Test
    void hashCode_EqualProducts_ReturnSameHash() {
        Product p1 = new Product();
        p1.setId(6L);
        Product p2 = new Product();
        p2.setId(6L);
        assertTrue(p1.equals(p2));
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
