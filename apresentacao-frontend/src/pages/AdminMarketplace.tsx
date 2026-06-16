import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Store, Ban, Search, Calendar, MapPin, Ticket, ExternalLink } from 'lucide-react'
import Navbar from '../components/Navbar'
import { revendaService, eventoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Anuncio { id: number; preco: number; status: string; quantidade: number; vendedorId: number; compradorId: number | null; eventoId: number; ingressoIds: string[] }
interface Evento { id: number; nome: string; dataHora: string; local: string }

const STATUS: Record<string, { label: string; classe: string }> = {
  DISPONIVEL: { label: 'Disponível', classe: 'badge--success' },
  RESERVADO: { label: 'Em negociação', classe: 'badge--warn' },
  VENDIDO: { label: 'Vendido', classe: 'badge--brand' },
  CANCELADO: { label: 'Cancelado', classe: 'badge--danger' },
}

type Filtro = 'TODOS' | 'DISPONIVEL' | 'RESERVADO'

export default function AdminMarketplace() {
  const { isAdmin } = useAuth()
  const [anuncios, setAnuncios] = useState<Anuncio[]>([])
  const [eventosMap, setEventosMap] = useState<Map<number, Evento>>(new Map())
  const [filtro, setFiltro] = useState<Filtro>('TODOS')
  const [busca, setBusca] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(false)

  useEffect(() => {
    let vivo = true
    ;(async () => {
      setCarregando(true); setErro(false)
      try {
        const todos = await revendaService.todos().then(r => r.data as Anuncio[])
        const eventoIds = [...new Set(todos.map(a => a.eventoId))]
        const eventos = await Promise.all(
          eventoIds.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, Evento]).catch(() => null)),
        )
        if (!vivo) return
        setAnuncios(todos)
        setEventosMap(new Map(eventos.filter(Boolean) as [number, Evento][]))
      } catch {
        if (vivo) setErro(true)
      } finally {
        if (vivo) setCarregando(false)
      }
    })()
    return () => { vivo = false }
  }, [])

  const filtrados = useMemo(() => {
    let lista = filtro === 'TODOS' ? anuncios : anuncios.filter(a => a.status === filtro)
    const termo = busca.trim().toLowerCase()
    if (termo) lista = lista.filter(a => (eventosMap.get(a.eventoId)?.nome ?? '').toLowerCase().includes(termo))
    return lista
  }, [anuncios, eventosMap, filtro, busca])

  if (!isAdmin()) {
    return (<><Navbar /><div className="app-container page"><div className="empty"><Ban size={40} /><h3>Acesso restrito</h3><p className="muted">Esta área é exclusiva de administradores.</p></div></div></>)
  }

  const cont = (s: string) => anuncios.filter(a => a.status === s).length

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head row" style={{ gap: 'var(--sp-3)' }}>
          <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><Store size={24} /></span>
          <div>
            <h1>Marketplace de revendas</h1>
            <p className="secondary">Acompanhe todos os anúncios ativos da plataforma. Visão somente leitura para fiscalização.</p>
          </div>
        </div>

        <div className="between wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-4)' }}>
          <div className="segmented">
            {([['TODOS', `Todos (${anuncios.length})`], ['DISPONIVEL', `Disponíveis (${cont('DISPONIVEL')})`], ['RESERVADO', `Negociando (${cont('RESERVADO')})`]] as const).map(([v, t]) => (
              <button key={v} className={`segmented__opt${filtro === v ? ' segmented__opt--active' : ''}`} onClick={() => setFiltro(v)}>{t}</button>
            ))}
          </div>
          <div className="topbar__search" style={{ display: 'flex', minWidth: 240 }}>
            <Search size={17} />
            <input placeholder="Buscar por evento…" value={busca} onChange={e => setBusca(e.target.value)} aria-label="Buscar por evento" />
          </div>
        </div>

        {erro && <div className="auth-alert auth-alert--err">Não foi possível carregar o marketplace. Tente recarregar a página.</div>}

        {carregando ? (
          <div className="stack" style={{ gap: 'var(--sp-3)' }}>{Array.from({ length: 4 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 84 }} />)}</div>
        ) : filtrados.length === 0 ? (
          <div className="empty"><Store size={40} /><h3>Nenhum anúncio</h3><p className="muted">{busca ? `Nada encontrado para "${busca}".` : 'Não há anúncios neste filtro no momento.'}</p></div>
        ) : (
          <div className="stack" style={{ gap: 'var(--sp-3)' }}>
            {filtrados.map(a => {
              const ev = eventosMap.get(a.eventoId)
              const st = STATUS[a.status] ?? { label: a.status, classe: '' }
              return (
                <div key={a.id} className="surface row" style={{ gap: 'var(--sp-4)', padding: 'var(--sp-4) var(--sp-5)' }}>
                  <span className="list-ico list-ico--out" style={{ width: 46, height: 46 }}><Ticket size={20} /></span>
                  <div className="grow" style={{ minWidth: 0 }}>
                    <div className="row wrap" style={{ gap: 8, marginBottom: 3 }}>
                      <span className="truncate" style={{ fontWeight: 600 }}>{ev?.nome ?? `Evento #${a.eventoId}`}</span>
                      <span className={`badge ${st.classe}`}>{st.label}</span>
                    </div>
                    <div className="muted row wrap" style={{ gap: 'var(--sp-3)', fontSize: '0.8125rem' }}>
                      {ev?.dataHora && <span className="row" style={{ gap: 5 }}><Calendar size={13} />{new Date(ev.dataHora).toLocaleDateString('pt-BR')}</span>}
                      {ev?.local && <span className="row" style={{ gap: 5 }}><MapPin size={13} />{ev.local}</span>}
                      <span className="row" style={{ gap: 5 }}><Ticket size={13} />{a.quantidade} ingresso{a.quantidade > 1 ? 's' : ''}</span>
                      <span>Vendedor #{a.vendedorId}</span>
                    </div>
                  </div>
                  <div className="stack" style={{ alignItems: 'flex-end', gap: 6, flexShrink: 0 }}>
                    <span className="money" style={{ fontSize: '1.15rem', color: 'var(--brand-strong)' }}>{formatMoeda(a.preco)}</span>
                    <Link to={`/eventos/${a.eventoId}`} className="muted row" style={{ gap: 5, fontSize: '0.8125rem' }}>
                      Ver evento <ExternalLink size={13} />
                    </Link>
                  </div>
                </div>
              )
            })}
          </div>
        )}

        <p className="muted" style={{ fontSize: '0.8125rem', marginTop: 'var(--sp-5)', textAlign: 'center' }}>
          Para aplicar penalidades, abra a <Link to="/denuncias" style={{ color: 'var(--brand-strong)', fontWeight: 600 }}>fila de moderação</Link>.
        </p>
      </div>
    </>
  )
}
