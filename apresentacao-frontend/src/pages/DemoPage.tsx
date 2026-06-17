import { useNavigate } from 'react-router-dom'
import { ShieldCheck, CalendarDays, Ticket, ScanLine, FlaskConical, LogIn, Info, X } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { setUsuarioId } from '../services/api'
import { MOCK_USERS, enableMockMode, disableMockMode, isMockMode } from '../mocks/mockInterceptor'

interface UserCard {
  id: number
  nome: string
  email: string
  papeis: string[]
  cor: string
  icon: typeof ShieldCheck
  descricao: string
  destino: string
}

const CARDS: UserCard[] = [
  { id: 1,  nome: 'Admin Sistema',      email: 'admin@ingressify.com',  papeis: ['ADMIN'],               cor: 'oklch(0.45 0.14 250)', icon: ShieldCheck,  descricao: 'Painel administrativo, moderação, relatórios e marketplace.',       destino: '/admin' },
  { id: 2,  nome: 'Maria Organizadora', email: 'maria@ingressify.com',  papeis: ['ORGANIZADOR','COMPRADOR'], cor: 'oklch(0.50 0.18 140)', icon: CalendarDays, descricao: 'Cria e gerencia eventos 1, 2 e 5. Pode comprar ingressos também.', destino: '/gerenciar' },
  { id: 7,  nome: 'João Organizador',   email: 'joao@ingressify.com',   papeis: ['ORGANIZADOR','COMPRADOR'], cor: 'oklch(0.50 0.18 160)', icon: CalendarDays, descricao: 'Cria e gerencia eventos 3 e 4. Pode comprar ingressos também.',    destino: '/gerenciar' },
  { id: 3,  nome: 'Ana Compradora',     email: 'ana@ingressify.com',    papeis: ['COMPRADOR'],           cor: 'oklch(0.48 0.20 25)',  icon: Ticket,       descricao: 'Tem 3 ingressos (eventos 1 e 3), saldo R$250, anúncios de revenda.', destino: '/' },
  { id: 5,  nome: 'Pedro Comprador',    email: 'pedro@ingressify.com',  papeis: ['COMPRADOR'],           cor: 'oklch(0.48 0.20 45)',  icon: Ticket,       descricao: 'Tem 2 ingressos (eventos 2 e 5), saldo R$100, inscrito em sorteio.', destino: '/' },
  { id: 4,  nome: 'Carlos Operador',    email: 'carlos@ingressify.com', papeis: ['OPERADOR_PORTA'],      cor: 'oklch(0.48 0.16 310)', icon: ScanLine,     descricao: 'Acesso ao check-in. Pode escanear ingressos dos eventos.',        destino: '/check-in' },
]

const PAPEIS_LABEL: Record<string, string> = {
  ADMIN: 'Admin', ORGANIZADOR: 'Organizador', COMPRADOR: 'Comprador', OPERADOR_PORTA: 'Operador',
}

const DATA_SUMMARY = [
  { label: '5 eventos', detalhe: '4 publicados + 1 rascunho' },
  { label: '9 tipos de ingresso', detalhe: 'Pista, VIP, Camarote, Estudante…' },
  { label: '7 ingressos ativos', detalhe: 'Ana tem 3, Pedro tem 2, Maria e João têm 1 cada' },
  { label: '3 anúncios de revenda', detalhe: 'Ana e Pedro vendendo ingressos' },
  { label: '4 denúncias', detalhe: '3 pendentes, 1 resolvida' },
  { label: '6 avaliações', detalhe: 'Nos eventos 1, 2 e 3' },
  { label: '2 mapas de assentos', detalhe: 'Arena Principal (ev.1) e Auditório CESAR (ev.3)' },
  { label: '3 sorteios', detalhe: '1 aberto, 1 encerrado com ganhador, 1 rascunho' },
  { label: '2 grupos de compra', detalhe: 'Aguardando pagamento' },
  { label: 'Cupons demo', detalhe: 'VERAO10, VIP50, CESAR20' },
  { label: 'Feed com posts', detalhe: '6 posts distribuídos nos eventos 1, 2 e 3' },
]

export default function DemoPage() {
  const { login, logout, usuario } = useAuth()
  const navigate = useNavigate()

  const entrar = (card: UserCard) => {
    enableMockMode()
    const u = MOCK_USERS.find(x => x.id === card.id)!
    setUsuarioId(u.id)
    login(u)
    navigate(card.destino)
  }

  const sair = () => {
    disableMockMode()
    logout()
    navigate('/demo')
  }

  return (
    <div className="app-container page page--mid" style={{ paddingTop: 'var(--sp-7)' }}>
      <div className="page-head between wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-6)' }}>
        <div className="row" style={{ gap: 'var(--sp-3)' }}>
          <span className="list-ico list-ico--out" style={{ width: 52, height: 52, background: 'oklch(0.94 0.05 250)', color: 'oklch(0.45 0.14 250)' }}>
            <FlaskConical size={26} />
          </span>
          <div>
            <h1>Modo Demo</h1>
            <p className="secondary">Escolha um usuário para testar todas as funcionalidades sem backend.</p>
          </div>
        </div>
        {isMockMode() && usuario && (
          <button className="btn btn--sm btn--ghost" onClick={sair}>
            <X size={15} /> Sair do modo demo
          </button>
        )}
      </div>

      {isMockMode() && usuario && (
        <div className="auth-alert auth-alert--ok" style={{ marginBottom: 'var(--sp-5)' }}>
          <LogIn size={18} />
          Logado como <strong>{usuario.nome}</strong> — {usuario.papeis.map(p => PAPEIS_LABEL[p] ?? p).join(', ')}. Todas as ações são simuladas localmente.
        </div>
      )}

      <div className="row wrap" style={{ gap: 'var(--sp-4)', marginBottom: 'var(--sp-7)' }}>
        {CARDS.map(card => {
          const Icon = card.icon
          const ativo = usuario?.id === card.id && isMockMode()
          return (
            <button
              key={card.id}
              onClick={() => entrar(card)}
              style={{
                background: ativo ? `${card.cor}18` : 'var(--surface)',
                border: `2px solid ${ativo ? card.cor : 'var(--border)'}`,
                borderRadius: 'var(--r-lg)',
                padding: 'var(--sp-4) var(--sp-5)',
                textAlign: 'left',
                cursor: 'pointer',
                flex: '1 1 280px',
                maxWidth: 340,
                transition: 'border-color 0.15s, background 0.15s',
                display: 'flex',
                flexDirection: 'column',
                gap: 'var(--sp-3)',
              }}
            >
              <div className="row" style={{ gap: 'var(--sp-3)', alignItems: 'center' }}>
                <span style={{ width: 44, height: 44, borderRadius: '50%', background: `${card.cor}20`, color: card.cor, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <Icon size={22} />
                </span>
                <div>
                  <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--ink)' }}>{card.nome}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--ink-muted)' }}>{card.email}</div>
                </div>
              </div>
              <div className="row wrap" style={{ gap: 6 }}>
                {card.papeis.map(p => (
                  <span key={p} className="badge badge--brand" style={{ background: `${card.cor}20`, color: card.cor, border: 'none', fontSize: '0.7rem' }}>
                    {PAPEIS_LABEL[p] ?? p}
                  </span>
                ))}
                {ativo && <span className="badge badge--success" style={{ fontSize: '0.7rem' }}>Ativo agora</span>}
              </div>
              <p style={{ fontSize: '0.875rem', color: 'var(--ink-muted)', margin: 0, lineHeight: 1.5 }}>{card.descricao}</p>
              <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', gap: 6, color: card.cor, fontWeight: 600, fontSize: '0.875rem' }}>
                <LogIn size={15} /> Entrar como {card.nome.split(' ')[0]}
              </div>
            </button>
          )
        })}
      </div>

      <section className="surface surface--pad" style={{ marginBottom: 'var(--sp-6)' }}>
        <div className="row" style={{ gap: 10, marginBottom: 'var(--sp-4)' }}>
          <Info size={18} style={{ color: 'var(--brand-strong)', flexShrink: 0 }} />
          <h2 style={{ fontSize: '1rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>Dados disponíveis no modo demo</h2>
        </div>
        <div className="row wrap" style={{ gap: 'var(--sp-2)' }}>
          {DATA_SUMMARY.map(({ label, detalhe }) => (
            <div key={label} style={{ background: 'var(--surface-2, var(--surface-alt, oklch(0.97 0 0)))', borderRadius: 'var(--r-md)', padding: '10px 14px', flex: '1 1 200px' }}>
              <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{label}</div>
              <div style={{ fontSize: '0.8rem', color: 'var(--ink-muted)', marginTop: 2 }}>{detalhe}</div>
            </div>
          ))}
        </div>
      </section>

      <div className="auth-alert" style={{ background: 'var(--warn-soft, oklch(0.97 0.03 70))', border: '1px solid var(--warn, oklch(0.65 0.15 70))', color: 'oklch(0.4 0.10 70)' }}>
        <Info size={16} />
        Todas as ações no modo demo são salvas apenas em memória. Recarregar a página reinicia os dados. Para persistir uma sessão entre recarregamentos, o usuário ativo é salvo no <code>sessionStorage</code>.
      </div>
    </div>
  )
}
