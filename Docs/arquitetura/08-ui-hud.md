# UI e HUD

## Estado da implementação Bleach — 10/09/2026

J abre o diário com paginação e rolagem, K reúne status/compras, Z seleciona formas. HUD e rastreamento são desenhados em código. Cobertura do fundo e interpolação de carga foram alteradas; os PNGs e a validação visual final permanecem pendentes.

Consulte o [manual atual](../jogador/manual-do-jogador.md) e o [relatório de implementação](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções seguintes preservam a referência do Dragon Mine Z e não devem ser interpretadas como funcionalidades já entregues no Bleach.


## Resumo

Quests e evolução têm superfícies distintas: uma **árvore de story** (tela cheia), um **HUD de quest trackeada**, um **diálogo de NPC**, **toasts**, e um **radial de forms**. Todas leem a capability **local** do client (populada pelos S2C). Nenhuma tela faz request/response de dados — só dispara C2S de ação. Character creation escolhe a raça (e portanto as árvores de form) antes de qualquer quest.

## Modelo de dados (telas)

| Tela | Path original | Dados que lê | Mutação |
|---|---|---|---|
| `QuestTreeScreen` | `client/gui/character/QuestTreeScreen.java` | `StatsData` + `PlayerQuestData` + `QuestRegistry` client | Action/claim/track/difficulty/saga/party C2S |
| `TrackedQuestHUD` | `client/gui/hud/TrackedQuestHUD.java` | `trackedQuestId` + registry | nenhuma |
| `QuestNPCDialogueScreen` | `client/gui/quest/QuestNPCDialogueScreen.java` | Listas do packet de abertura + registry | `QuestActionC2S` |
| Story toasts | `client/gui/quest/StoryToast` + `StoryNotificationManager` | Payload do `StoryToastS2C` | nenhuma |
| Radial de forms | `client/gui/radial/nodes/RadialForms` etc. | `Character` + `ConfigManager` + `TransformationsHelper` | `SelectFormC2S`, opcional `ExecuteActionC2S` |
| `RaceSelectionScreen` | `client/gui/character/RaceSelectionScreen.java` | `ConfigManager.getLoadedRaces()` | `StatsSyncC2S` / create character |
| `CharacterCustomizationScreen` | mesmo pacote | `Character` local | `UpdateCharacterC2S` |
| HUD de recursos | `XenoverseHUD` / `AlternativeHUD` | energy, stamina, `actionCharge`, `powerRelease`, aura | keybinds → `UpdateStatC2S` |

## Fluxo de funcionamento

### Árvore de quests

1. Abre pelo menu do personagem (keybind / botão).
2. `updateStatsData()` relê a cap local.
3. Se `!difficultyChosen`, overlay de EASY/NORMAL/HARD bloqueia o resto.
4. Layout funcional:
   - **Esquerda (~⅓):** navegador saga / side branches, scroll
   - **Centro:** grafo pan/zoom; cor do nó = status
   - **Direita (~⅓):** título, descrição, objetivos, rewards; botão primário (Start / Resummon / Track / Turn-in); claim
   - **Topo:** abas de saga; `UnlockSagaC2S` se locked
   - **Claim all** flutua quando há rewards pendentes
   - **Party** (overlays de invite) — omitir no MVP
5. Clique em nó seleciona; não começa sozinho.
6. Após C2S, a tela espera `ProgressionSyncS2C` e redesenha.

### HUD trackeado

- Overlay Forge **acima** da health vanilla (`RegisterGuiOverlaysEvent`).
- Canto superior direito: título da quest + linhas do(s) objetivo(s) ativo(s) (respeita sequencial vs paralelo).
- Sem packet próprio.

### Diálogo de NPC

1. Server calcula três listas (oferecíveis / em progresso / turn-in) e manda `OpenQuestNPCDialogueS2C`.
2. Layout: texto de diálogo (scroll) | lista de quests | detalhe da selecionada.
3. Start/Turn-in → `QuestActionC2S`. NPCs “master” no original também abrem treino — fora do MVP.

### Radial de forms

- Categorias: super / more / stack, filtradas por raça + skill + mastery.
- Máximo ~5 slots visíveis; overflow vira nó “More”.
- Select = `SelectFormC2S`. Double-select no original também manda instant transform.
- Preview 3D (`FormPreview`) — opcional no Bleach; um ícone de estágio basta.

### Criação de personagem

- Se `!hasCreatedCharacter`, o client **força** a tela (tick + key).
- Carousel de raças com preview → customização (cabelo/olhos/aura/classe) → confirm seta a flag no server.
- No Bleach MVP: uma raça (Shinigami) + nome da Zanpakutō / cores já bastam; o carousel pode ser stub.

### HUD de reiatsu / estágio

- Barras: energia (reiatsu), stamina, charge de transformação (`FormRelease`), power release.
- Indicador textual ou ícone do estágio ativo (`activeForm`).
- Dois skins no original (Xenoverse / Alternative); o Bleach precisa de **um**.

## Pontos de integração

| Sistema | Como a UI acessa |
|---|---|
| Capability client | fonte de verdade após S2C |
| QuestRegistry client | definições (título, objetivos, rewards) |
| ConfigManager client | forms/raças (sync de config no login) |
| Rede | só ações, nunca fetch |

## Decisões de design observadas

- UI burra + server inteligente: a árvore não decide se a quest está disponível; o server recusa e devolve feedback.
- Registry syncado em JSON permite a árvore desenhar quests que o client nunca “compilou”.
- Inferência: misturar party, dificuldade e árvore na mesma tela deixou `QuestTreeScreen` enorme. No Bleach, separar “Journal” de “Party” se party existir um dia.

## Riscos/complexidades para reimplementar

- `QuestTreeScreen` original é um dos arquivos mais densos do client — **não copiar**. Recriar um journal linear (lista + detalhe) já cobre o MVP; o grafo pan/zoom é polish.
- Forçar character creation no tick é agressivo (bloqueia o mundo). Manter, senão quests/forms correm sem raça.
- HUD deve sobreviver a `trackedQuestId` apontando para quest que o registry ainda não recebeu (race no login): esconder até o registry chegar.
- Não documentar pixel-a-pixel: o visual Bleach será outro. A estrutura funcional acima é o contrato.
