# Design

Sistema visual do Ingressify. **Register:** product. **Estratégia de cor:** Committed-restrained — um accent de marca (violeta) carrega a identidade sobre neutros verdadeiros; coral entra só como energia pontual.

## Theme

Claro premium. Fundo off-white **puro** (chroma ~0, nunca cream), tinta near-black, superfícies brancas com bordas suaves e sombras de baixa opacidade tingidas de violeta. Sensação: arejado, confiável, com vida. Cena: o comprador no celular à noite escolhendo um show; o organizador no desktop de dia gerindo eventos — ambos precisam ler valores e status sem esforço.

## Color (OKLCH)

```
--bg:            oklch(0.985 0.003 285)   /* off-white puro, whisper de violeta */
--surface:       oklch(1     0     0)     /* cards/painéis */
--surface-2:     oklch(0.965 0.006 285)   /* zonas sutis, hover de linha */
--border:        oklch(0.910 0.008 285)
--border-strong: oklch(0.840 0.012 285)

--ink:           oklch(0.24  0.030 285)   /* texto principal (~14:1) */
--ink-2:         oklch(0.44  0.025 285)   /* texto secundário (~7:1) */
--ink-muted:     oklch(0.50  0.020 285)   /* meta/placeholder (≥4.5:1) */

--brand:         oklch(0.55  0.20  285)   /* violeta elétrico — ações primárias */
--brand-strong:  oklch(0.47  0.21  285)   /* hover/active e texto de marca em branco (AA) */
--brand-soft:    oklch(0.955 0.030 285)   /* tint de seleção/estado ativo */
--brand-contrast:oklch(0.99  0.005 285)   /* texto sobre --brand */

--accent:        oklch(0.68  0.17  35)    /* coral — destaque pontual (ao vivo/featured) */
--accent-soft:   oklch(0.96  0.03  45)

--success:       oklch(0.60  0.15  150)
--warn:          oklch(0.68  0.15  75)
--danger:        oklch(0.57  0.21  25)
--danger-soft:   oklch(0.955 0.03  25)
```

Regras: texto de marca usa `--brand-strong` sobre branco (AA). Status nunca só por cor — sempre ícone/rótulo. Sombras tingidas de violeta, alpha ≤0.12.

## Typography

- **Display** (`--font-display`): **Clash Display** — h1/h2, números grandes (preços, valores). Peso 600–700. letter-spacing ≥ -0.02em.
- **Texto/UI** (`--font-sans`): **General Sans** — corpo, labels, dados. Pareamento por eixo display×texto (mesma foundry, Fontshare).
- Escala (clamp): display `clamp(2rem, 5vw, 3.5rem)`; h1 ~2rem; h2 ~1.5rem; h3 ~1.25rem; corpo 1rem/1.0625rem; meta 0.8125rem. Linha de prosa ≤ 70ch.
- `text-wrap: balance` em h1–h3; `pretty` em prosa.

## Spacing & Radius

- Espaço base 4px: 4 · 8 · 12 · 16 · 24 · 32 · 48 · 64 · 96.
- Raio: sm 8 · md 12 · lg 16 · xl 24 · pill 999. Padrão de cards/inputs: 12–16.

## Elevation & Motion

- `--shadow-sm: 0 1px 2px oklch(0.4 0.05 285 / .06)`
- `--shadow-md: 0 4px 16px oklch(0.4 0.05 285 / .08)`
- `--shadow-lg: 0 16px 40px oklch(0.4 0.05 285 / .12)`
- Easing: `--ease: cubic-bezier(0.16, 1, 0.3, 1)` (ease-out-expo). Durações 150/240/400ms. Sem bounce/elastic.
- `@media (prefers-reduced-motion: reduce)`: transições viram crossfade/instantâneo.

## Z-index scale

`--z-dropdown:1000 · --z-sticky:1100 · --z-backdrop:1200 · --z-modal:1300 · --z-toast:1400 · --z-tooltip:1500`

## Components

- **Button**: `.btn` (primário sólido violeta), `.btn--ghost`, `.btn--soft`, `.btn--danger`, tamanhos sm/md/lg, pill. Foco com ring `--brand`.
- **Input/Field**: `.field` com label flutuante/topo, `.input`, estados focus/erro; alvo ≥44px.
- **Card/Surface**: `.surface` borda 1px + raio + sombra sm; sem nested cards; sem side-stripe.
- **Badge/Chip**: status (sólido suave) e categoria (cor por `corCategoria`), `.chip` para filtros selecionáveis.
- **App shell**: top bar (logo, busca global, saldo, menu de perfil) + navegação primária por papel; mobile com bottom-tab + sheet. Substitui o esquema "principal/Acesso rápido".
- **Money**: valores monetários em `--font-display`, tabular-nums.

## Imagery & Icons

Ícones: biblioteca SVG consistente (lucide), 1.5px stroke — substitui os emojis atuais. Capas de evento com overlay para contraste de texto.
