# Sistema de Evolução / Progressão

## Resumo

Não existe um enum global `EvolutionStage`. A progressão é **data-driven em três camadas**: raça permanente → grupo de forms (árvore) → form (estágio ordenado). Uma camada ortogonal, *stack form*, empilha um multiplicador (Kaioken etc.). Unlock mistura **skill comprada com TP** + **mastery** na form anterior. A transição em si é uma **ação do jogador** (segurar charge até 100, ou instant se mastery alta), não um tick que “evolui sozinho”. Quests podem pular etapas via `TransformationReward`.

Para o Bleach, o mapeamento natural é: raça = Shinigami; grupo = `zanpakuto`; forms ordenadas = `sealed` → `shikai` → `bankai`. Hollow/Quincy entram como outras raças/grupos nas fases seguintes.

## Modelo de dados

### Form (estágio) — `FormConfig.FormData`

JSON desserializado; a ordem no `LinkedHashMap` do grupo **é** a ordem de step-up/down.

| Campo | Tipo | Papel |
|---|---|---|
| `name` | `String` | Id do estágio |
| `unlockOnSkillLevel` | `Integer` | Nível mínimo da form-skill (0-based no array de custos TP) |
| `formRequisite` | `String` | CSV `"grupo.form"` de mastery prévia |
| `formRequisiteType` | `String` | `"all"` (default) ou `"any"` |
| `unlockOnMastery` | `Double` | Mastery mínima nos requisitos |
| `stackOnMastery` | `Double` | Mastery para poder stackar |
| `instantTransformOnMastery` | `Double` | Limiar de instant (default 40) |
| `allowFreeTransformOnMastery` | `Double` | Limiar para pular de grupo / selecionar no radial (default 50) |
| `formStackable` | `Boolean` | Pode combinar com stack |
| `incompatibleWith` | `List<String>` | `"grupo.form"` bloqueados |
| `shareMasteryWith` / `shareMasteryMultiplier` | lista / double | Mastery compartilhada |
| `str/skp/stm/def/vit/pwr/eneMultiplier` | `Double` | Multiplicadores de combate |
| `speedMultiplier`, `attackSpeed`, `staminaDrainMultiplier` | `Double` | |
| `energyDrain`, `staminaDrain`, `healthDrain` | `Double` | % do máximo por tick (depois escalada) |
| `stackDrainMultiplier` | `Double` | Extra drain se houver stack |
| `maxMastery` | `Double` | Teto do float de mastery |
| `masteryPerHitDealt` / `masteryPerHitReceived` / `passiveMasteryEveryFiveSeconds` | `Double` | Ganho |
| `maxCostMultiplier` / `maxStatsMultiplier` | `Double` | Mastery escala drain vs poder |
| `customModel`, `transformationAnimation`, `modelScaling` | visual | Fora do detalhe do MVP |
| `triggerItemCosts` / `durationItemCosts` | itens | Gate opcional |
| `formCombo` | combo | Override de arma |
| `mobEffects` | efeitos | Aplicados enquanto transformado |

### Grupo — `FormConfig`

| Campo | Tipo |
|---|---|
| `groupName` | `String` |
| `formType` | `String` — mapeia para o nome da skill (`superforms`, `legendaryforms`, `godforms`, `androidforms`, ou o próprio tipo em stacks) |
| `forms` | `Map<String, FormData>` ordenado |

### Estado no jogador — `Character`

Ver tabela em `02-capability-system.md`. Distinção crítica:

- **selected** = o que o radial aponta (alvo da charge)
- **active** = o que está valendo agora

### Mastery — `FormMasteries`

Chave: `group.toLowerCase() + ":" + form.toLowerCase()`. Valor: `double` capado em `maxMastery`.

### ActionMode

`FORM`, `STACK`, `FUSION`, `RACIAL`. Só FORM (e STACK se o Bleach quiser camada extra) importam no MVP.

## Lista de estágios no original (mecânica, não tema)

Seis raças, cada uma com 1–4 grupos. Ordem **dentro do grupo**:

| Raça | Grupo | Cadeia |
|---|---|---|
| human | superforms | buffed → fullpower → overdrive → solaris |
| human | androidforms | androidbase → superandroid → fusedandroid |
| human | legendaryforms | shiyoken → shin_shiyoken → chou_shiyoken |
| saiyan | oozaru | oozaru → goldenoozaru → supersaiyan4 |
| saiyan | ssgrades | supersaiyan → grade2 → grade3 |
| saiyan | supersaiyan | mastered → ssj2 → ssj3 |
| saiyan | legendaryforms | ikari → ssjhybrid → ssjfullpower |
| namekian | superforms | giant → fullpower → supernamekian |
| frostdemon | evolutionforms | second → third → final → fullpower → fifth |
| majin | pureforms | kid → evil → super → ultra |
| bioandroid | bioevolution | semiperfect → perfect → superperfect → ultraperfect |
| (qualquer) | kaioken (stack) | x2 → x3 → x4 → x10 → x20 → x100 |
| (qualquer) | ultimate (stack) | ultimate (soma o delta da **melhor** form base unlocked) |

Gates especiais do original (não portar tema, só saber que o sistema *permite* gates):

- Android upgraded: só `androidforms` + `godforms`
- Cauda Saiyan: oozaru
- Lua cheia: oozaru auto-charge (sem charge manual)
- Efeito mutant: acesso legendary com skill level −1 e modifier de poder

O factory default encadeia forms sequenciais com `unlockOnMastery = 25` (`DefaultFormsFactory.applySequentialMasteryRequisites`).

### Mapeamento sugerido para o MVP Bleach

| Camada original | Bleach MVP |
|---|---|
| race `human`/`saiyan`/… | race `shinigami` |
| group `superforms` | group `zanpakuto` |
| forms ordenadas | `sealed` → `shikai` → `bankai` |
| form-skill `superforms` | skill `zanpakuto` níveis 0, 1, 2 |
| stack | omitir no MVP |
| TransformationReward | quest de nome da Zanpakutō / treino de Bankai |

## Requisitos de transição

Uma form está **unlocked** quando **todos** valem:

1. **Form-skill** — `Skills.isUnlockedAtLevel(skillName, unlockOnSkillLevel)`. Skill registrada por `RaceCharacterConfig.formSkillsCosts` (array de preços TP). `StatsData.updateTransformationSkillLimits(race)` no create/load/troca de raça.
2. **Mastery chain** — `formRequisite` + `unlockOnMastery`. Creative bypass. Tokens `"grupo.form"`. `any` vs `all`. Lê mastery de form **ou** stack (o maior).
3. **Gates de raça/status** (android, cauda, mutant) — o Bleach substitui pelos seus (ex. “já fez a quest de Shikai”).
4. **Selecionável no radial** — primeira form unlocked do grupo **ou** mastery ≥ `allowFreeTransformOnMastery`.
5. **No instante do transform:**
   - Custo upfront: `0.10 * maxRecurso * drainDaForm` para energia, stamina e health
   - Instant: mastery ≥ `instantTransformOnMastery` e custo `adjustedEnergyDrain * 4`
   - Itens de trigger/duração se configurados
   - Stack: ambas `formStackable`, listas compatíveis, mastery ≥ `stackOnMastery`
   - Pulo de grupo: precisa `allowFreeTransformOnMastery` salvo se já está no grupo
6. **Atalho de quest** — `TransformationReward` seta mastery (default 100), mastery da cadeia de requisitos, e o nível da form-skill.

Não há XP de “evolução”. TP compra a skill; combate gera mastery; quest pode pular.

## O que muda no jogador ao evoluir

### Stats

- `getFormMultiplier(stat)` lê o multiplier da form ativa; `RES` = média DEF+STM.
- Mastery escala poder: se `baseMult > 1`, `baseMult * (1 + ratio * (maxStatsMultiplier - 1))` onde `ratio = clamp(mastery/maxMastery)`.
- `getTotalMultiplier` = form × stack × effects × secondary, **ou** soma de (x−1) se a config `multiplicationInsteadOfAdditionForMultipliers` for false.
- Ultimate stack: em vez de um mult simples, soma o delta da melhor form base unlocked compatível.
- Mutant: reduz/aumenta o bônus da legendary group.

### Drain (enquanto transformado)

Para energia, stamina e health, a mesma receita:

1. Drain base da form (e do stack, se houver)
2. Se os dois ativos, cada drain × `stackDrainMultiplier` das duas
3. `costFactor = 1 + ratio * (maxCostMultiplier - 1)`
4. Se drain < 0 (regen): `drain / costFactor * powerRelease`; senão `drain * costFactor * powerRelease`
5. Soma base + stack
6. × `CombatConfig.baselineFormDrain` × multiplicador de gravity load
7. Clamp: se negativo, `min(-1, scaled)`; se positivo, `max(1, scaled)`

StaminaDrainMultiplier à parte: `base * stack * load`, piso 0.001.

Revert forçado: energia ≤ 5% do máximo; recurso zerado pelo tick de drain; item de duração acabou; (original) perdeu a cauda em oozaru. Power release cai a 0 no revert forçado.

### Visual / status

- `refreshDimensions()` (scale)
- Efeitos `TRANSFORMED` / `TRANSFORM` (charging)
- Aura, modelo, animação — client
- `formCombo` troca o combo de arma

## Como a transição é disparada

| Gatilho | Mecânica |
|---|---|
| Segurar charge | Client `UpdateStatC2S(ACTION_CHARGE, true)` → `Status.isActionCharging` → tick incrementa `actionCharge` até 100 → `FormModeHandler.performAction` → `attemptTransform` |
| Instant | `ExecuteActionC2S(INSTANT_TRANSFORM)` se mastery ≥ limiar |
| Descend | `ExecuteActionC2S(FORCE_DESCEND)` — form anterior no mapa, ou snapshot `previousForm`, ou base. Android upgraded volta para `androidbase`, não vazio |
| Auto (oozaru) | Lua / fake moon; sem charge manual — **não portar** |
| Revert passivo | Drain / item / cauda / ki ≤ 5% |
| Só selecionar | `SelectFormC2S` muda selected + ActionMode; **não** transforma |

Ritmo da charge: `10 + min(15, mastery * 0.2)` por passo no handler.

## Comunicação ao jogador

- Action bar / chat: `message.dragonminez.transformation`, `form.no_ki`, `form.no_stamina`, `form.free_transform_mastery`, `form.drained_ki`
- Sons: `TRANSFORM_ON/OFF`, `INSTA_FORM_ON/OFF`, `STACK_FORM`, `NO_KI_FORM`
- HUD: barra de `actionCharge` + `powerRelease`; cor da aura da form ativa
- Radial: nós filtrados por `getSelectableFormNames`
- Packets: `StatsSyncS2C` após select/transform; `AppearanceSyncS2C` em ganho de mastery; `TriggerAnimationS2C` se houver anim

## Fluxo completo (progresso → mudança de estágio)

### A. Unlock de longo prazo

1. Player ganha TP (quest, treino, combate).
2. Compra níveis da form-skill (`UpdateSkillC2S` PURCHASE/UPGRADE).
3. Forms com `unlockOnSkillLevel <= nível` entram em `getUnlockedForms`.
4. Combate na form: mastery sobe por hit dado/tomado (`StatsEvents`) e a cada 5 s (`TickHandler`) se não está no teto.
5. Mastery da form N libera N+1 via `formRequisite` + `unlockOnMastery`.
6. Opcional: quest `TRANSFORMATION` salta skill + mastery.

### B. Seleção

1. Radial mostra `getSelectableFormNames`.
2. `SelectFormC2S(group, form, stack=false)`.
3. `StatsSyncS2C`.

### C. Transformação carregada

1. Hold → `ACTION_CHARGE true`.
2. Charging effect + aura.
3. Tick: incrementa `actionCharge`.
4. Ao 100: `attemptTransform`:
   - Valida gates, recursos, itens
   - Deduz 10% × drain de cada recurso
   - `setActiveForm`, marca `formsUsedBefore`, som, mensagem, efeito TRANSFORMED
   - `refreshDimensions`, sync
5. `TransformStatusHandler` detecta mudança → mob effects + `FormChangeEvent`.

### D. Durante

1. Tick: drains; possível revert.
2. A cada 5 s: mastery passiva.
3. Multipliers entram em todo cálculo de dano/defesa.

### E. Instant / descend

- Instant: burst de ki, sem barra.
- Descend: um degrau para trás ou base.

## Pontos de integração

| Sistema | Relação |
|---|---|
| Skills | Form-skill é o gate de tier; `SkillReward` também sobe |
| Quests | `TransformationReward`, objetivo `SKILL`, pré-req `SKILL`/`RACE` |
| Resources | Custo e drain; HUD de reiatsu |
| Rede | Select / charge / execute / sync |
| UI | Radial, HUD, character creation (raça) |

## Decisões de design observadas

- Tudo é string + JSON: adicionar Bankai é um arquivo, não um enum novo. É o maior acerto para um fork temático.
- Selected vs active: o jogador pode “mirar” Bankai sem estar nele — a charge é o ritual.
- Mastery como float separado da skill: a skill é compra (progressão de conta); mastery é prática (progressão de uso). O Bleach pode manter os dois (treinar Zanpakutō vs despertar via quest) ou colapsar num só se quiser MVP mais simples.
- Inferência: o dual stack+base existe para Kaioken-sobre-SSJ; Hollowfication-sobre-Shikai seria o análogo, mas é fase futura.
- Inferência: oozaru auto-charge é um caso especial que polui `TransformationsHelper`; não copiar.

## Riscos/complexidades para reimplementar

- Fórmulas de multiplier/drain/mastery são fáceis de divergir 1% e quebrar balance — documentadas acima; `FormModeHandler` e `TransformationsHelper` estão em `/reference-code`. `StatsData` é grande demais e misturado com combate: **não** foi copiado; reimplementar só `getFormMultiplier`, `applyMasteryStatBonus`, `getMasteryCostFactor` e os três `getAdjusted*Drain`.
- `getUnlockedForms` vs `getSelectableFormNames` (unlocked ≠ clicável) é um bug clássico de UI se confundidos.
- Creative bypass em mastery requisite: decidir se o Bleach quer isso.
- `updateTransformationSkillLimits` no load: esquecer isso deixa form-skills com maxLevel 0.
- Não portar oozaru/lua/android upgraded/mutant/ultimate-best-form no MVP — cada um é um ramo extra.
- Aparência (modelo, cabelo, aura) pode ser stub: só um indicador de estágio no HUD basta para o MVP.

## Implementação Bleach — drain rebalanceado

Os defaults novos usam `energyDrain` 0,08/tick no Shikai e 0,16/tick no Bankai. Controle aplica `drain base ÷ (1 + 0,10 × rank)`, com retorno decrescente e sem possibilidade de drain negativo. O custo de entrada continua usando o `energyDrain` configurado. Como defaults não sobrescrevem JSON de mundo, configurações antigas 0,4/0,8 precisam de migração explícita.
