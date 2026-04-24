import { useEffect, useState } from "react";
import { apiGet } from "../api/client";

type EventoResumo = {
  id: number;
  nome: string;
  dataHora: string;
  local: string;
  status: string;
  capacidade: number;
};

export function EventosCatalogo() {
  const [eventos, setEventos] = useState<EventoResumo[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  useEffect(() => {
    apiGet<EventoResumo[]>("/backend/evento/catalogo")
      .then(setEventos)
      .catch((e: Error) => setErro(e.message));
  }, []);

  if (erro) {
    return (
      <main style={{ fontFamily: "system-ui", padding: 24 }}>
        <p>Não foi possível carregar o catálogo: {erro}</p>
        <p>Confirme o backend em execução e CORS (perfil desenvolvimento).</p>
      </main>
    );
  }

  if (!eventos) {
    return (
      <main style={{ fontFamily: "system-ui", padding: 24 }}>
        <p>Carregando eventos…</p>
      </main>
    );
  }

  return (
    <main style={{ fontFamily: "system-ui", padding: 24, maxWidth: 720 }}>
      <h1>Catálogo de eventos</h1>
      <ul>
        {eventos.map((e) => (
          <li key={e.id}>
            <strong>{e.nome}</strong> — {e.local} — {e.dataHora} — capacidade {e.capacidade}
          </li>
        ))}
      </ul>
      {eventos.length === 0 && <p>Nenhum evento ativo futuro.</p>}
    </main>
  );
}
