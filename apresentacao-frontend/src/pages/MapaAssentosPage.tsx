import { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import Navbar from '../components/Navbar'
import { api } from '../services/api'

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

const corStatus: Record<string, string> = {
  DISPONIVEL: '#22c55e',
  RESERVADO: '#f59e0b',
  VENDIDO: '#64748b',
  BLOQUEADO: '#1e293b',
}

const corTipo: Record<string, string> = {
  NORMAL: '#3b82f6',
  VIP: '#a855f7',
  ACESSIBILIDADE: '#06b6d4',
  BLOQUEADO: '#1e293b',
}

const formatMoeda = (v: number) =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v)

export default function MapaAssentosPage() {
  const [searchParams] = useSearchParams()
  const eventoId = searchParams.get('eventoId')
  const { usuario } = useAuth()

  const [mapa, setMapa] = useState<MapaAssentos | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [selecionados, setSelecionados] = useState<number[]>([])
  const [mensagem, setMensagem] = useState<{ texto: string; tipo: 'ok' | 'erro' } | null>(null)

  // Form para criar mapa (organizador)
  const [criando, setCriando] = useState(false)
  const [form, setForm] = useState({
    totalLinhas: '5',
    totalColunas: '10',
    precoNormal: '100.00',
    precoVip: '150.00',
  })

  const carregarMapa = () => {
    if (!eventoId) return
    setCarregando(true)
    api.get('/mapas-assentos', { params: { eventoId } })
      .then(r => setMapa(r.data))
      .catch(() => setMapa(null))
      .finally(() => setCarregando(false))
  }

  useEffect(() => { carregarMapa() }, [eventoId])

  const exibirMensagem = (texto: string, tipo: 'ok' | 'erro') => {
    setMensagem({ texto, tipo })
    setTimeout(() => setMensagem(null), 4000)
  }

  const toggleSelecionado = (assento: Assento) => {
    if (assento.status !== 'DISPONIVEL') return
    setSelecionados(prev =>
      prev.includes(assento.id)
        ? prev.filter(id => id !== assento.id)
        : [...prev, assento.id]
    )
  }

  const reservar = () => {
    if (!mapa || selecionados.length === 0) return
    api.post(`/mapas-assentos/${mapa.id}/reservar`,
      { assentoIds: selecionados },
      { headers: { 'X-Usuario-Id': usuario?.id } }
    )
      .then(() => {
        exibirMensagem(`${selecionados.length} assento(s) reservado(s) por 5 minutos!`, 'ok')
        setSelecionados([])
        carregarMapa()
      })
      .catch(err => {
        exibirMensagem(err.response?.data?.motivo ?? 'Erro ao reservar assentos.', 'erro')
      })
  }

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

  const ehOrganizador = usuario?.papeis?.includes('ORGANIZADOR')

  // Agrupa assentos por seção+fileira para renderizar grade
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

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '32px 16px' }}>

        {/* Cabeçalho */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 }}>
          <div>
            <h1 style={{ fontSize: 28, fontWeight: 700, color: '#0f172a', margin: 0 }}>
              🗺️ Mapa de Assentos
            </h1>
            <p style={{ color: '#64748b', marginTop: 4 }}>
              Selecione seu(s) assento(s) e garanta sua posição
            </p>
          </div>
          {ehOrganizador && !mapa && !criando && (
            <button onClick={() => setCriando(true)} style={{
              background: '#1d4ed8', color: '#fff', border: 'none',
              borderRadius: 8, padding: '10px 20px', cursor: 'pointer', fontWeight: 600
            }}>
              + Criar Mapa
            </button>
          )}
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

        {/* Form criar mapa */}
        {criando && (
          <form onSubmit={criarMapa} style={{
            background: '#f8fafc', border: '1px solid #e2e8f0',
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
                  <span style={{ fontSize: 13, color: '#475569', fontWeight: 500 }}>{label}</span>
                  <input
                    type="number"
                    value={(form as any)[field]}
                    onChange={e => setForm(prev => ({ ...prev, [field]: e.target.value }))}
                    min="1" step="0.01" required
                    style={{ padding: '8px 12px', borderRadius: 6, border: '1px solid #cbd5e1', fontSize: 14 }}
                  />
                </label>
              ))}
            </div>
            <p style={{ color: '#64748b', fontSize: 13, marginTop: 12 }}>
              💡 Assentos VIP são gerados na 1ª fileira (centro) com +50% no preço.
              Assentos de Acessibilidade ficam na última fileira com 50% de desconto.
            </p>
            <div style={{ display: 'flex', gap: 12, marginTop: 16 }}>
              <button type="submit" style={btnStyle('#16a34a')}>Criar Mapa</button>
              <button type="button" onClick={() => setCriando(false)} style={btnStyle('#64748b')}>Cancelar</button>
            </div>
          </form>
        )}

        {/* Legenda */}
        {mapa && (
          <div style={{ display: 'flex', gap: 16, marginBottom: 20, flexWrap: 'wrap' }}>
            {[
              { label: 'Disponível', cor: corStatus.DISPONIVEL },
              { label: 'Reservado', cor: corStatus.RESERVADO },
              { label: 'Vendido', cor: corStatus.VENDIDO },
              { label: 'Normal', cor: corTipo.NORMAL },
              { label: 'VIP', cor: corTipo.VIP },
              { label: 'Acessível', cor: corTipo.ACESSIBILIDADE },
            ].map(({ label, cor }) => (
              <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13 }}>
                <span style={{ width: 16, height: 16, borderRadius: 4, background: cor, display: 'inline-block' }} />
                <span style={{ color: '#475569' }}>{label}</span>
              </div>
            ))}
          </div>
        )}

        {carregando ? (
          <p style={{ color: '#64748b', textAlign: 'center', padding: 40 }}>Carregando mapa...</p>
        ) : !mapa ? (
          <div style={{
            textAlign: 'center', padding: '60px 20px', background: '#f8fafc',
            borderRadius: 12, border: '2px dashed #e2e8f0'
          }}>
            <p style={{ fontSize: 48, margin: '0 0 16px' }}>💺</p>
            <p style={{ color: '#64748b', fontSize: 16 }}>Nenhum mapa de assentos configurado para este evento.</p>
            {ehOrganizador && (
              <button onClick={() => setCriando(true)} style={{ ...btnStyle('#1d4ed8'), marginTop: 16 }}>
                Criar Mapa de Assentos
              </button>
            )}
          </div>
        ) : (
          <>
            {/* Palco */}
            <div style={{
              background: '#0f172a', color: '#94a3b8', borderRadius: 8,
              padding: '10px 0', textAlign: 'center', fontWeight: 600,
              fontSize: 14, letterSpacing: 4, marginBottom: 24
            }}>
              ▬▬▬▬▬ PALCO ▬▬▬▬▬
            </div>

            {/* Grade de assentos */}
            <div style={{ overflowX: 'auto' }}>
              {[...agruparPorFileira(mapa.assentos).entries()].map(([fileira, assentos]) => (
                <div key={fileira} style={{ display: 'flex', gap: 6, marginBottom: 8, alignItems: 'center' }}>
                  <span style={{ width: 20, color: '#94a3b8', fontSize: 13, fontWeight: 600, flexShrink: 0 }}>
                    {fileira}
                  </span>
                  {assentos
                    .sort((a, b) => {
                      const na = parseInt(a.codigo.substring(1)) || 0
                      const nb = parseInt(b.codigo.substring(1)) || 0
                      return na - nb
                    })
                    .map(a => {
                      const isSelecionado = selecionados.includes(a.id)
                      const ehMeuReservado = a.reservadoPor === usuario?.id
                      const corBase = a.status === 'DISPONIVEL' ? corTipo[a.tipo] : corStatus[a.status]
                      return (
                        <button
                          key={a.id}
                          onClick={() => toggleSelecionado(a)}
                          title={`${a.codigo} — ${a.tipo} — ${formatMoeda(a.preco)} — ${a.status}`}
                          style={{
                            width: 36, height: 36, borderRadius: 6,
                            border: isSelecionado ? '3px solid #fbbf24' : ehMeuReservado ? '3px solid #f59e0b' : '2px solid transparent',
                            background: isSelecionado ? '#fbbf24' : corBase,
                            cursor: a.status === 'DISPONIVEL' ? 'pointer' : 'not-allowed',
                            fontSize: 9, color: '#fff', fontWeight: 600,
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            opacity: a.status === 'BLOQUEADO' ? 0.4 : 1,
                            transition: 'transform 0.1s',
                            transform: isSelecionado ? 'scale(1.1)' : 'scale(1)',
                            flexShrink: 0,
                          }}
                        >
                          {a.codigo.substring(1)}
                        </button>
                      )
                    })}
                </div>
              ))}
            </div>

            {/* Painel de seleção */}
            {selecionados.length > 0 && (
              <div style={{
                position: 'sticky', bottom: 20, marginTop: 32,
                background: '#0f172a', borderRadius: 12, padding: '16px 24px',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                boxShadow: '0 8px 32px rgba(0,0,0,0.3)'
              }}>
                <div>
                  <p style={{ color: '#94a3b8', margin: 0, fontSize: 13 }}>
                    {selecionados.length} assento(s) selecionado(s)
                  </p>
                  <p style={{ color: '#f1f5f9', fontWeight: 700, fontSize: 20, margin: '4px 0 0' }}>
                    {formatMoeda(totalSelecionado)}
                  </p>
                </div>
                <div style={{ display: 'flex', gap: 12 }}>
                  <button
                    onClick={() => setSelecionados([])}
                    style={{ background: '#334155', color: '#cbd5e1', border: 'none', borderRadius: 8, padding: '10px 16px', cursor: 'pointer' }}>
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
      </div>
    </>
  )
}

const btnStyle = (bg: string): React.CSSProperties => ({
  background: bg, color: '#fff', border: 'none',
  borderRadius: 8, padding: '10px 20px', cursor: 'pointer', fontWeight: 600
})
