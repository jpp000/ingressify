import { useEffect, useState } from 'react'
import { Plus, RotateCcw, Ticket, ArrowDownLeft, ArrowUpRight, CheckCircle2, AlertCircle } from 'lucide-react'
import Navbar from '../components/Navbar'
import { saldoService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Transacao { id: number; tipo: string; valor: number; data: string }

const TIPO_POSITIVO = new Set(['REEMBOLSO', 'VENDA', 'DEPOSITO'])
const RAPIDOS = [50, 100, 200]

const LABEL: Record<string, string> = {
  DEPOSITO: 'Recarga de saldo', COMPRA: 'Compra de ingresso', VENDA: 'Venda (revenda)',
  REEMBOLSO: 'Reembolso', TRANSFERENCIA: 'Transferência', AJUSTE_SALDO: 'Ajuste de saldo',
}

export default function SaldoPage() {
  const { usuario } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [deposito, setDeposito] = useState('')
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [salvando, setSalvando] = useState(false)

  const carregar = () => {
    if (!usuario) return
    saldoService.obter(usuario.id).then(r => setSaldo(r.data.valor))
    saldoService.transacoes(usuario.id).then(r => setTransacoes(r.data))
  }
  useEffect(() => { carregar() }, [])

  const adicionar = async () => {
    const valor = parseFloat(deposito.replace(',', '.'))
    if (!valor || valor <= 0) { setMsg({ ok: false, texto: 'Informe um valor positivo' }); return }
    setSalvando(true)
    try {
      await saldoService.adicionar(usuario!.id, valor)
      setMsg({ ok: true, texto: `${formatMoeda(valor)} adicionados à sua carteira` })
      setDeposito('')
      carregar()
    } catch {
      setMsg({ ok: false, texto: 'Erro ao adicionar saldo' })
    } finally { setSalvando(false) }
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--narrow">
        <div className="page-head">
          <h1>Minha carteira</h1>
          <p className="secondary">Adicione saldo e acompanhe suas movimentações.</p>
        </div>

        <div className="wallet" style={{ marginBottom: 'var(--sp-5)' }}>
          <div className="wallet__label">SALDO DISPONÍVEL</div>
          <div className="wallet__value">{saldo !== null ? formatMoeda(saldo) : '—'}</div>
        </div>

        <div className="surface surface--pad" style={{ marginBottom: 'var(--sp-5)' }}>
          <h2 style={{ fontSize: '1.15rem', marginBottom: 'var(--sp-4)' }}>Adicionar saldo</h2>
          <div className="row wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-3)' }}>
            {RAPIDOS.map(v => (
              <button key={v} className="chip" onClick={() => setDeposito(String(v))}>+ {formatMoeda(v)}</button>
            ))}
          </div>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <div className="input-group grow">
              <span style={{ position: 'absolute', left: 14, color: 'var(--ink-muted)', fontWeight: 600 }}>R$</span>
              <input className="input" style={{ paddingLeft: 42, fontWeight: 600 }} inputMode="decimal" placeholder="0,00" value={deposito} onChange={e => setDeposito(e.target.value)} />
            </div>
            <button className="btn" onClick={adicionar} disabled={salvando}>
              <Plus size={18} />{salvando ? 'Depositando…' : 'Depositar'}
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
            const Ico = t.tipo === 'DEPOSITO' ? ArrowDownLeft : t.tipo === 'REEMBOLSO' ? RotateCcw : positivo ? ArrowDownLeft : ArrowUpRight
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
