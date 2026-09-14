# Eventos Forge

## Atualização Bleach — 10/09/2026

Quatro GameTests exercitam clone após invalidação, resgates sem duplicação, permissões e reload. O clone preservou pontos/mastery e cancelou carga. Isso não substitui teste com dois clientes reais.

[Relatório atual](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções de Dragon Mine Z abaixo são referência do original.


## Resumo

Quests avançam por quatro hooks de gameplay (tick, morte, interact, summon de dragão). Evolução avança por tick (charge, drain, mastery passiva) e por hits (mastery). A capability se anexa e se clona por eventos de lifecycle do player. Eventos **custom** (`DMZEvent`) encapsulam os pontos de extensão canceláveis. Abaixo só o que toca quest/evolução — o original tem dezenas de outros handlers de combate.

## Modelo de dados

Não há “modelo” além do par `(Event, Handler)`. Handlers relevantes:

### Capability / persistência

| Classe | Evento | O que faz |
|---|---|---|
| `StatsCapability` | `RegisterCapabilitiesEvent` | Registra `StatsData` |
| `StatsCapability` | `AttachCapabilitiesEvent<Entity>` | Anexa `StatsProvider` a `Player` |
| `StatsCapability` | `PlayerEvent.Clone` | `copyFrom` + grace de force-kill; cache client se personagem criado |
| `StatsCapability` | `PlayerEvent.PlayerLoggedInEvent` | Sync configs + quest registry + `StatsSyncS2C` |
| `StatsCapability` | `TickEvent.PlayerTickEvent` | `StatsData.tick()` (só cooldowns) |
| `StatsCapability` | `PlayerEvent.PlayerRespawnEvent` | Refill energia/stamina, `ResourceSyncS2C` |
| `StatsCapability` | `PlayerEvent.PlayerChangedDimensionEvent` | Reset ki sense, `StatsSyncS2C` |
| `ForgeCommonEvents` | `ServerStartingEvent` | `StorageManager.init`, `QuestRegistry.loadAll`, permissions |
| `StorageManager` | login hook | Load async externo (não usar no MVP) |

### Quests

| Classe | Evento | O que faz |
|---|---|---|
| `QuestEvents` | `TickEvent.PlayerTickEvent` | A cada 20 ticks: location/item/skill + timers de start requirement |
| `QuestEvents` | `LivingDeathEvent` | Crédito de KILL; fail se wipe de party em quest com kill |
| `QuestEvents` | `PlayerInteractEvent.EntityInteract` | INTERACT e TALK_TO |
| `QuestEvents` | `DMZEvent.DragonSummonedEvent` | Objetivo DRAGON_SUMMON — **não portar** |
| `PartyManager` | `PlayerLoggedInEvent` | Entra no scoreboard team da party |
| `PartyManager` | `PlayerLoggedOutEvent` | Limpa invite; transfere liderança |
| `ForgeClientEvents` | `ClientPlayerNetworkEvent.Clone` | Limpa cooldown de resummon no client |

### Evolução / skills / recursos

| Classe | Evento | O que faz |
|---|---|---|
| `TickHandler` | `TickEvent.PlayerTickEvent` | Regen, charge de form, charge de technique, drains, mastery passiva 5 s, `StatsSyncS2C` a cada 10 ticks |
| `TickHandler` | `PlayerLoggedInEvent` | Zera charge/transform no join (evita stuck charging) |
| `FormModeHandler` | chamado pelo TickHandler, não é `@SubscribeEvent` | Executa transform ao charge=100 |
| `TransformStatusHandler` | tick | Detecta mudança de form, aplica efeitos, dispara `FormChangeEvent` |
| `StatsEvents` | hits (LivingHurt / equivalente) | Mastery por hit dado/tomado |
| `ForgeCommonEvents` | `LivingDeathEvent` | Limpa forms/stacks ativas, desliga kaioken |
| `ClientStatsEvents` | `TickEvent.ClientTickEvent` | Envia `UpdateStatC2S` (ki/form charge, block, instant) |
| `ClientStatsEvents` | `InputEvent.MouseScrollingEvent` | Bloqueia scroll durante charge de technique |

### UI

| Classe | Evento | O que faz |
|---|---|---|
| `ModClientEvents` | `RegisterGuiOverlaysEvent` | Registra `TrackedQuestHUD` |
| `ForgeClientEvents` | `InputEvent.Key` | Abre menu de stats ou race select |
| `ForgeClientEvents` | `TickEvent.ClientTickEvent` | Força tela de criação se `!hasCreatedCharacter` |
| `ForgeClientEvents` | `ClientPlayerNetworkEvent.LoggingIn` | Cache de `hasCreatedCharacter` |

### Custom canceláveis (`DMZEvent`)

| Evento | Quem dispara | Cancelável | Efeito se cancelado |
|---|---|---|---|
| `QuestStartEvent` | `QuestService.startQuest` | sim | não aceita |
| `QuestObjectiveProgressEvent` | `QuestEvents` | sim | não incrementa |
| `QuestFailEvent` | `QuestEvents` (wipe) | sim | não falha |
| `QuestTurnInEvent` | `QuestService.turnInQuest` | sim | não entrega |
| `QuestCompletedEvent` | `checkAndComplete` / turn-in | sim | não marca SUCCESS |
| `QuestRewardClaimEvent` | `claimAvailableRewards` | sim | não entrega reward |
| `FormChangeEvent` / `StackFormChangeEvent` | `TransformStatusHandler` | típico informativo | — |

## Fluxo de funcionamento

1. MOD bus: registries + `RegisterCapabilities`.
2. Forge bus: attach em todo player.
3. Server starting: carrega quests.
4. Login: sync.
5. Tick 20: objetivos “quentes” (item/local/skill).
6. Eventos de mundo (kill/interact) incrementam o resto.
7. Tick 1: charge/drain de form + sync periódico.
8. Morte: clone da cap; possível fail de quest; limpa form ativa.

## Pontos de integração

Os handlers leem/escrevem a mesma `StatsData`. Eventos custom são o ponto para o conteúdo Bleach (ex. um datapack futuro ou um NPC script) sem editar `QuestEvents`.

## Decisões de design observadas

- Tick 20 para objetivos de inventário/local: barato o suficiente, 1 s de atraso aceitável.
- Kill e death compartilham `LivingDeathEvent`: a ordem do handler importa (crédito antes do fail, ou o contrário). Conferir no original ao portar — `QuestEvents` está em `/reference-code` por isso.
- Inferência: `TickHandler` cresceu até virar um “god tick”. No Bleach, separar `QuestTick` de `FormTick`.

## Riscos/complexidades para reimplementar

- Registrar handlers no bus errado (MOD vs Forge) é o erro nº 1 de Forge.
- `PlayerTickEvent` precisa filtrar fase (`END`) e lado (`!level.isClientSide`) — o original faz isso; copiar o filtro, não o resto.
- Wipe de party no mesmo `LivingDeathEvent` do kill: race sutil se o último hit mata o player e o mob no mesmo tick.
- Client tick forçando GUI compete com vanilla pause/death screen — testar.
- Não assinar `DragonSummonedEvent`.

## Implementação Bleach — combate

`LivingHurtEvent` separa três caminhos: Zanpakutō/Asauchi usa Zanjutsu e bônus da forma; mão principal vazia usa Hakuda; jogador atingido por golpe físico direto usa Resistência. Vitalidade é um modifier de `MAX_HEALTH` reaplicado na normalização do jogador. Danos ambientais, mágicos e de projéteis não passam pela mitigação de Resistência.

## Bleach 0.3.0 — ciclo de vida das viagens

TravelEvents importa o mapa em ServerAboutToStartEvent, antes da abertura dos níveis. ServerStartingEvent carrega configurações. ServerTickEvent END conclui preparação de chunks e aplica teleporte na thread do servidor. Logout, morte e mudança externa de dimensão cancelam pendências; ServerStoppedEvent libera tickets e remove estado por servidor. EntityTravelToDimensionEvent impede portais externos de entrar/sair da Soul Society; PlayerSetSpawnEvent impede respawn nessa dimensão.
