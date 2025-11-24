package com.ecommerce.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;

@Mapper(componentModel = "spring", uses = {
    OrderItemMapper.class
})
public interface OrderMapper {

    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(entity.getOrderItems()))")
    OrderDTO toOrderDto(Order entity);

    default BigDecimal calculateTotalAmount(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orderItems.stream()
            .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, (acc, elem) -> acc.add(elem));
    }
}
