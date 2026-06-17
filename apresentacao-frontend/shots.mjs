import { chromium } from 'playwright'
import { mkdirSync } from 'node:fs'

const BASE = process.env.BASE || 'http://localhost:3000'
const EMAIL = process.env.EMAIL || 'ana@ingressify.local'
const SENHA = process.env.SENHA || 'Senha123'
const OUT = 'shots'
mkdirSync(OUT, { recursive: true })

// rotas: [path, nome, {mobile?}]
const ROTAS = JSON.parse(process.env.ROTAS || '[["/","catalogo"],["/meus-ingressos","carteira"],["/saldo","saldo"],["/revendas","revendas"],["/sorteios","sorteios"]]')

const browser = await chromium.launch()

const VPS = JSON.parse(process.env.VPS || '[{"w":1280,"h":900,"tag":"desktop"},{"w":390,"h":844,"tag":"mobile"}]')
for (const vp of VPS) {
  const ctx = await browser.newContext({ viewport: { width: vp.w, height: vp.h }, deviceScaleFactor: Number(process.env.DSF || 2) })
  const page = await ctx.newPage()
  if (!process.env.NOLOGIN) {
    // login na MESMA aba (sessionStorage é por-aba)
    await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' })
    const inputs = page.locator('input')
    await inputs.nth(0).fill(EMAIL)
    await page.locator('input[type="password"]').first().fill(SENHA)
    await page.locator('button[type="submit"], button:has-text("Entrar")').first().click()
    await page.waitForURL(u => !u.pathname.endsWith('/login'), { timeout: 8000 }).catch(() => {})
    await page.waitForTimeout(800)
  }

  for (const [path, nome] of ROTAS) {
    try {
      await page.goto(`${BASE}${path}`, { waitUntil: 'networkidle', timeout: 15000 })
      await page.waitForTimeout(900)
      await page.screenshot({ path: `${OUT}/${nome}-${vp.tag}.png`, fullPage: process.env.FULL !== '0' })
      console.log(`ok ${nome}-${vp.tag}`)
    } catch (e) {
      console.log(`ERRO ${nome}-${vp.tag}: ${e.message}`)
    }
  }
  await ctx.close()
}
await browser.close()
console.log('done')
