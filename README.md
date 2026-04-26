<div align="center">

# Ingressify

**Marketplace de revenda de ingressos — monólito modular com DDD**

[![Java](https://img.shields.io/badge/Java-21%2B-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-Vite-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)


</div>

---
[![Prototipo](https://img.shields.io/badge/Prot%C3%B3tipo-ff3737?style=for-the-badge&logo=Figma&logoColor=white)](https://stitch.withgoogle.com/preview/16366040039797702871?node-id=ad9461aa2ae847b9b6f2639d74895a54)
[![Documentação](https://img.shields.io/badge/Documentação-ff3737?style=for-the-badge&logo=GoogleDocs&logoColor=white)](https://drive.google.com/drive/folders/1sPfKQZKRNiRfDLrPCzHwCgD-K8S7FFn2)
[![Mapa de Historias](https://img.shields.io/badge/Mapa_de_Histórias-ff3737?style=for-the-badge&logo=Lucid&logoColor=white)](https://lucid.app/lucidspark/c012a823-f686-4f14-a4ff-758ffb78807a/edit?viewport_loc=-1201%2C-1477%2C4944%2C2459%2C0_0&invitationId=inv_d96d5d5a-eaf1-4d05-897a-97f6ac6f3451)



## Sobre o projeto

O **Ingressify** é uma plataforma de revenda de ingressos construída como monólito modular Maven seguindo **Domain-Driven Design (DDD)**. O sistema organiza o domínio em dois bounded contexts principais — **Marketplace** e **Financeiro** — compartilhando um kernel comum para tipos primitivos de identidade e valor monetário.

A camada de aplicação orquestra as transações diretamente (sem barramento de eventos), mantendo rastreabilidade explícita dos fluxos de compra direta e revenda entre usuários.

---

## Arquitetura

```
┌─────────────────────────────────────────────────┐
│                 Domain Ingressify                │
│                                                  │
│   ┌──────────────┐        ┌──────────────────┐   │
│   │  Marketplace │        │    Financeiro    │   │
│   │              │        │                  │   │
│   │  • Evento    │        │  • Pagamento     │   │
│   │  • Ingresso  │        │  • Saldo         │   │
│   │  • TipoIngr. │        │  • Transacao     │   │
│   │  • Anuncio   │        │                  │   │
│   │  • Pedido    │        │                  │   │
│   └──────┬───────┘        └────────┬─────────┘   │
│          │    Shared-Kernel        │              │
│          └──────────┬──────────────┘              │
│                     │                             │
│            ┌────────┴────────┐                   │
│            │  SharedKernel   │                   │
│            │  • UsuarioId    │                   │
│            │  • Dinheiro     │                   │
│            └─────────────────┘                   │
└─────────────────────────────────────────────────┘
```

### Módulos Maven

| Módulo                    | Artefato                              | Responsabilidade                         |
|---------------------------|---------------------------------------|------------------------------------------|
| `parent/`                 | `ingressify-parent`                   | BOM — gerenciamento de dependências      |
| `domain-shared/`          | `ingressify-domain-shared`            | Shared Kernel (UsuarioId, Dinheiro)      |
| `domain-marketplace/`     | `ingressify-domain-marketplace`       | BC Marketplace (Evento, Ingresso, etc.)  |
| `domain-financeiro/`      | `ingressify-domain-financeiro`        | BC Financeiro (Pagamento, Saldo)         |
| `application/`            | `ingressify-application`              | Orquestração transacional                |
| `infrastructure/`         | `ingressify-infrastructure`           | JPA, Flyway, adaptadores externos        |
| `presentation-backend/`   | `ingressify-presentation-backend`     | API REST (Spring Boot)                   |
| `presentation-frontend/`  | —                                     | SPA React + Vite (fora do reactor Maven) |

---

## Tech Stack

**Backend**
- Java 21 · Spring Boot 3 · Spring Data JPA · Spring Security
- Flyway (migrações de banco) · PostgreSQL 18
- Context Mapper (modelo CML do domínio)

**Frontend**
- React 18 · Vite · TypeScript

**Testes**
- JUnit 5 · Cucumber (BDD) — features por bounded context

**Infraestrutura**
- Docker · Docker Compose

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Java (JDK) | 21             |
| Maven      | 3.9            |
| Docker     | 24             |
| Node.js    | 20             |

---

## Como executar

### 1. Subir o banco de dados

```bash
docker compose up -d postgres
```

PostgreSQL disponível em `localhost:5432` — database, usuário e senha: `ingressify`.

### 2. Build do backend

```bash
mvn clean install
```

### 3. Executar o backend

```bash
# A partir da raiz
mvn -pl presentation-backend spring-boot:run -Dspring-boot.run.profiles=desenvolvimento
```

API disponível em: `http://localhost:8080/backend/`

### 4. Executar o frontend

```bash
cd presentation-frontend
npm install
npm run dev
```

Frontend disponível em: `http://localhost:5173`

> Defina `VITE_API_URL=http://localhost:8080` se necessário.

---

### Stack completa via Docker Compose

Para subir Postgres + backend + frontend de uma vez:

```bash
docker compose up
```

---

## Bounded Contexts

### Marketplace

Gerencia o ciclo de vida de eventos, ingressos e anúncios de revenda.

| Agregado        | Responsabilidade                                                  |
|-----------------|-------------------------------------------------------------------|
| `Evento`        | Criação, atualização e cancelamento de eventos                    |
| `TipoIngresso`  | Lotes de ingressos com preço e controle de quantidade disponível  |
| `Ingresso`      | Ingresso individual — transferência e ciclo de revenda            |
| `AnuncioRevenda`| Publicação, reserva e conclusão de revendas entre usuários        |
| `Pedido`        | Compra direta de ingresso (ainda não esgotado)                    |

**Regras de negócio relevantes**
- Revenda só é permitida quando os ingressos oficiais do evento estiverem esgotados
- Um ingresso já anunciado não pode receber novo anúncio ativo
- Apenas o proprietário do ingresso pode anunciá-lo

### Financeiro

Controla pagamentos, saldos e histórico de transações dos usuários.

| Agregado    | Responsabilidade                                             |
|-------------|--------------------------------------------------------------|
| `Pagamento` | Fluxo PENDENTE → CONFIRMADO / REJEITADO por operação         |
| `Saldo`     | Saldo por usuário com crédito e débito (lança se insuficiente)|
| `Transacao` | Histórico imutável de movimentações (compra, venda, ajuste)  |

### Shared Kernel

Tipos primitivos compartilhados entre os dois bounded contexts:
- `UsuarioId` — identificador de usuário
- `Dinheiro` — valor monetário (`BigDecimal`)

---

## Testes (BDD)

Os testes são escritos em Gherkin e organizados por bounded context:

```
domain-marketplace/src/test/resources/features/marketplace/
  ├── cadastrar_evento.feature
  ├── ingresso.feature
  ├── anuncio_revenda.feature
  ├── realizar_compra.feature
  └── tipo_ingresso.feature

domain-financeiro/src/test/resources/features/financeiro/
  ├── processar_pagamento.feature
  ├── saldo.feature
  └── transacao.feature
```

Para executar:

```bash
mvn test
```

---

## Modelo de Domínio (Context Mapper)

O arquivo `ingressify.cml` na raiz do repositório descreve o mapa de contexto e todos os agregados em linguagem Context Mapper. Utilize a extensão [Context Mapper](https://contextmapper.org/) no VS Code ou IntelliJ para visualizar o diagrama gerado automaticamente.

---
