import { useEffect, useState } from 'react'
import axios from 'axios'
import { Link, useLocation } from 'react-router-dom'
import {
  Ticket, MapPin, Clock, X, Star, Repeat, RotateCcw, Wallet, ArrowDownLeft, ArrowUpRight, CheckCircle2, AlertCircle, Tag,
} from 'lucide-react'
import { ingressoService, eventoService, tipoIngressoService, saldoService, revendaService, avaliacaoService, mapaAssentosService } from '../services/api'
import Navbar from '../components/Navbar'
import { formatMoeda, formatDataBadge } from '../constants'
import { useAuth } from '../context/AuthContext'
import { gerarQrCode } from '../utils/qrCode'

interface IngressoRaw { id: string; eventoId: number; tipoIngressoId: number; proprietarioId: number; status: string; bloqueadoPorReembolso: boolean; codigo: string; qrCode: string }
interface Evento { id: number; nome: string; dataHora: string; local: string; imagemCapaUrl?: string; aberturaPortoes?: string; status: string }
interface TipoIngresso { id: number; nome: string; preco: number }
interface IngressoEnriquecido { ingresso: IngressoRaw; evento: Evento; tipo: TipoIngresso }
interface AnuncioRaw { id: number; ingressoIds: string[]; vendedorId: number; compradorId: number | null; eventoId: number; preco: number; status: string; quantidade: number }
interface AnuncioEnriquecido { anuncio: AnuncioRaw; evento: Evento }
interface Transacao { id: number; tipo: string; valor: number; data: string }
interface AssentoComprado { id: number; eventoId: number; secao: string; codigo: string; tipo: string; preco: number; status: string }
interface AssentoEnriquecido { assento: AssentoComprado; evento: Evento }

const TIPO_POSITIVO = new Set(['REEMBOLSO', 'VENDA', 'DEPOSITO'])
const LABEL: Record<string, string> = { DEPOSITO: 'Recarga de saldo', COMPRA: 'Compra de ingresso', VENDA: 'Venda', REEMBOLSO: 'Reembolso', AJUSTE_SALDO: 'Ajuste', TRANSFERENCIA: 'Transferência' }
const STATUS_BADGE: Record<string, { c: string; t: string }> = {
  ATIVO: { c: 'badge--success', t: 'Ativo' }, EM_REVENDA: { c: 'badge--warn', t: 'Em revenda' },
  REVENDIDO: { c: 'badge--brand', t: 'Revendido' }, UTILIZADO: { c: 'badge', t: 'Utilizado' },
  REEMBOLSADO: { c: 'badge', t: 'Reembolsado' }, CANCELADO: { c: 'badge--danger', t: 'Cancelado' },
}
const podeReembolsar = (dataHora?: string) => !dataHora || (new Date(dataHora).getTime() - Date.now() > 48 * 3600 * 1000)

async function enriquecer(raw: IngressoRaw[]): Promise<IngressoEnriquecido[]> {
  const ids = [...new Set(raw.map(i => i.eventoId))]
  const ev = new Map<number, Evento>(), ti = new Map<number, TipoIngresso[]>()
  await Promise.all(ids.map(async id => {
    const [e, t] = await Promise.all([eventoService.detalhe(id), tipoIngressoService.listar(id)])
    ev.set(id, e.data); ti.set(id, t.data)
  }))
  return raw.map(i => ({
    ingresso: i,
    evento: ev.get(i.eventoId) ?? { id: i.eventoId, nome: 'Evento', dataHora: '', local: '', status: 'ATIVO' },
    tipo: (ti.get(i.eventoId) ?? []).find(t => t.id === i.tipoIngressoId) ?? { id: i.tipoIngressoId, nome: 'Ingresso', preco: 0 },
  }))
}

export default function MeusIngressos() {
  const location = useLocation()
  const { usuario } = useAuth()
  const state = location.state as { sucesso?: boolean; anuncioPublicado?: boolean } | null

  const [ingressos, setIngressos] = useState<IngressoEnriquecido[]>([])
  const [assentos, setAssentos] = useState<AssentoEnriquecido[]>([])
  const [anuncios, setAnuncios] = useState<AnuncioEnriquecido[]>([])
  const [transacoes, setTransacoes] = useState<Transacao[]>([])
  const [saldo, setSaldo] = useState<number | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [aba, setAba] = useState<'proximos' | 'encerrados' | 'revendas'>('proximos')
  const [ingressoAberto, setIngressoAberto] = useState<IngressoEnriquecido | null>(null)
  const [assentoAberto, setAssentoAberto] = useState<AssentoEnriquecido | null>(null)
  const [avaliacaoAberta, setAvaliacaoAberta] = useState<IngressoEnriquecido | null>(null)
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(
    state?.anuncioPublicado ? { ok: true, texto: 'Anúncio publicado com sucesso!' } :
    state?.sucesso ? { ok: true, texto: 'Compra realizada com sucesso!' } : null
  )

  const recarregar = async () => {
    if (!usuario) return
    const [ingRes] = await Promise.all([ingressoService.meus(usuario.id)])
    setIngressos(await enriquecer(ingRes.data))
  }

  useEffect(() => {
    if (!usuario) return
    Promise.all([
      ingressoService.meus(usuario.id), saldoService.obter(usuario.id),
      saldoService.transacoes(usuario.id), revendaService.meus(usuario.id).catch(() => ({ data: [] })),
      mapaAssentosService.meusAssentos(usuario.id).catch(() => ({ data: [] })),
    ]).then(async ([ingRes, saldoRes, transRes, anunRes, assRes]) => {
      setSaldo(saldoRes.data.valor); setTransacoes(transRes.data)
      const enr = await enriquecer(ingRes.data)
      setIngressos(enr)
      const evMap = new Map(enr.map(e => [e.evento.id, e.evento]))
      const anunRaw: AnuncioRaw[] = anunRes.data
      const faltam = [...new Set(anunRaw.map(a => a.eventoId).filter(id => !evMap.has(id)))]
      await Promise.all(faltam.map(async id => { evMap.set(id, (await eventoService.detalhe(id)).data) }))
      setAnuncios(anunRaw.map(a => ({ anuncio: a, evento: evMap.get(a.eventoId) ?? { id: a.eventoId, nome: 'Evento', dataHora: '', local: '', status: 'ATIVO' } })))
      const assRaw: AssentoComprado[] = assRes.data ?? []
      if (assRaw.length > 0) {
        const evFaltam = [...new Set(assRaw.map(a => a.eventoId).filter(id => id != null && !evMap.has(id)))]
        await Promise.all(evFaltam.map(async id => {
          try { evMap.set(id, (await eventoService.detalhe(id)).data) } catch { /* ignora */ }
        }))
      }
      setAssentos(assRaw.map(a => ({ assento: a, evento: evMap.get(a.eventoId) ?? { id: a.eventoId ?? 0, nome: 'Evento', dataHora: '', local: '', status: 'ATIVO' } })))
    }).finally(() => setCarregando(false))
  }, [])

  const agora = new Date()
  const proximos = ingressos.filter(e => e.evento.dataHora ? new Date(e.evento.dataHora) > agora : true)
  const encerrados = ingressos.filter(e => e.evento.dataHora ? new Date(e.evento.dataHora) <= agora : false)
  const lista = aba === 'proximos' ? proximos : aba === 'encerrados' ? encerrados : []
  const listaAssentos = aba === 'proximos' || aba === 'encerrados' ? assentos : []

  const reembolsar = async (id: string) => {
    try {
      await ingressoService.reembolsar(id, usuario!.id)
      setMsg({ ok: true, texto: 'Reembolso solicitado! Nossa equipe irá analisar em breve.' })
      setIngressoAberto(null)
      await recarregar()
    } catch (err) {
      const motivo = axios.isAxiosError(err) && typeof err.response?.data?.motivo === 'string' ? err.response.data.motivo : null
      setMsg({ ok: false, texto: motivo ?? 'Erro ao solicitar reembolso.' })
    }
  }

  const cancelarAnuncio = async (a: AnuncioEnriquecido) => {
    if (!confirm('Cancelar o anúncio? O ingresso volta para a carteira.')) return
    try {
      await revendaService.cancelar(a.anuncio.id, usuario!.id)
      setMsg({ ok: true, texto: 'Anúncio cancelado. Ingresso de volta na carteira.' })
      setAnuncios(prev => prev.filter(x => x.anuncio.id !== a.anuncio.id))
      recarregar()
    } catch { setMsg({ ok: false, texto: 'Erro ao cancelar anúncio.' }) }
  }

  return (
    <>
      <Navbar />
      <div className="app-container page">
        {msg && (
          <div className={`auth-alert ${msg.ok ? 'auth-alert--ok' : 'auth-alert--err'}`}>
            {msg.ok ? <CheckCircle2 size={18} /> : <AlertCircle size={18} />}{msg.texto}
          </div>
        )}

        <div className="between wrap page-head" style={{ gap: 'var(--sp-4)' }}>
          <div><h1>Meus ingressos</h1><p className="secondary">Acesse, transfira, revenda ou solicite reembolso.</p></div>
          <div className="segmented">
            {([['proximos', 'Próximos'], ['encerrados', 'Encerrados'], ['revendas', `Em revenda${anuncios.length ? ` · ${anuncios.length}` : ''}`]] as const).map(([v, t]) => (
              <button key={v} className={`segmented__opt${aba === v ? ' segmented__opt--active' : ''}`} onClick={() => setAba(v)}>{t}</button>
            ))}
          </div>
        </div>

        {carregando ? (
          <div className="cat-grid">{Array.from({ length: 4 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 150 }} />)}</div>
        ) : aba === 'revendas' ? (
          anuncios.length === 0 ? (
            <div className="empty"><Tag size={40} /><h3>Nenhum anúncio ativo</h3><p className="muted">Você não tem ingressos à venda no momento.</p></div>
          ) : (
            <div className="cat-grid cat-grid--feat">
              {anuncios.map(a => <CardAnuncio key={a.anuncio.id} item={a} onCancelar={() => cancelarAnuncio(a)} />)}
            </div>
          )
        ) : lista.length === 0 && listaAssentos.length === 0 ? (
          <div className="empty"><Ticket size={40} /><h3>Nada por aqui</h3><p className="muted">Nenhum ingresso {aba === 'proximos' ? 'próximo' : 'encerrado'}.</p>
            <Link to="/" className="btn btn--soft" style={{ marginTop: 'var(--sp-4)' }}>Explorar eventos</Link></div>
        ) : (
          <div className="cat-grid cat-grid--feat">
            {lista.map(e => <CardIngresso key={e.ingresso.id} item={e} onVer={() => setIngressoAberto(e)} />)}
            {listaAssentos.map(e => <CardAssento key={`assento-${e.assento.id}`} item={e} onVer={() => setAssentoAberto(e)} />)}
          </div>
        )}

        {/* Histórico + saldo */}
        <div className="two-col" style={{ marginTop: 'var(--sp-7)' }}>
          <section>
            <div className="between" style={{ marginBottom: 'var(--sp-3)' }}>
              <h2>Movimentações recentes</h2>
              <Link to="/saldo" style={{ color: 'var(--brand-strong)', fontWeight: 600, fontSize: '0.875rem' }}>Ver tudo</Link>
            </div>
            <div className="surface" style={{ overflow: 'hidden' }}>
              {transacoes.length === 0 ? (
                <div className="empty" style={{ padding: 'var(--sp-6)' }}><Wallet size={32} /><p className="muted">Nenhuma movimentação.</p></div>
              ) : transacoes.slice(0, 5).map(t => {
                const pos = TIPO_POSITIVO.has(t.tipo)
                const Ico = t.tipo === 'REEMBOLSO' ? RotateCcw : pos ? ArrowDownLeft : ArrowUpRight
                return (
                  <div key={t.id} className="list-row">
                    <div className={`list-ico ${pos ? 'list-ico--in' : 'list-ico--out'}`}><Ico size={20} /></div>
                    <div className="grow"><div style={{ fontWeight: 600 }}>{LABEL[t.tipo] ?? t.tipo}</div>
                      <div className="muted" style={{ fontSize: '0.8125rem' }}>{t.data ? new Date(t.data).toLocaleDateString('pt-BR') : ''}</div></div>
                    <span className={pos ? 'amt-in' : 'amt-out'}>{pos ? '+' : '−'}{formatMoeda(Math.abs(t.valor))}</span>
                  </div>
                )
              })}
            </div>
          </section>

          {saldo !== null && (
            <div className="sticky-side">
              <div className="wallet">
                <div className="wallet__label">SALDO NA CARTEIRA</div>
                <div className="wallet__value">{formatMoeda(saldo)}</div>
                <Link to="/saldo"><button className="btn btn--block" style={{ marginTop: 'var(--sp-4)', background: 'oklch(1 0 0 / 0.16)', color: '#fff' }}><Wallet size={17} /> Gerenciar carteira</button></Link>
              </div>
            </div>
          )}
        </div>
      </div>

      {assentoAberto && (
        <ModalAssentoBilhete item={assentoAberto} onFechar={() => setAssentoAberto(null)} />
      )}

      {avaliacaoAberta && (
        <ModalAvaliacao item={avaliacaoAberta} usuarioId={usuario!.id}
          onFechar={() => setAvaliacaoAberta(null)}
          onSucesso={() => { setAvaliacaoAberta(null); setMsg({ ok: true, texto: 'Avaliação enviada. Obrigado!' }) }} />
      )}

      {ingressoAberto && (
        <div className="modal-backdrop" onClick={() => setIngressoAberto(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal__head"><h3>Seu ingresso</h3><button className="modal__x" onClick={() => setIngressoAberto(null)}><X size={18} /></button></div>

            <div className="surface surface--flat surface--pad" style={{ background: 'var(--surface-2)', marginBottom: 'var(--sp-4)' }}>
              <div style={{ fontWeight: 600, fontSize: '1.05rem' }}>{ingressoAberto.evento.nome}</div>
              <div className="muted row" style={{ gap: 6, fontSize: '0.875rem', marginTop: 4 }}><MapPin size={14} /> {ingressoAberto.evento.local}</div>
              {ingressoAberto.evento.dataHora && <div className="muted row" style={{ gap: 6, fontSize: '0.875rem', marginTop: 2 }}><Clock size={14} /> {new Date(ingressoAberto.evento.dataHora).toLocaleString('pt-BR')}</div>}
            </div>

            <div className="qr" style={{ marginBottom: 'var(--sp-4)' }}>
              <QrCode valor={ingressoAberto.ingresso.qrCode || ingressoAberto.ingresso.codigo} />
              <div className="qr__code">{ingressoAberto.ingresso.codigo}</div>
            </div>

            <div className="kv"><span className="muted">Tipo</span><span style={{ fontWeight: 600 }}>{ingressoAberto.tipo.nome}</span></div>
            <div className="kv" style={{ marginBottom: 'var(--sp-4)' }}><span className="muted">Status</span>
              <span className={`badge ${STATUS_BADGE[ingressoAberto.ingresso.status]?.c ?? 'badge'}`}>{STATUS_BADGE[ingressoAberto.ingresso.status]?.t ?? ingressoAberto.ingresso.status}</span></div>

            {ingressoAberto.ingresso.status === 'ATIVO' && !ingressoAberto.ingresso.bloqueadoPorReembolso && (
              <>
                <div className="row" style={{ gap: 'var(--sp-2)' }}>
                  <Link to={`/revender/${ingressoAberto.ingresso.id}`} className="grow"><button className="btn btn--soft btn--block"><Repeat size={17} /> Revender</button></Link>
                  <button className="btn btn--ghost grow" onClick={() => reembolsar(ingressoAberto.ingresso.id)} disabled={!podeReembolsar(ingressoAberto.evento.dataHora)}>
                    <RotateCcw size={17} /> Reembolsar
                  </button>
                </div>
                {!podeReembolsar(ingressoAberto.evento.dataHora) && <p className="muted" style={{ fontSize: '0.8125rem', marginTop: 8, textAlign: 'center' }}>Reembolso indisponível: evento em menos de 48h.</p>}
              </>
            )}

            {ingressoAberto.evento.dataHora && new Date(ingressoAberto.evento.dataHora) < new Date() && ingressoAberto.evento.status !== 'CANCELADO' && (
              <button className="btn btn--accent btn--block" style={{ marginTop: 'var(--sp-3)' }} onClick={() => { const a = ingressoAberto; setIngressoAberto(null); setAvaliacaoAberta(a) }}>
                <Star size={17} /> Avaliar evento
              </button>
            )}
          </div>
        </div>
      )}
    </>
  )
}

function QrCode({ valor }: { valor: string }) {
  const matriz = gerarQrCode(valor)

  return (
    <div className="qr__grid" aria-label={`QR Code do ingresso ${valor}`}>
      {matriz.map((linha, y) =>
        linha.map((escuro, x) => (
          <span key={`${y}-${x}`} className={escuro ? 'qr__dot qr__dot--on' : 'qr__dot'} />
        ))
      )}
    </div>
  )
}

function CardIngresso({ item, onVer }: { item: IngressoEnriquecido; onVer: () => void }) {
  const { dia, mes } = item.evento.dataHora ? formatDataBadge(item.evento.dataHora) : { dia: '--', mes: '---' }
  const sb = STATUS_BADGE[item.ingresso.status]
  return (
    <div className="tcard">
      <div className="tcard__side"><Ticket size={40} /><div className="evcard__date" style={{ top: 12, left: 12 }}><strong>{dia}</strong><span>{mes}</span></div></div>
      <div className="tcard__body">
        <div className="row wrap" style={{ gap: 6 }}>
          <span className="badge badge--brand">{item.tipo.nome}</span>
          {item.ingresso.status !== 'ATIVO' && sb && <span className={`badge ${sb.c}`}>{sb.t}</span>}
        </div>
        <h3 style={{ fontSize: '1.1rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>{item.evento.nome}</h3>
        <div className="muted row" style={{ gap: 6, fontSize: '0.875rem' }}><MapPin size={14} /> {item.evento.local}</div>
        <div className="tcard__foot">
          {item.evento.dataHora && <span className="muted row" style={{ gap: 6, fontSize: '0.8125rem' }}><Clock size={14} /> {new Date(item.evento.dataHora).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}</span>}
          <button className="btn btn--sm" onClick={onVer}>Ver ingresso</button>
        </div>
      </div>
    </div>
  )
}

function CardAnuncio({ item, onCancelar }: { item: AnuncioEnriquecido; onCancelar: () => void }) {
  const { dia, mes } = item.evento.dataHora ? formatDataBadge(item.evento.dataHora) : { dia: '--', mes: '---' }
  const reservado = item.anuncio.status === 'RESERVADO'
  return (
    <div className={`tcard${reservado ? ' tcard--alert' : ''}`}>
      <div className="tcard__side" style={{ background: 'linear-gradient(150deg, var(--accent) 0%, oklch(0.5 0.16 35) 100%)' }}>
        <Repeat size={36} /><div className="evcard__date" style={{ top: 12, left: 12 }}><strong>{dia}</strong><span>{mes}</span></div>
      </div>
      <div className="tcard__body">
        <div className="between">
          <span className={`badge ${reservado ? 'badge--warn' : 'badge--success'}`}>{reservado ? 'Reservado' : 'Disponível'}</span>
          <span className="money" style={{ fontSize: '1.1rem', color: 'var(--brand-strong)' }}>{formatMoeda(item.anuncio.preco)}</span>
        </div>
        <h3 style={{ fontSize: '1.1rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>{item.evento.nome}</h3>
        <div className="muted row" style={{ gap: 6, fontSize: '0.875rem' }}><MapPin size={14} /> {item.evento.local} · {item.anuncio.quantidade} ingresso(s)</div>
        {reservado && <p className="muted" style={{ fontSize: '0.8125rem' }}>Reservado — aguardando confirmação de pagamento.</p>}
        <div className="tcard__foot">
          <Link to={`/revendas?eventoId=${item.evento.id}`} style={{ color: 'var(--brand-strong)', fontWeight: 600, fontSize: '0.875rem' }}>Ver anúncio</Link>
          {!reservado && <button className="btn btn--sm btn--ghost" onClick={onCancelar}>Cancelar</button>}
        </div>
      </div>
    </div>
  )
}

const COR_TIPO_ASSENTO: Record<string, string> = {
  VIP: '#a855f7', ACESSIBILIDADE: '#06b6d4', NORMAL: 'var(--brand)', BLOQUEADO: '#6b7280',
}

function CardAssento({ item, onVer }: { item: AssentoEnriquecido; onVer: () => void }) {
  const { dia, mes } = item.evento.dataHora ? formatDataBadge(item.evento.dataHora) : { dia: '--', mes: '---' }
  const corTipo = COR_TIPO_ASSENTO[item.assento.tipo] ?? COR_TIPO_ASSENTO.NORMAL
  return (
    <div className="tcard">
      <div className="tcard__side" style={{ background: `linear-gradient(150deg, ${corTipo} 0%, color-mix(in oklab, ${corTipo} 60%, black) 100%)` }}>
        <Ticket size={40} />
        <div className="evcard__date" style={{ top: 12, left: 12 }}><strong>{dia}</strong><span>{mes}</span></div>
      </div>
      <div className="tcard__body">
        <div className="row wrap" style={{ gap: 6 }}>
          <span className="badge badge--brand">Assento {item.assento.codigo}</span>
          <span className="badge" style={{ background: corTipo + '22', color: corTipo, border: `1px solid ${corTipo}44` }}>
            {item.assento.tipo}
          </span>
        </div>
        <h3 style={{ fontSize: '1.1rem', fontFamily: 'var(--font-sans)', letterSpacing: 0 }}>{item.evento.nome}</h3>
        <div className="muted row" style={{ gap: 6, fontSize: '0.875rem' }}><MapPin size={14} /> {item.evento.local}</div>
        <div className="tcard__foot">
          {item.evento.dataHora && <span className="muted row" style={{ gap: 6, fontSize: '0.8125rem' }}><Clock size={14} /> {new Date(item.evento.dataHora).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })}</span>}
          <button className="btn btn--sm" onClick={onVer}>Ver bilhete</button>
        </div>
      </div>
    </div>
  )
}

function ModalAssentoBilhete({ item, onFechar }: { item: AssentoEnriquecido; onFechar: () => void }) {
  const corTipo = COR_TIPO_ASSENTO[item.assento.tipo] ?? COR_TIPO_ASSENTO.NORMAL
  const codigoQr = `ASSENTO-${item.assento.id}-${item.evento.id}`
  return (
    <div className="modal-backdrop" onClick={e => { if (e.target === e.currentTarget) onFechar() }}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal__head">
          <h3>Bilhete de Assento</h3>
          <button className="modal__x" onClick={onFechar}><X size={18} /></button>
        </div>

        {/* Cabeçalho do evento */}
        <div className="surface surface--flat surface--pad" style={{ background: 'var(--surface-2)', marginBottom: 'var(--sp-4)' }}>
          <div style={{ fontWeight: 600, fontSize: '1.05rem' }}>{item.evento.nome}</div>
          <div className="muted row" style={{ gap: 6, fontSize: '0.875rem', marginTop: 4 }}><MapPin size={14} /> {item.evento.local}</div>
          {item.evento.dataHora && <div className="muted row" style={{ gap: 6, fontSize: '0.875rem', marginTop: 2 }}><Clock size={14} /> {new Date(item.evento.dataHora).toLocaleString('pt-BR')}</div>}
        </div>

        {/* Destaque do assento */}
        <div style={{ background: `linear-gradient(135deg, ${corTipo}18, ${corTipo}08)`, border: `2px solid ${corTipo}44`, borderRadius: 12, padding: '16px 20px', marginBottom: 'var(--sp-4)', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <p style={{ fontSize: 11, fontWeight: 700, color: corTipo, textTransform: 'uppercase', letterSpacing: 1, marginBottom: 4 }}>Seu assento</p>
            <p style={{ fontSize: 42, fontWeight: 900, color: corTipo, lineHeight: 1, letterSpacing: -1 }}>{item.assento.codigo}</p>
          </div>
          <div style={{ textAlign: 'right' }}>
            <span style={{ display: 'inline-block', fontSize: 12, fontWeight: 700, padding: '4px 14px', borderRadius: 20, background: corTipo, color: '#fff', marginBottom: 8 }}>
              {item.assento.tipo}
            </span>
            <p style={{ fontSize: 22, fontWeight: 800, color: 'var(--brand-strong)' }}>{formatMoeda(item.assento.preco)}</p>
          </div>
        </div>

        {/* QR Code visual */}
        <div className="qr" style={{ marginBottom: 'var(--sp-4)' }}>
          <div className="qr__grid">
            {Array.from({ length: 49 }).map((_, i) => (
              <div key={i} style={{ background: (i * 11 + i % 7 + codigoQr.charCodeAt(i % codigoQr.length)) % 2 === 0 ? '#fff' : 'transparent' }} />
            ))}
          </div>
          <div className="qr__code">{codigoQr.substring(0, 12).toUpperCase()}</div>
        </div>

        {/* Detalhes */}
        <div className="kv"><span className="muted">Seção</span><span style={{ fontWeight: 600 }}>{item.assento.secao || 'Principal'}</span></div>
        <div className="kv"><span className="muted">Tipo de assento</span><span style={{ fontWeight: 600, color: corTipo }}>{item.assento.tipo}</span></div>
        <div className="kv"><span className="muted">Valor pago</span><span style={{ fontWeight: 700, color: 'var(--brand-strong)' }}>{formatMoeda(item.assento.preco)}</span></div>
        <div className="kv" style={{ marginBottom: 'var(--sp-4)' }}>
          <span className="muted">Status</span>
          <span className="badge badge--success">Confirmado</span>
        </div>
      </div>
    </div>
  )
}

function ModalAvaliacao({ item, usuarioId, onFechar, onSucesso }: { item: IngressoEnriquecido; usuarioId: number; onFechar: () => void; onSucesso: () => void }) {
  const [nota, setNota] = useState(0)
  const [hover, setHover] = useState(0)
  const [comentario, setComentario] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')

  const enviar = async () => {
    if (nota === 0) { setErro('Selecione uma nota de 1 a 5 estrelas.'); return }
    setEnviando(true); setErro('')
    try {
      await avaliacaoService.avaliar(item.evento.id, usuarioId, { nota, comentario: comentario.trim() || null })
      onSucesso()
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string; motivo?: string } } }
      setErro(err.response?.data?.motivo ?? err.response?.data?.message ?? 'Erro ao enviar avaliação.')
      setEnviando(false)
    }
  }
  const ROTULO = ['', 'Muito ruim', 'Ruim', 'Regular', 'Bom', 'Excelente']

  return (
    <div className="modal-backdrop" onClick={e => { if (e.target === e.currentTarget) onFechar() }}>
      <div className="modal">
        <div className="modal__head"><h3>Avaliar evento</h3><button className="modal__x" onClick={onFechar}><X size={18} /></button></div>
        <div className="surface surface--flat surface--pad" style={{ background: 'var(--surface-2)', marginBottom: 'var(--sp-4)' }}>
          <div style={{ fontWeight: 600 }}>{item.evento.nome}</div>
          <div className="muted" style={{ fontSize: '0.8125rem' }}>{item.evento.dataHora ? new Date(item.evento.dataHora).toLocaleDateString('pt-BR', { day: 'numeric', month: 'long', year: 'numeric' }) : ''} · {item.evento.local}</div>
        </div>
        <div className="field" style={{ marginBottom: 'var(--sp-4)' }}>
          <span className="label">Sua nota</span>
          <div className="row" style={{ gap: 4 }}>
            {[1, 2, 3, 4, 5].map(i => (
              <button key={i} className={`star-btn${i <= (hover || nota) ? ' star-btn--on' : ''}`} onClick={() => setNota(i)} onMouseEnter={() => setHover(i)} onMouseLeave={() => setHover(0)} aria-label={`${i} estrelas`}>
                <Star size={34} fill={i <= (hover || nota) ? 'currentColor' : 'none'} />
              </button>
            ))}
          </div>
          {nota > 0 && <span className="field-hint">{ROTULO[nota]}</span>}
        </div>
        <div className="field" style={{ marginBottom: 'var(--sp-4)' }}>
          <label className="label">Comentário <span className="muted" style={{ fontWeight: 400 }}>(opcional)</span></label>
          <textarea className="textarea" value={comentario} onChange={e => setComentario(e.target.value)} placeholder="Como foi sua experiência?" />
        </div>
        {erro && <div className="auth-alert auth-alert--err"><AlertCircle size={18} />{erro}</div>}
        <div className="row" style={{ gap: 'var(--sp-2)' }}>
          <button className="btn btn--ghost grow" onClick={onFechar}>Cancelar</button>
          <button className="btn grow" style={{ flex: 2 }} onClick={enviar} disabled={enviando || nota === 0}>{enviando ? 'Enviando…' : 'Enviar avaliação'}</button>
        </div>
      </div>
    </div>
  )
}
