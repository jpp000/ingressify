import { useEffect, useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import {
  Dices, Plus, Target, Trophy, Clock, CheckCircle2, AlertTriangle, DoorOpen, Lock, Ban, Ticket,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Navbar from '../components/Navbar'
import SeletorEvento from '../components/SeletorEvento'
import { api, tipoIngressoService, eventoService } from '../services/api'
import { formatMoeda } from '../constants'

interface Sorteio {
  id: number
  eventoId: number
  tipoIngressoId: number
  organizadorId: number
  quantidadeIngressos: number
  quantidadeListaEspera: number
  prazoInscricao: string
  prazoConfirmacaoHoras: number
  status: string
}

interface InscricaoSorteio {
  id: number
  sorteioId: number
  participanteId: number
  inscritoEm: string
  status: string
  posicao: number
}

interface TipoIngresso {
  id: number
  nome: string
  preco: number
}

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
}

const statusLabel: Record<string, { label: string; variant: string }> = {
  CONFIGURADO:        { label: 'Configurado',        variant: 'badge' },
  INSCRICOES_ABERTAS: { label: 'Inscrições Abertas', variant: 'badge badge--success' },
  AGUARDANDO_SORTEIO: { label: 'Aguardando Sorteio', variant: 'badge badge--warn' },
  SORTEADO:           { label: 'Sorteado',           variant: 'badge badge--brand' },
  ENCERRADO:          { label: 'Encerrado',          variant: 'badge' },
  CANCELADO:          { label: 'Cancelado',          variant: 'badge badge--danger' },
}

const inscricaoLabel: Record<string, { label: string; variant: string; Icon?: typeof Trophy }> = {
  INSCRITO:     { label: 'Inscrito',        variant: 'badge' },
  CONTEMPLADO:  { label: 'Contemplado',     variant: 'badge badge--success', Icon: Trophy },
  LISTA_ESPERA: { label: 'Lista de Espera', variant: 'badge badge--warn', Icon: Clock },
  CONFIRMADO:   { label: 'Confirmado',      variant: 'badge badge--brand', Icon: CheckCircle2 },
  EXPIRADO:     { label: 'Expirado',        variant: 'badge badge--danger' },
  CANCELADO:    { label: 'Cancelado',       variant: 'badge badge--danger' },
}

const formatData = (iso: string) =>
  new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export default function SorteioPage() {
  const [searchParams] = useSearchParams()
  const eventoId = searchParams.get('eventoId')
  const { usuario, isOrganizador, isAdmin } = useAuth()

  const [sorteios, setSorteios] = useState<Sorteio[]>([])
  const [sorteioSelecionado, setSorteioSelecionado] = useState<Sorteio | null>(null)
  const [inscricoes, setInscricoes] = useState<InscricaoSorteio[]>([])
  const [minhaInscricao, setMinhaInscricao] = useState<InscricaoSorteio | null>(null)
  const [tipos, setTipos] = useState<TipoIngresso[]>([])
  const [evento, setEvento] = useState<Evento | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [mensagem, setMensagem] = useState<{ texto: string; tipo: 'ok' | 'erro'; link?: string } | null>(null)

  // Form para criar sorteio (organizador)
  const [criando, setCriando] = useState(false)
  const [form, setForm] = useState({
    tipoIngressoId: '',
    quantidadeIngressos: '1',
    quantidadeListaEspera: '0',
    prazoInscricao: '',
    prazoConfirmacaoHoras: '48',
  })

  const carregarSorteios = () => {
    if (!eventoId) return
    setCarregando(true)
    api.get('/sorteios', { params: { eventoId } })
      .then(r => setSorteios(r.data))
      .catch(() => setSorteios([]))
      .finally(() => setCarregando(false))
  }

  useEffect(() => { carregarSorteios() }, [eventoId])

  useEffect(() => {
    if (!eventoId) { setTipos([]); setEvento(null); return }
    tipoIngressoService.listar(Number(eventoId))
      .then(r => setTipos(r.data))
      .catch(() => setTipos([]))
    eventoService.detalhe(Number(eventoId))
      .then(r => setEvento(r.data))
      .catch(() => setEvento(null))
  }, [eventoId])

  const carregarInscricoes = (sorteio: Sorteio) => {
    setSorteioSelecionado(sorteio)
    api.get(`/sorteios/${sorteio.id}/inscricoes`)
      .then(r => {
        const lista: InscricaoSorteio[] = r.data
        setInscricoes(lista)
        const minha = lista.find(i => i.participanteId === usuario?.id) ?? null
        setMinhaInscricao(minha)
      })
      .catch(() => setInscricoes([]))
  }

  const exibirMensagem = (texto: string, tipo: 'ok' | 'erro', link?: string) => {
    setMensagem({ texto, tipo, link })
    setTimeout(() => setMensagem(null), link ? 8000 : 4000)
  }

  const acao = (
    url: string,
    metodo: 'post' | 'delete' = 'post',
    sucesso?: { texto: string; link?: string },
  ) => {
    api({ method: metodo, url, headers: { 'X-Usuario-Id': usuario?.id } })
      .then(() => {
        exibirMensagem(sucesso?.texto ?? 'Operação realizada com sucesso!', 'ok', sucesso?.link)
        carregarSorteios()
        if (sorteioSelecionado) carregarInscricoes(sorteioSelecionado)
      })
      .catch(err => {
        const motivo = err.response?.data?.motivo ?? 'Erro ao processar operação.'
        exibirMensagem(motivo, 'erro')
      })
  }

  const criarSorteio = (e: React.FormEvent) => {
    e.preventDefault()
    if (!eventoId) return
    api.post('/sorteios', {
      eventoId: Number(eventoId),
      tipoIngressoId: Number(form.tipoIngressoId),
      quantidadeIngressos: Number(form.quantidadeIngressos),
      quantidadeListaEspera: Number(form.quantidadeListaEspera),
      prazoInscricao: form.prazoInscricao,
      prazoConfirmacaoHoras: Number(form.prazoConfirmacaoHoras),
    }, { headers: { 'X-Usuario-Id': usuario?.id } })
      .then(() => {
        exibirMensagem('Sorteio criado com sucesso!', 'ok')
        setCriando(false)
        carregarSorteios()
      })
      .catch(err => {
        exibirMensagem(err.response?.data?.motivo ?? 'Erro ao criar sorteio.', 'erro')
      })
  }

  const ehOrganizador = isOrganizador()
  const ehAdmin = isAdmin()
  // Todo usuário é COMPRADOR por construção; a UI de comprador (inscrever/confirmar)
  // só faz sentido para quem NÃO gerencia sorteios (nem organizador, nem admin).
  const ehCompradorPuro = !ehOrganizador && !ehAdmin
  const semTipos = tipos.length === 0

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">

        {/* Cabeçalho */}
        <div className="page-head between wrap" style={{ gap: 'var(--sp-3)' }}>
          <div className="row" style={{ gap: 'var(--sp-3)' }}>
            <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><Dices size={24} /></span>
            <div>
              <h1>Sorteio de Ingressos</h1>
              {evento ? (
                <p className="secondary">
                  <strong style={{ color: 'var(--ink)' }}>{evento.nome}</strong>
                  {evento.local && <> · {evento.local}</>}
                  {evento.dataHora && <> · {formatData(evento.dataHora)}</>}
                </p>
              ) : (
                <p className="secondary">Inscreva-se para concorrer a ingressos por sorteio justo.</p>
              )}
            </div>
          </div>
          {ehOrganizador && eventoId && !criando && (
            <button className="btn btn--sm" onClick={() => setCriando(true)}>
              <Plus size={16} /> Criar Sorteio
            </button>
          )}
        </div>

        {/* Mensagem feedback */}
        {mensagem && (
          <div className={`auth-alert ${mensagem.tipo === 'ok' ? 'auth-alert--ok' : 'auth-alert--err'}`}>
            {mensagem.tipo === 'ok' ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />}
            <span>{mensagem.texto}</span>
            {mensagem.link && (
              <Link to={mensagem.link} className="row" style={{ gap: 6, fontWeight: 700, marginLeft: 'auto' }}>
                <Ticket size={16} /> Ver em Meus Ingressos
              </Link>
            )}
          </div>
        )}

        {/* Formulário de criação (organizador) */}
        {criando && eventoId && (
          <form onSubmit={criarSorteio} className="surface surface--pad" style={{ marginBottom: 'var(--sp-6)' }}>
            <h3 style={{ marginBottom: 'var(--sp-4)' }}>Novo Sorteio</h3>
            {semTipos && (
              <div className="auth-alert auth-alert--err" style={{ marginBottom: 'var(--sp-4)' }}>
                <AlertTriangle size={18} />
                <span>Cadastre um tipo de ingresso para este evento antes de criar um sorteio.</span>
              </div>
            )}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 'var(--sp-4)' }}>
              <div className="field" style={{ gridColumn: '1 / -1' }}>
                <span className="label">Tipo de Ingresso</span>
                <select
                  className="input"
                  value={form.tipoIngressoId}
                  onChange={e => setForm(prev => ({ ...prev, tipoIngressoId: e.target.value }))}
                  required
                  disabled={semTipos}
                >
                  <option value="" disabled>Selecione o tipo de ingresso</option>
                  {tipos.map(t => (
                    <option key={t.id} value={t.id}>{t.nome} — {formatMoeda(Number(t.preco))}</option>
                  ))}
                </select>
              </div>
              {[
                { label: 'Quantidade de Ingressos', field: 'quantidadeIngressos', type: 'number' },
                { label: 'Vagas na Lista de Espera', field: 'quantidadeListaEspera', type: 'number' },
                { label: 'Horas para Confirmação', field: 'prazoConfirmacaoHoras', type: 'number' },
              ].map(({ label, field, type }) => (
                <div key={field} className="field">
                  <span className="label">{label}</span>
                  <input
                    className="input"
                    type={type}
                    value={(form as Record<string, string>)[field]}
                    onChange={e => setForm(prev => ({ ...prev, [field]: e.target.value }))}
                    required
                  />
                </div>
              ))}
              <div className="field" style={{ gridColumn: '1 / -1' }}>
                <span className="label">Prazo para Inscrições</span>
                <input
                  className="input"
                  type="datetime-local"
                  value={form.prazoInscricao}
                  onChange={e => setForm(prev => ({ ...prev, prazoInscricao: e.target.value }))}
                  required
                />
              </div>
            </div>
            <div className="row" style={{ gap: 'var(--sp-3)', marginTop: 'var(--sp-5)' }}>
              <button type="submit" className="btn" disabled={semTipos}>
                <Plus size={16} /> Criar Sorteio
              </button>
              <button type="button" className="btn btn--ghost" onClick={() => setCriando(false)}>
                Cancelar
              </button>
            </div>
          </form>
        )}

        {/* Lista de sorteios */}
        {!eventoId ? (
          <SeletorEvento
            titulo="Selecione o evento"
            descricao="Escolha um evento para ver ou participar dos sorteios de ingressos."
            rotaDestino="/sorteios"
            icone="🎲"
          />
        ) : carregando ? (
          <div className="stack" style={{ gap: 'var(--sp-3)' }}>
            {Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 110 }} />)}
          </div>
        ) : sorteios.length === 0 ? (
          <div className="empty">
            <Dices size={40} />
            <h3>Nenhum sorteio</h3>
            <p className="muted">Nenhum sorteio disponível para este evento.</p>
          </div>
        ) : (
          <div className="stack" style={{ gap: 'var(--sp-4)' }}>
            {sorteios.map(s => {
              const st = statusLabel[s.status] ?? { label: s.status, variant: 'badge' }
              const aberto = s.status === 'INSCRICOES_ABERTAS'
              const selecionado = sorteioSelecionado?.id === s.id
              return (
                <div
                  key={s.id}
                  className="surface"
                  style={{ overflow: 'hidden', boxShadow: selecionado ? '0 0 0 2px var(--brand)' : undefined }}
                >
                  <div
                    onClick={() => carregarInscricoes(s)}
                    className="between"
                    style={{ padding: 'var(--sp-5)', cursor: 'pointer', gap: 'var(--sp-3)' }}
                  >
                    <div>
                      <div className="row" style={{ gap: 'var(--sp-2)', marginBottom: 6 }}>
                        <span style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--ink)' }}>
                          Sorteio #{s.id}
                        </span>
                        <span className={st.variant}>{st.label}</span>
                      </div>
                      <p className="secondary" style={{ fontSize: '0.875rem' }}>
                        {s.quantidadeIngressos} ingresso(s) · {s.quantidadeListaEspera} na lista de espera
                      </p>
                      <p className="muted" style={{ fontSize: '0.8125rem', marginTop: 4 }}>
                        Inscrições até: {formatData(s.prazoInscricao)}
                      </p>
                    </div>
                    {aberto && ehCompradorPuro && (
                      <span className="row" style={{ gap: 6, color: 'var(--brand-strong)', fontWeight: 600, fontSize: '0.875rem' }}>
                        <Target size={16} /> Clique para se inscrever
                      </span>
                    )}
                  </div>

                  {/* Painel expandido */}
                  {selecionado && (
                    <div style={{ borderTop: '1px solid var(--border)', padding: 'var(--sp-5)', background: 'var(--surface-2)' }}>

                      {/* Ações do comprador */}
                      {ehCompradorPuro && (
                        <div style={{ marginBottom: 'var(--sp-5)' }}>
                          {minhaInscricao ? (
                            <div className="surface between wrap" style={{ gap: 'var(--sp-3)', padding: 'var(--sp-3) var(--sp-4)' }}>
                              <div className="row" style={{ gap: 'var(--sp-3)' }}>
                                <span className="secondary">Sua situação:</span>
                                {(() => {
                                  const il = inscricaoLabel[minhaInscricao.status] ?? { label: minhaInscricao.status, variant: 'badge' }
                                  return (
                                    <span className={il.variant}>
                                      {il.Icon && <il.Icon size={14} />} {il.label}
                                    </span>
                                  )
                                })()}
                              </div>
                              {(minhaInscricao.status === 'CONTEMPLADO' || minhaInscricao.status === 'LISTA_ESPERA') && (
                                <button
                                  className="btn btn--sm"
                                  onClick={() => acao(`/sorteios/${s.id}/confirmar`, 'post', {
                                    texto: 'Ingresso garantido! 🎉 Ele já está na sua carteira.',
                                    link: '/meus-ingressos',
                                  })}
                                >
                                  <CheckCircle2 size={16} /> Confirmar Participação
                                </button>
                              )}
                            </div>
                          ) : aberto ? (
                            <button className="btn" onClick={() => acao(`/sorteios/${s.id}/inscrever`)}>
                              <Target size={18} /> Inscrever-se no Sorteio
                            </button>
                          ) : (
                            <p className="secondary" style={{ fontSize: '0.875rem' }}>Inscrições não disponíveis no momento.</p>
                          )}
                        </div>
                      )}

                      {/* Ações de gestão (organizador dono ou admin) */}
                      {((ehOrganizador && s.organizadorId === usuario?.id) || ehAdmin) && (
                        <div className="row wrap" style={{ gap: 'var(--sp-2)', marginBottom: 'var(--sp-5)' }}>
                          {s.status === 'CONFIGURADO' && (
                            <button className="btn btn--sm" onClick={() => acao(`/sorteios/${s.id}/abrir`)}>
                              <DoorOpen size={16} /> Abrir Inscrições
                            </button>
                          )}
                          {s.status === 'INSCRICOES_ABERTAS' && (
                            <button className="btn btn--sm btn--ghost" onClick={() => acao(`/sorteios/${s.id}/encerrar-inscricoes`)}>
                              <Lock size={16} /> Encerrar Inscrições
                            </button>
                          )}
                          {s.status === 'AGUARDANDO_SORTEIO' && (
                            <button className="btn btn--sm" onClick={() => acao(`/sorteios/${s.id}/sortear`)}>
                              <Dices size={16} /> Realizar Sorteio
                            </button>
                          )}
                          {!['ENCERRADO', 'CANCELADO'].includes(s.status) && (
                            <button className="btn btn--sm btn--danger" onClick={() => acao(`/sorteios/${s.id}/cancelar`)}>
                              <Ban size={16} /> Cancelar Sorteio
                            </button>
                          )}
                        </div>
                      )}

                      {/* Lista de inscrições */}
                      <h4 className="secondary" style={{ marginBottom: 'var(--sp-3)' }}>
                        Inscrições ({inscricoes.length})
                      </h4>
                      {inscricoes.length === 0 ? (
                        <p className="muted" style={{ fontSize: '0.875rem' }}>Nenhuma inscrição ainda.</p>
                      ) : (
                        <div className="surface" style={{ maxHeight: 240, overflowY: 'auto' }}>
                          {inscricoes.map((i, idx) => {
                            const il = inscricaoLabel[i.status] ?? { label: i.status, variant: 'badge' }
                            return (
                              <div
                                key={i.id}
                                className="between"
                                style={{ padding: 'var(--sp-3) var(--sp-4)', gap: 'var(--sp-3)', borderTop: idx === 0 ? undefined : '1px solid var(--border)', fontSize: '0.875rem' }}
                              >
                                <span className="secondary">Participante #{i.participanteId}</span>
                                <div className="row" style={{ gap: 'var(--sp-2)' }}>
                                  {i.posicao > 0 && (
                                    <span className="muted" style={{ fontSize: '0.75rem' }}>#{i.posicao}</span>
                                  )}
                                  <span className={il.variant}>
                                    {il.Icon && <il.Icon size={12} />} {il.label}
                                  </span>
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
