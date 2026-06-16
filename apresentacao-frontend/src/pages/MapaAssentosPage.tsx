import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Navbar from '../components/Navbar'
import SeletorEvento from '../components/SeletorEvento'
import { api, filaEsperaService, mapaAssentosService } from '../services/api'

interface Assento {
  id: number
  secao: string
  codigo: string
  tipo: 'NORMAL' | 'VIP' | 'ACESSIBILIDADE' | 'BLOQUEADO'
  preco: number
  status: 'DISPONIVEL' | 'RESERVADO' | 'VENDIDO' | 'BLOQUEADO'
  reservadoPor?: number
  reservadoAte?: string
}

interface MapaAssentos {
  id: number
  eventoId: number
  totalLinhas: number
  totalColunas: number
  assentos: Assento[]
}

interface FilaStatus {
  naFila: boolean
  posicao?: number
  tamanhoFila: number
}

type Etapa = 'selecao' | 'reservado' | 'sucesso'

const MINUTOS_RESERVA = 5

const corStatus: Record<string, string> = {
  DISPONIVEL: '#22c55e',
  RESERVADO: '#f59e0b',
  VENDIDO: 'var(--ink-2)',
  BLOQUEADO: '#6b7280',
}

const corTipo: Record<string, string> = {
  NORMAL: 'var(--brand)',
  VIP: '#a855f7',
  ACESSIBILIDADE: '#06b6d4',
  BLOQUEADO: '#6b7280',
}

const formatMoeda = (v: number) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v)

const formatTempo = (s: number) => {
  const m = Math.floor(s / 60).toString().padStart(2, '0')
  const seg = (s % 60).toString().padStart(2, '0')
  return `${m}:${seg}`
}

export default function MapaAssentosPage() {
  const [searchParams] = useSearchParams()
  const eventoId = searchParams.get('eventoId')
  const { usuario } = useAuth()
  const navigate = useNavigate()

  const [mapa, setMapa] = useState<MapaAssentos | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [selecionados, setSelecionados] = useState<number[]>([])
  const [mensagem, setMensagem] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)
  const [etapa, setEtapa] = useState<Etapa>('selecao')
  const [assentosReservados, setAssentosReservados] = useState<Assento[]>([])
  const [tempoRestante, setTempoRestante] = useState(MINUTOS_RESERVA * 60)
  const [comprando, setComprando] = useState(false)
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null)

  // Organizer block mode
  const [modoOrganizador, setModoOrganizador] = useState(false)
  const [paraBloquear, setParaBloquear] = useState<number[]>([])
  const [paraDesbloquear, setParaDesbloquear] = useState<number[]>([])
  const [aplicandoBloqueio, setAplicandoBloqueio] = useState(false)

  // Fila de espera
  const [filaStatus, setFilaStatus] = useState<FilaStatus | null>(null)
  const [processandoFila, setProcessandoFila] = useState(false)

  // Form criar mapa (organizador)
  const [criando, setCriando] = useState(false)
  const [form, setForm] = useState({
    totalLinhas: '5',
    totalColunas: '10',
    precoNormal: '100.00',
    precoVip: '150.00',
  })

  const ehOrganizador = usuario?.papeis?.includes('ORGANIZADOR')

  const carregarMapa = useCallback(() => {
    if (!eventoId) return
    api.get('/mapas-assentos', { params: { eventoId } })
      .then(r => setMapa(r.data))
      .catch(() => setMapa(null))
  }, [eventoId])

  const carregarFila = useCallback((mapaId: number) => {
    if (!usuario) return
    filaEsperaService.consultarPosicao(mapaId, usuario.id)
      .then(r => setFilaStatus(r.data))
      .catch(() => {})
  }, [usuario])

  // Carga inicial
  useEffect(() => {
    if (!eventoId) return
    setCarregando(true)
    api.get('/mapas-assentos', { params: { eventoId } })
      .then(r => {
        setMapa(r.data)
        if (!ehOrganizador) carregarFila(r.data.id)
      })
      .catch(() => setMapa(null))
      .finally(() => setCarregando(false))
  }, [eventoId])

  // Polling a cada 30s para atualizar estado dos assentos
  useEffect(() => {
    if (etapa !== 'selecao' || !eventoId) return
    pollRef.current = setInterval(carregarMapa, 30_000)
    return () => { if (pollRef.current) clearInterval(pollRef.current) }
  }, [etapa, eventoId, carregarMapa])

  // Countdown timer quando em etapa 'reservado'
  useEffect(() => {
    if (etapa !== 'reservado') return
    setTempoRestante(MINUTOS_RESERVA * 60)
    timerRef.current = setInterval(() => {
      setTempoRestante(prev => {
        if (prev <= 1) {
          clearInterval(timerRef.current!)
          exibirMensagem('Reserva expirada! Selecione os assentos novamente.', 'erro')
          setEtapa('selecao')
          setSelecionados([])
          setAssentosReservados([])
          carregarMapa()
          return 0
        }
        return prev - 1
      })
    }, 1000)
    return () => { if (timerRef.current) clearInterval(timerRef.current) }
  }, [etapa])

  const exibirMensagem = (texto: string, tipo: 'ok' | 'erro') => {
    setMensagem({ texto, tipo })
    setTimeout(() => setMensagem(null), 5000)
  }

  const temDisponivel = mapa?.assentos.some(a => a.status === 'DISPONIVEL') ?? false

  // ─── Compra normal ───────────────────────────────────────────────────
  const toggleSelecionado = (assento: Assento) => {
    if (modoOrganizador) {
      if (assento.status === 'DISPONIVEL') {
        setParaBloquear(prev =>
          prev.includes(assento.id) ? prev.filter(id => id !== assento.id) : [...prev, assento.id]
        )
      } else if (assento.status === 'BLOQUEADO') {
        setParaDesbloquear(prev =>
          prev.includes(assento.id) ? prev.filter(id => id !== assento.id) : [...prev, assento.id]
        )
      }
      return
    }
    if (assento.status !== 'DISPONIVEL') return
    if (selecionados.length >= 6 && !selecionados.includes(assento.id)) {
      exibirMensagem('Limite de 6 assentos por compra.', 'erro')
      return
    }
    setSelecionados(prev =>
      prev.includes(assento.id)
        ? prev.filter(id => id !== assento.id)
        : [...prev, assento.id]
    )
  }

  const reservar = async () => {
    if (!mapa || selecionados.length === 0) return
    try {
      await mapaAssentosService.reservar(mapa.id, usuario!.id, selecionados)
      const resp = await api.get('/mapas-assentos', { params: { eventoId } })
      setMapa(resp.data)
      const todosAssentos: Assento[] = resp.data.assentos
      const reservados = todosAssentos.filter(
        a => selecionados.includes(a.id) && a.status === 'RESERVADO'
      )
      setAssentosReservados(reservados)
      setEtapa('reservado')
    } catch (err: any) {
      exibirMensagem(err.response?.data?.motivo ?? 'Erro ao reservar assentos.', 'erro')
    }
  }

  const comprar = async () => {
    if (!mapa || assentosReservados.length === 0) return
    setComprando(true)
    try {
      await mapaAssentosService.comprar(mapa.id, usuario!.id, assentosReservados.map(a => a.id))
      if (timerRef.current) clearInterval(timerRef.current)
      setEtapa('sucesso')
      carregarMapa()
    } catch (err: any) {
      exibirMensagem(err.response?.data?.motivo ?? 'Erro ao finalizar compra.', 'erro')
    } finally {
      setComprando(false)
    }
  }

  const cancelarReserva = () => {
    if (timerRef.current) clearInterval(timerRef.current)
    setSelecionados([])
    setAssentosReservados([])
    setEtapa('selecao')
    carregarMapa()
  }

  // ─── Bloqueio de assentos (organizador) ─────────────────────────────
  const aplicarBloqueio = async () => {
    if (!mapa) return
    setAplicandoBloqueio(true)
    try {
      if (paraBloquear.length > 0) {
        await mapaAssentosService.bloquear(mapa.id, usuario!.id, paraBloquear)
      }
      if (paraDesbloquear.length > 0) {
        await mapaAssentosService.desbloquear(mapa.id, usuario!.id, paraDesbloquear)
      }
      const bloqueados = paraBloquear.length
      const desbloqueados = paraDesbloquear.length
      setParaBloquear([])
      setParaDesbloquear([])
      carregarMapa()
      const partes = []
      if (bloqueados > 0) partes.push(`${bloqueados} bloqueado(s)`)
      if (desbloqueados > 0) partes.push(`${desbloqueados} desbloqueado(s)`)
      exibirMensagem(partes.join(', ') + '.', 'ok')
    } catch (err: any) {
      exibirMensagem(err.response?.data?.motivo ?? 'Erro ao aplicar bloqueio.', 'erro')
    } finally {
      setAplicandoBloqueio(false)
    }
  }

  const cancelarModoOrganizador = () => {
    setModoOrganizador(false)
    setParaBloquear([])
    setParaDesbloquear([])
  }

  // ─── Fila de espera ──────────────────────────────────────────────────
  const entrarFila = async () => {
    if (!mapa || !usuario) return
    setProcessandoFila(true)
    try {
      const r = await filaEsperaService.entrar(mapa.id, usuario.id)
      setFilaStatus({ naFila: true, posicao: r.data.posicao, tamanhoFila: r.data.posicao })
      exibirMensagem(r.data.mensagem, 'ok')
    } catch (err: any) {
      exibirMensagem(err.response?.data?.motivo ?? 'Erro ao entrar na fila.', 'erro')
    } finally {
      setProcessandoFila(false)
    }
  }

  const sairFila = async () => {
    if (!mapa || !usuario) return
    setProcessandoFila(true)
    try {
      await filaEsperaService.sair(mapa.id, usuario.id)
      setFilaStatus(prev => ({ naFila: false, tamanhoFila: Math.max(0, (prev?.tamanhoFila ?? 1) - 1) }))
      exibirMensagem('Você saiu da fila de espera.', 'ok')
    } catch (err: any) {
      exibirMensagem(err.response?.data?.motivo ?? 'Erro ao sair da fila.', 'erro')
    } finally {
      setProcessandoFila(false)
    }
  }

  // ─── Criar mapa ──────────────────────────────────────────────────────
  const criarMapa = (e: React.FormEvent) => {
    e.preventDefault()
    if (!eventoId) return
    api.post('/mapas-assentos', {
      eventoId: Number(eventoId),
      totalLinhas: Number(form.totalLinhas),
      totalColunas: Number(form.totalColunas),
      precoNormal: Number(form.precoNormal),
      precoVip: Number(form.precoVip),
    }, { headers: { 'X-Usuario-Id': usuario?.id } })
      .then(r => {
        setMapa(r.data)
        setCriando(false)
        exibirMensagem('Mapa de assentos criado!', 'ok')
      })
      .catch(err => {
        exibirMensagem(err.response?.data?.motivo ?? 'Erro ao criar mapa.', 'erro')
      })
  }

  const agruparPorFileira = (assentos: Assento[]) => {
    const fileiras = new Map<string, Assento[]>()
    assentos.forEach(a => {
      const fileira = a.codigo.charAt(0)
      if (!fileiras.has(fileira)) fileiras.set(fileira, [])
      fileiras.get(fileira)!.push(a)
    })
    return fileiras
  }

  const totalSelecionado = selecionados.reduce((acc, id) => {
    const a = mapa?.assentos.find(a => a.id === id)
    return acc + (a?.preco ?? 0)
  }, 0)

  const totalReservado = assentosReservados.reduce((acc, a) => acc + a.preco, 0)
  const corContagem = tempoRestante < 60 ? '#dc2626' : tempoRestante < 120 ? '#f59e0b' : '#16a34a'

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '32px 16px' }}>

        {/* Indicador de etapas */}
        {etapa !== 'selecao' && (
          <div style={{ display: 'flex', gap: 8, marginBottom: 24, alignItems: 'center' }}>
            {(['selecao', 'reservado', 'sucesso'] as Etapa[]).map((e, i) => {
              const labels = ['1. Selecionar', '2. Reservar', '3. Confirmar']
              const ativo = etapa === e
              const concluido = (['selecao', 'reservado', 'sucesso'] as Etapa[]).indexOf(etapa) > i
              return (
                <div key={e} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span style={{
                    padding: '4px 14px', borderRadius: 20, fontSize: 13, fontWeight: 600,
                    background: ativo ? 'var(--brand-strong)' : concluido ? '#16a34a' : 'var(--surface-2)',
                    color: ativo || concluido ? '#fff' : 'var(--ink-2)',
                    border: '1.5px solid ' + (ativo ? 'var(--brand-strong)' : concluido ? '#16a34a' : 'var(--border)'),
                  }}>
                    {concluido ? '✓ ' : ''}{labels[i]}
                  </span>
                  {i < 2 && <span style={{ color: 'var(--ink-muted)' }}>›</span>}
                </div>
              )
            })}
          </div>
        )}

        {/* Cabeçalho */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 }}>
          <div>
            <h1 style={{ fontSize: 28, fontWeight: 700, color: 'var(--ink)', margin: 0 }}>
              {etapa === 'sucesso' ? 'Compra Realizada!' : 'Mapa de Assentos'}
            </h1>
            <p style={{ color: 'var(--ink-2)', marginTop: 4 }}>
              {etapa === 'selecao' && !modoOrganizador && 'Selecione seus assentos e reserve por 5 minutos'}
              {etapa === 'selecao' && modoOrganizador && 'Modo organizador: clique para selecionar assentos a bloquear/desbloquear'}
              {etapa === 'reservado' && 'Seus assentos estão reservados. Confirme a compra antes do tempo esgotar!'}
              {etapa === 'sucesso' && 'Seus assentos foram confirmados e o saldo foi debitado.'}
            </p>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            {ehOrganizador && mapa && etapa === 'selecao' && (
              <button
                onClick={() => modoOrganizador ? cancelarModoOrganizador() : setModoOrganizador(true)}
                style={btnStyle(modoOrganizador ? '#dc2626' : '#7c3aed')}>
                {modoOrganizador ? 'Cancelar Modo' : 'Modo Bloqueio'}
              </button>
            )}
            {ehOrganizador && eventoId && !mapa && !criando && (
              <button onClick={() => setCriando(true)} style={btnStyle('var(--brand-strong)')}>
                + Criar Mapa
              </button>
            )}
          </div>
        </div>

        {/* Mensagem */}
        {mensagem && (
          <div style={{
            background: mensagem.tipo === 'ok' ? '#dcfce7' : '#fee2e2',
            color: mensagem.tipo === 'ok' ? '#166534' : '#991b1b',
            borderRadius: 8, padding: '12px 16px', marginBottom: 20, fontWeight: 500
          }}>
            {mensagem.texto}
          </div>
        )}

        {/* ─── ETAPA SUCESSO ─── */}
        {etapa === 'sucesso' && (
          <div style={{
            textAlign: 'center', padding: '60px 20px', background: '#f0fdf4',
            borderRadius: 16, border: '2px solid #86efac'
          }}>
            <p style={{ fontSize: 64, margin: '0 0 16px' }}>🎟️</p>
            <h2 style={{ color: '#166534', fontSize: 24, fontWeight: 700 }}>
              {assentosReservados.length} assento(s) confirmado(s)!
            </h2>
            <p style={{ color: '#15803d', fontSize: 16, marginBottom: 24 }}>
              Total debitado: {formatMoeda(totalReservado)}
            </p>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'center', flexWrap: 'wrap' }}>
              {assentosReservados.map(a => (
                <span key={a.id} style={{
                  background: '#fff', border: '1.5px solid #86efac', borderRadius: 8,
                  padding: '6px 14px', fontWeight: 600, color: '#166534'
                }}>
                  {a.codigo} — {a.tipo} — {formatMoeda(a.preco)}
                </span>
              ))}
            </div>
            <div style={{ display: 'flex', gap: 12, marginTop: 32, justifyContent: 'center' }}>
              <button onClick={() => navigate('/meus-ingressos')} style={btnStyle('#16a34a')}>
                Ver Meus Ingressos
              </button>
              <button onClick={() => { setEtapa('selecao'); carregarMapa() }} style={btnStyle('var(--brand-strong)')}>
                Continuar Comprando
              </button>
            </div>
          </div>
        )}

        {/* ─── ETAPA RESERVADO ─── */}
        {etapa === 'reservado' && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 360px', gap: 24, alignItems: 'start' }}>
            <div>
              <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 16 }}>Seus Assentos Reservados</h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                {assentosReservados.map(a => (
                  <div key={a.id} style={{
                    background: 'var(--surface-2)', borderRadius: 10, padding: '14px 18px',
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    border: '1.5px solid var(--border)'
                  }}>
                    <div>
                      <span style={{ fontWeight: 700, fontSize: 18, color: 'var(--ink)' }}>{a.codigo}</span>
                      <span style={{
                        marginLeft: 10, fontSize: 12, padding: '2px 8px', borderRadius: 20,
                        background: a.tipo === 'VIP' ? '#f3e8ff' : a.tipo === 'ACESSIBILIDADE' ? '#e0f2fe' : '#f0fdf4',
                        color: a.tipo === 'VIP' ? '#7c3aed' : a.tipo === 'ACESSIBILIDADE' ? '#0369a1' : '#166534',
                        fontWeight: 600
                      }}>
                        {a.tipo}
                      </span>
                    </div>
                    <span style={{ fontWeight: 700, fontSize: 18, color: 'var(--brand-strong)' }}>
                      {formatMoeda(a.preco)}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            <div style={{
              background: 'var(--surface-2)', border: '1.5px solid var(--border)',
              borderRadius: 16, padding: 24, position: 'sticky', top: 24
            }}>
              <div style={{ textAlign: 'center', marginBottom: 20 }}>
                <p style={{ color: 'var(--ink-2)', fontSize: 13, margin: '0 0 6px' }}>
                  Reserva expira em
                </p>
                <span style={{
                  fontSize: 48, fontWeight: 800, color: corContagem,
                  fontVariantNumeric: 'tabular-nums', letterSpacing: 2
                }}>
                  {formatTempo(tempoRestante)}
                </span>
                <div style={{
                  height: 6, background: 'var(--border)', borderRadius: 3, marginTop: 8, overflow: 'hidden'
                }}>
                  <div style={{
                    height: '100%', background: corContagem, borderRadius: 3,
                    width: `${(tempoRestante / (MINUTOS_RESERVA * 60)) * 100}%`,
                    transition: 'width 1s linear, background 0.5s'
                  }} />
                </div>
              </div>

              <div style={{ borderTop: '1px solid var(--border)', paddingTop: 16, marginBottom: 16 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                  <span style={{ color: 'var(--ink-2)' }}>{assentosReservados.length} assento(s)</span>
                  <span style={{ fontWeight: 600 }}>{formatMoeda(totalReservado)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: 18 }}>
                  <span>Total</span>
                  <span style={{ color: 'var(--brand-strong)' }}>{formatMoeda(totalReservado)}</span>
                </div>
              </div>

              <p style={{ fontSize: 12, color: 'var(--ink-2)', marginBottom: 16, textAlign: 'center' }}>
                O valor será debitado do seu saldo na carteira.
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                <button
                  onClick={comprar}
                  disabled={comprando}
                  style={{
                    ...btnStyle('#16a34a'), width: '100%', padding: '14px',
                    fontSize: 16, opacity: comprando ? 0.7 : 1
                  }}>
                  {comprando ? 'Processando...' : 'Confirmar Compra'}
                </button>
                <button onClick={cancelarReserva} style={{
                  background: 'none', color: 'var(--ink-2)', border: '1px solid var(--border)',
                  borderRadius: 8, padding: '10px', cursor: 'pointer', fontWeight: 500
                }}>
                  Cancelar Reserva
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ─── ETAPA SELEÇÃO ─── */}
        {etapa === 'selecao' && (
          <>
            {criando && (
              <form onSubmit={criarMapa} style={{
                background: 'var(--surface-2)', border: '1px solid var(--border)',
                borderRadius: 12, padding: 24, marginBottom: 32
              }}>
                <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 16 }}>Configurar Mapa de Assentos</h2>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                  {[
                    { label: 'Total de Fileiras', field: 'totalLinhas' },
                    { label: 'Assentos por Fileira', field: 'totalColunas' },
                    { label: 'Preço Normal (R$)', field: 'precoNormal' },
                    { label: 'Preço VIP (R$)', field: 'precoVip' },
                  ].map(({ label, field }) => (
                    <label key={field} style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                      <span style={{ fontSize: 13, color: 'var(--ink-2)', fontWeight: 500 }}>{label}</span>
                      <input
                        type="number"
                        value={(form as any)[field]}
                        onChange={e => setForm(prev => ({ ...prev, [field]: e.target.value }))}
                        min="1" step="0.01" required
                        style={{ padding: '8px 12px', borderRadius: 6, border: '1px solid var(--ink-muted)', fontSize: 14 }}
                      />
                    </label>
                  ))}
                </div>
                <p style={{ color: 'var(--ink-2)', fontSize: 13, marginTop: 12 }}>
                  Assentos VIP são gerados na 1ª fileira (centro). Acessibilidade: última fileira, extremidades.
                </p>
                <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
                  <button type="submit" style={btnStyle('#16a34a')}>Criar Mapa</button>
                  <button type="button" onClick={() => setCriando(false)} style={btnStyle('var(--ink-2)')}>Cancelar</button>
                </div>
              </form>
            )}

            {!eventoId ? (
              <SeletorEvento
                titulo="Selecione o evento"
                descricao="Escolha um evento para visualizar ou reservar assentos."
                rotaDestino="/mapa-assentos"
                icone="💺"
              />
            ) : carregando ? (
              <p style={{ color: 'var(--ink-2)', textAlign: 'center', padding: 40 }}>Carregando mapa...</p>
            ) : !mapa ? (
              <div style={{
                textAlign: 'center', padding: '60px 20px', background: 'var(--surface-2)',
                borderRadius: 12, border: '2px dashed var(--border)'
              }}>
                <p style={{ fontSize: 48, margin: '0 0 16px' }}>💺</p>
                <p style={{ color: 'var(--ink-2)', fontSize: 16 }}>Nenhum mapa de assentos configurado para este evento.</p>
                {ehOrganizador && (
                  <button onClick={() => setCriando(true)} style={{ ...btnStyle('var(--brand-strong)'), marginTop: 16 }}>
                    Criar Mapa de Assentos
                  </button>
                )}
              </div>
            ) : (
              <>
                {/* Legenda */}
                <div style={{ display: 'flex', gap: 16, marginBottom: 20, flexWrap: 'wrap', alignItems: 'center' }}>
                  <span style={{ fontSize: 12, color: 'var(--ink-2)', fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1 }}>
                    Legenda:
                  </span>
                  {[
                    { label: 'Disponível', cor: corStatus.DISPONIVEL },
                    { label: 'Reservado', cor: corStatus.RESERVADO },
                    { label: 'Vendido', cor: corStatus.VENDIDO },
                    { label: 'Bloqueado', cor: corStatus.BLOQUEADO },
                    { label: 'Normal', cor: corTipo.NORMAL, dashed: true },
                    { label: 'VIP', cor: corTipo.VIP, dashed: true },
                    { label: 'Acessível', cor: corTipo.ACESSIBILIDADE, dashed: true },
                  ].map(({ label, cor, dashed }) => (
                    <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13 }}>
                      <span style={{
                        width: 16, height: 16, borderRadius: 4, background: cor,
                        display: 'inline-block',
                        outline: dashed ? '2px dashed rgba(0,0,0,0.25)' : 'none',
                        outlineOffset: -2,
                      }} />
                      <span style={{ color: 'var(--ink-2)' }}>{label}</span>
                    </div>
                  ))}
                  <span style={{ marginLeft: 'auto', fontSize: 12, color: 'var(--ink-2)' }}>
                    Atualiza a cada 30s
                  </span>
                </div>

                {/* Fila de espera — quando não há assentos disponíveis */}
                {!temDisponivel && !ehOrganizador && (
                  <div style={{
                    background: filaStatus?.naFila ? '#fef3c7' : '#fef9c3',
                    border: `1.5px solid ${filaStatus?.naFila ? '#f59e0b' : '#facc15'}`,
                    borderRadius: 12, padding: '16px 20px', marginBottom: 24,
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, flexWrap: 'wrap'
                  }}>
                    <div>
                      <p style={{ fontWeight: 700, color: '#92400e', margin: '0 0 4px', fontSize: 15 }}>
                        Esgotado — Fila de Espera
                      </p>
                      {filaStatus?.naFila ? (
                        <p style={{ color: '#78350f', margin: 0, fontSize: 14 }}>
                          Você está na posição <strong>{filaStatus.posicao}</strong> de {filaStatus.tamanhoFila} na fila.
                          Será reservado automaticamente quando um assento abrir.
                        </p>
                      ) : (
                        <p style={{ color: '#78350f', margin: 0, fontSize: 14 }}>
                          {filaStatus && filaStatus.tamanhoFila > 0
                            ? `${filaStatus.tamanhoFila} pessoa(s) já na fila. `
                            : ''}
                          Entre na fila para ser atendido automaticamente quando um assento ficar disponível.
                        </p>
                      )}
                    </div>
                    {filaStatus?.naFila ? (
                      <button
                        onClick={sairFila}
                        disabled={processandoFila}
                        style={{ ...btnStyle('#dc2626'), opacity: processandoFila ? 0.7 : 1, whiteSpace: 'nowrap' }}>
                        {processandoFila ? 'Aguarde...' : 'Sair da Fila'}
                      </button>
                    ) : (
                      <button
                        onClick={entrarFila}
                        disabled={processandoFila}
                        style={{ ...btnStyle('#d97706'), opacity: processandoFila ? 0.7 : 1, whiteSpace: 'nowrap' }}>
                        {processandoFila ? 'Aguarde...' : 'Entrar na Fila'}
                      </button>
                    )}
                  </div>
                )}

                {/* Aviso quando há assentos mas usuário ainda está na fila */}
                {temDisponivel && filaStatus?.naFila && !ehOrganizador && (
                  <div style={{
                    background: '#dcfce7', border: '1.5px solid #86efac',
                    borderRadius: 12, padding: '14px 20px', marginBottom: 24,
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, flexWrap: 'wrap'
                  }}>
                    <p style={{ color: '#166534', margin: 0, fontWeight: 600 }}>
                      Assentos disponíveis! Você está na posição {filaStatus.posicao} da fila.
                      Selecione e reserve agora, ou aguarde a promoção automática.
                    </p>
                    <button
                      onClick={sairFila}
                      disabled={processandoFila}
                      style={{ ...btnStyle('#dc2626'), opacity: processandoFila ? 0.7 : 1, whiteSpace: 'nowrap', fontSize: 13 }}>
                      Sair da Fila
                    </button>
                  </div>
                )}

                {/* Modo organizador — painel de controle */}
                {modoOrganizador && (
                  <div style={{
                    background: '#f5f3ff', border: '1.5px solid #a78bfa',
                    borderRadius: 12, padding: '14px 20px', marginBottom: 24,
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16, flexWrap: 'wrap'
                  }}>
                    <div style={{ fontSize: 14, color: '#5b21b6' }}>
                      <span style={{ fontWeight: 700 }}>Modo Bloqueio ativo — </span>
                      {(paraBloquear.length > 0 || paraDesbloquear.length > 0)
                        ? [
                          paraBloquear.length > 0 && `${paraBloquear.length} para bloquear`,
                          paraDesbloquear.length > 0 && `${paraDesbloquear.length} para desbloquear`,
                        ].filter(Boolean).join(', ')
                        : 'Clique nos assentos verdes (bloquear) ou cinzas (desbloquear).'
                      }
                    </div>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button
                        onClick={aplicarBloqueio}
                        disabled={aplicandoBloqueio || (paraBloquear.length === 0 && paraDesbloquear.length === 0)}
                        style={{
                          ...btnStyle('#7c3aed'),
                          opacity: (aplicandoBloqueio || (paraBloquear.length === 0 && paraDesbloquear.length === 0)) ? 0.5 : 1
                        }}>
                        {aplicandoBloqueio ? 'Aplicando...' : 'Aplicar'}
                      </button>
                      <button onClick={cancelarModoOrganizador} style={btnStyle('#6b7280')}>
                        Cancelar
                      </button>
                    </div>
                  </div>
                )}

                {/* Palco */}
                <div style={{
                  background: 'var(--ink)', color: 'var(--ink-muted)', borderRadius: 8,
                  padding: '10px 0', textAlign: 'center', fontWeight: 600,
                  fontSize: 14, letterSpacing: 4, marginBottom: 24
                }}>
                  ▬▬▬▬▬ PALCO ▬▬▬▬▬
                </div>

                {/* Grade de assentos */}
                <div style={{ overflowX: 'auto' }}>
                  {[...agruparPorFileira(mapa.assentos).entries()].map(([fileira, assentos]) => (
                    <div key={fileira} style={{ display: 'flex', gap: 6, marginBottom: 8, alignItems: 'center' }}>
                      <span style={{ width: 20, color: 'var(--ink-muted)', fontSize: 13, fontWeight: 600, flexShrink: 0 }}>
                        {fileira}
                      </span>
                      {assentos
                        .sort((a, b) => (parseInt(a.codigo.substring(1)) || 0) - (parseInt(b.codigo.substring(1)) || 0))
                        .map(a => {
                          const isSelecionado = selecionados.includes(a.id)
                          const ehMeuReservado = a.reservadoPor === usuario?.id
                          const marcadoParaBloquear = paraBloquear.includes(a.id)
                          const marcadoParaDesbloquear = paraDesbloquear.includes(a.id)
                          const marcadoOrg = marcadoParaBloquear || marcadoParaDesbloquear

                          const corBase = a.status === 'DISPONIVEL'
                            ? (corTipo[a.tipo] ?? corTipo.NORMAL)
                            : (corStatus[a.status] ?? corStatus.VENDIDO)

                          const clicavel = modoOrganizador
                            ? (a.status === 'DISPONIVEL' || a.status === 'BLOQUEADO')
                            : a.status === 'DISPONIVEL'

                          return (
                            <button
                              key={a.id}
                              onClick={() => toggleSelecionado(a)}
                              title={`${a.codigo} — ${a.tipo} — ${formatMoeda(a.preco)} — ${a.status}`}
                              style={{
                                width: 36, height: 36, borderRadius: 6,
                                border: marcadoOrg
                                  ? '3px solid #7c3aed'
                                  : isSelecionado
                                    ? '3px solid #fbbf24'
                                    : ehMeuReservado
                                      ? '3px solid #f59e0b'
                                      : '2px solid transparent',
                                background: marcadoOrg ? '#ddd6fe' : isSelecionado ? '#fbbf24' : corBase,
                                cursor: clicavel ? 'pointer' : 'not-allowed',
                                fontSize: 9, color: marcadoOrg ? '#5b21b6' : '#fff', fontWeight: 700,
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                opacity: (a.status === 'BLOQUEADO' && !modoOrganizador) ? 0.4 : 1,
                                transition: 'transform 0.1s',
                                transform: (isSelecionado || marcadoOrg) ? 'scale(1.1)' : 'scale(1)',
                                flexShrink: 0,
                              }}
                            >
                              {marcadoParaBloquear ? '✕' : marcadoParaDesbloquear ? '↩' : a.codigo.substring(1)}
                            </button>
                          )
                        })}
                    </div>
                  ))}
                </div>

                {/* Painel sticky de seleção (comprador) */}
                {!modoOrganizador && selecionados.length > 0 && (
                  <div style={{
                    position: 'sticky', bottom: 20, marginTop: 32,
                    background: 'var(--ink)', borderRadius: 12, padding: '16px 24px',
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    boxShadow: '0 8px 32px rgba(0,0,0,0.3)'
                  }}>
                    <div>
                      <p style={{ color: 'var(--ink-muted)', margin: 0, fontSize: 13 }}>
                        {selecionados.length}/6 assento(s) selecionado(s)
                      </p>
                      <p style={{ color: 'var(--surface-2)', fontWeight: 700, fontSize: 20, margin: '4px 0 0' }}>
                        {formatMoeda(totalSelecionado)}
                      </p>
                    </div>
                    <div style={{ display: 'flex', gap: 12 }}>
                      <button
                        onClick={() => setSelecionados([])}
                        style={{ background: 'var(--ink-2)', color: 'var(--ink-muted)', border: 'none', borderRadius: 8, padding: '10px 16px', cursor: 'pointer' }}>
                        Limpar
                      </button>
                      <button
                        onClick={reservar}
                        style={{ background: '#16a34a', color: '#fff', border: 'none', borderRadius: 8, padding: '10px 20px', cursor: 'pointer', fontWeight: 700 }}>
                        Reservar por 5 min ⏱
                      </button>
                    </div>
                  </div>
                )}
              </>
            )}
          </>
        )}
      </div>
    </>
  )
}

const btnStyle = (bg: string): React.CSSProperties => ({
  background: bg, color: '#fff', border: 'none',
  borderRadius: 8, padding: '10px 20px', cursor: 'pointer', fontWeight: 600
})
