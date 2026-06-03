<div align="center">

# Ingressify

**Marketplace de revenda de ingressos — monólito modular com DDD**

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)](https://www.postgresql.org/)

</div>

---

[![Prototipo](https://img.shields.io/badge/Prot%C3%B3tipo-ff3737?style=for-the-badge&logo=Figma&logoColor=white)](https://stitch.withgoogle.com/preview/16366040039797702871?node-id=ad9461aa2ae847b9b6f2639d74895a54)
[![Documentação](https://img.shields.io/badge/Documentação-ff3737?style=for-the-badge&logo=GoogleDocs&logoColor=white)](https://drive.google.com/drive/folders/1sPfKQZKRNiRfDLrPCzHwCgD-K8S7FFn2)
[![Mapa de Historias](https://img.shields.io/badge/Mapa_de_Histórias-ff3737?style=for-the-badge&logo=Lucid&logoColor=white)](https://lucid.app/lucidspark/c012a823-f686-4f14-a4ff-758ffb78807a/edit?viewport_loc=-1201%2C-1477%2C4944%2C2459%2C0_0&invitationId=inv_d96d5d5a-eaf1-4d05-897a-97f6ac6f3451)

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|------------|---------------|
| Docker     | 24            |
| Node.js    | 20            |
| npm        | 10            |

---

## Como executar (do zero)

### Passo 1 — Backend + Banco de Dados (Docker)

Na pasta raiz do projeto (`ingressify/`):

```bash
docker compose up -d --build
```

Aguarde até o container `ingressify-backend-1` ficar saudável (~30s). Para acompanhar os logs:

```bash
docker compose logs -f backend
```

Você deve ver `Started IngressifyApplication` quando estiver pronto.

- Backend disponível em: `http://localhost:8080`
- Banco PostgreSQL em: `localhost:5433` (user/senha/db: `ingressify`)

> Os dados persistem entre restarts do backend. Para apagar tudo e recomeçar do zero: `docker compose down -v`

---

### Passo 2 — Frontend (local)

Em outro terminal, na pasta `apresentacao-frontend/`:

```bash
cd apresentacao-frontend
npm install
npm run dev
```

Frontend disponível em: **http://localhost:3000**

---

## Fluxo de uso

1. Acesse `http://localhost:3000` — você será redirecionado para o **Login**
2. Clique em **"Criar conta agora"** para cadastrar um usuário
3. Escolha o papel: **Comprador** (compra ingressos) ou **Organizador** (cria eventos)
4. Após cadastro, faça login — você será redirecionado para o dashboard correto

---

## Comandos úteis

### Rebuildar após alterações no backend

```bash
docker compose build backend
docker compose up -d backend
```

> No PowerShell (Windows), use ponto e vírgula ao invés de `&&`:
> ```powershell
> docker compose build backend; docker compose up -d backend
> ```

### Parar tudo

```bash
docker compose down
```

### Parar e apagar dados do banco

```bash
docker compose down -v
```

### Ver logs do backend

```bash
docker compose logs -f backend
```

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                     Ingressify                          │
│                                                         │
│  apresentacao-frontend/   →   React + Vite (porta 3000) │
│  apresentacao-backend/    →   Spring Boot  (porta 8080) │
│  aplicacao/               →   Casos de uso              │
│  infraestrutura/          →   JPA, repositórios         │
│  dominio-marketplace/     →   BC Marketplace            │
│  dominio-financeiro/      →   BC Financeiro             │
│  dominio-identidade/      →   BC Identidade             │
│  dominio-compartilhado/   →   Shared Kernel             │
└─────────────────────────────────────────────────────────┘
```

### Módulos Maven

| Módulo                    | Responsabilidade                              |
|---------------------------|-----------------------------------------------|
| `pai/`                    | BOM — gerenciamento de dependências           |
| `dominio-compartilhado/`  | Shared Kernel (UsuarioId, Dinheiro)           |
| `dominio-identidade/`     | BC Identidade (Usuario, papéis, senha)        |
| `dominio-marketplace/`    | BC Marketplace (Evento, Ingresso, Anuncio...) |
| `dominio-financeiro/`     | BC Financeiro (Pagamento, Saldo, Transacao)   |
| `aplicacao/`              | Casos de uso (*ServicoAplicacao)              |
| `infraestrutura/`         | JPA, Flyway, repositórios concretos           |
| `apresentacao-backend/`   | API REST — controllers e DTOs                 |

---

## Endpoints principais

| Método | Rota                                    | Descrição                              |
|--------|-----------------------------------------|----------------------------------------|
| POST   | `/auth/login`                           | Login (retorna id, nome, papeis)       |
| POST   | `/auth/cadastro`                        | Cadastro (COMPRADOR/ORGANIZADOR)       |
| GET    | `/eventos/catalogo`                     | Catálogo público de eventos            |
| POST   | `/eventos`                              | Criar evento (Organizador)             |
| POST   | `/ingressos/comprar`                    | Comprar ingresso (Comprador)           |
| GET    | `/meus-ingressos`                       | Listar ingressos do usuário            |
| GET    | `/saldo`                                | Consultar saldo                        |
| POST   | `/saldo/adicionar`                      | Adicionar saldo                        |
| GET    | `/transacoes`                           | Histórico de transações                |
| GET    | `/revendas/anuncios`                    | Todos os anúncios ativos (marketplace) |
| GET    | `/revendas/anuncios?eventoId={id}`      | Anúncios filtrados por evento          |
| GET    | `/revendas/anuncios/meus`               | Meus anúncios de revenda               |
| POST   | `/revendas/anuncios`                    | Criar anúncio de revenda               |
| POST   | `/revendas/anuncios/{id}/reservar`      | Reservar ingresso de revenda           |
| POST   | `/revendas/anuncios/{id}/confirmar`     | Confirmar compra de revenda            |
| DELETE | `/revendas/anuncios/{id}`               | Cancelar anúncio de revenda            |

---

## Tech Stack

**Backend:** Java 17 · Spring Boot 3.5 · Spring Data JPA · Flyway · PostgreSQL 16

**Frontend:** React 18 · Vite · TypeScript · Axios · React Router v6

**Infra:** Docker · Docker Compose

**Padrões de Design:** Observer · Strategy · Template Method · Proxy · Iterator · Decorator

---

## Testes BDD

```bash
# Na raiz do projeto (requer Maven instalado localmente)
mvn test
```
