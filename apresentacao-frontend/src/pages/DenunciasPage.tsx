import { useEffect, useMemo, useState } from 'react'
import {
  ShieldAlert, Archive, Trash2, AlertTriangle, Ban, CheckCircle2, MapPin, Tag, User, Store, Clock, Inbox, FlaskConical, Flag, Calendar,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import { denunciaService, denunciaEventoService, eventoService, revendaService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Denuncia { id: number; anuncioId: number; denuncianteId: number; motivo: string; descricao: string; status: string; decisao: string | null; criadaEm: string; decididaEm: string | null }
interface Anuncio { id: number; preco: number; status: string; vendedorId: number; eventoId: number }
interface EventoSimples { id: number; nome: string; local: string }
interface DenunciaEvento { id: number; eventoId: number; denuncianteId: number; motivo: string; descricao: string; status: string; criadaEm: string; decididaEm: string | null }

const MOCK_DENUNCIAS: Denuncia[] = [
  { id: 101, anuncioId: 201, denuncianteId: 3, motivo: 'PRECO_ABUSIVO', descricao: 'Vendedor está cobrando 3x o valor de face do ingresso sem nenhuma justificativa.', status: 'PENDENTE', decisao: null, criadaEm: '2026-06-10T14:30:00', decididaEm: null },
  { id: 102, anuncioId: 202, denuncianteId: 4, motivo: 'INGRESSO_SUSPEITO', descricao: 'O QR Code enviado parece já ter sido utilizado em outro evento. Possível duplicata.', status: 'PENDENTE', decisao: null, criadaEm: '2026-06-12T09:15:00', decididaEm: null },
  { id: 103, anuncioId: 203, denuncianteId: 5, motivo: 'COMPORTAMENTO_INADEQUADO', descricao: 'Vendedor ameaçou cancelar a transação após o pagamento já ter sido confirmado.', status: 'RESOLVIDA', decisao: 'VENDEDOR_BLOQUEADO', criadaEm: '2026-06-08T11:00:00', decididaEm: '2026-06-09T16:30:00' },
  { id: 104, anuncioId: 204, denuncianteId: 3, motivo: 'OUTRO', descricao: 'Ingresso anunciado para setor VIP mas na realidade é para pista comum.', status: 'RESOLVIDA', decisao: 'ANUNCIO_REMOVIDO', criadaEm: '2026-06-05T08:45:00', decididaEm: '2026-06-06T10:00:00' },
]

const MOCK_ANUNCIOS = new Map<number, Anuncio>([
  [201, { id: 201, preco: 480.00, status: 'DISPONIVEL', vendedorId: 10, eventoId: 1001 }],
  [202, { id: 202, preco: 320.00, status: 'DISPONIVEL', vendedorId: 11, eventoId: 1002 }],
  [203, { id: 203, preco: 600.00, status: 'CANCELADO', vendedorId: 12, eventoId: 1001 }],
  [204, { id: 204, preco: 250.00, status: 'CANCELADO', vendedorId: 13, eventoId: 1003 }],
])

const MOCK_EVENTOS = new Map<number, EventoSimples>([
  [1001, { id: 1001, nome: 'Festival de Verão 2026', local: 'Arena Recife' }],
  [1002, { id: 1002, nome: 'Rock in Recife', local: 'Espaço Cultural' }],
  [1003, { id: 1003, nome: 'Tech Conference CESAR', local: 'CESAR - Recife' }],
])

const MOCK_DENUNCIAS_EVENTO: DenunciaEvento[] = [
  { id: 1, eventoId: 1001, denuncianteId: 5, motivo: 'FRAUDE', descricao: 'Acredito que o organizador não tem a licença para realizar este evento no local indicado.', status: 'PENDENTE', criadaEm: '2026-06-14T09:00:00', decididaEm: null },
  { id: 2, eventoId: 1002, denuncianteId: 7, motivo: 'EVENTO_FALSO', descricao: 'Não existe registro da banda headliner nesta data. Parece ser enganoso.', status: 'EM_ANALISE', criadaEm: '2026-06-13T15:30:00', decididaEm: null },
  { id: 3, eventoId: 1003, denuncianteId: 3, motivo: 'CONTEUDO_INAPROPRIADO', descricao: 'Material de divulgação contém imagens ofensivas.', status: 'APROVADA', criadaEm: '2026-06-10T11:00:00', decididaEm: '2026-06-11T14:00:00' },
]

const MOTIVO_LABELS_REVENDA: Record<string, string> = {
  PRECO_ABUSIVO: 'Preço abusivo', INGRESSO_SUSPEITO: 'Ingresso suspeito',
  COMPORTAMENTO_INADEQUADO: 'Comportamento inadequado', OUTRO: 'Outro',
}
const MOTIVO_LABELS_EVENTO: Record<string, string> = {
  FRAUDE: 'Fraude / golpe', CONTEUDO_INAPROPRIADO: 'Conteúdo inapropriado',
  EVENTO_FALSO: 'Evento falso', SEGURANCA: 'Risco à segurança', OUTRO: 'Outro',
}
const STATUS_EVENTO_LABEL: Record<string, string> = {
  PENDENTE: 'Pendente', EM_ANALISE: 'Em análise', APROVADA: 'Aprovada', REJEITADA: 'Rejeitada',
}
const STATUS_EVENTO_BADGE: Record<string, string> = {
  PENDENTE: 'badge--warn', EM_ANALISE: 'badge--brand', APROVADA: 'badge--success', REJEITADA: 'badge--danger',
}
const DECISOES = [
  { id: 'ARQUIVADA', label: 'Arquivar', Icon: Archive, danger: false },
  { id: 'VENDEDOR_AVISADO', label: 'Avisar vendedor', Icon: AlertTriangle, danger: false },
  { id: 'ANUNCIO_REMOVIDO', label: 'Remover anúncio', Icon: Trash2, danger: true },
  { id: 'VENDEDOR_BLOQUEADO', label: 'Bloquear vendedor', Icon: Ban, danger: true },
]
const DECISAO_LABEL: Record<string, string> = Object.fromEntries(DECISOES.map(d => [d.id, d.label]))

export default function DenunciasPage() {
  const { usuario, isAdmin } = useAuth()
  const [aba, setAba] = useState<'revendas' | 'eventos'>('revendas')

  if (!isAdmin()) {
    return (<><Navbar /><div className="app-container page"><div className="empty"><Ban size={40} /><h3>Acesso restrito</h3><p className="muted">Esta área é exclusiva de administradores.</p></div></div></>)
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head row" style={{ gap: 'var(--sp-3)' }}>
          <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><ShieldAlert size={24} /></span>
          <div><h1>Moderação</h1><p className="secondary">Gerencie denúncias de revendas e eventos.</p></div>
        </div>

        <div className="segmented" style={{ marginBottom: 'var(--sp-5)' }}>
          <button className={`segmented__opt${aba === 'revendas' ? ' segmented__opt--active' : ''}`} onClick={() => setAba('revendas')}>
            <Store size={15} /> Revendas
          </button>
          <button className={`segmented__opt${aba === 'eventos' ? ' segmented__opt--active' : ''}`} onClick={() => setAba('eventos')}>
            <Flag size={15} /> Eventos
          </button>
        </div>

        {aba === 'revendas' ? (
          <RevendasTab usuario={usuario} />
        ) : (
          <EventosTab usuario={usuario} />
        )}
      </div>
    </>
  )
}

function RevendasTab({ usuario }: { usuario: { id: number } | null }) {
  const [denuncias, setDenuncias] = useState<Denuncia[]>([])
  const [anunciosMap, setAnunciosMap] = useState<Map<number, Anuncio>>(new Map())
  const [eventosMap, setEventosMap] = useState<Map<number, EventoSimples>>(new Map())
  const [filtro, setFiltro] = useState<'TODAS' | 'PENDENTE' | 'RESOLVIDA'>('PENDENTE')
  const [carregando, setCarregando] = useState(true)
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [decidindoId, setDecidindoId] = useState<number | null>(null)
  const [usandoMock, setUsandoMock] = useState(false)

  const ativarMock = () => {
    setDenuncias(MOCK_DENUNCIAS); setAnunciosMap(MOCK_ANUNCIOS); setEventosMap(MOCK_EVENTOS)
    setUsandoMock(true); setFiltro('TODAS')
    setMsg({ ok: true, texto: 'Dados demo carregados.' }); setTimeout(() => setMsg(null), 4000)
  }

  const carregar = async () => {
    if (!usuario || usandoMock) return
    setCarregando(true); setMsg(null)
    try {
      const res = await denunciaService.listar(usuario.id)
      const lista: Denuncia[] = res.data
      const anuncioIds = [...new Set(lista.map(d => d.anuncioId))]
      const anuncios = await Promise.all(anuncioIds.map(id => revendaService.detalhe(id).then(r => r.data as Anuncio).catch(() => null)))
      const mapaA = new Map<number, Anuncio>()
      anuncios.forEach(a => { if (a) mapaA.set(a.id, a) })
      const eventoIds = [...new Set(anuncios.filter(Boolean).map(a => (a as Anuncio).eventoId))]
      const eventos = await Promise.all(eventoIds.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, EventoSimples])))
      setDenuncias(lista); setAnunciosMap(mapaA); setEventosMap(new Map(eventos))
    } catch { setMsg({ ok: false, texto: 'Erro ao carregar denúncias.' }) }
    finally { setCarregando(false) }
  }
  useEffect(() => { carregar() }, [usuario])

  const pendentes = denuncias.filter(d => d.status === 'PENDENTE').length
  const filtradas = useMemo(() => filtro === 'TODAS' ? denuncias : denuncias.filter(d => d.status === filtro), [denuncias, filtro])

  const decidir = async (id: number, decisao: string) => {
    if (!usuario) return
    setDecidindoId(id); setMsg(null)
    if (usandoMock) {
      setDenuncias(prev => prev.map(d => d.id === id ? { ...d, status: 'RESOLVIDA', decisao, decididaEm: new Date().toISOString() } : d))
      setMsg({ ok: true, texto: `[Demo] Decisão aplicada: ${DECISAO_LABEL[decisao]}.` })
      setTimeout(() => setMsg(null), 3000); setDecidindoId(null); return
    }
    try {
      await denunciaService.decidir(id, usuario.id, decisao)
      setMsg({ ok: true, texto: `Decisão aplicada: ${DECISAO_LABEL[decisao]}.` }); await carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro ao decidir.' })
    } finally { setDecidindoId(null) }
  }

  return (
    <>
      <div className="row wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-5)' }}>
        <div className="stat"><span className="stat__n" style={{ color: 'var(--warn)' }}>{pendentes}</span><span className="stat__l">Pendentes</span></div>
        <div className="stat"><span className="stat__n" style={{ color: 'oklch(0.45 0.12 150)' }}>{denuncias.length - pendentes}</span><span className="stat__l">Resolvidas</span></div>
        <div className="stat"><span className="stat__n">{denuncias.length}</span><span className="stat__l">Total</span></div>
      </div>
      <div className="between wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-4)' }}>
        <div className="segmented">
          {([['PENDENTE', 'Pendentes'], ['RESOLVIDA', 'Resolvidas'], ['TODAS', 'Todas']] as const).map(([v, t]) => (
            <button key={v} className={`segmented__opt${filtro === v ? ' segmented__opt--active' : ''}`} onClick={() => setFiltro(v)}>{t}</button>
          ))}
        </div>
        {!usandoMock && !carregando && (
          <button className="btn btn--sm btn--ghost" onClick={ativarMock}><FlaskConical size={15} /> Dados demo</button>
        )}
        {usandoMock && <span className="badge badge--warn"><FlaskConical size={13} /> Modo demo</span>}
      </div>
      {msg && <div className={`auth-alert ${msg.ok ? 'auth-alert--ok' : 'auth-alert--err'}`}>{msg.ok ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}{msg.texto}</div>}
      {carregando ? (
        <div className="stack" style={{ gap: 'var(--sp-3)' }}>{Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 160 }} />)}</div>
      ) : filtradas.length === 0 ? (
        <div className="empty">
          <Inbox size={40} /><h3>Tudo em ordem</h3>
          <p className="muted">Nenhuma denúncia {filtro === 'PENDENTE' ? 'pendente' : filtro === 'RESOLVIDA' ? 'resolvida' : ''}.</p>
          {!usandoMock && <button className="btn btn--sm btn--ghost" onClick={ativarMock} style={{ marginTop: 'var(--sp-3)' }}><FlaskConical size={15} /> Dados demo</button>}
        </div>
      ) : (
        <div className="stack" style={{ gap: 'var(--sp-4)' }}>
          {filtradas.map(d => {
            const anuncio = anunciosMap.get(d.anuncioId)
            const evento = anuncio ? eventosMap.get(anuncio.eventoId) : undefined
            const resolvida = d.status === 'RESOLVIDA'
            return (
              <div key={d.id} className="surface surface--pad">
                <div className="between wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-3)' }}>
                  <div className="row" style={{ gap: 'var(--sp-2)' }}>
                    <span className="muted" style={{ fontWeight: 600 }}>#{d.id}</span>
                    <h3 style={{ fontSize: '1.1rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>{evento?.nome ?? `Anúncio #${d.anuncioId}`}</h3>
                    <span className={`badge ${resolvida ? 'badge--success' : 'badge--warn'}`}>{resolvida ? 'Resolvida' : 'Pendente'}</span>
                  </div>
                  <span className="muted row" style={{ gap: 6, fontSize: '0.8125rem' }}><Clock size={14} /> {new Date(d.criadaEm).toLocaleDateString('pt-BR')}</span>
                </div>
                <div className="den-grid">
                  <Info Icon={Tag} label="Motivo" value={MOTIVO_LABELS_REVENDA[d.motivo] ?? d.motivo} />
                  <Info Icon={MapPin} label="Local" value={evento?.local ?? '—'} />
                  <Info Icon={User} label="Denunciante" value={`Usuário #${d.denuncianteId}`} />
                  {anuncio && <Info Icon={Store} label="Vendedor" value={`Usuário #${anuncio.vendedorId}`} />}
                  {anuncio && <Info Icon={Tag} label="Preço anunciado" value={formatMoeda(anuncio.preco)} />}
                  {d.decisao && <Info Icon={CheckCircle2} label="Decisão" value={DECISAO_LABEL[d.decisao] ?? d.decisao} />}
                </div>
                {d.descricao && <p className="org-reply" style={{ marginTop: 'var(--sp-3)' }}>{d.descricao}</p>}
                {!resolvida && (
                  <div className="row wrap" style={{ gap: 'var(--sp-2)', marginTop: 'var(--sp-4)' }}>
                    {DECISOES.map(({ id, label, Icon, danger }) => (
                      <button key={id} className={`btn btn--sm ${danger ? 'btn--danger' : 'btn--ghost'}`} disabled={decidindoId === d.id} onClick={() => decidir(d.id, id)}>
                        <Icon size={16} /> {label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </>
  )
}

function EventosTab({ usuario }: { usuario: { id: number } | null }) {
  const [denuncias, setDenuncias] = useState<DenunciaEvento[]>([])
  const [eventosMap, setEventosMap] = useState<Map<number, EventoSimples>>(new Map())
  const [filtro, setFiltro] = useState<'TODAS' | 'PENDENTE' | 'EM_ANALISE' | 'APROVADA' | 'REJEITADA'>('PENDENTE')
  const [carregando, setCarregando] = useState(true)
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [agindoId, setAgindoId] = useState<number | null>(null)
  const [usandoMock, setUsandoMock] = useState(false)

  const ativarMock = () => {
    setDenuncias(MOCK_DENUNCIAS_EVENTO)
    const ev = new Map<number, EventoSimples>([[1001, { id: 1001, nome: 'Festival de Verão 2026', local: 'Arena Recife' }], [1002, { id: 1002, nome: 'Rock in Recife', local: 'Espaço Cultural' }], [1003, { id: 1003, nome: 'Tech Conference CESAR', local: 'CESAR - Recife' }]])
    setEventosMap(ev); setUsandoMock(true); setFiltro('TODAS')
    setMsg({ ok: true, texto: 'Dados demo carregados.' }); setTimeout(() => setMsg(null), 4000)
  }

  const carregar = async () => {
    if (!usuario || usandoMock) return
    setCarregando(true); setMsg(null)
    try {
      const res = await denunciaEventoService.listar(usuario.id)
      const lista: DenunciaEvento[] = res.data
      const eventoIds = [...new Set(lista.map(d => d.eventoId))]
      const eventos = await Promise.all(eventoIds.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, EventoSimples]).catch(() => [id, { id, nome: `Evento #${id}`, local: '—' }] as [number, EventoSimples])))
      setDenuncias(lista); setEventosMap(new Map(eventos))
    } catch { setMsg({ ok: false, texto: 'Erro ao carregar denúncias de eventos.' }) }
    finally { setCarregando(false) }
  }
  useEffect(() => { carregar() }, [usuario])

  const pendentes = denuncias.filter(d => d.status === 'PENDENTE').length
  const emAnalise = denuncias.filter(d => d.status === 'EM_ANALISE').length
  const filtradas = useMemo(() => filtro === 'TODAS' ? denuncias : denuncias.filter(d => d.status === filtro), [denuncias, filtro])

  const agir = async (id: number, acao: 'analisar' | 'aprovar' | 'rejeitar') => {
    if (!usuario) return
    setAgindoId(id); setMsg(null)
    if (usandoMock) {
      const novoStatus = acao === 'analisar' ? 'EM_ANALISE' : acao === 'aprovar' ? 'APROVADA' : 'REJEITADA'
      setDenuncias(prev => prev.map(d => d.id === id ? { ...d, status: novoStatus, decididaEm: acao !== 'analisar' ? new Date().toISOString() : null } : d))
      setMsg({ ok: true, texto: `[Demo] Ação aplicada.` }); setTimeout(() => setMsg(null), 3000); setAgindoId(null); return
    }
    try {
      if (acao === 'analisar') await denunciaEventoService.analisar(id, usuario.id)
      else if (acao === 'aprovar') await denunciaEventoService.aprovar(id, usuario.id)
      else await denunciaEventoService.rejeitar(id, usuario.id)
      setMsg({ ok: true, texto: 'Ação aplicada.' }); await carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro.' })
    } finally { setAgindoId(null) }
  }

  return (
    <>
      <div className="row wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-5)' }}>
        <div className="stat"><span className="stat__n" style={{ color: 'var(--warn)' }}>{pendentes}</span><span className="stat__l">Pendentes</span></div>
        <div className="stat"><span className="stat__n" style={{ color: 'var(--brand-strong)' }}>{emAnalise}</span><span className="stat__l">Em análise</span></div>
        <div className="stat"><span className="stat__n">{denuncias.length}</span><span className="stat__l">Total</span></div>
      </div>
      <div className="between wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-4)' }}>
        <div className="segmented" style={{ flexWrap: 'wrap' }}>
          {([['PENDENTE', 'Pendentes'], ['EM_ANALISE', 'Em análise'], ['APROVADA', 'Aprovadas'], ['REJEITADA', 'Rejeitadas'], ['TODAS', 'Todas']] as const).map(([v, t]) => (
            <button key={v} className={`segmented__opt${filtro === v ? ' segmented__opt--active' : ''}`} onClick={() => setFiltro(v)}>{t}</button>
          ))}
        </div>
        {!usandoMock && !carregando && (
          <button className="btn btn--sm btn--ghost" onClick={ativarMock}><FlaskConical size={15} /> Dados demo</button>
        )}
        {usandoMock && <span className="badge badge--warn"><FlaskConical size={13} /> Modo demo</span>}
      </div>
      {msg && <div className={`auth-alert ${msg.ok ? 'auth-alert--ok' : 'auth-alert--err'}`}>{msg.ok ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}{msg.texto}</div>}
      {carregando ? (
        <div className="stack" style={{ gap: 'var(--sp-3)' }}>{Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 140 }} />)}</div>
      ) : filtradas.length === 0 ? (
        <div className="empty">
          <Inbox size={40} /><h3>Nenhuma denúncia</h3>
          <p className="muted">Nenhuma denúncia de evento {filtro !== 'TODAS' ? `com status "${STATUS_EVENTO_LABEL[filtro]}"` : ''}.</p>
          {!usandoMock && <button className="btn btn--sm btn--ghost" onClick={ativarMock} style={{ marginTop: 'var(--sp-3)' }}><FlaskConical size={15} /> Dados demo</button>}
        </div>
      ) : (
        <div className="stack" style={{ gap: 'var(--sp-4)' }}>
          {filtradas.map(d => {
            const evento = eventosMap.get(d.eventoId)
            const resolvida = d.status === 'APROVADA' || d.status === 'REJEITADA'
            return (
              <div key={d.id} className="surface surface--pad">
                <div className="between wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-3)' }}>
                  <div className="row" style={{ gap: 'var(--sp-2)' }}>
                    <span className="muted" style={{ fontWeight: 600 }}>#{d.id}</span>
                    <h3 style={{ fontSize: '1.1rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>{evento?.nome ?? `Evento #${d.eventoId}`}</h3>
                    <span className={`badge ${STATUS_EVENTO_BADGE[d.status] ?? 'badge--warn'}`}>{STATUS_EVENTO_LABEL[d.status] ?? d.status}</span>
                  </div>
                  <span className="muted row" style={{ gap: 6, fontSize: '0.8125rem' }}><Clock size={14} /> {new Date(d.criadaEm).toLocaleDateString('pt-BR')}</span>
                </div>
                <div className="den-grid">
                  <Info Icon={Flag} label="Motivo" value={MOTIVO_LABELS_EVENTO[d.motivo] ?? d.motivo} />
                  {evento && <Info Icon={MapPin} label="Local" value={evento.local} />}
                  <Info Icon={User} label="Denunciante" value={`Usuário #${d.denuncianteId}`} />
                  <Info Icon={Calendar} label="Evento ID" value={`#${d.eventoId}`} />
                  {d.decididaEm && <Info Icon={CheckCircle2} label="Decidida em" value={new Date(d.decididaEm).toLocaleDateString('pt-BR')} />}
                </div>
                {d.descricao && <p className="org-reply" style={{ marginTop: 'var(--sp-3)' }}>{d.descricao}</p>}
                {!resolvida && (
                  <div className="row wrap" style={{ gap: 'var(--sp-2)', marginTop: 'var(--sp-4)' }}>
                    {d.status === 'PENDENTE' && (
                      <button className="btn btn--sm btn--ghost" disabled={agindoId === d.id} onClick={() => agir(d.id, 'analisar')}>
                        <FlaskConical size={16} /> Iniciar análise
                      </button>
                    )}
                    <button className="btn btn--sm btn--ghost" disabled={agindoId === d.id} onClick={() => agir(d.id, 'aprovar')}>
                      <CheckCircle2 size={16} /> Aprovar
                    </button>
                    <button className="btn btn--sm btn--danger" disabled={agindoId === d.id} onClick={() => agir(d.id, 'rejeitar')}>
                      <Ban size={16} /> Rejeitar
                    </button>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </>
  )
}

function Info({ Icon, label, value }: { Icon: typeof Tag; label: string; value: string }) {
  return (
    <div className="row" style={{ gap: 10, alignItems: 'flex-start' }}>
      <Icon size={16} style={{ color: 'var(--ink-muted)', marginTop: 2, flexShrink: 0 }} />
      <div><div className="muted" style={{ fontSize: '0.75rem' }}>{label}</div><div style={{ fontWeight: 500 }}>{value}</div></div>
    </div>
  )
}
