import type { FormEvent } from 'react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import {
  Compass, Dices, Repeat, Ticket, Wallet, CalendarDays,
  ScanLine, Armchair, ShieldAlert, Search, Plus, LogOut, ChevronDown, MoreHorizontal,
  type LucideIcon,
} from 'lucide-react'
import { saldoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Papeis { comprador: boolean; organizador: boolean; operador: boolean; admin: boolean }
interface Destino {
  to: string
  label: string
  Icon: LucideIcon
  match: (p: string) => boolean
  visivel: (r: Papeis) => boolean
  state?: object
}

const DESTINOS: Destino[] = [
  { to: '/', label: 'Explorar', Icon: Compass, match: p => p === '/', visivel: () => true },
  { to: '/gerenciar', label: 'Meus eventos', Icon: CalendarDays, match: p => p.startsWith('/gerenciar'), visivel: r => r.organizador },
  { to: '/meus-ingressos', label: 'Carteira', Icon: Ticket, match: p => p.startsWith('/meus-ingressos') || p.startsWith('/revender') || p.startsWith('/revisao'), visivel: r => r.comprador },
  { to: '/revendas', label: 'Revendas', Icon: Repeat, match: p => p.startsWith('/revendas'), visivel: r => r.comprador },
  { to: '/sorteios', label: 'Sorteios', Icon: Dices, match: p => p.startsWith('/sorteios'), visivel: r => r.comprador || r.organizador },
  { to: '/check-in', label: 'Check-in', Icon: ScanLine, match: p => p.startsWith('/check-in'), visivel: r => r.operador || r.organizador || r.admin },
  { to: '/mapa-assentos', label: 'Mapa de assentos', Icon: Armchair, match: p => p.startsWith('/mapa-assentos'), visivel: r => r.organizador || r.comprador },
  { to: '/saldo', label: 'Saldo', Icon: Wallet, match: p => p === '/saldo', visivel: r => r.comprador },
  { to: '/denuncias', label: 'Denúncias', Icon: ShieldAlert, match: p => p.startsWith('/denuncias'), visivel: r => r.admin },
]

const MAX_INLINE = 4

function iniciais(nome: string): string {
  const p = nome.trim().split(/\s+/)
  return ((p[0]?.[0] ?? '') + (p.length > 1 ? p[p.length - 1][0] : '')).toUpperCase()
}

export default function Navbar() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { usuario, logout, isOrganizador, isComprador, isOperadorPorta, isAdmin } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [busca, setBusca] = useState('')
  const [aberto, setAberto] = useState<'perfil' | 'mais' | null>(null)
  const navRef = useRef<HTMLElement>(null)

  const papeis: Papeis = { comprador: isComprador(), organizador: isOrganizador(), operador: isOperadorPorta(), admin: isAdmin() }
  const destinos = useMemo(() => DESTINOS.filter(d => d.visivel(papeis)),
    [papeis.comprador, papeis.organizador, papeis.operador, papeis.admin])

  // se houver overflow, o último slot inline vira o botão "Mais"
  const temMais = destinos.length > MAX_INLINE
  const inline = temMais ? destinos.slice(0, MAX_INLINE - 1) : destinos.slice(0, MAX_INLINE)
  const extras = temMais ? destinos.slice(MAX_INLINE - 1) : []
  const primariosMobile = destinos.slice(0, 4)
  const homeLink = destinos[0]?.to ?? '/'

  useEffect(() => {
    if (papeis.comprador && usuario) saldoService.obter(usuario.id).then(r => setSaldo(r.data.valor)).catch(() => {})
  }, [papeis.comprador, usuario])

  useEffect(() => {
    const fechar = (e: MouseEvent) => { if (navRef.current && !navRef.current.contains(e.target as Node)) setAberto(null) }
    document.addEventListener('mousedown', fechar)
    return () => document.removeEventListener('mousedown', fechar)
  }, [])
  useEffect(() => { setAberto(null) }, [pathname])

  const submitBusca = (e: FormEvent) => { e.preventDefault(); navigate(busca.trim() ? `/?busca=${encodeURIComponent(busca.trim())}` : '/') }
  const sair = () => { logout(); navigate('/login') }

  if (!usuario) return null
  const algumExtraAtivo = extras.some(d => d.match(pathname))

  return (
    <>
      <header className="topbar" ref={navRef}>
        <div className="app-container topbar__inner">
          <Link to={homeLink} className="brand" aria-label="Ingressify — início">
            Ingress<b>ify</b><span className="brand__dot" aria-hidden />
          </Link>

          <nav className="nav-primary" aria-label="Navegação principal">
            {inline.map(({ to, label, Icon, match, state }) => (
              <Link key={to} to={to} state={state} className={`nav-link${match(pathname) ? ' nav-link--active' : ''}`}>
                <Icon size={18} /><span className="nav-link__t">{label}</span>
              </Link>
            ))}
            {temMais && (
              <div style={{ position: 'relative' }}>
                <button
                  className={`nav-link${algumExtraAtivo || aberto === 'mais' ? ' nav-link--active' : ''}`}
                  onClick={() => setAberto(a => a === 'mais' ? null : 'mais')}
                  aria-haspopup="menu" aria-expanded={aberto === 'mais'}
                >
                  <MoreHorizontal size={18} /><span className="nav-link__t">Mais</span>
                </button>
                {aberto === 'mais' && (
                  <div className="menu" style={{ left: 0, right: 'auto', minWidth: 220 }} role="menu">
                    {extras.map(({ to, label, Icon, match, state }) => (
                      <Link key={to} to={to} state={state} className={`menu__item${match(pathname) ? ' menu__item--active' : ''}`} role="menuitem">
                        <Icon size={18} />{label}
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            )}
          </nav>

          <form className="topbar__search" onSubmit={submitBusca} role="search">
            <Search size={17} />
            <input placeholder="Buscar eventos…" value={busca} onChange={e => setBusca(e.target.value)} aria-label="Buscar eventos" />
          </form>

          {papeis.comprador && saldo !== null && (
            <Link to="/saldo" className="saldo-pill" title="Seu saldo">
              <Wallet size={17} /><span className="money">{formatMoeda(saldo)}</span>
            </Link>
          )}

          {papeis.organizador && (
            <Link to="/gerenciar" state={{ aba: 'criar-evento' }} className="topbar__cta">
              <button className="btn btn--sm" type="button"><Plus size={17} /><span className="nav-link__t">Criar evento</span></button>
            </Link>
          )}

          <div style={{ position: 'relative', flexShrink: 0 }}>
            <button className="avatar-btn" onClick={() => setAberto(a => a === 'perfil' ? null : 'perfil')} aria-haspopup="menu" aria-expanded={aberto === 'perfil'}>
              <span className="avatar" aria-hidden>{iniciais(usuario.nome)}</span>
              <span className="avatar-btn__name truncate">{usuario.nome.split(' ')[0]}</span>
              <ChevronDown size={16} style={{ color: 'var(--ink-muted)' }} />
            </button>
            {aberto === 'perfil' && (
              <div className="menu" role="menu">
                <div className="menu__header">
                  <div style={{ fontWeight: 600 }}>{usuario.nome}</div>
                  <div className="muted" style={{ fontSize: '0.8125rem' }}>{usuario.email}</div>
                  <div className="row wrap" style={{ gap: 6, marginTop: 8 }}>
                    {usuario.papeis.map(p => <span key={p} className="badge badge--brand" style={{ fontSize: '0.6875rem' }}>{p}</span>)}
                  </div>
                </div>
                <hr className="divider" style={{ margin: '4px 0' }} />
                <button className="menu__item menu__item--danger" onClick={sair} role="menuitem"><LogOut size={18} />Sair</button>
              </div>
            )}
          </div>
        </div>
      </header>

      <nav className="bottomnav" aria-label="Navegação">
        {primariosMobile.map(({ to, label, Icon, match, state }) => (
          <Link key={to} to={to} state={state} className={`bottomnav__item${match(pathname) ? ' bottomnav__item--active' : ''}`}>
            <Icon size={22} /><span>{label}</span>
          </Link>
        ))}
      </nav>
    </>
  )
}
