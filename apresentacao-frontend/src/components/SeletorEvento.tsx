import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { eventoService } from '../services/api'
import { useAuth } from '../context/AuthContext'

interface EventoOpcao {
  id: number
  nome: string
  dataHora: string
  local: string
}

interface SeletorEventoProps {
  titulo: string
  descricao: string
  rotaDestino: string
  icone?: string
}

export default function SeletorEvento({ titulo, descricao, rotaDestino, icone = '📅' }: SeletorEventoProps) {
  const navigate = useNavigate()
  const { usuario, isOrganizador } = useAuth()
  const [eventos, setEventos] = useState<EventoOpcao[]>([])
  const [carregando, setCarregando] = useState(true)
  const [busca, setBusca] = useState('')

  useEffect(() => {
    const carregar = async () => {
      setCarregando(true)
      try {
        if (isOrganizador() && usuario) {
          const res = await eventoService.listar(usuario.id)
          setEventos(res.data)
        } else {
          const res = await eventoService.catalogo()
          setEventos(res.data)
        }
      } catch {
        setEventos([])
      } finally {
        setCarregando(false)
      }
    }
    carregar()
  }, [usuario?.id, isOrganizador])

  const filtrados = eventos.filter(e =>
    e.nome.toLowerCase().includes(busca.toLowerCase()) ||
    e.local.toLowerCase().includes(busca.toLowerCase())
  )

  return (
    <div style={{
      textAlign: 'center',
      padding: '48px 24px',
      background: '#f8fafc',
      borderRadius: 16,
      border: '2px dashed #e2e8f0',
    }}>
      <p style={{ fontSize: 48, margin: '0 0 12px' }}>{icone}</p>
      <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b', margin: '0 0 8px' }}>{titulo}</h2>
      <p style={{ color: '#64748b', fontSize: 14, marginBottom: 24 }}>{descricao}</p>

      <div style={{ maxWidth: 400, margin: '0 auto 24px' }}>
        <input
          type="text"
          placeholder="Buscar evento..."
          value={busca}
          onChange={e => setBusca(e.target.value)}
          style={{
            width: '100%',
            padding: '10px 16px',
            border: '1px solid #e2e8f0',
            borderRadius: 8,
            fontSize: 14,
            outline: 'none',
          }}
        />
      </div>

      {carregando ? (
        <p style={{ color: '#94a3b8' }}>Carregando eventos...</p>
      ) : filtrados.length === 0 ? (
        <p style={{ color: '#94a3b8' }}>Nenhum evento encontrado.</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxWidth: 520, margin: '0 auto' }}>
          {filtrados.map(ev => (
            <button
              key={ev.id}
              onClick={() => navigate(`${rotaDestino}?eventoId=${ev.id}`)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                background: '#fff',
                border: '1px solid #e2e8f0',
                borderRadius: 10,
                padding: '14px 18px',
                cursor: 'pointer',
                textAlign: 'left',
                transition: 'border-color 0.15s',
              }}
            >
              <span style={{ fontSize: 22 }}>🎫</span>
              <div style={{ flex: 1 }}>
                <p style={{ fontWeight: 700, color: '#1e293b', fontSize: 14, margin: 0 }}>{ev.nome}</p>
                <p style={{ fontSize: 12, color: '#64748b', margin: '2px 0 0' }}>
                  📍 {ev.local} · {new Date(ev.dataHora).toLocaleDateString('pt-BR')}
                </p>
              </div>
              <span style={{ color: '#1d4ed8', fontWeight: 600, fontSize: 13 }}>Selecionar →</span>
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
