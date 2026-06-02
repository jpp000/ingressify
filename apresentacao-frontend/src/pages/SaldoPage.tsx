import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import { saldoService } from '../services/api'
import { USUARIO_ID, formatMoeda } from '../constants'

interface Transacao {
  id: number
  tipo: string
  valor: number
  data: string
}

const TIPO_POSITIVO = new Set(['REEMBOLSO', 'VENDA', 'DEPOSITO'])

export default function SaldoPage() {
  const [saldo, setSaldo] = useState<number | null>(null)
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [deposito, setDeposito] = useState('')
  const [msg, setMsg] = useState('')
  const [salvando, setSalvando] = useState(false)

  const carregarDados = () => {
    saldoService.obter(USUARIO_ID).then(r => setSaldo(r.data.valor))
    saldoService.transacoes(USUARIO_ID).then(r => setTransacoes(r.data))
  }

  useEffect(() => { carregarDados() }, [])

  const adicionar = async () => {
    const valor = parseFloat(deposito.replace(',', '.'))
    if (!valor || valor <= 0) { setMsg('Informe um valor positivo'); return }
    setSalvando(true)
    try {
      await saldoService.adicionar(USUARIO_ID, valor)
      setMsg('Saldo adicionado com sucesso!')
      setDeposito('')
      carregarDados()
    } catch {
      setMsg('Erro ao adicionar saldo')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 800, margin: '0 auto', padding: '32px 24px' }}>
        <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1e293b', marginBottom: 24 }}>Minha Carteira</h1>

        {/* Card saldo */}
        <div style={{
          background: 'linear-gradient(135deg, #1e3a8a 0%, #1d4ed8 100%)',
          borderRadius: 20,
          padding: '28px 32px',
          color: '#fff',
          marginBottom: 28,
          boxShadow: '0 8px 24px rgba(29,78,216,0.3)',
        }}>
          <p style={{ fontSize: 13, opacity: 0.8, marginBottom: 6, textTransform: 'uppercase', letterSpacing: 0.8, fontWeight: 600 }}>
            Saldo Disponível
          </p>
          <p style={{ fontSize: 40, fontWeight: 800, letterSpacing: -1 }}>
            {saldo !== null ? formatMoeda(saldo) : '---'}
          </p>
        </div>

        {/* Depositar */}
        <div style={{ background: '#fff', borderRadius: 16, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 28 }}>
          <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', marginBottom: 16 }}>Adicionar Saldo</h2>
          <div style={{ display: 'flex', gap: 10 }}>
            <div style={{ flex: 1, position: 'relative' }}>
              <span style={{ position: 'absolute', left: 14, top: '50%', transform: 'translateY(-50%)', color: '#64748b', fontWeight: 600 }}>R$</span>
              <input
                type="text"
                placeholder="0,00"
                value={deposito}
                onChange={e => setDeposito(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px 14px 12px 44px',
                  border: '1px solid #e2e8f0',
                  borderRadius: 10,
                  fontSize: 16,
                  fontWeight: 600,
                  outline: 'none',
                  color: '#1e293b',
                }}
              />
            </div>
            <button
              onClick={adicionar}
              disabled={salvando}
              style={{
                background: salvando ? '#93c5fd' : '#16a34a',
                color: '#fff',
                border: 'none',
                borderRadius: 10,
                padding: '12px 28px',
                fontSize: 15,
                fontWeight: 700,
                cursor: salvando ? 'default' : 'pointer',
              }}
            >
              {salvando ? 'Depositando...' : 'Depositar'}
            </button>
          </div>
          {msg && (
            <p style={{ marginTop: 10, fontSize: 13, color: msg.includes('sucesso') ? '#16a34a' : '#ef4444', fontWeight: 600 }}>
              {msg}
            </p>
          )}
        </div>

        {/* Extrato */}
        <div style={{ background: '#fff', borderRadius: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
          <div style={{ padding: '20px 24px', borderBottom: '1px solid #f1f5f9' }}>
            <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b' }}>Extrato</h2>
          </div>
          {transacoes.length === 0 ? (
            <p style={{ textAlign: 'center', padding: '32px 0', color: '#94a3b8', fontSize: 14 }}>
              Nenhuma movimentação.
            </p>
          ) : (
            transacoes.map((t, i) => (
              <div
                key={t.id}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 14,
                  padding: '14px 24px',
                  borderBottom: i < transacoes.length - 1 ? '1px solid #f8fafc' : 'none',
                }}
              >
                <div style={{
                  width: 40, height: 40,
                  background: TIPO_POSITIVO.has(t.tipo) ? '#f0fdf4' : '#eff6ff',
                  borderRadius: 10,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 18, flexShrink: 0,
                }}>
                  {t.tipo === 'DEPOSITO' ? '➕' : t.tipo === 'REEMBOLSO' ? '↩️' : '🎟️'}
                </div>
                <div style={{ flex: 1 }}>
                  <p style={{ fontWeight: 600, fontSize: 14, color: '#1e293b' }}>
                    {t.tipo === 'DEPOSITO' ? 'Recarga de Saldo' : t.tipo === 'COMPRA' ? 'Compra de Ingresso' : t.tipo}
                  </p>
                  <p style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
                    {t.data ? new Date(t.data).toLocaleDateString('pt-BR', { day: '2-digit', month: 'long', year: 'numeric' }) : ''}
                  </p>
                </div>
                <span style={{
                  fontWeight: 700,
                  fontSize: 15,
                  color: TIPO_POSITIVO.has(t.tipo) ? '#16a34a' : '#1e293b',
                }}>
                  {TIPO_POSITIVO.has(t.tipo) ? '+' : '-'}{formatMoeda(Math.abs(t.valor))}
                </span>
              </div>
            ))
          )}
        </div>
      </div>
    </>
  )
}
