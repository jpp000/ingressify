import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { ingressoService, saldoService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, formatDataBadge } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  imagemCapaUrl?: string
}

interface TipoIngresso {
  id: number
  nome: string
  preco: number
}

interface ItemPedido {
  tipoId: number
  quantidade: number
}

interface LocationState {
  evento: Evento
  tipos: TipoIngresso[]
  itens: ItemPedido[]
}

export default function RevisaoPedido() {
  const location = useLocation()
  const navigate = useNavigate()
  const { usuario } = useAuth()
  const state = location.state as LocationState | null

  const [cupom, setCupom] = useState('')
  const [desconto, setDesconto] = useState(0)
  const [mensagemCupom, setMensagemCupom] = useState('')
  const [confirmando, setConfirmando] = useState(false)
  const [erro, setErro] = useState('')

  if (!state) {
    navigate('/')
    return null
  }

  const { evento, tipos, itens } = state

  const subtotal = itens.reduce((acc, item) => {
    const tipo = tipos.find(t => t.id === item.tipoId)
    return acc + (tipo ? Number(tipo.preco) * item.quantidade : 0)
  }, 0)

  const total = subtotal - desconto

  const aplicarCupom = () => {
    if (cupom.toUpperCase() === 'BEMVINDO10') {
      setDesconto(subtotal * 0.10)
      setMensagemCupom('Cupom aplicado! 10% de desconto.')
    } else {
      setDesconto(0)
      setMensagemCupom('Cupom inválido.')
    }
  }

  const confirmarCompra = async () => {
    setConfirmando(true)
    setErro('')
    try {
      const saldoRes = await saldoService.obter(usuario!.id)
      const saldoAtual = Number(saldoRes.data.valor)
      if (saldoAtual < total) {
        setErro(`Saldo insuficiente. Seu saldo é ${formatMoeda(saldoAtual)} e o valor cobrado é ${formatMoeda(total)}.`)
        setConfirmando(false)
        return
      }
      for (const item of itens) {
        await ingressoService.comprar(usuario!.id, {
          tipoIngressoId: item.tipoId,
          quantidade: item.quantidade,
        })
      }
      navigate('/meus-ingressos', { state: { sucesso: true } })
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      setErro(err.response?.data?.message ?? 'Erro ao confirmar compra. Verifique seu saldo.')
      setConfirmando(false)
    }
  }

  const { dia, mes } = formatDataBadge(evento.dataHora)
  const imagemEvento = evento.imagemCapaUrl ||
    'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&q=60'

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px' }}>
        <h1 style={{ fontSize: 28, fontWeight: 800, color: '#1e293b', marginBottom: 6 }}>Revise seu pedido</h1>
        <p style={{ color: '#64748b', fontSize: 14, marginBottom: 32 }}>
          Por favor, verifique os detalhes do ingresso antes de confirmar.
        </p>

        <div style={{ display: 'flex', gap: 28, alignItems: 'flex-start' }}>
          {/* Coluna esquerda */}
          <div style={{ flex: 1 }}>
            {/* Card evento */}
            <div style={{ background: '#fff', borderRadius: 16, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', marginBottom: 24 }}>
              <div style={{ position: 'relative', height: 180 }}>
                <img src={imagemEvento} alt={evento.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                <div style={{
                  position: 'absolute',
                  top: 12, left: 12,
                  background: 'rgba(0,0,0,0.75)',
                  color: '#fff',
                  borderRadius: 8,
                  padding: '4px 12px',
                  textAlign: 'center',
                }}>
                  <div style={{ fontSize: 16, fontWeight: 700 }}>{mes}</div>
                  <div style={{ fontSize: 22, fontWeight: 800, lineHeight: 1 }}>{dia}</div>
                </div>
              </div>
              <div style={{ padding: '16px 20px' }}>
                <h2 style={{ fontSize: 18, fontWeight: 700, color: '#1e293b', marginBottom: 4 }}>{evento.nome}</h2>
                <p style={{ fontSize: 14, color: '#64748b' }}>📍 {evento.local}</p>
              </div>
            </div>

            {/* Resumo do pedido */}
            <div style={{ background: '#fff', borderRadius: 16, padding: 24, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
              <h3 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 20, paddingBottom: 14, borderBottom: '1px solid #f1f5f9' }}>
                Resumo do Pedido
              </h3>

              {itens.map(item => {
                const tipo = tipos.find(t => t.id === item.tipoId)
                if (!tipo) return null
                return (
                  <div key={item.tipoId} style={{ marginBottom: 14 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontWeight: 600, color: '#1e293b', fontSize: 15 }}>{tipo.nome}</span>
                      <span style={{ fontWeight: 600, color: '#1e293b' }}>{formatMoeda(Number(tipo.preco) * item.quantidade)}</span>
                    </div>
                    <p style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>Qtd: {item.quantidade}</p>
                  </div>
                )
              })}

              {desconto > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0' }}>
                  <span style={{ fontSize: 14, color: '#16a34a' }}>Desconto (cupom)</span>
                  <span style={{ fontSize: 14, color: '#16a34a' }}>- {formatMoeda(desconto)}</span>
                </div>
              )}

              {/* Cupom */}
              <div style={{ margin: '16px 0' }}>
                <p style={{ fontSize: 11, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: 8 }}>
                  Cupom de Desconto
                </p>
                <div style={{ display: 'flex', gap: 8 }}>
                  <input
                    placeholder="Insira o código"
                    value={cupom}
                    onChange={e => setCupom(e.target.value)}
                    style={{ flex: 1, padding: '10px 14px', border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14, outline: 'none' }}
                  />
                  <button
                    onClick={aplicarCupom}
                    style={{ background: '#1d4ed8', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 20px', fontSize: 13, fontWeight: 700, cursor: 'pointer' }}
                  >
                    APLICAR
                  </button>
                </div>
                {mensagemCupom && (
                  <p style={{ fontSize: 13, color: mensagemCupom.includes('aplicado') ? '#16a34a' : '#ef4444', marginTop: 6 }}>
                    {mensagemCupom}
                  </p>
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', paddingTop: 16, borderTop: '2px solid #f1f5f9' }}>
                <span style={{ fontSize: 17, fontWeight: 700, color: '#1e293b' }}>Total</span>
                <span style={{ fontSize: 24, fontWeight: 800, color: '#1d4ed8' }}>{formatMoeda(total)}</span>
              </div>
            </div>
          </div>

          {/* Coluna direita */}
          <div style={{ width: 340, flexShrink: 0 }}>
            <div style={{ background: '#fff', borderRadius: 16, padding: 24, boxShadow: '0 2px 8px rgba(0,0,0,0.08)', marginBottom: 16 }}>
              <h3 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 16 }}>Método de Pagamento</h3>

              <div style={{
                border: '2px solid #1d4ed8',
                borderRadius: 12,
                padding: '14px 16px',
                display: 'flex',
                alignItems: 'center',
                gap: 14,
                marginBottom: 16,
              }}>
                <div style={{
                  width: 20, height: 20,
                  borderRadius: '50%',
                  border: '2px solid #1d4ed8',
                  background: '#1d4ed8',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#fff' }} />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, flex: 1 }}>
                  <span style={{ fontSize: 22 }}>💳</span>
                  <div>
                    <p style={{ fontWeight: 700, fontSize: 14, color: '#1e293b' }}>Carteira Ingressefy</p>
                  </div>
                </div>
              </div>

              {/* Info saldo suficiente */}
              <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', borderRadius: 10, padding: '12px 14px', marginBottom: 20 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                  <span style={{ color: '#16a34a', fontSize: 16 }}>✅</span>
                  <p style={{ fontWeight: 700, fontSize: 13, color: '#15803d' }}>Saldo Suficiente</p>
                </div>
                <p style={{ fontSize: 12, color: '#16a34a' }}>
                  O saldo da sua carteira cobre o custo total.
                </p>
              </div>

              {erro && (
                <div style={{ background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 10, padding: '12px 14px', marginBottom: 16 }}>
                  <p style={{ fontSize: 13, color: '#ef4444' }}>{erro}</p>
                </div>
              )}

              <button
                onClick={confirmarCompra}
                disabled={confirmando}
                style={{
                  width: '100%',
                  background: confirmando ? '#93c5fd' : '#1d4ed8',
                  color: '#fff',
                  border: 'none',
                  borderRadius: 10,
                  padding: '16px',
                  fontSize: 16,
                  fontWeight: 700,
                  cursor: confirmando ? 'default' : 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: 8,
                }}
              >
                🔒 {confirmando ? 'Processando...' : 'Confirmar Compra'}
              </button>

              <p style={{ textAlign: 'center', fontSize: 11, color: '#94a3b8', marginTop: 12, lineHeight: 1.5 }}>
                Ao confirmar, você concorda com nossos{' '}
                <span style={{ color: '#1d4ed8' }}>Termos de Serviço</span>{' '}
                e{' '}
                <span style={{ color: '#1d4ed8' }}>Políticas do Evento</span>.
                Todas as vendas são finais.
              </p>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}
