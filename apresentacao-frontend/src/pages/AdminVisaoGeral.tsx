import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ShieldAlert, ShieldCheck, Store, Ban, Clock, ArrowRight, Inbox, Tag, Activity,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import { denunciaService, revendaService, eventoService } from '../services/api'
import { useAuth } from '../context/AuthContext'

interface Denuncia { id: number; anuncioId: number; denuncianteId: number; motivo: string; descricao: string; status: string; decisao: string | null; criadaEm: string; decididaEm: string | null }
interface Anuncio { id: number; preco: number; status: string; vendedorId: number; eventoId: number }
interface Evento { id: number; nome: string; local: string }

const MOTIVO_LABELS: Record<string, string> = {
  PRECO_ABUSIVO: 'Preço abusivo', INGRESSO_SUSPEITO: 'Ingresso suspeito',
  COMPORTAMENTO_INADEQUADO: 'Comportamento inadequado', OUTRO: 'Outro',
}

export default function AdminVisaoGeral() {
  const { usuario, isAdmin } = useAuth()
  const [denuncias, setDenuncias] = useState<Denuncia[]>([])
  const [anunciosMap, setAnunciosMap] = useState<Map<number, Anuncio>>(new Map())
  const [eventosMap, setEventosMap] = useState<Map<number, Evento>>(new Map())
  const [marketplace, setMarketplace] = useState<Anuncio[]>([])
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    if (!usuario) return
    let vivo = true
    ;(async () => {
      setCarregando(true)
      const [lista, ativos] = await Promise.all([
        denunciaService.listar(usuario.id).then(r => r.data as Denuncia[]).catch(() => [] as Denuncia[]),
        revendaService.todos().then(r => r.data as Anuncio[]).catch(() => [] as Anuncio[]),
      ])
      // Anúncios referenciados por denúncias (para descobrir vendedor/evento)
      const anuncioIds = [...new Set(lista.map(d => d.anuncioId))]
      const anuncios = await Promise.all(
        anuncioIds.map(id => revendaService.detalhe(id).then(r => r.data as Anuncio).catch(() => null)),
      )
      const mapaA = new Map<number, Anuncio>()
      anuncios.forEach(a => { if (a) mapaA.set(a.id, a) })
      ativos.forEach(a => { if (!mapaA.has(a.id)) mapaA.set(a.id, a) })
      const eventoIds = [...new Set([...mapaA.values()].map(a => a.eventoId))]
      const eventos = await Promise.all(
        eventoIds.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, Evento]).catch(() => null)),
      )
      if (!vivo) return
      setDenuncias(lista)
      setAnunciosMap(mapaA)
      setMarketplace(ativos)
      setEventosMap(new Map(eventos.filter(Boolean) as [number, Evento][]))
      setCarregando(false)
    })()
    return () => { vivo = false }
  }, [usuario])

  const pendentes = useMemo(() => denuncias.filter(d => d.status === 'PENDENTE'), [denuncias])
  const bloqueados = useMemo(() => {
    const ids = new Set<number>()
    denuncias.filter(d => d.decisao === 'VENDEDOR_BLOQUEADO').forEach(d => {
      const a = anunciosMap.get(d.anuncioId)
      if (a) ids.add(a.vendedorId)
    })
    return [...ids]
  }, [denuncias, anunciosMap])

  const disponiveis = marketplace.filter(a => a.status === 'DISPONIVEL').length
  const reservados = marketplace.filter(a => a.status === 'RESERVADO').length

  const nomeEvento = (anuncioId: number) => {
    const a = anunciosMap.get(anuncioId)
    const ev = a ? eventosMap.get(a.eventoId) : undefined
    return ev?.nome ?? `Anúncio #${anuncioId}`
  }

  if (!isAdmin()) {
    return (<><Navbar /><div className="app-container page"><div className="empty"><Ban size={40} /><h3>Acesso restrito</h3><p className="muted">Esta área é exclusiva de administradores.</p></div></div></>)
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head between wrap" style={{ gap: 'var(--sp-3)' }}>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><ShieldCheck size={24} /></span>
            <div>
              <h1>Painel administrativo</h1>
              <p className="secondary">Olá, {usuario?.nome.split(' ')[0]} — visão geral da moderação e da saúde do marketplace.</p>
            </div>
          </div>
          <Link to="/denuncias" className="btn btn--sm"><ShieldAlert size={16} /> Ir para moderação</Link>
        </div>

        <div className="row wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-6)' }}>
          <Stat n={pendentes.length} label="Denúncias pendentes" cor="var(--warn)" carregando={carregando} />
          <Stat n={disponiveis} label="Anúncios disponíveis" carregando={carregando} />
          <Stat n={reservados} label="Em negociação" carregando={carregando} />
          <Stat n={bloqueados.length} label="Vendedores bloqueados" cor="var(--danger)" carregando={carregando} />
        </div>

        <div className="two-col">
          {/* Fila de moderação */}
          <section className="surface" aria-labelledby="fila-mod">
            <div className="between" style={{ padding: 'var(--sp-4) var(--sp-5)', borderBottom: '1px solid var(--border)' }}>
              <div className="row" style={{ gap: 10 }}>
                <ShieldAlert size={18} style={{ color: 'var(--warn)' }} />
                <h2 id="fila-mod" style={{ fontSize: '1.0625rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>Fila de moderação</h2>
              </div>
              {pendentes.length > 0 && <span className="badge badge--warn">{pendentes.length} pendente{pendentes.length > 1 ? 's' : ''}</span>}
            </div>

            {carregando ? (
              <div className="stack" style={{ padding: 'var(--sp-4)', gap: 'var(--sp-3)' }}>
                {Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 64 }} />)}
              </div>
            ) : pendentes.length === 0 ? (
              <div className="empty" style={{ padding: 'var(--sp-7) var(--sp-4)' }}>
                <Inbox size={36} /><h3>Tudo em ordem</h3>
                <p className="muted">Nenhuma denúncia aguardando análise.</p>
              </div>
            ) : (
              <div>
                {pendentes.slice(0, 6).map(d => (
                  <Link key={d.id} to="/denuncias" className="list-row mod-row">
                    <span className="list-ico" style={{ background: 'var(--warn-soft)', color: 'oklch(0.45 0.12 70)', width: 40, height: 40 }}>
                      <Tag size={17} />
                    </span>
                    <div className="grow" style={{ minWidth: 0 }}>
                      <div className="truncate" style={{ fontWeight: 600 }}>{nomeEvento(d.anuncioId)}</div>
                      <div className="muted row" style={{ gap: 6, fontSize: '0.8125rem' }}>
                        {MOTIVO_LABELS[d.motivo] ?? d.motivo}
                        <span aria-hidden>·</span>
                        <Clock size={12} /> {new Date(d.criadaEm).toLocaleDateString('pt-BR')}
                      </div>
                    </div>
                    <ArrowRight size={17} style={{ color: 'var(--ink-muted)', flexShrink: 0 }} />
                  </Link>
                ))}
                {pendentes.length > 6 && (
                  <Link to="/denuncias" className="list-row" style={{ justifyContent: 'center', color: 'var(--brand-strong)', fontWeight: 600, fontSize: '0.9375rem' }}>
                    Ver todas as {pendentes.length} denúncias
                  </Link>
                )}
              </div>
            )}
          </section>

          {/* Lateral */}
          <div className="stack sticky-side" style={{ gap: 'var(--sp-4)' }}>
            <section className="surface surface--pad">
              <div className="row" style={{ gap: 10, marginBottom: 'var(--sp-4)' }}>
                <Ban size={18} style={{ color: 'var(--danger)' }} />
                <h2 style={{ fontSize: '1.0625rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>Vendedores bloqueados</h2>
              </div>
              {carregando ? (
                <div className="skeleton" style={{ height: 48 }} />
              ) : bloqueados.length === 0 ? (
                <p className="muted" style={{ fontSize: '0.9375rem' }}>Nenhum vendedor bloqueado. O controle antifraude está sem penalidades ativas.</p>
              ) : (
                <div className="stack" style={{ gap: 'var(--sp-2)' }}>
                  {bloqueados.map(id => (
                    <div key={id} className="row" style={{ gap: 10, padding: '8px 10px', background: 'var(--danger-soft)', borderRadius: 'var(--r-md)' }}>
                      <Ban size={16} style={{ color: 'var(--danger)', flexShrink: 0 }} />
                      <span style={{ fontWeight: 600 }}>Usuário #{id}</span>
                      <span className="badge badge--danger" style={{ marginLeft: 'auto' }}>Bloqueado</span>
                    </div>
                  ))}
                </div>
              )}
            </section>

            <section className="surface surface--pad">
              <div className="row" style={{ gap: 10, marginBottom: 'var(--sp-4)' }}>
                <Activity size={18} style={{ color: 'var(--brand-strong)' }} />
                <h2 style={{ fontSize: '1.0625rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>Saúde do marketplace</h2>
              </div>
              <div className="stack" style={{ gap: 'var(--sp-3)' }}>
                <SaudeRow label="Anúncios disponíveis" valor={disponiveis} />
                <SaudeRow label="Em negociação (reservados)" valor={reservados} />
                <SaudeRow label="Total de anúncios ativos" valor={marketplace.length} forte />
              </div>
              <Link to="/admin/marketplace" className="btn btn--ghost btn--block btn--sm" style={{ marginTop: 'var(--sp-4)' }}>
                <Store size={16} /> Ver marketplace completo
              </Link>
            </section>
          </div>
        </div>
      </div>
    </>
  )
}

function Stat({ n, label, cor, carregando }: { n: number; label: string; cor?: string; carregando: boolean }) {
  return (
    <div className="stat">
      <span className="stat__n" style={{ color: cor }}>{carregando ? '—' : n}</span>
      <span className="stat__l">{label}</span>
    </div>
  )
}

function SaudeRow({ label, valor, forte }: { label: string; valor: number; forte?: boolean }) {
  return (
    <div className="between" style={{ paddingBlock: 2 }}>
      <span className={forte ? '' : 'secondary'} style={{ fontSize: '0.9375rem', fontWeight: forte ? 600 : 400 }}>{label}</span>
      <span className="money" style={{ fontSize: '1.05rem' }}>{valor}</span>
    </div>
  )
}
