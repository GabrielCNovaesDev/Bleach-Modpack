# UI e HUD

## Atualização Bleach — 10/09/2026

Status de 304 × 228 mantém sete atributos/BP, com tooltips de compra. Radial explica bloqueio de sequência. Toasts usam desenho em código e subtítulo de até duas linhas. katana_handheld compensa a orientação das três texturas. PNGs e homologação visual continuam pendentes.

[Relatório atual](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções de Dragon Mine Z abaixo são referência do original.

## Branch `Life-UI` — HUD conceitual (2026)

A branch `Life-UI` substitui os corações de Minecraft por uma HUD Bleach toda desenhada em código:

- Painel base: `src/main/resources/assets/bleachmod/textures/gui/hud/hud_panel_full.png` (2169 × 725), desenhado com escala responsiva limitada entre 0.09× e 0.14×.
- Três trilhos de preenchimento entregues pelo usuário:
  - `hud_health_fill.png` (Vida) — gradiente vermelho.
  - `hud_reiatsu_fill.png` (Reiatsu) — gradiente azul.
  - `hud_transform_fill.png` (Transformação) — gradiente vermelho→azul.
- Reiatsu numérica, estágio da Zanpakutō e Spiritual Points formam um bloco separado no canto inferior esquerdo, imediatamente ao lado da hotbar (ou acima dela em telas estreitas). O game-side já chama esses valores de SP (Spiritual Points), embora a chave de tradução histórica seja `hud.bleachmod.tp`.
- Cansaço da barra de Transformação: lê `ResourcesData.getActionCharge` (0–100). 0% quando o R não está sendo segurado.

### Suprimir corações vanilla

O contrato de `RegisterGuiOverlaysEvent` permite **registrar** overlays acima dos existentes, mas não remove os vanilla. A solução é escutar `RenderGuiOverlayEvent.Pre` no bus **Forge** e cancelar quando o overlay atual for o da vida do Minecraft.

Componente: `com.bleachmod.client.hud.VanillaHealthHider` (registrado por `@Mod.EventBusSubscriber` com `Dist.CLIENT` e `Bus.FORGE`). Filtra por `VanillaGuiOverlay.PLAYER_HEALTH.type()` e por `StatusData.hasCreatedCharacter()` — antes da confirmação de Shinigami, os corações vanilla permanecem visíveis (mesma regra que o `ReiatsuHud` usa para se esconder).

### Mapeamento dos pontos de saúde do client

O `ProgressionService.applyVitality` aplica um modificador transient de `MAX_HEALTH` apenas no server. Para o client não voltar a 20 fixo, o `ReiatsuHud` reconstrói o `getMaxHealth()` somando `2 * nível de Vitalidade` (de `AttributeData.VITALITY`) ao valor base do atributo vanilla. Assim a barra de Vida segue a regra do manual do jogador §5 mesmo sem sync do modifier.

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

## Implementação Bleach — tela de status

A tela K distribui sete categorias em duas colunas, mostra rank sem sufixo de cap, custo do próximo nível e tooltip de efeito. O BP informativo é recalculado do snapshot local sincronizado. Compra continua sendo C2S e validada no servidor; a tela apenas desabilita o botão quando o saldo local é insuficiente.

## Implementação Bleach — HUD (`bleach_player_hud`)

A antiga `ReiatsuHud` foi rebatizada para overlay `bleach_player_hud` (registrado em `BleachClient.registerOverlays`) e reescrita para renderizar o painel conceitual. O `HudLayout` limita a escala pelo tamanho lógico da GUI, mantendo o painel no alto à esquerda com margem e reduzindo-o nos GUI Scales maiores. O overlay usa `GuiGraphics` para escalar o PNG inteiro com `PoseStack#scale`, depois blita cada fill na sua faixa interna e desenha a percentagem como texto. O bloco de valores detalhados é ancorado à esquerda da hotbar e sobe para cima dela quando não há largura disponível. O cancelamento dos corações vanilla fica separado, em `VanillaHealthHider`, justamente porque o registro de overlay no Forge não permite removê-los.
