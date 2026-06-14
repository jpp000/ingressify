import { useState, type FormEvent } from 'react'
import { useNavigate, Link, useLocation } from 'react-router-dom'
import { Mail, Lock, Eye, EyeOff, CheckCircle2, AlertCircle, ShieldCheck, Repeat, Dices } from 'lucide-react'
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

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault()
    setErro('')
    if (!email.trim() || !senha) { setErro('Email e senha são obrigatórios'); return }
    setCarregando(true)
    try {
      const res = await authService.login(email, senha)
      const raw = res.data as { id: number; nome: string; email: string; papeis: string[] | Record<string, string> }
      const papeis = Array.isArray(raw.papeis) ? raw.papeis : Object.values(raw.papeis ?? {})
      const usuario = { ...raw, papeis }
      login(usuario)
      setUsuarioId(usuario.id)
      if (papeis.includes('ADMIN')) navigate('/denuncias')
      else if (papeis.includes('ORGANIZADOR')) navigate('/gerenciar')
      else if (papeis.includes('OPERADOR_PORTA')) navigate('/check-in')
      else navigate('/')
    } catch (err: unknown) {
      const e = err as { response?: { data?: { erro?: { mensagem?: string } } } }
      setErro(e.response?.data?.erro?.mensagem ?? 'Email ou senha incorretos')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <div className="auth">
      <aside className="auth__brand">
        <span className="brand">Ingress<b>ify</b></span>
        <div className="stack" style={{ gap: 'var(--sp-5)' }}>
          <h2 className="auth__tagline">Sua entrada para momentos inesquecíveis.</h2>
          <ul className="auth__points">
            <li className="auth__point"><span className="ico"><ShieldCheck size={18} /></span> Compra e revenda com pagamento garantido</li>
            <li className="auth__point"><span className="ico"><Repeat size={18} /></span> Revenda segura, sem cambismo</li>
            <li className="auth__point"><span className="ico"><Dices size={18} /></span> Sorteios oficiais dos organizadores</li>
          </ul>
        </div>
        <div style={{ fontSize: '0.8125rem', opacity: 0.7 }}>© Ingressify</div>
      </aside>

      <main className="auth__panel">
        <div className="auth__card">
          <h1>Bem-vindo de volta</h1>
          <p className="secondary" style={{ marginBottom: 'var(--sp-5)' }}>Entre para continuar comprando e gerenciando ingressos.</p>

          {mensagemSucesso && (
            <div className="auth-alert auth-alert--ok"><CheckCircle2 size={18} />{mensagemSucesso}</div>
          )}
          {erro && (
            <div className="auth-alert auth-alert--err" role="alert"><AlertCircle size={18} />{erro}</div>
          )}

          <form onSubmit={handleLogin} className="stack" style={{ gap: 'var(--sp-4)' }}>
            <div className="field">
              <label className="label" htmlFor="email">E-mail</label>
              <div className="input-group">
                <Mail size={18} />
                <input id="email" className="input" type="email" placeholder="voce@email.com" value={email} onChange={e => setEmail(e.target.value)} required autoComplete="email" />
              </div>
            </div>

            <div className="field">
              <label className="label" htmlFor="senha">Senha</label>
              <div className="input-group input-pass">
                <Lock size={18} />
                <input id="senha" className="input" type={mostrarSenha ? 'text' : 'password'} placeholder="••••••••" value={senha} onChange={e => setSenha(e.target.value)} required autoComplete="current-password" />
                <button type="button" onClick={() => setMostrarSenha(v => !v)} aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}>
                  {mostrarSenha ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn--lg btn--block" disabled={carregando} style={{ marginTop: 4 }}>
              {carregando ? 'Entrando…' : 'Entrar na conta'}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 'var(--sp-5)' }} className="secondary">
            Não tem uma conta? <Link to="/cadastro" style={{ color: 'var(--brand-strong)', fontWeight: 600 }}>Criar conta agora</Link>
          </p>
        </div>
      </main>
    </div>
  )
}
