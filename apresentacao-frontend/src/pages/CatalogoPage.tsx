import { useEffect, useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import {
  Search, MapPin, Star, ArrowRight, Music2, Drama, Trophy, Mic, Disc3, UtensilsCrossed, Ticket, SlidersHorizontal,
  type LucideIcon,
} from 'lucide-react'
import { eventoService } from '../services/api'
import Navbar from '../components/Navbar'
import { CATEGORIAS, formatMoeda, formatDataBadge, corCategoria } from '../constants'

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

const ICONE_CATEGORIA: Record<string, LucideIcon> = {
  'Música': Music2,
  'Teatro e Artes': Drama,
  'Esportes': Trophy,
  'Conferências': Mic,
  'Vida Noturna': Disc3,
  'Comida e Bebida': UtensilsCrossed,
}

function coverStyle(cat?: string) {
  const cor = corCategoria(cat)
  return {
    background: `linear-gradient(140deg, ${cor} 0%, color-mix(in oklab, ${cor} 52%, black) 100%)`,
  }
}

export default function CatalogoPage() {
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
      .then(r => { setEventos(r.data); setPagina(1) })
      .catch(() => setEventos([]))
      .finally(() => setCarregando(false))
  }

  useEffect(() => { carregarEventos() }, [categoriasSelecionadas, filtroPreco])
  useEffect(() => { setBusca(searchParams.get('busca') ?? '') }, [searchParams])

  const toggleCategoria = (cat: string) =>
    setCategoriasSelecionadas(prev => prev.includes(cat) ? prev.filter(c => c !== cat) : [cat])

  const handleBuscar = (e: FormEvent<HTMLFormElement>) => { e.preventDefault(); carregarEventos() }

  const destaques = eventos.slice(0, 2)
  const proximos = eventos.slice(2, 2 + pagina * POR_PAGINA)
  const temMais = eventos.length > 2 + pagina * POR_PAGINA

  return (
    <>
      <Navbar />

      {/* Hero */}
      <section className="cat-hero">
        <div className="app-container cat-hero__inner">
          <span className="badge badge--brand badge--dot">Revenda segura · sem cambismo</span>
          <h1 className="cat-hero__title">Sua próxima experiência ao vivo começa aqui.</h1>
          <p className="cat-hero__sub secondary">
            Descubra shows, festivais e eventos — compre, revenda e participe de sorteios com segurança.
          </p>
          <form className="cat-search" onSubmit={handleBuscar}>
            <label className="cat-search__field">
              <Search size={18} />
              <input placeholder="Evento ou artista" value={busca} onChange={e => setBusca(e.target.value)} aria-label="Evento ou artista" />
            </label>
            <span className="cat-search__div" aria-hidden />
            <label className="cat-search__field">
              <MapPin size={18} />
              <input placeholder="Cidade ou local" value={cidade} onChange={e => setCidade(e.target.value)} aria-label="Cidade ou local" />
            </label>
            <button className="btn" type="submit">Buscar</button>
          </form>
        </div>
      </section>

      {/* Filtros */}
      <div className="app-container" style={{ paddingTop: 'var(--sp-6)' }}>
        <div className="cat-filtros">
          <div className="cat-filtros__chips" role="group" aria-label="Filtrar por categoria">
            <button className={`chip${categoriasSelecionadas.length === 0 ? ' chip--active' : ''}`} aria-pressed={categoriasSelecionadas.length === 0} onClick={() => setCategoriasSelecionadas([])}>
              <SlidersHorizontal size={15} /> Tudo
            </button>
            {CATEGORIAS.map(cat => {
              const Icone = ICONE_CATEGORIA[cat] ?? Ticket
              const ativo = categoriasSelecionadas.includes(cat)
              return (
                <button key={cat} className={`chip${ativo ? ' chip--active' : ''}`} aria-pressed={ativo} onClick={() => toggleCategoria(cat)}>
                  <Icone size={15} /> {cat}
                </button>
              )
            })}
          </div>
          <div className="segmented" role="group" aria-label="Filtrar por preço">
            {([['todos', 'Qualquer preço'], ['gratis', 'Grátis'], ['ate50', 'Até R$50']] as const).map(([v, label]) => (
              <button key={v} className={`segmented__opt${filtroPreco === v ? ' segmented__opt--active' : ''}`} aria-pressed={filtroPreco === v} onClick={() => setFiltroPreco(v)}>
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Conteúdo */}
      <main className="app-container" style={{ paddingBlock: 'var(--sp-6) var(--sp-8)' }}>
        {carregando ? (
          <div className="cat-grid">
            {Array.from({ length: 6 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 320 }} />)}
          </div>
        ) : eventos.length === 0 ? (
          <div className="empty">
            <Ticket size={40} />
            <h3>Nenhum evento encontrado</h3>
            <p className="muted">Tente ajustar os filtros ou a busca.</p>
          </div>
        ) : (
          <>
            {destaques.length > 0 && (
              <section style={{ marginBottom: 'var(--sp-7)' }}>
                <div className="between" style={{ marginBottom: 'var(--sp-4)' }}>
                  <h2>Em destaque</h2>
                </div>
                <div className="cat-grid cat-grid--feat">
                  {destaques.map(e => <EventoCard key={e.id} evento={e} destaque />)}
                </div>
              </section>
            )}

            {proximos.length > 0 && (
              <section>
                <div className="between" style={{ marginBottom: 'var(--sp-4)' }}>
                  <h2>Próximos eventos</h2>
                </div>
                <div className="cat-grid">
                  {proximos.map(e => <EventoCard key={e.id} evento={e} />)}
                </div>
              </section>
            )}

            {temMais && (
              <div style={{ textAlign: 'center', marginTop: 'var(--sp-6)' }}>
                <button className="btn btn--ghost" onClick={() => setPagina(p => p + 1)}>Carregar mais eventos</button>
              </div>
            )}
          </>
        )}
      </main>
    </>
  )
}

function EventoCard({ evento, destaque = false }: { evento: EventoCatalogo; destaque?: boolean }) {
  const { dia, mes } = formatDataBadge(evento.dataHora)
  const esgotado = !evento.temEstoquePrimario && !evento.temRevendaDisponivel
  const Icone = ICONE_CATEGORIA[evento.categoria ?? ''] ?? Ticket

  return (
    <Link to={`/eventos/${evento.id}`} className={`evcard${destaque ? ' evcard--feat' : ''}`}>
      <div className="evcard__cover" style={coverStyle(evento.categoria)}>
        {evento.imagemCapaUrl
          ? <img src={evento.imagemCapaUrl} alt="" />
          : <Icone size={destaque ? 76 : 56} />}
        <div className="evcard__date">
          <strong>{dia}</strong><span>{mes}</span>
        </div>
        <div className="evcard__tags">
          {esgotado
            ? <span className="badge badge--danger">Esgotado</span>
            : evento.temRevendaDisponivel && !evento.temEstoquePrimario
              ? <span className="badge badge--accent">Só revenda</span>
              : evento.categoria && <span className="badge" style={{ background: 'oklch(1 0 0 / .9)', color: corCategoria(evento.categoria) }}>{evento.categoria}</span>}
        </div>
      </div>
      <div className="evcard__body">
        <h3 className="evcard__title">{evento.nome}</h3>
        <p className="evcard__meta secondary"><MapPin size={14} /> {evento.local}</p>
        <div className="between evcard__foot">
          <span>
            {esgotado
              ? <span className="badge badge--danger">Esgotado</span>
              : evento.precoMinimo != null
                ? <><span className="muted" style={{ fontSize: '0.8125rem' }}>a partir de </span><span className="money" style={{ fontSize: '1.05rem' }}>{formatMoeda(evento.precoMinimo)}</span></>
                : <span className="badge badge--success">Entrada franca</span>}
          </span>
          {evento.mediaAvaliacao > 0 && (
            <span className="row muted" style={{ gap: 4, fontSize: '0.8125rem', fontWeight: 600 }}>
              <Star size={14} fill="currentColor" style={{ color: 'var(--warn)' }} /> {evento.mediaAvaliacao.toFixed(1)}
            </span>
          )}
          {!esgotado && evento.mediaAvaliacao === 0 && <ArrowRight size={18} style={{ color: 'var(--brand-strong)' }} />}
        </div>
      </div>
    </Link>
  )
}
