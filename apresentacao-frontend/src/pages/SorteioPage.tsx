import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Navbar from '../components/Navbar'
import SeletorEvento from '../components/SeletorEvento'
import { api } from '../services/api'

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

const statusLabel: Record<string, { label: string; cor: string }> = {
  CONFIGURADO:       { label: 'Configurado',        cor: '#64748b' },
  INSCRICOES_ABERTAS:{ label: 'Inscrições Abertas', cor: '#16a34a' },
  AGUARDANDO_SORTEIO:{ label: 'Aguardando Sorteio', cor: '#d97706' },
  SORTEADO:          { label: 'Sorteado',           cor: '#2563eb' },
  ENCERRADO:         { label: 'Encerrado',          cor: '#1e293b' },
  CANCELADO:         { label: 'Cancelado',          cor: '#dc2626' },
}

const inscricaoLabel: Record<string, { label: string; cor: string }> = {
  INSCRITO:     { label: 'Inscrito',          cor: '#64748b' },
  CONTEMPLADO:  { label: '🏆 Contemplado',   cor: '#16a34a' },
  LISTA_ESPERA: { label: '⏳ Lista de Espera', cor: '#d97706' },
  CONFIRMADO:   { label: '✅ Confirmado',    cor: '#2563eb' },
  EXPIRADO:     { label: 'Expirado',          cor: '#dc2626' },
  CANCELADO:    { label: 'Cancelado',         cor: '#dc2626' },
}

const formatData = (iso: string) =>
  new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export default function SorteioPage() {
  const [searchParams] = useSearchParams()
  const eventoId = searchParams.get('eventoId')
  const { usuario } = useAuth()

  const [sorteios, setSorteios] = useState<Sorteio[]>([])
  const [sorteioSelecionado, setSorteioSelecionado] = useState<Sorteio | null>(null)
  const [inscricoes, setInscricoes] = useState<InscricaoSorteioResponse[]>([])
  const [minhaInscricao, setMinhaInscricao] = useState<InscricaoSorteio | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [mensagem, setMensagem] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)

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

  const exibirMensagem = (texto: string, tipo: 'ok' | 'erro') => {
    setMensagem({ texto, tipo })
    setTimeout(() => setMensagem(null), 4000)
  }

  const acao = (url: string, metodo: 'post' | 'delete' = 'post') => {
    api({ method: metodo, url, headers: { 'X-Usuario-Id': usuario?.id } })
      .then(() => {
        exibirMensagem('Operação realizada com sucesso!', 'ok')
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

  const ehOrganizador = usuario?.papeis?.includes('ORGANIZADOR')

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 16px' }}>

        {/* Cabeçalho */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
          <div>
            <h1 style={{ fontSize: 28, fontWeight: 700, color: '#0f172a', margin: 0 }}>
              🎟️ Sorteio de Ingressos
            </h1>
            <p style={{ color: '#64748b', marginTop: 4 }}>
              Inscreva-se para concorrer a ingressos por sorteio justo
            </p>
          </div>
          {ehOrganizador && eventoId && !criando && (
            <button
              onClick={() => setCriando(true)}
              style={{
                background: '#1d4ed8', color: '#fff', border: 'none',
                borderRadius: 8, padding: '10px 20px', cursor: 'pointer', fontWeight: 600
              }}>
              + Criar Sorteio
            </button>
          )}
        </div>

        {/* Mensagem feedback */}
        {mensagem && (
          <div style={{
            background: mensagem.tipo === 'ok' ? '#dcfce7' : '#fee2e2',
            color: mensagem.tipo === 'ok' ? '#166534' : '#991b1b',
            borderRadius: 8, padding: '12px 16px', marginBottom: 20, fontWeight: 500
          }}>
            {mensagem.texto}
          </div>
        )}

        {/* Formulário de criação (organizador) */}
        {criando && eventoId && (
          <form onSubmit={criarSorteio} style={{
            background: '#f8fafc', border: '1px solid #e2e8f0',
            borderRadius: 12, padding: 24, marginBottom: 32
          }}>
            <h2 style={{ fontSize: 18, fontWeight: 600, marginBottom: 16 }}>Novo Sorteio</h2>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
              {[
                { label: 'ID Tipo de Ingresso', field: 'tipoIngressoId', type: 'number' },
                { label: 'Quantidade de Ingressos', field: 'quantidadeIngressos', type: 'number' },
                { label: 'Vagas na Lista de Espera', field: 'quantidadeListaEspera', type: 'number' },
                { label: 'Horas para Confirmação', field: 'prazoConfirmacaoHoras', type: 'number' },
              ].map(({ label, field, type }) => (
                <label key={field} style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                  <span style={{ fontSize: 13, color: '#475569', fontWeight: 500 }}>{label}</span>
                  <input
                    type={type}
                    value={(form as any)[field]}
                    onChange={e => setForm(prev => ({ ...prev, [field]: e.target.value }))}
                    required
                    style={{
                      padding: '8px 12px', borderRadius: 6, border: '1px solid #cbd5e1',
                      fontSize: 14, outline: 'none'
                    }}
                  />
                </label>
              ))}
              <label style={{ display: 'flex', flexDirection: 'column', gap: 4, gridColumn: '1 / -1' }}>
                <span style={{ fontSize: 13, color: '#475569', fontWeight: 500 }}>Prazo para Inscrições</span>
                <input
                  type="datetime-local"
                  value={form.prazoInscricao}
                  onChange={e => setForm(prev => ({ ...prev, prazoInscricao: e.target.value }))}
                  required
                  style={{
                    padding: '8px 12px', borderRadius: 6, border: '1px solid #cbd5e1',
                    fontSize: 14, outline: 'none'
                  }}
                />
              </label>
            </div>
            <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
              <button type="submit" style={{
                background: '#16a34a', color: '#fff', border: 'none',
                borderRadius: 8, padding: '10px 20px', cursor: 'pointer', fontWeight: 600
              }}>
                Criar Sorteio
              </button>
              <button type="button" onClick={() => setCriando(false)} style={{
                background: '#e2e8f0', color: '#475569', border: 'none',
                borderRadius: 8, padding: '10px 20px', cursor: 'pointer'
              }}>
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
          <p style={{ color: '#64748b', textAlign: 'center', padding: 40 }}>Carregando sorteios...</p>
        ) : sorteios.length === 0 ? (
          <div style={{
            textAlign: 'center', padding: '60px 20px', background: '#f8fafc',
            borderRadius: 12, border: '2px dashed #e2e8f0'
          }}>
            <p style={{ fontSize: 48, margin: '0 0 16px' }}>🎲</p>
            <p style={{ color: '#64748b', fontSize: 16 }}>Nenhum sorteio disponível para este evento.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {sorteios.map(s => {
              const st = statusLabel[s.status] ?? { label: s.status, cor: '#64748b' }
              const aberto = s.status === 'INSCRICOES_ABERTAS'
              return (
                <div key={s.id} style={{
                  border: '1px solid #e2e8f0', borderRadius: 12,
                  background: '#fff', overflow: 'hidden',
                  boxShadow: sorteioSelecionado?.id === s.id ? '0 0 0 2px #1d4ed8' : 'none'
                }}>
                  <div
                    onClick={() => carregarInscricoes(s)}
                    style={{
                      padding: 20, cursor: 'pointer',
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                    }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                        <span style={{ fontWeight: 700, fontSize: 16, color: '#0f172a' }}>
                          Sorteio #{s.id}
                        </span>
                        <span style={{
                          background: st.cor, color: '#fff', borderRadius: 20,
                          padding: '2px 12px', fontSize: 12, fontWeight: 600
                        }}>
                          {st.label}
                        </span>
                      </div>
                      <p style={{ color: '#475569', margin: 0, fontSize: 14 }}>
                        {s.quantidadeIngressos} ingresso(s) · {s.quantidadeListaEspera} na lista de espera
                      </p>
                      <p style={{ color: '#94a3b8', margin: '4px 0 0', fontSize: 13 }}>
                        Inscrições até: {formatData(s.prazoInscricao)}
                      </p>
                    </div>
                    {aberto && (
                      <span style={{ color: '#1d4ed8', fontWeight: 600, fontSize: 14 }}>
                        Clique para se inscrever →
                      </span>
                    )}
                  </div>

                  {/* Painel expandido */}
                  {sorteioSelecionado?.id === s.id && (
                    <div style={{ borderTop: '1px solid #e2e8f0', padding: 20, background: '#f8fafc' }}>

                      {/* Ações do comprador */}
                      {!ehOrganizador && (
                        <div style={{ marginBottom: 20 }}>
                          {minhaInscricao ? (
                            <div style={{
                              display: 'flex', alignItems: 'center', gap: 12,
                              background: '#fff', border: '1px solid #e2e8f0',
                              borderRadius: 8, padding: '12px 16px'
                            }}>
                              <span style={{ fontSize: 16 }}>Sua situação:</span>
                              <span style={{
                                background: inscricaoLabel[minhaInscricao.status]?.cor ?? '#64748b',
                                color: '#fff', borderRadius: 20, padding: '4px 14px',
                                fontWeight: 600, fontSize: 13
                              }}>
                                {inscricaoLabel[minhaInscricao.status]?.label ?? minhaInscricao.status}
                              </span>
                              {(minhaInscricao.status === 'CONTEMPLADO' || minhaInscricao.status === 'LISTA_ESPERA') && (
                                <button
                                  onClick={() => acao(`/sorteios/${s.id}/confirmar`)}
                                  style={{
                                    background: '#16a34a', color: '#fff', border: 'none',
                                    borderRadius: 8, padding: '8px 16px', cursor: 'pointer', fontWeight: 600
                                  }}>
                                  Confirmar Participação
                                </button>
                              )}
                            </div>
                          ) : aberto ? (
                            <button
                              onClick={() => acao(`/sorteios/${s.id}/inscrever`)}
                              style={{
                                background: '#1d4ed8', color: '#fff', border: 'none',
                                borderRadius: 8, padding: '12px 24px', cursor: 'pointer',
                                fontWeight: 600, fontSize: 15
                              }}>
                              🎯 Inscrever-se no Sorteio
                            </button>
                          ) : (
                            <p style={{ color: '#64748b', fontSize: 14 }}>Inscrições não disponíveis no momento.</p>
                          )}
                        </div>
                      )}

                      {/* Ações do organizador */}
                      {ehOrganizador && s.organizadorId === usuario?.id && (
                        <div style={{ display: 'flex', gap: 10, marginBottom: 20, flexWrap: 'wrap' }}>
                          {s.status === 'CONFIGURADO' && (
                            <button onClick={() => acao(`/sorteios/${s.id}/abrir`)} style={btnStyle('#16a34a')}>
                              Abrir Inscrições
                            </button>
                          )}
                          {s.status === 'INSCRICOES_ABERTAS' && (
                            <button onClick={() => acao(`/sorteios/${s.id}/encerrar-inscricoes`)} style={btnStyle('#d97706')}>
                              Encerrar Inscrições
                            </button>
                          )}
                          {s.status === 'AGUARDANDO_SORTEIO' && (
                            <button onClick={() => acao(`/sorteios/${s.id}/sortear`)} style={btnStyle('#1d4ed8')}>
                              🎲 Realizar Sorteio
                            </button>
                          )}
                          {!['ENCERRADO', 'CANCELADO'].includes(s.status) && (
                            <button onClick={() => acao(`/sorteios/${s.id}/cancelar`)} style={btnStyle('#dc2626')}>
                              Cancelar Sorteio
                            </button>
                          )}
                        </div>
                      )}

                      {/* Lista de inscrições */}
                      <h3 style={{ fontSize: 14, fontWeight: 600, color: '#475569', marginBottom: 12 }}>
                        Inscrições ({inscricoes.length})
                      </h3>
                      {inscricoes.length === 0 ? (
                        <p style={{ color: '#94a3b8', fontSize: 14 }}>Nenhuma inscrição ainda.</p>
                      ) : (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 240, overflowY: 'auto' }}>
                          {inscricoes.map(i => {
                            const il = inscricaoLabel[i.status] ?? { label: i.status, cor: '#64748b' }
                            return (
                              <div key={i.id} style={{
                                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                                background: '#fff', borderRadius: 6, padding: '8px 14px',
                                border: '1px solid #e2e8f0', fontSize: 13
                              }}>
                                <span style={{ color: '#475569' }}>Participante #{i.participanteId}</span>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                  {i.posicao > 0 && (
                                    <span style={{ color: '#94a3b8', fontSize: 12 }}>#{i.posicao}</span>
                                  )}
                                  <span style={{
                                    background: il.cor, color: '#fff',
                                    borderRadius: 20, padding: '2px 10px', fontSize: 11, fontWeight: 600
                                  }}>
                                    {il.label}
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

const btnStyle = (bg: string): React.CSSProperties => ({
  background: bg, color: '#fff', border: 'none',
  borderRadius: 8, padding: '8px 16px', cursor: 'pointer', fontWeight: 600, fontSize: 13
})

// Necessário para tipagem local
type InscricaoSorteioResponse = InscricaoSorteio
