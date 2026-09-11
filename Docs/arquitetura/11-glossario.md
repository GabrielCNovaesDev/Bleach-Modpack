# Glossário

## Estado da implementação Bleach — 10/09/2026

No Bleach atual: reiatsu é energia; pontos espirituais compram skills/categorias; descoberta autoriza a compra da forma e mastery representa domínio. Hollow continua como tema; os alvos provisórios são zumbis.

Consulte o [manual atual](../jogador/manual-do-jogador.md) e o [relatório de implementação](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções seguintes preservam a referência do Dragon Mine Z e não devem ser interpretadas como funcionalidades já entregues no Bleach.


Termos do código original traduzidos para o domínio Bleach e para o vocabulário do fork.

## Identidade e progressão

| Termo original | Significado mecânico | Analogia Bleach sugerida |
|---|---|---|
| Race | Tipo permanente do personagem; escolhe quais árvores de form existem | Raça: Shinigami, Hollow, Quincy, Fullbringer |
| Class | Especialização dentro da raça; altera stats base (`warrior`…) | Especialização (Kido / Zanjutsu / Hakuda) — opcional no MVP |
| Form group | Ramo de transformação (`supersaiyan`, `evolutionforms`) | Ramo: `zanpakuto`, `hollowfication`, `vollstandig` |
| Form | Um estágio ordenado dentro do grupo | Estágio: sealed → shikai → bankai (Shinigami) |
| Stack form | Multiplicador ortogonal (Kaioken, Ultimate) | Camada extra (ex. Hollowfication sobre Shikai) — fase futura |
| Form mastery | Float por `group:form`; libera o próximo estágio e instant transform | Domínio da Zanpakutō / do estágio |
| Form skill | Skill comprada com TP que libera tiers (`superforms` nível N) | Skill de progressão (ex. `zanpakuto` nível 0/1/2 = selada/shikai/bankai) |
| Training Points (TP / TPS) | Moeda de progressão ganha em quest/treino | Pode virar XP de reiatsu / pontos de habilidade |
| Power release | % de poder liberado; escala dano e drain | % de reiatsu liberada |
| Action charge / FormRelease | Barra 0–100 da transformação | Charge de Shikai/Bankai |
| Dynamic growth | XP de prática por stat | Fora do MVP |

## Recursos

| Termo original | Significado | Analogia Bleach |
|---|---|---|
| Energy / Ki | Recurso principal; custa transform e técnicas | **Reiatsu** |
| Stamina | Recurso secundário; dash/bloqueio/forms | Stamina (manter) |
| Poise | Resistência a stagger | Fora do MVP |
| Alignment | Eixo moral numérico | Fora do MVP (ou “Hollowfication residual”) |
| Battle power | Stats × release | Pressão espiritual |

## Stats

| Sigla | Nome no original | Uso |
|---|---|---|
| STR | Strength | Dano físico |
| SKP | Strike Power | Dano de golpe/ki strike |
| RES | Resistance | Média implícita DEF+STM em alguns cálculos |
| DEF | Defense | Redução de dano |
| STM | Stamina stat | Cap/regen de stamina |
| VIT | Vitality | HP |
| PWR | Ki Power | Poder de técnicas |
| ENE | Energy | Cap/regen de ki |

O Bleach pode renomear, mas o **contrato de multiplier por stat** (form JSON) assume essas chaves.

## Quests

| Termo original | Significado |
|---|---|
| Saga | Arco linear de quests (`saiyan_saga`); requer saga anterior |
| Sidequest | Missão avulsa com `stringId` |
| Daily / Event | Tipos no enum; **sem lógica especial de reset** no código atual |
| Quest key | Saga: `"sagaId:numericId"`; side: `"stringId"` |
| Prerequisites | Condições para **aparecer/estar disponível** |
| Requirements (start requirements) | Condições no **momento de aceitar** (bioma, dimensão, TIME…) |
| Claim mode | `TREE_OR_NPC` ou `NPC_ONLY` |
| Turn-in | NPC que fecha a quest; se vazio, completa automático |
| Party controller | Líder da party; dono do estado sincronizado |
| Story difficulty | EASY/NORMAL/HARD; escala HP/dano de inimigos de quest e rewards |
| Story reset | Wish que zera story e decai TP (`storyResetCount`) |
| Secret quest | Oculta na UI até pré-requisitos |
| Tracked quest | Quest exibida no HUD |
| Count mode | Kill: só spawns da quest vs qualquer entidade matching |
| Spawn mode | Kill: `QUEST` (spawna ao aceitar) vs `NATURAL` |
| Contiguous kill block | Kills sequenciais no JSON compartilham unlock — pitfall documentado no Memory.md do original |

## Rede e persistência

| Termo original | Significado |
|---|---|
| StatsSyncS2C | NBT completo de `StatsData` |
| ResourceSyncS2C | Só `Resources` + `Status` |
| ProgressionSyncS2C | Stats, bônus, skills, techniques, **PlayerQuestData** |
| AppearanceSyncS2C | Só `Character` |
| SyncQuestRegistryS2C | Definições de saga/quest em JSON (até 1 MB cada) |
| Partial load | `StatsData.load` só aplica chaves presentes |
| World JSON | `{world}/dragonminez/quests|sagas|sidequests` — fonte runtime, não datapack |
| previousQuests / previousConfigs | Snapshot no JAR para upgrade/merge, **não** carregado como runtime |

## Eventos custom

| Evento | Quando |
|---|---|
| `QuestStartEvent` | Antes de aceitar; cancelável |
| `QuestObjectiveProgressEvent` | Incremento de objetivo |
| `QuestFailEvent` | Wipe de party em quest com kill |
| `QuestTurnInEvent` | Entrega no NPC |
| `QuestCompletedEvent` | Status → SUCCESS |
| `QuestRewardClaimEvent` | Antes de entregar reward |
| `FormChangeEvent` | Form ativa mudou |
| `DragonSummonedEvent` | Fora do escopo Bleach (esferas) |

## UI

| Termo | Significado |
|---|---|
| Quest tree | Tela principal de sagas/sidequests |
| Radial menu | Seleção de form/stack |
| Story toast | Notificação de start/fail/complete/objective |
| Scouter | HUD de leitura de BP alheio — tema DB; o Bleach pode ter um “sensor de reiatsu” depois |

## Ações de transformação

| ActionMode | Papel |
|---|---|
| FORM | Charge/transform da form base |
| STACK | Charge do multiplicador |
| FUSION | Fora do MVP |
| RACIAL | Passiva racial (cauda etc.) |

| ExecuteAction | Papel |
|---|---|
| INSTANT_TRANSFORM | Pula a barra se mastery ≥ limiar |
| FORCE_DESCEND | Volta um estágio / base |
| INSTANT_RELEASE | Sobe power release sem hold |

## Prefixos e ids

| Prefixo | Uso |
|---|---|
| `dmz_` | NBT tags em entidades de quest (`dmz_quest_key`, `dmz_quest_owner`…) |
| `dragonminez:` | Namespace de items/entities/dimensões |
| `/dmz*` | Commands de admin |

No Bleach, escolher um prefixo único (`blc_`, `bleach_`) e não reusar `dmz_` para evitar colisão se alguém rodar os dois mods.
