import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { eventoService, tipoIngressoService, analyticsService } from '../services/api'
import Navbar from '../components/Navbar'
import { CATEGORIAS, formatMoeda } from '../constants'
import { useAuth } from '../context/AuthContext'

type Aba = 'meus-eventos' | 'criar-evento' | 'relatorios' | 'configuracoes'

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  status: string
  capacidade: number
  categoria?: string
}

interface TipoMetrica {
  id: number
  nome: string
  preco: number
  quantidadeTotal: number
  quantidadeDisponivel: number
}

interface AnalyticsData {
  totalVendidos: number
  totalRevendidos: number
  totalDisponiveis: number
  totalCapacidade: number
  taxaOcupacao: number
  taxaRevenda: number
  mediaAvaliacao: number
  totalAvaliacoes: number
}

interface LoteForm {
  nome: string
  preco: string
  quantidade: string
  dataInicio: string
  dataFim: string
}

interface TipoForm {
  nome: string
  descricao: string
  quantidadeTotal: string
  beneficios: string[]
  lotes: LoteForm[]
}

interface EventoForm {
  nome: string
  dataHora: string
  local: string
  descricao: string
  capacidade: string
  prazoReembolsoDias: string
  aberturaPortoes: string
  imagemCapaUrl: string
  categoria: string
}

const FORM_INICIAL: EventoForm = {
  nome: '',
  dataHora: '',
  local: '',
  descricao: '',
  capacidade: '500',
  prazoReembolsoDias: '7',
  aberturaPortoes: '',
  imagemCapaUrl: '',
  categoria: '',
}

const TIPO_INICIAL: TipoForm = {
  nome: '',
  descricao: '',
  quantidadeTotal: '100',
  beneficios: [],
  lotes: [{ nome: 'Lote 1', preco: '50,00', quantidade: '100', dataInicio: '', dataFim: '' }],
}

export default function GerenciarEventos() {
  const location = useLocation()
  const { usuario } = useAuth()
  const abaInicial: Aba = (location.state as { aba?: Aba } | null)?.aba ?? 'criar-evento'

  const [abaAtiva, setAbaAtiva] = useState<Aba>(abaInicial)
  const [eventos, setEventos] = useState<Evento[]>([])
  const [form, setForm] = useState<EventoForm>(FORM_INICIAL)
  const [tipos, setTipos] = useState<TipoForm[]>([])
  const [msg, setMsg] = useState('')
  const [erros, setErros] = useState<string[]>([])
  const [salvando, setSalvando] = useState(false)
  const [imagemPreview, setImagemPreview] = useState<string | null>(null)
  const [modalAberto, setModalAberto] = useState(false)
  const [tipoEditandoIndex, setTipoEditandoIndex] = useState<number | null>(null)
  const [eventoRelatorio, setEventoRelatorio] = useState<number | null>(null)
  const [tiposMetrica, setTiposMetrica] = useState<TipoMetrica[]>([])
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(null)
  const [carregandoMetricas, setCarregandoMetricas] = useState(false)

  const carregarEventos = () => {
    if (!usuario) return
    eventoService.listar(usuario.id).then(r => setEventos(r.data)).catch(() => {})
  }

  useEffect(() => { carregarEventos() }, [])

  const abrirModalNovo = () => {
    setTipoEditandoIndex(null)
    setModalAberto(true)
  }

  const abrirModalEditar = (index: number) => {
    setTipoEditandoIndex(index)
    setModalAberto(true)
  }

  const fecharModal = () => setModalAberto(false)

  const salvarTipo = (tipo: TipoForm) => {
    if (tipoEditandoIndex === null) {
      setTipos(prev => [...prev, tipo])
    } else {
      setTipos(prev => prev.map((t, i) => i === tipoEditandoIndex ? tipo : t))
    }
    setModalAberto(false)
  }

  const removerTipo = (index: number) => {
    setTipos(prev => prev.filter((_, i) => i !== index))
  }

  const validarFormulario = (): string[] => {
    const msgs: string[] = []

    if (!form.nome.trim()) msgs.push('Nome do evento é obrigatório.')
    if (!form.dataHora) msgs.push('Data e horário do evento são obrigatórios.')
    else if (new Date(form.dataHora) <= new Date()) msgs.push('A data do evento deve ser futura.')
    if (!form.aberturaPortoes) msgs.push('Horário de abertura dos portões é obrigatório.')
    else if (form.dataHora && new Date(form.aberturaPortoes) >= new Date(form.dataHora))
      msgs.push('Abertura dos portões deve ser antes do início do evento.')
    if (!form.local.trim()) msgs.push('Endereço do local é obrigatório.')
    const cap = Number(form.capacidade)
    if (!cap || cap < 1) msgs.push('Capacidade total deve ser pelo menos 1.')

    if (tipos.length === 0) {
      msgs.push('Adicione pelo menos um tipo de ingresso.')
    } else {
      tipos.forEach((tipo, i) => {
        const prefix = `Tipo "${tipo.nome || `${i + 1}`}": `
        if (!tipo.nome.trim()) msgs.push(`Tipo ${i + 1}: nome é obrigatório.`)

        const qtdTotal = Number(tipo.quantidadeTotal)
        if (!qtdTotal || qtdTotal < 1)
          msgs.push(`${prefix}quantidade total deve ser pelo menos 1.`)

        const lotesValidos = tipo.lotes.filter(l => l.nome.trim() && Number(l.quantidade) > 0)
        if (lotesValidos.length === 0)
          msgs.push(`${prefix}adicione pelo menos um lote com nome e quantidade preenchidos.`)

        lotesValidos.forEach(lote => {
          const preco = parseFloat(lote.preco.replace(',', '.'))
          if (isNaN(preco) || preco <= 0)
            msgs.push(`${prefix}Lote "${lote.nome}": preço deve ser maior que zero.`)
          if (Number(lote.quantidade) <= 0)
            msgs.push(`${prefix}Lote "${lote.nome}": quantidade deve ser maior que zero.`)
        })

        const somaLotes = tipo.lotes.reduce((s, l) => s + (Number(l.quantidade) || 0), 0)
        if (qtdTotal && somaLotes > qtdTotal)
          msgs.push(`${prefix}soma dos lotes (${somaLotes}) excede a quantidade total (${qtdTotal}).`)
      })

      const somaTipos = tipos.reduce((s, t) => s + (Number(t.quantidadeTotal) || 0), 0)
      if (cap && somaTipos > cap)
        msgs.push(`A soma das quantidades dos tipos (${somaTipos}) excede a capacidade do evento (${cap}).`)
    }

    return msgs
  }

  const criar = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setMsg('')

    const errosValidacao = validarFormulario()
    if (errosValidacao.length > 0) {
      setErros(errosValidacao)
      window.scrollTo({ top: 0, behavior: 'smooth' })
      return
    }
    setErros([])

    setSalvando(true)
    let novoEventoId: number | null = null

    try {
      const dados = {
        nome: form.nome,
        dataHora: form.dataHora,
        local: form.local,
        descricao: form.descricao || null,
        capacidade: Number(form.capacidade),
        imagemCapaUrl: form.imagemCapaUrl || null,
        prazoReembolsoDias: Number(form.prazoReembolsoDias),
        aberturaPortoes: form.aberturaPortoes || null,
        categoria: form.categoria || null,
      }
      const res = await eventoService.criar(usuario!.id, dados)
      novoEventoId = res.data.id

      for (const tipo of tipos) {
        if (!tipo.nome.trim()) continue
        const lotesValidos = tipo.lotes.filter(l => l.nome.trim() && Number(l.quantidade) > 0)
        const qtdTotal = Number(tipo.quantidadeTotal) || lotesValidos.reduce((s, l) => s + (Number(l.quantidade) || 0), 0)
        const precoBase = parseFloat((lotesValidos[0]?.preco ?? '0').replace(',', '.')) || 0

        await tipoIngressoService.criar(novoEventoId!, usuario!.id, {
          nome: tipo.nome,
          preco: precoBase,
          quantidade: qtdTotal,
          descricao: tipo.descricao || null,
          beneficios: tipo.beneficios.filter(b => b.trim()),
          lotes: lotesValidos.map(l => ({
            nome: l.nome,
            preco: parseFloat(l.preco.replace(',', '.')) || 0,
            quantidade: Number(l.quantidade) || 0,
            dataInicio: l.dataInicio || null,
            dataFim: l.dataFim || null,
          })),
        })
      }

      setMsg('Evento criado com sucesso!')
      setForm(FORM_INICIAL)
      setTipos([])
      setImagemPreview(null)
      carregarEventos()
      setAbaAtiva('meus-eventos')
    } catch (err: unknown) {
      if (novoEventoId !== null) {
        try { await eventoService.remover(novoEventoId, usuario!.id) } catch { /* ignore */ }
      }
      const e = err as { response?: { data?: { message?: string } } }
      const fallback = novoEventoId === null ? 'Erro ao criar evento. Verifique os dados preenchidos.' : 'Erro ao criar tipos de ingresso.'
      const mensagemErro = e.response?.data?.message ?? fallback
      setErros([`Evento não foi publicado. ${mensagemErro}`])
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } finally {
      setSalvando(false)
    }
  }

  const cancelarEvento = async (id: number) => {
    if (!confirm('Cancelar este evento? Todos os ingressos serão reembolsados.')) return
    try {
      await eventoService.cancelar(id, usuario!.id)
      setMsg('Evento cancelado.')
      carregarEventos()
    } catch {
      setMsg('Erro ao cancelar evento.')
    }
  }

  const excluirEvento = async (id: number) => {
    if (!confirm('Excluir este evento permanentemente? Esta ação não pode ser desfeita.')) return
    try {
      await eventoService.remover(id, usuario!.id)
      setMsg('Evento excluído.')
      carregarEventos()
    } catch {
      setMsg('Erro ao excluir evento. Certifique-se de que não há ingressos vendidos.')
    }
  }

  const selecionarEventoRelatorio = async (eventoId: number) => {
    setEventoRelatorio(eventoId)
    setCarregandoMetricas(true)
    setAnalyticsData(null)
    try {
      const [tiposRes, analyticsRes] = await Promise.all([
        tipoIngressoService.listar(eventoId),
        analyticsService.obter(eventoId, usuario!.id),
      ])
      setTiposMetrica(tiposRes.data)
      setAnalyticsData(analyticsRes.data)
    } catch {
      setTiposMetrica([])
      setAnalyticsData(null)
    } finally {
      setCarregandoMetricas(false)
    }
  }

  const SIDEBAR: { id: Aba; label: string; icon: string }[] = [
    { id: 'meus-eventos', label: 'Meus Eventos', icon: '📅' },
    { id: 'criar-evento', label: 'Criar Evento', icon: '➕' },
    { id: 'relatorios', label: 'Relatórios de Vendas', icon: '📊' },
    { id: 'configuracoes', label: 'Configurações', icon: '⚙️' },
  ]

  return (
    <>
      <Navbar variant="organizer" />
      <div style={{ display: 'flex', minHeight: 'calc(100vh - 64px)' }}>

        <aside style={{ width: 220, background: '#fff', borderRight: '1px solid #e2e8f0', padding: '24px 0', flexShrink: 0 }}>
          {SIDEBAR.map(item => (
            <button
              key={item.id}
              onClick={() => setAbaAtiva(item.id)}
              style={{
                display: 'flex', alignItems: 'center', gap: 12,
                width: '100%', padding: '12px 20px',
                background: abaAtiva === item.id ? '#eff6ff' : 'transparent',
                border: 'none',
                borderLeft: abaAtiva === item.id ? '3px solid #1d4ed8' : '3px solid transparent',
                color: abaAtiva === item.id ? '#1d4ed8' : '#64748b',
                fontWeight: abaAtiva === item.id ? 700 : 400,
                fontSize: 14, cursor: 'pointer', textAlign: 'left',
              }}
            >
              <span>{item.icon}</span>
              {item.label}
            </button>
          ))}
        </aside>

        <main style={{ flex: 1, padding: '32px 40px', overflowY: 'auto' }}>
          {msg && (
            <div style={{
              background: '#f0fdf4', border: '1px solid #bbf7d0',
              borderRadius: 10, padding: '12px 16px', marginBottom: 24,
              color: '#15803d', fontSize: 14, fontWeight: 600,
            }}>
              {msg}
            </div>
          )}

          {erros.length > 0 && (
            <div style={{
              background: '#fef2f2', border: '1px solid #fecaca',
              borderRadius: 10, padding: '14px 16px', marginBottom: 24,
            }}>
              <p style={{ fontSize: 14, fontWeight: 700, color: '#ef4444', marginBottom: 8 }}>
                Corrija os seguintes problemas antes de continuar:
              </p>
              <ul style={{ margin: 0, paddingLeft: 20 }}>
                {erros.map((e, i) => (
                  <li key={i} style={{ fontSize: 13, color: '#b91c1c', marginBottom: 4 }}>{e}</li>
                ))}
              </ul>
            </div>
          )}

          {abaAtiva === 'meus-eventos' && (
            <MeusEventos eventos={eventos} onCancelar={cancelarEvento} onExcluir={excluirEvento} />
          )}

          {abaAtiva === 'criar-evento' && (
            <CriarEventoForm
              form={form}
              tipos={tipos}
              salvando={salvando}
              imagemPreview={imagemPreview}
              onFormChange={setForm}
              onAbrirModalNovo={abrirModalNovo}
              onAbrirModalEditar={abrirModalEditar}
              onRemoverTipo={removerTipo}
              onImagemPreview={setImagemPreview}
              onSubmit={criar}
            />
          )}

          {abaAtiva === 'relatorios' && (
            <RelatoriosPanel
              eventos={eventos}
              eventoSelecionado={eventoRelatorio}
              tiposMetrica={tiposMetrica}
              analytics={analyticsData}
              carregando={carregandoMetricas}
              onSelecionarEvento={selecionarEventoRelatorio}
            />
          )}

          {abaAtiva === 'configuracoes' && (
            <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>⚙️</div>
              <p>Configurações em breve.</p>
            </div>
          )}
        </main>
      </div>

      {modalAberto && (
        <ModalTipoIngresso
          tipoInicial={tipoEditandoIndex !== null ? tipos[tipoEditandoIndex] : null}
          capacidadeEvento={Number(form.capacidade) || 0}
          quantidadeJaAlocada={tipos.reduce((s, t, idx) => idx !== tipoEditandoIndex ? s + (Number(t.quantidadeTotal) || 0) : s, 0)}
          onSalvar={salvarTipo}
          onFechar={fecharModal}
        />
      )}
    </>
  )
}

function MeusEventos({ eventos, onCancelar, onExcluir }: {
  eventos: Evento[]
  onCancelar: (id: number) => void
  onExcluir: (id: number) => void
}) {
  return (
    <>
      <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1e293b', marginBottom: 24 }}>Meus Eventos</h1>
      {eventos.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>
          <div style={{ fontSize: 48, marginBottom: 16 }}>📅</div>
          <p>Você ainda não criou nenhum evento.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {eventos.map(ev => (
            <div key={ev.id} style={{ background: '#fff', borderRadius: 12, padding: '16px 20px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)', display: 'flex', alignItems: 'center', gap: 16 }}>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
                  <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b' }}>{ev.nome}</h3>
                  <span style={{
                    fontSize: 11, fontWeight: 700, padding: '2px 10px', borderRadius: 20,
                    background: ev.status === 'ATIVO' ? '#dcfce7' : ev.status === 'CANCELADO' ? '#fee2e2' : '#fef3c7',
                    color: ev.status === 'ATIVO' ? '#16a34a' : ev.status === 'CANCELADO' ? '#ef4444' : '#b45309',
                  }}>
                    {ev.status}
                  </span>
                </div>
                <p style={{ fontSize: 13, color: '#64748b' }}>
                  🕐 {ev.dataHora ? new Date(ev.dataHora).toLocaleString('pt-BR') : ''} &nbsp;|&nbsp;
                  📍 {ev.local} &nbsp;|&nbsp;
                  👥 Cap: {ev.capacidade}
                </p>
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <Link to={`/eventos/${ev.id}`}>
                  <button style={{ background: '#eff6ff', color: '#1d4ed8', border: 'none', borderRadius: 8, padding: '8px 16px', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}>
                    Ver Detalhes
                  </button>
                </Link>
                {ev.status === 'ATIVO' && (
                  <button
                    onClick={() => onCancelar(ev.id)}
                    style={{ background: '#fef2f2', color: '#ef4444', border: 'none', borderRadius: 8, padding: '8px 16px', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}
                  >
                    Cancelar
                  </button>
                )}
                <button
                  onClick={() => onExcluir(ev.id)}
                  style={{ background: '#1e293b', color: '#fff', border: 'none', borderRadius: 8, padding: '8px 16px', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}
                  title="Excluir evento permanentemente"
                >
                  Excluir
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  )
}

interface CriarEventoFormProps {
  form: EventoForm
  tipos: TipoForm[]
  salvando: boolean
  imagemPreview: string | null
  onFormChange: (f: EventoForm) => void
  onAbrirModalNovo: () => void
  onAbrirModalEditar: (i: number) => void
  onRemoverTipo: (i: number) => void
  onImagemPreview: (url: string | null) => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}

function CriarEventoForm({
  form, tipos, salvando, imagemPreview,
  onFormChange, onAbrirModalNovo, onAbrirModalEditar, onRemoverTipo, onImagemPreview, onSubmit,
}: CriarEventoFormProps) {
  const set = (campo: keyof EventoForm) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    onFormChange({ ...form, [campo]: e.target.value })

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '12px 14px', border: '1px solid #e2e8f0',
    borderRadius: 8, fontSize: 14, color: '#1e293b', outline: 'none', background: '#fff',
  }

  const cardStyle: React.CSSProperties = {
    background: '#fff', borderRadius: 16, padding: 28,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24,
  }

  const precoRangeTipo = (tipo: TipoForm): string => {
    if (!tipo.lotes.length) return '—'
    const precos = tipo.lotes.map(l => parseFloat(l.preco.replace(',', '.')) || 0).filter(p => p > 0)
    if (!precos.length) return '—'
    const min = Math.min(...precos)
    const max = Math.max(...precos)
    if (min === max) return `R$ ${min.toFixed(2).replace('.', ',')}`
    return `R$ ${min.toFixed(2).replace('.', ',')} – R$ ${max.toFixed(2).replace('.', ',')}`
  }

  const capacidade = Number(form.capacidade) || 0
  const totalAlocado = tipos.reduce((s, t) => s + (Number(t.quantidadeTotal) || 0), 0)

  return (
    <>
      <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1e293b', marginBottom: 28 }}>Criar Evento</h1>
      <form onSubmit={onSubmit}>

        <div style={cardStyle}>
          <h2 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Informações Básicas</h2>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
              Nome do Evento <span style={{ color: '#ef4444' }}>*</span>
            </label>
            <input placeholder="ex: Festival de Música de Verão" value={form.nome} onChange={set('nome')} required style={inputStyle} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Categoria</label>
              <select value={form.categoria} onChange={set('categoria')} style={inputStyle}>
                <option value="">Selecionar Categoria</option>
                {CATEGORIAS.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
                Data e Horário <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input type="datetime-local" value={form.dataHora} onChange={set('dataHora')} required style={inputStyle} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
                Abertura dos Portões <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input type="datetime-local" value={form.aberturaPortoes} onChange={set('aberturaPortoes')} required style={inputStyle} />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
                Capacidade Total <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input type="number" min="1" value={form.capacidade} onChange={set('capacidade')} required style={inputStyle} />
              {capacidade > 0 && totalAlocado > 0 && (
                <p style={{ fontSize: 11, color: totalAlocado > capacidade ? '#ef4444' : '#64748b', marginTop: 4 }}>
                  {totalAlocado} de {capacidade} ingressos alocados nos tipos
                </p>
              )}
            </div>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
              Endereço do Local <span style={{ color: '#ef4444' }}>*</span>
            </label>
            <input placeholder="Comece a digitar o endereço..." value={form.local} onChange={set('local')} required style={inputStyle} />
          </div>

          <div style={{ background: '#e8f4fd', borderRadius: 12, height: 180, display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid #bfdbfe' }}>
            <div style={{ textAlign: 'center', color: '#64748b' }}>
              <div style={{ fontSize: 36, marginBottom: 8 }}>🗺️</div>
              <p style={{ fontSize: 13 }}>Mapa do local aparecerá aqui</p>
            </div>
          </div>

          <div style={{ marginTop: 16 }}>
            <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Descrição</label>
            <textarea
              placeholder="Descreva o evento..."
              value={form.descricao}
              onChange={set('descricao')}
              rows={4}
              style={{ ...inputStyle, resize: 'vertical' }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginTop: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Prazo de Reembolso (dias)</label>
              <input type="number" min="0" value={form.prazoReembolsoDias} onChange={set('prazoReembolsoDias')} style={inputStyle} />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>URL da Imagem de Capa</label>
              <input
                placeholder="https://..."
                value={form.imagemCapaUrl}
                onChange={e => { set('imagemCapaUrl')(e); onImagemPreview(e.target.value || null) }}
                style={inputStyle}
              />
            </div>
          </div>
        </div>

        {/* Tipos de Ingresso */}
        <div style={cardStyle}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <div>
              <h2 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 4 }}>Tipos de Ingresso</h2>
              <p style={{ fontSize: 13, color: '#94a3b8' }}>Configure categorias, benefícios e lotes de venda para cada tipo.</p>
            </div>
            <button
              type="button"
              onClick={onAbrirModalNovo}
              style={{
                background: '#1d4ed8', color: '#fff', border: 'none', borderRadius: 8,
                padding: '10px 18px', fontSize: 13, fontWeight: 700, cursor: 'pointer',
                display: 'flex', alignItems: 'center', gap: 6, whiteSpace: 'nowrap',
              }}
            >
              + Criar Tipo de Ingresso
            </button>
          </div>

          {tipos.length === 0 ? (
            <div style={{
              border: '2px dashed #e2e8f0', borderRadius: 12, padding: '36px 24px',
              textAlign: 'center', color: '#94a3b8',
            }}>
              <div style={{ fontSize: 32, marginBottom: 10 }}>🎟️</div>
              <p style={{ fontSize: 14, fontWeight: 600, marginBottom: 4 }}>Nenhum tipo de ingresso criado</p>
              <p style={{ fontSize: 13 }}>Clique em "Criar Tipo de Ingresso" para configurar categorias, preços e lotes.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {tipos.map((tipo, i) => (
                <div
                  key={i}
                  style={{
                    border: '1px solid #e2e8f0', borderRadius: 12, padding: '16px 20px',
                    display: 'flex', alignItems: 'flex-start', gap: 16,
                    background: '#fafbff', cursor: 'pointer',
                    transition: 'border-color 0.15s',
                  }}
                  onClick={() => onAbrirModalEditar(i)}
                  onMouseEnter={e => (e.currentTarget.style.borderColor = '#93c5fd')}
                  onMouseLeave={e => (e.currentTarget.style.borderColor = '#e2e8f0')}
                >
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                      <span style={{ fontSize: 15, fontWeight: 700, color: '#1e293b' }}>{tipo.nome || '(sem nome)'}</span>
                      <span style={{ fontSize: 12, fontWeight: 600, color: '#1d4ed8', background: '#eff6ff', borderRadius: 6, padding: '2px 8px' }}>
                        {tipo.lotes.length} {tipo.lotes.length === 1 ? 'lote' : 'lotes'}
                      </span>
                    </div>
                    <div style={{ display: 'flex', gap: 20, fontSize: 13, color: '#64748b' }}>
                      <span>💰 {precoRangeTipo(tipo)}</span>
                      <span>🎟 {Number(tipo.quantidadeTotal) || 0} ingressos</span>
                      {tipo.beneficios.filter(b => b.trim()).length > 0 && (
                        <span>✅ {tipo.beneficios.filter(b => b.trim()).length} {tipo.beneficios.filter(b => b.trim()).length === 1 ? 'benefício' : 'benefícios'}</span>
                      )}
                    </div>
                    {tipo.beneficios.filter(b => b.trim()).length > 0 && (
                      <div style={{ marginTop: 8, display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                        {tipo.beneficios.filter(b => b.trim()).slice(0, 4).map((b, j) => (
                          <span key={j} style={{ fontSize: 11, background: '#f0fdf4', color: '#16a34a', borderRadius: 6, padding: '2px 8px', fontWeight: 600 }}>
                            {b}
                          </span>
                        ))}
                        {tipo.beneficios.filter(b => b.trim()).length > 4 && (
                          <span style={{ fontSize: 11, color: '#94a3b8' }}>+{tipo.beneficios.filter(b => b.trim()).length - 4} mais</span>
                        )}
                      </div>
                    )}
                  </div>
                  <button
                    type="button"
                    onClick={e => { e.stopPropagation(); onRemoverTipo(i) }}
                    style={{ background: 'none', border: 'none', color: '#cbd5e1', fontSize: 18, cursor: 'pointer', padding: 4, flexShrink: 0 }}
                    title="Remover tipo"
                  >
                    🗑️
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Imagem de Capa */}
        <div style={cardStyle}>
          <h2 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Imagem de Capa</h2>
          {imagemPreview ? (
            <div style={{ position: 'relative', borderRadius: 12, overflow: 'hidden', height: 200 }}>
              <img src={imagemPreview} alt="Preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              <button
                type="button"
                onClick={() => { onImagemPreview(null); onFormChange({ ...form, imagemCapaUrl: '' }) }}
                style={{ position: 'absolute', top: 10, right: 10, background: 'rgba(0,0,0,0.6)', color: '#fff', border: 'none', borderRadius: '50%', width: 32, height: 32, cursor: 'pointer', fontSize: 16 }}
              >
                ✕
              </button>
            </div>
          ) : (
            <div style={{
              border: '2px dashed #cbd5e1', borderRadius: 12, height: 160,
              display: 'flex', flexDirection: 'column', alignItems: 'center',
              justifyContent: 'center', color: '#94a3b8', background: '#fafafa',
            }}>
              <span style={{ fontSize: 36, marginBottom: 10 }}>☁️</span>
              <p style={{ fontSize: 14, fontWeight: 600 }}>Clique para fazer upload ou arraste e solte</p>
              <p style={{ fontSize: 12, marginTop: 4 }}>SVG, PNG, JPG ou GIF (máx. 800×400px)</p>
              <p style={{ fontSize: 12, marginTop: 4 }}>Ou insira a URL acima</p>
            </div>
          )}
        </div>

        <button
          type="submit"
          disabled={salvando}
          style={{
            background: salvando ? '#93c5fd' : '#1d4ed8',
            color: '#fff', border: 'none', borderRadius: 12,
            padding: '16px 48px', fontSize: 16, fontWeight: 700,
            cursor: salvando ? 'default' : 'pointer', width: '100%',
          }}
        >
          {salvando ? 'Publicando evento...' : 'Publicar Evento'}
        </button>
      </form>
    </>
  )
}

interface ModalTipoIngressoProps {
  tipoInicial: TipoForm | null
  capacidadeEvento: number
  quantidadeJaAlocada: number
  onSalvar: (tipo: TipoForm) => void
  onFechar: () => void
}

function ModalTipoIngresso({ tipoInicial, capacidadeEvento, quantidadeJaAlocada, onSalvar, onFechar }: ModalTipoIngressoProps) {
  const [tipo, setTipo] = useState<TipoForm>(
    tipoInicial
      ? JSON.parse(JSON.stringify(tipoInicial))
      : { ...TIPO_INICIAL, beneficios: [], lotes: [{ nome: 'Lote 1', preco: '50,00', quantidade: '', dataInicio: '', dataFim: '' }] }
  )
  const [modalErros, setModalErros] = useState<string[]>([])

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '10px 12px', border: '1px solid #e2e8f0',
    borderRadius: 8, fontSize: 14, color: '#1e293b', outline: 'none', background: '#fff',
    boxSizing: 'border-box',
  }

  const setTipoField = (campo: keyof TipoForm, valor: unknown) =>
    setTipo(prev => ({ ...prev, [campo]: valor }))

  const adicionarBeneficio = () =>
    setTipoField('beneficios', [...tipo.beneficios, ''])

  const atualizarBeneficio = (i: number, valor: string) =>
    setTipoField('beneficios', tipo.beneficios.map((b, j) => j === i ? valor : b))

  const removerBeneficio = (i: number) =>
    setTipoField('beneficios', tipo.beneficios.filter((_, j) => j !== i))

  const adicionarLote = () =>
    setTipoField('lotes', [
      ...tipo.lotes,
      { nome: `Lote ${tipo.lotes.length + 1}`, preco: '0,00', quantidade: '', dataInicio: '', dataFim: '' },
    ])

  const removerLote = (i: number) => {
    if (tipo.lotes.length <= 1) return
    setTipoField('lotes', tipo.lotes.filter((_, j) => j !== i))
  }

  const atualizarLote = (i: number, campo: keyof LoteForm, valor: string) =>
    setTipoField('lotes', tipo.lotes.map((l, j) => j === i ? { ...l, [campo]: valor } : l))

  const qtdTotal = Number(tipo.quantidadeTotal) || 0
  const somaLotes = tipo.lotes.reduce((s, l) => s + (Number(l.quantidade) || 0), 0)
  const capacidadeDisponivel = capacidadeEvento > 0 ? capacidadeEvento - quantidadeJaAlocada : 0

  const handleSalvar = () => {
    const erros: string[] = []

    if (!tipo.nome.trim()) erros.push('Nome do tipo de ingresso é obrigatório.')

    const qtd = Number(tipo.quantidadeTotal)
    if (!qtd || qtd < 1) erros.push('Quantidade total de ingressos deve ser pelo menos 1.')

    if (capacidadeEvento > 0 && qtd > capacidadeDisponivel) {
      erros.push(`Quantidade total (${qtd}) excede a capacidade disponível do evento (${capacidadeDisponivel}).`)
    }

    const lotesPreenchidos = tipo.lotes.filter(l => l.nome.trim() || Number(l.quantidade) > 0 || l.preco.trim())
    const lotesValidos = lotesPreenchidos.filter(l => l.nome.trim() && Number(l.quantidade) > 0)

    if (lotesValidos.length === 0) erros.push('Adicione pelo menos um lote com nome e quantidade preenchidos.')

    lotesValidos.forEach(lote => {
      const preco = parseFloat(lote.preco.replace(',', '.'))
      if (isNaN(preco) || preco <= 0)
        erros.push(`Lote "${lote.nome}": preço deve ser maior que zero.`)
    })

    if (qtd && somaLotes > qtd)
      erros.push(`Soma das quantidades dos lotes (${somaLotes}) excede a quantidade total (${qtd}).`)

    if (erros.length > 0) {
      setModalErros(erros)
      return
    }

    setModalErros([])
    onSalvar(tipo)
  }

  return (
    <div
      style={{
        position: 'fixed', inset: 0, background: 'rgba(15,23,42,0.55)',
        zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center',
        padding: '20px',
      }}
      onClick={e => { if (e.target === e.currentTarget) onFechar() }}
    >
      <div style={{
        background: '#fff', borderRadius: 20, width: '100%', maxWidth: 640,
        maxHeight: '90vh', display: 'flex', flexDirection: 'column',
        boxShadow: '0 24px 60px rgba(0,0,0,0.18)',
      }}>
        {/* Header */}
        <div style={{
          padding: '24px 28px 20px', borderBottom: '1px solid #f1f5f9',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexShrink: 0,
        }}>
          <div>
            <h2 style={{ fontSize: 20, fontWeight: 800, color: '#1e293b', marginBottom: 2 }}>
              {tipoInicial ? 'Editar Tipo de Ingresso' : 'Novo Tipo de Ingresso'}
            </h2>
            <p style={{ fontSize: 13, color: '#94a3b8' }}>Configure nome, benefícios e lotes de venda.</p>
          </div>
          <button
            onClick={onFechar}
            style={{
              background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: 8,
              width: 36, height: 36, fontSize: 18, cursor: 'pointer', color: '#64748b',
              display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}
          >
            ✕
          </button>
        </div>

        {/* Body */}
        <div style={{ overflowY: 'auto', padding: '24px 28px', flex: 1 }}>

          {modalErros.length > 0 && (
            <div style={{
              background: '#fef2f2', border: '1px solid #fecaca',
              borderRadius: 10, padding: '12px 16px', marginBottom: 20,
            }}>
              <p style={{ fontSize: 13, fontWeight: 700, color: '#ef4444', marginBottom: 6 }}>Corrija os problemas abaixo:</p>
              <ul style={{ margin: 0, paddingLeft: 18 }}>
                {modalErros.map((e, i) => (
                  <li key={i} style={{ fontSize: 12, color: '#b91c1c', marginBottom: 3 }}>{e}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Informações básicas */}
          <section style={{ marginBottom: 28 }}>
            <h3 style={{ fontSize: 13, fontWeight: 700, color: '#475569', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 14 }}>
              Informações Básicas
            </h3>
            <div style={{ marginBottom: 14 }}>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
                Nome do Tipo <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input
                placeholder="ex: VIP, Pista, Camarote..."
                value={tipo.nome}
                onChange={e => setTipoField('nome', e.target.value)}
                style={inputStyle}
              />
            </div>

            <div style={{ marginBottom: 14 }}>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>
                Quantidade Total de Ingressos <span style={{ color: '#ef4444' }}>*</span>
              </label>
              <input
                type="number"
                min="1"
                max={capacidadeEvento > 0 ? capacidadeDisponivel : undefined}
                placeholder="ex: 200"
                value={tipo.quantidadeTotal}
                onChange={e => setTipoField('quantidadeTotal', e.target.value)}
                style={inputStyle}
              />
              {capacidadeEvento > 0 && (
                <p style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>
                  Disponível no evento: {capacidadeDisponivel} ingressos
                </p>
              )}
            </div>

            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Descrição</label>
              <textarea
                placeholder="Descreva o que está incluso neste tipo..."
                value={tipo.descricao}
                onChange={e => setTipoField('descricao', e.target.value)}
                rows={2}
                style={{ ...inputStyle, resize: 'vertical' }}
              />
            </div>
          </section>

          {/* Benefícios */}
          <section style={{ marginBottom: 28 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
              <h3 style={{ fontSize: 13, fontWeight: 700, color: '#475569', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                Benefícios Inclusos
              </h3>
              <button
                type="button"
                onClick={adicionarBeneficio}
                style={{ background: 'none', border: 'none', color: '#1d4ed8', fontSize: 13, fontWeight: 600, cursor: 'pointer' }}
              >
                + Adicionar
              </button>
            </div>
            {tipo.beneficios.length === 0 ? (
              <p style={{ fontSize: 13, color: '#cbd5e1', fontStyle: 'italic' }}>
                Nenhum benefício adicionado. Ex: "Open bar", "Área VIP", "Meet &amp; Greet".
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {tipo.beneficios.map((b, i) => (
                  <div key={i} style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <span style={{ color: '#22c55e', fontSize: 16, flexShrink: 0 }}>✓</span>
                    <input
                      placeholder="Descreva o benefício..."
                      value={b}
                      onChange={e => atualizarBeneficio(i, e.target.value)}
                      style={{ ...inputStyle, flex: 1 }}
                    />
                    <button
                      type="button"
                      onClick={() => removerBeneficio(i)}
                      style={{ background: 'none', border: 'none', color: '#cbd5e1', fontSize: 18, cursor: 'pointer', padding: 4, flexShrink: 0 }}
                    >
                      ✕
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>

          {/* Lotes */}
          <section>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
              <div>
                <h3 style={{ fontSize: 13, fontWeight: 700, color: '#475569', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: 2 }}>
                  Lotes de Venda
                </h3>
                <p style={{ fontSize: 12, color: '#94a3b8' }}>Cada lote tem seu próprio preço e período de vendas.</p>
              </div>
              <button
                type="button"
                onClick={adicionarLote}
                style={{ background: 'none', border: 'none', color: '#1d4ed8', fontSize: 13, fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap' }}
              >
                + Lote
              </button>
            </div>

            {/* Contador de uso */}
            {qtdTotal > 0 && (
              <div style={{
                background: somaLotes > qtdTotal ? '#fef2f2' : somaLotes === qtdTotal ? '#f0fdf4' : '#eff6ff',
                border: `1px solid ${somaLotes > qtdTotal ? '#fecaca' : somaLotes === qtdTotal ? '#bbf7d0' : '#bfdbfe'}`,
                borderRadius: 8, padding: '8px 14px', marginBottom: 14,
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
              }}>
                <span style={{ fontSize: 12, fontWeight: 600, color: somaLotes > qtdTotal ? '#ef4444' : somaLotes === qtdTotal ? '#16a34a' : '#1d4ed8' }}>
                  {somaLotes > qtdTotal
                    ? `Excedido: ${somaLotes} / ${qtdTotal} ingressos nos lotes`
                    : `${somaLotes} / ${qtdTotal} ingressos alocados nos lotes`}
                </span>
                <span style={{ fontSize: 11, color: '#94a3b8' }}>
                  {qtdTotal - somaLotes > 0 ? `${qtdTotal - somaLotes} ainda sem lote` : somaLotes === qtdTotal ? 'Todos alocados' : ''}
                </span>
              </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {tipo.lotes.map((lote, i) => {
                const somaOutros = tipo.lotes.reduce((s, l, idx) => idx !== i ? s + (Number(l.quantidade) || 0) : s, 0)
                const maxLote = qtdTotal > 0 ? qtdTotal - somaOutros : undefined

                return (
                  <div
                    key={i}
                    style={{
                      border: '1px solid #e2e8f0', borderRadius: 12, padding: '16px',
                      background: '#fafbff', position: 'relative',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                      <span style={{
                        fontSize: 12, fontWeight: 700, color: '#1d4ed8',
                        background: '#eff6ff', borderRadius: 6, padding: '3px 10px',
                      }}>
                        Lote {i + 1}
                      </span>
                      {tipo.lotes.length > 1 && (
                        <button
                          type="button"
                          onClick={() => removerLote(i)}
                          style={{ background: 'none', border: 'none', color: '#cbd5e1', fontSize: 16, cursor: 'pointer', padding: 0 }}
                          title="Remover lote"
                        >
                          🗑️
                        </button>
                      )}
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: 10, marginBottom: 10 }}>
                      <div>
                        <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#94a3b8', marginBottom: 4 }}>
                          Nome do Lote <span style={{ color: '#ef4444' }}>*</span>
                        </label>
                        <input
                          placeholder="ex: Lote 1, Early Bird..."
                          value={lote.nome}
                          onChange={e => atualizarLote(i, 'nome', e.target.value)}
                          style={inputStyle}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#94a3b8', marginBottom: 4 }}>
                          Preço (R$) <span style={{ color: '#ef4444' }}>*</span>
                        </label>
                        <input
                          placeholder="0,00"
                          value={lote.preco}
                          onChange={e => atualizarLote(i, 'preco', e.target.value)}
                          style={inputStyle}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#94a3b8', marginBottom: 4 }}>
                          Quantidade <span style={{ color: '#ef4444' }}>*</span>
                        </label>
                        <input
                          type="number"
                          min="1"
                          max={maxLote}
                          placeholder={maxLote !== undefined ? `máx ${maxLote}` : ''}
                          value={lote.quantidade}
                          onChange={e => atualizarLote(i, 'quantidade', e.target.value)}
                          style={{
                            ...inputStyle,
                            borderColor: maxLote !== undefined && Number(lote.quantidade) > maxLote ? '#ef4444' : '#e2e8f0',
                          }}
                        />
                        {maxLote !== undefined && (
                          <p style={{ fontSize: 10, color: Number(lote.quantidade) > maxLote ? '#ef4444' : '#94a3b8', marginTop: 2 }}>
                            máx: {maxLote}
                          </p>
                        )}
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
                      <div>
                        <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#94a3b8', marginBottom: 4 }}>
                          Início das Vendas <span style={{ fontWeight: 400, color: '#cbd5e1' }}>(opcional)</span>
                        </label>
                        <input
                          type="datetime-local"
                          value={lote.dataInicio}
                          onChange={e => atualizarLote(i, 'dataInicio', e.target.value)}
                          style={inputStyle}
                        />
                      </div>
                      <div>
                        <label style={{ display: 'block', fontSize: 11, fontWeight: 600, color: '#94a3b8', marginBottom: 4 }}>
                          Fim das Vendas <span style={{ fontWeight: 400, color: '#cbd5e1' }}>(opcional)</span>
                        </label>
                        <input
                          type="datetime-local"
                          value={lote.dataFim}
                          onChange={e => atualizarLote(i, 'dataFim', e.target.value)}
                          style={inputStyle}
                        />
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </section>
        </div>

        {/* Footer */}
        <div style={{
          padding: '20px 28px', borderTop: '1px solid #f1f5f9',
          display: 'flex', gap: 12, justifyContent: 'flex-end', flexShrink: 0,
        }}>
          <button
            type="button"
            onClick={onFechar}
            style={{
              background: '#f8fafc', color: '#64748b', border: '1px solid #e2e8f0',
              borderRadius: 10, padding: '10px 24px', fontSize: 14, fontWeight: 600, cursor: 'pointer',
            }}
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={handleSalvar}
            style={{
              background: '#1d4ed8', color: '#fff', border: 'none', borderRadius: 10,
              padding: '10px 28px', fontSize: 14, fontWeight: 700, cursor: 'pointer',
            }}
          >
            {tipoInicial ? 'Salvar Alterações' : 'Adicionar Tipo'}
          </button>
        </div>
      </div>
    </div>
  )
}

// ─── Painel de Relatórios ─────────────────────────────────────────────────────

interface RelatoriosPanelProps {
  eventos: Evento[]
  eventoSelecionado: number | null
  tiposMetrica: TipoMetrica[]
  analytics: AnalyticsData | null
  carregando: boolean
  onSelecionarEvento: (id: number) => void
}

function RelatoriosPanel({ eventos, eventoSelecionado, tiposMetrica, analytics, carregando, onSelecionarEvento }: RelatoriosPanelProps) {
  const evento = eventos.find(e => e.id === eventoSelecionado) ?? null

  const totalVendidos = analytics?.totalVendidos ?? tiposMetrica.reduce((s, t) => s + (t.quantidadeTotal - t.quantidadeDisponivel), 0)
  const totalCapacidade = analytics?.totalCapacidade ?? tiposMetrica.reduce((s, t) => s + t.quantidadeTotal, 0)
  const receitaRealizada = tiposMetrica.reduce((s, t) => s + (t.quantidadeTotal - t.quantidadeDisponivel) * t.preco, 0)
  const receitaPotencial = tiposMetrica.reduce((s, t) => s + t.quantidadeTotal * t.preco, 0)
  const taxaOcupacao = analytics?.taxaOcupacao ?? (totalCapacidade > 0 ? (totalVendidos / totalCapacidade) * 100 : 0)

  const cardMetrica: React.CSSProperties = {
    background: '#fff', borderRadius: 14, padding: '20px 24px',
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', flex: 1,
  }

  return (
    <>
      <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1e293b', marginBottom: 24 }}>Relatórios e Analytics</h1>

      {/* Seletor de evento */}
      <div style={{ background: '#fff', borderRadius: 14, padding: '20px 24px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24 }}>
        <label style={{ display: 'block', fontSize: 13, fontWeight: 700, color: '#475569', marginBottom: 10 }}>
          Selecionar Evento
        </label>
        {eventos.length === 0 ? (
          <p style={{ color: '#94a3b8', fontSize: 14 }}>Você não possui eventos criados ainda.</p>
        ) : (
          <select
            value={eventoSelecionado ?? ''}
            onChange={e => onSelecionarEvento(Number(e.target.value))}
            style={{ width: '100%', padding: '12px 14px', border: '1px solid #e2e8f0', borderRadius: 8, fontSize: 14, color: '#1e293b', outline: 'none' }}
          >
            <option value="">Escolha um evento para ver as métricas</option>
            {eventos.map(ev => (
              <option key={ev.id} value={ev.id}>
                {ev.nome} — {ev.dataHora ? new Date(ev.dataHora).toLocaleDateString('pt-BR') : ''} [{ev.status}]
              </option>
            ))}
          </select>
        )}
      </div>

      {carregando && (
        <div style={{ textAlign: 'center', padding: '40px 0', color: '#94a3b8' }}>Carregando métricas...</div>
      )}

      {!carregando && eventoSelecionado && evento && tiposMetrica.length === 0 && (
        <div style={{ textAlign: 'center', padding: '40px 0', color: '#94a3b8' }}>
          <div style={{ fontSize: 36, marginBottom: 12 }}>🎟️</div>
          <p>Nenhum tipo de ingresso encontrado para este evento.</p>
        </div>
      )}

      {!carregando && eventoSelecionado && tiposMetrica.length > 0 && (
        <>
          {/* Cards de métricas principais */}
          <div style={{ display: 'flex', gap: 16, marginBottom: 24 }}>
            <div style={cardMetrica}>
              <p style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>
                Taxa de Ocupação
              </p>
              <p style={{ fontSize: 32, fontWeight: 800, color: taxaOcupacao >= 80 ? '#16a34a' : taxaOcupacao >= 50 ? '#f59e0b' : '#1d4ed8' }}>
                {taxaOcupacao.toFixed(1)}%
              </p>
              <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
                {totalVendidos.toLocaleString('pt-BR')} / {totalCapacidade.toLocaleString('pt-BR')} ingressos
              </p>
              <div style={{ marginTop: 10, background: '#e2e8f0', borderRadius: 4, height: 6, overflow: 'hidden' }}>
                <div style={{ background: taxaOcupacao >= 80 ? '#16a34a' : taxaOcupacao >= 50 ? '#f59e0b' : '#1d4ed8', height: '100%', width: `${Math.min(taxaOcupacao, 100)}%`, borderRadius: 4 }} />
              </div>
            </div>

            <div style={cardMetrica}>
              <p style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>
                Receita Realizada
              </p>
              <p style={{ fontSize: 28, fontWeight: 800, color: '#16a34a' }}>{formatMoeda(receitaRealizada)}</p>
              <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
                de {formatMoeda(receitaPotencial)} potencial
              </p>
              {receitaPotencial > 0 && (
                <p style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
                  {((receitaRealizada / receitaPotencial) * 100).toFixed(1)}% da receita potencial
                </p>
              )}
            </div>

            <div style={cardMetrica}>
              <p style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>
                Ingressos Disponíveis
              </p>
              <p style={{ fontSize: 32, fontWeight: 800, color: '#1e293b' }}>
                {tiposMetrica.reduce((s, t) => s + t.quantidadeDisponivel, 0).toLocaleString('pt-BR')}
              </p>
              <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>ingressos ainda à venda</p>
            </div>

            <div style={cardMetrica}>
              <p style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>
                Tipos de Ingresso
              </p>
              <p style={{ fontSize: 32, fontWeight: 800, color: '#1d4ed8' }}>{tiposMetrica.length}</p>
              <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>categorias configuradas</p>
            </div>
          </div>

          {/* Cards secundários — dados do analytics backend */}
          {analytics && (
            <div style={{ display: 'flex', gap: 16, marginBottom: 24 }}>
              <div style={cardMetrica}>
                <p style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>
                  Ingressos Revendidos
                </p>
                <p style={{ fontSize: 32, fontWeight: 800, color: '#7c3aed' }}>{analytics.totalRevendidos}</p>
                <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
                  {analytics.taxaRevenda.toFixed(1)}% dos vendidos
                </p>
              </div>
              <div style={cardMetrica}>
                <p style={{ fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8 }}>
                  Avaliação Média
                </p>
                <p style={{ fontSize: 32, fontWeight: 800, color: '#f59e0b' }}>
                  {analytics.totalAvaliacoes > 0 ? analytics.mediaAvaliacao.toFixed(1) : '—'}
                </p>
                <p style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
                  {analytics.totalAvaliacoes > 0 ? `${analytics.totalAvaliacoes} avaliações` : 'Sem avaliações ainda'}
                </p>
                {analytics.totalAvaliacoes > 0 && (
                  <div style={{ display: 'flex', gap: 2, marginTop: 6 }}>
                    {[1,2,3,4,5].map(i => (
                      <span key={i} style={{ color: i <= Math.round(analytics.mediaAvaliacao) ? '#f59e0b' : '#e2e8f0', fontSize: 14 }}>★</span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Breakdown por tipo */}
          <div style={{ background: '#fff', borderRadius: 14, padding: '24px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24 }}>
            <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Ocupação por Tipo de Ingresso</h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              {tiposMetrica.map(t => {
                const vendidos = t.quantidadeTotal - t.quantidadeDisponivel
                const taxa = t.quantidadeTotal > 0 ? (vendidos / t.quantidadeTotal) * 100 : 0
                const receita = vendidos * t.preco
                return (
                  <div key={t.id}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: 6 }}>
                      <div>
                        <span style={{ fontWeight: 700, color: '#1e293b', fontSize: 14 }}>{t.nome}</span>
                        <span style={{ marginLeft: 10, fontSize: 12, color: '#64748b' }}>
                          {vendidos} / {t.quantidadeTotal} vendidos · {formatMoeda(t.preco)} cada
                        </span>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <span style={{ fontWeight: 700, color: '#16a34a', fontSize: 14 }}>{formatMoeda(receita)}</span>
                        <span style={{ marginLeft: 8, fontSize: 12, color: '#94a3b8' }}>{taxa.toFixed(1)}%</span>
                      </div>
                    </div>
                    <div style={{ background: '#e2e8f0', borderRadius: 4, height: 8, overflow: 'hidden' }}>
                      <div style={{
                        background: taxa >= 80 ? '#16a34a' : taxa >= 50 ? '#f59e0b' : '#1d4ed8',
                        height: '100%', width: `${Math.min(taxa, 100)}%`, borderRadius: 4,
                        transition: 'width 0.4s ease',
                      }} />
                    </div>
                  </div>
                )
              })}
            </div>
          </div>

          {/* Resumo financeiro */}
          <div style={{ background: '#fff', borderRadius: 14, padding: '24px', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
            <h2 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b', marginBottom: 16 }}>Resumo Financeiro</h2>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #f1f5f9' }}>
                  <th style={{ textAlign: 'left', padding: '10px 0', fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase' }}>Tipo</th>
                  <th style={{ textAlign: 'right', padding: '10px 0', fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase' }}>Preço</th>
                  <th style={{ textAlign: 'right', padding: '10px 0', fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase' }}>Vendidos</th>
                  <th style={{ textAlign: 'right', padding: '10px 0', fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase' }}>Receita</th>
                  <th style={{ textAlign: 'right', padding: '10px 0', fontSize: 12, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase' }}>Potencial</th>
                </tr>
              </thead>
              <tbody>
                {tiposMetrica.map(t => {
                  const vendidos = t.quantidadeTotal - t.quantidadeDisponivel
                  return (
                    <tr key={t.id} style={{ borderBottom: '1px solid #f8fafc' }}>
                      <td style={{ padding: '12px 0', fontSize: 14, fontWeight: 600, color: '#1e293b' }}>{t.nome}</td>
                      <td style={{ padding: '12px 0', fontSize: 14, color: '#64748b', textAlign: 'right' }}>{formatMoeda(t.preco)}</td>
                      <td style={{ padding: '12px 0', fontSize: 14, color: '#1e293b', textAlign: 'right' }}>{vendidos}</td>
                      <td style={{ padding: '12px 0', fontSize: 14, fontWeight: 700, color: '#16a34a', textAlign: 'right' }}>{formatMoeda(vendidos * t.preco)}</td>
                      <td style={{ padding: '12px 0', fontSize: 14, color: '#94a3b8', textAlign: 'right' }}>{formatMoeda(t.quantidadeTotal * t.preco)}</td>
                    </tr>
                  )
                })}
              </tbody>
              <tfoot>
                <tr style={{ borderTop: '2px solid #e2e8f0' }}>
                  <td style={{ padding: '14px 0', fontWeight: 800, color: '#1e293b' }}>Total</td>
                  <td />
                  <td style={{ padding: '14px 0', fontWeight: 800, color: '#1e293b', textAlign: 'right' }}>{totalVendidos}</td>
                  <td style={{ padding: '14px 0', fontWeight: 800, color: '#16a34a', textAlign: 'right' }}>{formatMoeda(receitaRealizada)}</td>
                  <td style={{ padding: '14px 0', fontWeight: 800, color: '#94a3b8', textAlign: 'right' }}>{formatMoeda(receitaPotencial)}</td>
                </tr>
              </tfoot>
            </table>
            <p style={{ fontSize: 11, color: '#cbd5e1', marginTop: 12 }}>
              * Velocidade de vendas ao longo do tempo e perfil detalhado de compradores requerem módulo avançado de analytics.
            </p>
          </div>
        </>
      )}
    </>
  )
}
