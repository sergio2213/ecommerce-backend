package com.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.model.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    
    @Mapping(target = "productId", source = "entity.product.id")
    @Mapping(target = "productName", source = "entity.product.name")
    OrderItemDTO toOrderItemDto(OrderItem entity);
}
