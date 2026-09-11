# Sistema de Quests

## Atualização Bleach — 10/09/2026

Resgate em lote usa índice -1 com flags individuais e sincronização final. As rotas de atualização/conclusão conferem a assinatura da quest. GameTests cobrem resgates de pontos, itens e transformação, repetição e reload.

[Relatório atual](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções de Dragon Mine Z abaixo são referência do original.


## Resumo

O sistema de quests é o coração reutilizável do original: definições em JSON no **mundo** (não datapack), progresso por jogador dentro da capability, objetivos polimórficos (10 tipos), recompensas reclamáveis (8 tipos), sagas lineares + sidequests, party com merge só-para-frente, e falha em wipe se a quest tem objetivo de kill. Recompensas **não** são automáticas — o jogador reclama na árvore ou no NPC. Tipos `DAILY` e `EVENT` existem no enum mas **não têm reset**.

## Modelo de dados

### Quest (definição)

| Campo | Tipo | Valores / notas |
|---|---|---|
| `id` | `int` | Id numérico de saga; `-1` se sidequest |
| `stringId` | `String` | Id de sidequest; nulo em saga |
| `type` | `QuestType` | `SAGA`, `SIDEQUEST`, `DAILY`, `EVENT` |
| `title` | `String` | Chave de tradução ou texto |
| `description` | `String` | |
| `objectives` | `List<QuestObjective>` | |
| `rewards` | `List<QuestReward>` | |
| `category` | `String` | ex. `saga_saiyan`, `training` |
| `parallelObjectives` | `boolean` | false = sequencial |
| `partyScaling` | `boolean` | escala count/HP de kill e count de item |
| `questGiver` | `String` | Id do NPC que oferece |
| `turnIn` | `String` | Id do NPC que fecha; vazio = auto-complete |
| `prerequisites` | `QuestPrerequisites` | Disponibilidade |
| `startRequirements` | `QuestPrerequisites` | JSON key `"requirements"` — no aceite |
| `secret` | `boolean` | Oculta até unlock |
| `claimMode` | `ClaimMode` | `TREE_OR_NPC` (default) ou `NPC_ONLY` |

Chave runtime:

- Saga: `"sagaId:numericId"` (ex. `"saiyan_saga:1"`)
- Sidequest: o `stringId`

### Objetivo base

| Campo | Tipo |
|---|---|
| `type` | `ObjectiveType` |
| `required` | `int` |
| `progress` | `int` (no template; o progresso real está em `PlayerQuestData`) |

### Tipos de objetivo

| Tipo | Classe | Campos JSON | Como incrementa |
|---|---|---|---|
| `ITEM` | `ItemObjective` | `item`, `count` | Poll do inventário a cada 20 ticks. **Não consome** o item |
| `KILL` | `KillObjective` | ver tabela abaixo | `LivingDeathEvent` |
| `INTERACT` | `InteractObjective` | `entity`, `entityName` | Right-click na entidade |
| `STRUCTURE` | `StructureObjective` | `structure` | Tick: jogador dentro da estrutura |
| `BIOME` | `BiomeObjective` | `biome` (aceita `#tag`) | Tick: bioma atual |
| `DIMENSION` | `DimensionObjective` | `dimension` | Tick: dimensão atual |
| `COORDS` | `CoordsObjective` | `x,y,z`, `radius` (default 10) | Tick: distância |
| `TALK_TO` | `TalkToObjective` | `npcId` | Interact, **exceto** se o NPC é o `turn_in` (aí só no turn-in) |
| `DRAGON_SUMMON` | `DragonSummonObjective` | `dragon` / `ball_set` | Evento custom de invocação — **não portar** (tema DB) |
| `SKILL` | `SkillObjective` | `skill`, `level` | Tick: `getSkillLevel >= required` |

#### KillObjective (campos extras)

| Campo | Tipo | Default |
|---|---|---|
| `entity` | registry id ou `#tag` | obrigatório |
| `count` | int | obrigatório |
| `health` | double | 20 |
| `meleeDamage` / `kiDamage` | double | 1 |
| `spawn` | `QUEST` \| `NATURAL` | `QUEST` |
| `count_mode` | `QUEST_SPAWNED_ONLY` \| `ANY_MATCHING` | `QUEST_SPAWNED_ONLY` |
| `TextureVariant` / `AITier` | int | -1 (sem override) |
| `canTransform` | bool | true |
| `TransformHealth` etc. | Double nullable | fallback na config global de entities |

Entidades spawnadas pela quest recebem NBT: `dmz_quest_key`, `dmz_quest_objective_index`, `dmz_quest_owner`, `dmz_saga_id`, `dmz_quest_team`, mais HP/dano já escalados.

### Tipos de recompensa

| Tipo | Classe | Campos JSON | Efeito |
|---|---|---|---|
| `ITEM` | `ItemReward` | `item`, `count` | Dá item; overflow dropa no chão |
| `GENERIC_ITEM` | `GenericItemReward` | `itemReward` (DTO) | Item genérico do sistema de wish |
| `TPS` | `TPSReward` | `amount` | Soma TP; **escala** com dificuldade + decay de story reset |
| `ALIGNMENT` | `AlignmentReward` | `amount` | Soma alignment |
| `SKILL` | `SkillReward` | `skill`, `level` | Sobe skill se estiver abaixo |
| `TRANSFORMATION` | `TransformationReward` | `formGroup`, `formName`, `mastery` (default 100), `stack` | Concede mastery + cadeia de requisitos + nível da form-skill |
| `KI_TECHNIQUE` | `KiTechniqueReward` | `code` | Unlock de técnica — fora do MVP |
| `COMMAND` | `CommandReward` | `command`, `translationKey` | Roda comando (`%player%`) |

Toda reward aceita filtro `difficulty` / `difficulties` (string, lista ou CSV). Sem filtro = todas as dificuldades.

### Status por jogador (`QuestProgress`)

| Campo | Tipo | Valores |
|---|---|---|
| `questId` | `String` | chave |
| `status` | `QuestStatus` | `NOT_STARTED`, `ACCEPTED`, `FAILED`, `SUCCESS` |
| `objectives` | `Map<int,int>` | índice → progresso |
| `objectiveRequirements` | `Map<int,int>` | índice → required já escalado (party) |
| `rewards` | `Map<int,bool>` | índice → reclamado |
| `failureCount` | `int` | incrementa a cada fail |
| `difficulty` | `Difficulty` | snapshot da dificuldade ao aceitar |

Rank de merge (só avança, nunca regride): `NOT_STARTED(0) < FAILED(1) < ACCEPTED(2) < SUCCESS(3)`.

### Pré-requisitos

Operador `AND` (default) ou `OR`; grupos aninhados permitidos.

| Tipo | Campos | Uso típico |
|---|---|---|
| `SAGA_QUEST` | `sagaId`, `questId` (int) | Quest anterior da saga |
| `QUEST` | `questId` (string) | Sidequest concluída |
| `STAT` | `stat`, `minValue` | Stat mínimo |
| `LEVEL` | `minLevel` | Nível derivado de `StatsData.getLevel()` |
| `BIOME` | `biome` | Start requirement espacial |
| `STRUCTURE` | `structure`, `hint{dimension,x,y,z}` | Idem + dica de mapa |
| `DIMENSION` | `dimension` | |
| `TIME` | `mode` (`GAME_TIME`\|`REAL_TIME`), `ticks` ou `milliseconds` | Tempo desde que a quest ficou elegível |
| `ALIGNMENT` | `min`, `max` | |
| `SKILL` | `skill`, `minLevel` | |
| `RACE` | `race` | |
| `CLASS` | `class` | |

`prerequisites` = pode ver/começar. `requirements` = precisa estar no lugar/tempo **no clique de Start**.

### Saga

| Campo | Tipo |
|---|---|
| `id` | `String` |
| `name` | `String` (tradução) |
| `quests` | lista ordenada |
| `requirements.previousSaga` | id da saga anterior ou `""` |
| `questFolder` | pasta em `quests/` |

Sagas do original (só mecânica): 6 manifestos, ~122 quests de saga, ~89 sidequests. **Não copiar o conteúdo DB.**

### Dificuldade

| Valor | HP inimigo | Dano inimigo | TP | Reward geral | AI tier |
|---|---|---|---|---|---|
| EASY | config `easyModeHPMultiplier` | `easyModeDamageMultiplier` | `easyModeTPMultiplier` | `easyModeQuestRewardMultiplier` | 1 |
| NORMAL | 1.0 | 1.0 | 1.0 | 1.0 | 2 |
| HARD | `hardMode*` | idem | idem | idem | 3 |

Multiplicador de reward entregue: `difficulty.questRewardMultiplier()`; se a reward é TPS, multiplica ainda `tpRewardMultiplier() = perReset ^ storyResetCount`.

## Como uma quest é definida

Não é datapack. Fluxo:

1. Server start → `QuestRegistry.loadAll(server)`.
2. Se `{world}/dragonminez/` não tem arquivos, geradores Java gravam defaults:
   - `QuestDefaults` → `quests/<pasta>/`
   - `SagaDefaults` → `sagas/`
   - `SideQuestDefaults` → `sidequests/<categoria>/`
3. `QuestParser.parseQuest` + `validate()` (chaves permitidas listadas abaixo).
4. Registry indexa por tipo de objetivo, quest giver e turn-in NPC.
5. Login → `SyncQuestRegistryS2C` manda o JSON das definições ao client (até 1 MB sagas + 1 MB quests).
6. `/dmzreload quests` recarrega e re-sincroniza online players.

`previousQuests/` no JAR é arquivo morto para o `QuestUpgrader` (merge quando o default muda). Não é carregado como runtime.

### Schema JSON (chaves válidas no topo)

`id`, `title`, `type`, `description`, `category`, `parallel_objectives`, `party_scaling`, `quest_giver`, `turn_in`, `secret`, `claim_mode`, `prerequisites`, `requirements`, `objectives`, `rewards`, `defaultsVersion`.

Obrigatórios: `id`, `title`, `type`.

### Exemplo de estrutura — quest de arco (tema removido)

```json
{
  "id": 1,
  "title": "mod.quest.arc1.name",
  "type": "SAGA",
  "parallel_objectives": false,
  "party_scaling": true,
  "requirements": {
    "operator": "AND",
    "conditions": [
      { "type": "LEVEL", "minLevel": 1 },
      { "type": "DIMENSION", "dimension": "minecraft:overworld" }
    ]
  },
  "objectives": [
    {
      "type": "KILL",
      "entity": "modid:story_enemy",
      "count": 1,
      "health": 450.0,
      "spawn": "QUEST",
      "count_mode": "QUEST_SPAWNED_ONLY"
    }
  ],
  "rewards": [
    { "type": "TPS", "amount": 2400 },
    { "type": "ITEM", "item": "modid:starter_item", "count": 1 }
  ]
}
```

### Exemplo de estrutura — sidequest

```json
{
  "id": "mentor_basic_training",
  "type": "SIDEQUEST",
  "quest_giver": "mentor",
  "turn_in": "mentor",
  "objectives": [
    { "type": "KILL", "entity": "minecraft:zombie", "count": 10, "spawn": "NATURAL", "count_mode": "ANY_MATCHING" },
    { "type": "TALK_TO", "npcId": "mentor" }
  ],
  "rewards": [{ "type": "TPS", "amount": 600 }]
}
```

### Manifesto de saga

```json
{
  "id": "first_arc",
  "name": "mod.saga.first_arc",
  "requirements": { "previousSaga": "" },
  "questFolder": "saga_first"
}
```

## Fluxo de funcionamento

1. **Boot:** `QuestRegistry.loadAll` gera/lê JSON, valida, indexa.
2. **Login:** client recebe registry + `PlayerQuestData` (via `StatsSyncS2C` / depois updates em `ProgressionSyncS2C`).
3. **Dificuldade (uma vez):** overlay da árvore → `SetStoryDifficultyC2S`. Só o líder da party. Marca `difficultyChosen=true`.
4. **Disponibilidade:** `QuestAvailabilityChecker` avalia saga anterior completa, quest anterior da lista em `SUCCESS`, `prerequisites`, flags de config (`storyModeEnabled` / `sideQuestsEnabled`), e se não é secret sem unlock.
5. **Start:** UI → `QuestActionC2S(START, questId, npcId)`.
   - Resolve controller (líder da party).
   - Se status é `FAILED`, **pula** re-check de availability e aceita de novo (`restartFailedQuest()` existe mas **nunca é chamado**).
   - Se já `ACCEPTED` ou `SUCCESS`, recusa (“already active”).
   - Dispara `QuestStartEvent` (cancelável).
   - `acceptQuest()` → `ACCEPTED`.
   - `initializeObjectiveRequirements(partySize)` grava requireds escalados.
   - Spawna inimigos de `KILL`+`QUEST`.
   - Seta tracked quest.
   - Toast + `ProgressionSyncS2C`.
6. **Progresso:**
   - Tick 20: ITEM (contagem no inv), STRUCTURE/BIOME/DIMENSION/COORDS, SKILL, timers TIME.
   - Morte de entidade: KILL (respeita spawn tag e count_mode); party inteira recebe crédito se tiver a quest aceita.
   - Interact: INTERACT / TALK_TO.
   - Cada incremento: `QuestObjectiveProgressEvent` → `setObjectiveProgress`.
   - Objetivo completo → toast `objectiveComplete`.
   - Sequencial (`parallel=false`): só o primeiro objetivo incompleto anda. **Exceção:** um bloco contíguo de KILLs compartilha o unlock (`isKillObjectiveUnlocked`).
   - Paralelo: todos andam.
   - Location em party: qualquer membro na zona atualiza todos.
7. **Completar:**
   - Sem `turn_in` e todos os objetivos ok → `checkAndComplete()` → `SUCCESS`, limpa tracked se era essa, toast, sync. **Não** dá reward.
   - Com `turn_in`: `isTurnInReady()` (tudo menos TALK_TO ok) → `QuestActionC2S(TURN_IN)` no NPC certo → completa o TALK_TO daquele NPC → `SUCCESS` → opcionalmente já reclama rewards do requester.
8. **Claim:** `ClaimQuestRewardC2S` ou `ClaimAllQuestRewardsC2S`.
   - Quest precisa ser `SUCCESS`.
   - Reward ainda não reclamada **por aquele jogador**.
   - Reward desbloqueada para a dificuldade dele.
   - `QuestRewardClaimEvent` → `giveReward(player, multiplier)` → `claimReward(index)`.
   - Party: cada um reclama o seu; merge **não** copia flags de claim.
9. **Próxima:** saga sequencial + prerequisites. Sidequest só prerequisites.

## Party e scaling

- Líder = controller (`PartyManager.resolveQuestController`).
- `syncPartyQuestState` faz `mergeForwardFrom` nos membros (status/progresso só sobem).
- Claims pessoais preservados.
- Se `party_scaling`:
  - Item required: `base * multiplier^extraMembers`, arredonda para cima num step (1 / 2 / 5 / 10 conforme o base).
  - Kill required: se `base<=1` não escala; senão `base * multiplier^(extra*0.75)`, ceil.
  - HP/dano do inimigo spawnado: `1 + extra * (perPlayer - 1)`.

## Edge cases

| Situação | Comportamento |
|---|---|
| Morte | `Clone` preserva capability. Se **todos** da party estão mortos/dying **e** a quest tem KILL → `failQuest()`: `FAILED`, progresso zerado, `failureCount++`, toast. Solo = wipe. |
| Restart após fail | `startQuest` aceita direto; progresso começa do zero (requireds re-inicializados). |
| Logout | NBT do jogador. Login reenvia registry + stats. |
| Repeatable | **Não existe.** `SUCCESS` não recomeça. Daily/Event sem reset. |
| Item objective | Só conta; não remove itens. |
| Story reset wish | Zera story, incrementa `storyResetCount`, decai só TP. |
| Config off | `storyModeEnabled` / `sideQuestsEnabled` escondem o tipo. |
| `NPC_ONLY` | Claim pela árvore bloqueado. |
| Reload | `/dmzreload quests` + re-sync. |

## Pontos de integração

| Sistema | Relação |
|---|---|
| Capability | Persistência e sync de `PlayerQuestData` |
| Evolução | Reward `TRANSFORMATION` e `SKILL`; pré-req `SKILL`/`RACE`; objetivo `SKILL` |
| Rede | Pacotes listados em `06` |
| UI | Árvore, HUD, diálogo, toasts (`08`) |
| Eventos Forge | `QuestEvents` + `DMZEvent` (`09`) |
| NPCs | Giver / turn-in por string id |

## Decisões de design observadas

- JSON no mundo em vez de datapack: admin edita live; o preço é um parser próprio e um upgrader.
- Reward manual: evita perder item se o inventário estiver cheio no instante do kill, e permite party com loot individual.
- Merge só-para-frente: impede grief de “descompletar” a story do amigo.
- Inferência: Daily/Event foram previstos e abandonados — o Bleach não deve assumir dailies sem implementar reset.
- Inferência: `restartFailedQuest()` morto sugere que fail→start foi um atalho tardio.

## Riscos/complexidades para reimplementar

- Lógica de kill (spawn tag vs any, bloco contíguo, party credit, fail em wipe) é a parte mais fácil de errar — por isso `KillObjective` e `QuestEvents` estão em `/reference-code`.
- Availability (saga + sequência + AND/OR aninhado + TIME anchors) é a segunda — `QuestAvailabilityChecker` e `QuestParser` preservados.
- Party merge + claims pessoais — `PartyManager` e `PlayerQuestData.QuestProgress.mergeForwardFrom`.
- Schema JSON tem muitas chaves alternativas (`formGroup`/`form_group`/`group`). Se o Bleach for data-driven, **fixar um schema estreito** e não portar todos os aliases, exceto se quiser compat com ferramentas do original.
- Não portar `DRAGON_SUMMON` nem rewards de técnica/wish.
- Conteúdo: reescrever sagas Shinigami do zero; só reusar o schema.

## Fluxo numerado (do registro à recompensa)

1. Server start carrega/gera JSON.
2. Player login sincroniza definições + progresso.
3. Player escolhe dificuldade (uma vez).
4. Checker libera o nó da árvore (saga anterior + quest anterior + prerequisites).
5. Player (ou líder) dá Start; server valida start requirements.
6. Evento cancelável; status ACCEPTED; requireds escalados; spawns de kill.
7. Eventos/tick incrementam objetivos (seq ou paralelo).
8. Completa auto ou via turn-in.
9. Player reclama cada reward (ou claim-all).
10. Próximo nó da saga torna-se sequencialmente alcançável.
