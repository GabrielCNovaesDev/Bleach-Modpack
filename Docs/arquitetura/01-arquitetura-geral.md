# Arquitetura geral (Dragon Mine Z)

## Estado da implementação Bleach — 10/09/2026

QuestRegistry e FormRegistry separam snapshots de cliente e servidor por lado lógico. RegistryReload prepara ambas as definições antes da instalação; ProgressionService centraliza compras e normalização.

Consulte o [manual atual](../jogador/manual-do-jogador.md) e o [relatório de implementação](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções seguintes preservam a referência do Dragon Mine Z e não devem ser interpretadas como funcionalidades já entregues no Bleach.


## Visão da estrutura

O original separa o código em quatro raízes sob `com.dragonminez`:

```
com.dragonminez/
├── DragonMineZ.java          — @Mod, guarda de incompatibilidade, chama init
├── Reference.java            — MOD_ID, constantes
├── client/                   — HUD, telas, render, input
├── common/                   — shared: stats, quest, network, config, init
├── server/                   — commands, ticks, world, storage
└── mixin/                    — interceptações vanilla (pouco relevante ao MVP)
```

## Árvore de pacotes (comentada, só o que interessa ao Bleach)

```
common/
  DMZCommon.java              — init central: config, quests, network, registries
  config/                     — JSON editável pelo jogador (raças, forms, skills, gameplay)
  diagnostics/                — relatório de erro de JSON
  events/
    ModCommonEvents.java      — MOD bus (atributos, capabilities de SavedData)
    ForgeCommonEvents.java    — Forge bus (server start, reload, commands)
    DMZEvent.java             — eventos custom canceláveis (quest start/complete, form change)
  network/
    NetworkHandler.java       — SimpleChannel único
    C2S/                      — 54 packets client→server
    S2C/                      — 26 packets server→client
  quest/                      — ★ sistema de quests (prioridade máxima)
    objectives/               — 10 tipos de objetivo
    rewards/                  — 8 tipos de recompensa
  stats/                      — ★ capability do jogador
    StatsCapability.java
    StatsProvider.java
    StatsData.java            — agregador de 12 sub-objetos
    character/                — Character, Resources, Status, Cooldowns
    skills/                   — Skill, Skills
    techniques/               — TechniqueData (combate; fora do MVP)
    extras/                   — FormMasteries, UsedForms, ActionMode
  util/
    TransformationsHelper.java — ★ regras de unlock/select de forms
  init/                       — MainItems, MainEntities, MainSounds, etc. (conteúdo DB)

server/
  events/
    QuestEvents.java          — ★ progresso de quest
    players/
      TickHandler.java        — regen, charge de form, sync periódico
      actionmode/
        FormModeHandler.java  — ★ executa transformação
        StackFormModeHandler.java
      statuseffect/
        TransformStatusHandler.java
      StatsEvents.java        — mastery por hit
  commands/                   — /dmz* (reload quests, set stats…)
  storage/                    — NBT + JSON + MariaDB (NBT basta no MVP)

client/
  gui/
    character/QuestTreeScreen.java
    hud/TrackedQuestHUD.java
    quest/                    — diálogo NPC, toasts
    radial/                   — menu radial de forms
```

## Ponto de entrada

1. `DragonMineZ` (`@Mod("dragonminez")`)
   - Recusa `legendarytooltips` / `epicfight` / `bettercombat`
   - Chama `DMZCommon.init()`
   - `DistExecutor`: `DMZClient.init()` ou `DMZServer.init()`
2. `DMZCommon.init()`
   - `ConfigManager.initialize()`
   - `QuestRegistry.init()` (estrutura vazia; load real no server start)
   - `NetworkHandler.register()`
   - GeckoLib
   - Deferred registers (`Main*` no bus do mod)
   - `ModCommonEvents::commonSetup`
3. `ForgeCommonEvents.onServerStarting`
   - `StorageManager.init()`
   - `QuestRegistry.loadAll(server)` — gera/lê JSON no mundo
4. `StatsCapability` (Forge bus) anexa a capability a todo `Player`

## Capabilities registradas

| Capability / SavedData | O que armazena | Relevância Bleach |
|---|---|---|
| `StatsCapability` → `StatsData` | Todo o estado do jogador (stats, forms, quests, skills, energia) | **Essencial** |
| `DragonBallSavedData` (nível) | Posições de dragon balls | Fora de escopo (tema DB) |
| `ForgeCapabilities.ENERGY` / `ITEM_HANDLER` em block entities | Máquinas | Fora de escopo |

Não existe capability separada de “Quest” ou “Evolution”: ambos vivem dentro de `StatsData`.

## Canal de rede

Um único `SimpleChannel`:

- Nome: `dragonminez:network`
- Protocolo: `"1.0"`
- IDs sequenciais — **ordem de registro é o protocolo**; só se adiciona no fim

Lista completa e payloads em `06-rede-sincronizacao.md`. Pacotes que o Bleach precisa recriar (mínimo):

- C2S: ação de quest, claim de recompensa, track de quest, seleção de form/estágio, charge de transformação
- S2C: sync de stats (ou recorte), sync de registry de quests, toast de progresso, feedback de ação

## Convenções de nomenclatura observadas

| Padrão | Exemplo | Recomendação Bleach |
|---|---|---|
| Prefixo do mod | `DMZCommon`, `DMZPermissions` | `BLC` / `Bleach` / o id escolhido |
| Registries | `MainItems`, `MainEntities` | Manter `Main*` ou `Mod*` |
| Pacotes por lado | `client/`, `common/`, `server/` | Reusar |
| Packets | `*C2S`, `*S2C` | Reusar |
| Eventos | `*Events`, `*Handler` | Reusar |
| IDs de form | string lowercase (`supersaiyan2`) | string lowercase (`shikai`, `bankai`) |
| IDs de quest saga | `"sagaId:numericId"` | `"arcId:numericId"` (ex. `"soul_society:1"`) |
| IDs de sidequest | snake_case (`roshi_basic_training`) | snake_case (`rukia_basic_training`) |
| Stats | STR, SKP, RES, VIT, PWR, ENE | Mapear para atributos Bleach (ver `11-glossario.md`) |
| Lombok | `@Getter` `@Setter` em data | Opcional |
| Namespace | `Reference.MOD_ID` | `bleachmod` (definir) |

## Eventos custom (`DMZEvent`) relevantes

Canceláveis, disparados no bus do Forge:

- `QuestStartEvent`
- `QuestObjectiveProgressEvent`
- `QuestFailEvent` (razão: `PLAYER_DEATH`)
- `QuestTurnInEvent`
- `QuestCompletedEvent`
- `QuestRewardClaimEvent`
- `FormChangeEvent` / `StackFormChangeEvent` — `(player, oldGroup, oldForm, newGroup, newForm)`

O Bleach deve manter eventos equivalentes para que conteúdo (quests, NPCs) se conecte sem acoplar ao HUD.

## O que NÃO precisa ser recriado na arquitetura do MVP

- Dimensões (Namek, HTC, Otherworld, Sacred Kai)
- Worldgen / TerraBlender
- Wish manager / dragon balls
- Técnicas de ki customizáveis
- Beam clash, lock-on, flight combat
- Storage MariaDB/JSON (NBT do jogador basta)
- Mixins de título, shader, first-person

## Decisão de design observada

O original é **data-driven no mundo**, não em datapack: configs e quests são JSON gravados em `{world}/dragonminez/` e `{config}/dragonminez/`. Isso permite o admin editar sem recompilar, e um `QuestUpgrader` faz merge com defaults quando o mod atualiza.

Inferência: para o Bleach, o mesmo padrão (JSON no mundo + defaults em Java) é o caminho de menor risco, porque o parser, o registry e o sync `SyncQuestRegistryS2C` já estão desenhados em torno disso.
