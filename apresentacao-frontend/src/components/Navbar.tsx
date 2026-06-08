import type { CSSProperties, KeyboardEvent } from 'react'
import { useEffect, useRef, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { saldoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface NavItem {
  to: string
  label: string
  icon: string
  match: (pathname: string) => boolean
  state?: object
}

function montarNavegacao(
  isOrganizador: boolean,
  isComprador: boolean,
  isOperadorPorta: boolean,
  isAdmin: boolean,
): { principal: NavItem[]; rapido: NavItem[] } {
  const explorar: NavItem = { to: '/', label: 'Explorar', icon: '🔍', match: p => p === '/' }
  const sorteios: NavItem = { to: '/sorteios', label: 'Sorteios', icon: '🎲', match: p => p.startsWith('/sorteios') }
  const revendas: NavItem = { to: '/revendas', label: 'Revendas', icon: '🔄', match: p => p.startsWith('/revendas') }
  const carteira: NavItem = { to: '/meus-ingressos', label: 'Carteira', icon: '🎟️', match: p => p.startsWith('/meus-ingressos') || p.startsWith('/revender') }
  const saldo: NavItem = { to: '/saldo', label: 'Saldo', icon: '💳', match: p => p === '/saldo' }
  const eventos: NavItem = { to: '/gerenciar', label: 'Meus Eventos', icon: '📅', match: p => p.startsWith('/gerenciar') }
  const checkin: NavItem = { to: '/check-in', label: 'Check-in', icon: '🎫', match: p => p.startsWith('/check-in') }
  const denuncias: NavItem = { to: '/denuncias', label: 'Denúncias', icon: '🚨', match: p => p.startsWith('/denuncias') }
  const mapa: NavItem = { to: '/mapa-assentos', label: 'Mapa de Assentos', icon: '💺', match: p => p.startsWith('/mapa-assentos') }

  const gestor = isOrganizador || isAdmin || isOperadorPorta

  if (gestor) {
    const principal: NavItem[] = []
    const rapido: NavItem[] = [explorar, sorteios, mapa]

    if (isAdmin) principal.push(denuncias)
    if (isOrganizador) principal.push(eventos)
    if (isOrganizador || isAdmin || isOperadorPorta) principal.push(checkin)
    if (isComprador) rapido.push(revendas, carteira, saldo)

    return { principal, rapido }
  }

  if (isComprador) {
    return {
      principal: [explorar, revendas, carteira, saldo],
      rapido: [sorteios, mapa],
    }
  }

  return { principal: [explorar, sorteios, mapa], rapido: [] }
}

export default function Navbar() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { usuario, logout, isOrganizador, isComprador, isOperadorPorta, isAdmin } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [busca, setBusca] = useState('')
  const [menuRapidoAberto, setMenuRapidoAberto] = useState(false)
  const menuRapidoRef = useRef<HTMLDivElement>(null)

  const org = isOrganizador()
  const comp = isComprador()
  const op = isOperadorPorta()
  const adm = isAdmin()
  const { principal, rapido } = montarNavegacao(org, comp, op, adm)

  const algumRapidoAtivo = rapido.some(item => item.match(pathname))

  useEffect(() => {
    if (comp && usuario) {
      saldoService.obter(usuario.id).then(r => setSaldo(r.data.valor)).catch(() => {})
    }
  }, [comp, usuario])

  useEffect(() => {
    const fechar = (e: MouseEvent) => {
      if (menuRapidoRef.current && !menuRapidoRef.current.contains(e.target as Node)) {
        setMenuRapidoAberto(false)
      }
    }
    document.addEventListener('mousedown', fechar)
    return () => document.removeEventListener('mousedown', fechar)
  }, [])

  const handleBusca = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && busca.trim()) {
      navigate(`/?busca=${encodeURIComponent(busca.trim())}`)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const linkStyle = (active: boolean): CSSProperties => ({
    color: active ? '#1d4ed8' : '#64748b',
    fontWeight: active ? 600 : 400,
    fontSize: 13,
    padding: '4px 0',
    borderBottom: active ? '2px solid #1d4ed8' : '2px solid transparent',
    transition: 'color 0.15s',
    whiteSpace: 'nowrap',
  })

  const homeLink = org ? '/gerenciar' : adm ? '/denuncias' : op ? '/check-in' : '/'

  return (
    <nav style={{
      background: '#fff',
      borderBottom: '1px solid #e2e8f0',
      position: 'sticky',
      top: 0,
      zIndex: 100,
    }}>
      <div style={{
        maxWidth: 1280,
        margin: '0 auto',
        padding: '0 24px',
        minHeight: 64,
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        flexWrap: 'wrap',
      }}>
        <Link to={homeLink} style={{ color: '#1e3a8a', fontWeight: 800, fontSize: 20, flexShrink: 0 }}>
          Ingressefy
        </Link>

        <div style={{ display: 'flex', gap: 20, alignItems: 'center', flexWrap: 'wrap', flex: 1 }}>
          {adm && !principal.some(l => l.to === '/denuncias') && (
            <Link to="/denuncias" style={linkStyle(pathname.startsWith('/denuncias'))}>
              Denúncias
            </Link>
          )}
          {principal.map(link => (
            <Link key={link.to + link.label} to={link.to} state={link.state} style={linkStyle(link.match(pathname))}>
              {link.label}
            </Link>
          ))}

          {rapido.length > 0 && (
            <div ref={menuRapidoRef} style={{ position: 'relative' }}>
              <button
                type="button"
                onClick={() => setMenuRapidoAberto(v => !v)}
                style={{
                  ...linkStyle(algumRapidoAtivo || menuRapidoAberto),
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: 4,
                  padding: '4px 0',
                }}
              >
                Acesso rápido
                <span style={{ fontSize: 10, opacity: 0.7 }}>{menuRapidoAberto ? '▲' : '▼'}</span>
              </button>

              {menuRapidoAberto && (
                <div style={{
                  position: 'absolute',
                  top: 'calc(100% + 8px)',
                  left: 0,
                  minWidth: 200,
                  background: '#fff',
                  border: '1px solid #e2e8f0',
                  borderRadius: 12,
                  boxShadow: '0 8px 24px rgba(0,0,0,0.1)',
                  padding: 8,
                  zIndex: 200,
                }}>
                  {rapido.map(item => (
                    <Link
                      key={item.to + item.label}
                      to={item.to}
                      state={item.state}
                      onClick={() => setMenuRapidoAberto(false)}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                        padding: '10px 12px',
                        borderRadius: 8,
                        textDecoration: 'none',
                        fontSize: 13,
                        fontWeight: item.match(pathname) ? 600 : 400,
                        color: item.match(pathname) ? '#1d4ed8' : '#475569',
                        background: item.match(pathname) ? '#eff6ff' : 'transparent',
                      }}
                    >
                      <span>{item.icon}</span>
                      {item.label}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {comp && saldo !== null && (
          <Link to="/saldo" style={{ textDecoration: 'none', flexShrink: 0 }}>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              background: pathname === '/saldo' ? '#dbeafe' : '#eff6ff',
              border: '1px solid #bfdbfe',
              borderRadius: 24,
              padding: '5px 14px',
            }}>
              <span style={{ fontSize: 14 }}>💳</span>
              <span style={{ color: '#1d4ed8', fontWeight: 700, fontSize: 13 }}>
                {formatMoeda(saldo)}
              </span>
            </div>
          </Link>
        )}

        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          background: '#f1f5f9',
          borderRadius: 24,
          padding: '7px 14px',
          border: '1px solid #e2e8f0',
          flexShrink: 0,
        }}>
          <span style={{ color: '#94a3b8', fontSize: 13 }}>🔍</span>
          <input
            placeholder="Buscar eventos..."
            value={busca}
            onChange={e => setBusca(e.target.value)}
            onKeyDown={handleBusca}
            style={{
              border: 'none',
              background: 'transparent',
              outline: 'none',
              fontSize: 13,
              color: '#1e293b',
              width: 140,
            }}
          />
        </div>

        {org && (
          <Link to="/gerenciar" state={{ aba: 'criar-evento' }} style={{ flexShrink: 0 }}>
            <button style={{
              background: '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: 24,
              padding: '7px 18px',
              fontSize: 13,
              fontWeight: 600,
              cursor: 'pointer',
            }}>
              + Criar Evento
            </button>
          </Link>
        )}

        {usuario && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexShrink: 0 }}>
            <div style={{ fontSize: 12, color: '#475569', maxWidth: 100, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {usuario.nome}
              {adm && <span style={{ marginLeft: 4, fontSize: 10, color: '#1d4ed8', fontWeight: 600 }}>ADMIN</span>}
            </div>
            <button
              onClick={handleLogout}
              title="Sair"
              style={{
                background: '#f1f5f9',
                border: '1px solid #e2e8f0',
                borderRadius: '50%',
                width: 34,
                height: 34,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 15,
                color: '#64748b',
                cursor: 'pointer',
              }}
            >
              🚪
            </button>
          </div>
        )}
      </div>
    </nav>
  )
}
