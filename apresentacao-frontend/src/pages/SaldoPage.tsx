import { useEffect, useState } from 'react'
import { saldoService } from '../services/api'

const USUARIO_ID = 1

interface Transacao {
  id: number
  tipo: string
  valor: number
  data: string
}

export default function SaldoPage() {
  const [saldo, setSaldo] = useState<number | null>(null)
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [deposito, setDeposito] = useState('')
  const [msg, setMsg] = useState('')

  const carregarDados = () => {
    saldoService.obter(USUARIO_ID).then(r => setSaldo(r.data.valor))
    saldoService.transacoes(USUARIO_ID).then(r => setTransacoes(r.data))
  }

  useEffect(() => { carregarDados() }, [])

  const adicionar = async () => {
    const valor = parseFloat(deposito)
    if (!valor || valor <= 0) { setMsg('Informe um valor positivo'); return }
    try {
      await saldoService.adicionar(USUARIO_ID, valor)
      setMsg('Saldo adicionado com sucesso!')
      setDeposito('')
      carregarDados()
    } catch {
      setMsg('Erro ao adicionar saldo')
    }
  }

  return (
    <div>
      <h1>Minha Carteira</h1>
      <div style={{ background: '#f0f7ff', padding: 16, borderRadius: 8, marginBottom: 16 }}>
        <p style={{ margin: 0, fontSize: 14, color: '#666' }}>Saldo disponível</p>
        <p style={{ margin: '4px 0 0', fontSize: 28, fontWeight: 'bold' }}>
          R$ {saldo !== null ? Number(saldo).toFixed(2) : '--'}
        </p>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <input
          type="number"
          min="0.01"
          step="0.01"
          placeholder="Valor a depositar"
          value={deposito}
          onChange={e => setDeposito(e.target.value)}
          style={{ flex: 1, padding: 8, borderRadius: 4, border: '1px solid #ddd' }}
        />
        <button onClick={adicionar} style={{ padding: '8px 16px', background: '#38a169', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
          Depositar
        </button>
      </div>
      {msg && <p style={{ color: msg.includes('sucesso') ? 'green' : 'red' }}>{msg}</p>}

      <h2>Extrato</h2>
      {transacoes.length === 0 && <p>Nenhuma movimentação.</p>}
      {transacoes.map(t => (
        <div key={t.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #eee' }}>
          <span>{t.tipo} — {new Date(t.data).toLocaleDateString('pt-BR')}</span>
          <span style={{ color: ['REEMBOLSO', 'VENDA'].includes(t.tipo) ? 'green' : 'inherit', fontWeight: 500 }}>
            R$ {Number(t.valor).toFixed(2)}
          </span>
        </div>
      ))}
    </div>
  )
}
