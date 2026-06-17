<div align="center">

# Ingressify

**Marketplace de revenda de ingressos — monólito modular com DDD**

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql)](https://www.postgresql.org/)

</div>

---

[![Prototipo]](https://img.shields.io/badge/Prot%C3%B3tipo-blue?style=for-the-badge&logo=googlechrome&logoColor=white)](https://stitch.withgoogle.com/preview/8074556060719376284?node-id=1356fa38cb0d4b499086121cc88a9293)

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

### Passo 2 — Frontend com hot reload (recomendado)

Na pasta raiz, com o backend já rodando:

```bash
./start-dev.sh
```

Ou manualmente:

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build --watch
```

O frontend recarrega automaticamente ao salvar arquivos em `apresentacao-frontend/`.
Alterações no backend disparam rebuild automático do container (pode levar ~1 min).

Frontend disponível em: **http://localhost:3000**

#### Alternativa — Frontend local (sem Docker)

```bash
cd apresentacao-frontend
npm install
npm run dev
```

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

**Padrões de Design:** Observer · Strategy · Template Method · Proxy · Iterator · Decorator · State

---

## Funcionalidades Não Triviais (14 no total)

Cada funcionalidade possui regras de negócio de complexidade média ou alta e altera estado persistido.

| # | Funcionalidade | Módulo principal | Feature BDD |
|---|---------------|-----------------|-------------|
| 1 | Cadastro e publicação de eventos | dominio-marketplace | `cadastrar_evento.feature` |
| 2 | Compra de ingressos (inteira/meia) | dominio-marketplace | `realizar_compra.feature` |
| 3 | Revenda de ingressos (anúncio + reserva + confirmação) | dominio-marketplace | `anuncio_revenda.feature` |
| 4 | Solicitação de reembolso (voluntário / por cancelamento) | dominio-marketplace | `reembolso.feature` |
| 5 | Gestão de reembolso pelo administrador | dominio-marketplace | `reembolso_admin.feature` |
| 6 | Check-in de ingresso no evento | dominio-marketplace | `check_in.feature` |
| 7 | Sorteio de ingressos (loteria) | dominio-marketplace | `sorteio.feature` |
| 8 | Mapa de assentos numerados | dominio-marketplace | `mapa_assentos.feature` |
| 9 | Avaliação de eventos por compradores | dominio-marketplace | `avaliacao.feature` |
| 10 | Processamento de pagamentos | dominio-financeiro | `processar_pagamento.feature` |
| 11 | Gestão de saldo e extrato financeiro | dominio-financeiro | `saldo.feature` / `transacao.feature` |
| 12 | Cupom de desconto (percentual e valor fixo) | dominio-marketplace | `cupom.feature` |
| 13 | Compra em grupo (prazo, pagamento individual, confirmação) | dominio-marketplace | `grupo_compra.feature` |
| 14 | Denúncia de eventos por compradores | dominio-marketplace | `denuncia_evento.feature` |

---

## Padrões de Design Implementados

| Padrão | Onde é aplicado | Arquivo de referência |
|--------|-----------------|-----------------------|
| **Strategy** | Cálculo de desconto em cupons (percentual vs. valor fixo) | `cupom/estrategia/EstrategiaDesconto.java` |
| **Strategy** | Estratégia de reembolso por tipo de cancelamento | `reembolso/estrategia/EstrategiaReembolso.java` |
| **Observer** | Notificação de eventos de domínio (compra, cancelamento) | `dominio-compartilhado/.../EventoObserver.java` |
| **Template Method** | Fluxo de compra de ingresso (validar → reservar → pagar → confirmar) | `aplicacao/.../compra/` |
| **Proxy** | Controle de acesso a operações administrativas via header `X-Usuario-Id` | `apresentacao-backend/.../controller/` |
| **Iterator** | Varredura de participantes do grupo de compra e lotes de ingressos | `GrupoCompraServico.java`, `SorteioServico.java` |
| **Decorator** | Enriquecimento de respostas HTTP com dados de domínio | DTOs em `apresentacao-backend/.../dto/` |
| **State** | Máquina de estados para status de denúncia, reembolso e grupo de compra | `DenunciaEvento.java`, `SolicitacaoReembolso.java`, `GrupoCompra.java` |

---

## DDD — Níveis de Abstração

| Nível | Artefato | Exemplos no projeto |
|-------|----------|---------------------|
| **Preliminar** | Linguagem ubíqua, glossário de negócio | Nomes de classes como `DenunciaEvento`, `SolicitacaoReembolso`, `GrupoCompra` |
| **Estratégico** | Bounded Contexts + Context Map | Módulos `dominio-marketplace`, `dominio-financeiro`, `dominio-identidade`, `dominio-compartilhado` |
| **Tático** | Aggregates, Value Objects, Domain Services, Repositories | `Evento` (aggregate root), `Dinheiro` (VO), `GrupoCompraServico`, `EventoRepositorio` |
| **Operacional** | Application Services, Infrastructure, REST API | `aplicacao/*ServicoAplicacao.java`, `infraestrutura/repositorios/`, `apresentacao-backend/controller/` |

---

## Testes BDD

### Rodar todos os testes

```bash
# Na raiz do projeto (requer Maven instalado localmente)
mvn test
```

### Rodar testes de um módulo específico

```bash
# Apenas testes do domínio de marketplace
mvn -pl dominio-marketplace test

# Apenas testes do domínio financeiro
mvn -pl dominio-financeiro test
```

### Localização das features

```
dominio-marketplace/src/test/resources/features/marketplace/
├── avaliacao.feature              # Avaliação de eventos
├── cadastrar_evento.feature       # Cadastro e publicação
├── check_in.feature               # Check-in de ingresso
├── cupom.feature                  # Cupom de desconto
├── denuncia_evento.feature        # Denúncia de eventos
├── grupo_compra.feature           # Compra em grupo
├── mapa_assentos.feature          # Mapa de assentos numerados
├── realizar_compra.feature        # Compra de ingressos
├── reembolso.feature              # Solicitação de reembolso
├── reembolso_admin.feature        # Gestão de reembolso (admin)
├── anuncio_revenda.feature        # Revenda de ingressos
└── sorteio.feature                # Sorteio de ingressos

dominio-financeiro/src/test/resources/features/financeiro/
├── processar_pagamento.feature
├── saldo.feature
└── transacao.feature
```

---

## Como descobrir IDs para testes manuais

Útil quando uma funcionalidade pede um `usuarioId` ou `tipoIngressoId` diretamente (ex.: Compra em Grupo).

### Passo 1 — ID do usuário

```bash
docker compose exec postgres psql -U ingressify -d ingressify -c \
  "SELECT id, nome, email FROM usuarios ORDER BY id;"
```

Alternativa sem acessar o banco: faça login via `POST /auth/login` (`{ "email": "...", "senha": "..." }`) — a resposta traz o campo `id` do usuário logado.

### Passo 2 — ID do evento

```bash
docker compose exec postgres psql -U ingressify -d ingressify -c \
  "SELECT id, nome FROM eventos ORDER BY id;"
```

### Passo 3 — ID do tipo de ingresso de um evento

Use o id do evento obtido no Passo 2 no lugar de `{eventoId}`:

```bash
docker compose exec postgres psql -U ingressify -d ingressify -c \
  "SELECT id, nome, quantidade_disponivel, quantidade_total FROM tipos_ingresso WHERE evento_id = {eventoId} ORDER BY id;"
```

Alternativa via API: `GET /eventos/{eventoId}/tipos-ingresso` retorna a lista com o campo `id` de cada tipo.
