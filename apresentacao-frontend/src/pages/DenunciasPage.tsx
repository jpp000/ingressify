import { useEffect, useMemo, useState } from 'react'
import {
  ShieldAlert, Archive, Trash2, AlertTriangle, Ban, CheckCircle2, MapPin, Tag, User, Store, Clock, Inbox,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import { denunciaService, eventoService, revendaService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Denuncia { id: number; anuncioId: number; denuncianteId: number; motivo: string; descricao: string; status: string; decisao: string | null; criadaEm: string; decididaEm: string | null }
interface Anuncio { id: number; preco: number; status: string; vendedorId: number; eventoId: number }
interface Evento { id: number; nome: string; local: string }

const MOTIVO_LABELS: Record<string, string> = {
  PRECO_ABUSIVO: 'Preço abusivo', INGRESSO_SUSPEITO: 'Ingresso suspeito',
  COMPORTAMENTO_INADEQUADO: 'Comportamento inadequado', OUTRO: 'Outro',
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
  const [denuncias, setDenuncias] = useState<Denuncia[]>([])
  const [anunciosMap, setAnunciosMap] = useState<Map<number, Anuncio>>(new Map())
  const [eventosMap, setEventosMap] = useState<Map<number, Evento>>(new Map())
  const [filtro, setFiltro] = useState<'TODAS' | 'PENDENTE' | 'RESOLVIDA'>('PENDENTE')
  const [carregando, setCarregando] = useState(true)
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [decidindoId, setDecidindoId] = useState<number | null>(null)

  const carregar = async () => {
    if (!usuario) return
    setCarregando(true); setMsg(null)
    try {
      const res = await denunciaService.listar(usuario.id)
      const lista: Denuncia[] = res.data
      const anuncioIds = [...new Set(lista.map(d => d.anuncioId))]
      const anuncios = await Promise.all(anuncioIds.map(id => revendaService.detalhe(id).then(r => r.data as Anuncio).catch(() => null)))
      const mapaA = new Map<number, Anuncio>()
      anuncios.forEach(a => { if (a) mapaA.set(a.id, a) })
      const eventoIds = [...new Set(anuncios.filter(Boolean).map(a => (a as Anuncio).eventoId))]
      const eventos = await Promise.all(eventoIds.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, Evento])))
      setDenuncias(lista); setAnunciosMap(mapaA); setEventosMap(new Map(eventos))
    } catch { setMsg({ ok: false, texto: 'Erro ao carregar denúncias.' }) }
    finally { setCarregando(false) }
  }
  useEffect(() => { carregar() }, [usuario])

  const pendentes = denuncias.filter(d => d.status === 'PENDENTE').length
  const resolvidas = denuncias.length - pendentes
  const filtradas = useMemo(() => filtro === 'TODAS' ? denuncias : denuncias.filter(d => d.status === filtro), [denuncias, filtro])

  const decidir = async (id: number, decisao: string) => {
    if (!usuario) return
    setDecidindoId(id); setMsg(null)
    try {
      await denunciaService.decidir(id, usuario.id, decisao)
      setMsg({ ok: true, texto: `Decisão aplicada: ${DECISAO_LABEL[decisao]}.` })
      await carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro ao decidir denúncia.' })
    } finally { setDecidindoId(null) }
  }

  if (!isAdmin()) {
    return (<><Navbar /><div className="app-container page"><div className="empty"><Ban size={40} /><h3>Acesso restrito</h3><p className="muted">Esta área é exclusiva de administradores.</p></div></div></>)
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head row" style={{ gap: 'var(--sp-3)' }}>
          <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><ShieldAlert size={24} /></span>
          <div><h1>Moderação de revendas</h1><p className="secondary">Avalie denúncias e aplique decisões sobre os anúncios.</p></div>
        </div>

        <div className="row wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-5)' }}>
          <div className="stat"><span className="stat__n" style={{ color: 'var(--warn)' }}>{pendentes}</span><span className="stat__l">Pendentes</span></div>
          <div className="stat"><span className="stat__n" style={{ color: 'oklch(0.45 0.12 150)' }}>{resolvidas}</span><span className="stat__l">Resolvidas</span></div>
          <div className="stat"><span className="stat__n">{denuncias.length}</span><span className="stat__l">Total</span></div>
        </div>

        <div className="between wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-4)' }}>
          <div className="segmented">
            {([['PENDENTE', 'Pendentes'], ['RESOLVIDA', 'Resolvidas'], ['TODAS', 'Todas']] as const).map(([v, t]) => (
              <button key={v} className={`segmented__opt${filtro === v ? ' segmented__opt--active' : ''}`} onClick={() => setFiltro(v)}>{t}</button>
            ))}
          </div>
        </div>

        {msg && <div className={`auth-alert ${msg.ok ? 'auth-alert--ok' : 'auth-alert--err'}`}>{msg.ok ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}{msg.texto}</div>}

        {carregando ? (
          <div className="stack" style={{ gap: 'var(--sp-3)' }}>{Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 160 }} />)}</div>
        ) : filtradas.length === 0 ? (
          <div className="empty"><Inbox size={40} /><h3>Tudo em ordem</h3><p className="muted">Nenhuma denúncia {filtro === 'PENDENTE' ? 'pendente' : filtro === 'RESOLVIDA' ? 'resolvida' : ''} no momento.</p></div>
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
                    <Info Icon={Tag} label="Motivo" value={MOTIVO_LABELS[d.motivo] ?? d.motivo} />
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
      </div>
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
