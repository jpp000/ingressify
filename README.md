# Ingressify

Monólito modular Maven (DDD) com bounded contexts **Marketplace** e **Financeiro**, shared kernel, camada de aplicação com orquestração transacional direta (sem barramento de eventos), persistência JPA + Flyway, API REST Spring Boot e frontend React (Vite) desacoplado do build Maven.

## Requisitos

- Java 17+
- Maven 3.9+
- Docker (para PostgreSQL e compose completo)
- Node 20+ (apenas para o frontend)

## Build backend

Na raiz do repositório:

```bash
mvn clean install
```

O artefato executável é o módulo `presentation-backend`. O frontend em `presentation-frontend/` não participa do reactor Maven.

## Banco de dados local

```bash
docker compose up -d
```

Isso sobe PostgreSQL 18 em `localhost:5432`, database/usuário/senha: `ingressify`.

## Executar o backend

Com o Postgres em execução e o perfil `desenvolvimento`:

```bash
cd presentation-backend
mvn spring-boot:run -Dspring-boot.run.profiles=desenvolvimento
```

Ou a partir da raiz:

```bash
mvn -pl presentation-backend spring-boot:run -Dspring-boot.run.profiles=desenvolvimento
```

API base: `http://localhost:8080/backend/...`

## Compose (Postgres + imagem backend)

Após gerar a imagem com Jib (ou build local da imagem `ingressify-backend:0.0.1-SNAPSHOT`):

```bash
docker compose -f docker-compose-ingressify.yml up
```

## Frontend

```bash
cd presentation-frontend
npm install
npm run dev
```

Por padrão o Vite usa a porta **5173**. Defina `VITE_API_URL=http://localhost:8080` se necessário.

## Módulos Maven

| Pasta                 | Artefato                        |
|-----------------------|---------------------------------|
| `parent/`             | `ingressify-parent` (BOM)       |
| `domain-shared/`      | `ingressify-domain-shared`      |
| `domain-marketplace/` | `ingressify-domain-marketplace` |
| `domain-financeiro/`  | `ingressify-domain-financeiro`  |
| `application/`      | `ingressify-application`        |
| `infrastructure/`   | `ingressify-infrastructure`     |
| `presentation-backend/` | `ingressify-presentation-backend` |

## Context Mapper

Modelo de contexto em `ingressify.cml`.

## Mapa de Histórias do Usuário

https://lucid.app/lucidspark/c012a823-f686-4f14-a4ff-758ffb78807a/edit?viewport_loc=-1201%2C-1477%2C4944%2C2459%2C0_0&invitationId=inv_d96d5d5a-eaf1-4d05-897a-97f6ac6f3451
