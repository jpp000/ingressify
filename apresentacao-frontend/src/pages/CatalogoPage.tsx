import { useEffect, useState, type CSSProperties, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { eventoService } from '../services/api'
import Navbar from '../components/Navbar'
import { CATEGORIAS, formatMoeda, formatDataBadge, corCategoria } from '../constants'
import { useAuth } from '../context/AuthContext'

interface EventoCatalogo {
  id: number
  nome: string
  dataHora: string
  local: string
  imagemCapaUrl?: string
  precoMinimo?: number
  mediaAvaliacao: number
  temEstoquePrimario: boolean
  temRevendaDisponivel: boolean
  categoria?: string
}

export default function CatalogoPage() {
  const { isComprador, isOrganizador, isOperadorPorta } = useAuth()
  const [searchParams] = useSearchParams()
  const [eventos, setEventos] = useState<EventoCatalogo[]>([])
  const [carregando, setCarregando] = useState(true)
  const [categoriasSelecionadas, setCategoriasSelecionadas] = useState<string[]>([])
  const [filtroPreco, setFiltroPreco] = useState<'todos' | 'gratis' | 'ate50'>('todos')
  const [busca, setBusca] = useState(searchParams.get('busca') ?? '')
  const [cidade, setCidade] = useState('')
  const [pagina, setPagina] = useState(1)
  const POR_PAGINA = 6

  const carregarEventos = () => {
    setCarregando(true)
    const params: Record<string, string | number> = {}
    if (busca) params.nome = busca
    if (cidade) params.cidade = cidade
    if (categoriasSelecionadas.length === 1) params.categoria = categoriasSelecionadas[0]
    if (filtroPreco === 'gratis') params.precoMax = 0
    if (filtroPreco === 'ate50') params.precoMax = 50

    eventoService.catalogo(params)
      .then(r => {
        setEventos(r.data)
        setPagina(1)
      })
      .catch(() => setEventos([]))
      .finally(() => setCarregando(false))
  }

  useEffect(() => { carregarEventos() }, [categoriasSelecionadas, filtroPreco])
  useEffect(() => {
    const buscaParam = searchParams.get('busca') ?? ''
    setBusca(buscaParam)
  }, [searchParams])

  const toggleCategoria = (cat: string) => {
    setCategoriasSelecionadas(prev =>
      prev.includes(cat) ? prev.filter(c => c !== cat) : [...prev, cat]
    )
  }

  const handleBuscarHero = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    carregarEventos()
  }

  const destaques = eventos.slice(0, 2)
  const proximos = eventos.slice(2, 2 + pagina * POR_PAGINA)
  const temMais = eventos.length > 2 + pagina * POR_PAGINA

  const getEventoImagem = (e: EventoCatalogo) =>
    e.imagemCapaUrl || `https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&q=60`

  return (
    <>
      <Navbar />

      {/* Hero */}
      <div style={{
        background: 'linear-gradient(135deg, #0f172a 0%, #1e3a8a 50%, #1d4ed8 100%)',
        padding: '72px 24px 56px',
        textAlign: 'center',
        position: 'relative',
        overflow: 'hidden',
      }}>
        <div style={{ position: 'absolute', inset: 0, backgroundImage: 'url(https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=1200&q=50)', backgroundSize: 'cover', backgroundPosition: 'center', opacity: 0.15 }} />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <h1 style={{ color: '#fff', fontSize: 40, fontWeight: 800, lineHeight: 1.2, marginBottom: 12 }}>
            Encontre Sua Próxima<br />Experiência
          </h1>
          <p style={{ color: '#93c5fd', fontSize: 16, marginBottom: 32 }}>
            Descubra eventos ao vivo inesquecíveis, garanta seu lugar e experimente o momento.
          </p>

          <form onSubmit={handleBuscarHero} style={{ display: 'flex', maxWidth: 820, margin: '0 auto', background: '#fff', borderRadius: 12, overflow: 'hidden', boxShadow: '0 8px 32px rgba(0,0,0,0.2)' }}>
            <div style={{ flex: 1, display: 'flex', alignItems: 'center', padding: '0 16px', borderRight: '1px solid #e2e8f0' }}>
              <span style={{ color: '#94a3b8', marginRight: 8 }}>🔍</span>
              <input
                placeholder="Nome do evento ou artista"
                value={busca}
                onChange={e => setBusca(e.target.value)}
                style={{ border: 'none', outline: 'none', fontSize: 14, color: '#1e293b', width: '100%', padding: '14px 0' }}
              />
            </div>
            <div style={{ flex: 1, display: 'flex', alignItems: 'center', padding: '0 16px', borderRight: '1px solid #e2e8f0' }}>
              <span style={{ color: '#94a3b8', marginRight: 8 }}>📍</span>
              <input
                placeholder="Cidade ou local"
                value={cidade}
                onChange={e => setCidade(e.target.value)}
                style={{ border: 'none', outline: 'none', fontSize: 14, color: '#1e293b', width: '100%', padding: '14px 0' }}
              />
            </div>
            <button
              type="submit"
              style={{ background: '#1d4ed8', color: '#fff', border: 'none', padding: '0 32px', fontSize: 15, fontWeight: 600, cursor: 'pointer' }}
            >
              Buscar
            </button>
          </form>
        </div>
      </div>

      {/* Acesso rápido */}
      <div style={{ background: '#fff', borderBottom: '1px solid #e2e8f0', padding: '16px 24px' }}>
        <div style={{ maxWidth: 1280, margin: '0 auto', display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
          <span style={{ fontSize: 13, color: '#64748b', fontWeight: 600, marginRight: 4 }}>Acesso rápido:</span>
          <Link to="/sorteios" style={chipStyle('#16a34a')}>🎲 Sorteios</Link>
          {isComprador() && <Link to="/revendas" style={chipStyle('#d97706')}>🔄 Revendas</Link>}
          {isComprador() && <Link to="/meus-ingressos" style={chipStyle('#1d4ed8')}>🎟️ Carteira</Link>}
          {isComprador() && <Link to="/saldo" style={chipStyle('#0284c7')}>💳 Saldo</Link>}
          <Link to="/mapa-assentos" style={chipStyle('#7c3aed')}>💺 Mapa de Assentos</Link>
          {isOrganizador() && <Link to="/gerenciar" style={chipStyle('#1e3a8a')}>📅 Meus Eventos</Link>}
          {(isOrganizador() || isOperadorPorta()) && <Link to="/check-in" style={chipStyle('#0f766e')}>🎫 Check-in</Link>}
        </div>
      </div>

      {/* Conteúdo */}
      <div style={{ maxWidth: 1280, margin: '0 auto', padding: '32px 24px', display: 'flex', gap: 28 }}>

        {/* Sidebar */}
        <aside style={{ width: 220, flexShrink: 0 }}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 1px 3px rgba(0,0,0,0.06)', marginBottom: 16 }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, color: '#1e293b', marginBottom: 14 }}>Categorias</h3>
            {CATEGORIAS.map(cat => (
              <label key={cat} style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', marginBottom: 10 }}>
                <input
                  type="checkbox"
                  checked={categoriasSelecionadas.includes(cat)}
                  onChange={() => toggleCategoria(cat)}
                  style={{ width: 16, height: 16, accentColor: '#1d4ed8' }}
                />
                <span style={{ fontSize: 14, color: '#475569' }}>{cat}</span>
              </label>
            ))}
          </div>

          <div style={{ background: '#fff', borderRadius: 12, padding: 20, boxShadow: '0 1px 3px rgba(0,0,0,0.06)' }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, color: '#1e293b', marginBottom: 14 }}>Categorias</h3>
            {(['todos', 'gratis', 'ate50'] as const).map(op => (
              <label key={op} style={{ display: 'flex', alignItems: 'center', gap: 10, cursor: 'pointer', marginBottom: 10 }}>
                <input
                  type="radio"
                  name="filtroPreco"
                  checked={filtroPreco === op}
                  onChange={() => setFiltroPreco(op)}
                  style={{ width: 16, height: 16, accentColor: '#1d4ed8' }}
                />
                <span style={{ fontSize: 14, color: '#475569' }}>
                  {op === 'todos' ? 'Qualquer preço' : op === 'gratis' ? 'Grátis' : 'Abaixo de R$ 50'}
                </span>
              </label>
            ))}
          </div>
        </aside>

        {/* Lista */}
        <main style={{ flex: 1, minWidth: 0 }}>
          {carregando ? (
            <div style={{ textAlign: 'center', padding: '48px 0', color: '#94a3b8' }}>Carregando eventos...</div>
          ) : (
            <>
              {/* Destaques */}
              {destaques.length > 0 && (
                <section style={{ marginBottom: 40 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
                    <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b' }}>Eventos em Destaque</h2>
                    <Link to="/" style={{ color: '#1d4ed8', fontSize: 14, fontWeight: 600 }}>Ver todos →</Link>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 20 }}>
                    {destaques.map(e => (
                      <CardDestaque key={e.id} evento={e} getImagem={getEventoImagem} />
                    ))}
                  </div>
                </section>
              )}

              {/* Próximos */}
              {proximos.length > 0 && (
                <section>
                  <h2 style={{ fontSize: 20, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Próximos Eventos</h2>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 20 }}>
                    {proximos.map(e => (
                      <CardProximo key={e.id} evento={e} getImagem={getEventoImagem} />
                    ))}
                  </div>
                </section>
              )}

              {eventos.length === 0 && (
                <div style={{ textAlign: 'center', padding: '48px 0', color: '#94a3b8' }}>
                  Nenhum evento encontrado.
                </div>
              )}

              {temMais && (
                <div style={{ textAlign: 'center', marginTop: 32 }}>
                  <button
                    onClick={() => setPagina(p => p + 1)}
                    style={{
                      background: '#fff',
                      border: '1px solid #e2e8f0',
                      borderRadius: 8,
                      padding: '12px 32px',
                      fontSize: 14,
                      fontWeight: 600,
                      color: '#475569',
                      cursor: 'pointer',
                    }}
                  >
                    Carregar Mais Eventos
                  </button>
                </div>
              )}
            </>
          )}
        </main>
      </div>
    </>
  )
}

function chipStyle(cor: string): CSSProperties {
  return {
    background: `${cor}11`,
    border: `1px solid ${cor}44`,
    color: cor,
    borderRadius: 20,
    padding: '6px 14px',
    fontSize: 13,
    fontWeight: 600,
    textDecoration: 'none',
  }
}

function CardDestaque({ evento, getImagem }: { evento: EventoCatalogo; getImagem: (e: EventoCatalogo) => string }) {
  const { dia, mes } = formatDataBadge(evento.dataHora)
  const esgotado = !evento.temEstoquePrimario && !evento.temRevendaDisponivel

  return (
    <Link to={`/eventos/${evento.id}`} style={{ display: 'block' }}>
      <div style={{
        background: '#fff',
        borderRadius: 12,
        overflow: 'hidden',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        transition: 'transform 0.15s, box-shadow 0.15s',
        cursor: 'pointer',
      }}>
        <div style={{ position: 'relative', height: 180 }}>
          <img src={getImagem(evento)} alt={evento.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          <div style={{
            position: 'absolute',
            inset: 0,
            background: 'linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 60%)',
          }} />
          <div style={{
            position: 'absolute',
            top: 12,
            left: 12,
            background: 'rgba(0,0,0,0.7)',
            color: '#fff',
            borderRadius: 8,
            padding: '4px 10px',
            textAlign: 'center',
            minWidth: 44,
          }}>
            <div style={{ fontSize: 18, fontWeight: 700, lineHeight: 1 }}>{dia}</div>
            <div style={{ fontSize: 10, fontWeight: 600, letterSpacing: 0.5 }}>{mes}</div>
          </div>
          {esgotado && (
            <div style={{ position: 'absolute', top: 12, right: 12 }}>
              <span style={{ background: '#ef4444', color: '#fff', fontSize: 11, fontWeight: 700, padding: '4px 10px', borderRadius: 20 }}>Esgotado</span>
            </div>
          )}
          {evento.categoria && !esgotado && (
            <div style={{ position: 'absolute', top: 12, right: 12 }}>
              <span style={{ background: corCategoria(evento.categoria), color: '#fff', fontSize: 11, fontWeight: 600, padding: '4px 10px', borderRadius: 20 }}>
                {evento.categoria}
              </span>
            </div>
          )}
        </div>
        <div style={{ padding: '14px 16px 16px' }}>
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#1e293b', marginBottom: 6, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {evento.nome}
          </h3>
          <p style={{ fontSize: 13, color: '#64748b', marginBottom: 12 }}>📍 {evento.local}</p>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: 14, color: '#475569', fontWeight: 500 }}>
              {esgotado ? <span style={{ color: '#ef4444' }}>Esgotado</span>
                : evento.precoMinimo != null
                  ? `A partir de ${formatMoeda(evento.precoMinimo)}`
                  : ''}
            </span>
            <button style={{
              background: esgotado ? '#94a3b8' : '#1d4ed8',
              color: '#fff',
              border: 'none',
              borderRadius: 8,
              padding: '7px 16px',
              fontSize: 13,
              fontWeight: 600,
              cursor: esgotado ? 'default' : 'pointer',
            }}>
              {esgotado ? 'Esgotado' : 'Ver Detalhes'}
            </button>
          </div>
        </div>
      </div>
    </Link>
  )
}

function CardProximo({ evento, getImagem }: { evento: EventoCatalogo; getImagem: (e: EventoCatalogo) => string }) {
  const { dia, mes } = formatDataBadge(evento.dataHora)
  const esgotado = !evento.temEstoquePrimario && !evento.temRevendaDisponivel

  return (
    <Link to={`/eventos/${evento.id}`} style={{ display: 'block' }}>
      <div style={{
        background: '#fff',
        borderRadius: 12,
        overflow: 'hidden',
        boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
        cursor: 'pointer',
      }}>
        <div style={{ position: 'relative', height: 140 }}>
          <img src={getImagem(evento)} alt={evento.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          <div style={{
            position: 'absolute',
            top: 10,
            left: 10,
            background: 'rgba(0,0,0,0.7)',
            color: '#fff',
            borderRadius: 6,
            padding: '3px 8px',
            textAlign: 'center',
            minWidth: 38,
          }}>
            <div style={{ fontSize: 15, fontWeight: 700, lineHeight: 1 }}>{dia}</div>
            <div style={{ fontSize: 9, fontWeight: 600, letterSpacing: 0.5 }}>{mes}</div>
          </div>
          {esgotado && (
            <div style={{ position: 'absolute', top: 10, right: 10 }}>
              <span style={{ background: '#ef4444', color: '#fff', fontSize: 10, fontWeight: 700, padding: '3px 8px', borderRadius: 20 }}>Esgotado</span>
            </div>
          )}
        </div>
        <div style={{ padding: '12px 14px' }}>
          {evento.categoria && (
            <div style={{ marginBottom: 6 }}>
              <span style={{ background: corCategoria(evento.categoria) + '18', color: corCategoria(evento.categoria), fontSize: 11, fontWeight: 600, padding: '2px 8px', borderRadius: 20 }}>
                {evento.categoria}
              </span>
            </div>
          )}
          <h3 style={{ fontSize: 14, fontWeight: 700, color: '#1e293b', marginBottom: 4, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {evento.nome}
          </h3>
          <p style={{ fontSize: 12, color: '#64748b', marginBottom: 8 }}>📍 {evento.local}</p>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: esgotado ? '#ef4444' : '#1e293b' }}>
              {esgotado ? 'Esgotado' : evento.precoMinimo != null ? formatMoeda(evento.precoMinimo) : 'Entrada Franca'}
            </span>
            <Link to={`/eventos/${evento.id}`} style={{ fontSize: 13, color: '#1d4ed8', fontWeight: 600 }}>
              {esgotado ? '' : 'Detalhes'}
            </Link>
          </div>
        </div>
      </div>
    </Link>
  )
}
