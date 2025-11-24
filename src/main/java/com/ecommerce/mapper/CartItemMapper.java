package com.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.model.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productId", source = "entity.product.id")
    @Mapping(target = "productName", source = "entity.product.name")
    @Mapping(target = "productPrice", source = "entity.product.price")
    CartItemDTO toCartItemDto(CartItem entity);
}
