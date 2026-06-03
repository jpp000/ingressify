import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { revendaService, eventoService } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { formatMoeda } from '../constants'

interface Anuncio {
  id: number
  preco: number
  status: string
  quantidade: number
  vendedorId: number
  compradorId: number | null
  eventoId: number
  ingressoIds: string[]
}

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  imagemCapaUrl?: string
}

type ActionState = { type: 'idle' } | { type: 'loading'; id: number } | { type: 'success'; id: number; msg: string } | { type: 'error'; id: number; msg: string }

export default function RevendasPage() {
  const { usuario } = useAuth()
  const [searchParams] = useSearchParams()
  const eventoIdParam = searchParams.get('eventoId')

  const [eventoId, setEventoId] = useState(eventoIdParam ?? '')
  const [anuncios, setAnuncios] = useState<Anuncio[]>([])
  const [evento, setEvento] = useState<Evento | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [msgGlobal, setMsgGlobal] = useState('')
  const [action, setAction] = useState<ActionState>({ type: 'idle' })

  useEffect(() => {
    if (eventoIdParam) buscar(Number(eventoIdParam))
  }, [eventoIdParam])

  const buscar = async (id?: number) => {
    const eid = id ?? Number(eventoId)
    if (!eid) return
    setCarregando(true)
    setMsgGlobal('')
    try {
      const [anunciosRes, eventoRes] = await Promise.all([
        revendaService.listar(eid),
        eventoService.detalhe(eid),
      ])
      setAnuncios(anunciosRes.data)
      setEvento(eventoRes.data)
    } catch {
      setMsgGlobal('Erro ao buscar anúncios.')
    } finally {
      setCarregando(false)
    }
  }

  const reservar = async (anuncioId: number) => {
    if (!usuario) return
    setAction({ type: 'loading', id: anuncioId })
    try {
      await revendaService.reservar(anuncioId, usuario.id)
      setAction({ type: 'success', id: anuncioId, msg: 'Reservado! Confirme a compra para finalizar.' })
      buscar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string; message?: string } } }
      setAction({ type: 'error', id: anuncioId, msg: err.response?.data?.motivo ?? err.response?.data?.message ?? 'Erro ao reservar.' })
    }
  }

  const confirmar = async (anuncioId: number) => {
    if (!usuario) return
    setAction({ type: 'loading', id: anuncioId })
    try {
      await revendaService.confirmar(anuncioId, usuario.id)
      setAction({ type: 'success', id: anuncioId, msg: 'Compra confirmada! O ingresso já está em Meus Ingressos.' })
      buscar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string; message?: string } } }
      setAction({ type: 'error', id: anuncioId, msg: err.response?.data?.motivo ?? 'Erro ao confirmar compra.' })
    }
  }

  const cancelarAnuncio = async (anuncioId: number) => {
    if (!usuario) return
    if (!confirm('Cancelar este anúncio? Seu ingresso voltará para a sua carteira.')) return
    setAction({ type: 'loading', id: anuncioId })
    try {
      await revendaService.cancelar(anuncioId, usuario.id)
      setAction({ type: 'idle' })
      buscar()
    } catch {
      setAction({ type: 'error', id: anuncioId, msg: 'Erro ao cancelar anúncio.' })
    }
  }

  const disponíveis = anuncios.filter(a => a.status === 'DISPONIVEL')
  const reservados = anuncios.filter(a => a.status === 'RESERVADO')
  const meusAnuncios = anuncios.filter(a => a.vendedorId === usuario?.id)
  const minhaReserva = anuncios.find(a => a.status === 'RESERVADO' && a.compradorId === usuario?.id)

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: '40px 24px' }}>

        <h1 style={{ fontSize: 28, fontWeight: 800, color: '#1e293b', marginBottom: 6 }}>
          Marketplace de Revendas
        </h1>
        <p style={{ color: '#64748b', fontSize: 14, marginBottom: 32 }}>
          Ingressos de compradores que não podem mais ir — transferência segura e garantida.
        </p>

        {/* Busca */}
        <div style={{
          background: '#fff', borderRadius: 16, padding: '20px 24px',
          boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 32,
          display: 'flex', gap: 12, alignItems: 'flex-end',
        }}>
          <div style={{ flex: 1 }}>
            <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
              ID do Evento
            </label>
            <input
              type="number"
              placeholder="ex: 1"
              value={eventoId}
              onChange={e => setEventoId(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && buscar()}
              style={{
                width: '100%', padding: '10px 14px',
                border: '1px solid #e2e8f0', borderRadius: 8,
                fontSize: 14, outline: 'none',
              }}
            />
          </div>
          <button
            onClick={() => buscar()}
            disabled={carregando || !eventoId}
            style={{
              padding: '10px 24px', background: carregando ? '#93c5fd' : '#1d4ed8',
              color: '#fff', border: 'none', borderRadius: 8,
              fontSize: 14, fontWeight: 700, cursor: 'pointer', whiteSpace: 'nowrap',
            }}
          >
            {carregando ? 'Buscando...' : 'Buscar'}
          </button>
        </div>

        {msgGlobal && (
          <div style={{ background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 10, padding: '12px 16px', marginBottom: 24, color: '#dc2626', fontSize: 14 }}>
            {msgGlobal}
          </div>
        )}

        {/* Evento encontrado */}
        {evento && (
          <>
            <div style={{
              background: '#fff', borderRadius: 16, padding: '16px 20px',
              boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24,
              display: 'flex', alignItems: 'center', gap: 16,
            }}>
              {evento.imagemCapaUrl && (
                <img src={evento.imagemCapaUrl} alt={evento.nome} style={{ width: 56, height: 56, borderRadius: 10, objectFit: 'cover' }} />
              )}
              <div>
                <p style={{ fontWeight: 800, fontSize: 16, color: '#1e293b' }}>{evento.nome}</p>
                <p style={{ fontSize: 13, color: '#64748b' }}>
                  📅 {evento.dataHora ? new Date(evento.dataHora).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' }) : ''} &nbsp;·&nbsp; 📍 {evento.local}
                </p>
              </div>
              <div style={{ marginLeft: 'auto', textAlign: 'right' }}>
                <p style={{ fontSize: 13, color: '#94a3b8' }}>{disponíveis.length} disponível(is)</p>
                {reservados.length > 0 && <p style={{ fontSize: 12, color: '#f59e0b' }}>{reservados.length} reservado(s)</p>}
              </div>
            </div>

            {/* Minha reserva pendente — destaque */}
            {minhaReserva && (
              <div style={{
                background: 'linear-gradient(135deg, #fffbeb, #fef3c7)',
                border: '1px solid #fde68a', borderRadius: 16,
                padding: '20px 24px', marginBottom: 24,
                display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16,
              }}>
                <div>
                  <p style={{ fontSize: 13, fontWeight: 700, color: '#92400e', marginBottom: 4 }}>
                    Você tem uma reserva pendente
                  </p>
                  <p style={{ fontSize: 15, fontWeight: 800, color: '#1e293b' }}>
                    {minhaReserva.quantidade} ingresso(s) · {formatMoeda(minhaReserva.preco)}
                  </p>
                  <p style={{ fontSize: 12, color: '#78716c', marginTop: 4 }}>
                    Confirme a compra para receber os ingressos. A reserva expira em breve.
                  </p>
                </div>
                <button
                  onClick={() => confirmar(minhaReserva.id)}
                  disabled={action.type === 'loading' && action.id === minhaReserva.id}
                  style={{
                    background: '#16a34a', color: '#fff', border: 'none',
                    borderRadius: 10, padding: '12px 24px',
                    fontSize: 14, fontWeight: 700, cursor: 'pointer',
                    whiteSpace: 'nowrap', flexShrink: 0,
                  }}
                >
                  {action.type === 'loading' && action.id === minhaReserva.id ? 'Confirmando...' : 'Confirmar Compra'}
                </button>
              </div>
            )}

            {/* Meus anúncios ativos */}
            {meusAnuncios.length > 0 && (
              <section style={{ marginBottom: 32 }}>
                <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', marginBottom: 12 }}>
                  Meus Anúncios Neste Evento
                </h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {meusAnuncios.map(a => (
                    <AnuncioCard
                      key={a.id}
                      anuncio={a}
                      isMeu
                      action={action}
                      onCancelar={cancelarAnuncio}
                    />
                  ))}
                </div>
              </section>
            )}

            {/* Feedback de ações */}
            {(action.type === 'success' || action.type === 'error') && (
              <div style={{
                background: action.type === 'success' ? '#f0fdf4' : '#fef2f2',
                border: `1px solid ${action.type === 'success' ? '#bbf7d0' : '#fecaca'}`,
                borderRadius: 10, padding: '12px 16px', marginBottom: 20,
                color: action.type === 'success' ? '#15803d' : '#dc2626',
                fontSize: 14, fontWeight: 600,
              }}>
                {action.msg}
              </div>
            )}

            {/* Anúncios disponíveis */}
            <section>
              <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', marginBottom: 12 }}>
                Disponíveis ({disponíveis.length})
              </h2>
              {disponíveis.length === 0 ? (
                <div style={{
                  textAlign: 'center', padding: '48px 24px',
                  background: '#f8fafc', borderRadius: 16,
                  color: '#94a3b8', border: '1px dashed #e2e8f0',
                }}>
                  <div style={{ fontSize: 40, marginBottom: 12 }}>🎟️</div>
                  <p style={{ fontWeight: 600, marginBottom: 4 }}>Nenhum anúncio disponível</p>
                  <p style={{ fontSize: 13 }}>Seja o primeiro a anunciar ou verifique mais tarde.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {disponíveis.map(a => (
                    <AnuncioCard
                      key={a.id}
                      anuncio={a}
                      isMeu={a.vendedorId === usuario?.id}
                      action={action}
                      onReservar={reservar}
                      onCancelar={cancelarAnuncio}
                      currentUserId={usuario?.id}
                    />
                  ))}
                </div>
              )}
            </section>

            {reservados.length > 0 && (
              <section style={{ marginTop: 32 }}>
                <h2 style={{ fontSize: 16, fontWeight: 700, color: '#94a3b8', marginBottom: 12 }}>
                  Reservados ({reservados.length})
                </h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {reservados.map(a => (
                    <AnuncioCard
                      key={a.id}
                      anuncio={a}
                      isMeu={a.vendedorId === usuario?.id}
                      action={action}
                      onCancelar={cancelarAnuncio}
                      currentUserId={usuario?.id}
                    />
                  ))}
                </div>
              </section>
            )}
          </>
        )}

        {!evento && !carregando && eventoId && (
          <div style={{ textAlign: 'center', padding: '48px 0', color: '#94a3b8' }}>
            Evento não encontrado.
          </div>
        )}
      </div>
    </>
  )
}

function AnuncioCard({
  anuncio, isMeu, action, currentUserId, onReservar, onCancelar,
}: {
  anuncio: Anuncio
  isMeu: boolean
  action: ActionState
  currentUserId?: number
  onReservar?: (id: number) => void
  onCancelar?: (id: number) => void
}) {
  const isLoading = action.type === 'loading' && action.id === anuncio.id
  const esgotado = anuncio.status === 'RESERVADO'

  return (
    <div style={{
      background: '#fff', border: `1px solid ${esgotado ? '#fde68a' : '#e2e8f0'}`,
      borderRadius: 12, padding: '16px 20px',
      display: 'flex', alignItems: 'center', gap: 16,
      opacity: esgotado && !isMeu ? 0.7 : 1,
    }}>
      <div style={{
        width: 44, height: 44, borderRadius: 10,
        background: isMeu ? '#eff6ff' : '#f0fdf4',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: 20, flexShrink: 0,
      }}>
        🎟️
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
          <span style={{ fontWeight: 700, color: '#1e293b', fontSize: 15 }}>
            {anuncio.quantidade} ingresso(s)
          </span>
          <span style={{
            fontSize: 11, fontWeight: 700, padding: '2px 8px', borderRadius: 20,
            background: anuncio.status === 'DISPONIVEL' ? '#dcfce7' : '#fef3c7',
            color: anuncio.status === 'DISPONIVEL' ? '#16a34a' : '#b45309',
          }}>
            {anuncio.status === 'DISPONIVEL' ? 'Disponível' : 'Reservado'}
          </span>
          {isMeu && (
            <span style={{ fontSize: 11, color: '#94a3b8', fontStyle: 'italic' }}>Seu anúncio</span>
          )}
        </div>
        <p style={{ fontSize: 12, color: '#94a3b8' }}>
          Anúncio #{anuncio.id}
        </p>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <span style={{ fontSize: 22, fontWeight: 800, color: '#1d4ed8' }}>
          {formatMoeda(anuncio.preco)}
        </span>
        {anuncio.status === 'DISPONIVEL' && !isMeu && onReservar && currentUserId && (
          <button
            onClick={() => onReservar(anuncio.id)}
            disabled={isLoading}
            style={{
              padding: '8px 20px', background: isLoading ? '#86efac' : '#16a34a',
              color: '#fff', border: 'none', borderRadius: 8,
              fontSize: 14, fontWeight: 600, cursor: 'pointer',
            }}
          >
            {isLoading ? '...' : 'Reservar'}
          </button>
        )}
        {isMeu && anuncio.status === 'DISPONIVEL' && onCancelar && (
          <button
            onClick={() => onCancelar(anuncio.id)}
            disabled={isLoading}
            style={{
              padding: '8px 16px', background: '#fef2f2',
              color: '#ef4444', border: 'none', borderRadius: 8,
              fontSize: 13, fontWeight: 600, cursor: 'pointer',
            }}
          >
            Cancelar
          </button>
        )}
      </div>
    </div>
  )
}
