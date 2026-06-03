import type { CSSProperties, KeyboardEvent } from 'react'
import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { saldoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface NavbarProps {
  variant?: 'default' | 'organizer' | 'minimal'
}

export default function Navbar({ variant = 'default' }: NavbarProps) {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { usuario, logout, isOrganizador } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [busca, setBusca] = useState('')

  const isWallet = pathname === '/meus-ingressos'

  useEffect(() => {
    if (isWallet && usuario) {
      saldoService.obter(usuario.id).then(r => setSaldo(r.data.valor)).catch(() => {})
    }
  }, [isWallet, usuario])

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
    fontSize: 14,
    paddingBottom: 4,
    borderBottom: active ? '2px solid #1d4ed8' : '2px solid transparent',
    transition: 'color 0.15s',
  })

  const modoOrganizador = variant === 'organizer' || isOrganizador()

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
        height: 64,
        display: 'flex',
        alignItems: 'center',
        gap: 24,
      }}>
        <Link to={modoOrganizador ? '/gerenciar' : '/'} style={{ color: '#1e3a8a', fontWeight: 800, fontSize: 20, flexShrink: 0 }}>
          Ingressefy
        </Link>

        <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
          {modoOrganizador ? (
            <>
              <Link to="/" style={linkStyle(pathname === '/')}>Explorar</Link>
              <Link to="/gerenciar" style={linkStyle(pathname.startsWith('/gerenciar'))}>Meus Eventos</Link>
            </>
          ) : (
            <>
              <Link to="/" style={linkStyle(pathname === '/')}>Explorar</Link>
              <Link to="/meus-ingressos" style={linkStyle(isWallet)}>Carteira</Link>
              <Link to="/revendas" style={linkStyle(pathname === '/revendas')}>Revendas</Link>
              <Link to="/saldo" style={linkStyle(pathname === '/saldo')}>Saldo</Link>
            </>
          )}
        </div>

        <div style={{ flex: 1 }} />

        {isWallet && saldo !== null && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            background: '#eff6ff',
            border: '1px solid #bfdbfe',
            borderRadius: 24,
            padding: '6px 16px',
            cursor: 'pointer',
          }}>
            <span style={{ fontSize: 18 }}>💳</span>
            <div>
              <div style={{ fontSize: 10, color: '#64748b', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Saldo Disponível
              </div>
              <div style={{ color: '#1d4ed8', fontWeight: 700, fontSize: 15 }}>
                {formatMoeda(saldo)}
              </div>
            </div>
          </div>
        )}

        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 8,
          background: '#f1f5f9',
          borderRadius: 24,
          padding: '8px 16px',
          border: '1px solid #e2e8f0',
        }}>
          <span style={{ color: '#94a3b8', fontSize: 14 }}>🔍</span>
          <input
            placeholder="Buscar eventos..."
            value={busca}
            onChange={e => setBusca(e.target.value)}
            onKeyDown={handleBusca}
            style={{
              border: 'none',
              background: 'transparent',
              outline: 'none',
              fontSize: 14,
              color: '#1e293b',
              width: 160,
            }}
          />
        </div>

        {modoOrganizador && (
          <Link to="/gerenciar">
            <button style={{
              background: '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: 24,
              padding: '8px 20px',
              fontSize: 14,
              fontWeight: 600,
              cursor: 'pointer',
            }}>
              Criar Evento
            </button>
          </Link>
        )}

        {usuario && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{ fontSize: 13, color: '#475569', maxWidth: 120, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {usuario.nome}
            </div>
            <button
              onClick={handleLogout}
              title="Sair"
              style={{
                background: '#f1f5f9',
                border: '1px solid #e2e8f0',
                borderRadius: '50%',
                width: 36,
                height: 36,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 16,
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
