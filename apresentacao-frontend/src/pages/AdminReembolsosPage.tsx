import { useEffect, useMemo, useState } from 'react'
import { RotateCcw, AlertTriangle, CheckCircle2, Ban, FlaskConical, Inbox, Clock, User, Tag, } from 'lucide-react'
import Navbar from '../components/Navbar'
import { reembolsoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Reembolso {
  id: number; ingressoId: string; solicitanteId: number; motivo: string;
  status: string; valor: number; criadaEm: string; decididaEm: string | null
}

const MOCK: Reembolso[] = [
  { id: 1, ingressoId: 'uuid-1', solicitanteId: 3, motivo: 'VOLUNTARIO', status: 'PENDENTE', valor: 120.00, criadaEm: '2026-06-15T10:00:00', decididaEm: null },
  { id: 2, ingressoId: 'uuid-2', solicitanteId: 4, motivo: 'VOLUNTARIO', status: 'EM_ANALISE', valor: 250.00, criadaEm: '2026-06-14T09:30:00', decididaEm: null },
  { id: 3, ingressoId: 'uuid-3', solicitanteId: 5, motivo: 'EVENTO_CANCELADO', status: 'APROVADA', valor: 180.00, criadaEm: '2026-06-12T11:00:00', decididaEm: '2026-06-13T14:00:00' },
  { id: 4, ingressoId: 'uuid-4', solicitanteId: 6, motivo: 'VOLUNTARIO', status: 'RECUSADA', valor: 90.00, criadaEm: '2026-06-10T08:00:00', decididaEm: '2026-06-11T12:00:00' },
]

const STATUS_LABEL: Record<string, string> = {
  PENDENTE: 'Pendente', EM_ANALISE: 'Em análise', APROVADA: 'Aprovada', RECUSADA: 'Recusada', CANCELADA: 'Cancelada',
}
const STATUS_BADGE: Record<string, string> = {
  PENDENTE: 'badge--warn', EM_ANALISE: 'badge--brand', APROVADA: 'badge--success', RECUSADA: 'badge--danger', CANCELADA: 'badge--danger',
}
const MOTIVO_LABEL: Record<string, string> = { VOLUNTARIO: 'Voluntário', EVENTO_CANCELADO: 'Evento cancelado' }

type Filtro = 'TODAS' | 'PENDENTE' | 'EM_ANALISE' | 'APROVADA' | 'RECUSADA'

export default function AdminReembolsosPage() {
  const { usuario, isAdmin } = useAuth()
  const [reembolsos, setReembolsos] = useState<Reembolso[]>([])
  const [filtro, setFiltro] = useState<Filtro>('PENDENTE')
  const [carregando, setCarregando] = useState(true)
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [agindoId, setAgindoId] = useState<number | null>(null)
  const [usandoMock, setUsandoMock] = useState(false)

  const ativarMock = () => {
    setReembolsos(MOCK); setUsandoMock(true); setFiltro('TODAS')
    setMsg({ ok: true, texto: 'Dados demo carregados.' }); setTimeout(() => setMsg(null), 4000)
  }

  const carregar = async () => {
    if (!usuario || usandoMock) return
    setCarregando(true); setMsg(null)
    try {
      const res = await reembolsoService.listarTodos(usuario.id)
      setReembolsos(res.data)
    } catch { setMsg({ ok: false, texto: 'Erro ao carregar reembolsos.' }) }
    finally { setCarregando(false) }
  }
  useEffect(() => { carregar() }, [usuario])

  const pendentes = reembolsos.filter(r => r.status === 'PENDENTE').length
  const emAnalise = reembolsos.filter(r => r.status === 'EM_ANALISE').length
  const filtrados = useMemo(() => filtro === 'TODAS' ? reembolsos : reembolsos.filter(r => r.status === filtro), [reembolsos, filtro])

  const agir = async (id: number, acao: 'analisar' | 'aprovar' | 'recusar') => {
    if (!usuario) return
    setAgindoId(id); setMsg(null)
    if (usandoMock) {
      const novoStatus = acao === 'analisar' ? 'EM_ANALISE' : acao === 'aprovar' ? 'APROVADA' : 'RECUSADA'
      setReembolsos(prev => prev.map(r => r.id === id ? { ...r, status: novoStatus, decididaEm: acao !== 'analisar' ? new Date().toISOString() : null } : r))
      setMsg({ ok: true, texto: '[Demo] Ação aplicada.' }); setTimeout(() => setMsg(null), 3000); setAgindoId(null); return
    }
    try {
      if (acao === 'analisar') await reembolsoService.analisar(id, usuario.id)
      else if (acao === 'aprovar') await reembolsoService.aprovar(id, usuario.id)
      else await reembolsoService.recusar(id, usuario.id)
      setMsg({ ok: true, texto: 'Ação aplicada.' }); await carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro.' })
    } finally { setAgindoId(null) }
  }

  if (!isAdmin()) {
    return (<><Navbar /><div className="app-container page"><div className="empty"><Ban size={40} /><h3>Acesso restrito</h3><p className="muted">Esta área é exclusiva de administradores.</p></div></div></>)
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head row" style={{ gap: 'var(--sp-3)' }}>
          <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><RotateCcw size={24} /></span>
          <div><h1>Gestão de Reembolsos</h1><p className="secondary">Analise, aprove ou recuse solicitações de reembolso.</p></div>
        </div>

        <div className="row wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-5)' }}>
          <div className="stat"><span className="stat__n" style={{ color: 'var(--warn)' }}>{pendentes}</span><span className="stat__l">Pendentes</span></div>
          <div className="stat"><span className="stat__n" style={{ color: 'var(--brand-strong)' }}>{emAnalise}</span><span className="stat__l">Em análise</span></div>
          <div className="stat"><span className="stat__n">{reembolsos.length}</span><span className="stat__l">Total</span></div>
        </div>

        <div className="between wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-4)' }}>
          <div className="segmented" style={{ flexWrap: 'wrap' }}>
            {([['PENDENTE', 'Pendentes'], ['EM_ANALISE', 'Em análise'], ['APROVADA', 'Aprovadas'], ['RECUSADA', 'Recusadas'], ['TODAS', 'Todas']] as const).map(([v, t]) => (
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
        ) : filtrados.length === 0 ? (
          <div className="empty">
            <Inbox size={40} /><h3>Nenhuma solicitação</h3>
            <p className="muted">Não há reembolsos {filtro !== 'TODAS' ? `com status "${STATUS_LABEL[filtro]}"` : ''} no momento.</p>
            {!usandoMock && <button className="btn btn--sm btn--ghost" onClick={ativarMock} style={{ marginTop: 'var(--sp-3)' }}><FlaskConical size={15} /> Dados demo</button>}
          </div>
        ) : (
          <div className="stack" style={{ gap: 'var(--sp-4)' }}>
            {filtrados.map(r => {
              const finalizado = r.status === 'APROVADA' || r.status === 'RECUSADA' || r.status === 'CANCELADA'
              return (
                <div key={r.id} className="surface surface--pad">
                  <div className="between wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-3)' }}>
                    <div className="row" style={{ gap: 'var(--sp-2)' }}>
                      <span className="muted" style={{ fontWeight: 600 }}>#{r.id}</span>
                      <span className={`badge ${STATUS_BADGE[r.status] ?? 'badge--warn'}`}>{STATUS_LABEL[r.status] ?? r.status}</span>
                    </div>
                    <span className="muted row" style={{ gap: 6, fontSize: '0.8125rem' }}><Clock size={14} /> {new Date(r.criadaEm).toLocaleDateString('pt-BR')}</span>
                  </div>
                  <div className="den-grid">
                    <InfoLine Icon={User} label="Solicitante" value={`Usuário #${r.solicitanteId}`} />
                    <InfoLine Icon={Tag} label="Motivo" value={MOTIVO_LABEL[r.motivo] ?? r.motivo} />
                    <InfoLine Icon={RotateCcw} label="Valor" value={formatMoeda(r.valor)} />
                    {r.decididaEm && <InfoLine Icon={CheckCircle2} label="Decidido em" value={new Date(r.decididaEm).toLocaleDateString('pt-BR')} />}
                  </div>
                  <div className="muted" style={{ fontSize: '0.75rem', marginTop: 'var(--sp-2)' }}>Ingresso: {r.ingressoId}</div>
                  {!finalizado && (
                    <div className="row wrap" style={{ gap: 'var(--sp-2)', marginTop: 'var(--sp-4)' }}>
                      {r.status === 'PENDENTE' && (
                        <button className="btn btn--sm btn--ghost" disabled={agindoId === r.id} onClick={() => agir(r.id, 'analisar')}>
                          <FlaskConical size={16} /> Iniciar análise
                        </button>
                      )}
                      <button className="btn btn--sm btn--ghost" disabled={agindoId === r.id} onClick={() => agir(r.id, 'aprovar')}>
                        <CheckCircle2 size={16} /> Aprovar
                      </button>
                      <button className="btn btn--sm btn--danger" disabled={agindoId === r.id} onClick={() => agir(r.id, 'recusar')}>
                        <Ban size={16} /> Recusar
                      </button>
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

function InfoLine({ Icon, label, value }: { Icon: typeof Tag; label: string; value: string }) {
  return (
    <div className="row" style={{ gap: 10, alignItems: 'flex-start' }}>
      <Icon size={16} style={{ color: 'var(--ink-muted)', marginTop: 2, flexShrink: 0 }} />
      <div><div className="muted" style={{ fontSize: '0.75rem' }}>{label}</div><div style={{ fontWeight: 500 }}>{value}</div></div>
    </div>
  )
}
