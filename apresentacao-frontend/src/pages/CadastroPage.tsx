import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authService } from '../services/api'

export default function CadastroPage() {
  const navigate = useNavigate()
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmarSenha, setConfirmarSenha] = useState('')
  const [papel, setPapel] = useState<'COMPRADOR' | 'ORGANIZADOR'>('COMPRADOR')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  const handleCadastro = async (e: React.FormEvent) => {
    e.preventDefault()
    setErro('')

    if (!nome.trim()) { setErro('Nome é obrigatório'); return }
    if (!email.trim()) { setErro('Email é obrigatório'); return }
    if (senha.length < 8) { setErro('Senha deve ter no mínimo 8 caracteres'); return }
    if (!/[A-Z]/.test(senha)) { setErro('Senha deve conter ao menos uma letra maiúscula'); return }
    if (!/\d/.test(senha)) { setErro('Senha deve conter ao menos um número'); return }
    if (senha !== confirmarSenha) { setErro('As senhas não coincidem'); return }

    setCarregando(true)
    try {
      await authService.cadastro(nome, email, senha, papel)
      navigate('/login', { state: { mensagem: 'Conta criada com sucesso! Faça login para continuar.' } })
    } catch (err: unknown) {
      const e = err as { response?: { data?: { erro?: { mensagem?: string } } } }
      setErro(e.response?.data?.erro?.mensagem ?? 'Erro ao criar conta. Tente novamente.')
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
            Junte-se à maior plataforma de eventos.
          </h2>
          <p style={{ color: '#93c5fd', fontSize: 14, lineHeight: 1.6 }}>
            Crie sua conta agora e comece a descobrir ou organizar eventos incríveis.
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
        <div style={{ width: '100%', maxWidth: 400 }}>
          <div style={{ color: '#1e3a8a', fontWeight: 800, fontSize: 20, marginBottom: 8 }}>
            Ingressefy
          </div>
          <h1 style={{ fontSize: 24, fontWeight: 700, color: '#1e293b', marginBottom: 6 }}>
            Criar nova conta
          </h1>
          <p style={{ color: '#64748b', fontSize: 14, marginBottom: 28 }}>
            Preencha os dados abaixo para começar.
          </p>

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

          <form onSubmit={handleCadastro} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div>
              <label style={labelStyle}>Nome completo</label>
              <input
                type="text"
                placeholder="Seu nome completo"
                value={nome}
                onChange={e => setNome(e.target.value)}
                required
                style={inputStyle}
              />
            </div>

            <div>
              <label style={labelStyle}>E-mail</label>
              <input
                type="email"
                placeholder="exemplo@email.com"
                value={email}
                onChange={e => setEmail(e.target.value)}
                required
                style={inputStyle}
              />
            </div>

            <div>
              <label style={labelStyle}>Senha</label>
              <div style={{ position: 'relative' }}>
                <input
                  type={mostrarSenha ? 'text' : 'password'}
                  placeholder="Mín. 8 caracteres, 1 maiúscula, 1 número"
                  value={senha}
                  onChange={e => setSenha(e.target.value)}
                  required
                  style={{ ...inputStyle, paddingRight: 40 }}
                />
                <button
                  type="button"
                  onClick={() => setMostrarSenha(v => !v)}
                  style={{ position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', color: '#94a3b8', fontSize: 16, padding: 0, cursor: 'pointer' }}
                >
                  {mostrarSenha ? '🙈' : '👁️'}
                </button>
              </div>
            </div>

            <div>
              <label style={labelStyle}>Confirmar senha</label>
              <input
                type={mostrarSenha ? 'text' : 'password'}
                placeholder="Repita sua senha"
                value={confirmarSenha}
                onChange={e => setConfirmarSenha(e.target.value)}
                required
                style={inputStyle}
              />
            </div>

            <div>
              <label style={labelStyle}>Tipo de conta</label>
              <div style={{ display: 'flex', gap: 12 }}>
                {(['COMPRADOR', 'ORGANIZADOR'] as const).map(p => (
                  <label key={p} style={{
                    flex: 1,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 8,
                    padding: '10px 14px',
                    border: `2px solid ${papel === p ? '#1d4ed8' : '#e2e8f0'}`,
                    borderRadius: 8,
                    cursor: 'pointer',
                    background: papel === p ? '#eff6ff' : '#fff',
                    transition: 'all 0.15s',
                  }}>
                    <input
                      type="radio"
                      name="papel"
                      value={p}
                      checked={papel === p}
                      onChange={() => setPapel(p)}
                      style={{ accentColor: '#1d4ed8' }}
                    />
                    <div>
                      <div style={{ fontSize: 14, fontWeight: 600, color: '#1e293b' }}>
                        {p === 'COMPRADOR' ? 'Comprador' : 'Organizador'}
                      </div>
                      <div style={{ fontSize: 11, color: '#64748b' }}>
                        {p === 'COMPRADOR' ? 'Compra ingressos' : 'Cria eventos'}
                      </div>
                    </div>
                  </label>
                ))}
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
              {carregando ? 'Criando conta...' : 'Criar conta'}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 24, fontSize: 14, color: '#64748b' }}>
            Já tem uma conta?{' '}
            <Link to="/login" style={{ color: '#1d4ed8', fontWeight: 600 }}>
              Fazer login
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

const labelStyle: React.CSSProperties = {
  display: 'block',
  fontSize: 11,
  fontWeight: 700,
  color: '#64748b',
  textTransform: 'uppercase',
  letterSpacing: 0.8,
  marginBottom: 6,
}

const inputStyle: React.CSSProperties = {
  width: '100%',
  padding: '12px',
  border: '1px solid #e2e8f0',
  borderRadius: 8,
  fontSize: 14,
  color: '#1e293b',
  outline: 'none',
  boxSizing: 'border-box',
}
