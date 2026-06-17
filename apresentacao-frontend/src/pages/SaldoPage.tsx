import { useEffect, useMemo, useState } from 'react'
import { Plus, Minus, RotateCcw, Ticket, ArrowDownLeft, ArrowUpRight, CheckCircle2, AlertCircle, Sparkles, Gift } from 'lucide-react'
import Navbar from '../components/Navbar'
import { saldoService, carteiraService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Transacao { id: number; tipo: string; valor: number; data: string }

const TIPO_POSITIVO = new Set(['REEMBOLSO', 'VENDA', 'DEPOSITO', 'RECARGA', 'RESGATE_PONTOS'])
const RAPIDOS = [50, 100, 200]
const MAX_RECARGA = 10000 // teto por operação (regra de front)
const PONTOS_POR_BLOCO = 1000 // 1.000 pontos = R$ 10
const REAIS_POR_BLOCO = 10

const LABEL: Record<string, string> = {
  DEPOSITO: 'Recarga de saldo', RECARGA: 'Recarga de saldo', SAQUE: 'Saque',
  COMPRA: 'Compra de ingresso', VENDA: 'Venda (revenda)', REEMBOLSO: 'Reembolso',
  TRANSFERENCIA: 'Transferência', AJUSTE_SALDO: 'Ajuste de saldo', RESGATE_PONTOS: 'Resgate de pontos',
}

type Filtro = 'TODAS' | 'ENTRADAS' | 'SAIDAS'

export default function SaldoPage() {
  const { usuario } = useAuth()
  const [saldo, setSaldo] = useState<number | null>(null)
  const [pontos, setPontos] = useState<number>(0)
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [valor, setValor] = useState('')
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)
  const [acao, setAcao] = useState<'recarregar' | 'sacar' | 'resgatar' | null>(null)
  const [filtro, setFiltro] = useState<Filtro>('TODAS')

  const carregar = () => {
    if (!usuario) return
    carteiraService.obter(usuario.id).then(r => setSaldo(r.data.valor)).catch(() => {})
    carteiraService.pontos(usuario.id).then(r => setPontos(r.data.pontos)).catch(() => {})
    saldoService.transacoes(usuario.id, 1, 50).then(r => setTransacoes(r.data)).catch(() => {})
  }
  useEffect(() => { carregar() }, [])

  const parseValor = () => parseFloat(valor.replace(',', '.'))

  // Regras de validação no front (espelham e antecipam as do back)
  const validar = (v: number, tipo: 'recarregar' | 'sacar'): string | null => {
    if (!v || Number.isNaN(v) || v <= 0) return 'Informe um valor positivo.'
    if (tipo === 'recarregar' && v > MAX_RECARGA) return `Recarga máxima de ${formatMoeda(MAX_RECARGA)} por operação.`
    if (tipo === 'sacar' && saldo !== null && v > saldo) return `Saldo insuficiente. Você tem ${formatMoeda(saldo)} disponível.`
    return null
  }

  const recarregar = async () => {
    const v = parseValor()
    const erro = validar(v, 'recarregar')
    if (erro) { setMsg({ ok: false, texto: erro }); return }
    setAcao('recarregar')
    try {
      await carteiraService.recarregar(usuario!.id, v)
      setMsg({ ok: true, texto: `${formatMoeda(v)} adicionados à sua carteira.` })
      setValor(''); carregar()
    } catch {
      setMsg({ ok: false, texto: 'Erro ao recarregar saldo.' })
    } finally { setAcao(null) }
  }

  const sacar = async () => {
    const v = parseValor()
    const erro = validar(v, 'sacar')
    if (erro) { setMsg({ ok: false, texto: erro }); return }
    setAcao('sacar')
    try {
      await carteiraService.sacar(usuario!.id, v)
      setMsg({ ok: true, texto: `${formatMoeda(v)} sacados da sua carteira.` })
      setValor(''); carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro ao sacar.' })
    } finally { setAcao(null) }
  }

  const blocosResgataveis = Math.floor(pontos / PONTOS_POR_BLOCO)
  const podeResgatar = blocosResgataveis > 0
  const valorResgate = blocosResgataveis * REAIS_POR_BLOCO
  const pontosAResgatar = blocosResgataveis * PONTOS_POR_BLOCO

  const resgatar = async () => {
    if (!podeResgatar) return
    setAcao('resgatar')
    try {
      const r = await carteiraService.resgatarPontos(usuario!.id)
      setMsg({ ok: true, texto: `Você resgatou ${r.data.pontosResgatados} pontos por ${formatMoeda(r.data.valorCreditado)}!` })
      carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg({ ok: false, texto: err.response?.data?.motivo ?? 'Erro ao resgatar pontos.' })
    } finally { setAcao(null) }
  }

  const ocupado = acao !== null

  const { entradas, saidas } = useMemo(() => {
    let e = 0, s = 0
    for (const t of transacoes) {
      if (TIPO_POSITIVO.has(t.tipo)) e += Math.abs(t.valor)
      else s += Math.abs(t.valor)
    }
    return { entradas: e, saidas: s }
  }, [transacoes])

  const visiveis = useMemo(() => transacoes.filter(t => {
    if (filtro === 'TODAS') return true
    return (filtro === 'ENTRADAS') === TIPO_POSITIVO.has(t.tipo)
  }), [transacoes, filtro])

  return (
    <>
      <Navbar />
      <div className="app-container page page--narrow">
        <div className="page-head">
          <h1>Minha carteira</h1>
          <p className="secondary">Recarregue, saque e acompanhe suas movimentações.</p>
        </div>

        <div className="wallet" style={{ marginBottom: 'var(--sp-4)' }}>
          <div className="wallet__label">SALDO DISPONÍVEL</div>
          <div className="wallet__value">{saldo !== null ? formatMoeda(saldo) : '—'}</div>
        </div>

        {/* Pontos de fidelidade */}
        <div className="surface surface--pad between wrap" style={{ marginBottom: 'var(--sp-5)', gap: 'var(--sp-4)' }}>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <span className="list-ico list-ico--in" style={{ width: 44, height: 44 }}><Sparkles size={22} /></span>
            <div>
              <div style={{ fontWeight: 700, fontSize: '1.25rem' }}>{pontos.toLocaleString('pt-BR')} pontos</div>
              <div className="muted" style={{ fontSize: '0.8125rem' }}>
                Ganhe 1 ponto por real recarregado · {PONTOS_POR_BLOCO.toLocaleString('pt-BR')} pontos = {formatMoeda(REAIS_POR_BLOCO)}
              </div>
            </div>
          </div>
          <div style={{ textAlign: 'right' }}>
            <button className="btn btn--sm" onClick={resgatar} disabled={!podeResgatar || ocupado}>
              <Gift size={16} />{acao === 'resgatar' ? 'Resgatando…' : 'Resgatar pontos'}
            </button>
            <div className="muted" style={{ fontSize: '0.75rem', marginTop: 6 }}>
              {podeResgatar
                ? `Resgatar ${pontosAResgatar.toLocaleString('pt-BR')} pontos → ${formatMoeda(valorResgate)}`
                : `Faltam ${(PONTOS_POR_BLOCO - pontos).toLocaleString('pt-BR')} pontos para o 1º resgate`}
            </div>
          </div>
        </div>

        <div className="surface surface--pad" style={{ marginBottom: 'var(--sp-5)' }}>
          <h2 style={{ fontSize: '1.15rem', marginBottom: 'var(--sp-4)' }}>Movimentar carteira</h2>
          <div className="row wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-3)' }}>
            {RAPIDOS.map(valorRapido => (
              <button key={valorRapido} className="chip" onClick={() => setValor(String(valorRapido))}>{formatMoeda(valorRapido)}</button>
            ))}
          </div>
          <div className="input-group grow" style={{ marginBottom: 'var(--sp-2)' }}>
            <span style={{ position: 'absolute', left: 14, color: 'var(--ink-muted)', fontWeight: 600 }}>R$</span>
            <input className="input" style={{ paddingLeft: 42, fontWeight: 600 }} inputMode="decimal" placeholder="0,00" value={valor} onChange={e => { setValor(e.target.value); setMsg(null) }} />
          </div>
          <p className="field-hint" style={{ marginBottom: 'var(--sp-3)' }}>
            Recarga até {formatMoeda(MAX_RECARGA)} por operação. Saque limitado ao saldo disponível.
          </p>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <button className="btn grow" onClick={recarregar} disabled={ocupado}>
              <Plus size={18} />{acao === 'recarregar' ? 'Recarregando…' : 'Recarregar'}
            </button>
            <button className="btn btn--soft grow" onClick={sacar} disabled={ocupado || saldo === 0}>
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
          <div className="between wrap" style={{ padding: 'var(--sp-4) var(--sp-5)', borderBottom: '1px solid var(--border)', gap: 'var(--sp-3)' }}>
            <h2 style={{ fontSize: '1.15rem' }}>Extrato</h2>
            <div className="segmented">
              {([['TODAS', 'Todas'], ['ENTRADAS', 'Entradas'], ['SAIDAS', 'Saídas']] as const).map(([val, t]) => (
                <button key={val} className={`segmented__opt${filtro === val ? ' segmented__opt--active' : ''}`} onClick={() => setFiltro(val)}>{t}</button>
              ))}
            </div>
          </div>

          {transacoes.length > 0 && (
            <div className="between wrap" style={{ padding: 'var(--sp-3) var(--sp-5)', borderBottom: '1px solid var(--border)', gap: 'var(--sp-4)', background: 'var(--surface-2)' }}>
              <span className="row" style={{ gap: 8, fontSize: '0.875rem' }}>
                <ArrowDownLeft size={16} style={{ color: 'oklch(0.45 0.13 150)' }} />
                <span className="muted">Entradas</span>
                <span className="amt-in">{formatMoeda(entradas)}</span>
              </span>
              <span className="row" style={{ gap: 8, fontSize: '0.875rem' }}>
                <ArrowUpRight size={16} style={{ color: 'var(--ink)' }} />
                <span className="muted">Saídas</span>
                <span className="amt-out">{formatMoeda(saidas)}</span>
              </span>
            </div>
          )}

          {visiveis.length === 0 ? (
            <div className="empty"><Ticket size={36} /><p className="muted">{transacoes.length === 0 ? 'Nenhuma movimentação ainda.' : 'Nenhuma movimentação neste filtro.'}</p></div>
          ) : visiveis.map(t => {
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
