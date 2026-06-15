import { chromium } from 'playwright'
import { mkdirSync } from 'node:fs'
const BASE = 'http://localhost:3000'
const OUT = 'shots'
mkdirSync(OUT, { recursive: true })

const ROLES = [
  { tag: 'comprador', email: 'ana@ingressify.local' },
  { tag: 'organizador', email: 'maria@ingressify.local' },
  { tag: 'operador', email: 'carlos@ingressify.local' },
  { tag: 'admin', email: 'admin@ingressify.local' },
]
const SENHA = 'Senha123'
const browser = await chromium.launch()

for (const r of ROLES) {
  const ctx = await browser.newContext({ viewport: { width: 1280, height: 860 }, deviceScaleFactor: 2 })
  const page = await ctx.newPage()
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' })
  await page.locator('input').nth(0).fill(r.email)
  await page.locator('input[type="password"]').first().fill(SENHA)
  await page.locator('button[type="submit"]').first().click()
  await page.waitForURL(u => !u.pathname.endsWith('/login'), { timeout: 8000 }).catch(() => {})
  await page.waitForTimeout(1200)
  await page.screenshot({ path: `${OUT}/nav-${r.tag}.png` })
  console.log(`landing ${r.tag} @ ${new URL(page.url()).pathname}`)

  // clicar no "Mais" se existir
  const mais = page.getByRole('button', { name: /^Mais$/ })
  if (await mais.count()) {
    await mais.first().click()
    await page.waitForTimeout(500)
    await page.screenshot({ path: `${OUT}/nav-${r.tag}-mais.png` })
    console.log(`  -> dropdown 'Mais' capturado`)
  } else {
    console.log(`  -> sem 'Mais' (poucos destinos)`)
  }
  await ctx.close()
}
await browser.close()
console.log('done')
