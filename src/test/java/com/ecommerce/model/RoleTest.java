package com.ecommerce.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleTest {

    @Test
    void equals_OtherWithoutId_ReturnsFalse() {
        Role r1 = new Role();
        r1.setId(2L);
        Role r2 = new Role();
        assertFalse(r1.equals(r2));
    }

    @Test
    void equals_DifferentInstancesWithSameId_ReturnsTrue() {
        Role r1 = new Role();
        r1.setId(2L);
        Role r2 = new Role();
        r2.setId(2L);
        assertTrue(r1.equals(r2));
    }
}
