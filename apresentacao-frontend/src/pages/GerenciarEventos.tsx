import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { eventoService, tipoIngressoService } from '../services/api'
import Navbar from '../components/Navbar'
import { USUARIO_ID, CATEGORIAS } from '../constants'

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

interface TipoForm {
  nome: string
  quantidade: string
  preco: string
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

export default function GerenciarEventos() {
  const location = useLocation()
  const abaInicial: Aba = (location.state as { aba?: Aba } | null)?.aba ?? 'criar-evento'

  const [abaAtiva, setAbaAtiva] = useState<Aba>(abaInicial)
  const [eventos, setEventos] = useState<Evento[]>([])
  const [form, setForm] = useState<EventoForm>(FORM_INICIAL)
  const [tipos, setTipos] = useState<TipoForm[]>([{ nome: 'Ingresso Geral', quantidade: '500', preco: '50,00' }])
  const [msg, setMsg] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [imagemPreview, setImagemPreview] = useState<string | null>(null)

  const carregarEventos = () => {
    eventoService.listar(USUARIO_ID).then(r => setEventos(r.data)).catch(() => {})
  }

  useEffect(() => { carregarEventos() }, [])

  const adicionarTipo = () => {
    setTipos(prev => [...prev, { nome: '', quantidade: '100', preco: '0,00' }])
  }

  const removerTipo = (index: number) => {
    setTipos(prev => prev.filter((_, i) => i !== index))
  }

  const atualizarTipo = (index: number, campo: keyof TipoForm, valor: string) => {
    setTipos(prev => prev.map((t, i) => i === index ? { ...t, [campo]: valor } : t))
  }

  const criar = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    setSalvando(true)
    setMsg('')
    try {
      const dados = {
        nome: form.nome,
        dataHora: form.dataHora,
        local: form.local,
        descricao: form.descricao,
        capacidade: Number(form.capacidade),
        imagemCapaUrl: form.imagemCapaUrl || null,
        prazoReembolsoDias: Number(form.prazoReembolsoDias),
        aberturaPortoes: form.aberturaPortoes,
        categoria: form.categoria || null,
      }
      const res = await eventoService.criar(USUARIO_ID, dados)
      const novoEventoId = res.data.id

      for (const tipo of tipos) {
        if (!tipo.nome.trim()) continue
        const precoNum = parseFloat(tipo.preco.replace(',', '.'))
        await tipoIngressoService.criar(novoEventoId, USUARIO_ID, {
          nome: tipo.nome,
          preco: isNaN(precoNum) ? 0 : precoNum,
          quantidade: Number(tipo.quantidade) || 0,
          descricao: null,
        })
      }

      setMsg('Evento criado com sucesso!')
      setForm(FORM_INICIAL)
      setTipos([{ nome: 'Ingresso Geral', quantidade: '500', preco: '50,00' }])
      setImagemPreview(null)
      carregarEventos()
      setAbaAtiva('meus-eventos')
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } }
      setMsg(e.response?.data?.message ?? 'Erro ao criar evento.')
    } finally {
      setSalvando(false)
    }
  }

  const cancelarEvento = async (id: number) => {
    if (!confirm('Cancelar este evento? Todos os ingressos serão reembolsados.')) return
    try {
      await eventoService.cancelar(id, USUARIO_ID)
      setMsg('Evento cancelado.')
      carregarEventos()
    } catch {
      setMsg('Erro ao cancelar evento.')
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

        {/* Sidebar */}
        <aside style={{ width: 220, background: '#fff', borderRight: '1px solid #e2e8f0', padding: '24px 0', flexShrink: 0 }}>
          {SIDEBAR.map(item => (
            <button
              key={item.id}
              onClick={() => setAbaAtiva(item.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                width: '100%',
                padding: '12px 20px',
                background: abaAtiva === item.id ? '#eff6ff' : 'transparent',
                border: 'none',
                borderLeft: abaAtiva === item.id ? '3px solid #1d4ed8' : '3px solid transparent',
                color: abaAtiva === item.id ? '#1d4ed8' : '#64748b',
                fontWeight: abaAtiva === item.id ? 700 : 400,
                fontSize: 14,
                cursor: 'pointer',
                textAlign: 'left',
              }}
            >
              <span>{item.icon}</span>
              {item.label}
            </button>
          ))}
        </aside>

        {/* Conteúdo */}
        <main style={{ flex: 1, padding: '32px 40px', overflowY: 'auto' }}>
          {msg && (
            <div style={{
              background: msg.includes('sucesso') ? '#f0fdf4' : '#fef2f2',
              border: `1px solid ${msg.includes('sucesso') ? '#bbf7d0' : '#fecaca'}`,
              borderRadius: 10, padding: '12px 16px', marginBottom: 24,
              color: msg.includes('sucesso') ? '#15803d' : '#ef4444',
              fontSize: 14, fontWeight: 600,
            }}>
              {msg}
            </div>
          )}

          {abaAtiva === 'meus-eventos' && (
            <MeusEventos eventos={eventos} onCancelar={cancelarEvento} />
          )}

          {abaAtiva === 'criar-evento' && (
            <CriarEventoForm
              form={form}
              tipos={tipos}
              salvando={salvando}
              imagemPreview={imagemPreview}
              onFormChange={setForm}
              onAdicionarTipo={adicionarTipo}
              onRemoverTipo={removerTipo}
              onAtualizarTipo={atualizarTipo}
              onImagemPreview={setImagemPreview}
              onSubmit={criar}
            />
          )}

          {abaAtiva === 'relatorios' && (
            <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>📊</div>
              <p>Relatórios em breve.</p>
            </div>
          )}

          {abaAtiva === 'configuracoes' && (
            <div style={{ textAlign: 'center', padding: '64px 0', color: '#94a3b8' }}>
              <div style={{ fontSize: 48, marginBottom: 16 }}>⚙️</div>
              <p>Configurações em breve.</p>
            </div>
          )}
        </main>
      </div>
    </>
  )
}

function MeusEventos({ eventos, onCancelar }: { eventos: Evento[]; onCancelar: (id: number) => void }) {
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
  onAdicionarTipo: () => void
  onRemoverTipo: (i: number) => void
  onAtualizarTipo: (i: number, campo: keyof TipoForm, valor: string) => void
  onImagemPreview: (url: string | null) => void
  onSubmit: (e: React.FormEvent<HTMLFormElement>) => void
}

function CriarEventoForm({
  form, tipos, salvando, imagemPreview,
  onFormChange, onAdicionarTipo, onRemoverTipo, onAtualizarTipo, onImagemPreview, onSubmit,
}: CriarEventoFormProps) {
  const set = (campo: keyof EventoForm) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    onFormChange({ ...form, [campo]: e.target.value })

  const inputStyle: React.CSSProperties = {
    width: '100%', padding: '12px 14px', border: '1px solid #e2e8f0',
    borderRadius: 8, fontSize: 14, color: '#1e293b', outline: 'none',
    background: '#fff',
  }

  const cardStyle: React.CSSProperties = {
    background: '#fff', borderRadius: 16, padding: 28,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)', marginBottom: 24,
  }

  return (
    <>
      <h1 style={{ fontSize: 26, fontWeight: 800, color: '#1e293b', marginBottom: 28 }}>Criar Evento</h1>
      <form onSubmit={onSubmit}>
        {/* Informações Básicas */}
        <div style={cardStyle}>
          <h2 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Informações Básicas</h2>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Nome do Evento</label>
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
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Data e Horário</label>
              <input type="datetime-local" value={form.dataHora} onChange={set('dataHora')} required style={inputStyle} />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 16 }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Abertura dos Portões</label>
              <input type="datetime-local" value={form.aberturaPortoes} onChange={set('aberturaPortoes')} required style={inputStyle} />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Capacidade Total</label>
              <input type="number" min="1" value={form.capacidade} onChange={set('capacidade')} required style={inputStyle} />
            </div>
          </div>

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: 'block', fontSize: 12, fontWeight: 600, color: '#64748b', marginBottom: 6 }}>Endereço do Local</label>
            <input placeholder="Comece a digitar o endereço..." value={form.local} onChange={set('local')} required style={inputStyle} />
          </div>

          {/* Mapa placeholder */}
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

        {/* Gerenciamento de Ingressos */}
        <div style={cardStyle}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <h2 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b' }}>Gerenciamento de Ingressos</h2>
            <button
              type="button"
              onClick={onAdicionarTipo}
              style={{ background: 'none', border: 'none', color: '#1d4ed8', fontSize: 14, fontWeight: 600, cursor: 'pointer' }}
            >
              + Adicionar Tipo de Ingresso
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            {tipos.map((tipo, i) => (
              <div key={i} style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                <input
                  placeholder="Nome do ingresso"
                  value={tipo.nome}
                  onChange={e => onAtualizarTipo(i, 'nome', e.target.value)}
                  style={{ ...inputStyle, flex: 2 }}
                />
                <input
                  placeholder="Quantidade"
                  type="number"
                  min="0"
                  value={tipo.quantidade}
                  onChange={e => onAtualizarTipo(i, 'quantidade', e.target.value)}
                  style={{ ...inputStyle, flex: 1 }}
                />
                <input
                  placeholder="Preço"
                  value={tipo.preco}
                  onChange={e => onAtualizarTipo(i, 'preco', e.target.value)}
                  style={{ ...inputStyle, flex: 1 }}
                />
                <button
                  type="button"
                  onClick={() => onRemoverTipo(i)}
                  style={{ background: 'none', border: 'none', color: '#94a3b8', fontSize: 20, cursor: 'pointer', padding: '0 4px', flexShrink: 0 }}
                  title="Remover"
                >
                  🗑️
                </button>
              </div>
            ))}
          </div>
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
              border: '2px dashed #cbd5e1',
              borderRadius: 12,
              height: 160,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#94a3b8',
              cursor: 'pointer',
              background: '#fafafa',
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
            color: '#fff',
            border: 'none',
            borderRadius: 12,
            padding: '16px 48px',
            fontSize: 16,
            fontWeight: 700,
            cursor: salvando ? 'default' : 'pointer',
            width: '100%',
          }}
        >
          {salvando ? 'Criando evento...' : 'Publicar Evento'}
        </button>
      </form>
    </>
  )
}
