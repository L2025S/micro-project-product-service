# Product Service

The product service is the part of the webshop backend that owns **products and categories**. It stores them in a PostgreSQL database and exposes a REST API for reading, creating, updating and deleting them, and for changing stock levels.

## Contents

- [Responsibilities](#responsibilities)
- [System overview](#system-overview)
- [Technologies](#technologies)
- [Database](#database)
- [Port](#port)
- [Environment variables](#environment-variables)
- [Running the application locally](#running-the-application-locally)
- [API and access rules](#api-and-access-rules)
- [Authentication](#authentication)
- [Project structure](#project-structure)
- [Tests](#tests)
- [Docker and CI](#docker-and-ci)
- [Other repositories](#other-repositories)

## Responsibilities

The product service is responsible for:

1. **Products.** Name, description, image link, price, stock level and category. Each product has a numeric `id` and a public `uuid`.
2. **Categories.** Product categories with unique names.
3. **Stock handling.** Increasing and reducing stock, including a batch endpoint the order service uses when an order is placed.
4. **Protecting its own endpoints.** It validates the JWT it receives and checks the user's role (`USER` or `ADMIN`).

It does **not** handle users or login (auth service) and does not handle orders (order service).

Business rules enforced by the domain model:

- A price must be greater than zero.
- Stock can never be negative, and stock cannot be reduced below zero (an "insufficient stock" error is returned).
- Category names must not be blank.

## System overview

```
+----------+     +---------+     +--------------------+
| Frontend |---->| Gateway |---->|  Product service   |  :8081
|  :5173   |     |  :8090  |     +----------+---------+
+----------+     +----+----+                |
                      |                     | JWT keys (JWKS)
                      |   +-----------------v--+
                      +-->|    Auth service    |  :8080
                      |   +--------------------+
                      |   +--------------------+
                      +-->|   Order service    |  :8082
                          +----------+---------+
                                     |
                                     +--> calls Product service
                                          (stock decrease)
```

The frontend should call the **gateway**, not this service directly. The gateway forwards product and category requests here. The ports are the default values used for local development.

## Technologies

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Build tool | Maven (Maven Wrapper: `mvnw` / `mvnw.cmd`) |
| Web | Spring Web MVC |
| Database access | Spring Data JPA (Hibernate) |
| Database | PostgreSQL (H2 in memory for tests) |
| Security | Spring Security, OAuth2 Resource Server (JWT) |
| Validation | Bean Validation (`spring-boot-starter-validation`) |
| Object mapping | MapStruct 1.6.3 |
| Boilerplate reduction | Lombok |
| API documentation | springdoc OpenAPI 3.0.2 (Swagger UI) |
| Tests | JUnit, Spring Boot test support, Spring Security test |
| Container | Docker (multi-stage build) |
| CI | GitHub Actions |

## Database

The service uses a **PostgreSQL** database. The connection is configured with environment variables (see below) and has no default, so the service will not start without them.

- **Tables:** `products` and `categories`. A product belongs to at most one category.
- **Schema handling:** `spring.jpa.hibernate.ddl-auto=update`. Hibernate creates missing tables and columns on startup, so no manual migration is needed.
- **Sample data (optional):** `sql/init.sql.txt` creates a `products` table and inserts 10 sample products. It does not include the `image_url` and `category_id` columns or the `categories` table. Hibernate adds those on startup.
    - If the tables **do not exist yet**, you can run the whole file, then start the application.
    - If the application has **already created the tables**, run only the `INSERT` statements, because `CREATE TABLE` would fail.

A database in the cloud works as well as a local one, as long as you provide its JDBC URL, user name and password.

## Port

The service listens on port **8081** by default. The value is controlled by the `PORT` environment variable (hosting platforms such as Render set it automatically).

## Environment variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `PRODUCT_DB_URL` | none | **Yes** | JDBC URL of the PostgreSQL database, for example `jdbc:postgresql://localhost:5432/productdb` |
| `PRODUCT_DB_USERNAME` | none | **Yes** | Database user name |
| `PRODUCT_DB_PASSWORD` | none | **Yes** | Database password |
| `AUTH_SERVER_URL` | `http://localhost:8080` | No | Address of the auth service. The service fetches the public JWT keys from `<AUTH_SERVER_URL>/auth/jwks` |
| `PORT` | `8081` | No | Port the service listens on |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://localhost:5174,http://localhost:5175` | No | Comma-separated list of browser origins allowed to call this service directly |

Never commit real passwords to the repository. Set them in your environment, in IntelliJ run configurations, or in your hosting platform's settings.

## Running the application locally

### Prerequisites

- Java 21 (JDK)
- A running PostgreSQL database and its connection details
- The auth service running (default `http://localhost:8080`), so that tokens can be validated

### Start the application

Set the database variables, then run the command in the project's root folder.

macOS and Linux:

```bash
export PRODUCT_DB_URL=jdbc:postgresql://localhost:5432/productdb
export PRODUCT_DB_USERNAME=your_user
export PRODUCT_DB_PASSWORD=your_password

./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
$env:PRODUCT_DB_URL="jdbc:postgresql://localhost:5432/productdb"
$env:PRODUCT_DB_USERNAME="your_user"
$env:PRODUCT_DB_PASSWORD="your_password"

mvnw.cmd spring-boot:run
```

The service is now available at `http://localhost:8081`.

In IntelliJ you can instead add the variables to the run configuration (Run → Edit Configurations → Environment variables).

### Check that it works

- Open the Swagger UI: `http://localhost:8081/swagger-ui.html`
- Or call a public endpoint: `http://localhost:8081/categories/all`

### Recommended start order for the whole system

1. Database
2. Auth service (port 8080)
3. **Product service** (port 8081)
4. Order service (port 8082)
5. Gateway (port 8090)
6. Frontend (port 5173): `npm install` followed by `npm run dev`

## API and access rules

Interactive documentation is available at `/swagger-ui.html` (OpenAPI JSON at `/v3/api-docs`). Both are public.

**Products**

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/products/all` | Public | All products |
| GET | `/products/all-paged` | Public | Products with pagination (`page`, `size`, `sort`) |
| GET | `/products/{id}` | Public | One product by numeric id |
| GET | `/products/uuid/{uuid}` | Public | One product by UUID |
| GET | `/products/category/{categoryId}` | Public | Products in a category |
| POST | `/products/new` | `ADMIN` | Create a product |
| PATCH | `/products/{uuid}/basic-info` | `ADMIN` | Update name, description and price (query parameters `name`, `description`, `price`) |
| PATCH | `/products/{uuid}/image` | `ADMIN` | Update the image link (`imageUrl`) |
| PATCH | `/products/{uuid}/category` | `ADMIN` | Change category (`categoryId`) |
| PATCH | `/products/{uuid}/stock/reduce` | `USER`, `ADMIN` | Reduce stock (`quantity`) |
| PATCH | `/products/{uuid}/stock/increase` | `USER`, `ADMIN` | Increase stock (`quantity`) |
| POST | `/products/stock/decrease` | `USER`, `ADMIN` | Reduce stock for several products at once (used by the order service) |
| DELETE | `/products/{id}` | `ADMIN` | Delete a product by id |
| DELETE | `/products/uuid/{uuid}` | `ADMIN` | Delete a product by UUID |

**Categories**

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/categories/all` | Public | All categories |
| POST | `/categories/new` | `ADMIN` | Create a category |

**Other**

| Method | Path | Access | Description |
|---|---|---|---|
| GET | `/test` | Logged in | Returns the user from the JWT, useful for checking authentication |

Any endpoint not listed above requires a valid login.

## Authentication

1. The service is an OAuth2 **resource server**. It does not issue tokens. The auth service does.
2. It verifies each JWT's signature with the public keys from `<AUTH_SERVER_URL>/auth/jwks`, and also checks that the token has not expired and was issued by `AUTH_SERVER_URL`.
3. The token is read from the `Authorization: Bearer <token>` header. If there is no such header, it is read from the `accessToken` cookie. Empty cookies are ignored, so the request is treated as anonymous.
4. Roles come from the `roles` claim in the JWT. They are used by rules such as `hasRole("ADMIN")`.
5. Sessions are not used (stateless) and CSRF protection is disabled, because each request carries its own token.

## Project structure

```
src/main/java/se/iths/lw/microprojectproductservice/
├── config/        Security, CORS, OpenAPI and the cookie token resolver
├── controller/    REST endpoints (products, categories, test)
├── service/       Business logic
├── repository/    Spring Data JPA repositories
├── model/         JPA entities: Product, Category
├── dto/           Request and response objects
├── mapper/        MapStruct mappers between entities and DTOs
└── exception/     Custom exceptions and the global exception handler
```

## Tests

```bash
./mvnw test
```

On Windows: `mvnw.cmd test`.

The tests use an in-memory **H2** database (configured in `src/test/resources/application.properties`), so no PostgreSQL database or auth service is needed. They cover the services, the controllers (including security rules) and the cookie authentication.

## Docker and CI

Build and run with Docker:

```bash
docker build -t micro-project-product-service .

docker run -p 8081:8081 \
  -e PRODUCT_DB_URL=jdbc:postgresql://host.docker.internal:5432/productdb \
  -e PRODUCT_DB_USERNAME=your_user \
  -e PRODUCT_DB_PASSWORD=your_password \
  -e AUTH_SERVER_URL=http://host.docker.internal:8080 \
  micro-project-product-service
```

GitHub Actions:

- `maven.yml` builds and tests the project on every push and pull request to `main`.
- `docker-build-push.yml` builds a Docker image and pushes it to Docker Hub when a pull request to `main` is merged, or when started manually.

## Other repositories

| Part | Repository |
|---|---|
| Product service (this repo) | https://github.com/L2025S/micro-project-product-service |
| Auth service | https://github.com/OP-Devloop/Micro-Project-Auth-Server |
| Order service | https://github.com/Yunus-CanYucel/Micro-ProjectOrder-Service |
| Gateway | https://github.com/L2025S/webshop-gateway |
| Mail service | https://github.com/FredrikAlmen93/Mail-Service |
| Frontend | https://github.com/L2025S/webshop-frontend |