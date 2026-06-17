import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  BarChart3, Camera, Download, Trash2, Edit2, Save, X,
  CheckCircle2, AlertTriangle, Ban,
} from 'lucide-react'
import Navbar from '../components/Navbar'
import SeletorEvento from '../components/SeletorEvento'
import { analyticsService, mapaAssentosService, eventoService } from '../services/api'
import { useAuth } from '../context/AuthContext'

interface Analytics {
  totalVendidos: number
  totalRevendidos: number
  totalDisponiveis: number
  totalCapacidade: number
  taxaOcupacao: number
  taxaRevenda: number
  mediaAvaliacao: number
  totalAvaliacoes: number
}

interface AssentoInfo {
  status: 'DISPONIVEL' | 'RESERVADO' | 'VENDIDO' | 'BLOQUEADO'
  tipo: 'NORMAL' | 'VIP' | 'ACESSIBILIDADE' | 'BLOQUEADO'
}

interface AssentoStats {
  total: number
  disponiveis: number
  reservados: number
  vendidos: number
  bloqueados: number
  vip: number
  acessibilidade: number
  normal: number
}

interface Snapshot {
  id: string
  titulo: string
  eventoNome: string
  criadoEm: string
  imagemBase64: string
}

const STORAGE_KEY = 'ingressify_relatorios_snapshots'

function carregarSnapshots(): Snapshot[] {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') } catch { return [] }
}

function persistirSnapshots(snaps: Snapshot[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(snaps))
}

function rr(ctx: CanvasRenderingContext2D, x: number, y: number, w: number, h: number, r: number) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.lineTo(x + w - r, y)
  ctx.quadraticCurveTo(x + w, y, x + w, y + r)
  ctx.lineTo(x + w, y + h - r)
  ctx.quadraticCurveTo(x + w, y + h, x + w - r, y + h)
  ctx.lineTo(x + r, y + h)
  ctx.quadraticCurveTo(x, y + h, x, y + h - r)
  ctx.lineTo(x, y + r)
  ctx.quadraticCurveTo(x, y, x + r, y)
  ctx.closePath()
}

function gerarPNG(
  analytics: Analytics | null,
  assentos: AssentoStats | null,
  eventoNome: string,
  titulo: string,
): string {
  const W = 900
  const secH = (analytics ? 120 : 0) + (assentos && assentos.total > 0 ? 210 : 0)
  const H = 96 + secH + 48
  const canvas = document.createElement('canvas')
  canvas.width = W; canvas.height = Math.max(H, 260)
  const ctx = canvas.getContext('2d')!

  ctx.fillStyle = '#f9fafb'
  ctx.fillRect(0, 0, W, canvas.height)

  ctx.fillStyle = '#4f46e5'
  ctx.fillRect(0, 0, W, 80)
  ctx.fillStyle = '#fff'
  ctx.font = 'bold 20px system-ui'
  ctx.fillText('Ingressify — Relatório de Analytics', 28, 32)
  ctx.font = '13px system-ui'
  ctx.fillStyle = 'rgba(255,255,255,0.8)'
  ctx.fillText(eventoNome || 'Evento não informado', 28, 56)
  const ds = new Date().toLocaleString('pt-BR')
  ctx.font = '11px system-ui'
  ctx.fillStyle = 'rgba(255,255,255,0.6)'
  ctx.fillText(ds, W - ctx.measureText(ds).width - 20, 48)

  let y = 100

  if (analytics) {
    ctx.fillStyle = '#111827'
    ctx.font = 'bold 13px system-ui'
    ctx.fillText('Analytics de Ingressos', 28, y); y += 12
    const cW = 200, cH = 82, gap = 10
    const cards = [
      { l: 'Vendidos', v: String(analytics.totalVendidos), c: '#4f46e5' },
      { l: 'Disponíveis', v: String(analytics.totalDisponiveis), c: '#16a34a' },
      { l: 'Taxa Ocupação', v: `${analytics.taxaOcupacao.toFixed(1)}%`, c: '#0891b2' },
      { l: 'Avg Avaliação', v: `${analytics.mediaAvaliacao.toFixed(1)}★`, c: '#d97706' },
    ]
    cards.forEach((cd, i) => {
      const cx = 28 + i * (cW + gap), cy = y
      ctx.fillStyle = '#fff'; ctx.shadowBlur = 3; ctx.shadowColor = 'rgba(0,0,0,0.07)'
      rr(ctx, cx, cy, cW, cH, 8); ctx.fill(); ctx.shadowBlur = 0
      ctx.strokeStyle = '#e5e7eb'; ctx.lineWidth = 1
      rr(ctx, cx, cy, cW, cH, 8); ctx.stroke()
      ctx.fillStyle = cd.c; ctx.font = 'bold 26px system-ui'
      ctx.fillText(cd.v, cx + 14, cy + 42)
      ctx.fillStyle = '#6b7280'; ctx.font = '11px system-ui'
      ctx.fillText(cd.l, cx + 14, cy + 64)
    })
    y += cH + 22
  }

  if (assentos && assentos.total > 0) {
    ctx.fillStyle = '#111827'; ctx.font = 'bold 13px system-ui'
    ctx.fillText('Mapa de Assentos', 28, y); y += 12
    const bars = [
      { l: 'Disponíveis', v: assentos.disponiveis, c: '#16a34a' },
      { l: 'Reservados', v: assentos.reservados, c: '#d97706' },
      { l: 'Vendidos', v: assentos.vendidos, c: '#7c3aed' },
      { l: 'Bloqueados', v: assentos.bloqueados, c: '#ef4444' },
    ]
    const maxBW = 560
    bars.forEach(b => {
      const pct = assentos.total > 0 ? b.v / assentos.total : 0
      const bw = Math.max(pct > 0 ? 4 : 0, pct * maxBW)
      ctx.fillStyle = '#f3f4f6'; rr(ctx, 28, y, maxBW, 20, 3); ctx.fill()
      if (bw > 0) { ctx.fillStyle = b.c; rr(ctx, 28, y, bw, 20, 3); ctx.fill() }
      ctx.fillStyle = '#374151'; ctx.font = '11px system-ui'
      ctx.fillText(`${b.l}: ${b.v} (${(pct * 100).toFixed(0)}%)`, 28 + maxBW + 10, y + 14)
      y += 28
    })
    ctx.fillStyle = '#6b7280'; ctx.font = '11px system-ui'
    ctx.fillText(`Total de assentos: ${assentos.total}  ·  VIP: ${assentos.vip}  ·  Acessibilidade: ${assentos.acessibilidade}`, 28, y + 12)
    y += 24
  }

  ctx.fillStyle = '#e5e7eb'; ctx.fillRect(0, canvas.height - 30, W, 30)
  ctx.fillStyle = '#9ca3af'; ctx.font = '10px system-ui'
  ctx.fillText(`Snapshot: ${titulo} — gerado por Ingressify`, 14, canvas.height - 10)

  return canvas.toDataURL('image/png')
}

export default function RelatoriosPage() {
  const [searchParams] = useSearchParams()
  const eventoIdParam = searchParams.get('eventoId')
  const eventoId = eventoIdParam ? Number(eventoIdParam) : null
  const { usuario, isAdmin, isOrganizador } = useAuth()

  const [analytics, setAnalytics] = useState<Analytics | null>(null)
  const [analyticsNegado, setAnalyticsNegado] = useState(false)
  const [assentos, setAssentos] = useState<AssentoStats | null>(null)
  const [semMapa, setSemMapa] = useState(false)
  const [eventoNome, setEventoNome] = useState('')
  const [carregando, setCarregando] = useState(false)

  const [snapshots, setSnapshots] = useState<Snapshot[]>(carregarSnapshots)
  const [tituloSnap, setTituloSnap] = useState('')
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [editandoTitulo, setEditandoTitulo] = useState('')
  const [msg, setMsg] = useState<{ ok: boolean; texto: string } | null>(null)

  const podeAcessar = isAdmin() || isOrganizador()

  useEffect(() => {
    if (!eventoId || !usuario) return
    setCarregando(true); setAnalytics(null); setAnalyticsNegado(false); setAssentos(null); setSemMapa(false)
    Promise.all([
      eventoService.detalhe(eventoId).then(r => setEventoNome(r.data.nome ?? '')).catch(() => {}),
      analyticsService.obter(eventoId, usuario.id)
        .then(r => setAnalytics(r.data))
        .catch(() => setAnalyticsNegado(true)),
      mapaAssentosService.obterPorEvento(eventoId)
        .then(r => {
          const lista: AssentoInfo[] = r.data?.assentos ?? []
          if (lista.length === 0) { setSemMapa(true); return }
          setAssentos({
            total: lista.length,
            disponiveis: lista.filter(a => a.status === 'DISPONIVEL').length,
            reservados: lista.filter(a => a.status === 'RESERVADO').length,
            vendidos: lista.filter(a => a.status === 'VENDIDO').length,
            bloqueados: lista.filter(a => a.status === 'BLOQUEADO').length,
            vip: lista.filter(a => a.tipo === 'VIP').length,
            acessibilidade: lista.filter(a => a.tipo === 'ACESSIBILIDADE').length,
            normal: lista.filter(a => a.tipo === 'NORMAL').length,
          })
        })
        .catch(() => setSemMapa(true)),
    ]).finally(() => setCarregando(false))
  }, [eventoId, usuario?.id])

  function salvarSnapshot() {
    if (!analytics && !assentos) {
      flash(false, 'Selecione um evento com dados para salvar.')
      return
    }
    const titulo = tituloSnap.trim() || `Snapshot ${new Date().toLocaleDateString('pt-BR')}`
    const imagem = gerarPNG(analytics, assentos, eventoNome, titulo)
    const novo: Snapshot = { id: Date.now().toString(), titulo, eventoNome, criadoEm: new Date().toISOString(), imagemBase64: imagem }
    const novos = [novo, ...snapshots]
    setSnapshots(novos); persistirSnapshots(novos); setTituloSnap('')
    const link = document.createElement('a')
    link.href = imagem
    link.download = `${titulo.replace(/[^\w\s-]/g, '').replace(/\s+/g, '_')}.png`
    link.click()
    flash(true, `Snapshot "${titulo}" salvo e baixado com sucesso!`)
  }

  function baixarSnapshot(snap: Snapshot) {
    const link = document.createElement('a')
    link.href = snap.imagemBase64
    link.download = `${snap.titulo.replace(/[^\w\s-]/g, '').replace(/\s+/g, '_')}.png`
    link.click()
  }

  function deletarSnapshot(id: string) {
    const novos = snapshots.filter(s => s.id !== id)
    setSnapshots(novos); persistirSnapshots(novos)
  }

  function confirmarEdicao(id: string) {
    const novos = snapshots.map(s => s.id === id ? { ...s, titulo: editandoTitulo.trim() || s.titulo } : s)
    setSnapshots(novos); persistirSnapshots(novos); setEditandoId(null)
  }

  function flash(ok: boolean, texto: string) {
    setMsg({ ok, texto }); setTimeout(() => setMsg(null), 4000)
  }

  if (!podeAcessar) {
    return (
      <>
        <Navbar />
        <div className="app-container page">
          <div className="empty"><Ban size={40} /><h3>Acesso restrito</h3><p className="muted">Área exclusiva de administradores e organizadores.</p></div>
        </div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div className="app-container page page--mid">
        <div className="page-head row" style={{ gap: 'var(--sp-3)' }}>
          <span className="list-ico list-ico--out" style={{ width: 48, height: 48 }}><BarChart3 size={24} /></span>
          <div>
            <h1>Relatórios e Analytics</h1>
            <p className="secondary">Métricas de ingressos, mapa de assentos e snapshots do dashboard por evento.</p>
          </div>
        </div>

        {!eventoId ? (
          <SeletorEvento
            titulo="Selecione um evento"
            descricao="Escolha um evento para ver relatórios completos, incluindo dados de assentos."
            rotaDestino="/admin/relatorios"
            icone="📊"
          />
        ) : carregando ? (
          <div className="stack" style={{ gap: 'var(--sp-3)' }}>
            {Array.from({ length: 3 }).map((_, i) => <div key={i} className="skeleton" style={{ height: 90 }} />)}
          </div>
        ) : (
          <>
            {/* ─── Analytics de Ingressos ─── */}
            <section style={{ marginBottom: 'var(--sp-6)' }}>
              <h2 style={{ marginBottom: 'var(--sp-4)' }}>Analytics de Ingressos</h2>
              {analyticsNegado ? (
                <div className="surface surface--pad" style={{ borderLeft: '3px solid var(--warn)' }}>
                  <p className="muted" style={{ margin: 0 }}>
                    Analytics de ingressos só está disponível para o organizador do evento. Faça login como organizador para visualizar.
                  </p>
                </div>
              ) : analytics && (
                <div className="row wrap" style={{ gap: 'var(--sp-3)' }}>
                  <StatCard n={analytics.totalVendidos} label="Vendidos" />
                  <StatCard n={analytics.totalRevendidos} label="Revendidos" />
                  <StatCard n={analytics.totalDisponiveis} label="Disponíveis" />
                  <StatCard n={analytics.totalCapacidade} label="Capacidade total" />
                  <StatCard n={`${analytics.taxaOcupacao.toFixed(1)}%`} label="Taxa de ocupação" cor="var(--brand-strong)" />
                  <StatCard n={`${analytics.taxaRevenda.toFixed(1)}%`} label="Taxa de revenda" />
                  <StatCard n={analytics.mediaAvaliacao.toFixed(1)} label="Média avaliações" cor="var(--warn)" />
                  <StatCard n={analytics.totalAvaliacoes} label="Total avaliações" />
                </div>
              )}
            </section>

            {/* ─── Mapa de Assentos ─── */}
            <section style={{ marginBottom: 'var(--sp-6)' }}>
              <h2 style={{ marginBottom: 'var(--sp-4)' }}>Mapa de Assentos</h2>
              {semMapa || !assentos ? (
                <div className="surface surface--pad">
                  <p className="muted" style={{ margin: 0 }}>Este evento não possui mapa de assentos configurado.</p>
                </div>
              ) : (
                <div className="surface surface--pad">
                  <h3 style={{ marginBottom: 'var(--sp-3)', fontSize: '0.9375rem' }}>Status dos assentos</h3>
                  <div className="row wrap" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-5)' }}>
                    <StatCard n={assentos.total} label="Total" />
                    <StatCard n={assentos.disponiveis} label="Disponíveis" cor="#16a34a" />
                    <StatCard n={assentos.reservados} label="Reservados" cor="var(--warn)" />
                    <StatCard n={assentos.vendidos} label="Vendidos" cor="var(--brand-strong)" />
                    <StatCard n={assentos.bloqueados} label="Bloqueados" cor="var(--danger)" />
                  </div>

                  <div className="stack" style={{ gap: 'var(--sp-3)', marginBottom: 'var(--sp-5)' }}>
                    {[
                      { label: 'Disponíveis', valor: assentos.disponiveis, cor: '#16a34a' },
                      { label: 'Reservados', valor: assentos.reservados, cor: '#d97706' },
                      { label: 'Vendidos', valor: assentos.vendidos, cor: '#7c3aed' },
                      { label: 'Bloqueados', valor: assentos.bloqueados, cor: '#ef4444' },
                    ].map(({ label, valor, cor }) => {
                      const pct = assentos.total > 0 ? (valor / assentos.total) * 100 : 0
                      return (
                        <div key={label}>
                          <div className="between" style={{ marginBottom: 4 }}>
                            <span style={{ fontSize: '0.875rem', color: 'var(--ink-2)' }}>{label}</span>
                            <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>
                              {valor} <span className="muted">({pct.toFixed(1)}%)</span>
                            </span>
                          </div>
                          <div style={{ height: 10, background: 'var(--surface-2)', borderRadius: 5, overflow: 'hidden' }}>
                            <div style={{ height: '100%', width: `${pct}%`, background: cor, borderRadius: 5, transition: 'width 0.6s ease' }} />
                          </div>
                        </div>
                      )
                    })}
                  </div>

                  <h3 style={{ marginBottom: 'var(--sp-3)', fontSize: '0.9375rem' }}>Por tipo</h3>
                  <div className="row wrap" style={{ gap: 'var(--sp-3)' }}>
                    <StatCard n={assentos.normal} label="Normal" />
                    <StatCard n={assentos.vip} label="VIP" cor="#a855f7" />
                    <StatCard n={assentos.acessibilidade} label="Acessibilidade" cor="#0891b2" />
                  </div>
                </div>
              )}
            </section>

            {/* ─── CRUD Snapshots ─── */}
            <section>
              <h2 style={{ marginBottom: 'var(--sp-4)' }}>Snapshots do Dashboard</h2>

              {msg && (
                <div className={`auth-alert ${msg.ok ? 'auth-alert--ok' : 'auth-alert--err'}`} style={{ marginBottom: 'var(--sp-4)' }}>
                  {msg.ok ? <CheckCircle2 size={18} /> : <AlertTriangle size={18} />} {msg.texto}
                </div>
              )}

              <div className="surface surface--pad" style={{ marginBottom: 'var(--sp-4)' }}>
                <h3 style={{ marginBottom: 'var(--sp-3)', fontSize: '0.9375rem' }}>Salvar foto do dashboard</h3>
                <div className="row wrap" style={{ gap: 'var(--sp-3)' }}>
                  <input
                    className="input"
                    placeholder="Nome do snapshot (opcional)"
                    value={tituloSnap}
                    onChange={e => setTituloSnap(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && salvarSnapshot()}
                    style={{ flex: 1, minWidth: 200 }}
                  />
                  <button className="btn" onClick={salvarSnapshot} disabled={!analytics && !assentos}>
                    <Camera size={17} /> Salvar foto do dashboard
                  </button>
                </div>
                <p className="muted" style={{ fontSize: '0.8125rem', marginTop: 'var(--sp-2)' }}>
                  Gera um PNG com os dados atuais, salva no histórico e baixa automaticamente para sua máquina.
                </p>
              </div>

              {snapshots.length === 0 ? (
                <div className="empty">
                  <Camera size={36} /><h3>Nenhum snapshot salvo</h3>
                  <p className="muted">Capture o dashboard de um evento para acessar o histórico aqui.</p>
                </div>
              ) : (
                <div className="stack" style={{ gap: 'var(--sp-3)' }}>
                  {snapshots.map(snap => (
                    <div key={snap.id} className="surface row" style={{ gap: 'var(--sp-4)', padding: 'var(--sp-4) var(--sp-5)' }}>
                      <img
                        src={snap.imagemBase64}
                        alt={snap.titulo}
                        style={{ width: 130, height: 72, objectFit: 'cover', borderRadius: 6, border: '1px solid var(--border)', flexShrink: 0 }}
                      />
                      <div className="grow" style={{ minWidth: 0 }}>
                        {editandoId === snap.id ? (
                          <div className="row" style={{ gap: 'var(--sp-2)', marginBottom: 4 }}>
                            <input
                              className="input"
                              value={editandoTitulo}
                              onChange={e => setEditandoTitulo(e.target.value)}
                              style={{ flex: 1 }}
                              autoFocus
                              onKeyDown={e => { if (e.key === 'Enter') confirmarEdicao(snap.id); if (e.key === 'Escape') setEditandoId(null) }}
                            />
                            <button className="btn btn--sm" onClick={() => confirmarEdicao(snap.id)}><Save size={14} /></button>
                            <button className="btn btn--sm btn--ghost" onClick={() => setEditandoId(null)}><X size={14} /></button>
                          </div>
                        ) : (
                          <div style={{ fontWeight: 600, marginBottom: 2 }}>{snap.titulo}</div>
                        )}
                        <div className="muted" style={{ fontSize: '0.8125rem' }}>
                          {snap.eventoNome && <>{snap.eventoNome} · </>}
                          {new Date(snap.criadoEm).toLocaleString('pt-BR')}
                        </div>
                      </div>
                      <div className="row" style={{ gap: 'var(--sp-2)', flexShrink: 0, alignItems: 'center' }}>
                        <button className="btn btn--sm btn--ghost" onClick={() => baixarSnapshot(snap)} title="Baixar PNG"><Download size={15} /></button>
                        <button className="btn btn--sm btn--ghost" onClick={() => { setEditandoId(snap.id); setEditandoTitulo(snap.titulo) }} title="Renomear"><Edit2 size={15} /></button>
                        <button className="btn btn--sm btn--danger" onClick={() => deletarSnapshot(snap.id)} title="Excluir"><Trash2 size={15} /></button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        )}
      </div>
    </>
  )
}

function StatCard({ n, label, cor }: { n: number | string; label: string; cor?: string }) {
  return (
    <div className="stat">
      <span className="stat__n" style={cor ? { color: cor } : undefined}>{n}</span>
      <span className="stat__l">{label}</span>
    </div>
  )
}
