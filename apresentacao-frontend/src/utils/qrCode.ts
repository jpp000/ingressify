const VERSION = 3
const SIZE = VERSION * 4 + 17
const DATA_CODEWORDS = 55
const ECC_CODEWORDS = 15

type Matrix = boolean[][]

const textEncoder = new TextEncoder()

export function gerarQrCode(texto: string): Matrix {
  const modulos = criarMatriz(false)
  const reservado = criarMatriz(false)

  desenharPadroesFuncao(modulos, reservado)

  const dados = criarDados(texto)
  const ecc = reedSolomonComputeRemainder(dados, ECC_CODEWORDS)
  const codewords = [...dados, ...ecc]

  desenharCodewords(modulos, reservado, codewords)
  aplicarMascara0(modulos, reservado)
  desenharFormato(modulos, reservado)

  return modulos
}

function criarMatriz(valor: boolean): Matrix {
  return Array.from({ length: SIZE }, () => Array.from({ length: SIZE }, () => valor))
}

function setModulo(modulos: Matrix, reservado: Matrix, x: number, y: number, escuro: boolean) {
  if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return
  modulos[y][x] = escuro
  reservado[y][x] = true
}

function desenharPadroesFuncao(modulos: Matrix, reservado: Matrix) {
  desenharFinder(modulos, reservado, 3, 3)
  desenharFinder(modulos, reservado, SIZE - 4, 3)
  desenharFinder(modulos, reservado, 3, SIZE - 4)
  desenharAlignment(modulos, reservado, 22, 22)

  for (let i = 0; i < SIZE; i++) {
    if (!reservado[6][i]) setModulo(modulos, reservado, i, 6, i % 2 === 0)
    if (!reservado[i][6]) setModulo(modulos, reservado, 6, i, i % 2 === 0)
  }

  setModulo(modulos, reservado, 8, SIZE - 8, true)

  for (let i = 0; i < 8; i++) {
    setModulo(modulos, reservado, 8, i, false)
    setModulo(modulos, reservado, i, 8, false)
    setModulo(modulos, reservado, SIZE - 1 - i, 8, false)
    setModulo(modulos, reservado, 8, SIZE - 1 - i, false)
  }
}

function desenharFinder(modulos: Matrix, reservado: Matrix, cx: number, cy: number) {
  for (let dy = -4; dy <= 4; dy++) {
    for (let dx = -4; dx <= 4; dx++) {
      const dist = Math.max(Math.abs(dx), Math.abs(dy))
      const escuro = dist === 3 || dist <= 1
      setModulo(modulos, reservado, cx + dx, cy + dy, escuro)
    }
  }
}

function desenharAlignment(modulos: Matrix, reservado: Matrix, cx: number, cy: number) {
  for (let dy = -2; dy <= 2; dy++) {
    for (let dx = -2; dx <= 2; dx++) {
      const dist = Math.max(Math.abs(dx), Math.abs(dy))
      setModulo(modulos, reservado, cx + dx, cy + dy, dist === 2 || dist === 0)
    }
  }
}

function criarDados(texto: string): number[] {
  const bytes = Array.from(textEncoder.encode(texto))
  if (bytes.length > 42) {
    throw new Error('Texto grande demais para o QR Code do ingresso.')
  }

  const bits: number[] = []
  appendBits(bits, 0b0100, 4)
  appendBits(bits, bytes.length, 8)
  for (const b of bytes) appendBits(bits, b, 8)

  const capacidadeBits = DATA_CODEWORDS * 8
  appendBits(bits, 0, Math.min(4, capacidadeBits - bits.length))
  while (bits.length % 8 !== 0) bits.push(0)

  const dados: number[] = []
  for (let i = 0; i < bits.length; i += 8) {
    dados.push(bits.slice(i, i + 8).reduce((acc, bit) => (acc << 1) | bit, 0))
  }
  for (let pad = 0; dados.length < DATA_CODEWORDS; pad++) {
    dados.push(pad % 2 === 0 ? 0xec : 0x11)
  }
  return dados
}

function appendBits(bits: number[], valor: number, tamanho: number) {
  for (let i = tamanho - 1; i >= 0; i--) bits.push((valor >>> i) & 1)
}

function desenharCodewords(modulos: Matrix, reservado: Matrix, codewords: number[]) {
  const bits = codewords.flatMap(c => Array.from({ length: 8 }, (_, i) => (c >>> (7 - i)) & 1))
  let bitIndex = 0
  let subindo = true

  for (let direita = SIZE - 1; direita >= 1; direita -= 2) {
    if (direita === 6) direita--
    for (let vert = 0; vert < SIZE; vert++) {
      const y = subindo ? SIZE - 1 - vert : vert
      for (let j = 0; j < 2; j++) {
        const x = direita - j
        if (reservado[y][x]) continue
        modulos[y][x] = bitIndex < bits.length && bits[bitIndex] === 1
        bitIndex++
      }
    }
    subindo = !subindo
  }
}

function aplicarMascara0(modulos: Matrix, reservado: Matrix) {
  for (let y = 0; y < SIZE; y++) {
    for (let x = 0; x < SIZE; x++) {
      if (!reservado[y][x] && (x + y) % 2 === 0) modulos[y][x] = !modulos[y][x]
    }
  }
}

function desenharFormato(modulos: Matrix, reservado: Matrix) {
  const bits = calcularBitsFormato()

  for (let i = 0; i <= 5; i++) setModulo(modulos, reservado, 8, i, bit(bits, i))
  setModulo(modulos, reservado, 8, 7, bit(bits, 6))
  setModulo(modulos, reservado, 8, 8, bit(bits, 7))
  setModulo(modulos, reservado, 7, 8, bit(bits, 8))
  for (let i = 9; i < 15; i++) setModulo(modulos, reservado, 14 - i, 8, bit(bits, i))

  for (let i = 0; i < 8; i++) setModulo(modulos, reservado, SIZE - 1 - i, 8, bit(bits, i))
  for (let i = 8; i < 15; i++) setModulo(modulos, reservado, 8, SIZE - 15 + i, bit(bits, i))
  setModulo(modulos, reservado, 8, SIZE - 8, true)
}

function calcularBitsFormato(): number {
  const dadosFormato = 0b01 << 3 // Correção L, máscara 0.
  let resto = dadosFormato << 10
  for (let i = 14; i >= 10; i--) {
    if (((resto >>> i) & 1) !== 0) resto ^= 0x537 << (i - 10)
  }
  return ((dadosFormato << 10) | resto) ^ 0x5412
}

function bit(valor: number, indice: number): boolean {
  return ((valor >>> indice) & 1) !== 0
}

function reedSolomonComputeRemainder(data: number[], degree: number): number[] {
  const generator = reedSolomonGenerator(degree)
  const result = Array.from({ length: degree }, () => 0)

  for (const b of data) {
    const factor = b ^ result.shift()!
    result.push(0)
    for (let i = 0; i < degree; i++) result[i] ^= gfMultiply(generator[i], factor)
  }
  return result
}

function reedSolomonGenerator(degree: number): number[] {
  const result = Array.from({ length: degree }, () => 0)
  result[degree - 1] = 1

  let root = 1
  for (let i = 0; i < degree; i++) {
    for (let j = 0; j < degree; j++) {
      result[j] = gfMultiply(result[j], root)
      if (j + 1 < degree) result[j] ^= result[j + 1]
    }
    root = gfMultiply(root, 0x02)
  }
  return result
}

function gfMultiply(x: number, y: number): number {
  let z = 0
  for (let i = 7; i >= 0; i--) {
    z = (z << 1) ^ ((z >>> 7) * 0x11d)
    z ^= ((y >>> i) & 1) * x
  }
  return z
}
