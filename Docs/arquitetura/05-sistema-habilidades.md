# Sistema de Habilidades

## Resumo

O original tem **dois** sistemas que o nome “skill” mistura: (1) `Skill` — progressão/toggle/gate de form, comprada com TP; (2) `Technique` — ataque executável com custo, charge e cooldown. O sistema de evolução depende só do primeiro. Técnicas de ki customizáveis ficam fora do MVP Bleach; o que importa é o modelo de `Skill` + a ponte `SkillReward` / form-skill.

## Modelo de dados

### Skill

| Campo | Tipo | Propósito |
|---|---|---|
| `name` | `String` | Id canônico (armazenado com casing original) |
| `level` | `int` | Nível atual, clamp em `maxLevel` |
| `isActive` | `boolean` | Toggle (voo, ki sense, ki manipulation…) |
| `maxLevel` | `int` | Cap da config (form-skills: length do array de custos da raça) |

Container `Skills`: `Map<String, Skill>` com chave **lowercased**. API: `registerDefaultSkill`, `setSkillLevel`, `addSkillLevel`, `setSkillActive`, `toggleSkillActive`, `isUnlockedAtLevel`, `repairSkillNames`.

Cap padrão: `SkillsConfig.getSkillCosts(name).costs.size()` (teto 50; `potentialunlock` teto 30). Form-skills ignoram esse teto e usam o da raça.

### TechniqueData (resumo — não portar no MVP)

| Campo | Tipo |
|---|---|
| `id`, `name`, `author` | `String` |
| `experience` | `int` |
| `baseCost`, `castTime`, `cooldown` | `int` |
| `tpCost` | `float` |

Subclasses: `KiAttackData`, `StrikeAttackData`. `Techniques` guarda unlocked + 8 slots + estado de charge. Cooldown vai para `Cooldowns` (mapa string→ticks), não para o `Skill`.

### Skills vs Techniques

| | Skill | Technique |
|---|---|---|
| Papel | Gate de progressão / passiva / toggle | Ataque |
| Progressão | Nível 0–N via TP | XP de uso |
| Ativação | Toggle ou check de nível | Slot + hold + fire |
| Energia | Indireta (transform, release) | `getCalculatedCost` no cast |
| Cooldown | Nenhum no objeto Skill | `TechniqueData.cooldown` |
| Unlock | Quest, compra TP, defaults da raça | Compra de skill pode clonar de `PredefinedTechniques` |

## Fluxo de funcionamento

1. Criação/load de personagem → `updateTransformationSkillLimits(race)` registra form-skills com max = length dos custos TP da raça.
2. Player abre menu de skills → C2S `UpdateSkillC2S`:
   - `TOGGLE`: flip `isActive` se `level > 0`
   - `PURCHASE` / `UPGRADE`: se TP ≥ custo do próximo nível, incrementa, pode unlock technique
3. Server responde `StatsSyncS2C`.
4. `TransformationsHelper.isFormUnlocked` consulta `isUnlockedAtLevel(formSkill, unlockOnSkillLevel)`.
5. Quest `SkillReward`: `setSkillLevel` só se o novo nível for maior.
6. Quest `SkillObjective`: a cada 20 ticks, progresso = nível atual vs required.
7. Skills de suporte que afetam transformação (original): `potentialunlock` sobe o teto de power release (`50 + 5*level`); `kicontrol` toggle de aura; `meditation`/`kiboost` regen durante charge. O Bleach pode ter equivalentes (`reiatsu_control`) ou omitir.

Não há tick de “ativar skill ofensiva”. Form transform é ActionMode, não um Skill.cast().

## Ciclo de vida (o que o Bleach precisa)

```
register (raça) → level 0
     ↓ compra TP ou reward de quest
  level N  →  desbloqueia forms com unlockOnSkillLevel ≤ N
     ↓ toggle (se a skill for ativa)
  isActive true/false  →  passiva/HUD
```

Custo de energia da transformação **não** está na Skill; está na FormData (ver `04`).

## Pontos de integração

| Sistema | Relação |
|---|---|
| Evolução | Form-skill é o portão de tier |
| Quests | Reward e objective de skill; pré-req SKILL |
| Resources | TP para compra |
| Rede | `UpdateSkillC2S` + sync |
| Técnicas | Ponte opcional na compra — omitir no MVP |

## Decisões de design observadas

- Skill é um **contador + flag**, não uma classe com `activate()`. Comportamento vive em handlers que perguntam `getSkillLevel` / `isActive`.
- Form-skills e skills “normais” compartilham o mesmo mapa. O nome (`superforms`) é o que as distingue.
- Inferência: isso é conveniente para um único menu de TP, mas polui o mapa. O Bleach pode separar `ProgressionSkills` (zanpakuto) de `UtilitySkills` se quiser clareza.

## Riscos/complexidades para reimplementar

- Esquecer `updateTransformationSkillLimits` no load → Bankai “comprável” com maxLevel 0.
- `isUnlockedAtLevel` vs `level > 0`: o original trata nível da skill como índice do tier, não como booleano.
- Não portar o editor de técnicas (create/import/upgrade) — é um jogo dentro do jogo.
- Cooldowns de técnica não devem ser confundidos com cooldown de form (não existe).

## Implementação Bleach — Kidou

Kidou é atualmente um atributo passivo, não uma skill executável. A fórmula central já fornece +10% de dano de feitiço por rank, mas nenhum ataque existente chama esse caminho ainda. Ao criar a primeira rajada, o handler deverá aplicar `CombatBalance.kidouDamage` no servidor; não reutilizar Zanjutsu ou Hakuda.
