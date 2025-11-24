package com.ecommerce.controller;

import com.ecommerce.dto.ProductInputDTO;
import com.ecommerce.model.Product;
import com.ecommerce.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(
    name = "Productos",
    description = "Gestión de inventario y listado de productos"
)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // endpoint de administración
    @Operation(
        summary = "Crear un nuevo producto (requiere admin)",
        description = "Permite al administrador agregar un nuevo artículo al inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. nombre vacío)."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "403", description = "Prohibido. Usuario autenticado no tiene rol ADMIN."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PostMapping("/admin/new")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductInputDTO productInputDTO) {
        Product savedProduct = this.productService.createProduct(productInputDTO);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Obtener todos los productos",
        description = "Devuelve una lista de todos los productos disponibles en el inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = this.productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @Operation(
        summary = "Obtener un producto por ID",
        description = "Devuelve los detalles de un producto específico dado su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado y devuelto exitosamente."),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado con el ID proporcionado."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = this.productService.getProductById(id);
        return product.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // endpoint de administración
    @Operation(
        summary = "Actualizar un producto existente (requiere admin)",
        description = "Permite al administrador modificar los detalles de un producto en el inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente."),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (ej. nombre vacío)."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "403", description = "Prohibido. Usuario autenticado no tiene rol ADMIN."),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado con el ID proporcionado."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @PutMapping("/admin/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductInputDTO dto) {
        Product updatedProduct = this.productService.updateProduct(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    // endpoint de administración
    @Operation(
        summary = "Eliminar un producto (requiere admin)",
        description = "Permite al administrador eliminar un producto del inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente."),
        @ApiResponse(responseCode = "401", description = "No autenticado. Faltan credenciales."),
        @ApiResponse(responseCode = "403", description = "Prohibido. Usuario autenticado no tiene rol ADMIN."),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado con el ID proporcionado."),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor.")
    })
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id) {
        this.productService.deleteProduct(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
