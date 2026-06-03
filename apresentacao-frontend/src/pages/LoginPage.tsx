import { useState } from 'react'
import { useNavigate, Link, useLocation } from 'react-router-dom'
import { authService, setUsuarioId } from '../services/api'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  const mensagemSucesso = (location.state as { mensagem?: string } | null)?.mensagem

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setErro('')
    if (!email.trim() || !senha) {
      setErro('Email e senha são obrigatórios')
      return
    }
    setCarregando(true)
    try {
      const res = await authService.login(email, senha)
      const usuario = res.data as { id: number; nome: string; email: string; papeis: string[] }
      login(usuario)
      setUsuarioId(usuario.id)
      if (usuario.papeis.includes('ORGANIZADOR')) {
        navigate('/gerenciar')
      } else {
        navigate('/')
      }
    } catch (err: unknown) {
      const e = err as { response?: { data?: { erro?: { mensagem?: string } } } }
      setErro(e.response?.data?.erro?.mensagem ?? 'Email ou senha incorretos')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: '#fff' }}>
      {/* Coluna esquerda */}
      <div style={{
        flex: '0 0 42%',
        background: 'linear-gradient(160deg, #0f172a 0%, #1e3a8a 60%, #1d4ed8 100%)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-end',
        padding: '48px 40px',
        position: 'relative',
        overflow: 'hidden',
        minHeight: '100vh',
      }}>
        <div style={{
          position: 'absolute',
          inset: 0,
          backgroundImage: 'url(https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&q=80)',
          backgroundSize: 'cover',
          backgroundPosition: 'center',
          opacity: 0.25,
        }} />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <h2 style={{ color: '#fff', fontSize: 28, fontWeight: 700, lineHeight: 1.3, marginBottom: 16 }}>
            Sua entrada para momentos inesquecíveis.
          </h2>
          <p style={{ color: '#93c5fd', fontSize: 14, lineHeight: 1.6 }}>
            Acesse sua conta para gerenciar ingressos, descobrir novos eventos e vivenciar o melhor do entretenimento.
          </p>
        </div>
      </div>

      {/* Coluna direita */}
      <div style={{
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '48px 40px',
        background: '#fff',
      }}>
        <div style={{ width: '100%', maxWidth: 380 }}>
          <div style={{ color: '#1e3a8a', fontWeight: 800, fontSize: 20, marginBottom: 8 }}>
            Ingressefy
          </div>
          <h1 style={{ fontSize: 24, fontWeight: 700, color: '#1e293b', marginBottom: 6 }}>
            Bem-vindo de volta
          </h1>
          <p style={{ color: '#64748b', fontSize: 14, marginBottom: 28 }}>
            Por favor, insira seus dados de acesso.
          </p>

          {mensagemSucesso && (
            <div style={{
              background: '#f0fdf4',
              border: '1px solid #86efac',
              borderRadius: 8,
              padding: '12px 14px',
              color: '#16a34a',
              fontSize: 14,
              marginBottom: 16,
            }}>
              {mensagemSucesso}
            </div>
          )}

          {erro && (
            <div style={{
              background: '#fef2f2',
              border: '1px solid #fca5a5',
              borderRadius: 8,
              padding: '12px 14px',
              color: '#dc2626',
              fontSize: 14,
              marginBottom: 16,
            }}>
              {erro}
            </div>
          )}

          <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: 6 }}>
                E-mail
              </label>
              <div style={{ position: 'relative' }}>
                <span style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: '#94a3b8', fontSize: 16 }}>@</span>
                <input
                  type="email"
                  placeholder="exemplo@email.com"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                  style={{
                    width: '100%',
                    padding: '12px 12px 12px 36px',
                    border: '1px solid #e2e8f0',
                    borderRadius: 8,
                    fontSize: 14,
                    color: '#1e293b',
                    outline: 'none',
                    boxSizing: 'border-box',
                  }}
                />
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                <label style={{ fontSize: 11, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.8 }}>
                  Senha
                </label>
              </div>
              <div style={{ position: 'relative' }}>
                <span style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: '#94a3b8', fontSize: 16 }}>🔒</span>
                <input
                  type={mostrarSenha ? 'text' : 'password'}
                  placeholder="••••••••"
                  value={senha}
                  onChange={e => setSenha(e.target.value)}
                  required
                  style={{
                    width: '100%',
                    padding: '12px 40px 12px 36px',
                    border: '1px solid #e2e8f0',
                    borderRadius: 8,
                    fontSize: 14,
                    color: '#1e293b',
                    outline: 'none',
                    boxSizing: 'border-box',
                  }}
                />
                <button
                  type="button"
                  onClick={() => setMostrarSenha(v => !v)}
                  style={{
                    position: 'absolute',
                    right: 12,
                    top: '50%',
                    transform: 'translateY(-50%)',
                    background: 'none',
                    border: 'none',
                    color: '#94a3b8',
                    fontSize: 16,
                    padding: 0,
                    cursor: 'pointer',
                  }}
                >
                  {mostrarSenha ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={carregando}
              style={{
                background: carregando ? '#93c5fd' : '#1d4ed8',
                color: '#fff',
                border: 'none',
                borderRadius: 8,
                padding: '14px',
                fontSize: 15,
                fontWeight: 600,
                width: '100%',
                marginTop: 4,
                cursor: carregando ? 'not-allowed' : 'pointer',
                transition: 'background 0.15s',
              }}
            >
              {carregando ? 'Entrando...' : 'Entrar na conta'}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 24, fontSize: 14, color: '#64748b' }}>
            Não tem uma conta?{' '}
            <Link to="/cadastro" style={{ color: '#1d4ed8', fontWeight: 600 }}>
              Criar conta agora
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
