package com.ecommerce.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(
    name = "Usuarios",
    description = "Gestión de perfil y datos del usuario autenticado"
)
public class UserController {

}
