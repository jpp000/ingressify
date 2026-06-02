import { useState } from 'react'
import { revendaService } from '../services/api'

const USUARIO_ID = 1

interface Anuncio {
  id: number
  preco: number
  status: string
  quantidade: number
  vendedorId: number
}

export default function RevendasPage() {
  const [eventoId, setEventoId] = useState('')
  const [anuncios, setAnuncios] = useState<Anuncio[]>([])
  const [msg, setMsg] = useState('')

  const buscar = async () => {
    if (!eventoId) return
    try {
      const r = await revendaService.listar(Number(eventoId))
      setAnuncios(r.data)
    } catch {
      setMsg('Erro ao buscar anúncios')
    }
  }

  const reservar = async (id: number) => {
    try {
      await revendaService.reservar(id, USUARIO_ID)
      setMsg(`Anúncio ${id} reservado com sucesso!`)
      buscar()
    } catch (e: any) {
      setMsg(e.response?.data?.message || 'Erro ao reservar')
    }
  }

  return (
    <div>
      <h1>Marketplace de Revendas</h1>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <input
          type="number"
          placeholder="ID do Evento"
          value={eventoId}
          onChange={e => setEventoId(e.target.value)}
          style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd', width: 160 }}
        />
        <button onClick={buscar} style={{ padding: '8px 16px', background: '#0070f3', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
          Buscar Anúncios
        </button>
      </div>
      {msg && <p style={{ color: msg.includes('sucesso') ? 'green' : 'red' }}>{msg}</p>}

      {anuncios.length === 0 && eventoId && <p>Nenhum anúncio disponível para este evento.</p>}
      {anuncios.map(a => (
        <div key={a.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 8, borderRadius: 6 }}>
          <strong>Anúncio #{a.id}</strong> — {a.quantidade} ingresso(s) — R$ {Number(a.preco).toFixed(2)} — {a.status}
          {a.status === 'DISPONIVEL' && a.vendedorId !== USUARIO_ID && (
            <button onClick={() => reservar(a.id)} style={{ marginLeft: 16, padding: '6px 12px', background: '#38a169', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
              Reservar
            </button>
          )}
        </div>
      ))}
    </div>
  )
}
