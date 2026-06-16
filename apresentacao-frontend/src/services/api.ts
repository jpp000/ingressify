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

// Autenticação
export const authService = {
  login: (email: string, senha: string) =>
    api.post('/auth/login', { email, senha }),
  cadastro: (nome: string, email: string, senha: string, papel: string) =>
    api.post('/auth/cadastro', { nome, email, senha, papel }),
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

// Analytics
export const analyticsService = {
  obter: (eventoId: number, usuarioId: number) =>
    api.get(`/eventos/${eventoId}/analytics`, { headers: { 'X-Usuario-Id': usuarioId } }),
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
  comprarPedido: (usuarioId: number, dados: object) =>
    api.post('/pedidos', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
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

// Carteira (recarga / saque)
export const carteiraService = {
  obter: (usuarioId: number) =>
    api.get('/carteira', { headers: { 'X-Usuario-Id': usuarioId } }),
  recarregar: (usuarioId: number, valor: number) =>
    api.post('/carteira/recarregar', { valor }, { headers: { 'X-Usuario-Id': usuarioId } }),
  sacar: (usuarioId: number, valor: number) =>
    api.post('/carteira/sacar', { valor }, { headers: { 'X-Usuario-Id': usuarioId } }),
}

// Cupons & Promoções
export const cupomService = {
  validar: (codigo: string, eventoId: number, valor: number) =>
    api.post('/cupons/validar', { codigo, eventoId, valor }),
  listar: (eventoId: number) => api.get(`/eventos/${eventoId}/cupons`),
  criar: (eventoId: number, usuarioId: number, dados: object) =>
    api.post(`/eventos/${eventoId}/cupons`, dados, { headers: { 'X-Usuario-Id': usuarioId } }),
}

// Revendas
export const revendaService = {
  meus: (usuarioId: number) =>
    api.get('/revendas/anuncios/meus', { headers: { 'X-Usuario-Id': usuarioId } }),
  todos: () =>
    api.get('/revendas/anuncios'),
  listar: (eventoId: number) =>
    api.get(`/revendas/anuncios?eventoId=${eventoId}`),
  detalhe: (id: number) => api.get(`/revendas/anuncios/${id}`),
  criar: (usuarioId: number, dados: object) =>
    api.post('/revendas/anuncios', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  reservar: (id: number, usuarioId: number) =>
    api.post(`/revendas/anuncios/${id}/reservar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  confirmar: (id: number, usuarioId: number) =>
    api.post(`/revendas/anuncios/${id}/confirmar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  cancelar: (id: number, usuarioId: number) =>
    api.delete(`/revendas/anuncios/${id}`, { headers: { 'X-Usuario-Id': usuarioId } }),
  alterarPreco: (id: number, usuarioId: number, preco: number) =>
    api.put(`/revendas/anuncios/${id}`, preco, { headers: { 'X-Usuario-Id': usuarioId } }),
  denunciar: (id: number, usuarioId: number, dados: object) =>
    api.post(`/revendas/anuncios/${id}/denunciar`, dados, { headers: { 'X-Usuario-Id': usuarioId } })
}

// Denúncias (moderação)
export const denunciaService = {
  listar: (usuarioId: number) =>
    api.get('/denuncias', { headers: { 'X-Usuario-Id': usuarioId } }),
  decidir: (id: number, usuarioId: number, decisao: string) =>
    api.post(`/denuncias/${id}/decidir`, { decisao }, { headers: { 'X-Usuario-Id': usuarioId } }),
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

// Sorteios
export const sorteioService = {
  listarPorEvento: (eventoId: number) =>
    api.get('/sorteios', { params: { eventoId } }),
  obter: (id: number) =>
    api.get(`/sorteios/${id}`),
  criar: (usuarioId: number, dados: object) =>
    api.post('/sorteios', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  abrir: (id: number, usuarioId: number) =>
    api.post(`/sorteios/${id}/abrir`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  encerrarInscricoes: (id: number, usuarioId: number) =>
    api.post(`/sorteios/${id}/encerrar-inscricoes`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  inscrever: (id: number, usuarioId: number) =>
    api.post(`/sorteios/${id}/inscrever`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  sortear: (id: number) =>
    api.post(`/sorteios/${id}/sortear`),
  confirmar: (id: number, usuarioId: number) =>
    api.post(`/sorteios/${id}/confirmar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  cancelar: (id: number, usuarioId: number) =>
    api.post(`/sorteios/${id}/cancelar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  listarInscricoes: (id: number) =>
    api.get(`/sorteios/${id}/inscricoes`),
}

// Mapa de Assentos
export const mapaAssentosService = {
  meusAssentos: (usuarioId: number) =>
    api.get('/mapas-assentos/meus', { headers: { 'X-Usuario-Id': usuarioId } }),
  obterPorEvento: (eventoId: number) =>
    api.get('/mapas-assentos', { params: { eventoId } }),
  listarAssentos: (mapaId: number) =>
    api.get(`/mapas-assentos/${mapaId}/assentos`),
  criar: (usuarioId: number, dados: object) =>
    api.post('/mapas-assentos', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  reservar: (mapaId: number, usuarioId: number, assentoIds: number[]) =>
    api.post(`/mapas-assentos/${mapaId}/reservar`, { assentoIds },
      { headers: { 'X-Usuario-Id': usuarioId } }),
  confirmar: (mapaId: number, usuarioId: number, assentoIds: number[]) =>
    api.post(`/mapas-assentos/${mapaId}/confirmar`, { assentoIds },
      { headers: { 'X-Usuario-Id': usuarioId } }),
  comprar: (mapaId: number, usuarioId: number, assentoIds: number[]) =>
    api.post(`/mapas-assentos/${mapaId}/comprar`, { assentoIds },
      { headers: { 'X-Usuario-Id': usuarioId } }),
  bloquear: (mapaId: number, usuarioId: number, assentoIds: number[]) =>
    api.post(`/mapas-assentos/${mapaId}/bloquear`, { assentoIds },
      { headers: { 'X-Usuario-Id': usuarioId } }),
  desbloquear: (mapaId: number, usuarioId: number, assentoIds: number[]) =>
    api.post(`/mapas-assentos/${mapaId}/desbloquear`, { assentoIds },
      { headers: { 'X-Usuario-Id': usuarioId } }),
  liberarExpirados: (mapaId: number) =>
    api.post(`/mapas-assentos/${mapaId}/liberar-expirados`),
}

// Fila de Espera
export const filaEsperaService = {
  entrar: (mapaId: number, usuarioId: number) =>
    api.post(`/mapas-assentos/${mapaId}/fila/entrar`, {},
      { headers: { 'X-Usuario-Id': usuarioId } }),
  sair: (mapaId: number, usuarioId: number) =>
    api.delete(`/mapas-assentos/${mapaId}/fila/sair`,
      { headers: { 'X-Usuario-Id': usuarioId } }),
  consultarPosicao: (mapaId: number, usuarioId: number) =>
    api.get(`/mapas-assentos/${mapaId}/fila/posicao`,
      { headers: { 'X-Usuario-Id': usuarioId } }),
}

// Compra em Grupo
export const grupoCompraService = {
  listarPorEvento: (eventoId: number) =>
    api.get('/grupos-compra', { params: { eventoId } }),
  meus: (usuarioId: number) =>
    api.get('/grupos-compra/meus', { headers: { 'X-Usuario-Id': usuarioId } }),
  obter: (id: number) =>
    api.get(`/grupos-compra/${id}`),
  participantes: (id: number) =>
    api.get(`/grupos-compra/${id}/participantes`),
  criar: (usuarioId: number, dados: object) =>
    api.post('/grupos-compra', dados, { headers: { 'X-Usuario-Id': usuarioId } }),
  pagar: (id: number, usuarioId: number) =>
    api.post(`/grupos-compra/${id}/pagar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
  cancelar: (id: number, usuarioId: number) =>
    api.post(`/grupos-compra/${id}/cancelar`, {}, { headers: { 'X-Usuario-Id': usuarioId } }),
}
