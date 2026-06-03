import { useState } from 'react'
import Navbar from '../components/Navbar'
import { revendaService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { formatMoeda } from '../constants'

interface Anuncio {
  id: number
  preco: number
  status: string
  quantidade: number
  vendedorId: number
}

export default function RevendasPage() {
  const { usuario } = useAuth()
  const [eventoId, setEventoId] = useState('')
  const [anuncios, setAnuncios] = useState<Anuncio[]>([])
  const [msg, setMsg] = useState('')
  const [carregando, setCarregando] = useState(false)

  const buscar = async () => {
    if (!eventoId) return
    setCarregando(true)
    try {
      const r = await revendaService.listar(Number(eventoId))
      setAnuncios(r.data)
      setMsg('')
    } catch {
      setMsg('Erro ao buscar anúncios')
    } finally {
      setCarregando(false)
    }
  }

  const reservar = async (id: number) => {
    if (!usuario) return
    try {
      await revendaService.reservar(id, usuario.id)
      setMsg(`Anúncio ${id} reservado! Você tem 30 minutos para confirmar a compra.`)
      buscar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      setMsg(err.response?.data?.message ?? 'Erro ao reservar')
    }
  }

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 24px' }}>
        <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1e293b', marginBottom: 8 }}>
          Marketplace de Revendas
        </h1>
        <p style={{ color: '#64748b', fontSize: 14, marginBottom: 24 }}>
          Encontre ingressos de compradores que não podem mais ir.
        </p>

        <div style={{ display: 'flex', gap: 10, marginBottom: 20 }}>
          <input
            type="number"
            placeholder="ID do Evento"
            value={eventoId}
            onChange={e => setEventoId(e.target.value)}
            style={{
              padding: '10px 14px',
              border: '1px solid #e2e8f0',
              borderRadius: 8,
              fontSize: 14,
              width: 180,
              outline: 'none',
            }}
          />
          <button
            onClick={buscar}
            disabled={carregando}
            style={{
              padding: '10px 20px',
              background: '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: 8,
              fontSize: 14,
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            {carregando ? 'Buscando...' : 'Buscar Anúncios'}
          </button>
        </div>

        {msg && (
          <div style={{
            padding: '12px 14px',
            borderRadius: 8,
            background: msg.includes('Erro') ? '#fef2f2' : '#f0fdf4',
            color: msg.includes('Erro') ? '#dc2626' : '#16a34a',
            fontSize: 14,
            marginBottom: 16,
          }}>
            {msg}
          </div>
        )}

        {anuncios.length === 0 && eventoId && !carregando && (
          <p style={{ color: '#94a3b8', fontSize: 14 }}>Nenhum anúncio disponível para este evento.</p>
        )}

        {anuncios.map(a => (
          <div key={a.id} style={{
            background: '#fff',
            border: '1px solid #e2e8f0',
            borderRadius: 12,
            padding: '16px 20px',
            marginBottom: 12,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}>
            <div>
              <p style={{ fontWeight: 700, color: '#1e293b', fontSize: 15 }}>
                {a.quantidade} ingresso(s)
              </p>
              <p style={{ fontSize: 13, color: '#64748b', marginTop: 2 }}>
                Status: {a.status}
              </p>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <span style={{ fontSize: 20, fontWeight: 800, color: '#1d4ed8' }}>
                {formatMoeda(a.preco)}
              </span>
              {a.status === 'DISPONIVEL' && a.vendedorId !== usuario?.id && (
                <button
                  onClick={() => reservar(a.id)}
                  style={{
                    padding: '8px 18px',
                    background: '#16a34a',
                    color: '#fff',
                    border: 'none',
                    borderRadius: 8,
                    fontSize: 14,
                    fontWeight: 600,
                    cursor: 'pointer',
                  }}
                >
                  Reservar
                </button>
              )}
              {a.vendedorId === usuario?.id && (
                <span style={{ fontSize: 12, color: '#94a3b8', fontStyle: 'italic' }}>
                  Seu anúncio
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </>
  )
}
