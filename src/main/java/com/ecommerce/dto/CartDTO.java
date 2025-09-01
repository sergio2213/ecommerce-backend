package com.ecommerce.dto;

import java.util.List;

import lombok.Data;

@Data
public class CartDTO {
    private Long id;
    private UserDTO user;
    private List<CartItemDTO> cartItems;
}
