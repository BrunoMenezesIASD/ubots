# FlowPay Atendimento — Distribuição e Monitoramento (Spring Boot + Angular)

Repositório completo para o desafio **Distribuição de Atendimentos FlowPay**.

## Visão geral

- **Back-end:** Java 17, Spring Boot 3, REST, JPA/Hibernate, Flyway, Swagger/OpenAPI, SSE (Server-Sent Events)
- **Front-end:** Angular 17 (standalone), Angular Material, consumo de REST + SSE para atualização em tempo real
- **Banco:** PostgreSQL (via Docker Compose)

### Regras de negócio
- Times:
  - `CARTOES` quando assunto for exatamente **"Problemas com cartão"**
  - `EMPRESTIMOS` quando assunto for exatamente **"Contratação de empréstimo"**
  - `OUTROS` para os demais
- Cada atendente atende no máximo **3** simultaneamente.
- Se não houver vaga no time, atendimento entra em **fila FIFO** (por time).
- Seleção de atendente: **menor número de atendimentos ativos** (status `ASSIGNED`), empate por **menor id**.
- Distribuição é **atômica** com transações e locking no banco (PostgreSQL):
  - Seleção do atendente disponível usa `FOR UPDATE` (PESSIMISTIC_WRITE) em query nativa para evitar race condition.
  - Retirada da fila usa lock no `QueueItem` mais antigo do time.

## Como rodar (Docker)
Na raiz:

```bash
docker compose up --build
```

Back-end: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html

## Como rodar o Front-end
Em outro terminal:

```bash
cd frontend
npm install
npm start
```

Front-end: http://localhost:4200

> O front assume o back em `http://localhost:8080/api`.

## Testar SSE (stream em tempo real)
```bash
curl -N http://localhost:8080/api/dashboard/stream
```

## Exemplos de uso (curl)

### Criar atendimento
```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Bruno","subject":"Problemas com cartão"}'
```

### Listar atendimentos
```bash
curl "http://localhost:8080/api/requests?team=CARTOES&status=ASSIGNED"
```

### Finalizar atendimento
```bash
curl -X POST http://localhost:8080/api/requests/1/finish
```

### Dashboard summary
```bash
curl http://localhost:8080/api/dashboard/summary
```

---
