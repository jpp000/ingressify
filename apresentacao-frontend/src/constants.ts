export const TAXA_SERVICO_PERCENTUAL = 0.125

export const CATEGORIAS = [
  'Música',
  'Teatro e Artes',
  'Esportes',
  'Conferências',
  'Vida Noturna',
  'Comida e Bebida',
]

export const MESES_PT = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']

export function formatMoeda(valor: number): string {
  return `R$ ${Number(valor).toFixed(2).replace('.', ',')}`
}

export function formatDataBadge(dataHora: string): { dia: string; mes: string } {
  const d = new Date(dataHora)
  return {
    dia: String(d.getDate()).padStart(2, '0'),
    mes: MESES_PT[d.getMonth()].toUpperCase(),
  }
}

export function corCategoria(categoria?: string): string {
  const map: Record<string, string> = {
    'Música': '#7c3aed',
    'Teatro e Artes': '#0284c7',
    'Esportes': '#16a34a',
    'Conferências': '#b45309',
    'Vida Noturna': '#db2777',
    'Comida e Bebida': '#ea580c',
  }
  return categoria ? (map[categoria] ?? '#1d4ed8') : '#1d4ed8'
}
