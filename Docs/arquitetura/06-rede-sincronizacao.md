# Rede e sincronização

## Atualização Bleach — 10/09/2026

O Bleach usa protocolo 2.1. ClaimQuestRewardC2S aceita índice -1 para lote; limita o ID a 256 caracteres e exige jogador vivo, personagem criado e intervalo mínimo de quatro ticks. SelectFormC2S limita IDs a 32 caracteres. Cliente e servidor devem ser atualizados juntos.

[Relatório atual](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções de Dragon Mine Z abaixo são referência do original.


## Resumo

Um único `SimpleChannel` (`dragonminez:network`, protocolo `"1.0"`) com IDs sequenciais. A ordem de `NetworkHandler.register()` **é** o protocolo: só se acrescenta no fim. Quest e evolução não têm canal próprio. O client aplica NBT com merge parcial (`StatsData.load`): chaves ausentes no packet não são tocadas. Não existe um router central — cada caller escolhe o packet pelo domínio.

## Modelo de dados

### Canal

| Item | Valor |
|---|---|
| Channel | `ResourceLocation("dragonminez", "network")` |
| Protocol | `"1.0"` |
| Helpers | `sendToServer`, `sendToPlayer`, `sendToAllPlayers`, `sendToTrackingEntityAndSelf`, `sendToTrackingEntity` |
| Extra | `PacketRateLimiter`, `CompressionUtil` |

### Estratégia de sync (visão)

| Packet | Subset NBT | Quando |
|---|---|---|
| `StatsSyncS2C` | `StatsData.save()` completo | Login, dimensão, form/skill/combat, **a cada 10 ticks** para self + tracking |
| `ResourceSyncS2C` | `Resources` + `Status` | Respawn, ganho pontual de TP/energia |
| `ProgressionSyncS2C` | `Stats`, `BonusStats`, `Skills`, `Techniques`, `PlayerQuestData` | Progresso de quest, claim, dificuldade, saga, party |
| `AppearanceSyncS2C` | `Character` | Mastery / aparência |
| `SyncQuestRegistryS2C` | JSON de sagas + quests (Utf 1 MB cada) | Login e reload de registry |

`StatsCapability` só dispara sync em lifecycle (login / respawn / dimensão). O resto é pontual nos services + o tick periódico.

## Packets C2S — quest e evolução

| Packet | Payload | Quando o client envia | O que o server faz | Sync de volta |
|---|---|---|---|---|
| `QuestActionC2S` | enum `START`/`RESUMMON`/`TURN_IN`, `questId` Utf, `npcId` Utf | Botões da árvore / diálogo | `QuestService.start/resummon/turnIn` | Feedback S2C em falha; sync via service em sucesso |
| `ClaimQuestRewardC2S` | `questId` Utf | Claim de uma quest | `claimRewards` | `ProgressionSyncS2C` |
| `ClaimAllQuestRewardsC2S` | vazio | Claim all | `claimAllRewards` | `ProgressionSyncS2C` |
| `UnlockSagaC2S` | `sagaId` Utf | Aba de saga trancada | `setSagaUnlocked` se ainda locked | `ProgressionSyncS2C` |
| `SetTrackedQuestC2S` | `questId` Utf (blank = limpar) | Track/untrack | Valida quest aceita e não completa | `ProgressionSyncS2C` só para o self |
| `SetStoryDifficultyC2S` | `Difficulty` ordinal VarInt | Overlay (uma vez) | Só líder; seta difficulty + chosen | Party sync → `ProgressionSyncS2C` |
| `SelectFormC2S` | `group` Utf, `form` Utf, `stack` bool | Clique no radial | Seta selected + ActionMode | `StatsSyncS2C` |
| `UpdateStatC2S` | enum `StatAction`, `value` bool | Keybinds | `ACTION_CHARGE` liga/desliga charge de form; outros: ki charge, descend, block | Sem sync imediato; tick + periódico |
| `ExecuteActionC2S` | enum `ActionType`, `rightClick` bool | Double-tap / tecla | `INSTANT_TRANSFORM`, `FORCE_DESCEND`, `INSTANT_RELEASE` | `StatsSyncS2C` se `needsSync` |
| `UpdateSkillC2S` | skill + ação TOGGLE/UPGRADE/PURCHASE | Menu de skills | Gasta TP / toggle | `StatsSyncS2C` |

Party packets (`CreatePartyC2S` … `LeavePartyC2S`) existem e afetam quest state; documentar só se o Bleach quiser party no MVP. Recomendação: **omitir party no MVP**.

## Packets S2C — quest e evolução

| Packet | Payload | Quando | Efeito no client |
|---|---|---|---|
| `StatsSyncS2C` | `playerId` int + NBT completo | Ver tabela de estratégia | Merge em `StatsData.load` |
| `ResourceSyncS2C` | `playerId` + NBT Resources/Status | Respawn, recursos | Merge parcial |
| `ProgressionSyncS2C` | `playerId` + NBT progressão | Quests/skills | Merge parcial (preserva forms se Character não veio) |
| `AppearanceSyncS2C` | `playerId` + NBT Character | Mastery / visual | Merge Character |
| `SyncQuestRegistryS2C` | `sagasJson`, `questsJson` | Login, `/dmzreload quests` | Reconstrói `QuestRegistry` client |
| `StoryToastS2C` | enum evento, `questId`, `objectiveIndex`, progress, required | Start/fail/complete/objective | Toast; **não** altera dados |
| `OpenQuestNPCDialogueS2C` | `npcId`, 3 listas de quest ids, `masterNpc`, `entityId` | Right-click NPC | Abre tela de diálogo |
| `QuestActionFeedbackS2C` | `Component` | Ação recusada | Mensagem de erro |

## Fluxo de funcionamento

1. `NetworkHandler.register()` registra C2S 0–53 e S2C 54–79 (números do 2.1.3).
2. Login: configs → registry de quests → `StatsSyncS2C`.
3. Client guarda definições em `QuestRegistry` local e progresso na capability local.
4. UI nunca “pede” dados: lê a cap local e manda C2S para mutar.
5. Server muta a cap do jogador (e da party) e escolhe o S2C de menor subset que cubra a mudança.
6. A cada 10 ticks o server manda `StatsSyncS2C` para self + quem trackeia o player (combate/forms visíveis).
7. Client `ClientPacketHandler.handleStatsSyncPacket` → `load(nbt)` parcial.

## Pontos de integração

| Sistema | Packets |
|---|---|
| Quests | Action, claim, track, difficulty, unlock saga, registry, toast, dialogue, feedback, progression |
| Evolução | SelectForm, UpdateStat(ACTION_CHARGE), ExecuteAction(transform/descend), StatsSync, AppearanceSync |
| Skills | UpdateSkillC2S, StatsSync / ProgressionSync |

## Decisões de design observadas

- Um channel só: simples, mas o protocolo é frágil (inserir packet no meio quebra clientes velhos).
- Quatro flavors de sync em vez de um delta-field: fácil de implementar, fácil de enviar demais (`StatsSync` a cada 10 ticks é pesado).
- Inferência: `ProgressionSync` foi criado quando perceberam que syncar o blob inteiro a cada kill de quest era waste — mas o tick ainda manda o blob.
- Registry em JSON no fio (1 MB) em vez de um codec binário: combina com o design “JSON no mundo”.

## Riscos/complexidades para reimplementar

- Recriar os 80 packets é desnecessário. O Bleach deve nascer com um channel **novo** e uma lista curta (tabela C2S/S2C acima, ~17 packets).
- Merge parcial é obrigatório: um `load` total em `ResourceSync` apaga quests no client.
- `SyncQuestRegistryS2C` com 200+ quests DB é grande; o MVP Shinigami terá poucas — Utf simples basta.
- Periodic full sync a cada 10 ticks: para o MVP, sync-on-change + sync-on-login é suficiente e mais barato.
- Nunca reusar os IDs numéricos do original (são do protocolo `dragonminez:network`).
