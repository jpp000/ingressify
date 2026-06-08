import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { eventoService, tipoIngressoService, avaliacaoService, revendaService, feedService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, formatDataBadge, corCategoria } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  descricao?: string
  imagemCapaUrl?: string
  status: string
  aberturaPortoes?: string
  categoria?: string
}

interface Lote {
  id: number
  numero: number
  nome: string
  preco: number
  quantidadeTotal: number
  quantidadeDisponivel: number
  dataInicio?: string
  dataFim?: string
  ativo: boolean
}

interface TipoIngresso {
  id: number
  nome: string
  preco: number
  quantidadeDisponivel: number
  descricao?: string
  lotes?: Lote[]
}

interface Avaliacao {
  id: number
  nota: number
  comentario?: string
  respostaOrganizador?: string
}

interface AnuncioRevenda {
  id: number
  preco: number
  status: string
  quantidade: number
  vendedorId: number
  compradorId: number | null
}

interface SelecaoIngresso {
  tipoId: number
  quantidade: number
}

interface Postagem {
  id: number
  eventoId: number
  autorId: number
  titulo: string
  conteudo: string
  imagemUrl?: string
  fixada: boolean
  criadaEm: string
}

export default function EventoDetalhe() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { usuario, isOrganizador } = useAuth()
  const eventoId = Number(id)

  const [evento, setEvento] = useState<Evento | null>(null)
  const [tipos, setTipos] = useState<TipoIngresso[]>([])
  const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([])
  const [revendas, setRevendas] = useState<AnuncioRevenda[]>([])
  const [postagens, setPostagens] = useState<Postagem[]>([])
  const [selecoes, setSelecoes] = useState<Record<number, number>>({})
  const [carregando, setCarregando] = useState(true)
  const [reservando, setReservando] = useState<number | null>(null)
  const [msgRevenda, setMsgRevenda] = useState('')
  const [novoPost, setNovoPost] = useState({ titulo: '', conteudo: '' })
  const [publicandoPost, setPublicandoPost] = useState(false)

  useEffect(() => {
    Promise.all([
      eventoService.detalhe(eventoId),
      tipoIngressoService.listar(eventoId),
      avaliacaoService.listar(eventoId),
      revendaService.listar(eventoId).catch(() => ({ data: [] })),
      feedService.listar(eventoId).catch(() => ({ data: [] })),
    ]).then(([evRes, tiposRes, avalRes, revRes, feedRes]) => {
      setEvento(evRes.data)
      setTipos(tiposRes.data)
      setAvaliacoes(avalRes.data)
      setRevendas(revRes.data)
      setPostagens(feedRes.data)
    }).finally(() => setCarregando(false))
  }, [eventoId])

  const publicarPost = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!usuario || !novoPost.titulo.trim() || !novoPost.conteudo.trim()) return
    setPublicandoPost(true)
    try {
      await feedService.criar(eventoId, usuario.id, {
        titulo: novoPost.titulo.trim(),
        conteudo: novoPost.conteudo.trim(),
        fixar: false,
      })
      setNovoPost({ titulo: '', conteudo: '' })
      const feedRes = await feedService.listar(eventoId)
      setPostagens(feedRes.data)
    } finally {
      setPublicandoPost(false)
    }
  }

  const reservarRevenda = async (anuncioId: number) => {
    if (!usuario) return
    setReservando(anuncioId)
    setMsgRevenda('')
    try {
      await revendaService.reservar(anuncioId, usuario.id)
      setMsgRevenda('Ingresso reservado! Vá para o Marketplace para confirmar a compra.')
      const r = await revendaService.listar(eventoId)
      setRevendas(r.data)
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsgRevenda(err.response?.data?.motivo ?? 'Erro ao reservar.')
    } finally {
      setReservando(null)
    }
  }

  const loteAtivoParaTipo = (tipo: TipoIngresso) =>
    tipo.lotes?.find(l => l.ativo) ?? null

  const maxParaTipo = (tipo: TipoIngresso) => {
    const lote = loteAtivoParaTipo(tipo)
    return lote ? lote.quantidadeDisponivel : tipo.quantidadeDisponivel
  }

  const alterarQtd = (tipoId: number, delta: number) => {
    setSelecoes(prev => {
      const atual = prev[tipoId] ?? 0
      const tipo = tipos.find(t => t.id === tipoId)
      const max = tipo ? maxParaTipo(tipo) : 0
      const nova = Math.max(0, Math.min(max, atual + delta))
      return { ...prev, [tipoId]: nova }
    })
  }

  const total = tipos.reduce((acc, t) => acc + (selecoes[t.id] ?? 0) * Number(t.preco), 0)
  const temSelecionado = Object.values(selecoes).some(q => q > 0)

  const irParaRevisao = () => {
    if (!temSelecionado || !evento) return
    const itens: SelecaoIngresso[] = Object.entries(selecoes)
      .filter(([, qtd]) => qtd > 0)
      .map(([tipoId, quantidade]) => ({ tipoId: Number(tipoId), quantidade }))
    navigate('/revisao', { state: { evento, tipos, itens } })
  }

  const imagemEvento = evento?.imagemCapaUrl ||
    'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1200&q=70'

  if (carregando) {
    return (
      <>
        <Navbar />
        <div style={{ textAlign: 'center', padding: '80px 0', color: '#94a3b8' }}>Carregando...</div>
      </>
    )
  }

  if (!evento) {
    return (
      <>
        <Navbar />
        <div style={{ textAlign: 'center', padding: '80px 0', color: '#ef4444' }}>Evento não encontrado.</div>
      </>
    )
  }

  const { dia, mes } = formatDataBadge(evento.dataHora)
  const dataObj = new Date(evento.dataHora)
  const horario = dataObj.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  const dataFormatada = dataObj.toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' })
  const horarioPortoes = evento.aberturaPortoes
    ? new Date(evento.aberturaPortoes).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
    : null

  return (
    <>
      <Navbar />

      {/* Hero */}
      <div style={{ position: 'relative', height: 340, overflow: 'hidden' }}>
        <img src={imagemEvento} alt={evento.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        <div style={{
          position: 'absolute',
          inset: 0,
          background: 'linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.3) 60%, transparent 100%)',
        }} />
        <div style={{ position: 'absolute', bottom: 32, left: 32 }}>
          {evento.categoria && (
            <span style={{
              display: 'inline-block',
              background: corCategoria(evento.categoria),
              color: '#fff',
              fontSize: 12,
              fontWeight: 700,
              padding: '4px 12px',
              borderRadius: 20,
              marginBottom: 10,
              textTransform: 'uppercase',
              letterSpacing: 0.5,
            }}>
              {evento.categoria}
            </span>
          )}
          <h1 style={{ color: '#fff', fontSize: 36, fontWeight: 800, margin: 0, textShadow: '0 2px 8px rgba(0,0,0,0.5)' }}>
            {evento.nome}
          </h1>
        </div>
      </div>

      {/* Atalhos rápidos */}
      <div style={{
        background: '#fff',
        borderBottom: '1px solid #e2e8f0',
        padding: '12px 24px',
      }}>
        <div style={{
          maxWidth: 1280, margin: '0 auto',
          display: 'flex', gap: 10, flexWrap: 'wrap',
        }}>
          {[
            { to: `/sorteios?eventoId=${eventoId}`, label: '🎲 Sorteios', cor: '#16a34a' },
            { to: `/revendas?eventoId=${eventoId}`, label: '🔄 Revendas', cor: '#d97706' },
            { to: `/mapa-assentos?eventoId=${eventoId}`, label: '💺 Mapa de Assentos', cor: '#7c3aed' },
            { to: `/check-in?eventoId=${eventoId}`, label: '🎫 Check-in', cor: '#0284c7' },
          ].map(item => (
            <Link
              key={item.to}
              to={item.to}
              style={{
                background: '#f8fafc',
                border: `1px solid ${item.cor}33`,
                color: item.cor,
                borderRadius: 20,
                padding: '6px 16px',
                fontSize: 13,
                fontWeight: 600,
                textDecoration: 'none',
              }}
            >
              {item.label}
            </Link>
          ))}
        </div>
      </div>

      {/* Conteúdo */}
      <div style={{ maxWidth: 1280, margin: '0 auto', padding: '32px 24px', display: 'flex', gap: 32 }}>

        {/* Esquerda */}
        <div style={{ flex: 1, minWidth: 0 }}>
          {/* Cards de info */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16, marginBottom: 32 }}>
            <InfoCard icon="📅" label="Data" value={dataFormatada} />
            <InfoCard icon="🕐" label="Horário" value={horario} extra={horarioPortoes ? `Portões ${horarioPortoes}` : undefined} />
            <InfoCard icon="📍" label="Local" value={evento.local} />
          </div>

          {/* Descrição */}
          {evento.descricao && (
            <section style={{ marginBottom: 32 }}>
              <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b', marginBottom: 16 }}>Sobre o Evento</h2>
              <div style={{ background: '#fff', borderRadius: 12, padding: 24, boxShadow: '0 1px 3px rgba(0,0,0,0.06)' }}>
                {evento.descricao.split('\n').map((linha, i) => (
                  <p key={i} style={{ color: '#475569', lineHeight: 1.7, marginBottom: 12 }}>{linha}</p>
                ))}
              </div>
            </section>
          )}

          {/* Localização */}
          <section style={{ marginBottom: 32 }}>
            <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b', marginBottom: 16 }}>Localização</h2>
            <div style={{ background: '#e2e8f0', borderRadius: 12, height: 220, display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
              <div style={{ textAlign: 'center', color: '#94a3b8' }}>
                <div style={{ fontSize: 40, marginBottom: 8 }}>🗺️</div>
                <p style={{ fontSize: 14, fontWeight: 500 }}>{evento.local}</p>
              </div>
            </div>
          </section>

          {/* Sorteio de Ingressos */}
          <section style={{ marginBottom: 32 }}>
            <div style={{
              background: '#fff', borderRadius: 12, padding: '20px 24px',
              boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
              display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 16,
              flexWrap: 'wrap',
            }}>
              <div>
                <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b', margin: '0 0 4px' }}>
                  🎟️ Sorteio de Ingressos
                </h2>
                <p style={{ color: '#64748b', fontSize: 14, margin: 0 }}>
                  Inscreva-se para concorrer a ingressos por sorteio justo.
                </p>
              </div>
              <Link
                to={`/sorteios?eventoId=${eventoId}`}
                style={{
                  background: '#16a34a', color: '#fff', borderRadius: 8,
                  padding: '10px 20px', fontSize: 14, fontWeight: 600,
                  textDecoration: 'none', whiteSpace: 'nowrap',
                }}
              >
                Ver Sorteios →
              </Link>
            </div>
          </section>

          {/* Feed do Evento */}
          <section style={{ marginBottom: 32 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b' }}>📢 Feed do Evento</h2>
              <span style={{ fontSize: 13, color: '#64748b' }}>{postagens.length} publicação(ões)</span>
            </div>

            {isOrganizador() && (
              <form onSubmit={publicarPost} style={{
                background: '#fff', borderRadius: 12, padding: 20,
                boxShadow: '0 1px 3px rgba(0,0,0,0.06)', marginBottom: 16,
              }}>
                <input
                  placeholder="Título da publicação"
                  value={novoPost.titulo}
                  onChange={e => setNovoPost(p => ({ ...p, titulo: e.target.value }))}
                  style={{
                    width: '100%', padding: '10px 14px', marginBottom: 10,
                    border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14, outline: 'none',
                  }}
                />
                <textarea
                  placeholder="Conteúdo da publicação..."
                  value={novoPost.conteudo}
                  onChange={e => setNovoPost(p => ({ ...p, conteudo: e.target.value }))}
                  rows={3}
                  style={{
                    width: '100%', padding: '10px 14px', marginBottom: 10,
                    border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14,
                    outline: 'none', resize: 'vertical', fontFamily: 'inherit',
                  }}
                />
                <button
                  type="submit"
                  disabled={publicandoPost || !novoPost.titulo.trim() || !novoPost.conteudo.trim()}
                  style={{
                    background: '#1d4ed8', color: '#fff', border: 'none',
                    borderRadius: 8, padding: '8px 20px', fontSize: 13, fontWeight: 600,
                    cursor: publicandoPost ? 'wait' : 'pointer',
                    opacity: !novoPost.titulo.trim() || !novoPost.conteudo.trim() ? 0.5 : 1,
                  }}
                >
                  {publicandoPost ? 'Publicando...' : 'Publicar'}
                </button>
              </form>
            )}

            {postagens.length === 0 ? (
              <div style={{
                background: '#f8fafc', borderRadius: 12, padding: '32px 24px',
                textAlign: 'center', color: '#94a3b8', border: '1px dashed #e2e8f0',
              }}>
                Nenhuma publicação no feed ainda.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {postagens.map(p => (
                  <div key={p.id} style={{
                    background: '#fff', borderRadius: 12, padding: 20,
                    boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
                    borderLeft: p.fixada ? '4px solid #1d4ed8' : '4px solid transparent',
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                      <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', margin: 0 }}>{p.titulo}</h3>
                      {p.fixada && (
                        <span style={{ fontSize: 11, background: '#eff6ff', color: '#1d4ed8', padding: '2px 8px', borderRadius: 12, fontWeight: 600 }}>
                          Fixado
                        </span>
                      )}
                    </div>
                    <p style={{ color: '#475569', fontSize: 14, lineHeight: 1.6, margin: '0 0 8px' }}>{p.conteudo}</p>
                    <p style={{ fontSize: 12, color: '#94a3b8', margin: 0 }}>
                      {new Date(p.criadaEm).toLocaleString('pt-BR')}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Marketplace de Revendas */}
          <section style={{ marginBottom: 32 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
              <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b' }}>
                Revendas Disponíveis
                <span style={{ marginLeft: 8, fontSize: 14, fontWeight: 600, color: '#16a34a', background: '#f0fdf4', borderRadius: 20, padding: '2px 10px' }}>
                  {revendas.filter(r => r.status === 'DISPONIVEL').length}
                </span>
              </h2>
              <Link to={`/revendas?eventoId=${eventoId}`} style={{ color: '#1d4ed8', fontSize: 13, fontWeight: 600 }}>
                Ver marketplace →
              </Link>
            </div>
            {msgRevenda && (
              <div style={{
                padding: '10px 14px', borderRadius: 8, marginBottom: 12,
                background: msgRevenda.includes('Erro') ? '#fef2f2' : '#f0fdf4',
                color: msgRevenda.includes('Erro') ? '#dc2626' : '#16a34a',
                fontSize: 13, fontWeight: 600,
              }}>
                {msgRevenda}
              </div>
            )}
            {revendas.filter(r => r.status === 'DISPONIVEL').length === 0 ? (
              <div style={{
                background: '#f8fafc', borderRadius: 12, padding: '24px',
                textAlign: 'center', color: '#94a3b8', border: '1px dashed #e2e8f0',
              }}>
                <p style={{ margin: '0 0 8px' }}>Nenhuma revenda disponível para este evento.</p>
                <Link to={`/revendas?eventoId=${eventoId}`} style={{ color: '#1d4ed8', fontSize: 13, fontWeight: 600 }}>
                  Ver marketplace de revendas →
                </Link>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {revendas.filter(r => r.status === 'DISPONIVEL').slice(0, 3).map(r => (
                  <div
                    key={r.id}
                    style={{
                      background: '#fff', borderRadius: 12, padding: '14px 18px',
                      boxShadow: '0 1px 3px rgba(0,0,0,0.06)',
                      display: 'flex', alignItems: 'center', gap: 14,
                    }}
                  >
                    <div style={{
                      width: 40, height: 40, borderRadius: 8,
                      background: '#f0fdf4', display: 'flex', alignItems: 'center',
                      justifyContent: 'center', fontSize: 18, flexShrink: 0,
                    }}>
                      🎟️
                    </div>
                    <div style={{ flex: 1 }}>
                      <p style={{ fontWeight: 700, color: '#1e293b', fontSize: 14 }}>
                        {r.quantidade} ingresso(s)
                      </p>
                      <p style={{ fontSize: 12, color: '#64748b' }}>Vendedor #{r.vendedorId}</p>
                    </div>
                    <span style={{ fontSize: 18, fontWeight: 800, color: '#1d4ed8' }}>
                      {formatMoeda(r.preco)}
                    </span>
                    {usuario && r.vendedorId !== usuario.id && (
                      <button
                        onClick={() => reservarRevenda(r.id)}
                        disabled={reservando === r.id}
                        style={{
                          padding: '8px 16px', background: '#16a34a',
                          color: '#fff', border: 'none', borderRadius: 8,
                          fontSize: 13, fontWeight: 600, cursor: 'pointer',
                        }}
                      >
                        {reservando === r.id ? '...' : 'Reservar'}
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Avaliações */}
          {avaliacoes.length > 0 && (
            <section>
              <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b', marginBottom: 16 }}>Avaliações</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                {avaliacoes.slice(0, 4).map(a => (
                  <div key={a.id} style={{ background: '#fff', borderRadius: 12, padding: 16, boxShadow: '0 1px 3px rgba(0,0,0,0.06)' }}>
                    <div style={{ display: 'flex', gap: 4, marginBottom: 6 }}>
                      {Array.from({ length: 5 }).map((_, i) => (
                        <span key={i} style={{ color: i < a.nota ? '#f59e0b' : '#e2e8f0', fontSize: 16 }}>★</span>
                      ))}
                    </div>
                    {a.comentario && <p style={{ color: '#475569', fontSize: 14, lineHeight: 1.5 }}>{a.comentario}</p>}
                    {a.respostaOrganizador && (
                      <div style={{ marginTop: 10, padding: '10px 14px', background: '#f0f9ff', borderLeft: '3px solid #0284c7', borderRadius: 4 }}>
                        <p style={{ fontSize: 13, color: '#0369a1', fontStyle: 'italic' }}>
                          <strong>Organizador:</strong> {a.respostaOrganizador}
                        </p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </section>
          )}
        </div>

        {/* Direita — Seletor de ingressos */}
        <div style={{ width: 320, flexShrink: 0 }}>
          <div style={{ background: '#fff', borderRadius: 16, padding: 24, boxShadow: '0 4px 16px rgba(0,0,0,0.08)', position: 'sticky', top: 80 }}>
            <h3 style={{ fontSize: 18, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Selecionar Ingressos</h3>

            {tipos.length === 0 ? (
              <p style={{ color: '#94a3b8', fontSize: 14, textAlign: 'center', padding: '16px 0' }}>
                Nenhum tipo de ingresso disponível.
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {tipos.map(t => {
                  const qtd = selecoes[t.id] ?? 0
                  const esgotado = t.quantidadeDisponivel === 0
                  const lote = loteAtivoParaTipo(t)
                  const max = maxParaTipo(t)
                  return (
                    <div key={t.id} style={{ borderBottom: '1px solid #f1f5f9', paddingBottom: 16 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
                        <div>
                          <p style={{ fontWeight: 700, color: '#1e293b', fontSize: 15 }}>{t.nome}</p>
                          <p style={{ color: '#1d4ed8', fontWeight: 700, fontSize: 14 }}>{formatMoeda(t.preco)}</p>
                          {lote && (
                            <p style={{ fontSize: 11, color: '#7c3aed', background: '#f5f3ff', display: 'inline-block', padding: '2px 8px', borderRadius: 12, marginTop: 3 }}>
                              {lote.nome} · {lote.quantidadeDisponivel} restante{lote.quantidadeDisponivel !== 1 ? 's' : ''}
                            </p>
                          )}
                          {t.descricao && (
                            <p style={{ color: '#94a3b8', fontSize: 12, marginTop: 2 }}>ℹ️ {t.descricao}</p>
                          )}
                        </div>
                        {esgotado ? (
                          <span style={{ background: '#fee2e2', color: '#ef4444', fontSize: 12, fontWeight: 600, padding: '4px 10px', borderRadius: 20 }}>
                            Esgotado
                          </span>
                        ) : (
                          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                            <button
                              onClick={() => alterarQtd(t.id, -1)}
                              disabled={qtd === 0}
                              style={{
                                width: 28, height: 28, borderRadius: '50%',
                                border: '1px solid #e2e8f0', background: qtd === 0 ? '#f8fafc' : '#fff',
                                color: qtd === 0 ? '#cbd5e1' : '#1e293b',
                                fontSize: 18, fontWeight: 700,
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                              }}
                            >
                              −
                            </button>
                            <span style={{ fontWeight: 700, fontSize: 16, minWidth: 20, textAlign: 'center' }}>{qtd}</span>
                            <button
                              onClick={() => alterarQtd(t.id, 1)}
                              disabled={qtd >= max}
                              style={{
                                width: 28, height: 28, borderRadius: '50%',
                                border: 'none', background: qtd >= max ? '#e2e8f0' : '#1d4ed8',
                                color: '#fff', fontSize: 18, fontWeight: 700,
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                              }}
                            >
                              +
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  )
                })}
              </div>
            )}

            <div style={{ marginTop: 20, paddingTop: 16, borderTop: '2px solid #f1f5f9' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <span style={{ color: '#64748b', fontSize: 14 }}>Total</span>
                <span style={{ color: '#1d4ed8', fontWeight: 800, fontSize: 22 }}>{formatMoeda(total)}</span>
              </div>
              <button
                onClick={irParaRevisao}
                disabled={!temSelecionado}
                style={{
                  width: '100%',
                  background: temSelecionado ? '#1d4ed8' : '#e2e8f0',
                  color: temSelecionado ? '#fff' : '#94a3b8',
                  border: 'none',
                  borderRadius: 10,
                  padding: '14px',
                  fontSize: 16,
                  fontWeight: 700,
                  cursor: temSelecionado ? 'pointer' : 'default',
                  transition: 'background 0.15s',
                }}
              >
                Comprar Ingressos
              </button>
              <p style={{ textAlign: 'center', fontSize: 12, color: '#94a3b8', marginTop: 10 }}>
                Transação segura via Ingressefy Pay
              </p>
            </div>
          </div>

          {/* Badge avaliação */}
          {avaliacoes.length > 0 && (
            <div style={{ marginTop: 16, background: '#fff', borderRadius: 12, padding: '12px 16px', boxShadow: '0 1px 3px rgba(0,0,0,0.06)', display: 'flex', alignItems: 'center', gap: 8 }}>
              <span style={{ fontSize: 20 }}>⭐</span>
              <div>
                <p style={{ fontWeight: 700, fontSize: 14, color: '#1e293b' }}>
                  {(avaliacoes.reduce((s, a) => s + a.nota, 0) / avaliacoes.length).toFixed(1)} / 5.0
                </p>
                <p style={{ fontSize: 12, color: '#64748b' }}>{avaliacoes.length} avaliações</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Badge data flutuante (mobile) */}
      <div style={{ display: 'none' }}>
        <span>{dia}/{mes}</span>
      </div>
    </>
  )
}

function InfoCard({ icon, label, value, extra }: { icon: string; label: string; value: string; extra?: string }) {
  return (
    <div style={{ background: '#fff', borderRadius: 12, padding: '16px 20px', boxShadow: '0 1px 3px rgba(0,0,0,0.06)', display: 'flex', alignItems: 'flex-start', gap: 14 }}>
      <div style={{ background: '#eff6ff', borderRadius: 8, width: 40, height: 40, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18, flexShrink: 0 }}>
        {icon}
      </div>
      <div>
        <p style={{ fontSize: 11, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.5 }}>{label}</p>
        <p style={{ fontWeight: 700, color: '#1e293b', fontSize: 15, marginTop: 2 }}>{value}</p>
        {extra && <p style={{ fontSize: 12, color: '#64748b', marginTop: 2 }}>{extra}</p>}
      </div>
    </div>
  )
}
