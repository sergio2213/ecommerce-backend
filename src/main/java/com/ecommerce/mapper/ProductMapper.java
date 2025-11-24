package com.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductInputDTO dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ProductInputDTO dto, @MappingTarget Product entity);

}
