# Sistema de Capability

## Estado da implementação Bleach — 10/09/2026

PlayerData inclui AttributeData e schemaVersion 2. A clonagem recupera dados serializados e redefine estados transitórios; invalidação do provider e sincronização de respawn foram tratadas. O ciclo completo Forge ainda exige teste em jogo.

Consulte o [manual atual](../jogador/manual-do-jogador.md) e o [relatório de implementação](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções seguintes preservam a referência do Dragon Mine Z e não devem ser interpretadas como funcionalidades já entregues no Bleach.


## Resumo

Todo o estado persistente do jogador — raça, forms, energia, skills, quests — vive numa única Forge Capability: `StatsCapability` → `StatsData`, anexada a toda entidade `Player`. Não há capability isolada de quest ou evolução. O provider serializa o agregado inteiro em NBT; a rede reusa o mesmo `save()`/`load()` com merge parcial (só as chaves presentes no packet sobrescrevem).

## Modelo de dados

### Registro e anexo

| Item | Valor |
|---|---|
| Token | `StatsCapability.INSTANCE` → `Capability<StatsData>` |
| Registro | `RegisterCapabilitiesEvent` → `event.register(StatsData.class)` |
| Anexo | `AttachCapabilitiesEvent<Entity>` se o objeto é `Player` e ainda não tem a cap |
| Provider | `StatsProvider` implementa `ICapabilityProvider` + `INBTSerializable<CompoundTag>` |
| ID do anexo | `StatsProvider.ID` (ResourceLocation do mod) |
| Cache client | `StatsCapability.CLIENT_CACHE` — usado no clone client-side da tela de criação |

### Agregado `StatsData`

`StatsData` é um facade: constrói 12 sub-objetos e um par de flags. Construtor recebe o `Player` e injeta referências cruzadas (`stats.setPlayer`, `resources.setStatsData`).

| Campo | Tipo | Propósito | Padrão |
|---|---|---|---|
| `stats` | `Stats` | Pontos STR/SKP/RES/VIT/PWR/ENE | valores iniciais da raça/classe |
| `status` | `Status` | Flags de gameplay (personagem criado, charging, flight, stun…) | tudo false / vazio |
| `cooldowns` | `Cooldowns` | `Map<String,Integer>` timers em ticks | vazio |
| `character` | `Character` | Raça, classe, forms ativas/selecionadas, mastery, aparência | strings vazias |
| `resources` | `Resources` | Energia, stamina, poise, release, TP, alignment | 0 / caps derivados |
| `skills` | `Skills` | `Map<String,Skill>` | vazio até race defaults |
| `effects` | `Effects` | Efeitos temporários nomeados | vazio |
| `secondaryStatEffects` | `SecondaryStatEffects` | Modificadores temporários de stat | vazio |
| `playerQuestData` | `PlayerQuestData` | Progresso de quests, sagas, party, dificuldade | difficulty NORMAL |
| `bonusStats` | `BonusStats` | Bônus por stat (equip/treino) | vazio |
| `techniques` | `Techniques` | Técnicas de combate (8 slots) | vazio — fora do MVP |
| `dynamicGrowth` | `DynamicGrowthData` | XP de prática por stat | vazio — fora do MVP |
| `hasInitializedHealth` | `boolean` | Evita reaplicar modifier de HP | false |
| `isDataLoaded` | `boolean` | Gate pós-`load()` | false |

### Sub-objeto `Character` (evolução)

| Campo | Tipo | Propósito | Padrão |
|---|---|---|---|
| `race` | `String` | Id da raça (`human`, `saiyan`…) | vazio até criação |
| `characterClass` | `String` | Classe (`warrior`…) — altera stats base | vazio |
| `selectedFormGroup` / `selectedForm` | `String` | Alvo da charge / radial | vazio |
| `activeFormGroup` / `activeForm` | `String` | Form atualmente transformada | vazio / `"base"` |
| `formMasteries` | `FormMasteries` | `Map<"group:form", Double>` | vazio |
| `formsUsedBefore` | `UsedForms` | Primeiro uso por grupo | vazio |
| `selectedStackFormGroup` / `selectedStackForm` | `String` | Stack selecionado (kaioken…) | vazio |
| `activeStackFormGroup` / `activeStackForm` | `String` | Stack ativo | vazio |
| `stackFormMasteries` | `FormMasteries` | Mastery de stack | vazio |
| `previousForm*` / `hasPreviousFormRecord` | snapshot | Memória para descend | vazio / false |
| `hasSaiyanTail` | `boolean` | Gate de forms oozaru | false |
| `activeFormItemDurationTicks` | `int` | Timer de item de duração | 0 |

### Sub-objeto `Resources` (energia / reiatsu)

| Campo | Tipo | Propósito | NBT |
|---|---|---|---|
| `currentEnergy` | `float` | Energia atual (ki) | `CurrentEnergy` |
| `currentStamina` | `float` | Stamina | `CurrentStamina` |
| `currentPoise` | `float` | Poise | `CurrentPoise` |
| `powerRelease` | `int` | % de poder liberado (0–100+) | `Release` |
| `releaseLimit` | `int` | Teto de release | `ReleaseLimit` |
| `actionCharge` | `int` | Barra 0–100 da transformação | `FormRelease` |
| `alignment` | `int` | Alinhamento moral | `Alignment` |
| `trainingPoints` | `float` | TP para comprar stats/skills | `TrainingPointsF` |
| `pendingAttributePoints` | `int` | Pontos aguardando alocação | `PendingAttributePoints` |
| `racialSkillCount` | `int` | Contador racial (zenkai no original) | `ZenkaiCount` |

### Sub-objeto `PlayerQuestData` (quests)

Ver detalhe completo em `03-sistema-quests.md` e `07-persistencia-nbt.md`. Campos-chave:

| Campo | Tipo | Propósito | Padrão |
|---|---|---|---|
| `quests` | `Map<String, QuestProgress>` | Progresso por quest key | vazio |
| `sagaUnlockState` | `Map<String, Boolean>` | Sagas liberadas | vazio |
| `startRequirementTimings` | `Map<String, timing>` | Âncora de requisitos TIME | vazio |
| `trackedQuestId` | `String` | Quest do HUD | null |
| `difficulty` | `Difficulty` | EASY / NORMAL / HARD | NORMAL |
| `difficultyChosen` | `boolean` | Overlay de escolha já feito | false |
| `storyResetCount` | `int` | Decai TP de recompensa | 0 |
| `activePartyId` / `partyLeaderId` | `UUID` | Party de story | null |

### `Skill`

| Campo | Tipo | Propósito |
|---|---|---|
| `name` | `String` | Id canônico |
| `level` | `int` | Nível atual (clamp em max) |
| `isActive` | `boolean` | Toggle (voo, ki sense…) |
| `maxLevel` | `int` | Cap vindo da config |

## Fluxo de funcionamento

1. Jogador entra no mundo → Forge dispara `AttachCapabilitiesEvent` → `StatsProvider` é anexado.
2. Se o save já tem NBT da capability, `deserializeNBT` → `StatsData.load()`.
3. `PlayerQuestData` é **obrigatório** no NBT: ausência lança `ClassNotFoundException` com mensagem pedindo regenerate.
4. Após load, se `race` não está vazio, `updateTransformationSkillLimits(race)` registra skills de form com o max da config da raça.
5. Login: `StatsCapability` envia configs do servidor, registry de quests e `StatsSyncS2C` (NBT completo).
6. A cada tick do jogador, `StatsData.tick()` só decremento de `Cooldowns`. Regen/charge/drain ficam em `TickHandler`.
7. Morte: `PlayerEvent.Clone` → `copyFrom` (inclui quests e forms). `reviveCaps`/`invalidateCaps` no original.
8. Respawn: `ResourceSyncS2C` (energia/stamina refill).
9. Troca de dimensão: `StatsSyncS2C` completo.
10. Logout: NBT vanilla do jogador + (se ativo) `StorageManager.savePlayer`.

## Pontos de integração

| Sistema | Lê | Escreve |
|---|---|---|
| Quests | `playerQuestData`, às vezes `skills` / `character.race` (pré-requisitos) | progresso, status, claims |
| Evolução | `character`, `skills` (nível de form-skill), `resources` (custo) | form ativa, mastery, drains |
| Rede | `save()` completo ou recortes | `load()` parcial no client |
| HUD | capability local do client | nenhum (só packets C2S) |
| Rewards de quest | `resources` (TP), `skills`, `character` (form mastery) | mesmos |

## Getters/setters relevantes e quem chama

| API | Quem chama |
|---|---|
| `StatsProvider.get(INSTANCE, player)` | Quase todos os handlers server/client |
| `character.setActiveForm(group, name)` | `FormModeHandler` |
| `character.getFormMasteries().addMastery(...)` | `TickHandler` (passivo), `StatsEvents` (hit), `TransformationReward` |
| `resources.addTrainingPoints` / `removeEnergy` | Rewards, transform, combate |
| `skills.setSkillLevel` / `isUnlockedAtLevel` | `UpdateSkillC2S`, `SkillReward`, `TransformationsHelper` |
| `playerQuestData.acceptQuest` / `completeQuest` / `setObjectiveProgress` | `QuestService`, `QuestEvents` |
| `getFormMultiplier(stat)` / `getAdjustedEnergyDrain()` | Combate e `TickHandler` |
| `getLevel()` | Pré-requisito LEVEL de quest — nível derivado da soma de stats, não um campo |

## Serialização NBT (visão)

`StatsData.save()` grava compostos irmãos:

`Stats`, `Status`, `Cooldowns`, `Character`, `Resources`, `Skills`, `Effects`, `SecondaryStatEffects`, `PlayerQuestData`, `BonusStats`, `Techniques`, `DynamicGrowth`, `HasInitializedHealth`.

`load()` é **parcial**: só sobrescreve chaves presentes. Isso é o contrato da rede (ver `06`).

Árvores exatas de `PlayerQuestData`, `Character`, `Resources` e `Skills` estão em `07-persistencia-nbt.md`.

## Decisões de design observadas

- Uma capability só: evita desync entre “quest cap” e “stats cap” e simplifica clone/login.
- Merge parcial no `load()` permite packets baratos (`ResourceSyncS2C`) sem invalidar quests.
- Nível do personagem é **derivado** (`getLevel()`), não persistido — pré-requisitos de quest LEVEL reavaliam na hora.
- Inferência: o original cresceu de um “player stats blob” e foi empilhando sub-objetos; o Bleach pode começar com um `PlayerData` menor (raça, estágio, reiatsu, quests, skills) e o mesmo padrão de provider.

## Riscos/complexidades para reimplementar

- Esquecer o merge parcial e fazer `load` total em todo packet apaga progresso de quest no client.
- `PlayerQuestData` obrigatório no load: um save antigo sem a chave quebra o login.
- `copyFrom` no clone precisa copiar quests **e** forms; senão morte reseta Shikai/Bankai ou missões.
- `getLevel()` derivado: se o Bleach quiser nível explícito (mais natural para Shinigami), documentar a mudança — o checker de quests do original assume a fórmula de stats.
- Storage JSON/MariaDB é um segundo caminho de persistência que **sobrescreve** o NBT no login. Não portar no MVP.

## Implementação Bleach — atributos e derivados

`PlayerData` agrega `AttributeData` com sete ranks sem teto de gameplay: `zanjutsu`, `hakuda`, `vitality`, `resistance`, `kidou`, `reserve` e `control`. `refreshDerivedResources()` recalcula reiatsu máxima a partir de Reserva; `ProgressionService` reaplica um modifier transitório de vida máxima com UUID fixo para Vitalidade, evitando acumulação em login/respawn/compra. BP é derivado, não persistido: soma dos sete ranks × reiatsu máxima/10.

## Bleach 0.3.0 — TravelData

A capability PlayerData existente agrega TravelData: retorno e cooldown persistentes. Nenhuma capability nova. Clone copia os dados; resetTransientState não apaga cooldown/retorno. Load de sync parcial sem travel preserva o estado; full save antigo (com schemaVersion) inicia sem travel.

PlayerProvider renova o LazyOptional quando a entidade volta a permitir consulta depois de reviveCaps. O PlayerData é preservado; handles antigos continuam inválidos. GameTest cobre invalidação, bloqueio antes de revive e preservação de progressão depois.
