import { useEffect, useState, useMemo } from 'react'
import { useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import { revendaService, eventoService, saldoService } from '../services/api'
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

const MOTIVOS_DENUNCIA = [
  { value: 'PRECO_ABUSIVO', label: 'Preço abusivo' },
  { value: 'INGRESSO_SUSPEITO', label: 'Ingresso suspeito' },
  { value: 'COMPORTAMENTO_INADEQUADO', label: 'Comportamento inadequado' },
  { value: 'OUTRO', label: 'Outro' },
]

export default function RevendasPage() {
  const { usuario } = useAuth()
  const [searchParams] = useSearchParams()
  const eventoIdParam = searchParams.get('eventoId')

  const [anuncios, setAnuncios] = useState<Anuncio[]>([])
  const [eventosMap, setEventosMap] = useState<Map<number, Evento>>(new Map())
  const [filtroNome, setFiltroNome] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [msgGlobal, setMsgGlobal] = useState('')
  const [action, setAction] = useState<ActionState>({ type: 'idle' })
  const [denunciaAberta, setDenunciaAberta] = useState<number | null>(null)
  const [denunciaForm, setDenunciaForm] = useState({ motivo: 'PRECO_ABUSIVO', descricao: '' })
  const [denunciando, setDenunciando] = useState(false)

  const carregar = async () => {
    setCarregando(true)
    setMsgGlobal('')
    try {
      const res = await revendaService.todos().catch(() => ({ data: [] }))
      const todos: Anuncio[] = res.data

      const idsUnicos = [...new Set(todos.map(a => a.eventoId))]
      const pares = await Promise.all(
        idsUnicos.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, Evento]))
      )
      const mapa = new Map<number, Evento>(pares)

      setAnuncios(todos)
      setEventosMap(mapa)

      if (eventoIdParam) {
        const ev = mapa.get(Number(eventoIdParam))
        if (ev) setFiltroNome(ev.nome)
      }
    } catch {
      setMsgGlobal('Erro ao carregar anúncios.')
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => { carregar() }, [])

  const anunciosFiltrados = useMemo(() => {
    if (!filtroNome.trim()) return anuncios
    const termo = filtroNome.toLowerCase()
    return anuncios.filter(a => {
      const nome = eventosMap.get(a.eventoId)?.nome ?? ''
      return nome.toLowerCase().includes(termo)
    })
  }, [anuncios, eventosMap, filtroNome])

  const disponiveis = anunciosFiltrados.filter(a => a.status === 'DISPONIVEL')
  const reservados = anunciosFiltrados.filter(a => a.status === 'RESERVADO')
  const minhaReserva = anunciosFiltrados.find(a => a.status === 'RESERVADO' && a.compradorId === usuario?.id)

  const reservar = async (anuncioId: number) => {
    if (!usuario) return
    const anuncio = anuncios.find(a => a.id === anuncioId)
    if (!anuncio) return
    setAction({ type: 'loading', id: anuncioId })
    try {
      const saldoRes = await saldoService.obter(usuario.id)
      const saldoAtual = Number(saldoRes.data.valor)
      if (saldoAtual < anuncio.preco) {
        setAction({
          type: 'error',
          id: anuncioId,
          msg: `Saldo insuficiente. Seu saldo é ${formatMoeda(saldoAtual)} e o valor cobrado é ${formatMoeda(anuncio.preco)}.`,
        })
        return
      }
      await revendaService.reservar(anuncioId, usuario.id)
      setAction({ type: 'success', id: anuncioId, msg: 'Reservado! Confirme a compra para finalizar.' })
      carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setAction({ type: 'error', id: anuncioId, msg: err.response?.data?.motivo ?? 'Erro ao reservar.' })
    }
  }

  const confirmar = async (anuncioId: number) => {
    if (!usuario) return
    setAction({ type: 'loading', id: anuncioId })
    try {
      await revendaService.confirmar(anuncioId, usuario.id)
      setAction({ type: 'success', id: anuncioId, msg: 'Compra confirmada! O ingresso já está em Meus Ingressos.' })
      carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setAction({ type: 'error', id: anuncioId, msg: err.response?.data?.motivo ?? 'Erro ao confirmar.' })
    }
  }

  const denunciar = async (anuncioId: number) => {
    if (!usuario) return
    setDenunciando(true)
    try {
      await revendaService.denunciar(anuncioId, usuario.id, {
        motivo: denunciaForm.motivo,
        descricao: denunciaForm.descricao.trim() || 'Denúncia registrada pelo comprador.',
      })
      setDenunciaAberta(null)
      setDenunciaForm({ motivo: 'PRECO_ABUSIVO', descricao: '' })
      setAction({ type: 'success', id: anuncioId, msg: 'Denúncia enviada com sucesso. Nossa equipe irá analisar.' })
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setAction({ type: 'error', id: anuncioId, msg: err.response?.data?.motivo ?? 'Erro ao enviar denúncia.' })
    } finally {
      setDenunciando(false)
    }
  }

  const cancelar = async (anuncioId: number) => {
    if (!usuario) return
    if (!confirm('Cancelar este anúncio? Seu ingresso voltará para a sua carteira.')) return
    setAction({ type: 'loading', id: anuncioId })
    try {
      await revendaService.cancelar(anuncioId, usuario.id)
      setAction({ type: 'idle' })
      carregar()
    } catch {
      setAction({ type: 'error', id: anuncioId, msg: 'Erro ao cancelar anúncio.' })
    }
  }

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: '40px 24px' }}>

        <h1 style={{ fontSize: 28, fontWeight: 800, color: '#1e293b', marginBottom: 6 }}>
          Marketplace de Revendas
        </h1>
        <p style={{ color: '#64748b', fontSize: 14, marginBottom: 28 }}>
          Ingressos de compradores que não podem mais ir — transferência segura e garantida.
        </p>

        {/* Filtro por nome de evento */}
        <div style={{
          background: '#fff', borderRadius: 16, padding: '16px 20px',
          boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 28,
          display: 'flex', alignItems: 'center', gap: 12,
        }}>
          <span style={{ fontSize: 18, flexShrink: 0 }}>🔍</span>
          <input
            type="text"
            placeholder="Filtrar por nome do evento..."
            value={filtroNome}
            onChange={e => setFiltroNome(e.target.value)}
            style={{
              flex: 1, padding: '10px 14px',
              border: '1px solid #e2e8f0', borderRadius: 8,
              fontSize: 14, outline: 'none',
            }}
          />
          {filtroNome && (
            <button
              onClick={() => setFiltroNome('')}
              style={{
                background: '#f1f5f9', border: 'none', borderRadius: 8,
                padding: '10px 16px', fontSize: 13, color: '#64748b',
                cursor: 'pointer', fontWeight: 600, whiteSpace: 'nowrap',
              }}
            >
              Limpar
            </button>
          )}
        </div>

        {msgGlobal && (
          <div style={{ background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 10, padding: '12px 16px', marginBottom: 24, color: '#dc2626', fontSize: 14 }}>
            {msgGlobal}
          </div>
        )}

        {carregando ? (
          <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>Carregando anúncios...</div>
        ) : (
          <>
            {/* Minha reserva pendente */}
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
                    {eventosMap.get(minhaReserva.eventoId)?.nome ?? `Evento #${minhaReserva.eventoId}`}
                    {' · '}{minhaReserva.quantidade} ingresso(s) · {formatMoeda(minhaReserva.preco)}
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

            {/* Disponíveis */}
            <section style={{ marginBottom: 32 }}>
              <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', marginBottom: 12 }}>
                Disponíveis ({disponiveis.length})
              </h2>
              {disponiveis.length === 0 ? (
                <div style={{
                  textAlign: 'center', padding: '48px 24px',
                  background: '#f8fafc', borderRadius: 16,
                  color: '#94a3b8', border: '1px dashed #e2e8f0',
                }}>
                  <div style={{ fontSize: 40, marginBottom: 12 }}>🎟️</div>
                  <p style={{ fontWeight: 600, marginBottom: 4 }}>
                    {filtroNome ? `Nenhum anúncio para "${filtroNome}"` : 'Nenhum anúncio disponível no momento'}
                  </p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {disponiveis.map(a => (
                    <AnuncioCard
                      key={a.id}
                      anuncio={a}
                      evento={eventosMap.get(a.eventoId)}
                      isMeu={a.vendedorId === usuario?.id}
                      action={action}
                      onReservar={reservar}
                      onCancelar={cancelar}
                      onDenunciar={id => setDenunciaAberta(id)}
                      currentUserId={usuario?.id}
                    />
                  ))}
                </div>
              )}
            </section>

            {/* Modal de denúncia */}
            {denunciaAberta !== null && (
              <div style={{
                position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)',
                display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 200,
              }}>
                <div style={{
                  background: '#fff', borderRadius: 16, padding: 28,
                  width: '100%', maxWidth: 440, margin: 16,
                  boxShadow: '0 16px 48px rgba(0,0,0,0.2)',
                }}>
                  <h3 style={{ fontSize: 18, fontWeight: 700, color: '#1e293b', margin: '0 0 16px' }}>
                    🚨 Denunciar Anúncio
                  </h3>
                  <label style={{ display: 'block', fontSize: 13, color: '#64748b', marginBottom: 6 }}>Motivo</label>
                  <select
                    value={denunciaForm.motivo}
                    onChange={e => setDenunciaForm(f => ({ ...f, motivo: e.target.value }))}
                    style={{
                      width: '100%', padding: '10px 14px', marginBottom: 14,
                      border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14,
                    }}
                  >
                    {MOTIVOS_DENUNCIA.map(m => (
                      <option key={m.value} value={m.value}>{m.label}</option>
                    ))}
                  </select>
                  <label style={{ display: 'block', fontSize: 13, color: '#64748b', marginBottom: 6 }}>Descrição (opcional)</label>
                  <textarea
                    value={denunciaForm.descricao}
                    onChange={e => setDenunciaForm(f => ({ ...f, descricao: e.target.value }))}
                    placeholder="Descreva o problema..."
                    rows={3}
                    style={{
                      width: '100%', padding: '10px 14px', marginBottom: 20,
                      border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14,
                      resize: 'vertical', fontFamily: 'inherit', outline: 'none',
                    }}
                  />
                  <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
                    <button
                      onClick={() => { setDenunciaAberta(null); setDenunciaForm({ motivo: 'PRECO_ABUSIVO', descricao: '' }) }}
                      style={{
                        background: '#f1f5f9', color: '#64748b', border: 'none',
                        borderRadius: 8, padding: '10px 20px', fontSize: 14, fontWeight: 600, cursor: 'pointer',
                      }}
                    >
                      Cancelar
                    </button>
                    <button
                      onClick={() => denunciar(denunciaAberta)}
                      disabled={denunciando}
                      style={{
                        background: '#dc2626', color: '#fff', border: 'none',
                        borderRadius: 8, padding: '10px 20px', fontSize: 14, fontWeight: 600,
                        cursor: denunciando ? 'wait' : 'pointer',
                      }}
                    >
                      {denunciando ? 'Enviando...' : 'Enviar Denúncia'}
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Reservados */}
            {reservados.length > 0 && (
              <section>
                <h2 style={{ fontSize: 16, fontWeight: 700, color: '#94a3b8', marginBottom: 12 }}>
                  Reservados ({reservados.length})
                </h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {reservados.map(a => (
                    <AnuncioCard
                      key={a.id}
                      anuncio={a}
                      evento={eventosMap.get(a.eventoId)}
                      isMeu={a.vendedorId === usuario?.id}
                      action={action}
                      onCancelar={cancelar}
                      currentUserId={usuario?.id}
                    />
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </div>
    </>
  )
}

function AnuncioCard({
  anuncio, evento, isMeu, action, currentUserId, onReservar, onCancelar, onDenunciar,
}: {
  anuncio: Anuncio
  evento?: Evento
  isMeu: boolean
  action: ActionState
  currentUserId?: number
  onReservar?: (id: number) => void
  onCancelar?: (id: number) => void
  onDenunciar?: (id: number) => void
}) {
  const isLoading = action.type === 'loading' && action.id === anuncio.id
  const reservado = anuncio.status === 'RESERVADO'
  const nomeEvento = evento?.nome ?? `Evento #${anuncio.eventoId}`
  const dataEvento = evento?.dataHora
    ? new Date(evento.dataHora).toLocaleDateString('pt-BR', { day: 'numeric', month: 'short', year: 'numeric' })
    : ''

  return (
    <div style={{
      background: '#fff', border: `1px solid ${reservado ? '#fde68a' : '#e2e8f0'}`,
      borderRadius: 12, padding: '16px 20px',
      display: 'flex', alignItems: 'center', gap: 16,
      opacity: reservado && !isMeu ? 0.7 : 1,
    }}>
      {evento?.imagemCapaUrl ? (
        <img
          src={evento.imagemCapaUrl}
          alt={nomeEvento}
          style={{ width: 52, height: 52, borderRadius: 8, objectFit: 'cover', flexShrink: 0 }}
        />
      ) : (
        <div style={{
          width: 52, height: 52, borderRadius: 8,
          background: isMeu ? '#eff6ff' : '#f0fdf4',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 22, flexShrink: 0,
        }}>
          🎟️
        </div>
      )}

      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 2, flexWrap: 'wrap' }}>
          <span style={{ fontWeight: 700, color: '#1e293b', fontSize: 15 }}>{nomeEvento}</span>
          <span style={{
            fontSize: 11, fontWeight: 700, padding: '2px 8px', borderRadius: 20,
            background: reservado ? '#fef3c7' : '#dcfce7',
            color: reservado ? '#b45309' : '#16a34a',
            flexShrink: 0,
          }}>
            {reservado ? 'Reservado' : 'Disponível'}
          </span>
          {isMeu && (
            <span style={{ fontSize: 11, color: '#94a3b8', fontStyle: 'italic' }}>Seu anúncio</span>
          )}
        </div>
        <p style={{ fontSize: 12, color: '#64748b' }}>
          {dataEvento && `📅 ${dataEvento} · `}
          {evento?.local && `📍 ${evento.local} · `}
          🎟️ {anuncio.quantidade} ingresso(s)
        </p>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexShrink: 0 }}>
        <span style={{ fontSize: 20, fontWeight: 800, color: '#1d4ed8' }}>
          {formatMoeda(anuncio.preco)}
        </span>
        {!reservado && !isMeu && onReservar && currentUserId && (
          <>
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
            {onDenunciar && (
              <button
                onClick={() => onDenunciar(anuncio.id)}
                title="Denunciar anúncio"
                style={{
                  padding: '8px 12px', background: '#fef2f2',
                  color: '#dc2626', border: 'none', borderRadius: 8,
                  fontSize: 13, fontWeight: 600, cursor: 'pointer',
                }}
              >
                🚨
              </button>
            )}
          </>
        )}
        {isMeu && !reservado && onCancelar && (
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
