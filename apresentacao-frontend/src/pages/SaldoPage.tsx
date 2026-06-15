import { useEffect, useState } from 'react'
import { Plus, Minus, RotateCcw, Ticket, ArrowDownLeft, ArrowUpRight, CheckCircle2, AlertCircle } from 'lucide-react'
import Navbar from '../components/Navbar'
import { saldoService, carteiraService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Transacao { id: number; tipo: string; valor: number; data: string }

const TIPO_POSITIVO = new Set(['REEMBOLSO', 'VENDA', 'DEPOSITO', 'RECARGA'])
const RAPIDOS = [50, 100, 200]

const LABEL: Record<string, string> = {
  DEPOSITO: 'Recarga de saldo', RECARGA: 'Recarga de saldo', SAQUE: 'Saque',
  COMPRA: 'Compra de ingresso', VENDA: 'Venda (revenda)', REEMBOLSO: 'Reembolso',
  TRANSFERENCIA: 'Transferência', AJUSTE_SALDO: 'Ajuste de saldo',
}

export default function SaldoPage() {
  const { usuario } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [valor, setValor] = useState('')
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [acao, setAcao] = useState<'recarregar' | 'sacar' | null>(null)

  const carregar = () => {
    if (!usuario) return
    carteiraService.obter(usuario.id).then(r => setSaldo(r.data.valor))
    saldoService.transacoes(usuario.id).then(r => setTransacoes(r.data))
  }
  useEffect(() => { carregar() }, [])

  const parseValor = () => parseFloat(valor.replace(',', '.'))

  const recarregar = async () => {
    const v = parseValor()
    if (!v || v <= 0) { setMsg({ ok: false, texto: 'Informe um valor positivo' }); return }
    setAcao('recarregar')
    try {
      await carteiraService.recarregar(usuario!.id, v)
      setMsg({ ok: true, texto: `${formatMoeda(v)} adicionados à sua carteira` })
      setValor(''); carregar()
    } catch {
      setMsg({ ok: false, texto: 'Erro ao recarregar saldo' })
    } finally { setAcao(null) }
  }

  const sacar = async () => {
    const v = parseValor()
    if (!v || v <= 0) { setMsg({ ok: false, texto: 'Informe um valor positivo' }); return }
    setAcao('sacar')
    try {
      await carteiraService.sacar(usuario!.id, v)
      setMsg({ ok: true, texto: `${formatMoeda(v)} sacados da sua carteira` })
      setValor(''); carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro ao sacar' })
    } finally { setAcao(null) }
  }

  const ocupado = acao !== null

  return (
    <>
      <Navbar />
      <div className="app-container page page--narrow">
        <div className="page-head">
          <h1>Minha carteira</h1>
          <p className="secondary">Recarregue, saque e acompanhe suas movimentações.</p>
        </div>

        <div className="wallet" style={{ marginBottom: 'var(--sp-5)' }}>
          <div className="wallet__label">SALDO DISPONÍVEL</div>
          <div className="wallet__value">{saldo !== null ? formatMoeda(saldo) : '—'}</div>
        </div>

        <div className="surface surface--pad" style={{ marginBottom: 'var(--sp-5)' }}>
          <h2 style={{ fontSize: '1.15rem', marginBottom: 'var(--sp-4)' }}>Movimentar carteira</h2>
          <div className="row wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-3)' }}>
            {RAPIDOS.map(v => (
              <button key={v} className="chip" onClick={() => setValor(String(v))}>{formatMoeda(v)}</button>
            ))}
          </div>
          <div className="input-group grow" style={{ marginBottom: 'var(--sp-3)' }}>
            <span style={{ position: 'absolute', left: 14, color: 'var(--ink-muted)', fontWeight: 600 }}>R$</span>
            <input className="input" style={{ paddingLeft: 42, fontWeight: 600 }} inputMode="decimal" placeholder="0,00" value={valor} onChange={e => setValor(e.target.value)} />
          </div>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <button className="btn grow" onClick={recarregar} disabled={ocupado}>
              <Plus size={18} />{acao === 'recarregar' ? 'Recarregando…' : 'Recarregar'}
            </button>
            <button className="btn btn--soft grow" onClick={sacar} disabled={ocupado}>
              <Minus size={18} />{acao === 'sacar' ? 'Sacando…' : 'Sacar'}
            </button>
          </div>
          {msg && (
            <div className={`auth-alert ${msg.ok ? 'auth-alert--ok' : 'auth-alert--err'}`} style={{ marginTop: 'var(--sp-3)', marginBottom: 0 }}>
              {msg.ok ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}{msg.texto}
            </div>
          )}
        </div>

        <div className="surface" style={{ overflow: 'hidden' }}>
          <div style={{ padding: 'var(--sp-4) var(--sp-5)', borderBottom: '1px solid var(--border)' }}>
            <h2 style={{ fontSize: '1.15rem' }}>Extrato</h2>
          </div>
          {transacoes.length === 0 ? (
            <div className="empty"><Ticket size={36} /><p className="muted">Nenhuma movimentação ainda.</p></div>
          ) : transacoes.map(t => {
            const positivo = TIPO_POSITIVO.has(t.tipo)
            const Ico = t.tipo === 'REEMBOLSO' ? RotateCcw : positivo ? ArrowDownLeft : ArrowUpRight
            return (
              <div key={t.id} className="list-row">
                <div className={`list-ico ${positivo ? 'list-ico--in' : 'list-ico--out'}`}><Ico size={20} /></div>
                <div className="grow">
                  <div style={{ fontWeight: 600 }}>{LABEL[t.tipo] ?? t.tipo}</div>
                  <div className="muted" style={{ fontSize: '0.8125rem' }}>
                    {t.data ? new Date(t.data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }) : ''}
                  </div>
                </div>
                <span className={positivo ? 'amt-in' : 'amt-out'}>{positivo ? '+' : '−'}{formatMoeda(Math.abs(t.valor))}</span>
              </div>
            )
          })}
        </div>
      </div>
    </>
  )
}
