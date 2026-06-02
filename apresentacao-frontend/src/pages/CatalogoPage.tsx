import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { eventoService } from '../services/api'

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  status: string
  imagemCapaUrl?: string
}

export default function CatalogoPage() {
  const [eventos, setEventos] = useState<Evento[]>([])
  const [erro, setErro] = useState('')

  useEffect(() => {
    eventoService.catalogo()
      .then(r => setEventos(r.data))
      .catch(() => setErro('Erro ao carregar eventos'))
  }, [])

  if (erro) return <p style={{ color: 'red' }}>{erro}</p>

  return (
    <div>
      <h1>Catálogo de Eventos</h1>
      {eventos.length === 0 && <p>Nenhum evento disponível.</p>}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 16 }}>
        {eventos.map(e => (
          <div key={e.id} style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
            {e.imagemCapaUrl && (
              <img src={e.imagemCapaUrl} alt={e.nome} style={{ width: '100%', height: 160, objectFit: 'cover', borderRadius: 4 }} />
            )}
            <h3 style={{ margin: '8px 0 4px' }}>{e.nome}</h3>
            <p style={{ margin: 0, color: '#666', fontSize: 14 }}>
              {new Date(e.dataHora).toLocaleString('pt-BR')}
            </p>
            <p style={{ margin: '4px 0', color: '#666', fontSize: 14 }}>{e.local}</p>
            <Link to={`/eventos/${e.id}`} style={{ display: 'block', marginTop: 8, textAlign: 'center', padding: '8px 0', background: '#0070f3', color: '#fff', borderRadius: 4, textDecoration: 'none' }}>
              Ver detalhes
            </Link>
          </div>
        ))}
      </div>
    </div>
  )
}
