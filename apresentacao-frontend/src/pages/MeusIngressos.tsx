import { useEffect, useState } from 'react'
import { ingressoService } from '../services/api'

const USUARIO_ID = 1

interface Ingresso {
  id: string
  eventoId: number
  status: string
  tipoIngressoId: number
}

export default function MeusIngressos() {
  const [ingressos, setIngressos] = useState<Ingresso[]>([])
  const [msg, setMsg] = useState('')

  useEffect(() => {
    ingressoService.meus(USUARIO_ID).then(r => setIngressos(r.data))
  }, [])

  const reembolsar = async (id: string) => {
    try {
      await ingressoService.reembolsar(id, USUARIO_ID)
      setMsg('Reembolso solicitado com sucesso!')
      setIngressos(prev => prev.filter(i => i.id !== id))
    } catch {
      setMsg('Erro ao solicitar reembolso')
    }
  }

  return (
    <div>
      <h1>Meus Ingressos</h1>
      {msg && <p style={{ color: msg.includes('sucesso') ? 'green' : 'red' }}>{msg}</p>}
      {ingressos.length === 0 && <p>Você não possui ingressos.</p>}
      {ingressos.map(i => (
        <div key={i.id} style={{ border: '1px solid #ddd', padding: 12, marginBottom: 8, borderRadius: 6 }}>
          <p style={{ margin: 0 }}><strong>ID:</strong> {i.id}</p>
          <p style={{ margin: '4px 0' }}><strong>Status:</strong> {i.status}</p>
          <p style={{ margin: '4px 0' }}><strong>QR Code:</strong> {i.id}</p>
          {i.status === 'ATIVO' && (
            <button onClick={() => reembolsar(i.id)} style={{ marginTop: 8, padding: '6px 12px', background: '#e53e3e', color: '#fff', border: 'none', borderRadius: 4, cursor: 'pointer' }}>
              Solicitar Reembolso
            </button>
          )}
        </div>
      ))}
    </div>
  )
}
