# Persistência NBT

## Estado da implementação Bleach — 10/09/2026

Schema 2 persiste categorias e formas descobertas. Saves antigos inferem descobertas por skill/mastery. Quests sem assinatura vinculam-se à definição carregada; não há detecção retroativa de mudanças anteriores. JSONs existentes conservam recompensas antigas.

Consulte o [manual atual](../jogador/manual-do-jogador.md) e o [relatório de implementação](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções seguintes preservam a referência do Dragon Mine Z e não devem ser interpretadas como funcionalidades já entregues no Bleach.


## Resumo

Quests e evolução salvam no **NBT do jogador**, dentro da capability `StatsData`. Não há SavedData de mundo para progresso individual. O original tem backends extras (JSON de arquivo e MariaDB via `StorageManager`) que, se ativos, **sobrescrevem** o NBT no login; o MVP Bleach deve usar só o caminho vanilla. Party também tem um `PartySavedData` de mundo — omitir se não houver party.

## Modelo de dados

### Onde lê/escreve

| Momento | O quê |
|---|---|
| Save do jogador | `StatsProvider.serializeNBT()` → `StatsData.save()` |
| Load do jogador | `deserializeNBT` → `StatsData.load()` |
| Clone (morte) | `copyFrom` (serialize/deserialize interno de `PlayerQuestData`) |
| Login com StorageManager | Load async externo → `stats.load(loaded)` → `StatsSyncS2C` |
| Autosave 5 min / logout | `StorageManager.savePlayer` se backend ≠ NBT |
| Mundo | `PartySavedData` (party); `DragonBallSavedData` (fora de escopo) |

`PlayerQuestData` **ausente** no load lança exceção — o campo é obrigatório.

### Raiz `StatsData.save()`

Compostos irmãos (nomes exatos):

`Stats`, `Status`, `Cooldowns`, `Character`, `Resources`, `Skills`, `Effects`, `SecondaryStatEffects`, `PlayerQuestData`, `BonusStats`, `Techniques`, `DynamicGrowth`, `HasInitializedHealth` (boolean).

`load` é parcial: `if (nbt.contains(key))`.

### `PlayerQuestData`

```
PlayerQuestData
├── difficulty                 String   EASY|NORMAL|HARD
├── difficultyChosen           bool
├── storyResetCount            int
├── questState                 (ver serializeFullQuestState)
│   ├── quests                 List<Compound>
│   │     ├── questId          String
│   │     ├── status           String  NOT_STARTED|ACCEPTED|FAILED|SUCCESS
│   │     ├── objectives       Compound  "0"→int, "1"→int…
│   │     ├── objectiveRequirements  Compound  "0"→int…
│   │     ├── failureCount     int
│   │     ├── difficulty       String
│   │     └── rewards          Compound  "0"→bool…
│   ├── sagaUnlocks            Compound  sagaId→bool
│   ├── startRequirementTimings Compound  questKey→{gameTimeStarted, realTimeStartedMs}
│   ├── trackedQuestId         String opcional
│   └── hostileNpcKeys         List<String> opcional
└── partyState                 Compound opcional
    ├── partyId, leaderId      UUID string
    ├── pvpEnabled             bool
    ├── members                List<UUID string>
    └── pendingInvite          Compound (inviterUUID, partyId, partyLeaderId, inviterName, expiresAtMs, partyDifficulty)
```

Legacy no load: layouts antigos `difficultyStates` e `hardModeEnabled` são migrados. O Bleach **não** precisa dessa migração (save novo).

### `Character` (raça / forms)

```
Character
├── Race, Gender, Class                    String
├── SelectedFormGroup, SelectedForm        String
├── CurrentFormGroup, CurrentForm          String   (active)
├── FormMasteries                          Compound  "group:form"→double
├── FormsUsedBefore                        Compound  group→"form1:form2:…"
├── SelectedStackFormGroup, SelectedStackForm
├── CurrentStackFormGroup, CurrentStackForm
├── StackFormMasteries, StackFormsUsedBefore
├── PreviousFormGroup, PreviousForm, HasPreviousFormRecord
├── PreviousStack* equivalentes
├── HasSaiyanTail                          bool
├── ActiveFormItemDurationTicks            int
├── ActiveStackFormItemDurationTicks       int
└── (aparência: HairId, cores, CustomHair, InteractedMasters, KnownMinigames…)
```

Legacy: `Race` numérico antigo → string via `ConfigManager.getLoadedRaces()`. Não portar.

Dados **fora** da capability (player persistent data):

- `dmz_form_duration_item_seconds` / `dmz_stack_form_duration_item_seconds`
- `dmzTransformMobEffects` — diff de efeitos da form

Se o Bleach não tiver item de duração, não criar essas keys.

### `Resources`

```
Resources
├── CurrentEnergy, CurrentStamina, CurrentPoise   float (legacy int aceito)
├── Release                                        int   powerRelease
├── ReleaseLimit                                   int
├── FormRelease                                    int   actionCharge 0–100
├── Alignment                                      int
├── TrainingPointsF                                float
├── PendingAttributePoints                         int
└── ZenkaiCount                                    int   racialSkillCount
```

### `Skills`

```
Skills
└── SkillsList   List<Compound>
      ├── Name       String
      ├── Level      int
      ├── IsActive   bool
      └── MaxLevel   int
```

### `Status` (flags que a evolução toca)

Relevantes: `Transforming` (= `isActionCharging`), `SelectedAction` (ActionMode), `hasCreatedCharacter`, e dezenas de flags de combate/flight que o Bleach pode omitir.

## Fluxo de funcionamento

1. Jogador sai / autosave vanilla → provider grava o composto.
2. Jogador entra → provider lê; `load` recompõe sub-objetos.
3. `updateTransformationSkillLimits(race)` após Character load.
4. Se StorageManager ativo, um load async **substitui** o NBT recém-lido.
5. Morte: clone copia o blob; respawn manda só Resources/Status.

## Pontos de integração

| Sistema | Keys |
|---|---|
| Quests | `PlayerQuestData` |
| Evolução | `Character` + `Resources.FormRelease` + `Status.Transforming` + `Skills` |
| Rede | Os mesmos compostos, recortados por packet |

## Decisões de design observadas

- Um blob só no player.dat: simples de clonar e de mandar pela rede.
- Chaves PascalCase misturadas com camelCase (`PlayerQuestData` vs `difficultyChosen`) — copiar os nomes se o Bleach for ler saves do original (não será); no fork, **padronizar camelCase**.
- Inferência: StorageManager existe para servers grandes (MariaDB). Para um modpack fan-made, NBT é o correto.

## Riscos/complexidades para reimplementar

- Partial load é o contrato. Testar: packet só com `Resources` não pode zerar quests.
- Listas de objetivo indexadas por string `"0"`, `"1"` — não usar ListTag de ints sem mapa, porque requireds escalados e claims precisam do mesmo índice.
- `copyFrom` de quest via serialize/deserialize (não field-copy) — garante deep copy; replicar.
- Não persistir `Quest.completed` / `currentObjectiveIndex` da definição: progresso é só no player.
- Prefixar persistent-data keys com o id do Bleach, nunca `dmz_`.
