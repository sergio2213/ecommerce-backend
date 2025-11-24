package com.ecommerce.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.dto.UserDTO;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "roles", target = "roles")
    UserDTO toUserDto(User entity);

    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(role -> role.getName()).collect(Collectors.toSet());
    }

}
