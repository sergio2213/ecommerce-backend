# Proyecto: Backend de E-commerce

Este proyecto es el backend de una aplicación de e-commerce desarrollada con Spring Boot. Se encarga de gestionar la creación de usuarios, productos, carritos de compra y el procesamiento de pedidos.

Cubre el flujo completo desde el registro del usuario hasta la creación del pedido, con autenticación mediante tokens JWT y autorización basada en roles (`ROLE_USER` y `ROLE_ADMIN`) con Spring Security.


## Tecnologías principales

- **Lenguaje**: Java 17
- **Framework**: Spring Boot 3.5.11
- **Base de datos**: H2 (en memoria)
- **Persistencia**: Spring Data JPA/Hibernate
- **Seguridad**: Spring Security con autenticación JWT
- **Mapeo de objetos**: MapStruct
- **Documentación de la API**: springdoc-openapi (Swagger UI)
- **Utilidades**: Lombok (para reducir código repetitivo)
- **Pruebas**: JUnit 5 y Mockito (unitarias), `TestRestTemplate` (integración)


## Funcionalidades cubiertas

- **Autenticación**: registro e inicio de sesión (`/api/auth`). El login devuelve un token JWT que se envía en la cabecera `Authorization: Bearer <token>`. Las contraseñas se almacenan con BCrypt.
- **Productos**: listado y consulta para usuarios autenticados. Alta, actualización y baja reservadas al rol `ADMIN` (`/api/products/admin/**`).
- **Carritos de compra**: los usuarios autenticados agregan productos a su carrito y consultan su contenido. Se valida que la cantidad sea positiva y que no se supere el stock disponible.
- **Pedidos**: lógica de *checkout* transaccional. El carrito se convierte en un pedido inmutable.
- **Stock**: el stock de los productos disminuye automáticamente al finalizar un pedido.
- **Roles iniciales**: al arrancar, la aplicación crea los roles `ROLE_USER` y `ROLE_ADMIN` si no existen.


## Requisitos

- JDK 17
- Maven (se incluye el wrapper `./mvnw`, no hace falta instalarlo)


## Configuración

### Variable de entorno `JWT_SECRET` (obligatoria)

Los tokens JWT se firman con un secreto que **no está en el código ni en el repositorio**: la aplicación lo lee de la variable de entorno `JWT_SECRET`. **Si la variable no está definida, la aplicación no arranca** (falla con `Could not resolve placeholder 'JWT_SECRET'`).

Requisitos del valor:

- Al menos 32 caracteres (HMAC-SHA256 rechaza claves más cortas).
- Debe ser aleatorio y privado. No lo subas al repositorio ni lo compartas.

Para generar uno:

```bash
openssl rand -base64 48
```

Definirla en la terminal (macOS/Linux) antes de arrancar:

```bash
export JWT_SECRET="<tu-secreto-generado>"
```

Definirla en IntelliJ IDEA: `Run > Edit Configurations...`, seleccionar `EcommerceApplication`, `Modify options > Environment variables` y cargar `JWT_SECRET=<tu-secreto>`. Dejá desmarcado *Store as project file*, para que el valor quede solo en tu máquina.

La variable cargada en IntelliJ solo aplica a las ejecuciones lanzadas desde IntelliJ. Si arrancás por terminal con ./mvnw spring-boot:run, definila con export en esa misma terminal

### Expiración del token

La duración del token se configura con `jwt.expiration` (en milisegundos) en `application.properties`. Por defecto son 24 horas (`86400000`).


## Cómo ejecutar

```bash
export JWT_SECRET="<tu-secreto>"
./mvnw spring-boot:run
```

Con la aplicación en marcha:

- Consola de H2: `http://localhost:8080/h2-console` (solo para desarrollo).
- Swagger UI: `http://localhost:8080/swagger-ui.html` (requiere un usuario con rol `ADMIN`).


## Pruebas

```bash
./mvnw test
```

No hace falta definir `JWT_SECRET` para las pruebas: usan el perfil `test`, que trae un secreto de prueba en `src/test/resources/application-test.properties`.

- **Unitarias**: lógica de servicios (con Mockito) y `JwtService`.
- **De integración**: levantan el contexto completo de Spring con una base H2 en memoria. Heredan de `BaseIntegrationTest`, que centraliza la configuración y la limpieza de la base entre pruebas.


## A cubrir o realizar

- Respuestas con DTO y paginación en el listado de productos.
- Excepciones propias en lugar de reutilizar `IllegalArgumentException`.
- Base de datos persistente (PostgreSQL) con migraciones (Flyway) y pruebas con Testcontainers.
- Integración continua con GitHub Actions.

Se seguirán añadiendo funcionalidades al proyecto para alcanzar una API de comercio electrónico completa.
