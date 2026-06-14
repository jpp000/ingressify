import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { MapPin, Tag, Lock, Wallet, CheckCircle2, AlertCircle, ShieldCheck } from 'lucide-react'
import { ingressoService, saldoService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, formatDataBadge, corCategoria } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Evento { id: number; nome: string; dataHora: string; local: string; imagemCapaUrl?: string; categoria?: string }
interface TipoIngresso { id: number; nome: string; preco: number }
interface ItemPedido { tipoId: number; quantidade: number }
interface LocationState { evento: Evento; tipos: TipoIngresso[]; itens: ItemPedido[] }

export default function RevisaoPedido() {
  const location = useLocation()
  const navigate = useNavigate()
  const { usuario } = useAuth()
  const state = location.state as LocationState | null

  const [cupom, setCupom] = useState('')
  const [desconto, setDesconto] = useState(0)
  const [mensagemCupom, setMensagemCupom] = useState<{ ok: boolean; texto: string } | null>(null)
  const [confirmando, setConfirmando] = useState(false)
  const [erro, setErro] = useState('')

  if (!state) { navigate('/'); return null }
  const { evento, tipos, itens } = state

  const subtotal = itens.reduce((acc, item) => {
    const tipo = tipos.find(t => t.id === item.tipoId)
    return acc + (tipo ? Number(tipo.preco) * item.quantidade : 0)
  }, 0)
  const total = subtotal - desconto

  const aplicarCupom = () => {
    if (cupom.toUpperCase() === 'BEMVINDO10') {
      setDesconto(subtotal * 0.10)
      setMensagemCupom({ ok: true, texto: 'Cupom aplicado: 10% de desconto.' })
    } else {
      setDesconto(0)
      setMensagemCupom({ ok: false, texto: 'Cupom inválido.' })
    }
  }

  const confirmarCompra = async () => {
    setConfirmando(true); setErro('')
    try {
      const saldoRes = await saldoService.obter(usuario!.id)
      const saldoAtual = Number(saldoRes.data.valor)
      if (saldoAtual < total) {
        setErro(`Saldo insuficiente. Seu saldo é ${formatMoeda(saldoAtual)} e o total é ${formatMoeda(total)}.`)
        setConfirmando(false); return
      }
      for (const item of itens) {
        await ingressoService.comprar(usuario!.id, { tipoIngressoId: item.tipoId, quantidade: item.quantidade })
      }
      navigate('/meus-ingressos', { state: { sucesso: true } })
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string; message?: string } } }
      setErro(err.response?.data?.motivo ?? err.response?.data?.message ?? 'Erro ao confirmar compra.')
      setConfirmando(false)
    }
  }

  const { dia, mes } = formatDataBadge(evento.dataHora)

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head">
          <h1>Revise seu pedido</h1>
          <p className="secondary">Confira os detalhes antes de confirmar a compra.</p>
        </div>

        <div className="two-col">
          <div className="stack" style={{ gap: 'var(--sp-5)' }}>
            {/* Evento */}
            <div className="surface" style={{ overflow: 'hidden' }}>
              <div style={{ position: 'relative', height: 150, display: 'grid', placeItems: 'center', ...{ background: `linear-gradient(140deg, ${corCategoria(evento.categoria)} 0%, color-mix(in oklab, ${corCategoria(evento.categoria)} 52%, black) 100%)` } }}>
                {evento.imagemCapaUrl && <img src={evento.imagemCapaUrl} alt="" style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' }} />}
                <div className="evcard__date" style={{ left: 16, top: 16 }}><strong>{dia}</strong><span>{mes}</span></div>
              </div>
              <div className="surface--pad">
                <h2 style={{ fontSize: '1.25rem', marginBottom: 4 }}>{evento.nome}</h2>
                <p className="secondary row" style={{ gap: 6 }}><MapPin size={15} /> {evento.local}</p>
              </div>
            </div>

            {/* Resumo */}
            <div className="surface surface--pad">
              <h3 style={{ marginBottom: 'var(--sp-4)' }}>Resumo do pedido</h3>
              <div className="stack" style={{ gap: 'var(--sp-3)' }}>
                {itens.map(item => {
                  const tipo = tipos.find(t => t.id === item.tipoId)
                  if (!tipo) return null
                  return (
                    <div key={item.tipoId} className="sum-row">
                      <span><strong style={{ fontWeight: 600 }}>{tipo.nome}</strong> <span className="muted">× {item.quantidade}</span></span>
                      <span className="money">{formatMoeda(Number(tipo.preco) * item.quantidade)}</span>
                    </div>
                  )
                })}
                {desconto > 0 && (
                  <div className="sum-row" style={{ color: 'oklch(0.45 0.13 150)' }}>
                    <span>Desconto (cupom)</span><span>− {formatMoeda(desconto)}</span>
                  </div>
                )}
              </div>

              <div className="field" style={{ marginTop: 'var(--sp-5)' }}>
                <span className="label">Cupom de desconto</span>
                <div className="row" style={{ gap: 'var(--sp-2)' }}>
                  <div className="input-group grow"><Tag size={18} />
                    <input className="input" placeholder="Ex.: BEMVINDO10" value={cupom} onChange={e => setCupom(e.target.value)} />
                  </div>
                  <button className="btn btn--soft" onClick={aplicarCupom}>Aplicar</button>
                </div>
                {mensagemCupom && <span className={mensagemCupom.ok ? 'field-hint' : 'field-error'} style={mensagemCupom.ok ? { color: 'oklch(0.45 0.13 150)' } : undefined}>{mensagemCupom.texto}</span>}
              </div>

              <div className="sum-total">
                <span style={{ fontWeight: 600, fontSize: '1.05rem' }}>Total</span>
                <span className="money" style={{ fontSize: '1.6rem', color: 'var(--brand-strong)' }}>{formatMoeda(total)}</span>
              </div>
            </div>
          </div>

          {/* Pagamento */}
          <div className="sticky-side">
            <div className="surface surface--pad">
              <h3 style={{ marginBottom: 'var(--sp-4)' }}>Pagamento</h3>
              <div className="choice choice--active" style={{ marginBottom: 'var(--sp-4)' }}>
                <span className="choice__ico"><Wallet size={18} /></span>
                <span><span className="choice__t" style={{ display: 'block' }}>Carteira Ingressify</span><span className="choice__d">Saldo da plataforma</span></span>
              </div>

              {erro ? (
                <div className="auth-alert auth-alert--err"><AlertCircle size={18} />{erro}</div>
              ) : (
                <div className="auth-alert auth-alert--ok"><CheckCircle2 size={18} />Tudo certo — seu saldo cobre o total.</div>
              )}

              <button className="btn btn--lg btn--block" onClick={confirmarCompra} disabled={confirmando}>
                <Lock size={18} />{confirmando ? 'Processando…' : `Confirmar · ${formatMoeda(total)}`}
              </button>
              <p className="muted row" style={{ gap: 6, justifyContent: 'center', fontSize: '0.75rem', marginTop: 'var(--sp-3)' }}>
                <ShieldCheck size={14} /> Pagamento protegido pela Ingressify
              </p>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
