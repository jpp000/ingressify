import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { eventoService, tipoIngressoService, ingressoService, avaliacaoService } from '../services/api'

const USUARIO_ID = 1 // simulado

interface TipoIngresso {
  id: number
  nome: string
  preco: number
  quantidadeDisponivel: number
}

interface Avaliacao {
  id: number
  nota: number
  comentario: string
}

export default function EventoDetalhe() {
  const { id } = useParams<{ id: string }>()
  const eventoId = Number(id)
  const [evento, setEvento] = useState<any>(null)
  const [tipos, setTipos] = useState<TipoIngresso[]>([])
  const [avaliacoes, setAvaliacoes] = useState<Avaliacao[]>([])
  const [qtd, setQtd] = useState(1)
  const [tipoSelecionado, setTipoSelecionado] = useState<number | null>(null)
  const [msg, setMsg] = useState('')

  useEffect(() => {
    eventoService.detalhe(eventoId).then(r => setEvento(r.data))
    tipoIngressoService.listar(eventoId).then(r => setTipos(r.data))
    avaliacaoService.listar(eventoId).then(r => setAvaliacoes(r.data))
  }, [eventoId])

  const comprar = async () => {
    if (!tipoSelecionado) { setMsg('Selecione um tipo de ingresso'); return }
    try {
      await ingressoService.comprar(USUARIO_ID, { tipoIngressoId: tipoSelecionado, quantidade: qtd })
      setMsg('Compra realizada com sucesso!')
    } catch (e: any) {
      setMsg(e.response?.data?.message || 'Erro ao comprar')
    }
  }

  if (!evento) return <p>Carregando...</p>

  return (
    <div>
      <h1>{evento.nome}</h1>
      <p>{new Date(evento.dataHora).toLocaleString('pt-BR')} — {evento.local}</p>
      {evento.descricao && <p>{evento.descricao}</p>}

      <h2>Ingressos Disponíveis</h2>
      {tipos.map(t => (
        <div key={t.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 8, borderRadius: 6, cursor: 'pointer', background: tipoSelecionado === t.id ? '#e8f4fd' : '#fff' }} onClick={() => setTipoSelecionado(t.id)}>
          <strong>{t.nome}</strong> — R$ {Number(t.preco).toFixed(2)} — Disponíveis: {t.quantidadeDisponivel}
        </div>
      ))}
      <div style={{ display: 'flex', gap: 8, alignItems: 'center', marginTop: 8 }}>
        <label>Quantidade:</label>
        <input type="number" min={1} value={qtd} onChange={e => setQtd(Number(e.target.value))} style={{ width: 60, padding: 4 }} />
        <button onClick={comprar} style={{ padding: '8px 16px', background: '#0070f3', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
          Comprar
        </button>
      </div>
      {msg && <p style={{ color: msg.includes('sucesso') ? 'green' : 'red' }}>{msg}</p>}

      <h2>Avaliações</h2>
      {avaliacoes.length === 0 && <p>Nenhuma avaliação ainda.</p>}
      {avaliacoes.map(a => (
        <div key={a.id} style={{ border: '1px solid #eee', padding: 8, marginBottom: 6, borderRadius: 4 }}>
          <strong>{'★'.repeat(a.nota)}{'☆'.repeat(5 - a.nota)}</strong>
          {a.comentario && <p style={{ margin: '4px 0 0' }}>{a.comentario}</p>}
        </div>
      ))}
    </div>
  )
}
