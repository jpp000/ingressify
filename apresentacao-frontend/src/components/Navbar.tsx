import type { FormEvent } from 'react'
import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import {
  Compass, Dices, Repeat, Ticket, Wallet, CalendarDays,
  ScanLine, Armchair, ShieldAlert, Search, Plus, LogOut, ChevronDown,
  type LucideIcon,
} from 'lucide-react'
import { saldoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Destino {
  to: string
  label: string
  Icon: LucideIcon
  match: (p: string) => boolean
  visivel: (r: Papeis) => boolean
  state?: object
}

interface Papeis {
  comprador: boolean
  organizador: boolean
  operador: boolean
  admin: boolean
}

const DESTINOS: Destino[] = [
  { to: '/', label: 'Explorar', Icon: Compass, match: p => p === '/', visivel: () => true },
  { to: '/gerenciar', label: 'Meus eventos', Icon: CalendarDays, match: p => p.startsWith('/gerenciar'), visivel: r => r.organizador },
  { to: '/meus-ingressos', label: 'Carteira', Icon: Ticket, match: p => p.startsWith('/meus-ingressos') || p.startsWith('/revender') || p.startsWith('/revisao'), visivel: r => r.comprador },
  { to: '/revendas', label: 'Revendas', Icon: Repeat, match: p => p.startsWith('/revendas'), visivel: r => r.comprador },
  { to: '/sorteios', label: 'Sorteios', Icon: Dices, match: p => p.startsWith('/sorteios'), visivel: r => r.comprador || r.organizador },
  { to: '/check-in', label: 'Check-in', Icon: ScanLine, match: p => p.startsWith('/check-in'), visivel: r => r.operador || r.organizador || r.admin },
  { to: '/saldo', label: 'Saldo', Icon: Wallet, match: p => p === '/saldo', visivel: r => r.comprador },
  { to: '/mapa-assentos', label: 'Mapa de assentos', Icon: Armchair, match: p => p.startsWith('/mapa-assentos'), visivel: r => r.organizador || r.comprador },
  { to: '/denuncias', label: 'Denúncias', Icon: ShieldAlert, match: p => p.startsWith('/denuncias'), visivel: r => r.admin },
]

const MAX_PRIMARIOS = 5

function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/)
  return ((partes[0]?.[0] ?? '') + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase()
}

export default function Navbar() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { usuario, logout, isOrganizador, isComprador, isOperadorPorta, isAdmin } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [busca, setBusca] = useState('')
  const [menuAberto, setMenuAberto] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

  const papeis: Papeis = {
    comprador: isComprador(),
    organizador: isOrganizador(),
    operador: isOperadorPorta(),
    admin: isAdmin(),
  }

  const destinos = useMemo(() => DESTINOS.filter(d => d.visivel(papeis)), [papeis.comprador, papeis.organizador, papeis.operador, papeis.admin])
  const primarios = destinos.slice(0, MAX_PRIMARIOS)
  const secundarios = destinos.slice(MAX_PRIMARIOS)
  const homeLink = destinos[0]?.to ?? '/'

  useEffect(() => {
    if (papeis.comprador && usuario) {
      saldoService.obter(usuario.id).then(r => setSaldo(r.data.valor)).catch(() => {})
    }
  }, [papeis.comprador, usuario])

  useEffect(() => {
    const fechar = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) setMenuAberto(false)
    }
    document.addEventListener('mousedown', fechar)
    return () => document.removeEventListener('mousedown', fechar)
  }, [])

  useEffect(() => { setMenuAberto(false) }, [pathname])

  const submitBusca = (e: FormEvent) => {
    e.preventDefault()
    navigate(busca.trim() ? `/?busca=${encodeURIComponent(busca.trim())}` : '/')
  }

  const sair = () => { logout(); navigate('/login') }

  if (!usuario) return null

  return (
    <>
      <header className="topbar">
        <div className="app-container topbar__inner">
          <Link to={homeLink} className="brand" aria-label="Ingressify — início">
            Ingress<b>ify</b><span className="brand__dot" aria-hidden />
          </Link>

          <nav className="nav-primary" aria-label="Navegação principal">
            {primarios.map(({ to, label, Icon, match, state }) => (
              <Link key={to} to={to} state={state} className={`nav-link${match(pathname) ? ' nav-link--active' : ''}`}>
                <Icon size={18} />{label}
              </Link>
            ))}
          </nav>

          <form className="topbar__search" onSubmit={submitBusca} role="search">
            <Search size={17} />
            <input
              placeholder="Buscar eventos…"
              value={busca}
              onChange={e => setBusca(e.target.value)}
              aria-label="Buscar eventos"
            />
          </form>

          {papeis.comprador && saldo !== null && (
            <Link to="/saldo" className="saldo-pill" title="Seu saldo">
              <Wallet size={17} /><span className="money">{formatMoeda(saldo)}</span>
            </Link>
          )}

          {papeis.organizador && (
            <Link to="/gerenciar" state={{ aba: 'criar-evento' }}>
              <button className="btn btn--sm" type="button"><Plus size={17} />Criar evento</button>
            </Link>
          )}

          <div ref={menuRef} style={{ position: 'relative', flexShrink: 0 }}>
            <button className="avatar-btn" onClick={() => setMenuAberto(v => !v)} aria-haspopup="menu" aria-expanded={menuAberto}>
              <span className="avatar" aria-hidden>{iniciais(usuario.nome)}</span>
              <span className="avatar-btn__name truncate">{usuario.nome.split(' ')[0]}</span>
              <ChevronDown size={16} style={{ color: 'var(--ink-muted)' }} />
            </button>

            {menuAberto && (
              <div className="menu" role="menu">
                <div className="menu__header">
                  <div style={{ fontWeight: 600 }}>{usuario.nome}</div>
                  <div className="muted" style={{ fontSize: '0.8125rem' }}>{usuario.email}</div>
                  <div className="row wrap" style={{ gap: 6, marginTop: 8 }}>
                    {usuario.papeis.map(p => (
                      <span key={p} className="badge badge--brand" style={{ fontSize: '0.6875rem' }}>{p}</span>
                    ))}
                  </div>
                </div>
                <hr className="divider" style={{ margin: '4px 0' }} />
                {secundarios.map(({ to, label, Icon, match }) => (
                  <Link key={to} to={to} className={`menu__item${match(pathname) ? ' menu__item--active' : ''}`} role="menuitem">
                    <Icon size={18} />{label}
                  </Link>
                ))}
                <button className="menu__item menu__item--danger" onClick={sair} role="menuitem">
                  <LogOut size={18} />Sair
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      <nav className="bottomnav" aria-label="Navegação">
        {primarios.map(({ to, label, Icon, match, state }) => (
          <Link key={to} to={to} state={state} className={`bottomnav__item${match(pathname) ? ' bottomnav__item--active' : ''}`}>
            <Icon size={22} />{label}
          </Link>
        ))}
      </nav>
    </>
  )
}
