import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'

export default function LoginPage() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [lembrar, setLembrar] = useState(false)

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault()
    navigate('/')
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
          <h2 style={{
            color: '#fff',
            fontSize: 28,
            fontWeight: 700,
            lineHeight: 1.3,
            marginBottom: 16,
          }}>
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
          <p style={{ color: '#64748b', fontSize: 14, marginBottom: 32 }}>
            Por favor, insira seus dados de acesso.
          </p>

          <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: 6 }}>
                E-mail ou Usuário
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
                    transition: 'border-color 0.15s',
                  }}
                />
              </div>
            </div>

            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
                <label style={{ fontSize: 11, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.8 }}>
                  Senha
                </label>
                <a href="#" style={{ fontSize: 13, color: '#1d4ed8', fontWeight: 500 }}>Esqueceu a senha?</a>
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
                  }}
                >
                  {mostrarSenha ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={lembrar}
                onChange={e => setLembrar(e.target.checked)}
                style={{ width: 16, height: 16, accentColor: '#1d4ed8' }}
              />
              <span style={{ fontSize: 14, color: '#475569' }}>Lembrar de mim</span>
            </label>

            <button
              type="submit"
              style={{
                background: '#1d4ed8',
                color: '#fff',
                border: 'none',
                borderRadius: 8,
                padding: '14px',
                fontSize: 15,
                fontWeight: 600,
                width: '100%',
                marginTop: 4,
                transition: 'background 0.15s',
              }}
            >
              Entrar na conta
            </button>
          </form>

          <div style={{ display: 'flex', alignItems: 'center', gap: 12, margin: '24px 0' }}>
            <div style={{ flex: 1, height: 1, background: '#e2e8f0' }} />
            <span style={{ color: '#94a3b8', fontSize: 12, whiteSpace: 'nowrap' }}>OU CONTINUE COM</span>
            <div style={{ flex: 1, height: 1, background: '#e2e8f0' }} />
          </div>

          <div style={{ display: 'flex', gap: 12 }}>
            <button style={{
              flex: 1,
              border: '1px solid #e2e8f0',
              borderRadius: 8,
              padding: '10px',
              background: '#fff',
              fontSize: 14,
              fontWeight: 500,
              color: '#1e293b',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
            }}>
              <span style={{ fontWeight: 800 }}>G</span> Google
            </button>
            <button style={{
              flex: 1,
              border: '1px solid #e2e8f0',
              borderRadius: 8,
              padding: '10px',
              background: '#fff',
              fontSize: 14,
              fontWeight: 500,
              color: '#1e293b',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
            }}>
              <span style={{ fontWeight: 800, color: '#1877f2' }}>f</span> Facebook
            </button>
          </div>

          <p style={{ textAlign: 'center', marginTop: 24, fontSize: 14, color: '#64748b' }}>
            Não tem uma conta?{' '}
            <Link to="/" style={{ color: '#1d4ed8', fontWeight: 600 }}>
              Criar conta agora
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
