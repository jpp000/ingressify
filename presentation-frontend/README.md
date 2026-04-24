# ingressify-frontend

SPA React (Vite + TypeScript), **fora do build Maven**.

## Desenvolvimento

```bash
npm install
npm run dev
```

A aplicação sobe em `http://localhost:5173`. O `vite.config.ts` faz proxy de `/backend` para `http://localhost:8080` em desenvolvimento.

## Variáveis

- `VITE_API_URL` — URL base da API (padrão `http://localhost:8080`).

O backend deve expor CORS para a origem do Vite (`app.cors.origins` no Spring).
