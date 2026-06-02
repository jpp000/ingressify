import { useEffect, useState } from 'react'
import { eventoService } from '../services/api'

const USUARIO_ID = 1

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  status: string
  capacidade: number
}

export default function GerenciarEventos() {
  const [eventos, setEventos] = useState<Evento[]>([])
  const [form, setForm] = useState({ nome: '', dataHora: '', local: '', descricao: '', capacidade: 100, prazoReembolsoDias: 7, aberturaPortoes: '' })
  const [msg, setMsg] = useState('')

  const carregar = () => {
    eventoService.listar(USUARIO_ID).then(r => setEventos(r.data))
  }

  useEffect(() => { carregar() }, [])

  const criar = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await eventoService.criar(USUARIO_ID, { ...form, imagemCapaUrl: null })
      setMsg('Evento criado com sucesso!')
      setForm({ nome: '', dataHora: '', local: '', descricao: '', capacidade: 100, prazoReembolsoDias: 7, aberturaPortoes: '' })
      carregar()
    } catch (err: any) {
      setMsg(err.response?.data?.message || 'Erro ao criar evento')
    }
  }

  const cancelar = async (id: number) => {
    try {
      await eventoService.cancelar(id, USUARIO_ID)
      setMsg('Evento cancelado')
      carregar()
    } catch {
      setMsg('Erro ao cancelar evento')
    }
  }

  return (
    <div>
      <h1>Gerenciar Eventos</h1>

      <h2>Criar Evento</h2>
      <form onSubmit={criar} style={{ display: 'flex', flexDirection: 'column', gap: 8, maxWidth: 480 }}>
        <input placeholder="Nome do evento" value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd' }} required />
        <input type="datetime-local" placeholder="Data e hora" value={form.dataHora} onChange={e => setForm({ ...form, dataHora: e.target.value })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd' }} required />
        <input placeholder="Local" value={form.local} onChange={e => setForm({ ...form, local: e.target.value })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd' }} required />
        <textarea placeholder="Descrição" value={form.descricao} onChange={e => setForm({ ...form, descricao: e.target.value })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd', resize: 'vertical' }} rows={3} />
        <input type="number" min={1} placeholder="Capacidade" value={form.capacidade} onChange={e => setForm({ ...form, capacidade: Number(e.target.value) })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd' }} required />
        <input type="number" min={0} placeholder="Prazo de reembolso (dias)" value={form.prazoReembolsoDias} onChange={e => setForm({ ...form, prazoReembolsoDias: Number(e.target.value) })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd' }} />
        <input type="datetime-local" placeholder="Abertura dos portões" value={form.aberturaPortoes} onChange={e => setForm({ ...form, aberturaPortoes: e.target.value })} style={{ padding: 8, borderRadius: 4, border: '1px solid #ddd' }} required />
        <button type="submit" style={{ padding: '10px', background: '#0070f3', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer', fontSize: 16 }}>
          Criar Evento
        </button>
      </form>
      {msg && <p style={{ color: msg.includes('sucesso') ? 'green' : 'red' }}>{msg}</p>}

      <h2>Meus Eventos</h2>
      {eventos.length === 0 && <p>Nenhum evento criado.</p>}
      {eventos.map(ev => (
        <div key={ev.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 8, borderRadius: 6 }}>
          <strong>{ev.nome}</strong> — {ev.status}
          <p style={{ margin: '4px 0', color: '#666', fontSize: 14 }}>
            {new Date(ev.dataHora).toLocaleString('pt-BR')} | {ev.local} | Cap: {ev.capacidade}
          </p>
          {ev.status === 'ATIVO' && (
            <button onClick={() => cancelar(ev.id)} style={{ marginTop: 8, padding: '6px 12px', background: '#e53e3e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
              Cancelar Evento
            </button>
          )}
        </div>
      ))}
    </div>
  )
}
