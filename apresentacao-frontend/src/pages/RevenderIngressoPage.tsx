import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ingressoService, eventoService, tipoIngressoService, revendaService } from '../services/api'
import Navbar from '../components/Navbar'
import { USUARIO_ID, formatMoeda } from '../constants'

const TAXA_REVENDA = 0.10

interface Ingresso {
  id: string
  eventoId: number
  tipoIngressoId: number
  status: string
}

interface Evento {
  id: number
  nome: string
  dataHora: string
  local: string
  imagemCapaUrl?: string
  categoria?: string
}

interface TipoIngresso {
  id: number
  nome: string
  preco: number
}

export default function RevenderIngressoPage() {
  const { id: ingressoId } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [ingresso, setIngresso] = useState<Ingresso | null>(null)
  const [evento, setEvento] = useState<Evento | null>(null)
  const [tipo, setTipo] = useState<TipoIngresso | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [preco, setPreco] = useState('')
  const [publicando, setPublicando] = useState(false)
  const [erro, setErro] = useState('')

  useEffect(() => {
    if (!ingressoId) return
    ingressoService.detalhe(ingressoId)
      .then(async res => {
        const ing: Ingresso = res.data
        setIngresso(ing)

        const [evRes, tiposRes] = await Promise.all([
          eventoService.detalhe(ing.eventoId),
          tipoIngressoService.listar(ing.eventoId),
        ])
        setEvento(evRes.data)
        const tipoEncontrado = (tiposRes.data as TipoIngresso[]).find(t => t.id === ing.tipoIngressoId)
        if (tipoEncontrado) {
          setTipo(tipoEncontrado)
          setPreco(Number(tipoEncontrado.preco).toFixed(2).replace('.', ','))
        }
      })
      .catch(() => setErro('Ingresso não encontrado.'))
      .finally(() => setCarregando(false))
  }, [ingressoId])

  const precoNum = parseFloat(preco.replace(',', '.')) || 0
  const taxaReais = precoNum * TAXA_REVENDA
  const voceRecebe = precoNum - taxaReais

  const publicar = async () => {
    if (!ingressoId || precoNum <= 0) {
      setErro('Informe um preço válido.')
      return
    }
    setPublicando(true)
    setErro('')
    try {
      await revendaService.criar(USUARIO_ID, {
        ingressoIds: [ingressoId],
        preco: precoNum,
      })
      navigate('/meus-ingressos', { state: { sucesso: true } })
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      setErro(err.response?.data?.message ?? 'Erro ao publicar anúncio.')
      setPublicando(false)
    }
  }

  const imagemEvento = evento?.imagemCapaUrl ||
    'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=600&q=60'

  if (carregando) {
    return (
      <>
        <Navbar />
        <div style={{ textAlign: 'center', padding: '80px 0', color: '#94a3b8' }}>Carregando...</div>
      </>
    )
  }

  if (erro && !evento) {
    return (
      <>
        <Navbar />
        <div style={{ textAlign: 'center', padding: '80px 0', color: '#ef4444' }}>{erro}</div>
      </>
    )
  }

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 1100, margin: '0 auto', padding: '40px 24px' }}>
        <h1 style={{ fontSize: 28, fontWeight: 800, color: '#1e293b', marginBottom: 6 }}>Revender meu Ingresso</h1>
        <p style={{ color: '#64748b', fontSize: 14, marginBottom: 36 }}>
          Configure os detalhes da sua oferta para o festival.
        </p>

        <div style={{ display: 'flex', gap: 28, alignItems: 'flex-start' }}>
          {/* Coluna esquerda */}
          <div style={{ flex: 1 }}>
            {/* Card do evento */}
            <div style={{ background: '#fff', borderRadius: 16, overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', marginBottom: 20 }}>
              <div style={{ position: 'relative', height: 200 }}>
                <img src={imagemEvento} alt={evento?.nome} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 60%)' }} />
                {evento?.dataHora && (
                  <div style={{
                    position: 'absolute', top: 12, left: 12,
                    background: 'rgba(0,0,0,0.7)', color: '#fff',
                    borderRadius: 8, padding: '4px 12px', textAlign: 'center',
                  }}>
                    <div style={{ fontSize: 14, fontWeight: 700 }}>
                      {new Date(evento.dataHora).getDate().toString().padStart(2, '0')}
                    </div>
                    <div style={{ fontSize: 10, fontWeight: 600, textTransform: 'uppercase' }}>
                      {new Date(evento.dataHora).toLocaleDateString('pt-BR', { month: 'short' }).toUpperCase()}
                    </div>
                  </div>
                )}
              </div>
              <div style={{ padding: '16px 20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                  {evento?.categoria && (
                    <span style={{ background: '#eff6ff', color: '#1d4ed8', fontSize: 11, fontWeight: 700, padding: '3px 10px', borderRadius: 20, textTransform: 'uppercase' }}>
                      {evento.categoria}
                    </span>
                  )}
                  {tipo && (
                    <span style={{ fontSize: 13, color: '#64748b' }}>
                      Preço Original <strong style={{ color: '#1e293b' }}>{formatMoeda(tipo.preco)}</strong>
                    </span>
                  )}
                </div>
                <h2 style={{ fontSize: 18, fontWeight: 700, color: '#1e293b', marginBottom: 12 }}>{evento?.nome}</h2>
                <div style={{ display: 'flex', gap: 24 }}>
                  <div>
                    <p style={{ fontSize: 11, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 2 }}>Localização</p>
                    <p style={{ fontSize: 13, color: '#1e293b', fontWeight: 600 }}>📍 {evento?.local}</p>
                  </div>
                  {tipo && (
                    <div>
                      <p style={{ fontSize: 11, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 2 }}>Setor</p>
                      <p style={{ fontSize: 13, color: '#1e293b', fontWeight: 600 }}>🎟️ {tipo.nome}</p>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Revenda Segura */}
            <div style={{ background: '#fff', borderRadius: 16, padding: 24, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
                <span style={{ fontSize: 24 }}>🛡️</span>
                <h3 style={{ fontSize: 16, fontWeight: 700, color: '#1e293b' }}>Revenda Segura</h3>
              </div>
              <p style={{ fontSize: 13, color: '#64748b', lineHeight: 1.6, marginBottom: 14 }}>
                Para garantir uma experiência justa, seguimos regras rigorosas de{' '}
                <span style={{ color: '#1d4ed8', textDecoration: 'underline', cursor: 'pointer' }}>transparência</span>{' '}
                e{' '}
                <span style={{ color: '#1d4ed8', textDecoration: 'underline', cursor: 'pointer' }}>segurança</span>.
              </p>
              <ul style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {[
                  'Sua revenda só será publicada se os ingressos oficiais estiverem esgotados.',
                  'O pagamento é garantido e processado via Ingressefy Pay.',
                  'O novo ingresso é gerado automaticamente para o comprador, invalidando o seu anterior.',
                ].map((item, i) => (
                  <li key={i} style={{ display: 'flex', alignItems: 'flex-start', gap: 10, fontSize: 13, color: '#475569' }}>
                    <span style={{ color: '#16a34a', fontSize: 16, flexShrink: 0, marginTop: 1 }}>✅</span>
                    {item}
                  </li>
                ))}
              </ul>
            </div>
          </div>

          {/* Coluna direita */}
          <div style={{ width: 340, flexShrink: 0 }}>
            <div style={{ background: '#fff', borderRadius: 16, padding: 24, boxShadow: '0 2px 8px rgba(0,0,0,0.08)', marginBottom: 16 }}>
              <h3 style={{ fontSize: 17, fontWeight: 700, color: '#1e293b', marginBottom: 20 }}>Configuração de Preço</h3>

              <div style={{ marginBottom: 16 }}>
                <label style={{ display: 'block', fontSize: 11, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.8, marginBottom: 8 }}>
                  Novo Preço de Venda
                </label>
                <div style={{ position: 'relative' }}>
                  <span style={{ position: 'absolute', left: 16, top: '50%', transform: 'translateY(-50%)', color: '#64748b', fontSize: 15, fontWeight: 600 }}>R$</span>
                  <input
                    type="text"
                    value={preco}
                    onChange={e => setPreco(e.target.value)}
                    placeholder="0,00"
                    style={{
                      width: '100%',
                      padding: '16px 16px 16px 48px',
                      border: '1px solid #e2e8f0',
                      borderRadius: 10,
                      fontSize: 22,
                      fontWeight: 700,
                      color: '#1e293b',
                      outline: 'none',
                    }}
                  />
                </div>
                <p style={{ fontSize: 12, color: '#94a3b8', marginTop: 8 }}>
                  💡 Dica: Preços próximos ao original vendem 3x mais rápido.
                </p>
              </div>

              {/* Breakdown */}
              <div style={{ background: '#f8fafc', borderRadius: 10, padding: '14px 16px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 10 }}>
                  <span style={{ fontSize: 13, color: '#64748b' }}>Seu Preço de Venda</span>
                  <span style={{ fontSize: 13, fontWeight: 600, color: '#1e293b' }}>{formatMoeda(precoNum)}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12, paddingBottom: 12, borderBottom: '1px solid #e2e8f0' }}>
                  <span style={{ fontSize: 13, color: '#64748b' }}>
                    Taxa de Serviço <span style={{ fontSize: 11, color: '#94a3b8' }}>ℹ️</span>
                  </span>
                  <span style={{ fontSize: 13, fontWeight: 600, color: '#ef4444' }}>- {formatMoeda(taxaReais)} (10%)</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ fontSize: 12, fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: 0.5 }}>
                    Você Receberá
                  </span>
                  <div style={{ textAlign: 'right' }}>
                    <p style={{ fontSize: 11, color: '#94a3b8' }}>Estimativa de depósito</p>
                    <p style={{ fontSize: 20, fontWeight: 800, color: '#1d4ed8' }}>{formatMoeda(voceRecebe)}</p>
                  </div>
                </div>
              </div>
            </div>

            {erro && (
              <div style={{ background: '#fef2f2', border: '1px solid #fecaca', borderRadius: 10, padding: '12px 14px', marginBottom: 16 }}>
                <p style={{ fontSize: 13, color: '#ef4444' }}>{erro}</p>
              </div>
            )}

            <button
              onClick={publicar}
              disabled={publicando || precoNum <= 0}
              style={{
                width: '100%',
                background: publicando || precoNum <= 0 ? '#93c5fd' : '#1d4ed8',
                color: '#fff',
                border: 'none',
                borderRadius: 12,
                padding: '16px',
                fontSize: 16,
                fontWeight: 700,
                cursor: publicando || precoNum <= 0 ? 'default' : 'pointer',
                marginBottom: 12,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: 8,
              }}
            >
              ↑ {publicando ? 'Publicando...' : 'Publicar para Revenda'}
            </button>

            <p style={{ textAlign: 'center', fontSize: 11, color: '#94a3b8', lineHeight: 1.5 }}>
              Ao clicar em publicar, você concorda com os{' '}
              <span style={{ color: '#1d4ed8', cursor: 'pointer' }}>Termos de Revenda</span>.
            </p>

            {ingresso && (
              <button
                onClick={() => navigate('/meus-ingressos')}
                style={{ width: '100%', background: 'none', border: 'none', color: '#94a3b8', fontSize: 13, marginTop: 12, cursor: 'pointer' }}
              >
                ← Voltar aos meus ingressos
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  )
}
