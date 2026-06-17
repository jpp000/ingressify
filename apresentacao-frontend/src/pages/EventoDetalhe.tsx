import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import {
  Calendar, Clock, MapPin, Star, Dices, Repeat, Armchair, Megaphone, Ticket,
  Minus, Plus, Lock, Pin, ArrowRight, Percent, Music2, Drama, Trophy, Mic, Disc3, UtensilsCrossed,
  Send, MessageCircle, FlaskConical, CheckCircle2, AlertTriangle, Flag, X,
  type LucideIcon,
} from 'lucide-react'
import { eventoService, tipoIngressoService, avaliacaoService, revendaService, feedService, denunciaEventoService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, corCategoria } from '../constants'
import { useAuth } from '../context/AuthContext'

const MOCK_AVALIACOES_DEMO: Avaliacao[] = [
  { id: 901, nota: 5, comentario: 'Evento incrível! Organização impecável, som perfeito e estrutura excelente.', respostaOrganizador: 'Obrigado pelo feedback! Esperamos você na próxima edição.' },
  { id: 902, nota: 4, comentario: 'Muito bom! Só achei a fila na entrada um pouco longa, mas valeu a pena.', respostaOrganizador: undefined },
  { id: 903, nota: 3, comentario: 'Razoável. Esperava mais atrações para o preço cobrado.', respostaOrganizador: 'Agradecemos o feedback! Estamos investindo em mais atrações para a próxima edição.' },
  { id: 904, nota: 5, comentario: 'Melhor show que já fui em Recife! Voltarei com certeza.', respostaOrganizador: undefined },
  { id: 905, nota: 2, comentario: 'Tive problemas com o QR Code do ingresso na entrada, demorou para resolver.', respostaOrganizador: 'Pedimos desculpas pela experiência. Estamos melhorando o sistema de check-in.' },
]

interface Evento { id: number; nome: string; dataHora: string; local: string; descricao?: string; imagemCapaUrl?: string; status: string; aberturaPortoes?: string; categoria?: string; temAssentosNumerados?: boolean }
interface Lote { id: number; nome: string; preco: number; quantidadeDisponivel: number; ativo: boolean }
interface TipoIngresso { id: number; nome: string; preco: number; quantidadeDisponivel: number; descricao?: string; lotes?: Lote[]; precoMeia?: number | null; meiaEntradaHabilitada?: boolean; cotaMeiaDisponivel?: number }
interface Avaliacao { id: number; nota: number; comentario?: string; respostaOrganizador?: string }
interface AnuncioRevenda { id: number; preco: number; status: string; quantidade: number; vendedorId: number; compradorId: number | null }
interface Postagem { id: number; titulo: string; conteudo: string; fixada: boolean; criadaEm: string }

const ICONE_CATEGORIA: Record<string, LucideIcon> = {
  'Música': Music2, 'Teatro e Artes': Drama, 'Esportes': Trophy, 'Conferências': Mic, 'Vida Noturna': Disc3, 'Comida e Bebida': UtensilsCrossed,
}

export default function EventoDetalhe() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { usuario, isOrganizador, isAdmin } = useAuth()
  const eventoId = Number(id)

  const [evento, setEvento] = useState<Evento | null>(null)
  const [tipos, setTipos] = useState<TipoIngresso[]>([])
  const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([])
  const [revendas, setRevendas] = useState<AnuncioRevenda[]>([])
  const [postagens, setPostagens] = useState<Postagem[]>([])
  const [selecoes, setSelecoes] = useState<Record<number, number>>({})
  const [selecoesMeia, setSelecoesMeia] = useState<Record<number, number>>({})
  const [carregando, setCarregando] = useState(true)
  const [reservando, setReservando] = useState<number | null>(null)
  const [msgRevenda, setMsgRevenda] = useState('')
  const [novoPost, setNovoPost] = useState({ titulo: '', conteudo: '' })
  const [publicandoPost, setPublicandoPost] = useState(false)
  // Avaliações
  const [notaNova, setNotaNova] = useState(0)
  const [notaHover, setNotaHover] = useState(0)
  const [comentarioNovo, setComentarioNovo] = useState('')
  const [enviandoAvaliacao, setEnviandoAvaliacao] = useState(false)
  const [msgAvaliacao, setMsgAvaliacao] = useState<{ ok: boolean; texto: string } | null>(null)
  const [mockAvaliacoes, setMockAvaliacoes] = useState(false)
  // Denúncia de evento
  const [modalDenuncia, setModalDenuncia] = useState(false)
  const [motivoDenuncia, setMotivoDenuncia] = useState('')
  const [descricaoDenuncia, setDescricaoDenuncia] = useState('')
  const [enviandoDenuncia, setEnviandoDenuncia] = useState(false)
  const [msgDenuncia, setMsgDenuncia] = useState<{ ok: boolean; texto: string } | null>(null)
  // Resposta do organizador
  const [respostaAberta, setRespostaAberta] = useState<number | null>(null)
  const [respostaTexto, setRespostaTexto] = useState('')
  const [enviandoResposta, setEnviandoResposta] = useState(false)

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
  const maxInteira = (t: TipoIngresso) => Math.max(0, maxParaTipo(t) - (selecoesMeia[t.id] ?? 0))
  const maxMeia = (t: TipoIngresso) => Math.max(0, Math.min(t.cotaMeiaDisponivel ?? 0, maxParaTipo(t) - (selecoes[t.id] ?? 0)))
  const alterarQtd = (tipoId: number, delta: number) => setSelecoes(prev => {
    const tipo = tipos.find(t => t.id === tipoId); const max = tipo ? maxInteira(tipo) : 0
    return { ...prev, [tipoId]: Math.max(0, Math.min(max, (prev[tipoId] ?? 0) + delta)) }
  })
  const alterarQtdMeia = (tipoId: number, delta: number) => setSelecoesMeia(prev => {
    const tipo = tipos.find(t => t.id === tipoId); const max = tipo ? maxMeia(tipo) : 0
    return { ...prev, [tipoId]: Math.max(0, Math.min(max, (prev[tipoId] ?? 0) + delta)) }
  })

  const total = tipos.reduce((acc, t) => acc
    + (selecoes[t.id] ?? 0) * Number(t.preco)
    + (selecoesMeia[t.id] ?? 0) * Number(t.precoMeia ?? t.preco), 0)
  const temSelecionado = Object.values(selecoes).some(q => q > 0) || Object.values(selecoesMeia).some(q => q > 0)
  const mediaAval = avaliacoes.length ? avaliacoes.reduce((s, a) => s + a.nota, 0) / avaliacoes.length : 0

  const ativarMockAvaliacoes = () => {
    setAvaliacoes(MOCK_AVALIACOES_DEMO)
    setMockAvaliacoes(true)
    setMsgAvaliacao({ ok: true, texto: 'Dados demo carregados. Ações são simuladas localmente.' })
    setTimeout(() => setMsgAvaliacao(null), 4000)
  }

  const enviarAvaliacao = async (e: FormEvent) => {
    e.preventDefault()
    if (!usuario || notaNova === 0) return
    setEnviandoAvaliacao(true); setMsgAvaliacao(null)
    if (mockAvaliacoes) {
      const nova: Avaliacao = { id: Date.now(), nota: notaNova, comentario: comentarioNovo.trim() || undefined, respostaOrganizador: undefined }
      setAvaliacoes(prev => [nova, ...prev])
      setNotaNova(0); setComentarioNovo('')
      setMsgAvaliacao({ ok: true, texto: '[Demo] Avaliação adicionada localmente.' })
      setTimeout(() => setMsgAvaliacao(null), 3000)
      setEnviandoAvaliacao(false)
      return
    }
    try {
      await avaliacaoService.avaliar(eventoId, usuario.id, { nota: notaNova, comentario: comentarioNovo.trim() || null })
      setNotaNova(0); setComentarioNovo('')
      setMsgAvaliacao({ ok: true, texto: 'Avaliação enviada com sucesso!' })
      const novas = await avaliacaoService.listar(eventoId)
      setAvaliacoes(novas.data)
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      setMsgAvaliacao({ ok: false, texto: err.response?.data?.message ?? 'Não foi possível enviar a avaliação. Verifique se você tem ingresso para este evento e se ele já ocorreu.' })
    } finally {
      setEnviandoAvaliacao(false)
      setTimeout(() => setMsgAvaliacao(null), 5000)
    }
  }

  const enviarResposta = async (avaliacaoId: number) => {
    if (!usuario || !respostaTexto.trim()) return
    setEnviandoResposta(true)
    if (mockAvaliacoes) {
      setAvaliacoes(prev => prev.map(a => a.id === avaliacaoId ? { ...a, respostaOrganizador: respostaTexto.trim() } : a))
      setRespostaAberta(null); setRespostaTexto('')
      setEnviandoResposta(false)
      return
    }
    try {
      await avaliacaoService.responder(eventoId, avaliacaoId, usuario.id, respostaTexto.trim())
      setRespostaAberta(null); setRespostaTexto('')
      const novas = await avaliacaoService.listar(eventoId)
      setAvaliacoes(novas.data)
    } catch {
      // silently fail — resposta do organizador é opcional
    } finally {
      setEnviandoResposta(false)
    }
  }

  const enviarDenuncia = async () => {
    if (!usuario || !motivoDenuncia) return
    setEnviandoDenuncia(true); setMsgDenuncia(null)
    try {
      await denunciaEventoService.denunciar(eventoId, usuario.id, motivoDenuncia, descricaoDenuncia.trim())
      setMsgDenuncia({ ok: true, texto: 'Denúncia enviada. Nossa equipe irá analisar em breve.' })
      setMotivoDenuncia(''); setDescricaoDenuncia('')
      setTimeout(() => { setModalDenuncia(false); setMsgDenuncia(null) }, 3000)
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsgDenuncia({ ok: false, texto: err.response?.data?.motivo ?? 'Erro ao enviar denúncia.' })
    } finally { setEnviandoDenuncia(false) }
  }

  const irParaRevisao = () => {
    if (!temSelecionado || !evento) return
    const itens: { tipoId: number; quantidade: number; meia: boolean }[] = []
    tipos.forEach(t => {
      const qi = selecoes[t.id] ?? 0
      if (qi > 0) itens.push({ tipoId: t.id, quantidade: qi, meia: false })
      const qm = selecoesMeia[t.id] ?? 0
      if (qm > 0) itens.push({ tipoId: t.id, quantidade: qm, meia: true })
    })
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
            {evento.temAssentosNumerados && (
              <Link to={`/mapa-assentos?eventoId=${eventoId}`} className="chip"><Armchair size={15} /> Escolher assento</Link>
            )}
            {usuario && !isOrganizador() && !isAdmin() && (
              <button className="chip" style={{ cursor: 'pointer', color: 'var(--danger)' }} onClick={() => setModalDenuncia(true)}>
                <Flag size={15} /> Denunciar evento
              </button>
            )}
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

          {/* ── Avaliações ── */}
          <section>
            <div className="between" style={{ marginBottom: 'var(--sp-3)' }}>
              <h2 className="row" style={{ gap: 8 }}>
                <Star size={20} /> Avaliações
                {avaliacoes.length > 0 && <span className="badge badge--accent">{avaliacoes.length}</span>}
              </h2>
              {!mockAvaliacoes && avaliacoes.length === 0 && (
                <button className="btn btn--sm btn--ghost" onClick={ativarMockAvaliacoes}>
                  <FlaskConical size={14} /> Dados demo
                </button>
              )}
              {mockAvaliacoes && (
                <span className="badge badge--warn" style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
                  <FlaskConical size={12} /> Modo demo
                </span>
              )}
            </div>

            {msgAvaliacao && (
              <div className={`auth-alert ${msgAvaliacao.ok ? 'auth-alert--ok' : 'auth-alert--err'}`} style={{ marginBottom: 'var(--sp-3)' }}>
                {msgAvaliacao.ok ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />} {msgAvaliacao.texto}
              </div>
            )}

            {/* Formulário de nova avaliação */}
            {usuario && (
              <form onSubmit={enviarAvaliacao} className="surface surface--pad stack" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-4)' }}>
                <div>
                  <p style={{ fontWeight: 600, marginBottom: 'var(--sp-2)', fontSize: '0.9375rem' }}>
                    <MessageCircle size={15} style={{ verticalAlign: 'middle', marginRight: 6 }} />
                    Avaliar este evento
                  </p>
                  <div className="row" style={{ gap: 4, marginBottom: 'var(--sp-2)' }}>
                    {Array.from({ length: 5 }).map((_, i) => {
                      const val = i + 1
                      const ativa = val <= (notaHover || notaNova)
                      return (
                        <button
                          key={val}
                          type="button"
                          onClick={() => setNotaNova(val)}
                          onMouseEnter={() => setNotaHover(val)}
                          onMouseLeave={() => setNotaHover(0)}
                          style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '2px 3px' }}
                          aria-label={`${val} estrela${val > 1 ? 's' : ''}`}
                        >
                          <Star size={26} fill={ativa ? '#f59e0b' : 'none'} style={{ color: ativa ? '#f59e0b' : 'var(--border-strong)', transition: 'color 0.1s' }} />
                        </button>
                      )
                    })}
                    {notaNova > 0 && <span className="muted" style={{ fontSize: '0.8125rem', marginLeft: 6 }}>{notaNova} de 5</span>}
                  </div>
                  <textarea
                    className="textarea"
                    placeholder="Comentário opcional..."
                    value={comentarioNovo}
                    onChange={e => setComentarioNovo(e.target.value)}
                    style={{ minHeight: 72, resize: 'vertical' }}
                  />
                </div>
                <button
                  className="btn btn--sm"
                  type="submit"
                  disabled={notaNova === 0 || enviandoAvaliacao}
                  style={{ alignSelf: 'flex-start' }}
                >
                  <Send size={14} /> {enviandoAvaliacao ? 'Enviando...' : 'Enviar avaliação'}
                </button>
              </form>
            )}

            {/* Lista de avaliações */}
            {avaliacoes.length === 0 ? (
              <div className="surface surface--flat" style={{ borderStyle: 'dashed' }}>
                <div className="empty" style={{ padding: 'var(--sp-6)' }}>
                  <Star size={32} />
                  <p className="muted">Nenhuma avaliação ainda. Seja o primeiro!</p>
                  {!usuario && <p className="muted" style={{ fontSize: '0.8125rem' }}>Faça login para avaliar.</p>}
                </div>
              </div>
            ) : (
              <div className="stack" style={{ gap: 'var(--sp-3)' }}>
                {avaliacoes.slice(0, 5).map(a => (
                  <div key={a.id} className="surface surface--pad">
                    <div className="review-stars" style={{ marginBottom: 6 }}>
                      {Array.from({ length: 5 }).map((_, i) => (
                        <Star key={i} size={16} fill={i < a.nota ? 'currentColor' : 'none'} style={{ color: i < a.nota ? 'var(--warn)' : 'var(--border-strong)' }} />
                      ))}
                    </div>
                    {a.comentario && <p className="secondary" style={{ marginBottom: 'var(--sp-2)' }}>{a.comentario}</p>}
                    {a.respostaOrganizador && (
                      <div className="org-reply"><strong>Organizador:</strong> {a.respostaOrganizador}</div>
                    )}
                    {isOrganizador() && !a.respostaOrganizador && respostaAberta !== a.id && (
                      <button
                        className="btn btn--sm btn--ghost"
                        style={{ marginTop: 'var(--sp-2)' }}
                        onClick={() => { setRespostaAberta(a.id); setRespostaTexto('') }}
                      >
                        <MessageCircle size={14} /> Responder
                      </button>
                    )}
                    {isOrganizador() && respostaAberta === a.id && (
                      <div className="stack" style={{ gap: 'var(--sp-2)', marginTop: 'var(--sp-2)' }}>
                        <textarea
                          className="textarea"
                          placeholder="Sua resposta..."
                          value={respostaTexto}
                          onChange={e => setRespostaTexto(e.target.value)}
                          style={{ minHeight: 60, resize: 'vertical' }}
                          autoFocus
                        />
                        <div className="row" style={{ gap: 'var(--sp-2)' }}>
                          <button
                            className="btn btn--sm"
                            disabled={!respostaTexto.trim() || enviandoResposta}
                            onClick={() => enviarResposta(a.id)}
                          >
                            <Send size={13} /> {enviandoResposta ? 'Enviando...' : 'Publicar resposta'}
                          </button>
                          <button className="btn btn--sm btn--ghost" onClick={() => setRespostaAberta(null)}>Cancelar</button>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>

        {/* Direita — seletor */}
        <div className="sticky-side">
          <div className="surface surface--pad" style={{ boxShadow: 'var(--shadow-md)' }}>
            <h3 style={{ marginBottom: 'var(--sp-4)' }}>Ingressos</h3>
            {tipos.length === 0 ? (
              <p className="muted" style={{ textAlign: 'center', padding: 'var(--sp-4) 0' }}>Nenhum tipo disponível.</p>
            ) : tipos.map(t => {
              const qtd = selecoes[t.id] ?? 0
              const qtdMeia = selecoesMeia[t.id] ?? 0
              const maxI = maxInteira(t)
              const maxM = maxMeia(t)
              const esgotado = maxParaTipo(t) === 0
              const lote = loteAtivo(t)
              const temMeia = t.meiaEntradaHabilitada && t.precoMeia != null
              return (
                <div key={t.id} className="ticket-row" style={{ flexDirection: 'column', alignItems: 'stretch', gap: 'var(--sp-2)' }}>
                  <div className="between" style={{ alignItems: 'flex-start', gap: 'var(--sp-3)' }}>
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
                      <button className="qty-btn qty-btn--add" onClick={() => alterarQtd(t.id, 1)} disabled={qtd >= maxI} aria-label="Adicionar"><Plus size={16} /></button>
                    </div>
                  )}
                  </div>
                  {!esgotado && temMeia && (
                    <div className="between" style={{ alignItems: 'center', gap: 'var(--sp-3)', paddingLeft: 'var(--sp-3)', borderLeft: '2px solid var(--brand-soft-2)' }}>
                      <div style={{ minWidth: 0 }}>
                        <div className="row" style={{ gap: 6, fontWeight: 600, fontSize: '0.9rem' }}><Percent size={13} /> Meia-entrada</div>
                        <div className="money" style={{ color: 'var(--brand-strong)', fontSize: '0.95rem' }}>
                          {formatMoeda(Number(t.precoMeia))}
                          <span className="muted" style={{ fontSize: '0.75rem', fontWeight: 400 }}> · restam {t.cotaMeiaDisponivel ?? 0}</span>
                        </div>
                      </div>
                      <div className="stepper">
                        <button className="qty-btn" onClick={() => alterarQtdMeia(t.id, -1)} disabled={qtdMeia === 0} aria-label="Remover meia"><Minus size={16} /></button>
                        <span className="qty-val">{qtdMeia}</span>
                        <button className="qty-btn qty-btn--add" onClick={() => alterarQtdMeia(t.id, 1)} disabled={qtdMeia >= maxM} aria-label="Adicionar meia"><Plus size={16} /></button>
                      </div>
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

      {/* Modal Denúncia de Evento */}
      {modalDenuncia && (
        <div className="modal-backdrop" onClick={() => setModalDenuncia(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal__head">
              <h3 className="row" style={{ gap: 8 }}><Flag size={18} style={{ color: 'var(--danger)' }} /> Denunciar evento</h3>
              <button className="modal__x" onClick={() => setModalDenuncia(false)}><X size={18} /></button>
            </div>
            <p className="secondary" style={{ marginBottom: 'var(--sp-4)', fontSize: '0.9rem' }}>
              Denúncias são analisadas pela nossa equipe. Use com responsabilidade.
            </p>
            <div className="field" style={{ marginBottom: 'var(--sp-3)' }}>
              <label className="label">Motivo *</label>
              <select className="input" value={motivoDenuncia} onChange={e => setMotivoDenuncia(e.target.value)}>
                <option value="">Selecione o motivo…</option>
                <option value="FRAUDE">Fraude / golpe</option>
                <option value="CONTEUDO_INAPROPRIADO">Conteúdo inapropriado</option>
                <option value="EVENTO_FALSO">Evento falso ou inexistente</option>
                <option value="SEGURANCA">Risco à segurança</option>
                <option value="OUTRO">Outro</option>
              </select>
            </div>
            <div className="field" style={{ marginBottom: 'var(--sp-4)' }}>
              <label className="label">Descrição complementar</label>
              <textarea
                className="textarea"
                placeholder="Descreva o problema com mais detalhes (opcional)…"
                value={descricaoDenuncia}
                onChange={e => setDescricaoDenuncia(e.target.value)}
                style={{ minHeight: 80, resize: 'vertical' }}
              />
            </div>
            {msgDenuncia && (
              <div className={`auth-alert ${msgDenuncia.ok ? 'auth-alert--ok' : 'auth-alert--err'}`} style={{ marginBottom: 'var(--sp-3)' }}>
                {msgDenuncia.ok ? <CheckCircle2 size={16} /> : <AlertTriangle size={16} />} {msgDenuncia.texto}
              </div>
            )}
            <div className="row" style={{ gap: 'var(--sp-2)' }}>
              <button className="btn btn--ghost grow" onClick={() => setModalDenuncia(false)}>Cancelar</button>
              <button
                className="btn btn--danger grow"
                disabled={!motivoDenuncia || enviandoDenuncia}
                onClick={enviarDenuncia}
              >
                <Flag size={16} /> {enviandoDenuncia ? 'Enviando…' : 'Enviar denúncia'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
