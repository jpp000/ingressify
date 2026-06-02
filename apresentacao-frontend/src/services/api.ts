import axios from 'axios'

const BASE_URL = '/api'

export const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const setUsuarioId = (id: number) => {
  api.defaults.headers.common['X-Usuario-Id'] = id
}

// Eventos
export const eventoService = {
  listar: (usuarioId: number) =>
    api.get('/eventos', { headers: { 'X-Usuario-Id': usuarioId } }),
  catalogo: (params?: { nome?: string; cidade?: string; categoria?: string; precoMin?: number; precoMax?: number }) =>
    api.get('/eventos/catalogo', { params }),
  detalhe: (id: number) => api.get(`/eventos/${id}`),
  criar: (usuarioId: number, dados: object) =>
    api.post('/eventos', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  editar: (id: number, usuarioId: number, dados: object) =>
    api.put(`/eventos/${id}`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  cancelar: (id: number, usuarioId: number) =>
    api.post(`/eventos/${id}/cancelar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  remover: (id: number, usuarioId: number) =>
    api.delete(`/eventos/${id}`, { headers: { 'X-Usuario-Id': usuarioId } })
}

// Tipos de Ingresso
export const tipoIngressoService = {
  listar: (eventoId: number) => api.get(`/eventos/${eventoId}/tipos-ingresso`),
  criar: (eventoId: number, usuarioId: number, dados: object) =>
    api.post(`/eventos/${eventoId}/tipos-ingresso`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  editar: (eventoId: number, tipoId: number, usuarioId: number, dados: object) =>
    api.put(`/eventos/${eventoId}/tipos-ingresso/${tipoId}`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  remover: (eventoId: number, tipoId: number, usuarioId: number) =>
    api.delete(`/eventos/${eventoId}/tipos-ingresso/${tipoId}`, { headers: { 'X-Usuario-Id': usuarioId } })
}

// Ingressos
export const ingressoService = {
  meus: (usuarioId: number) =>
    api.get('/meus-ingressos', { headers: { 'X-Usuario-Id': usuarioId } }),
  detalhe: (id: string) => api.get(`/ingressos/${id}`),
  comprar: (usuarioId: number, dados: object) =>
    api.post('/ingressos/comprar', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  transferir: (id: string, usuarioId: number, dados: object) =>
    api.post(`/ingressos/${id}/transferir`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  reembolsar: (id: string, usuarioId: number) =>
    api.post(`/ingressos/${id}/reembolsar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  qrcode: (id: string) => api.get(`/ingressos/${id}/qrcode`)
}

// Saldo e Transações
export const saldoService = {
  obter: (usuarioId: number) =>
    api.get('/saldo', { headers: { 'X-Usuario-Id': usuarioId } }),
  adicionar: (usuarioId: number, valor: number) =>
    api.post('/saldo/adicionar', { valor }, { headers: { 'X-Usuario-Id': usuarioId } }),
  transacoes: (usuarioId: number, page = 1, limit = 10) =>
    api.get(`/transacoes?page=${page}&limit=${limit}`, { headers: { 'X-Usuario-Id': usuarioId } })
}

// Revendas
export const revendaService = {
  listar: (eventoId: number) =>
    api.get(`/revendas/anuncios?eventoId=${eventoId}`),
  detalhe: (id: number) => api.get(`/revendas/anuncios/${id}`),
  criar: (usuarioId: number, dados: object) =>
    api.post('/revendas/anuncios', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  reservar: (id: number, usuarioId: number) =>
    api.post(`/revendas/anuncios/${id}/reservar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  cancelar: (id: number, usuarioId: number) =>
    api.delete(`/revendas/anuncios/${id}`, { headers: { 'X-Usuario-Id': usuarioId } }),
  alterarPreco: (id: number, usuarioId: number, preco: number) =>
    api.put(`/revendas/anuncios/${id}`, preco, { headers: { 'X-Usuario-Id': usuarioId } }),
  denunciar: (id: number, usuarioId: number, dados: object) =>
    api.post(`/revendas/anuncios/${id}/denunciar`, dados, { headers: { 'X-Usuario-Id': usuarioId } })
}

// Avaliações
export const avaliacaoService = {
  listar: (eventoId: number) => api.get(`/eventos/${eventoId}/avaliacoes`),
  avaliar: (eventoId: number, usuarioId: number, dados: object) =>
    api.post(`/eventos/${eventoId}/avaliacoes`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  responder: (eventoId: number, avaliacaoId: number, usuarioId: number, texto: string) =>
    api.put(`/eventos/${eventoId}/avaliacoes/${avaliacaoId}/responder`, texto,
      { headers: { 'X-Usuario-Id': usuarioId, 'Content-Type': 'text/plain' } })
}

// Check-in
export const checkinService = {
  escanear: (usuarioId: number, qrCode: string) =>
    api.post('/check-in/escanear', { qrCode }, { headers: { 'X-Usuario-Id': usuarioId } }),
  manual: (usuarioId: number, codigoIngresso: string) =>
    api.post('/check-in/manual', { codigoIngresso }, { headers: { 'X-Usuario-Id': usuarioId } }),
  relatorio: (eventoId: number, usuarioId: number) =>
    api.get(`/check-in/relatorio/${eventoId}`, { headers: { 'X-Usuario-Id': usuarioId } })
}

// Feed
export const feedService = {
  listar: (eventoId: number) => api.get(`/eventos/${eventoId}/posts`),
  criar: (eventoId: number, usuarioId: number, dados: object) =>
    api.post(`/eventos/${eventoId}/posts`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  editar: (eventoId: number, postId: number, usuarioId: number, dados: object) =>
    api.put(`/eventos/${eventoId}/posts/${postId}`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  remover: (eventoId: number, postId: number, usuarioId: number) =>
    api.delete(`/eventos/${eventoId}/posts/${postId}`, { headers: { 'X-Usuario-Id': usuarioId } }),
  fixar: (eventoId: number, postId: number, usuarioId: number) =>
    api.post(`/eventos/${eventoId}/posts/${postId}/fixar`, {}, { headers: { 'X-Usuario-Id': usuarioId } })
}
