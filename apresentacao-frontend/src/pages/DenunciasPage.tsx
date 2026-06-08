import { useEffect, useMemo, useState } from 'react'
import Navbar from '../components/Navbar'
import { denunciaService, eventoService, revendaService } from '../services/api'
import { formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

interface Denuncia {
  id: number
  anuncioId: number
  denuncianteId: number
  motivo: string
  descricao: string
  status: string
  decisao: string | null
  criadaEm: string
  decididaEm: string | null
}

interface Anuncio {
  id: number
  preco: number
  status: string
  vendedorId: number
  eventoId: number
}

interface Evento {
  id: number
  nome: string
  local: string
}

const MOTIVO_LABELS: Record<string, string> = {
  PRECO_ABUSIVO: 'Preço abusivo',
  INGRESSO_SUSPEITO: 'Ingresso suspeito',
  COMPORTAMENTO_INADEQUADO: 'Comportamento inadequado',
  OUTRO: 'Outro',
}

const DECISAO_LABELS: Record<string, string> = {
  ARQUIVADA: 'Arquivar',
  ANUNCIO_REMOVIDO: 'Remover anúncio',
  VENDEDOR_AVISADO: 'Avisar vendedor',
  VENDEDOR_BLOQUEADO: 'Bloquear vendedor',
}

const DECISOES = Object.keys(DECISAO_LABELS)

export default function DenunciasPage() {
  const { usuario, isAdmin } = useAuth()
  const [denuncias, setDenuncias] = useState<Denuncia[]>([])
  const [anunciosMap, setAnunciosMap] = useState<Map<number, Anuncio>>(new Map())
  const [eventosMap, setEventosMap] = useState<Map<number, Evento>>(new Map())
  const [filtroStatus, setFiltroStatus] = useState<'TODAS' | 'PENDENTE' | 'RESOLVIDA'>('TODAS')
  const [carregando, setCarregando] = useState(true)
  const [msg, setMsg] = useState('')
  const [decidindoId, setDecidindoId] = useState<number | null>(null)

  const carregar = async () => {
    if (!usuario) return
    setCarregando(true)
    setMsg('')
    try {
      const res = await denunciaService.listar(usuario.id)
      const lista: Denuncia[] = res.data
      const anuncioIds = [...new Set(lista.map(d => d.anuncioId))]

      const anuncios = await Promise.all(
        anuncioIds.map(id => revendaService.detalhe(id).then(r => r.data as Anuncio).catch(() => null))
      )
      const mapaAnuncios = new Map<number, Anuncio>()
      anuncios.forEach(a => { if (a) mapaAnuncios.set(a.id, a) })

      const eventoIds = [...new Set(anuncios.filter(Boolean).map(a => (a as Anuncio).eventoId))]
      const eventos = await Promise.all(
        eventoIds.map(id => eventoService.detalhe(id).then(r => [id, r.data] as [number, Evento]))
      )

      setDenuncias(lista)
      setAnunciosMap(mapaAnuncios)
      setEventosMap(new Map(eventos))
    } catch {
      setMsg('Erro ao carregar denúncias.')
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => { carregar() }, [usuario])

  const denunciasFiltradas = useMemo(() => {
    if (filtroStatus === 'TODAS') return denuncias
    return denuncias.filter(d => d.status === filtroStatus)
  }, [denuncias, filtroStatus])

  const pendentes = denuncias.filter(d => d.status === 'PENDENTE').length

  const decidir = async (denunciaId: number, decisao: string) => {
    if (!usuario) return
    setDecidindoId(denunciaId)
    setMsg('')
    try {
      await denunciaService.decidir(denunciaId, usuario.id, decisao)
      setMsg('Denúncia resolvida com sucesso.')
      await carregar()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { motivo?: string } } }
      setMsg(err.response?.data?.motivo ?? 'Erro ao decidir denúncia.')
    } finally {
      setDecidindoId(null)
    }
  }

  if (!isAdmin()) {
    return (
      <>
        <Navbar />
        <div style={{ maxWidth: 900, margin: '0 auto', padding: '40px 24px', textAlign: 'center' }}>
          <p style={{ color: '#64748b' }}>Acesso restrito a administradores.</p>
        </div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24, flexWrap: 'wrap', gap: 16 }}>
          <div>
            <h1 style={{ fontSize: 28, fontWeight: 800, color: '#1e293b', marginBottom: 6 }}>
              Denúncias de Revendas
            </h1>
            <p style={{ color: '#64748b', fontSize: 14 }}>
              {pendentes > 0 ? `${pendentes} denúncia(s) aguardando moderação` : 'Nenhuma denúncia pendente'}
            </p>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            {(['TODAS', 'PENDENTE', 'RESOLVIDA'] as const).map(status => (
              <button
                key={status}
                onClick={() => setFiltroStatus(status)}
                style={{
                  background: filtroStatus === status ? '#1d4ed8' : '#f1f5f9',
                  color: filtroStatus === status ? '#fff' : '#475569',
                  border: 'none',
                  borderRadius: 20,
                  padding: '8px 16px',
                  fontSize: 13,
                  fontWeight: 600,
                  cursor: 'pointer',
                }}
              >
                {status === 'TODAS' ? 'Todas' : status === 'PENDENTE' ? 'Pendentes' : 'Resolvidas'}
              </button>
            ))}
          </div>
        </div>

        {msg && (
          <div style={{
            background: msg.includes('sucesso') ? '#f0fdf4' : '#fef2f2',
            border: `1px solid ${msg.includes('sucesso') ? '#bbf7d0' : '#fecaca'}`,
            borderRadius: 10,
            padding: '12px 16px',
            marginBottom: 20,
            fontSize: 14,
            color: msg.includes('sucesso') ? '#166534' : '#b91c1c',
          }}>
            {msg}
          </div>
        )}

        {carregando ? (
          <p style={{ color: '#94a3b8', textAlign: 'center', padding: 40 }}>Carregando...</p>
        ) : denunciasFiltradas.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 60, color: '#94a3b8' }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>📋</div>
            <p>Nenhuma denúncia encontrada.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {denunciasFiltradas.map(d => {
              const anuncio = anunciosMap.get(d.anuncioId)
              const evento = anuncio ? eventosMap.get(anuncio.eventoId) : undefined
              const resolvida = d.status === 'RESOLVIDA'

              return (
                <div
                  key={d.id}
                  style={{
                    background: '#fff',
                    border: '1px solid #e2e8f0',
                    borderRadius: 14,
                    padding: '20px 24px',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.04)',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                        <span style={{ fontWeight: 700, color: '#1e293b', fontSize: 16 }}>
                          #{d.id} — {evento?.nome ?? `Anúncio #${d.anuncioId}`}
                        </span>
                        <span style={{
                          fontSize: 11,
                          fontWeight: 700,
                          padding: '2px 8px',
                          borderRadius: 20,
                          background: resolvida ? '#f1f5f9' : '#fef3c7',
                          color: resolvida ? '#64748b' : '#92400e',
                        }}>
                          {resolvida ? 'Resolvida' : 'Pendente'}
                        </span>
                      </div>
                      {evento && (
                        <p style={{ fontSize: 13, color: '#64748b', margin: 0 }}>{evento.local}</p>
                      )}
                    </div>
                    <span style={{ fontSize: 12, color: '#94a3b8' }}>
                      {new Date(d.criadaEm).toLocaleString('pt-BR')}
                    </span>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: 12, marginBottom: 14 }}>
                    <Info label="Motivo" value={MOTIVO_LABELS[d.motivo] ?? d.motivo} />
                    <Info label="Denunciante" value={`Usuário #${d.denuncianteId}`} />
                    {anuncio && (
                      <>
                        <Info label="Vendedor" value={`Usuário #${anuncio.vendedorId}`} />
                        <Info label="Preço anunciado" value={formatMoeda(anuncio.preco)} />
                        <Info label="Status do anúncio" value={anuncio.status} />
                      </>
                    )}
                    {d.decisao && <Info label="Decisão" value={DECISAO_LABELS[d.decisao] ?? d.decisao} />}
                  </div>

                  {d.descricao && (
                    <p style={{
                      fontSize: 14,
                      color: '#475569',
                      background: '#f8fafc',
                      borderRadius: 8,
                      padding: '10px 14px',
                      margin: '0 0 14px',
                      lineHeight: 1.5,
                    }}>
                      {d.descricao}
                    </p>
                  )}

                  {!resolvida && (
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      {DECISOES.map(decisao => (
                        <button
                          key={decisao}
                          disabled={decidindoId === d.id}
                          onClick={() => decidir(d.id, decisao)}
                          style={{
                            background: decisao === 'VENDEDOR_BLOQUEADO' || decisao === 'ANUNCIO_REMOVIDO'
                              ? '#fef2f2' : '#f1f5f9',
                            color: decisao === 'VENDEDOR_BLOQUEADO' || decisao === 'ANUNCIO_REMOVIDO'
                              ? '#b91c1c' : '#475569',
                            border: '1px solid #e2e8f0',
                            borderRadius: 8,
                            padding: '8px 14px',
                            fontSize: 13,
                            fontWeight: 600,
                            cursor: decidindoId === d.id ? 'wait' : 'pointer',
                            opacity: decidindoId === d.id ? 0.6 : 1,
                          }}
                        >
                          {DECISAO_LABELS[decisao]}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>
    </>
  )
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <div style={{ fontSize: 11, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 2 }}>
        {label}
      </div>
      <div style={{ fontSize: 14, color: '#1e293b', fontWeight: 500 }}>{value}</div>
    </div>
  )
}
