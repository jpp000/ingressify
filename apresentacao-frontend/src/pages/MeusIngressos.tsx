import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { ingressoService, eventoService, tipoIngressoService, saldoService, revendaService, avaliacaoService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, formatDataBadge } from '../constants'
import { useAuth } from '../context/AuthContext'

interface IngressoRaw {
  id: string
  eventoId: number
  tipoIngressoId: number
  proprietarioId: number
  status: string
  bloqueadoPorReembolso: boolean
}

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  imagemCapaUrl?: string
  aberturaPortoes?: string
  status: string
}

interface TipoIngresso {
  id: number
  nome: string
  preco: number
}

interface IngressoEnriquecido {
  ingresso: IngressoRaw
  evento: Evento
  tipo: TipoIngresso
}

interface AnuncioRaw {
  id: number
  ingressoIds: string[]
  vendedorId: number
  compradorId: number | null
  eventoId: number
  preco: number
  status: string
  quantidade: number
}

interface AnuncioEnriquecido {
  anuncio: AnuncioRaw
  evento: Evento
}

interface Transacao {
  id: number
  tipo: string
  valor: number
  data: string
}

const TIPO_LABELS: Record<string, string> = {
  COMPRA: 'Cartão de Crédito',
  DEPOSITO: 'PIX',
  REEMBOLSO: 'Reembolso',
  VENDA: 'Venda',
  AJUSTE: 'Ajuste',
}

const TIPO_POSITIVO = new Set(['REEMBOLSO', 'VENDA', 'DEPOSITO'])

export default function MeusIngressos() {
  const location = useLocation()
  const { usuario } = useAuth()
  const state = location.state as { sucesso?: boolean; anuncioPublicado?: boolean } | null

  const [ingressos, setIngressos] = useState<IngressoEnriquecido[]>([])
  const [anuncios, setAnuncios] = useState<AnuncioEnriquecido[]>([])
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [saldo, setSaldo] = useState<number | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [abaAtiva, setAbaAtiva] = useState<'proximos' | 'encerrados' | 'revendas'>('proximos')
  const [ingressoAberto, setIngressoAberto] = useState<IngressoEnriquecido | null>(null)
  const [avaliacaoAberta, setAvaliacaoAberta] = useState<IngressoEnriquecido | null>(null)
  const [msg, setMsg] = useState(
    state?.anuncioPublicado ? 'Anúncio publicado com sucesso! 🎉' :
    state?.sucesso ? 'Compra realizada com sucesso! 🎉' : ''
  )

  useEffect(() => {
    if (!usuario) return
    Promise.all([
      ingressoService.meus(usuario.id),
      saldoService.obter(usuario.id),
      saldoService.transacoes(usuario.id),
      revendaService.meus(usuario.id).catch(() => ({ data: [] })),
    ]).then(async ([ingRes, saldoRes, transRes, anunciosRes]) => {
      setSaldo(saldoRes.data.valor)
      setTransacoes(transRes.data)

      const rawIngressos: IngressoRaw[] = ingRes.data
      const rawAnuncios: AnuncioRaw[] = anunciosRes.data

      const todosEventoIds = [
        ...new Set([
          ...rawIngressos.map(i => i.eventoId),
          ...rawAnuncios.map(a => a.eventoId),
        ]),
      ]

      const eventosMap = new Map<number, Evento>()
      const tiposMap = new Map<number, TipoIngresso[]>()

      await Promise.all(
        todosEventoIds.map(async (eventoId) => {
          const [evRes, tiposRes] = await Promise.all([
            eventoService.detalhe(eventoId),
            tipoIngressoService.listar(eventoId),
          ])
          eventosMap.set(eventoId, evRes.data)
          tiposMap.set(eventoId, tiposRes.data)
        })
      )

      const enriquecidos: IngressoEnriquecido[] = rawIngressos.map(ing => {
        const ev = eventosMap.get(ing.eventoId) ?? {
          id: ing.eventoId, nome: 'Evento', dataHora: '', local: '', status: 'ATIVO',
        }
        const tipos = tiposMap.get(ing.eventoId) ?? []
        const tipo = tipos.find(t => t.id === ing.tipoIngressoId) ?? {
          id: ing.tipoIngressoId, nome: 'Ingresso', preco: 0,
        }
        return { ingresso: ing, evento: ev, tipo }
      })

      const anunciosEnriquecidos: AnuncioEnriquecido[] = rawAnuncios.map(a => {
        const ev = eventosMap.get(a.eventoId) ?? {
          id: a.eventoId, nome: 'Evento', dataHora: '', local: '', status: 'ATIVO',
        }
        return { anuncio: a, evento: ev }
      })

      setIngressos(enriquecidos)
      setAnuncios(anunciosEnriquecidos)
    }).finally(() => setCarregando(false))
  }, [])

  const agora = new Date()
  const proximos = ingressos.filter(e =>
    e.evento.dataHora ? new Date(e.evento.dataHora) > agora : true
  )
  const encerrados = ingressos.filter(e =>
    e.evento.dataHora ? new Date(e.evento.dataHora) <= agora : false
  )
  const lista = abaAtiva === 'proximos' ? proximos : abaAtiva === 'encerrados' ? encerrados : []

  const soliciarReembolso = async (id: string) => {
    try {
      await ingressoService.reembolsar(id, usuario!.id)
      setMsg('Reembolso solicitado com sucesso!')
      setIngressos(prev => prev.filter(e => e.ingresso.id !== id))
      setIngressoAberto(null)
    } catch {
      setMsg('Erro ao solicitar reembolso.')
    }
  }

  const cancelarAnuncioRevenda = async (ingressoItem: IngressoEnriquecido) => {
    if (!confirm('Cancelar o anúncio? Seu ingresso voltará para a carteira.')) return
    try {
      const r = await revendaService.listar(ingressoItem.evento.id)
      const anuncio = r.data.find((a: { ingressoIds: string[]; vendedorId: number }) =>
        a.ingressoIds.includes(ingressoItem.ingresso.id) && a.vendedorId === usuario!.id
      )
      if (!anuncio) { setMsg('Anúncio não encontrado.'); return }
      await revendaService.cancelar(anuncio.id, usuario!.id)
      setMsg('Anúncio cancelado. Ingresso de volta na sua carteira!')
      setIngressoAberto(null)
      const ingRes = await ingressoService.meus(usuario!.id)
      const rawIngressos: IngressoRaw[] = ingRes.data
      const eventoIdsUnicos = [...new Set(rawIngressos.map(i => i.eventoId))]
      const eventosMap = new Map<number, Evento>()
      const tiposMap = new Map<number, TipoIngresso[]>()
      await Promise.all(eventoIdsUnicos.map(async (eid) => {
        const [evRes, tiposRes] = await Promise.all([eventoService.detalhe(eid), tipoIngressoService.listar(eid)])
        eventosMap.set(eid, evRes.data)
        tiposMap.set(eid, tiposRes.data)
      }))
      setIngressos(rawIngressos.map(ing => {
        const ev = eventosMap.get(ing.eventoId) ?? { id: ing.eventoId, nome: 'Evento', dataHora: '', local: '', status: 'ATIVO' }
        const tipos = tiposMap.get(ing.eventoId) ?? []
        const tipo = tipos.find(t => t.id === ing.tipoIngressoId) ?? { id: ing.tipoIngressoId, nome: 'Ingresso', preco: 0 }
        return { ingresso: ing, evento: ev, tipo }
      }))
    } catch {
      setMsg('Erro ao cancelar anúncio.')
    }
  }

  return (
    <>
      <Navbar />

      <div style={{ maxWidth: 1280, margin: '0 auto', padding: '32px 24px' }}>
        {msg && (
          <div style={{
            background: msg.includes('sucesso') || msg.includes('🎉') ? '#f0fdf4' : '#fef2f2',
            border: `1px solid ${msg.includes('sucesso') || msg.includes('🎉') ? '#bbf7d0' : '#fecaca'}`,
            borderRadius: 10,
            padding: '12px 16px',
            marginBottom: 24,
            color: msg.includes('sucesso') || msg.includes('🎉') ? '#15803d' : '#ef4444',
            fontSize: 14,
            fontWeight: 600,
          }}>
            {msg}
          </div>
        )}

        {/* Cabeçalho */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 28 }}>
          <h1 style={{ fontSize: 28, fontWeight: 800, color: '#1e293b' }}>Meus Ingressos</h1>
          <div style={{ display: 'flex', background: '#f1f5f9', borderRadius: 24, padding: 4 }}>
            {([
              { id: 'proximos', label: 'Próximos' },
              { id: 'encerrados', label: 'Encerrados' },
              { id: 'revendas', label: `Em Revenda${anuncios.length > 0 ? ` (${anuncios.length})` : ''}` },
            ] as const).map(aba => (
              <button
                key={aba.id}
                onClick={() => setAbaAtiva(aba.id)}
                style={{
                  padding: '8px 20px',
                  borderRadius: 20,
                  border: 'none',
                  background: abaAtiva === aba.id ? '#fff' : 'transparent',
                  color: abaAtiva === aba.id
                    ? aba.id === 'revendas' ? '#b45309' : '#1e293b'
                    : '#64748b',
                  fontWeight: abaAtiva === aba.id ? 700 : 400,
                  fontSize: 14,
                  boxShadow: abaAtiva === aba.id ? '0 1px 4px rgba(0,0,0,0.1)' : 'none',
                  cursor: 'pointer',
                  transition: 'all 0.15s',
                }}
              >
                {aba.label}
              </button>
            ))}
          </div>
        </div>

        {carregando ? (
          <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>Carregando ingressos...</div>
        ) : abaAtiva === 'revendas' ? (
          anuncios.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>🏷️</div>
              <p>Você não tem ingressos anunciados no momento.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16, marginBottom: 40 }}>
              {anuncios.map(a => (
                <CardAnuncio
                  key={a.anuncio.id}
                  item={a}
                  onCancelar={async () => {
                    if (!confirm('Cancelar o anúncio? Seu ingresso voltará para a carteira.')) return
                    try {
                      await revendaService.cancelar(a.anuncio.id, usuario!.id)
                      setMsg('Anúncio cancelado. Ingresso de volta na sua carteira!')
                      setAnuncios(prev => prev.filter(x => x.anuncio.id !== a.anuncio.id))
                    } catch {
                      setMsg('Erro ao cancelar anúncio.')
                    }
                  }}
                />
              ))}
            </div>
          )
        ) : lista.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>
            <div style={{ fontSize: 48, marginBottom: 16 }}>🎟️</div>
            <p>Nenhum ingresso {abaAtiva === 'proximos' ? 'próximo' : 'encerrado'} encontrado.</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 20, marginBottom: 40 }}>
            {lista.map(e => (
              <CardIngresso
                key={e.ingresso.id}
                item={e}
                onVerIngresso={() => setIngressoAberto(e)}
              />
            ))}
          </div>
        )}

        {/* Histórico de Compras */}
        <section>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b' }}>Histórico de Compras</h2>
            <Link to="/saldo" style={{ color: '#1d4ed8', fontSize: 14, fontWeight: 600 }}>Ver tudo</Link>
          </div>

          <div style={{ background: '#fff', borderRadius: 16, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', overflow: 'hidden' }}>
            {transacoes.length === 0 ? (
              <p style={{ textAlign: 'center', padding: '32px 0', color: '#94a3b8', fontSize: 14 }}>
                Nenhuma movimentação registrada.
              </p>
            ) : (
              transacoes.slice(0, 5).map((t, i) => (
                <div
                  key={t.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 16,
                    padding: '16px 20px',
                    borderBottom: i < Math.min(transacoes.length, 5) - 1 ? '1px solid #f1f5f9' : 'none',
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
                      {t.tipo === 'DEPOSITO' ? 'Recarga de Saldo' : 'Ingresso'}
                    </p>
                    <p style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
                      {t.data ? new Date(t.data).toLocaleDateString('pt-BR') : ''} • {TIPO_LABELS[t.tipo] ?? t.tipo}
                    </p>
                  </div>
                  <span style={{
                    fontWeight: 700,
                    fontSize: 15,
                    color: TIPO_POSITIVO.has(t.tipo) ? '#16a34a' : '#1e293b',
                  }}>
                    {TIPO_POSITIVO.has(t.tipo) ? '+' : ''}{formatMoeda(t.valor)}
                  </span>
                </div>
              ))
            )}
          </div>
        </section>

        {/* Saldo resumo */}
        {saldo !== null && (
          <div style={{ marginTop: 24, background: '#fff', borderRadius: 16, padding: '20px 24px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <p style={{ fontSize: 13, color: '#64748b', marginBottom: 4 }}>Saldo disponível na Carteira</p>
              <p style={{ fontSize: 26, fontWeight: 800, color: '#1d4ed8' }}>{formatMoeda(saldo)}</p>
            </div>
            <Link to="/saldo">
              <button style={{ background: '#eff6ff', color: '#1d4ed8', border: 'none', borderRadius: 10, padding: '10px 20px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
                Gerenciar Carteira
              </button>
            </Link>
          </div>
        )}
      </div>

      {/* Modal Avaliação */}
      {avaliacaoAberta && (
        <ModalAvaliacao
          item={avaliacaoAberta}
          usuarioId={usuario!.id}
          onFechar={() => setAvaliacaoAberta(null)}
          onSucesso={() => { setAvaliacaoAberta(null); setMsg('Avaliação enviada com sucesso! Obrigado pelo feedback.') }}
        />
      )}

      {/* Modal Ver Ingresso */}
      {ingressoAberto && (
        <div
          style={{
            position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 200, padding: 24,
          }}
          onClick={() => setIngressoAberto(null)}
        >
          <div
            style={{ background: '#fff', borderRadius: 20, padding: 32, maxWidth: 420, width: '100%', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }}
            onClick={e => e.stopPropagation()}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
              <h3 style={{ fontSize: 18, fontWeight: 700, color: '#1e293b' }}>Seu Ingresso</h3>
              <button onClick={() => setIngressoAberto(null)} style={{ background: 'none', border: 'none', fontSize: 20, color: '#94a3b8', cursor: 'pointer' }}>✕</button>
            </div>

            <div style={{ background: '#f8fafc', borderRadius: 12, padding: '16px 20px', marginBottom: 20 }}>
              <p style={{ fontWeight: 700, fontSize: 16, color: '#1e293b', marginBottom: 4 }}>{ingressoAberto.evento.nome}</p>
              <p style={{ fontSize: 13, color: '#64748b' }}>📍 {ingressoAberto.evento.local}</p>
              {ingressoAberto.evento.dataHora && (
                <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
                  🕐 {new Date(ingressoAberto.evento.dataHora).toLocaleString('pt-BR')}
                </p>
              )}
            </div>

            <div style={{ background: '#1e293b', borderRadius: 12, padding: 20, textAlign: 'center', marginBottom: 20 }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 2, maxWidth: 140, margin: '0 auto' }}>
                {Array.from({ length: 49 }).map((_, i) => (
                  <div key={i} style={{ background: (i + Math.floor(i / 7)) % 2 === 0 ? '#fff' : '#1e293b', aspectRatio: '1', borderRadius: 1 }} />
                ))}
              </div>
              <p style={{ color: '#94a3b8', fontSize: 11, marginTop: 12, fontFamily: 'monospace' }}>
                {ingressoAberto.ingresso.id.substring(0, 8).toUpperCase()}
              </p>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 12 }}>
              <span style={{ color: '#64748b' }}>Tipo</span>
              <span style={{ fontWeight: 600, color: '#1e293b' }}>{ingressoAberto.tipo.nome}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 20 }}>
              <span style={{ color: '#64748b' }}>Status</span>
              <span style={{
                fontWeight: 700,
                color: ingressoAberto.ingresso.status === 'ATIVO' ? '#16a34a'
                  : ingressoAberto.ingresso.status === 'EM_REVENDA' ? '#f59e0b'
                  : ingressoAberto.ingresso.status === 'REVENDIDO' ? '#7c3aed'
                  : '#94a3b8',
              }}>
                {ingressoAberto.ingresso.status === 'ATIVO' ? 'Ativo'
                  : ingressoAberto.ingresso.status === 'EM_REVENDA' ? 'Em Revenda'
                  : ingressoAberto.ingresso.status === 'REVENDIDO' ? 'Revendido'
                  : ingressoAberto.ingresso.status}
              </span>
            </div>

            {ingressoAberto.ingresso.status === 'ATIVO' && !ingressoAberto.ingresso.bloqueadoPorReembolso && (
              <div style={{ display: 'flex', gap: 10 }}>
                <Link to={`/revender/${ingressoAberto.ingresso.id}`} style={{ flex: 1 }}>
                  <button style={{ width: '100%', background: '#eff6ff', color: '#1d4ed8', border: 'none', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
                    Revender
                  </button>
                </Link>
                <button
                  onClick={() => soliciarReembolso(ingressoAberto.ingresso.id)}
                  style={{ flex: 1, background: '#fef2f2', color: '#ef4444', border: 'none', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}
                >
                  Reembolsar
                </button>
              </div>
            )}

            {/* Botão Avaliar — disponível após o evento */}
            {ingressoAberto.evento.dataHora && new Date(ingressoAberto.evento.dataHora) < new Date() && ingressoAberto.evento.status !== 'CANCELADO' && (
              <div style={{ marginTop: 10 }}>
                <button
                  onClick={() => { setIngressoAberto(null); setAvaliacaoAberta(ingressoAberto) }}
                  style={{ width: '100%', background: '#fef9c3', color: '#854d0e', border: '1px solid #fde047', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}
                >
                  ⭐ Avaliar Evento
                </button>
              </div>
            )}

            {ingressoAberto.ingresso.status === 'EM_REVENDA' && (
              <div>
                <div style={{ background: '#fffbeb', border: '1px solid #fde68a', borderRadius: 10, padding: '10px 14px', marginBottom: 10 }}>
                  <p style={{ fontSize: 13, color: '#92400e', fontWeight: 600 }}>
                    Este ingresso está anunciado no marketplace.
                  </p>
                  <p style={{ fontSize: 12, color: '#78716c', marginTop: 4 }}>
                    Aguardando um comprador. Você pode cancelar o anúncio a qualquer momento.
                  </p>
                </div>
                <div style={{ display: 'flex', gap: 10 }}>
                  <Link to={`/revendas?eventoId=${ingressoAberto.evento.id}`} style={{ flex: 1 }}>
                    <button style={{ width: '100%', background: '#fffbeb', color: '#b45309', border: '1px solid #fde68a', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
                      Ver Anúncio
                    </button>
                  </Link>
                  <button
                    onClick={() => cancelarAnuncioRevenda(ingressoAberto)}
                    style={{ flex: 1, background: '#fef2f2', color: '#ef4444', border: 'none', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}
                  >
                    Cancelar Anúncio
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </>
  )
}

function CardAnuncio({ item, onCancelar }: { item: AnuncioEnriquecido; onCancelar: () => void }) {
  const { dia, mes } = item.evento.dataHora
    ? formatDataBadge(item.evento.dataHora)
    : { dia: '--', mes: '---' }

  const imagemEvento = item.evento.imagemCapaUrl ||
    'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=400&q=60'

  const reservado = item.anuncio.status === 'RESERVADO'

  return (
    <div style={{
      background: '#fff',
      borderRadius: 16,
      overflow: 'hidden',
      boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
      display: 'flex',
      border: reservado ? '1.5px solid #fde68a' : '1.5px solid transparent',
    }}>
      {/* Imagem */}
      <div style={{ position: 'relative', width: 140, flexShrink: 0 }}>
        <img src={imagemEvento} alt={item.evento.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        <div style={{
          position: 'absolute', top: 10, left: 10,
          background: 'rgba(0,0,0,0.7)', color: '#fff',
          borderRadius: 6, padding: '3px 8px', textAlign: 'center', minWidth: 36,
        }}>
          <div style={{ fontSize: 16, fontWeight: 800, lineHeight: 1 }}>{dia}</div>
          <div style={{ fontSize: 9, fontWeight: 600, letterSpacing: 0.5 }}>{mes}</div>
        </div>
      </div>

      {/* Conteúdo */}
      <div style={{ flex: 1, padding: '14px 16px', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
          <span style={{
            background: reservado ? '#fffbeb' : '#f0fdf4',
            color: reservado ? '#b45309' : '#16a34a',
            fontSize: 11, fontWeight: 700,
            padding: '3px 10px', borderRadius: 20,
            textTransform: 'uppercase', letterSpacing: 0.5,
          }}>
            {reservado ? 'Reservado' : 'Disponível'}
          </span>
          <span style={{ fontSize: 18, fontWeight: 800, color: '#1d4ed8' }}>
            {formatMoeda(item.anuncio.preco)}
          </span>
        </div>

        <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b', marginBottom: 4, lineHeight: 1.3 }}>
          {item.evento.nome}
        </h3>
        <p style={{ fontSize: 12, color: '#64748b', marginBottom: 4 }}>📍 {item.evento.local}</p>
        <p style={{ fontSize: 12, color: '#64748b', marginBottom: 8 }}>
          🎟️ {item.anuncio.quantidade} ingresso{item.anuncio.quantidade !== 1 ? 's' : ''}
        </p>

        {reservado && (
          <div style={{
            background: '#fffbeb', border: '1px solid #fde68a',
            borderRadius: 8, padding: '6px 10px', marginBottom: 10,
            fontSize: 12, color: '#92400e',
          }}>
            Reservado por um comprador — aguardando confirmação de pagamento.
          </div>
        )}

        <div style={{ marginTop: 'auto', display: 'flex', gap: 10 }}>
          <Link to={`/revendas?eventoId=${item.evento.id}`} style={{ flex: 1 }}>
            <button style={{
              width: '100%', background: '#fffbeb', color: '#b45309',
              border: '1px solid #fde68a', borderRadius: 10,
              padding: '8px', fontSize: 13, fontWeight: 600, cursor: 'pointer',
            }}>
              Ver Anúncio
            </button>
          </Link>
          {!reservado && (
            <button
              onClick={onCancelar}
              style={{
                flex: 1, background: '#fef2f2', color: '#ef4444',
                border: 'none', borderRadius: 10,
                padding: '8px', fontSize: 13, fontWeight: 600, cursor: 'pointer',
              }}
            >
              Cancelar
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

function CardIngresso({ item, onVerIngresso }: { item: IngressoEnriquecido; onVerIngresso: () => void }) {
  const { dia, mes } = item.evento.dataHora
    ? formatDataBadge(item.evento.dataHora)
    : { dia: '--', mes: '---' }

  const imagemEvento = item.evento.imagemCapaUrl ||
    'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=400&q=60'

  const COR_TIPO: Record<string, string> = {
    'VIP': '#f59e0b',
    'Pista': '#1d4ed8',
    'Plateia': '#7c3aed',
    'Geral': '#16a34a',
  }
  const corTipo = Object.entries(COR_TIPO).find(([k]) =>
    item.tipo.nome.toLowerCase().includes(k.toLowerCase())
  )?.[1] ?? '#1d4ed8'

  return (
    <div style={{ background: '#fff', borderRadius: 16, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', display: 'flex' }}>
      {/* Imagem */}
      <div style={{ position: 'relative', width: 140, flexShrink: 0 }}>
        <img src={imagemEvento} alt={item.evento.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        <div style={{
          position: 'absolute',
          top: 10, left: 10,
          background: 'rgba(0,0,0,0.7)',
          color: '#fff',
          borderRadius: 6,
          padding: '3px 8px',
          textAlign: 'center',
          minWidth: 36,
        }}>
          <div style={{ fontSize: 16, fontWeight: 800, lineHeight: 1 }}>{dia}</div>
          <div style={{ fontSize: 9, fontWeight: 600, letterSpacing: 0.5 }}>{mes}</div>
        </div>
      </div>

      {/* Conteúdo */}
      <div style={{ flex: 1, padding: '14px 16px', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
          <div style={{ display: 'flex', gap: 6, alignItems: 'center', flexWrap: 'wrap' }}>
            <span style={{
              background: corTipo + '20',
              color: corTipo,
              fontSize: 11,
              fontWeight: 700,
              padding: '3px 10px',
              borderRadius: 20,
              textTransform: 'uppercase',
              letterSpacing: 0.5,
            }}>
              {item.tipo.nome}
            </span>
            {item.ingresso.status === 'EM_REVENDA' && (
              <span style={{
                background: '#fffbeb', color: '#b45309',
                fontSize: 10, fontWeight: 700,
                padding: '2px 8px', borderRadius: 20,
                border: '1px solid #fde68a',
              }}>
                Em Revenda
              </span>
            )}
          </div>
          <span style={{ fontSize: 18, color: '#94a3b8', cursor: 'pointer' }} title="QR Code">⊞</span>
        </div>

        <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b', marginBottom: 6, lineHeight: 1.3 }}>
          {item.evento.nome}
        </h3>

        <p style={{ fontSize: 12, color: '#64748b', marginBottom: 4 }}>📍 {item.evento.local}</p>

        {item.evento.dataHora && (
          <p style={{ fontSize: 12, color: '#64748b', marginBottom: 12 }}>
            🕐 {new Date(item.evento.dataHora).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
            {item.evento.aberturaPortoes && ` • Portões ${new Date(item.evento.aberturaPortoes).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}`}
          </p>
        )}

        <div style={{ marginTop: 'auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div style={{
            width: 28, height: 28, borderRadius: '50%',
            background: '#1d4ed8',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            color: '#fff', fontSize: 12, fontWeight: 700,
          }}>
            {(item.ingresso.proprietarioId ?? 1).toString().charAt(0).toUpperCase()}
          </div>
          <button
            onClick={onVerIngresso}
            style={{
              background: '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: 20,
              padding: '7px 18px',
              fontSize: 13,
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Ver Ingresso
          </button>
        </div>
      </div>
    </div>
  )
}

// ─── Modal de Avaliação ───────────────────────────────────────────────────────

function ModalAvaliacao({ item, usuarioId, onFechar, onSucesso }: {
  item: IngressoEnriquecido
  usuarioId: number
  onFechar: () => void
  onSucesso: () => void
}) {
  const [nota, setNota] = useState(0)
  const [hover, setHover] = useState(0)
  const [comentario, setComentario] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  const enviar = async () => {
    if (nota === 0) { setErro('Selecione uma nota de 1 a 5 estrelas.'); return }
    setEnviando(true)
    setErro('')
    try {
      await avaliacaoService.avaliar(item.evento.id, usuarioId, { nota, comentario: comentario.trim() || null })
      onSucesso()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      setErro(err.response?.data?.message ?? 'Erro ao enviar avaliação. Tente novamente.')
      setEnviando(false)
    }
  }

  return (
    <div
      style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 300, padding: 24 }}
      onClick={e => { if (e.target === e.currentTarget) onFechar() }}
    >
      <div style={{ background: '#fff', borderRadius: 20, padding: 32, maxWidth: 440, width: '100%', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 style={{ fontSize: 18, fontWeight: 700, color: '#1e293b' }}>Avaliar Evento</h3>
          <button onClick={onFechar} style={{ background: 'none', border: 'none', fontSize: 20, color: '#94a3b8', cursor: 'pointer' }}>✕</button>
        </div>

        <div style={{ background: '#f8fafc', borderRadius: 12, padding: '12px 16px', marginBottom: 20 }}>
          <p style={{ fontWeight: 700, fontSize: 15, color: '#1e293b' }}>{item.evento.nome}</p>
          <p style={{ fontSize: 12, color: '#64748b', marginTop: 2 }}>
            {item.evento.dataHora ? new Date(item.evento.dataHora).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' }) : ''} · {item.evento.local}
          </p>
        </div>

        {/* Seletor de estrelas */}
        <div style={{ marginBottom: 20 }}>
          <p style={{ fontSize: 13, fontWeight: 600, color: '#64748b', marginBottom: 10 }}>
            Sua nota <span style={{ color: '#ef4444' }}>*</span>
          </p>
          <div style={{ display: 'flex', gap: 8 }}>
            {[1, 2, 3, 4, 5].map(i => (
              <button
                key={i}
                onClick={() => setNota(i)}
                onMouseEnter={() => setHover(i)}
                onMouseLeave={() => setHover(0)}
                style={{
                  background: 'none', border: 'none', cursor: 'pointer',
                  fontSize: 36, lineHeight: 1,
                  color: i <= (hover || nota) ? '#f59e0b' : '#e2e8f0',
                  transition: 'color 0.1s',
                }}
              >
                ★
              </button>
            ))}
          </div>
          {nota > 0 && (
            <p style={{ fontSize: 12, color: '#94a3b8', marginTop: 6 }}>
              {['', 'Muito ruim', 'Ruim', 'Regular', 'Bom', 'Excelente'][nota]}
            </p>
          )}
        </div>

        {/* Comentário */}
        <div style={{ marginBottom: 20 }}>
          <label style={{ display: 'block', fontSize: 13, fontWeight: 600, color: '#64748b', marginBottom: 8 }}>
            Comentário <span style={{ fontWeight: 400, color: '#94a3b8' }}>(opcional)</span>
          </label>
          <textarea
            value={comentario}
            onChange={e => setComentario(e.target.value)}
            placeholder="Conte como foi sua experiência no evento..."
            rows={3}
            style={{ width: '100%', padding: '10px 12px', border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14, color: '#1e293b', resize: 'vertical', outline: 'none', boxSizing: 'border-box' }}
          />
        </div>

        {erro && (
          <p style={{ fontSize: 13, color: '#ef4444', marginBottom: 14, fontWeight: 600 }}>{erro}</p>
        )}

        <div style={{ display: 'flex', gap: 10 }}>
          <button onClick={onFechar} style={{ flex: 1, background: '#f8fafc', color: '#64748b', border: '1px solid #e2e8f0', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}>
            Cancelar
          </button>
          <button
            onClick={enviar}
            disabled={enviando || nota === 0}
            style={{ flex: 2, background: nota > 0 ? '#1d4ed8' : '#e2e8f0', color: nota > 0 ? '#fff' : '#94a3b8', border: 'none', borderRadius: 10, padding: '12px', fontSize: 14, fontWeight: 700, cursor: nota > 0 ? 'pointer' : 'default' }}
          >
            {enviando ? 'Enviando...' : 'Enviar Avaliação'}
          </button>
        </div>
      </div>
    </div>
  )
}
