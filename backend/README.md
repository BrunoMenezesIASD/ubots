# FlowPay Atendimento — Backend (Java)

Backend da aplicação **FlowPay Atendimento**, construído com **Spring Boot** e **PostgreSQL**, com documentação via **Swagger/OpenAPI**, migrations com **Flyway** e endpoints de observabilidade via **Actuator**.

---

## ✅ Stack e versões

Conforme o `pom.xml`:

- **Java:** 17
    - `maven.compiler.release/source/target = 17`
    - compilação com `-parameters` (importante para `@PathVariable`, `@RequestParam` sem `name`)
- **Spring Boot:** 3.3.2
- **Springdoc OpenAPI (Swagger UI):** 2.6.0
- **Persistência:** Spring Data JPA
- **Banco:** PostgreSQL (driver runtime)
- **Migrations:** Flyway (`flyway-core` + `flyway-database-postgresql`)
- **Observabilidade:** Spring Boot Actuator
- **Testes:** Spring Boot Starter Test + Testcontainers (PostgreSQL)

---

## ▶️ Como executar (porta 8080)

### Opção 1) Rodar via Maven
```bash
mvn spring-boot:run
```
---
### ▶️ Opção 2) Buildar e executar o JAR
```bash
mvn clean package
java -jar target/backend-1.0.0.jar

A aplicação sobe por padrão em:

Base URL: http://localhost:8080
```
---
### 🧭 Links úteis (Swagger, Health, Actuator)

#### Swagger (OpenAPI UI)

Swagger UI: http://localhost:8080/swagger-ui/index.html

OpenAPI JSON: http://localhost:8080/v3/api-docs

#### Actuator

Health: http://localhost:8080/actuator/health

Info: http://localhost:8080/actuator/info

Metrics: http://localhost:8080/actuator/metrics

### Testes

```bash
mvn test
```
---
