# Plano de implementação — NPCs de quests

## Objetivo do primeiro incremento

Adicionar oito NPCs humanoides estáticos, cada um com skin própria e ovo de spawn. Ao usar o botão direito em um NPC, o jogador abre um painel que lista apenas as quests oferecidas por aquele personagem e pode aceitar uma quest de teste.

NPCs previstos:

| ID lógico | Nome exibido | Entidade | Ovo |
|---|---|---|---|
| `sado_yasutora` | Yasutora Sado | `bleachmod:npc_sado_yasutora` | `bleachmod:npc_sado_yasutora_spawn_egg` |
| `ichigo` | Ichigo Kurosaki | `bleachmod:npc_ichigo` | `bleachmod:npc_ichigo_spawn_egg` |
| `orihime` | Orihime Inoue | `bleachmod:npc_orihime` | `bleachmod:npc_orihime_spawn_egg` |
| `uryuu` | Uryuu Ishida | `bleachmod:npc_uryuu` | `bleachmod:npc_uryuu_spawn_egg` |
| `rukia` | Rukia Kuchiki | `bleachmod:npc_rukia` | `bleachmod:npc_rukia_spawn_egg` |
| `byakuya` | Byakuya Kuchiki | `bleachmod:npc_byakuya` | `bleachmod:npc_byakuya_spawn_egg` |
| `urahara` | Kisuke Urahara | `bleachmod:npc_urahara` | `bleachmod:npc_urahara_spawn_egg` |
| `ulquiorra` | Ulquiorra Cifer | `bleachmod:npc_ulquiorra` | `bleachmod:npc_ulquiorra_spawn_egg` |

O ID lógico fica separado do ID da entidade. Assim, as quests usam nomes simples (`rukia`, `ichigo`) e o registro deixa explícito que a entidade é um NPC.

## Onde colocar as skins

As skins foram fornecidas diretamente na pasta:

```text
src/main/resources/assets/bleachmod/textures/entity/
```

Arquivos esperados:

```text
sado_yasutora.png
ichigo.png
orihime.png
uryuu.png
rukia.png
byakuya.png
urahara.png
ulquiorra.png
```

Requisitos dos arquivos:

- PNG no layout de skin de jogador do Minecraft, preferencialmente `64x64`.
- Nomes totalmente minúsculos, sem espaços, acentos ou caracteres especiais.
- Manter transparência somente onde ela fizer sentido na segunda camada da skin.
- Informar quais skins usam braços finos (modelo Alex). Sem essa informação, o primeiro incremento usará braços padrão (modelo Steve) para todas.
- Não colocar as skins em `run/`, pois essa pasta é apenas o mundo/cliente de desenvolvimento e não é empacotada no JAR.

## Arquitetura proposta

### 1. Entidade compartilhada e oito tipos registrados

Criar uma classe `QuestNpcEntity`, reutilizada pelos oito `EntityType`s. Cada tipo terá identidade, tradução e ovo próprios, mas compartilhará toda a lógica.

Comportamento inicial:

- criatura passiva e invulnerável;
- sem IA de caminhada, ataque ou alvo;
- sem despawn natural;
- não empurra nem é empurrada;
- permanece no ponto e rotação em que foi colocada;
- exibe o nome do personagem;
- não tem spawn natural: aparece somente por ovo ou comando.

Não usar `setNoAi(true)` como única regra. A imobilidade deve fazer parte da implementação da entidade, para não depender de NBT externo e para continuar consistente após salvar/recarregar o mundo.

Arquivos principais:

- `entity/QuestNpcEntity.java`: interação, imobilidade e identidade do NPC.
- `registry/ModEntities.java`: oito registros de entidade e uma tabela segura `EntityType -> npcId`.
- `common/events/ModEntityEvents.java`: atributos dos oito tipos.
- `init/ModItems.java`: oito `ForgeSpawnEggItem`s.
- `init/ModCreativeTabs.java`: inclusão dos ovos na aba do mod.

O arquivo `registry/ModItems.java` é uma duplicata que atualmente não é registrado por `BleachCommon`; a implementação deve usar `init/ModItems.java` e, em uma limpeza separada ou no mesmo incremento com teste, remover a duplicata para evitar futuros registros no lugar errado.

### 2. Renderização das skins

Criar um renderer compartilhado, por exemplo `QuestNpcRenderer`, usando `PlayerModel` e `ModelLayers.PLAYER`/`PLAYER_SLIM`. O renderer seleciona a textura pelo ID lógico do NPC.

Não são necessárias animações personalizadas: o modelo vanilla de jogador já fornece a pose básica. A entidade ficará parada; somente a animação visual básica do modelo pode ocorrer.

Registrar o mesmo renderer para os oito tipos em `client/events/ModEntityRenderers.java`.

### 3. Integração real com o sistema de quests

O código atual ainda não implementa `quest_giver`, embora a documentação de arquitetura já mencione esse campo. Adicionar à classe `Quest`:

- campo `questGiver`;
- getter e setter;
- serialização como `quest_giver`;
- inclusão em `QuestParser` com validação do formato do ID;
- índice em `QuestRegistry`, ou método de consulta, para obter as quests de um NPC sem percorrer e filtrar dados em cada frame.

Uma quest sem `quest_giver` continua aparecendo e funcionando no diário global. Uma quest com `quest_giver` somente pode ser aceita ao interagir com o NPC correspondente.

### 4. Abertura segura do painel

Fluxo proposto:

1. Jogador usa o botão direito no NPC.
2. No servidor, `QuestNpcEntity.mobInteract` verifica a mão principal, jogador vivo e distância.
3. O servidor envia `OpenNpcQuestScreenS2C(npcId, entityId)` somente ao jogador que interagiu.
4. O cliente abre `NpcQuestScreen`, carregando as quests já sincronizadas no `QuestRegistry` e filtrando por `quest_giver`.
5. Ao clicar em Aceitar, o cliente envia `QuestActionC2S(START, questId, entityId)`.
6. O servidor busca novamente a entidade pelo `entityId`, confirma que ela ainda existe, é um `QuestNpcEntity`, está próxima e que seu `npcId` coincide com o `quest_giver` da quest.
7. Somente depois dessas validações chama `QuestService.startQuest`.

Essa validação no servidor é necessária mesmo em single-player e impede aceitar quests exclusivas sem estar diante do NPC.

### 5. Painel de teste

Criar `client/gui/NpcQuestScreen.java`, visualmente simples e sem inventário/container. Ele deve mostrar:

- nome do NPC no cabeçalho;
- lista das quests oferecidas;
- título, descrição, objetivos e recompensas da quest selecionada;
- estado: indisponível, disponível, aceita ou concluída;
- botão `Aceitar` apenas quando permitido;
- mensagem clara caso o NPC ainda não tenha quests.

O painel pode reaproveitar pequenos métodos de formatação do `JournalScreen`, mas não deve herdar dele, pois o contexto e os botões são diferentes. Para este incremento, recompensa e rastreamento continuam no diário existente; depois eles podem ser adicionados ao diálogo do NPC.

### 6. Quests de teste

Adicionar uma sidequest pequena para cada NPC em `QuestDefaults`, dentro de `sidequests/npc_test/`. Todas recebem `quest_giver` com o ID lógico correspondente e usam apenas recursos já suportados pelo MVP (`KILL`, `ITEM`, TPS e item).

Sugestão de conteúdo provisório:

| NPC | Teste |
|---|---|
| Sado | derrotar 3 zumbis |
| Ichigo | derrotar 2 Hollows |
| Orihime | possuir 3 maçãs |
| Uryuu | derrotar 3 esqueletos |
| Rukia | manter o treino repetível atual, agora oferecido por `rukia` |
| Byakuya | derrotar 4 aranhas |
| Urahara | possuir 4 papéis |
| Ulquiorra | derrotar 3 endermen |

Como `QuestDefaults.writeIfMissing` não sobrescreve JSONs já existentes, um mundo de desenvolvimento anterior conservará a versão antiga de `rukia_basic_training.json`. Para testar os novos defaults, usar um mundo novo ou editar/remover apenas os JSONs de teste em `run/<mundo>/bleachmod/sidequests/`, e então executar o comando de reload existente.

### 7. Recursos e traduções

Adicionar:

- oito modelos de item em `assets/bleachmod/models/item/`, todos com parent `minecraft:item/template_spawn_egg`;
- nomes de entidade, ovos, quests e textos da tela em `pt_br.json` e `en_us.json`;
- opcionalmente cores de ovo relacionadas à paleta de cada personagem;
- loot tables vazias para os NPCs somente se eles deixarem de ser invulneráveis no futuro. No comportamento inicial, não são necessárias.

## Ordem de implementação

1. Receber e validar as oito skins (dimensão, layout e tipo de braço).
2. Implementar e registrar entidade, atributos, renderer, texturas e ovos.
3. Adicionar `quest_giver` ao modelo, parser, serialização e registro de quests.
4. Implementar o pacote de abertura e a tela do NPC.
5. Endurecer `QuestActionC2S` com contexto do NPC e validação no servidor.
6. Adicionar as oito quests de teste e traduções.
7. Cobrir parser/filtro/validação com regressões e validar manualmente no cliente.

## Critérios de aceite

- Os oito ovos aparecem na aba criativa do Bleach Mod.
- Cada ovo cria o NPC correto e ele mantém posição/rotação após recarregar o mundo.
- Cada NPC usa sua própria skin e seu nome aparece corretamente.
- Botão direito abre o painel apenas para o jogador que clicou.
- O painel mostra somente quests cujo `quest_giver` corresponde ao NPC.
- Uma quest disponível pode ser aceita e progride pelo sistema existente.
- Uma quest de outro NPC não pode ser aceita por pacote adulterado nem pelo painel errado.
- Jogador distante, morto ou diante de uma entidade removida não consegue iniciar a quest.
- Servidor dedicado inicia sem carregar classes exclusivas do cliente.
- `gradlew.bat check` e os GameTests passam.

## Fora deste primeiro incremento

- diálogo cinematográfico;
- animações, voz, patrulha ou combate dos NPCs;
- edição visual de quests;
- spawn natural;
- marcador de quest sobre a cabeça;
- sistema completo de entrega (`turn_in`) e recompensa dentro do painel;
- skin/modelo diferente por transformação ou roupa.
