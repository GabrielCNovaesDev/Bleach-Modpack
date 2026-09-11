# Art Brief — Bleach Mod MVP (Forge 1.20.1)

Estado em 10/09/2026: o HUD foi redesenhado em código e o fundo recebeu cobertura proporcional. Os PNGs de espada e personagem permanecem pendentes; a tentativa gerada com quadriculado opaco foi descartada. Ver [relatório](../planejamento/relatorio-implementacao-mvp-2026-09-10.md).


## Nota de revisão após teste em jogo

Este briefing é referência de arte, não evidência de que os assets atuais atendem aos requisitos. O [plano de implementação](../planejamento/plano-implementacao-mvp.md) registra V01–V08: cobertura do background, transparência do espadachim/reiatsu/espada, orientação do Asauchi, alinhamento de HUD/diário e animação de carga.

Antes de substituir arte, distinguir defeito do PNG (alpha, bordas, orientação) de defeito de renderização/layout. Validar recortes sobre fundos claros e escuros, mantendo transparente somente a área pretendida. Conferir espada nas três formas, nas duas mãos e em primeira/terceira pessoa. O background deve cobrir a tela preservando proporção, com controles em área segura.

Novos assets de status e radial devem ser produzidos conforme as dimensões reais da UI implementada. Habilidades sem implementação não devem parecer ações disponíveis. Preservar arquivos-fonte de arte e preparar versões menores de runtime quando adequado. Aprovação visual deve ocorrer no jogo em múltiplas escalas; os relatos ainda não foram corrigidos.

---

You are a senior pixel artist and art director for an unofficial, fan-made Minecraft Forge mod inspired by *Bleach*. You are **not** copying Tite Kubo’s character designs, official Zanpakutō silhouettes, studio key art, or any copyrighted frame.

You will generate a **cohesive 31-texture UI + weapon pack** that looks like it belongs in the same game: Soul Society at dusk, handmade ink, spiritual pressure, restrained gold.

Work asset-by-asset. One image per filename. Never collage multiple assets into one canvas unless the spec says so.

---

## 0. Global style bible (apply to EVERY asset)

### World feeling
A field of spiritual pressure over old Japanese wood and night sky. Think: white haori cloth, black shihakushō, wet cobblestone, paper lanterns, distant seireitei walls. Not neon Tokyo. Not anime screenshot. Not “generic magic RPG purple fire.”

The player is an unnamed new Shinigami. The blade has **no name yet**. Keep it anonymous, ceremonial, a little lonely.

### Visual rules
- **Pixel art**, crisp nearest-neighbor edges. No blur, no photographic texture, no AI plastic sheen, no glow bloom that eats the silhouette.
- Limited palette, high readability at tiny sizes.
- One light source: cold moon from upper-left. Shadows fall down-right.
- Reiatsu is **lilac / violet mist**, never rainbow, never electric cyan.
- Gold is **old shrine gold**, used only for charge, titles, and Bankai accents.
- Transparent background on all items, icons, fills, and overlays. Only full-screen / panel backgrounds may be opaque.
- **No faces of canon characters.** No Ichigo, Rukia, Byakuya, Zangetsu, Senbonzakura, etc. No copied bankai shapes (giant black sword, cherry petals, ice flowers).
- No readable Latin or Japanese sentences in the pixels (a single seal mark is ok if abstract).
- No watermark, no signature, no UI chrome from other games.

### Shared palette (hex)
| Role | Hex | Use |
|---|---|---|
| Night ink | `#0E0A16` | outlines, voids |
| Haori black | `#1A1028` | panels, cloth |
| Soul wood | `#3A2A1C` | frames, journal |
| Paper cream | `#F2E9C8` | titles, parchment |
| Steel | `#C8D0DC` | sealed blade |
| Steel shadow | `#6A7380` | blade edge |
| Reiatsu | `#7B5CFF` | energy, Shikai accent |
| Reiatsu deep | `#2A1B4A` | bar empty, night |
| Charge gold | `#E8C547` | transformation charge |
| Fail blood | `#8B1E2D` | failed quest |
| Success moss | `#5FA86A` | claimed / complete |

### Technical
- PNG-32, sRGB, no EXIF junk.
- Exact pixel dimensions below. Do not add padding beyond the canvas. Do not scale after drawing — draw at target size.
- Filenames **exactly** as written (lowercase, underscores).
- Output folder implied by each section.

---

## 1. Weapons — `textures/item/`

All three are the **same sword evolving**. Same hilt language. Same diagonal pose as Minecraft iron sword: **pommel bottom-left, tip top-right**, about 35–40°. Leave 1px transparent margin so `handheld` does not clip.

### `asauchi.png` — 32×32 — REQUIRED (replaces current file)
**Prompt:** Front-facing pixel sword, unnamed Asauchi. A nameless academy blade: straight silver katana, unadorned iron tsuba as a plain oval, black-wrapped tsuka with one thin cream ito highlight, no charm, no glow. The steel is cold and unused, like it has never tasted a name. Moonlit rim-light on the back edge. Tiny nick on the kissaki so it feels forged, not clipart. Transparent background. Minecraft handheld diagonal. 32×32 pixel art, 1px outline in `#0E0A16`.

**Do not:** flames, runes, eyes on the blade, oversized anime guard, scabbard, blood.

### `asauchi_shikai.png` — 32×32
**Prompt:** The same Asauchi after first release. Keep the identical hilt and tsuba silhouette so the player recognizes it. The blade is 2px longer, slightly wider mid, a faint lilac reiatsu hairline along the hamon like breath on steel. One small abstract spirit-mark etched near the habaki — a broken circle, not a kanji from the manga. The wrap gains a single violet thread. Still a real sword, not an energy beam. Same diagonal, 32×32, transparent.

**Mood:** the blade just whispered its first syllable.

### `asauchi_bankai.png` — 32×32
**Prompt:** Same lineage, third form. Hilt still readable as the Asauchi’s child. Blade heavier, a darker steel core with a bright reiatsu edge. Tsuba now has four short spikes like a sealed sun, gold pin in the kashira. A short ribbon of haori-white cloth at the pommel, 3–4 pixels. Stronger violet-gold contrast, but the silhouette must still read as “sword” at 16×16 when downscaled. No giant slab, no chains filling the canvas, no skulls. 32×32, transparent, same diagonal.

**Mood:** the unnamed blade finally stands like a captain’s, without stealing anyone’s bankai.

---

## 2. Stage icons — `textures/gui/forms/` — 16×16 each

These sit beside the HUD label. Must be readable as a 16px stamp.

### `sealed.png`
**Prompt:** 16×16 pixel seal: a closed black saya (scabbard) seen as a short diagonal bar with a cream sageo knot. No face. Quiet. Transparent.

### `shikai.png`
**Prompt:** 16×16 unsheathed short blade with a single lilac spark at the tip. Same angle as sealed. Transparent.

### `bankai.png`
**Prompt:** 16×16 blade plus a small ring of reiatsu (broken circle) behind it, gold fleck at center. Still iconic, not a full illustration. Transparent.

---

## 3. HUD bars — `textures/gui/hud/`

Design frame and fill as a pair: the fill’s colored pixels must live in the **inner well** of the frame so a left-to-right crop looks like a draining/filling gauge.

### `reiatsu_frame.png` — 128×16
**Prompt:** Horizontal spiritual pressure gauge frame, 128×16. Dark wood-and-iron casing, thin cream inner lip, two tiny rivets. Empty well is deep violet `#2A1B4A`. Left cap looks like a miniature tsuba. Right cap like a saya chape. Pixel art, slight wear, no numbers, no text. Transparent outside the frame.

### `reiatsu_fill.png` — 128×16
**Prompt:** Only the inner liquid of reiatsu, 128×16. Horizontal gradient of `#2A1B4A` → `#7B5CFF` → a 1px cream highlight along the top of the fluid. Soft “wave” of 2px every 16px so it feels alive, not a flat Photoshop bar. Transparent everywhere that is not fill. The fill must align to the well of `reiatsu_frame.png` (start at x=6, height 8px centered).

### `charge_frame.png` — 128×16
**Prompt:** Thinner sibling of the reiatsu frame. Same length, slimmer well (4px tall). Iron and gold, like a ritual charging groove on a shrine step. Transparent outside.

### `charge_fill.png` — 128×16
**Prompt:** Gold `#E8C547` charge liquid with a 1px white-hot core. Align to the thin well. Transparent elsewhere. Feels like sunlight poured into a crack.

### `icon_reiatsu.png` — 16×16
**Prompt:** Abstract spiritual pressure: a small spiral of lilac mist around a dark pupil, not a Sharingan, not a sharingan clone, not a yin-yang. 16×16, transparent.

### `icon_tp.png` — 16×16
**Prompt:** Spirit points: a folded paper crane in cream and gold, 16×16, one lilac shadow. Transparent. Not a coin, not a star.

---

## 4. Tracked quest — `textures/gui/hud/`

### `quest_panel.png` — 192×64
**Prompt:** Compact mission scrap pinned to the upper-right of the screen. Dark translucent paper (`#1A1028` at ~80% — paint it opaque on dark, we will blit it). Torn cream edge, one wax-seal blot in gold, faint woodgrain. Room for 3 lines of text on the right two-thirds. Left strip reserved for a 16px icon. No letters.

### `icon_objective_kill.png` — 16×16
**Prompt:** Hollow-hunt mark: a simple white mask shard with two black holes, slashed by a silver line. Not a full Hollow mask from the anime. 16×16, transparent.

### `icon_objective_item.png` — 16×16
**Prompt:** A tied cloth furoshiki bundle, cream and brown, one reiatsu spark on the knot. 16×16, transparent.

### `icon_quest_track.png` — 16×16
**Prompt:** A small golden ofuda tag with a single vertical ink stroke. 16×16, transparent.

---

## 5. Toasts — `textures/gui/toast/`

### `toast_bg.png` — 192×32
**Prompt:** Top-of-screen banner. Horizontal lacquered black wood with a cream paper inset. Thin gold hairline. Space for icon at left (16×16 inset at x=6) and two text lines. No letters. Pixel art.

### `start.png` — 16×16
**Prompt:** A closed mission scroll just being untied. Cream, black cord. Transparent.

### `objective.png` — 16×16
**Prompt:** A check made of two sword scratches in gold on dark. Transparent.

### `complete.png` — 16×16
**Prompt:** An open fan, cream, one lilac vein. Victory without fireworks. Transparent.

### `fail.png` — 16×16
**Prompt:** A snapped sageo cord and a drop of `#8B1E2D`. Somber, not gore-porn. Transparent.

### `claim.png` — 16×16
**Prompt:** A small wooden offering box with a glow of gold inside. Transparent.

---

## 6. Character confirm — `textures/gui/character/`

### `confirm_bg.png` — 256×192
**Prompt:** Wide establishing pixel painting, 256×192. Night over a quiet Rukongai-like district: tiled roofs, one stone stair, paper lanterns, huge pale moon, distant white walls. No recognizable landmark from the anime city maps. A lone unsheathed Asauchi planted in the ground in the lower third, catching moonlight. Palette locked to the bible. Painterly pixels, not photobash. This may be fully opaque.

**Mood:** the night you decide to stop being human.

### `race_shinigami.png` — 128×128
**Prompt:** Anonymous Shinigami from the back-three-quarter, 128×128. Black shihakushō, white haori with no squad number, short dark hair, face turned away or lost in bangs — **no identifiable canon face**. Asauchi at the hip, sealed. Wind in the haori. Pixel art portrait, cream moonlight on the cloth edge. Transparent background around the figure.

---

## 7. Journal — `textures/gui/journal/`

### `journal_bg.png` — 320×200
**Prompt:** Open mission ledger. Left page: vertical list area (darker). Right page: detail area (lighter cream). Binding in the center like black silk. Corners worn. A dried ink brush in the margin. No written words, only ruled lines and a faint circular seal. Opaque. 320×200 pixel art.

### `journal_header.png` — 256×32
**Prompt:** A horizontal title plaque of dark wood and gold corners, empty center (we draw the title in code). 256×32, transparent outside the plaque.

### `status_not_started.png` — 16×16
**Prompt:** A closed empty circle, cream stroke on transparent. Locked potential.

### `status_accepted.png` — 16×16
**Prompt:** The same circle, half-filled with lilac. In progress.

### `status_success.png` — 16×16
**Prompt:** Circle filled gold with a tiny blade tick. Done.

### `status_failed.png` — 16×16
**Prompt:** Circle broken by a red crack. Failed, not evil.

---

## 8. Negative prompt (append to every generation)

`photorealistic, 3d render, blender, unreal engine, anime screenshot, official bleach art, ichigo, rukia, aizen, zangetsu, tensa zangetsu, senbonzakura, hyorinmaru, zabimaru, studio pierrot, watermark, signature, blur, bloom, jpeg artifacts, extra fingers, readable text, english letters, kanji sentences, neon cyberpunk, chibi sticker, emoji, glossy plastic, fan-service, gore, nsfw`

---

## 9. Generation order (do this sequence)

1. `asauchi.png` — this is the hero. Lock the hilt language.
2. `asauchi_shikai.png` then `asauchi_bankai.png` — mutate, do not redesign.
3. The three `forms/*.png` — distill the swords into stamps.
4. HUD frames + fills as a matching pair (generate frame first, fill second).
5. Quest panel + toast background (same wood/paper family as journal).
6. All 16×16 icons in one pass so stroke weight matches.
7. `confirm_bg.png` + `race_shinigami.png` last, using the finished Asauchi as reference in the scene.

After each image: downscale on/off in your mind. If it dies at 16×16, simplify.

---

## 10. Delivery checklist

Save exactly here, names exact:

```
src/main/resources/assets/bleachmod/textures/item/asauchi.png
src/main/resources/assets/bleachmod/textures/item/asauchi_shikai.png
src/main/resources/assets/bleachmod/textures/item/asauchi_bankai.png
src/main/resources/assets/bleachmod/textures/gui/forms/sealed.png
src/main/resources/assets/bleachmod/textures/gui/forms/shikai.png
src/main/resources/assets/bleachmod/textures/gui/forms/bankai.png
src/main/resources/assets/bleachmod/textures/gui/hud/reiatsu_frame.png
src/main/resources/assets/bleachmod/textures/gui/hud/reiatsu_fill.png
src/main/resources/assets/bleachmod/textures/gui/hud/charge_frame.png
src/main/resources/assets/bleachmod/textures/gui/hud/charge_fill.png
src/main/resources/assets/bleachmod/textures/gui/hud/icon_reiatsu.png
src/main/resources/assets/bleachmod/textures/gui/hud/icon_tp.png
src/main/resources/assets/bleachmod/textures/gui/hud/quest_panel.png
src/main/resources/assets/bleachmod/textures/gui/hud/icon_objective_kill.png
src/main/resources/assets/bleachmod/textures/gui/hud/icon_objective_item.png
src/main/resources/assets/bleachmod/textures/gui/hud/icon_quest_track.png
src/main/resources/assets/bleachmod/textures/gui/toast/toast_bg.png
src/main/resources/assets/bleachmod/textures/gui/toast/start.png
src/main/resources/assets/bleachmod/textures/gui/toast/objective.png
src/main/resources/assets/bleachmod/textures/gui/toast/complete.png
src/main/resources/assets/bleachmod/textures/gui/toast/fail.png
src/main/resources/assets/bleachmod/textures/gui/toast/claim.png
src/main/resources/assets/bleachmod/textures/gui/character/confirm_bg.png
src/main/resources/assets/bleachmod/textures/gui/character/race_shinigami.png
src/main/resources/assets/bleachmod/textures/gui/journal/journal_bg.png
src/main/resources/assets/bleachmod/textures/gui/journal/journal_header.png
src/main/resources/assets/bleachmod/textures/gui/journal/status_not_started.png
src/main/resources/assets/bleachmod/textures/gui/journal/status_accepted.png
src/main/resources/assets/bleachmod/textures/gui/journal/status_success.png
src/main/resources/assets/bleachmod/textures/gui/journal/status_failed.png
```

When you finish an asset, reply with the filename and a one-line art note (what you changed from the previous stage). Then wait for the next filename. Do not invent extra files.

## Revisão dos assets — 10/09/2026

As três texturas de espada e o espadachim têm alfa parcial excessivo; os PNGs originais permanecem inalterados, aguardando autorização para edição determinística. As tentativas de geração não produziram alfa real. O modelo katana_handheld compensa a orientação em 180 graus; se uma futura edição rotacionar a textura, remover essa compensação. Toasts passam a usar desenho em código, preservando a fila e limitando textos; seus PNGs antigos não são mais renderizados. Ver contagens e limites no [relatório](../planejamento/relatorio-implementacao-mvp-2026-09-10.md).
