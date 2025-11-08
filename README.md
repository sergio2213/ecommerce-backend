# Proyecto: Backend de E-commerce

Este proyecto es el backend de una aplicación de e-commerce desarrollada con Spring Boot. Se encarga de gestionar la creación de usuarios, productos, carritos de compra y el procesamiento de pedidos.

Actualmente cubre un flujo desde la creación del usuario hasta la creación del pedido, todo bajo la seguridad que nos provee Spring Security.


## Tecnologías principales

- **Framework**: Spring Boot 3.5.6 (versión actual)
- **Base de datos**: H2 (en memoria)
- **Persistencia**: Spring Data JPA/Hibernate
- **Seguridad**: Spring Security (Basic Auth)
- **Utilidades**: Lombok (para reducir código repetitivo)

### Funcionalidades cubiertas
- **Productos**: gestión de listado de productos con nombre, descripción, precio y stock.
- **Usuarios**: registro de nuevos usuarios y almacenamiento seguro de la contraseña usando BCrypt.
- **Carritos de compra**: permite que los usuarios autenticados agreguen productos a su carrito y ver su contenido.
- **Pedidos**: implementación de la lógica *checkout* (transaccional). El carrito se convierte en un pedido inmutable.
- **Stock**: el stock de los productos se actualiza automáticamente (disminuye) al finalizar un pedido (checkout).


### A cubrir o realizar
- Implementación de roles y permisos para definir reglas de autorización basadas en roles.
- Creación de endpoints para que el usuario con el rol de ADMIN pueda crear, actualizar y eliminar productos.

Se seguirán añadiendo funcionalidades al proyecto para alcanzar una API de comercio electronico completa.