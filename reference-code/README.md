# Reference code — Dragon Mine Z

Subconjunto deliberado de fontes do [Dragon Mine Z](https://github.com/DragonMineZ/dragonminez), preservado sob **GPL-3.0**.

Critério (plano §5): só entra arquivo cuja lógica de cálculo/algoritmo é complexa o bastante para que reescrever só com a documentação em texto tenha alto risco de erro sutil.

**Não** estão aqui: registries, boilerplate Forge, getters/setters, UI visual, defaults temáticos de Dragon Ball, `StatsData` inteiro (fórmulas de form/drain foram parafraseadas em `Docs/arquitetura/04` e `Docs/arquitetura/07`).

Cada `.java` tem no topo: origem, licença, e por que foi preservado.

| Arquivo | Por que preservar |
|---|---|
| `quest/QuestParser.java` | Schema JSON real: aliases de chave, defaults de enum, validação. Errar o parser quebra todo o conteúdo data-driven. |
| `quest/Quest.java` | Scaling de party (item step, kill ^0.75, HP/dano linear). Fácil divergir 1 inimigo. |
| `quest/QuestAvailabilityChecker.java` | AND/OR aninhado + saga sequencial + TIME anchors + secret. |
| `quest/QuestService.java` | Start / fail-restart / turn-in / claim / spawn de kill. Orquestração com vários early-returns. |
| `quest/PlayerQuestData.java` | NBT + `mergeForwardFrom` + multiplicadores de reward/TP decay. |
| `quest/PartyManager.java` | Sync de party e resolução do controller. |
| `quest/objectives/KillObjective.java` | SpawnMode / CountMode / match de tag / transform overrides. |
| `events/QuestEvents.java` | Crédito de kill, bloco contíguo, wipe→fail, tick de objetivos. Ordem dos handlers importa. |
| `evolution/TransformationsHelper.java` | Unlock vs selectable, mastery requisite any/all, cross-group, descend. |
| `evolution/FormModeHandler.java` | Custo upfront 10%×drain, validações no `attemptTransform`. |

Como usar: ler o `.md` correspondente primeiro; abrir o `.java` só para conferir um ramo (ex. “o que acontece se `count_mode` for ANY e o mob não tem tag”). Recriar no pacote do Bleach, não compilar estes arquivos no fork.

Origem dos paths no clone: `dragonminez/src/main/java/com/dragonminez/…`
