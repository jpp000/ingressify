import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { User, Mail, Lock, Eye, EyeOff, AlertCircle, ShoppingBag, CalendarPlus, ShieldCheck, Repeat, Dices } from 'lucide-react'
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

  const handleCadastro = async (e: FormEvent) => {
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
    <div className="auth">
      <aside className="auth__brand">
        <span className="brand">Ingress<b>ify</b></span>
        <div className="stack" style={{ gap: 'var(--sp-5)' }}>
          <h2 className="auth__tagline">Junte-se à plataforma de eventos mais confiável.</h2>
          <ul className="auth__points">
            <li className="auth__point"><span className="ico"><ShieldCheck size={18} /></span> Pagamento garantido em toda transação</li>
            <li className="auth__point"><span className="ico"><Repeat size={18} /></span> Revenda sem cambismo</li>
            <li className="auth__point"><span className="ico"><Dices size={18} /></span> Concorra a ingressos em sorteios</li>
          </ul>
        </div>
        <div style={{ fontSize: '0.8125rem', opacity: 0.7 }}>© Ingressify</div>
      </aside>

      <main className="auth__panel">
        <div className="auth__card">
          <h1>Criar nova conta</h1>
          <p className="secondary" style={{ marginBottom: 'var(--sp-5)' }}>Leva menos de um minuto.</p>

          {erro && <div className="auth-alert auth-alert--err" role="alert"><AlertCircle size={18} />{erro}</div>}

          <form onSubmit={handleCadastro} className="stack" style={{ gap: 'var(--sp-4)' }}>
            <div className="field">
              <label className="label" htmlFor="nome">Nome completo</label>
              <div className="input-group"><User size={18} />
                <input id="nome" className="input" placeholder="Seu nome" value={nome} onChange={e => setNome(e.target.value)} required />
              </div>
            </div>
            <div className="field">
              <label className="label" htmlFor="email">E-mail</label>
              <div className="input-group"><Mail size={18} />
                <input id="email" className="input" type="email" placeholder="voce@email.com" value={email} onChange={e => setEmail(e.target.value)} required autoComplete="email" />
              </div>
            </div>
            <div className="field">
              <label className="label" htmlFor="senha">Senha</label>
              <div className="input-group input-pass"><Lock size={18} />
                <input id="senha" className="input" type={mostrarSenha ? 'text' : 'password'} placeholder="Mín. 8 caracteres" value={senha} onChange={e => setSenha(e.target.value)} required autoComplete="new-password" />
                <button type="button" onClick={() => setMostrarSenha(v => !v)} aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}>
                  {mostrarSenha ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
              <span className="field-hint">Use ao menos 1 letra maiúscula e 1 número.</span>
            </div>
            <div className="field">
              <label className="label" htmlFor="conf">Confirmar senha</label>
              <div className="input-group"><Lock size={18} />
                <input id="conf" className="input" type={mostrarSenha ? 'text' : 'password'} placeholder="Repita a senha" value={confirmarSenha} onChange={e => setConfirmarSenha(e.target.value)} required autoComplete="new-password" />
              </div>
            </div>

            <div className="field">
              <span className="label">Tipo de conta</span>
              <div className="choices">
                {([['COMPRADOR', 'Comprador', 'Compra ingressos', ShoppingBag], ['ORGANIZADOR', 'Organizador', 'Cria eventos', CalendarPlus]] as const).map(([v, t, d, Ico]) => (
                  <label key={v} className={`choice${papel === v ? ' choice--active' : ''}`}>
                    <input type="radio" name="papel" value={v} checked={papel === v} onChange={() => setPapel(v)} className="sr-only" />
                    <span className="choice__ico"><Ico size={18} /></span>
                    <span><span className="choice__t" style={{ display: 'block' }}>{t}</span><span className="choice__d">{d}</span></span>
                  </label>
                ))}
              </div>
            </div>

            <button type="submit" className="btn btn--lg btn--block" disabled={carregando} style={{ marginTop: 4 }}>
              {carregando ? 'Criando conta…' : 'Criar conta'}
            </button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 'var(--sp-5)' }} className="secondary">
            Já tem uma conta? <Link to="/login" style={{ color: 'var(--brand-strong)', fontWeight: 600 }}>Fazer login</Link>
          </p>
        </div>
      </main>
    </div>
  )
}
