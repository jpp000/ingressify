import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import {
  Calendar, Clock, MapPin, Star, Dices, Repeat, Armchair, Megaphone, Ticket,
  Minus, Plus, Lock, Pin, ArrowRight, Music2, Drama, Trophy, Mic, Disc3, UtensilsCrossed, type LucideIcon,
} from 'lucide-react'
import { eventoService, tipoIngressoService, avaliacaoService, revendaService, feedService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, corCategoria } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Evento { id: number; nome: string; dataHora: string; local: string; descricao?: string; imagemCapaUrl?: string; status: string; aberturaPortoes?: string; categoria?: string }
interface Lote { id: number; nome: string; preco: number; quantidadeDisponivel: number; ativo: boolean }
interface TipoIngresso { id: number; nome: string; preco: number; quantidadeDisponivel: number; descricao?: string; lotes?: Lote[] }
interface Avaliacao { id: number; nota: number; comentario?: string; respostaOrganizador?: string }
interface AnuncioRevenda { id: number; preco: number; status: string; quantidade: number; vendedorId: number; compradorId: number | null }
interface Postagem { id: number; titulo: string; conteudo: string; fixada: boolean; criadaEm: string }

const ICONE_CATEGORIA: Record<string, LucideIcon> = {
  'Música': Music2, 'Teatro e Artes': Drama, 'Esportes': Trophy, 'Conferências': Mic, 'Vida Noturna': Disc3, 'Comida e Bebida': UtensilsCrossed,
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
    ]).then(([ev, ti, av, rev, feed]) => {
      setEvento(ev.data); setTipos(ti.data); setAvaliacoes(av.data); setRevendas(rev.data); setPostagens(feed.data)
    }).finally(() => setCarregando(false))
  }, [eventoId])

  const publicarPost = async (e: FormEvent) => {
    e.preventDefault()
    if (!usuario || !novoPost.titulo.trim() || !novoPost.conteudo.trim()) return
    setPublicandoPost(true)
    try {
      await feedService.criar(eventoId, usuario.id, { titulo: novoPost.titulo.trim(), conteudo: novoPost.conteudo.trim(), fixar: false })
      setNovoPost({ titulo: '', conteudo: '' })
      setPostagens((await feedService.listar(eventoId)).data)
    } finally { setPublicandoPost(false) }
  }

  const reservarRevenda = async (anuncioId: number) => {
    if (!usuario) return
    setReservando(anuncioId); setMsgRevenda('')
    try {
      await revendaService.reservar(anuncioId, usuario.id)
      setMsgRevenda('Ingresso reservado! Confirme a compra no marketplace de revendas.')
      setRevendas((await revendaService.listar(eventoId)).data)
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsgRevenda(err.response?.data?.motivo ?? 'Erro ao reservar.')
    } finally { setReservando(null) }
  }

  const loteAtivo = (t: TipoIngresso) => t.lotes?.find(l => l.ativo) ?? null
  const maxParaTipo = (t: TipoIngresso) => { const l = loteAtivo(t); return l ? l.quantidadeDisponivel : t.quantidadeDisponivel }
  const alterarQtd = (tipoId: number, delta: number) => setSelecoes(prev => {
    const tipo = tipos.find(t => t.id === tipoId); const max = tipo ? maxParaTipo(tipo) : 0
    return { ...prev, [tipoId]: Math.max(0, Math.min(max, (prev[tipoId] ?? 0) + delta)) }
  })

  const total = tipos.reduce((acc, t) => acc + (selecoes[t.id] ?? 0) * Number(t.preco), 0)
  const temSelecionado = Object.values(selecoes).some(q => q > 0)
  const mediaAval = avaliacoes.length ? avaliacoes.reduce((s, a) => s + a.nota, 0) / avaliacoes.length : 0

  const irParaRevisao = () => {
    if (!temSelecionado || !evento) return
    const itens = Object.entries(selecoes).filter(([, q]) => q > 0).map(([tipoId, quantidade]) => ({ tipoId: Number(tipoId), quantidade }))
    navigate('/revisao', { state: { evento, tipos, itens } })
  }

  if (carregando) return (<><Navbar /><div className="app-container page"><div className="skeleton" style={{ height: 300, borderRadius: 'var(--r-lg)' }} /></div></>)
  if (!evento) return (<><Navbar /><div className="app-container page"><div className="empty"><Ticket size={40} /><h3>Evento não encontrado</h3></div></div></>)

  const dataObj = new Date(evento.dataHora)
  const horario = dataObj.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
  const dataFormatada = dataObj.toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' })
  const portoes = evento.aberturaPortoes ? new Date(evento.aberturaPortoes).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' }) : null
  const cor = corCategoria(evento.categoria)
  const Icone = ICONE_CATEGORIA[evento.categoria ?? ''] ?? Ticket
  const revendasDisp = revendas.filter(r => r.status === 'DISPONIVEL')

  return (
    <>
      <Navbar />

      <header className="ev-hero">
        <div className="ev-hero__bg" style={{ background: `linear-gradient(140deg, ${cor} 0%, color-mix(in oklab, ${cor} 48%, black) 100%)` }}>
          {evento.imagemCapaUrl && <img src={evento.imagemCapaUrl} alt="" />}
        </div>
        <Icone size={150} className="ev-hero__ico" />
        <div className="ev-hero__scrim" />
        <div className="app-container ev-hero__inner">
          {evento.categoria && <span className="badge" style={{ alignSelf: 'flex-start', background: 'oklch(1 0 0 / 0.92)', color: cor }}>{evento.categoria}</span>}
          <h1 className="ev-hero__title">{evento.nome}</h1>
          <div className="ev-hero__meta">
            <span><Calendar size={16} /> {dataFormatada}</span>
            <span><Clock size={16} /> {horario}{portoes ? ` · portões ${portoes}` : ''}</span>
            <span><MapPin size={16} /> {evento.local}</span>
            {mediaAval > 0 && <span><Star size={16} fill="currentColor" /> {mediaAval.toFixed(1)} ({avaliacoes.length})</span>}
          </div>
        </div>
      </header>

      <div className="app-container page two-col">
        {/* Esquerda */}
        <div className="stack" style={{ gap: 'var(--sp-6)', minWidth: 0 }}>
          <div className="ctx-links">
            <Link to={`/sorteios?eventoId=${eventoId}`} className="chip"><Dices size={15} /> Sorteios</Link>
            <Link to={`/revendas?eventoId=${eventoId}`} className="chip"><Repeat size={15} /> Revendas</Link>
            <Link to={`/mapa-assentos?eventoId=${eventoId}`} className="chip"><Armchair size={15} /> Mapa de assentos</Link>
          </div>

          {evento.descricao && (
            <section>
              <h2 style={{ marginBottom: 'var(--sp-3)' }}>Sobre o evento</h2>
              <div className="surface surface--pad secondary" style={{ lineHeight: 1.7 }}>
                {evento.descricao.split('\n').map((l, i) => <p key={i} style={{ marginBottom: 8 }}>{l}</p>)}
              </div>
            </section>
          )}

          {/* Feed */}
          <section>
            <div className="between" style={{ marginBottom: 'var(--sp-3)' }}>
              <h2 className="row" style={{ gap: 8 }}><Megaphone size={22} /> Feed do evento</h2>
              <span className="muted" style={{ fontSize: '0.875rem' }}>{postagens.length} publicação(ões)</span>
            </div>
            {isOrganizador() && (
              <form onSubmit={publicarPost} className="surface surface--pad stack" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-3)' }}>
                <input className="input" placeholder="Título da publicação" value={novoPost.titulo} onChange={e => setNovoPost(p => ({ ...p, titulo: e.target.value }))} />
                <textarea className="textarea" placeholder="Conteúdo…" value={novoPost.conteudo} onChange={e => setNovoPost(p => ({ ...p, conteudo: e.target.value }))} />
                <button className="btn btn--sm" style={{ alignSelf: 'flex-start' }} disabled={publicandoPost || !novoPost.titulo.trim() || !novoPost.conteudo.trim()}>
                  {publicandoPost ? 'Publicando…' : 'Publicar'}
                </button>
              </form>
            )}
            {postagens.length === 0 ? (
              <div className="surface surface--flat" style={{ borderStyle: 'dashed' }}><div className="empty" style={{ padding: 'var(--sp-6)' }}><Megaphone size={32} /><p className="muted">Nenhuma publicação ainda.</p></div></div>
            ) : (
              <div className="stack" style={{ gap: 'var(--sp-3)' }}>
                {postagens.map(p => (
                  <div key={p.id} className={`surface post${p.fixada ? ' post--fix' : ''}`}>
                    <div className="row" style={{ gap: 8, marginBottom: 6 }}>
                      <h3 style={{ fontSize: '1.05rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>{p.titulo}</h3>
                      {p.fixada && <span className="badge badge--brand"><Pin size={12} /> Fixado</span>}
                    </div>
                    <p className="secondary" style={{ marginBottom: 6 }}>{p.conteudo}</p>
                    <p className="muted" style={{ fontSize: '0.8125rem' }}>{new Date(p.criadaEm).toLocaleString('pt-BR')}</p>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Revendas */}
          <section>
            <div className="between" style={{ marginBottom: 'var(--sp-3)' }}>
              <h2 className="row" style={{ gap: 8 }}>Revendas disponíveis {revendasDisp.length > 0 && <span className="badge badge--accent">{revendasDisp.length}</span>}</h2>
              <Link to={`/revendas?eventoId=${eventoId}`} className="row" style={{ gap: 4, color: 'var(--brand-strong)', fontWeight: 600, fontSize: '0.875rem' }}>Ver tudo <ArrowRight size={15} /></Link>
            </div>
            {msgRevenda && <div className={`auth-alert ${msgRevenda.includes('Erro') ? 'auth-alert--err' : 'auth-alert--ok'}`}>{msgRevenda}</div>}
            {revendasDisp.length === 0 ? (
              <div className="surface surface--flat" style={{ borderStyle: 'dashed' }}><div className="empty" style={{ padding: 'var(--sp-6)' }}><Repeat size={32} /><p className="muted">Nenhuma revenda disponível.</p></div></div>
            ) : (
              <div className="surface" style={{ overflow: 'hidden' }}>
                {revendasDisp.slice(0, 4).map(r => (
                  <div key={r.id} className="list-row">
                    <div className="list-ico list-ico--out"><Ticket size={20} /></div>
                    <div className="grow">
                      <div style={{ fontWeight: 600 }}>{r.quantidade} ingresso(s)</div>
                      <div className="muted" style={{ fontSize: '0.8125rem' }}>Vendedor #{r.vendedorId}</div>
                    </div>
                    <span className="money" style={{ fontSize: '1.05rem' }}>{formatMoeda(r.preco)}</span>
                    {usuario && r.vendedorId !== usuario.id && (
                      <button className="btn btn--sm btn--soft" onClick={() => reservarRevenda(r.id)} disabled={reservando === r.id}>
                        {reservando === r.id ? '…' : 'Reservar'}
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
              <h2 style={{ marginBottom: 'var(--sp-3)' }}>Avaliações</h2>
              <div className="stack" style={{ gap: 'var(--sp-3)' }}>
                {avaliacoes.slice(0, 5).map(a => (
                  <div key={a.id} className="surface surface--pad">
                    <div className="review-stars" style={{ marginBottom: 6 }}>
                      {Array.from({ length: 5 }).map((_, i) => <Star key={i} size={16} fill={i < a.nota ? 'currentColor' : 'none'} style={{ color: i < a.nota ? 'var(--warn)' : 'var(--border-strong)' }} />)}
                    </div>
                    {a.comentario && <p className="secondary">{a.comentario}</p>}
                    {a.respostaOrganizador && <div className="org-reply"><strong>Organizador:</strong> {a.respostaOrganizador}</div>}
                  </div>
                ))}
              </div>
            </section>
          )}
        </div>

        {/* Direita — seletor */}
        <div className="sticky-side">
          <div className="surface surface--pad" style={{ boxShadow: 'var(--shadow-md)' }}>
            <h3 style={{ marginBottom: 'var(--sp-4)' }}>Ingressos</h3>
            {tipos.length === 0 ? (
              <p className="muted" style={{ textAlign: 'center', padding: 'var(--sp-4) 0' }}>Nenhum tipo disponível.</p>
            ) : tipos.map(t => {
              const qtd = selecoes[t.id] ?? 0
              const max = maxParaTipo(t)
              const esgotado = max === 0
              const lote = loteAtivo(t)
              return (
                <div key={t.id} className="ticket-row between" style={{ alignItems: 'flex-start', gap: 'var(--sp-3)' }}>
                  <div style={{ minWidth: 0 }}>
                    <div style={{ fontWeight: 600 }}>{t.nome}</div>
                    <div className="money" style={{ color: 'var(--brand-strong)' }}>{formatMoeda(t.preco)}</div>
                    {lote && <span className="badge badge--brand" style={{ marginTop: 4 }}>{lote.nome} · {lote.quantidadeDisponivel} restante(s)</span>}
                    {t.descricao && <p className="muted" style={{ fontSize: '0.8125rem', marginTop: 4 }}>{t.descricao}</p>}
                  </div>
                  {esgotado ? (
                    <span className="badge badge--danger">Esgotado</span>
                  ) : (
                    <div className="stepper">
                      <button className="qty-btn" onClick={() => alterarQtd(t.id, -1)} disabled={qtd === 0} aria-label="Remover"><Minus size={16} /></button>
                      <span className="qty-val">{qtd}</span>
                      <button className="qty-btn qty-btn--add" onClick={() => alterarQtd(t.id, 1)} disabled={qtd >= max} aria-label="Adicionar"><Plus size={16} /></button>
                    </div>
                  )}
                </div>
              )
            })}
            <div className="sum-total" style={{ marginTop: 'var(--sp-4)' }}>
              <span className="secondary">Total</span>
              <span className="money" style={{ fontSize: '1.5rem', color: 'var(--brand-strong)' }}>{formatMoeda(total)}</span>
            </div>
            <button className="btn btn--lg btn--block" style={{ marginTop: 'var(--sp-4)' }} onClick={irParaRevisao} disabled={!temSelecionado}>
              <Lock size={18} /> Comprar ingressos
            </button>
            <p className="muted" style={{ textAlign: 'center', fontSize: '0.75rem', marginTop: 'var(--sp-3)' }}>Transação segura via Ingressify Pay</p>
          </div>
        </div>
      </div>
    </>
  )
}
