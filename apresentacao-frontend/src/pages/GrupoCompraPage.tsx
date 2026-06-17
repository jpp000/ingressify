import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  Users, Plus, Trash2, Wallet, CheckCircle2, AlertTriangle, Ban, Crown,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Navbar from '../components/Navbar'
import SeletorEvento from '../components/SeletorEvento'
import { grupoCompraService, tipoIngressoService } from '../services/api'
import { formatMoeda } from '../constants'

interface GrupoCompra {
  id: number
  eventoId: number
  tipoIngressoId: number
  liderId: number
  quantidadeTotal: number
  prazoPagamento: string
  status: string
  criadoEm: string
}

interface ParticipanteGrupo {
  id: number
  grupoCompraId: number
  usuarioId: number
  quantidade: number
  meiaEntrada: boolean
  documento: string | null
  valor: number
  status: string
  pagoEm: string | null
}

interface TipoIngresso {
  id: number
  nome: string
  preco: number
  quantidadeDisponivel: number
  precoMeia: number | null
  meiaEntradaHabilitada: boolean
}

interface LinhaParticipante {
  email: string
  quantidade: string
  meiaEntrada: boolean
  documento: string
}

const statusGrupoLabel: Record<string, { label: string; variant: string }> = {
  ABERTO:     { label: 'Aberto',     variant: 'badge badge--success' },
  CONFIRMADO: { label: 'Confirmado', variant: 'badge badge--brand' },
  EXPIRADO:   { label: 'Expirado',   variant: 'badge badge--danger' },
  CANCELADO:  { label: 'Cancelado',  variant: 'badge badge--danger' },
}

const statusParticipanteLabel: Record<string, { label: string; variant: string }> = {
  PENDENTE:    { label: 'Pendente',    variant: 'badge badge--warn' },
  PAGO:        { label: 'Pago',        variant: 'badge badge--success' },
  REEMBOLSADO: { label: 'Reembolsado', variant: 'badge' },
}

const formatData = (iso: string) =>
  new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

const linhaVazia = (email = ''): LinhaParticipante =>
  ({ email, quantidade: '1', meiaEntrada: false, documento: '' })

export default function GrupoCompraPage() {
  const [searchParams] = useSearchParams()
  const eventoId = searchParams.get('eventoId')
  const { usuario } = useAuth()

  const [grupos, setGrupos] = useState<GrupoCompra[]>([])
  const [tiposIngresso, setTiposIngresso] = useState<TipoIngresso[]>([])
  const [grupoSelecionado, setGrupoSelecionado] = useState<GrupoCompra | null>(null)
  const [participantes, setParticipantes] = useState<ParticipanteGrupo[]>([])
  const [carregando, setCarregando] = useState(false)
  const [mensagem, setMensagem] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)

  const [criando, setCriando] = useState(false)
  const [form, setForm] = useState({ tipoIngressoId: '', prazoPagamento: '' })
  const [linhas, setLinhas] = useState<LinhaParticipante[]>([linhaVazia()])

  const carregarGrupos = () => {
    if (!eventoId) return
    setCarregando(true)
    grupoCompraService.listarPorEvento(Number(eventoId))
      .then(r => setGrupos(r.data))
      .catch(() => setGrupos([]))
      .finally(() => setCarregando(false))
  }

  useEffect(() => {
    carregarGrupos()
    if (!eventoId) {
      setTiposIngresso([])
      return
    }
    tipoIngressoService.listar(Number(eventoId))
      .then(r => setTiposIngresso(r.data))
      .catch(() => setTiposIngresso([]))
  }, [eventoId])

  const carregarParticipantes = (grupo: GrupoCompra) => {
    setGrupoSelecionado(grupo)
    grupoCompraService.participantes(grupo.id)
      .then(r => setParticipantes(r.data))
      .catch(() => setParticipantes([]))
  }

  const exibirMensagem = (texto: string, tipo: 'ok' | 'erro') => {
    setMensagem({ texto, tipo })
    setTimeout(() => setMensagem(null), 4000)
  }

  const atualizarApósAção = (mensagemOk: string) => {
    exibirMensagem(mensagemOk, 'ok')
    carregarGrupos()
    if (grupoSelecionado) carregarParticipantes(grupoSelecionado)
  }

  const pagarMinhaParte = (grupo: GrupoCompra) => {
    if (!usuario) return
    grupoCompraService.pagar(grupo.id, usuario.id)
      .then(() => atualizarApósAção('Pagamento confirmado com sucesso!'))
      .catch(err => exibirMensagem(err.response?.data?.motivo ?? 'Erro ao processar pagamento.', 'erro'))
  }

  const cancelarGrupo = (grupo: GrupoCompra) => {
    if (!usuario) return
    grupoCompraService.cancelar(grupo.id, usuario.id)
      .then(() => atualizarApósAção('Grupo de compra cancelado.'))
      .catch(err => exibirMensagem(err.response?.data?.motivo ?? 'Erro ao cancelar grupo.', 'erro'))
  }

  const abrirFormulario = () => {
    setLinhas([linhaVazia(usuario?.email ?? '')])
    setForm({ tipoIngressoId: tiposIngresso[0]?.id ? String(tiposIngresso[0].id) : '', prazoPagamento: '' })
    setCriando(true)
  }

  const adicionarLinha = () => setLinhas(prev => [...prev, linhaVazia()])
  const removerLinha = (idx: number) =>
    setLinhas(prev => prev.length > 1 ? prev.filter((_, i) => i !== idx) : prev)
  const atualizarLinha = (idx: number, campo: keyof LinhaParticipante, valor: string | boolean) =>
    setLinhas(prev => prev.map((linha, i) => i === idx ? { ...linha, [campo]: valor } : linha))

  const criarGrupo = (e: React.FormEvent) => {
    e.preventDefault()
    if (!eventoId || !usuario) return
    const participantesPayload = linhas.map(l => ({
      email: l.email.trim(),
      quantidade: Number(l.quantidade),
      meiaEntrada: l.meiaEntrada,
      documento: l.meiaEntrada ? l.documento : null,
    }))
    grupoCompraService.criar(usuario.id, {
      eventoId: Number(eventoId),
      tipoIngressoId: Number(form.tipoIngressoId),
      prazoPagamento: form.prazoPagamento,
      participantes: participantesPayload,
    })
      .then(() => {
        exibirMensagem('Grupo de compra criado com sucesso!', 'ok')
        setCriando(false)
        carregarGrupos()
      })
      .catch(err => exibirMensagem(err.response?.data?.motivo ?? 'Erro ao criar grupo de compra.', 'erro'))
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">

        <div className="page-head between wrap" style={{ gap: 'var(--sp-3)' }}>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><Users size={24} /></span>
            <div>
              <h1>Compra em Grupo</h1>
              <p className="secondary">Reúna amigos para comprar ingressos juntos — cada um paga sua parte.</p>
            </div>
          </div>
          {eventoId && !criando && (
            <button className="btn btn--sm" onClick={abrirFormulario}>
              <Plus size={16} /> Criar Grupo
            </button>
          )}
        </div>

        {mensagem && (
          <div className={`auth-alert ${mensagem.tipo === 'ok' ? 'auth-alert--ok' : 'auth-alert--err'}`}>
            {mensagem.tipo === 'ok' ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}
            {mensagem.texto}
          </div>
        )}

        {criando && eventoId && (
          <form onSubmit={criarGrupo} className="surface surface--pad" style={{ marginBottom: 'var(--sp-6)' }}>
            <h3 style={{ marginBottom: 'var(--sp-4)' }}>Novo Grupo de Compra</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--sp-4)' }}>
              <div className="field">
                <span className="label">Tipo de Ingresso</span>
                <select
                  className="input"
                  value={form.tipoIngressoId}
                  onChange={e => setForm(prev => ({ ...prev, tipoIngressoId: e.target.value }))}
                  required
                >
                  <option value="" disabled>Selecione um tipo</option>
                  {tiposIngresso.map(tipo => (
                    <option key={tipo.id} value={tipo.id}>
                      {tipo.nome} - {formatMoeda(tipo.preco)} - {tipo.quantidadeDisponivel} disponiveis
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <span className="label">Prazo para Pagamento</span>
                <input
                  className="input"
                  type="datetime-local"
                  value={form.prazoPagamento}
                  onChange={e => setForm(prev => ({ ...prev, prazoPagamento: e.target.value }))}
                  required
                />
              </div>
            </div>

            <h4 className="secondary" style={{ margin: 'var(--sp-5) 0 var(--sp-3)' }}>Participantes</h4>
            <div className="stack" style={{ gap: 'var(--sp-3)' }}>
              {linhas.map((linha, idx) => (
                <div key={idx} className="row wrap" style={{ gap: 'var(--sp-3)', alignItems: 'flex-end' }}>
                  <div className="field" style={{ flex: '1 1 120px' }}>
                    <span className="label">E-mail do convidado</span>
                    <input
                      className="input"
                      type="email"
                      value={linha.email}
                      onChange={e => atualizarLinha(idx, 'email', e.target.value)}
                      placeholder="nome@email.com"
                      required
                    />
                  </div>
                  <div className="field" style={{ flex: '1 1 100px' }}>
                    <span className="label">Quantidade</span>
                    <input
                      className="input"
                      type="number"
                      min={1}
                      value={linha.quantidade}
                      onChange={e => atualizarLinha(idx, 'quantidade', e.target.value)}
                      required
                    />
                  </div>
                  <label className="row" style={{ gap: 6, paddingBottom: 10 }}>
                    <input
                      type="checkbox"
                      checked={linha.meiaEntrada}
                      onChange={e => atualizarLinha(idx, 'meiaEntrada', e.target.checked)}
                    />
                    Meia-entrada
                  </label>
                  {linha.meiaEntrada && (
                    <div className="field" style={{ flex: '1 1 160px' }}>
                      <span className="label">Documento</span>
                      <input
                        className="input"
                        type="text"
                        value={linha.documento}
                        onChange={e => atualizarLinha(idx, 'documento', e.target.value)}
                        required
                      />
                    </div>
                  )}
                  <button
                    type="button"
                    className="btn btn--sm btn--ghost btn--danger"
                    onClick={() => removerLinha(idx)}
                    disabled={linhas.length === 1}
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ))}
            </div>
            <button type="button" className="btn btn--sm btn--ghost" onClick={adicionarLinha} style={{ marginTop: 'var(--sp-3)' }}>
              <Plus size={16} /> Adicionar Participante
            </button>

            <div className="row" style={{ gap: 'var(--sp-3)', marginTop: 'var(--sp-5)' }}>
              <button type="submit" className="btn">
                <Plus size={16} /> Criar Grupo
              </button>
              <button type="button" className="btn btn--ghost" onClick={() => setCriando(false)}>
                Cancelar
              </button>
            </div>
          </form>
        )}

        {!eventoId ? (
          <SeletorEvento
            titulo="Selecione o evento"
            descricao="Escolha um evento para ver ou criar grupos de compra."
            rotaDestino="/grupos-compra"
            icone="👥"
          />
        ) : carregando ? (
          <div className="stack" style={{ gap: 'var(--sp-3)' }}>
            {Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 110 }} />)}
          </div>
        ) : grupos.length === 0 ? (
          <div className="empty">
            <Users size={40} />
            <h3>Nenhum grupo de compra</h3>
            <p className="muted">Nenhum grupo de compra disponível para este evento.</p>
          </div>
        ) : (
          <div className="stack" style={{ gap: 'var(--sp-4)' }}>
            {grupos.map(g => {
              const st = statusGrupoLabel[g.status] ?? { label: g.status, variant: 'badge' }
              const selecionado = grupoSelecionado?.id === g.id
              const souLider = g.liderId === usuario?.id
              const minhaParticipacao = selecionado
                ? participantes.find(p => p.usuarioId === usuario?.id) ?? null
                : null
              return (
                <div
                  key={g.id}
                  className="surface"
                  style={{ overflow: 'hidden', boxShadow: selecionado ? '0 0 0 2px var(--brand)' : undefined }}
                >
                  <div
                    onClick={() => carregarParticipantes(g)}
                    className="between"
                    style={{ padding: 'var(--sp-5)', cursor: 'pointer', gap: 'var(--sp-3)' }}
                  >
                    <div>
                      <div className="row" style={{ gap: 'var(--sp-2)', marginBottom: 6 }}>
                        <span style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--ink)' }}>
                          Grupo #{g.id}
                        </span>
                        <span className={st.variant}>{st.label}</span>
                        {souLider && (
                          <span className="badge" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                            <Crown size={12} /> Você é o líder
                          </span>
                        )}
                      </div>
                      <p className="secondary" style={{ fontSize: '0.875rem' }}>
                        {g.quantidadeTotal} ingresso(s) no total
                      </p>
                      <p className="muted" style={{ fontSize: '0.8125rem', marginTop: 4 }}>
                        Pagamento até: {formatData(g.prazoPagamento)}
                      </p>
                    </div>
                  </div>

                  {selecionado && (
                    <div style={{ borderTop: '1px solid var(--border)', padding: 'var(--sp-5)', background: 'var(--surface-2)' }}>

                      {minhaParticipacao && minhaParticipacao.status === 'PENDENTE' && g.status === 'ABERTO' && (
                        <div className="surface between wrap" style={{ gap: 'var(--sp-3)', padding: 'var(--sp-3) var(--sp-4)', marginBottom: 'var(--sp-5)' }}>
                          <div className="row" style={{ gap: 'var(--sp-3)' }}>
                            <span className="secondary">Sua parte:</span>
                            <span style={{ fontWeight: 700 }}>{formatMoeda(minhaParticipacao.valor)}</span>
                          </div>
                          <button className="btn btn--sm" onClick={() => pagarMinhaParte(g)}>
                            <Wallet size={16} /> Pagar minha parte
                          </button>
                        </div>
                      )}

                      {souLider && g.status === 'ABERTO' && (
                        <div className="row wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-5)' }}>
                          <button className="btn btn--sm btn--danger" onClick={() => cancelarGrupo(g)}>
                            <Ban size={16} /> Cancelar Grupo
                          </button>
                        </div>
                      )}

                      <h4 className="secondary" style={{ marginBottom: 'var(--sp-3)' }}>
                        Participantes ({participantes.length})
                      </h4>
                      {participantes.length === 0 ? (
                        <p className="muted" style={{ fontSize: '0.875rem' }}>Nenhum participante ainda.</p>
                      ) : (
                        <div className="surface" style={{ maxHeight: 280, overflowY: 'auto' }}>
                          {participantes.map((p, idx) => {
                            const pl = statusParticipanteLabel[p.status] ?? { label: p.status, variant: 'badge' }
                            return (
                              <div
                                key={p.id}
                                className="between"
                                style={{ padding: 'var(--sp-3) var(--sp-4)', gap: 'var(--sp-3)', borderTop: idx === 0 ? undefined : '1px solid var(--border)', fontSize: '0.875rem' }}
                              >
                                <div className="row" style={{ gap: 'var(--sp-2)' }}>
                                  <span className="secondary">Usuário #{p.usuarioId}</span>
                                  {p.usuarioId === g.liderId && (
                                    <span className="muted" style={{ fontSize: '0.75rem' }} title="Líder do grupo">
                                      <Crown size={12} />
                                    </span>
                                  )}
                                  <span className="muted" style={{ fontSize: '0.75rem' }}>
                                    {p.quantidade}x{p.meiaEntrada ? ' (meia)' : ''}
                                  </span>
                                </div>
                                <div className="row" style={{ gap: 'var(--sp-2)' }}>
                                  <span className="muted" style={{ fontSize: '0.75rem' }}>{formatMoeda(p.valor)}</span>
                                  <span className={pl.variant}>{pl.label}</span>
                                </div>
                              </div>
                            )
                          })}
                        </div>
                      )}
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
