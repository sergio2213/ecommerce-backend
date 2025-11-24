package com.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.dto.CartDTO;
import com.ecommerce.model.Cart;

@Mapper(componentModel = "spring", uses = {
    UserMapper.class,
    CartItemMapper.class
})
public interface CartMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "cartItems", source = "cartItems")
    CartDTO toCartDto(Cart entity);

}
