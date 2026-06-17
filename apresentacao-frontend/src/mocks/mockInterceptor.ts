import { api } from '../services/api'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'

const MOCK_KEY = 'ingressify_mock_mode'
let mockMode = false
let interceptorId: number | null = null
let nextId = 9000

const mkId = () => ++nextId

// ── Users ──────────────────────────────────────────────────────────────────
export const MOCK_USERS = [
  { id: 1,  nome: 'Admin Sistema',      email: 'admin@ingressify.com',  papeis: ['ADMIN'] },
  { id: 2,  nome: 'Maria Organizadora', email: 'maria@ingressify.com',  papeis: ['ORGANIZADOR', 'COMPRADOR'] },
  { id: 3,  nome: 'Ana Compradora',     email: 'ana@ingressify.com',    papeis: ['COMPRADOR'] },
  { id: 4,  nome: 'Carlos Operador',    email: 'carlos@ingressify.com', papeis: ['OPERADOR_PORTA'] },
  { id: 5,  nome: 'Pedro Comprador',    email: 'pedro@ingressify.com',  papeis: ['COMPRADOR'] },
  { id: 7,  nome: 'João Organizador',   email: 'joao@ingressify.com',   papeis: ['ORGANIZADOR', 'COMPRADOR'] },
]

// ── Mutable state ──────────────────────────────────────────────────────────
type Evento = {
  id: number; nome: string; descricao: string; local: string; cidade: string
  data: string; categoria: string; status: string; organizadorId: number
  capacidade: number; imagemUrl?: string
}

type TipoIngresso = {
  id: number; eventoId: number; nome: string; preco: number
  quantidadeTotal: number; quantidadeVendida: number; descricao?: string
}

type Ingresso = {
  id: string; usuarioId: number; eventoId: number; tipoIngressoId: number
  status: string; preco: number; codigoQr: string; criadoEm: string
  nomeEvento?: string; nomeTipo?: string
}

type Anuncio = {
  id: number; ingressoId: string; vendedorId: number; eventoId: number
  preco: number; status: string; criadoEm: string
}

type Denuncia = {
  id: number; anuncioId: number; denuncianteId: number; motivo: string
  descricao: string; status: string; decisao: string | null
  criadaEm: string; decididaEm: string | null
}

type Avaliacao = {
  id: number; eventoId: number; usuarioId: number; nota: number
  comentario: string; criadaEm: string; respostaOrganizador?: string
}

type Post = {
  id: number; eventoId: number; autorId: number; titulo: string
  conteudo: string; fixado: boolean; criadoEm: string
}

type Sorteio = {
  id: number; eventoId: number; organizadorId: number; descricao: string
  premio: string; status: string; criadoEm: string; inscritos: number[]
  ganhadorId?: number
}

type GrupoCompra = {
  id: number; eventoId: number; tipoIngressoId: number; liderId: number
  tamanhoMinimo: number; tamanhoMaximo: number; status: string
  participantes: number[]; criadoEm: string
}

type Assento = {
  id: number; mapaId: number; fileira: string; numero: number
  status: string; usuarioId?: number
}

type MapaAssentos = {
  id: number; eventoId: number; nome: string; assentos: Assento[]
}

type Checkin = { ingressoId: string; operadorId: number; eventoId: number; realizadoEm: string }

// ── Seed data ──────────────────────────────────────────────────────────────
let EVENTOS: Evento[] = [
  { id: 1, nome: 'Festival de Verão 2026', descricao: 'O maior festival de verão do Nordeste com shows nacionais e internacionais.', local: 'Arena Recife', cidade: 'Recife', data: '2026-12-15T18:00:00', categoria: 'Música', status: 'PUBLICADO', organizadorId: 2, capacidade: 500 },
  { id: 2, nome: 'Rock in Recife', descricao: 'Festival de rock com as maiores bandas do Brasil.', local: 'Espaço Cultural', cidade: 'Recife', data: '2026-11-20T19:00:00', categoria: 'Música', status: 'PUBLICADO', organizadorId: 2, capacidade: 300 },
  { id: 3, nome: 'Tech Conference CESAR', descricao: 'Conferência de tecnologia com palestras sobre IA, cloud e startups.', local: 'CESAR - Recife', cidade: 'Recife', data: '2026-09-10T09:00:00', categoria: 'Tecnologia', status: 'PUBLICADO', organizadorId: 7, capacidade: 200 },
  { id: 4, nome: 'Show de Jazz', descricao: 'Uma noite intimista de jazz com músicos locais.', local: 'Teatro Santa Isabel', cidade: 'Recife', data: '2026-10-05T20:00:00', categoria: 'Música', status: 'RASCUNHO', organizadorId: 7, capacidade: 150 },
  { id: 5, nome: 'Feira Maker Recife', descricao: 'Exposição de projetos DIY, robótica e impressão 3D.', local: 'RioMar Recife', cidade: 'Recife', data: '2026-08-22T10:00:00', categoria: 'Tecnologia', status: 'PUBLICADO', organizadorId: 2, capacidade: 400 },
]

let TIPOS: TipoIngresso[] = [
  { id: 11, eventoId: 1, nome: 'Pista',     preco: 80,  quantidadeTotal: 300, quantidadeVendida: 180 },
  { id: 12, eventoId: 1, nome: 'VIP',       preco: 160, quantidadeTotal: 150, quantidadeVendida: 90  },
  { id: 13, eventoId: 1, nome: 'Camarote',  preco: 300, quantidadeTotal: 50,  quantidadeVendida: 25  },
  { id: 21, eventoId: 2, nome: 'Pista',     preco: 120, quantidadeTotal: 200, quantidadeVendida: 120 },
  { id: 22, eventoId: 2, nome: 'VIP',       preco: 220, quantidadeTotal: 100, quantidadeVendida: 50  },
  { id: 31, eventoId: 3, nome: 'Geral',     preco: 50,  quantidadeTotal: 150, quantidadeVendida: 60  },
  { id: 32, eventoId: 3, nome: 'Estudante', preco: 30,  quantidadeTotal: 50,  quantidadeVendida: 20  },
  { id: 41, eventoId: 4, nome: 'Geral',     preco: 70,  quantidadeTotal: 100, quantidadeVendida: 0   },
  { id: 51, eventoId: 5, nome: 'Entrada',   preco: 25,  quantidadeTotal: 300, quantidadeVendida: 200 },
]

let INGRESSOS: Ingresso[] = [
  { id: 'ING-001', usuarioId: 3, eventoId: 1, tipoIngressoId: 11, status: 'ATIVO', preco: 80,  codigoQr: 'QR-ING-001', criadoEm: '2026-05-10T10:00:00', nomeEvento: 'Festival de Verão 2026', nomeTipo: 'Pista' },
  { id: 'ING-002', usuarioId: 3, eventoId: 1, tipoIngressoId: 12, status: 'ATIVO', preco: 160, codigoQr: 'QR-ING-002', criadoEm: '2026-05-10T10:01:00', nomeEvento: 'Festival de Verão 2026', nomeTipo: 'VIP' },
  { id: 'ING-003', usuarioId: 3, eventoId: 3, tipoIngressoId: 31, status: 'ATIVO', preco: 50,  codigoQr: 'QR-ING-003', criadoEm: '2026-06-01T14:00:00', nomeEvento: 'Tech Conference CESAR', nomeTipo: 'Geral' },
  { id: 'ING-004', usuarioId: 5, eventoId: 2, tipoIngressoId: 21, status: 'ATIVO', preco: 120, codigoQr: 'QR-ING-004', criadoEm: '2026-05-20T09:00:00', nomeEvento: 'Rock in Recife',          nomeTipo: 'Pista' },
  { id: 'ING-005', usuarioId: 5, eventoId: 5, tipoIngressoId: 51, status: 'ATIVO', preco: 25,  codigoQr: 'QR-ING-005', criadoEm: '2026-06-05T11:00:00', nomeEvento: 'Feira Maker Recife',      nomeTipo: 'Entrada' },
  { id: 'ING-006', usuarioId: 2, eventoId: 1, tipoIngressoId: 13, status: 'ATIVO', preco: 300, codigoQr: 'QR-ING-006', criadoEm: '2026-04-01T08:00:00', nomeEvento: 'Festival de Verão 2026', nomeTipo: 'Camarote' },
  { id: 'ING-007', usuarioId: 7, eventoId: 3, tipoIngressoId: 32, status: 'ATIVO', preco: 30,  codigoQr: 'QR-ING-007', criadoEm: '2026-06-02T09:30:00', nomeEvento: 'Tech Conference CESAR', nomeTipo: 'Estudante' },
]

const SALDO: Record<number, number> = { 1: 0, 2: 1200, 3: 250, 4: 0, 5: 100, 7: 800 }

let TRANSACOES: Record<number, { id: number; tipo: string; valor: number; descricao: string; criadoEm: string }[]> = {
  2: [
    { id: 1, tipo: 'RECEBIMENTO', valor: 800,  descricao: 'Venda ingressos Festival de Verão', criadoEm: '2026-05-15T12:00:00' },
    { id: 2, tipo: 'RECEBIMENTO', valor: 400,  descricao: 'Venda ingressos Rock in Recife',    criadoEm: '2026-05-20T12:00:00' },
  ],
  3: [
    { id: 3, tipo: 'COMPRA',      valor: -80,  descricao: 'Ingresso Pista — Festival de Verão', criadoEm: '2026-05-10T10:00:00' },
    { id: 4, tipo: 'COMPRA',      valor: -160, descricao: 'Ingresso VIP — Festival de Verão',   criadoEm: '2026-05-10T10:01:00' },
    { id: 5, tipo: 'RECARGA',     valor: 490,  descricao: 'Recarga via PIX',                   criadoEm: '2026-05-09T08:00:00' },
  ],
  5: [
    { id: 6, tipo: 'COMPRA',      valor: -120, descricao: 'Ingresso Pista — Rock in Recife',   criadoEm: '2026-05-20T09:00:00' },
    { id: 7, tipo: 'RECARGA',     valor: 245,  descricao: 'Recarga via PIX',                   criadoEm: '2026-05-19T15:00:00' },
  ],
}

let ANUNCIOS: Anuncio[] = [
  { id: 201, ingressoId: 'ING-001', vendedorId: 3, eventoId: 1, preco: 150, status: 'DISPONIVEL', criadoEm: '2026-06-10T14:00:00' },
  { id: 202, ingressoId: 'ING-004', vendedorId: 5, eventoId: 2, preco: 180, status: 'DISPONIVEL', criadoEm: '2026-06-12T09:00:00' },
  { id: 203, ingressoId: 'ING-002', vendedorId: 3, eventoId: 1, preco: 280, status: 'DISPONIVEL', criadoEm: '2026-06-13T11:00:00' },
]

let DENUNCIAS: Denuncia[] = [
  { id: 101, anuncioId: 201, denuncianteId: 5, motivo: 'PRECO_ABUSIVO',           descricao: 'Vendedor cobra quase 2x o valor de face sem justificativa.',         status: 'PENDENTE',  decisao: null,                criadaEm: '2026-06-10T14:30:00', decididaEm: null },
  { id: 102, anuncioId: 202, denuncianteId: 3, motivo: 'INGRESSO_SUSPEITO',       descricao: 'O QR Code parece já ter sido utilizado. Possível duplicata.',         status: 'PENDENTE',  decisao: null,                criadaEm: '2026-06-12T09:15:00', decididaEm: null },
  { id: 103, anuncioId: 203, denuncianteId: 7, motivo: 'COMPORTAMENTO_INADEQUADO',descricao: 'Vendedor ameaçou cancelar após pagamento confirmado.',                 status: 'RESOLVIDA', decisao: 'VENDEDOR_AVISADO',  criadaEm: '2026-06-08T11:00:00', decididaEm: '2026-06-09T16:30:00' },
  { id: 104, anuncioId: 201, denuncianteId: 7, motivo: 'OUTRO',                   descricao: 'Anunciado setor VIP mas na realidade é pista comum.',                  status: 'PENDENTE',  decisao: null,                criadaEm: '2026-06-15T10:20:00', decididaEm: null },
]

let AVALIACOES: Record<number, Avaliacao[]> = {
  1: [
    { id: 1, eventoId: 1, usuarioId: 3, nota: 5, comentario: 'Incrível! Melhor show que já fui. Organização impecável.',           criadaEm: '2026-01-20T10:00:00', respostaOrganizador: 'Muito obrigada, Ana! Fico feliz que tenha curtido!' },
    { id: 2, eventoId: 1, usuarioId: 5, nota: 4, comentario: 'Ótimo evento, mas a fila para o banheiro estava enorme.',            criadaEm: '2026-01-21T09:00:00' },
    { id: 3, eventoId: 1, usuarioId: 7, nota: 3, comentario: 'Sonorização podia ser melhor no setor VIP.',                        criadaEm: '2026-01-22T11:00:00' },
  ],
  2: [
    { id: 4, eventoId: 2, usuarioId: 3, nota: 5, comentario: 'Setlist perfeito! Voltaria com certeza.',                           criadaEm: '2026-02-15T10:00:00' },
    { id: 5, eventoId: 2, usuarioId: 5, nota: 4, comentario: 'Muito bom, mas poderia ter mais bandas locais.',                    criadaEm: '2026-02-16T08:00:00', respostaOrganizador: 'Anotado para a próxima edição, Pedro!' },
  ],
  3: [
    { id: 6, eventoId: 3, usuarioId: 3, nota: 5, comentario: 'Palestras muito relevantes. Aprendi muito sobre IA.',               criadaEm: '2026-03-11T18:00:00' },
  ],
}

let POSTS: Record<number, Post[]> = {
  1: [
    { id: 1, eventoId: 1, autorId: 2, titulo: 'Line-up completo anunciado!',   conteudo: 'Confirmamos todos os artistas. Veja o line-up completo no site!', fixado: true,  criadoEm: '2026-05-01T10:00:00' },
    { id: 2, eventoId: 1, autorId: 2, titulo: 'Informações de acesso',         conteudo: 'Portões abrem às 17h. Tragam documento com foto.',                  fixado: false, criadoEm: '2026-05-15T09:00:00' },
    { id: 3, eventoId: 1, autorId: 2, titulo: 'Última chamada para ingressos', conteudo: 'Restam apenas 30 ingressos Pista. Garanta o seu!',                  fixado: false, criadoEm: '2026-06-01T14:00:00' },
  ],
  2: [
    { id: 4, eventoId: 2, autorId: 2, titulo: 'Confirmação de abertura',       conteudo: 'Banda de abertura confirmada: The Warnings (MX)!',                  fixado: true,  criadoEm: '2026-05-10T11:00:00' },
  ],
  3: [
    { id: 5, eventoId: 3, autorId: 7, titulo: 'Grade de palestras publicada',  conteudo: 'Confira a grade completa no site do CESAR.',                        fixado: true,  criadoEm: '2026-06-01T08:00:00' },
    { id: 6, eventoId: 3, autorId: 7, titulo: 'Workshop de IA — vagas extras', conteudo: 'Abrimos 20 vagas extras no workshop de IA Generativa.',             fixado: false, criadoEm: '2026-06-10T10:00:00' },
  ],
}

let SORTEIOS: Sorteio[] = [
  { id: 1, eventoId: 1, organizadorId: 2, descricao: 'Sorteio Backstage Pass', premio: 'Acesso ao backstage + encontro com artistas', status: 'ABERTO',    criadoEm: '2026-05-01T10:00:00', inscritos: [3, 5, 7] },
  { id: 2, eventoId: 2, organizadorId: 2, descricao: 'Meet & Greet',           premio: 'Foto com a banda principal',                  status: 'ENCERRADO', criadoEm: '2026-04-15T10:00:00', inscritos: [3, 5], ganhadorId: 3 },
  { id: 3, eventoId: 3, organizadorId: 7, descricao: 'Sorteio Palestra VIP',   premio: 'Acesso a palestra fechada com CEOs',          status: 'RASCUNHO',  criadoEm: '2026-06-05T10:00:00', inscritos: [] },
]

let GRUPOS: GrupoCompra[] = [
  { id: 1, eventoId: 1, tipoIngressoId: 11, liderId: 3, tamanhoMinimo: 3, tamanhoMaximo: 5, status: 'AGUARDANDO_PAGAMENTO', participantes: [3, 5, 7], criadoEm: '2026-06-01T10:00:00' },
  { id: 2, eventoId: 3, tipoIngressoId: 31, liderId: 5, tamanhoMinimo: 2, tamanhoMaximo: 4, status: 'AGUARDANDO_PAGAMENTO', participantes: [5, 3],   criadoEm: '2026-06-05T14:00:00' },
]

function gerarAssentos(mapaId: number, rows: string[], cols: number): Assento[] {
  const seats: Assento[] = []
  let id = mapaId * 1000
  for (const row of rows) {
    for (let n = 1; n <= cols; n++) {
      const occupied = Math.random() < 0.4
      seats.push({
        id: ++id,
        mapaId,
        fileira: row,
        numero: n,
        status: occupied ? 'VENDIDO' : 'DISPONIVEL',
        usuarioId: occupied ? (n % 3 === 0 ? 3 : n % 3 === 1 ? 5 : 7) : undefined,
      })
    }
  }
  return seats
}

let MAPAS: MapaAssentos[] = [
  { id: 1, eventoId: 1, nome: 'Arena Principal', assentos: gerarAssentos(1, ['A','B','C','D','E'], 10) },
  { id: 3, eventoId: 3, nome: 'Auditório CESAR', assentos: gerarAssentos(3, ['A','B','C','D'], 8) },
]

let CHECKINS: Checkin[] = []

const CUPONS: Record<number, { id: number; codigo: string; desconto: number; tipo: string; eventoId: number; ativo: boolean }[]> = {
  1: [
    { id: 1, codigo: 'VERAO10', desconto: 10, tipo: 'PERCENTUAL', eventoId: 1, ativo: true },
    { id: 2, codigo: 'VIP50',   desconto: 50, tipo: 'FIXO',       eventoId: 1, ativo: true },
  ],
  3: [
    { id: 3, codigo: 'CESAR20', desconto: 20, tipo: 'PERCENTUAL', eventoId: 3, ativo: true },
  ],
}

// ── Helpers ────────────────────────────────────────────────────────────────
function ok(data: unknown, status = 200): AxiosResponse {
  return { data, status, statusText: 'OK', headers: {}, config: {} as InternalAxiosRequestConfig, request: {} }
}

function err(status: number, msg: string): never {
  const error = Object.assign(new Error(msg), {
    response: { data: { motivo: msg }, status, statusText: 'Error', headers: {}, config: {}, request: {} },
    isAxiosError: true,
  })
  throw error
}

function currentUserId(config: InternalAxiosRequestConfig): number {
  const h = (config.headers?.['X-Usuario-Id'] ?? (api.defaults.headers.common as Record<string, unknown>)?.['X-Usuario-Id'])
  return Number(h) || 0
}

function eventoDetalheObj(ev: Evento) {
  const tipos = TIPOS.filter(t => t.eventoId === ev.id)
  const precoMinimo = tipos.length ? Math.min(...tipos.map(t => t.preco)) : 0
  return { ...ev, precoMinimo, totalVendidos: tipos.reduce((s, t) => s + t.quantidadeVendida, 0) }
}

// ── Router ─────────────────────────────────────────────────────────────────
function route(rawUrl: string, method: string, config: InternalAxiosRequestConfig): AxiosResponse {
  const url = rawUrl.replace(/^\/api/, '').split('?')[0]
  const body = typeof config.data === 'string' ? JSON.parse(config.data || '{}') : (config.data ?? {})
  const uid = currentUserId(config)

  // ── Auth ──────────────────────────────────────────────────────────────────
  if (url === '/auth/login' && method === 'post') {
    const u = MOCK_USERS.find(x => x.email === body.email)
    if (!u) err(401, 'Credenciais inválidas')
    return ok(u)
  }
  if (url === '/auth/cadastro' && method === 'post') {
    const id = mkId()
    const u = { id, nome: body.nome, email: body.email, papeis: [body.papel ?? 'COMPRADOR'] }
    MOCK_USERS.push(u)
    return ok(u)
  }

  // ── Eventos ───────────────────────────────────────────────────────────────
  if (url === '/eventos/catalogo' && method === 'get') {
    const params = config.params as Record<string, string> | undefined
    let lista = EVENTOS.filter(e => e.status === 'PUBLICADO')
    if (params?.nome) lista = lista.filter(e => e.nome.toLowerCase().includes(params.nome.toLowerCase()))
    if (params?.cidade) lista = lista.filter(e => e.cidade.toLowerCase().includes(params.cidade.toLowerCase()))
    if (params?.categoria) lista = lista.filter(e => e.categoria.toLowerCase() === params.categoria.toLowerCase())
    return ok(lista.map(eventoDetalheObj))
  }

  if (url === '/eventos' && method === 'get') {
    return ok(EVENTOS.filter(e => e.organizadorId === uid).map(eventoDetalheObj))
  }

  if (url === '/eventos' && method === 'post') {
    const ev: Evento = { id: mkId(), organizadorId: uid, status: 'RASCUNHO', ...body }
    EVENTOS.push(ev)
    return ok(ev, 201)
  }

  const mEvento = url.match(/^\/eventos\/(\d+)$/)
  if (mEvento) {
    const id = Number(mEvento[1])
    const ev = EVENTOS.find(e => e.id === id)
    if (!ev) err(404, 'Evento não encontrado')
    if (method === 'get') return ok(eventoDetalheObj(ev!))
    if (method === 'put') {
      if (ev!.organizadorId !== uid) err(403, 'Sem permissão')
      Object.assign(ev!, body)
      return ok(ev)
    }
    if (method === 'delete') {
      if (ev!.organizadorId !== uid) err(403, 'Sem permissão')
      EVENTOS = EVENTOS.filter(e => e.id !== id)
      return ok({})
    }
  }

  if (url.match(/^\/eventos\/\d+\/cancelar$/) && method === 'post') {
    const id = Number(url.split('/')[2])
    const ev = EVENTOS.find(e => e.id === id)
    if (!ev) err(404, 'Evento não encontrado')
    if (ev!.organizadorId !== uid) err(403, 'Sem permissão')
    ev!.status = 'CANCELADO'
    return ok(ev)
  }

  // ── Analytics ──────────────────────────────────────────────────────────────
  if (url.match(/^\/eventos\/\d+\/analytics$/) && method === 'get') {
    const id = Number(url.split('/')[2])
    const ev = EVENTOS.find(e => e.id === id)
    if (!ev) err(404, 'Evento não encontrado')
    const user = MOCK_USERS.find(u => u.id === uid)
    if (!user?.papeis.includes('ADMIN') && ev!.organizadorId !== uid) err(403, 'Acesso negado')
    const tipos = TIPOS.filter(t => t.eventoId === id)
    const totalVendidos = tipos.reduce((s, t) => s + t.quantidadeVendida, 0)
    const receita = tipos.reduce((s, t) => s + t.quantidadeVendida * t.preco, 0)
    return ok({ totalVendidos, receita, totalCapacidade: ev!.capacidade, checkins: Math.floor(totalVendidos * 0.6), ticketsPorTipo: tipos.map(t => ({ nome: t.nome, vendidos: t.quantidadeVendida, receita: t.quantidadeVendida * t.preco })) })
  }

  // ── Tipos Ingresso ─────────────────────────────────────────────────────────
  if (url.match(/^\/eventos\/\d+\/tipos-ingresso$/) && method === 'get') {
    const id = Number(url.split('/')[2])
    return ok(TIPOS.filter(t => t.eventoId === id))
  }
  if (url.match(/^\/eventos\/\d+\/tipos-ingresso$/) && method === 'post') {
    const evId = Number(url.split('/')[2])
    const t: TipoIngresso = { id: mkId(), eventoId: evId, quantidadeVendida: 0, ...body }
    TIPOS.push(t)
    return ok(t, 201)
  }
  if (url.match(/^\/eventos\/\d+\/tipos-ingresso\/\d+$/) && method === 'put') {
    const tipoId = Number(url.split('/')[4])
    const t = TIPOS.find(x => x.id === tipoId)
    if (!t) err(404, 'Tipo não encontrado')
    Object.assign(t!, body)
    return ok(t)
  }
  if (url.match(/^\/eventos\/\d+\/tipos-ingresso\/\d+$/) && method === 'delete') {
    const tipoId = Number(url.split('/')[4])
    TIPOS = TIPOS.filter(x => x.id !== tipoId)
    return ok({})
  }

  // ── Ingressos ──────────────────────────────────────────────────────────────
  if (url === '/meus-ingressos' && method === 'get') {
    return ok(INGRESSOS.filter(i => i.usuarioId === uid))
  }
  if (url.match(/^\/ingressos\/[\w-]+$/) && method === 'get') {
    const id = url.split('/')[2]
    const ing = INGRESSOS.find(i => i.id === id)
    if (!ing) err(404, 'Ingresso não encontrado')
    return ok(ing)
  }
  if (url.match(/^\/ingressos\/[\w-]+\/qrcode$/) && method === 'get') {
    const id = url.split('/')[2]
    return ok({ qrCode: `QR-${id}-${Date.now()}` })
  }
  if ((url === '/ingressos/comprar' || url === '/pedidos') && method === 'post') {
    const tipoId = body.tipoIngressoId ?? body.itens?.[0]?.tipoIngressoId
    const tipo = TIPOS.find(t => t.id === tipoId)
    if (!tipo) err(400, 'Tipo de ingresso não encontrado')
    if (tipo!.quantidadeTotal - tipo!.quantidadeVendida <= 0) err(400, 'Ingressos esgotados')
    const ev = EVENTOS.find(e => e.id === tipo!.eventoId)
    const ing: Ingresso = { id: `ING-${mkId()}`, usuarioId: uid, eventoId: tipo!.eventoId, tipoIngressoId: tipoId, status: 'ATIVO', preco: tipo!.preco, codigoQr: `QR-${mkId()}`, criadoEm: new Date().toISOString(), nomeEvento: ev?.nome, nomeTipo: tipo!.nome }
    INGRESSOS.push(ing)
    tipo!.quantidadeVendida++
    if (SALDO[uid] !== undefined) SALDO[uid] -= tipo!.preco
    return ok(ing, 201)
  }
  if (url.match(/^\/ingressos\/[\w-]+\/reembolsar$/) && method === 'post') {
    const id = url.split('/')[2]
    const ing = INGRESSOS.find(i => i.id === id)
    if (!ing) err(404, 'Ingresso não encontrado')
    if (ing!.usuarioId !== uid) err(403, 'Sem permissão')
    ing!.status = 'REEMBOLSADO'
    if (SALDO[uid] !== undefined) SALDO[uid] += ing!.preco
    return ok(ing)
  }
  if (url.match(/^\/ingressos\/[\w-]+\/transferir$/) && method === 'post') {
    const id = url.split('/')[2]
    const ing = INGRESSOS.find(i => i.id === id)
    if (!ing) err(404, 'Ingresso não encontrado')
    if (ing!.usuarioId !== uid) err(403, 'Sem permissão')
    ing!.usuarioId = body.destinatarioId ?? uid
    ing!.status = 'TRANSFERIDO'
    return ok(ing)
  }

  // ── Saldo / Carteira ────────────────────────────────────────────────────────
  if (url === '/saldo' && method === 'get') {
    return ok({ valor: SALDO[uid] ?? 0 })
  }
  if (url === '/saldo/adicionar' && method === 'post') {
    SALDO[uid] = (SALDO[uid] ?? 0) + Number(body.valor)
    const tr = { id: mkId(), tipo: 'RECARGA', valor: body.valor, descricao: 'Recarga manual', criadoEm: new Date().toISOString() }
    if (!TRANSACOES[uid]) TRANSACOES[uid] = []
    TRANSACOES[uid].unshift(tr)
    return ok({ valor: SALDO[uid] })
  }
  if (url.startsWith('/transacoes') && method === 'get') {
    return ok(TRANSACOES[uid] ?? [])
  }
  if (url === '/carteira' && method === 'get') {
    return ok({ saldo: SALDO[uid] ?? 0, transacoes: TRANSACOES[uid] ?? [] })
  }
  if (url === '/carteira/recarregar' && method === 'post') {
    SALDO[uid] = (SALDO[uid] ?? 0) + Number(body.valor)
    return ok({ saldo: SALDO[uid] })
  }
  if (url === '/carteira/sacar' && method === 'post') {
    if ((SALDO[uid] ?? 0) < body.valor) err(400, 'Saldo insuficiente')
    SALDO[uid] -= body.valor
    return ok({ saldo: SALDO[uid] })
  }

  // ── Cupons ─────────────────────────────────────────────────────────────────
  if (url === '/cupons/validar' && method === 'post') {
    const evCupons = CUPONS[body.eventoId] ?? []
    const c = evCupons.find(x => x.codigo === body.codigo && x.ativo)
    if (!c) err(400, 'Cupom inválido ou expirado')
    const desconto = c!.tipo === 'PERCENTUAL' ? body.valor * c!.desconto / 100 : c!.desconto
    return ok({ valido: true, desconto, valorFinal: body.valor - desconto })
  }
  if (url.match(/^\/eventos\/\d+\/cupons$/) && method === 'get') {
    const evId = Number(url.split('/')[2])
    return ok(CUPONS[evId] ?? [])
  }
  if (url.match(/^\/eventos\/\d+\/cupons$/) && method === 'post') {
    const evId = Number(url.split('/')[2])
    const c = { id: mkId(), eventoId: evId, ativo: true, ...body }
    if (!CUPONS[evId]) CUPONS[evId] = []
    CUPONS[evId].push(c)
    return ok(c, 201)
  }

  // ── Revendas ────────────────────────────────────────────────────────────────
  if (url === '/revendas/anuncios/meus' && method === 'get') {
    return ok(ANUNCIOS.filter(a => a.vendedorId === uid))
  }
  if (url === '/revendas/anuncios' && method === 'get') {
    const evId = config.params?.eventoId ? Number(config.params.eventoId) : null
    return ok(evId ? ANUNCIOS.filter(a => a.eventoId === evId) : ANUNCIOS)
  }
  if (url.match(/^\/revendas\/anuncios\/\d+$/) && method === 'get') {
    const id = Number(url.split('/')[3])
    const a = ANUNCIOS.find(x => x.id === id)
    if (!a) err(404, 'Anúncio não encontrado')
    return ok(a)
  }
  if (url === '/revendas/anuncios' && method === 'post') {
    const ing = INGRESSOS.find(i => i.id === body.ingressoId)
    if (!ing || ing.usuarioId !== uid) err(403, 'Ingresso não pertence a você')
    const a: Anuncio = { id: mkId(), vendedorId: uid, eventoId: ing.eventoId, preco: body.preco, status: 'DISPONIVEL', criadoEm: new Date().toISOString(), ingressoId: body.ingressoId }
    ANUNCIOS.push(a)
    return ok(a, 201)
  }
  if (url.match(/^\/revendas\/anuncios\/\d+\/reservar$/) && method === 'post') {
    const id = Number(url.split('/')[3])
    const a = ANUNCIOS.find(x => x.id === id)
    if (!a) err(404, 'Anúncio não encontrado')
    a!.status = 'RESERVADO'
    return ok(a)
  }
  if (url.match(/^\/revendas\/anuncios\/\d+\/confirmar$/) && method === 'post') {
    const id = Number(url.split('/')[3])
    const a = ANUNCIOS.find(x => x.id === id)
    if (!a) err(404, 'Anúncio não encontrado')
    a!.status = 'VENDIDO'
    return ok(a)
  }
  if (url.match(/^\/revendas\/anuncios\/\d+$/) && method === 'delete') {
    const id = Number(url.split('/')[3])
    ANUNCIOS = ANUNCIOS.filter(a => a.id !== id)
    return ok({})
  }
  if (url.match(/^\/revendas\/anuncios\/\d+$/) && method === 'put') {
    const id = Number(url.split('/')[3])
    const a = ANUNCIOS.find(x => x.id === id)
    if (!a) err(404, 'Anúncio não encontrado')
    a!.preco = Number(body) || body.preco || a!.preco
    return ok(a)
  }
  if (url.match(/^\/revendas\/anuncios\/\d+\/denunciar$/) && method === 'post') {
    const anuncioId = Number(url.split('/')[3])
    const d: Denuncia = { id: mkId(), anuncioId, denuncianteId: uid, motivo: body.motivo, descricao: body.descricao ?? '', status: 'PENDENTE', decisao: null, criadaEm: new Date().toISOString(), decididaEm: null }
    DENUNCIAS.push(d)
    return ok(d, 201)
  }

  // ── Denúncias ───────────────────────────────────────────────────────────────
  if (url === '/denuncias' && method === 'get') {
    return ok(DENUNCIAS)
  }
  if (url.match(/^\/denuncias\/\d+\/decidir$/) && method === 'post') {
    const id = Number(url.split('/')[2])
    const d = DENUNCIAS.find(x => x.id === id)
    if (!d) err(404, 'Denúncia não encontrada')
    d!.status = 'RESOLVIDA'
    d!.decisao = body.decisao
    d!.decididaEm = new Date().toISOString()
    return ok(d)
  }

  // ── Avaliações ──────────────────────────────────────────────────────────────
  if (url.match(/^\/eventos\/\d+\/avaliacoes$/) && method === 'get') {
    const evId = Number(url.split('/')[2])
    return ok(AVALIACOES[evId] ?? [])
  }
  if (url.match(/^\/eventos\/\d+\/avaliacoes$/) && method === 'post') {
    const evId = Number(url.split('/')[2])
    const av: Avaliacao = { id: mkId(), eventoId: evId, usuarioId: uid, nota: body.nota, comentario: body.comentario, criadaEm: new Date().toISOString() }
    if (!AVALIACOES[evId]) AVALIACOES[evId] = []
    AVALIACOES[evId].push(av)
    return ok(av, 201)
  }
  if (url.match(/^\/eventos\/\d+\/avaliacoes\/\d+\/responder$/) && method === 'put') {
    const evId = Number(url.split('/')[2])
    const avId = Number(url.split('/')[4])
    const av = (AVALIACOES[evId] ?? []).find(a => a.id === avId)
    if (!av) err(404, 'Avaliação não encontrada')
    av!.respostaOrganizador = typeof body === 'string' ? body : body.resposta ?? ''
    return ok(av)
  }

  // ── Check-in ────────────────────────────────────────────────────────────────
  if ((url === '/check-in/escanear' || url === '/check-in/manual') && method === 'post') {
    const qr = body.qrCode ?? body.codigoIngresso
    const ing = INGRESSOS.find(i => i.codigoQr === qr || i.id === qr)
    if (!ing) err(404, 'Ingresso não encontrado')
    if (ing!.status !== 'ATIVO') err(400, 'Ingresso inválido ou já utilizado')
    const already = CHECKINS.find(c => c.ingressoId === ing!.id)
    if (already) err(409, 'Check-in já realizado')
    const ck: Checkin = { ingressoId: ing!.id, operadorId: uid, eventoId: ing!.eventoId, realizadoEm: new Date().toISOString() }
    CHECKINS.push(ck)
    return ok({ sucesso: true, ingresso: ing, checkin: ck })
  }
  if (url.match(/^\/check-in\/relatorio\/\d+$/) && method === 'get') {
    const evId = Number(url.split('/')[3])
    const lista = CHECKINS.filter(c => c.eventoId === evId)
    const total = INGRESSOS.filter(i => i.eventoId === evId && i.status === 'ATIVO').length
    return ok({ realizados: lista.length, total, checkins: lista })
  }

  // ── Feed / Posts ─────────────────────────────────────────────────────────────
  if (url.match(/^\/eventos\/\d+\/posts$/) && method === 'get') {
    const evId = Number(url.split('/')[2])
    return ok((POSTS[evId] ?? []).sort((a, b) => (b.fixado ? 1 : 0) - (a.fixado ? 1 : 0)))
  }
  if (url.match(/^\/eventos\/\d+\/posts$/) && method === 'post') {
    const evId = Number(url.split('/')[2])
    const p: Post = { id: mkId(), eventoId: evId, autorId: uid, fixado: false, criadoEm: new Date().toISOString(), ...body }
    if (!POSTS[evId]) POSTS[evId] = []
    POSTS[evId].push(p)
    return ok(p, 201)
  }
  if (url.match(/^\/eventos\/\d+\/posts\/\d+$/) && method === 'put') {
    const evId = Number(url.split('/')[2])
    const pId = Number(url.split('/')[4])
    const p = (POSTS[evId] ?? []).find(x => x.id === pId)
    if (!p) err(404, 'Post não encontrado')
    Object.assign(p!, body)
    return ok(p)
  }
  if (url.match(/^\/eventos\/\d+\/posts\/\d+$/) && method === 'delete') {
    const evId = Number(url.split('/')[2])
    const pId = Number(url.split('/')[4])
    if (POSTS[evId]) POSTS[evId] = POSTS[evId].filter(x => x.id !== pId)
    return ok({})
  }
  if (url.match(/^\/eventos\/\d+\/posts\/\d+\/fixar$/) && method === 'post') {
    const evId = Number(url.split('/')[2])
    const pId = Number(url.split('/')[4])
    const p = (POSTS[evId] ?? []).find(x => x.id === pId)
    if (!p) err(404, 'Post não encontrado')
    p!.fixado = !p!.fixado
    return ok(p)
  }

  // ── Sorteios ─────────────────────────────────────────────────────────────────
  if (url === '/sorteios' && method === 'get') {
    const evId = config.params?.eventoId ? Number(config.params.eventoId) : null
    return ok(evId ? SORTEIOS.filter(s => s.eventoId === evId) : SORTEIOS)
  }
  if (url.match(/^\/sorteios\/\d+$/) && method === 'get') {
    const id = Number(url.split('/')[2])
    const s = SORTEIOS.find(x => x.id === id)
    if (!s) err(404, 'Sorteio não encontrado')
    return ok(s)
  }
  if (url === '/sorteios' && method === 'post') {
    const s: Sorteio = { id: mkId(), organizadorId: uid, status: 'RASCUNHO', inscritos: [], criadoEm: new Date().toISOString(), ...body }
    SORTEIOS.push(s)
    return ok(s, 201)
  }
  if (url.match(/^\/sorteios\/\d+\/abrir$/) && method === 'post') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    s!.status = 'ABERTO'
    return ok(s)
  }
  if (url.match(/^\/sorteios\/\d+\/encerrar-inscricoes$/) && method === 'post') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    s!.status = 'ENCERRADO'
    return ok(s)
  }
  if (url.match(/^\/sorteios\/\d+\/inscrever$/) && method === 'post') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    if (s!.status !== 'ABERTO') err(400, 'Sorteio não está aberto para inscrições')
    if (!s!.inscritos.includes(uid)) s!.inscritos.push(uid)
    return ok(s)
  }
  if (url.match(/^\/sorteios\/\d+\/sortear$/) && method === 'post') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    if (!s!.inscritos.length) err(400, 'Nenhum inscrito para sortear')
    s!.ganhadorId = s!.inscritos[Math.floor(Math.random() * s!.inscritos.length)]
    s!.status = 'REALIZADO'
    return ok(s)
  }
  if (url.match(/^\/sorteios\/\d+\/confirmar$/) && method === 'post') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    s!.status = 'CONFIRMADO'
    return ok(s)
  }
  if (url.match(/^\/sorteios\/\d+\/cancelar$/) && method === 'post') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    s!.status = 'CANCELADO'
    return ok(s)
  }
  if (url.match(/^\/sorteios\/\d+\/inscricoes$/) && method === 'get') {
    const s = SORTEIOS.find(x => x.id === Number(url.split('/')[2]))
    if (!s) err(404, 'Sorteio não encontrado')
    return ok(s!.inscritos.map(id => ({ usuarioId: id, nome: MOCK_USERS.find(u => u.id === id)?.nome ?? `Usuário #${id}` })))
  }

  // ── Mapas de Assentos ────────────────────────────────────────────────────────
  if (url === '/mapas-assentos/meus' && method === 'get') {
    const ing = INGRESSOS.filter(i => i.usuarioId === uid)
    const evIds = [...new Set(ing.map(i => i.eventoId))]
    return ok(MAPAS.filter(m => evIds.includes(m.eventoId)))
  }
  if (url === '/mapas-assentos' && method === 'get') {
    const evId = config.params?.eventoId ? Number(config.params.eventoId) : null
    const m = evId ? MAPAS.find(x => x.eventoId === evId) : null
    if (!m) err(404, 'Mapa não encontrado')
    return ok(m)
  }
  if (url === '/mapas-assentos' && method === 'post') {
    const m: MapaAssentos = { id: mkId(), assentos: [], ...body }
    MAPAS.push(m)
    return ok(m, 201)
  }
  if (url.match(/^\/mapas-assentos\/\d+\/assentos$/) && method === 'get') {
    const id = Number(url.split('/')[2])
    const m = MAPAS.find(x => x.id === id)
    if (!m) err(404, 'Mapa não encontrado')
    return ok(m!.assentos)
  }
  if (url.match(/^\/mapas-assentos\/\d+\/(reservar|confirmar|comprar)$/) && method === 'post') {
    const id = Number(url.split('/')[2])
    const action = url.split('/')[3]
    const m = MAPAS.find(x => x.id === id)
    if (!m) err(404, 'Mapa não encontrado')
    const statusMap: Record<string, string> = { reservar: 'RESERVADO', confirmar: 'CONFIRMADO', comprar: 'VENDIDO' }
    const newStatus = statusMap[action]
    ;(body.assentoIds as number[]).forEach(sid => {
      const s = m!.assentos.find(a => a.id === sid)
      if (s) { s.status = newStatus; if (newStatus === 'VENDIDO') s.usuarioId = uid }
    })
    return ok({ sucesso: true })
  }
  if (url.match(/^\/mapas-assentos\/\d+\/(bloquear|desbloquear)$/) && method === 'post') {
    const id = Number(url.split('/')[2])
    const bloquear = url.endsWith('/bloquear')
    const m = MAPAS.find(x => x.id === id)
    if (!m) err(404, 'Mapa não encontrado')
    ;(body.assentoIds as number[]).forEach(sid => {
      const s = m!.assentos.find(a => a.id === sid)
      if (s) s.status = bloquear ? 'BLOQUEADO' : 'DISPONIVEL'
    })
    return ok({ sucesso: true })
  }
  if (url.match(/^\/mapas-assentos\/\d+\/liberar-expirados$/) && method === 'post') {
    return ok({ liberados: 0 })
  }
  if (url.match(/^\/mapas-assentos\/\d+\/fila\/entrar$/) && method === 'post') {
    return ok({ posicao: Math.floor(Math.random() * 5) + 1 })
  }
  if (url.match(/^\/mapas-assentos\/\d+\/fila\/sair$/) && method === 'delete') {
    return ok({})
  }
  if (url.match(/^\/mapas-assentos\/\d+\/fila\/posicao$/) && method === 'get') {
    return ok({ posicao: Math.floor(Math.random() * 5) + 1 })
  }

  // ── Grupos de Compra ─────────────────────────────────────────────────────────
  if (url === '/grupos-compra' && method === 'get') {
    const evId = config.params?.eventoId ? Number(config.params.eventoId) : null
    return ok(evId ? GRUPOS.filter(g => g.eventoId === evId) : GRUPOS)
  }
  if (url === '/grupos-compra/meus' && method === 'get') {
    return ok(GRUPOS.filter(g => g.participantes.includes(uid)))
  }
  if (url.match(/^\/grupos-compra\/\d+$/) && method === 'get') {
    const id = Number(url.split('/')[2])
    const g = GRUPOS.find(x => x.id === id)
    if (!g) err(404, 'Grupo não encontrado')
    return ok(g)
  }
  if (url.match(/^\/grupos-compra\/\d+\/participantes$/) && method === 'get') {
    const id = Number(url.split('/')[2])
    const g = GRUPOS.find(x => x.id === id)
    if (!g) err(404, 'Grupo não encontrado')
    return ok(g!.participantes.map(uid2 => ({ usuarioId: uid2, nome: MOCK_USERS.find(u => u.id === uid2)?.nome ?? `Usuário #${uid2}` })))
  }
  if (url === '/grupos-compra' && method === 'post') {
    const g: GrupoCompra = { id: mkId(), liderId: uid, status: 'AGUARDANDO_PAGAMENTO', participantes: [uid], criadoEm: new Date().toISOString(), ...body }
    GRUPOS.push(g)
    return ok(g, 201)
  }
  if (url.match(/^\/grupos-compra\/\d+\/pagar$/) && method === 'post') {
    const id = Number(url.split('/')[2])
    const g = GRUPOS.find(x => x.id === id)
    if (!g) err(404, 'Grupo não encontrado')
    g!.status = 'CONCLUIDO'
    return ok(g)
  }
  if (url.match(/^\/grupos-compra\/\d+\/cancelar$/) && method === 'post') {
    const id = Number(url.split('/')[2])
    const g = GRUPOS.find(x => x.id === id)
    if (!g) err(404, 'Grupo não encontrado')
    g!.status = 'CANCELADO'
    return ok(g)
  }

  // Fallback
  console.warn(`[MockInterceptor] Unhandled: ${method.toUpperCase()} ${url}`)
  return ok({})
}

// ── Adapter ────────────────────────────────────────────────────────────────
function mockAdapter(config: InternalAxiosRequestConfig): Promise<AxiosResponse> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      try {
        resolve(route(config.url ?? '', (config.method ?? 'get').toLowerCase(), config))
      } catch (err) {
        reject(err)
      }
    }, 60)
  })
}

// ── Public API ─────────────────────────────────────────────────────────────
export function isMockMode(): boolean {
  return mockMode
}

export function enableMockMode(): void {
  mockMode = true
  localStorage.setItem(MOCK_KEY, '1')
  if (interceptorId === null) {
    interceptorId = api.interceptors.request.use(cfg => {
      if (mockMode) cfg.adapter = mockAdapter
      return cfg
    })
  }
}

export function disableMockMode(): void {
  mockMode = false
  localStorage.removeItem(MOCK_KEY)
}

export function initMockMode(): void {
  if (localStorage.getItem(MOCK_KEY) === '1') enableMockMode()
}
