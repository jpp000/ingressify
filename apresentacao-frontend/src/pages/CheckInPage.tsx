import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import Navbar from '../components/Navbar'
import SeletorEvento from '../components/SeletorEvento'
import { checkinService, eventoService } from '../services/api'
import { useAuth } from '../context/AuthContext'

interface RegistroCheckIn {
  registroId: string
  ingressoId: string
  operadorId: number
  dataHora: string
}

interface Evento {
  id: number
  nome: string
}

export default function CheckInPage() {
  const [searchParams] = useSearchParams()
  const eventoId = searchParams.get('eventoId')
  const { usuario, isOrganizador, isAdmin, isOperadorPorta } = useAuth()

  const [evento, setEvento] = useState<Evento | null>(null)
  const [codigo, setCodigo] = useState('')
  const [relatorio, setRelatorio] = useState<RegistroCheckIn[]>([])
  const [carregando, setCarregando] = useState(false)
  const [msg, setMsg] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)

  const podeOperar = isOrganizador() || isAdmin() || isOperadorPorta()

  useEffect(() => {
    if (!eventoId) return
    eventoService.detalhe(Number(eventoId))
      .then(r => setEvento(r.data))
      .catch(() => setEvento(null))
    carregarRelatorio()
  }, [eventoId])

  const carregarRelatorio = () => {
    if (!eventoId || !usuario) return
    checkinService.relatorio(Number(eventoId), usuario.id)
      .then(r => setRelatorio(r.data))
      .catch(() => setRelatorio([]))
  }

  const exibirMsg = (texto: string, tipo: 'ok' | 'erro') => {
    setMsg({ texto, tipo })
    setTimeout(() => setMsg(null), 4000)
  }

  const realizarCheckIn = async (metodo: 'manual' | 'escanear') => {
    if (!usuario || !codigo.trim()) return
    setCarregando(true)
    try {
      const res = metodo === 'manual'
        ? await checkinService.manual(usuario.id, codigo.trim())
        : await checkinService.escanear(usuario.id, codigo.trim())
      const data = res.data as { sucesso: boolean; mensagem: string }
      if (data.sucesso) {
        exibirMsg(data.mensagem || 'Check-in realizado com sucesso!', 'ok')
        setCodigo('')
        carregarRelatorio()
      } else {
        exibirMsg(data.mensagem || 'Falha no check-in.', 'erro')
      }
    } catch (e: unknown) {
      const err = e as { response?: { data?: { mensagem?: string } } }
      exibirMsg(err.response?.data?.mensagem ?? 'Erro ao realizar check-in.', 'erro')
    } finally {
      setCarregando(false)
    }
  }

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: '40px 24px' }}>
        <h1 style={{ fontSize: 28, fontWeight: 800, color: 'var(--ink)', marginBottom: 6 }}>
          Check-in de Ingressos
        </h1>
        <p style={{ color: 'var(--ink-2)', fontSize: 14, marginBottom: 28 }}>
          Escaneie QR codes ou digite o código do ingresso para validar a entrada.
        </p>

        {!podeOperar && (
          <div style={{
            background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 10,
            padding: '12px 16px', marginBottom: 24, color: '#dc2626', fontSize: 14,
          }}>
            Apenas organizadores e operadores de porta podem realizar check-in.
          </div>
        )}

        {!eventoId ? (
          <SeletorEvento
            titulo="Selecione o evento"
            descricao="Escolha o evento para gerenciar o check-in dos participantes."
            rotaDestino="/check-in"
            icone="🎫"
          />
        ) : (
          <>
            {evento && (
              <div style={{
                background: 'var(--brand-soft)', border: '1px solid var(--brand-soft-2)', borderRadius: 12,
                padding: '14px 20px', marginBottom: 24, display: 'flex', alignItems: 'center', gap: 12,
              }}>
                <span style={{ fontSize: 24 }}>📅</span>
                <div>
                  <p style={{ fontWeight: 700, color: 'var(--ink)', margin: 0 }}>{evento.nome}</p>
                  <p style={{ fontSize: 12, color: 'var(--ink-2)', margin: '2px 0 0' }}>Evento #{eventoId}</p>
                </div>
              </div>
            )}

            {msg && (
              <div style={{
                background: msg.tipo === 'ok' ? '#f0fdf4' : '#fef2f2',
                border: `1px solid ${msg.tipo === 'ok' ? '#bbf7d0' : '#fecaca'}`,
                borderRadius: 10, padding: '12px 16px', marginBottom: 20,
                color: msg.tipo === 'ok' ? '#15803d' : '#dc2626', fontSize: 14, fontWeight: 600,
              }}>
                {msg.texto}
              </div>
            )}

            {podeOperar && (
              <div style={{
                background: '#fff', borderRadius: 16, padding: 24,
                boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 32,
              }}>
                <h2 style={{ fontSize: 16, fontWeight: 700, color: 'var(--ink)', marginBottom: 16 }}>
                  Validar Ingresso
                </h2>
                <div style={{ display: 'flex', gap: 12, marginBottom: 12 }}>
                  <input
                    type="text"
                    placeholder="Cole o QR code ou digite o código do ingresso..."
                    value={codigo}
                    onChange={e => setCodigo(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && realizarCheckIn('manual')}
                    style={{
                      flex: 1, padding: '12px 16px',
                      border: '1px solid var(--border)', borderRadius: 8, fontSize: 14, outline: 'none',
                    }}
                  />
                </div>
                <div style={{ display: 'flex', gap: 10 }}>
                  <button
                    onClick={() => realizarCheckIn('manual')}
                    disabled={carregando || !codigo.trim()}
                    style={{
                      background: 'var(--brand-strong)', color: '#fff', border: 'none',
                      borderRadius: 8, padding: '10px 20px', fontSize: 14, fontWeight: 600,
                      cursor: carregando ? 'wait' : 'pointer',
                      opacity: !codigo.trim() ? 0.5 : 1,
                    }}
                  >
                    {carregando ? 'Validando...' : '✓ Check-in Manual'}
                  </button>
                  <button
                    onClick={() => realizarCheckIn('escanear')}
                    disabled={carregando || !codigo.trim()}
                    style={{
                      background: '#16a34a', color: '#fff', border: 'none',
                      borderRadius: 8, padding: '10px 20px', fontSize: 14, fontWeight: 600,
                      cursor: carregando ? 'wait' : 'pointer',
                      opacity: !codigo.trim() ? 0.5 : 1,
                    }}
                  >
                    📷 Validar QR Code
                  </button>
                </div>
              </div>
            )}

            <section>
              <h2 style={{ fontSize: 16, fontWeight: 700, color: 'var(--ink)', marginBottom: 12 }}>
                Relatório de Check-ins ({relatorio.length})
              </h2>
              {relatorio.length === 0 ? (
                <div style={{
                  textAlign: 'center', padding: '40px 24px',
                  background: 'var(--surface-2)', borderRadius: 12, color: 'var(--ink-muted)',
                }}>
                  Nenhum check-in registrado ainda.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                  {relatorio.map(r => (
                    <div
                      key={r.registroId}
                      style={{
                        background: '#fff', border: '1px solid var(--border)', borderRadius: 10,
                        padding: '12px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                      }}
                    >
                      <div>
                        <p style={{ fontWeight: 600, color: 'var(--ink)', fontSize: 14, margin: 0 }}>
                          Ingresso {r.ingressoId.slice(0, 8)}...
                        </p>
                        <p style={{ fontSize: 12, color: 'var(--ink-2)', margin: '2px 0 0' }}>
                          Operador #{r.operadorId}
                        </p>
                      </div>
                      <span style={{ fontSize: 12, color: '#16a34a', fontWeight: 600 }}>
                        {new Date(r.dataHora).toLocaleString('pt-BR')}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </div>
    </>
  )
}
