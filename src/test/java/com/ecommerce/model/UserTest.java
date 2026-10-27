package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    @Test
    void equals_OtherWithoutId_ReturnsFalse() {
        User u1 = new User();
        u1.setId(2L);
        User u2 = new User();
        assertFalse(u1.equals(u2));
    }

    @Test
    void equals_DifferentInstancesWithSameId_ReturnsTrue() {
        User u1 = new User();
        u1.setId(2L);
        User u2 = new User();
        u2.setId(2L);
        assertTrue(u1.equals(u2));
    }
}
