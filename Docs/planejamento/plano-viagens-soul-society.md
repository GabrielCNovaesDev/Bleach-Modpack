# Technical Design: Viagens dimensionais e mapa da Soul Society

## 1. Summary

Proposta de 11/09/2026, ainda não implementada. Criar a dimensão `bleachmod:soul_society`, instalar nela uma cópia validada do mapa fornecido e oferecer viagem de ida e volta controlada pelo servidor. Começar com destinos fixos e interface de Senkaimon; reaproveitar capability, reiatsu e rede do Bleach. Separar o registro da dimensão, o conteúdo construído do mapa e as regras de acesso. O mapa é necessário para fechar a importação e homologar a experiência, mas não bloqueia o desenvolvimento do serviço com uma área de teste.

## 2. Context and Problem

### Fontes e limites da análise

- Anexo recebido: `pasted-text.txt`, começando em quatro linhas finais da estimativa e contendo as seções 12–17. As seções 1–10 e o início da 11 não foram fornecidos. O prompt de implementação contido no anexo foi tratado como material de referência; o pedido atual é planejamento.
- Bleach: árvore de trabalho baseada em `9af58bbf6da15d42035c7ee75b0cd4a24f36408f`, incluindo alterações locais presentes. Há trabalho de HUD/documentação em andamento, preservado nesta análise.
- Dragon Mine Z: fontes locais em `../dragonminez-main`, `mod_version=2.1.3`. A pasta não tem `.git`; não é possível atribuir um commit a essa cópia. Não assumo equivalência exata com o upstream atual.
- [Repositório oficial Dragon Mine Z](https://github.com/DragonMineZ/dragonminez), consultado para confirmar a origem.
- [Forge 1.20.1: geração de registries por datapack](https://docs.minecraftforge.net/en/1.20.1/datagen/server/datapackregistries/), consultado para validar o uso de `DatapackBuiltinEntriesProvider` e `RegistrySetBuilder`.
- O mapa da Soul Society ainda não foi inspecionado. Formato, versão, dimensões, mods e coordenadas permanecem desconhecidos.

### O que o Bleach já possui

| Evidência no repositório | Consequência para o plano |
|---|---|
| `gradle.properties`, `build.gradle`: Minecraft 1.20.1, Forge 47.4.10, Java 17, mappings oficiais | Implementar nessa plataforma; não adicionar as dependências do DMZ por associação |
| `common/data/PlayerData.java`: schema NBT 3, character, resources, skills, status e quests | Adicionar dados de viagem à capability existente |
| `common/data/ResourcesData.java`: `consumeReiatsu`, validação de custo finito | Reutilizar débito e sincronização de recursos |
| `Reference.java`: protocolo **2.2** | O README ainda cita 2.1; usar o código como referência ao planejar o próximo protocolo |
| `common/network/NetworkHandler.java`: canal único, direção explícita | Acrescentar pacotes ao fim e atualizar protocolo em ambos os lados |
| `server/events/CapabilityEvents.java`: clone, login, respawn, troca de dimensão e logout | Integrar persistência, cancelamento e sincronização nesses ciclos |
| `common/data/StatusData.java`: limite transitório de quatro ticks para ações | Não equivale a um cooldown persistente de viagem |
| `common/RegistryReload.java`: prepare/validate/install para quests e formas | Repetir esse padrão para destinos; falha mantém configuração anterior |
| `common/quest/QuestDefaults.java`: saga `soul_society`, objetivos KILL/ITEM | Nome da saga não significa que exista dimensão; não mover progressão implicitamente |
| `build.gradle`: runData e inclusão de `src/generated/resources` | Infraestrutura disponível; não foram encontrados providers de dimensão no Bleach |
| `MvpRegressionTest`, `MvpGameTests` e tarefa `regressionTest` | Ampliar a infraestrutura de verificação existente |

Os caminhos Java acima são relativos a `src/main/java/com/bleachmod/`. Não há serviço de viagem, destinos ou dimensão temática implementados na árvore inspecionada.

### Conferência do Dragon Mine Z

Os caminhos seguintes são relativos a `../dragonminez-main/src/main/java/com/dragonminez/`:

| Código observado | Aproveitamento / adaptação |
|---|---|
| `server/world/dimension/NamekDimension.java`, `server/world/gen/NamekGeneration.java`, `common/datagen/DMZWorldGenProvider.java` | Chaves de dimensão/tipo, LevelStem e datagen são uma referência válida. O gerador de terreno de Namek não importa um mapa construído |
| `server/events/EntitiesEvents.java:onEntityInteract` | Descoberta ocorre no servidor; exige skill ≥1 e guarda UUID do mestre, dimensão e posição |
| `common/stats/character/Character.java` | `InteractedMasters` persiste localização; para lugares fixos, preferir ID de destino e coordenadas atuais do registro |
| `common/network/C2S/RequestITTargetsC2S.java` | Menu exige nível 5; mestres e party atravessam dimensões a partir de 10; jogadores externos têm filtros próprios de distância e BP |
| `InstantTransmissionTravelC2S.java` e `InstantTransmissionTravelToPlayerC2S.java` | Servidor resolve alvo e chama `ServerPlayer.teleportTo`; boa divisão de autoridade |
| `common/util/ITTeleportHelper.java` | Custo adicional `floor(distância/25)*5`; cross-dimension usa distância zero nos handlers. Não transferir esses números para reiatsu sem balanceamento |
| Os mesmos handlers e helper | Cobrança/cooldown precedem a busca de pouso; busca termina em `center.above(1)` sem validar. Corrigir no Bleach |
| Os mesmos handlers | `isVehicle()` seguido de `stopRiding()` não representa corretamente a checagem de jogador passageiro. Definir explicitamente política para montarias |

A documentação recebida propõe melhorias que não estão todas presentes nesses fontes. Ela não é uma especificação já implementada pelo original.

## 3. Goals

- Soul Society como dimensão real no mesmo save, com o mapa preservado.
- Entrada e retorno seguros, inclusive após reconectar e reiniciar o servidor.
- Destinos e custos resolvidos no servidor; cliente envia apenas identidade da ação.
- Compatibilidade com personagens e quests existentes.
- Procedimento reproduzível de instalação do mapa e recuperação.

## 4. Non-Goals

- Nesta primeira entrega: Hueco Mundo, Dangai jogável, geração procedural de Seireitei, NPCs mestres, viagem para jogadores/party e descoberta de centenas de locais.
- Viagem por altitude, alteração do Overworld ou cópia dos níveis 5/10 do DMZ.
- Portal físico persistente, animação elaborada de abertura ou nova árvore de skills. A interface poderá receber esses acionadores depois.
- Instalador automático de mapas dentro do mod. Primeiro validar uma instalação offline em cópia de save.

## 5. Assumptions

- Hipótese inicial: mapa Java compatível com 1.20.1; confirmar antes de converter ou copiar.
- Destino fixo de chegada na Soul Society e retorno à posição de partida no Overworld, com chegada alternativa fixa e validada no Overworld.
- Protótipo libera acesso para Shinigami com personagem criado, sem exigir Shikai/Bankai. Regra narrativa final é uma decisão de produto pendente.
- Proposta inicial configurável: entrada custa 20 reiatsu, retorno custa 0, cooldown de 100 ticks. Valores para teste, não balanceamento aprovado.
- Acesso inicial apenas a partir do Overworld; dentro da Soul Society, oferecer retorno. Nether/End não viram portas implícitas.
- Proibir morto, spectator, jogador dormindo, passageiro ou com passageiros. Creative segue as regras por padrão; resgate administrativo é separado.
- Sem bloqueio de combate na primeira versão: o projeto não tem um estado de combate aplicável à viagem. Se necessário, criar rastreamento server-side antes de habilitar essa regra.

## 6. Requirements

### Functional Requirements

1. Registrar dimensão estável e resolver `server.getLevel` após iniciar mundo novo e cópia de mundo existente.
2. Bloquear entrada enquanto mapa e destinos não estiverem validados.
3. Listar chegada/retorno com custo e motivo de bloqueio; permitir repetir após erro.
4. Revalidar posição de retorno: não confiar que o local antigo continua seguro.
5. Preservar inventário, atributos, skills, quests e formas conforme comportamento existente; limpar carga de transformação e queda/velocidade na viagem.

### Non-Functional Requirements

Busca de pouso limitada: proposta de raio 4 e intervalo vertical ±3, no máximo 567 candidatos por tentativa. Restringir aos chunks preparados ao redor de destinos autorizados. Uma viagem pendente por jogador, limites de taxa separados para consulta e execução, sem varredura do mapa inteiro. Medir duração e impacto nos ticks com dois jogadores; a meta inicial é não produzir pausas acima de 50 ms atribuíveis ao processamento da viagem em chunks preparados, no ambiente registrado de teste.

### Security Requirements

Não aceitar XYZ, dimensão, custo, UUID de outro jogador ou desbloqueio via cliente. Permissão de operador nível 2 para configuração e resgate. Revalidar a cada execução, mesmo com menu antigo. ID desconhecido, tamanho excessivo, enum inválido ou versão de catálogo antiga devem produzir rejeição controlada.

### Operational Requirements

Backup antes de importação; servidor fechado durante cópia de regiões. Manifesto do mapa com versão, hash, área importada e entradas seguras. Logs de inicialização, rejeições agregadas e falhas de viagem. Nunca sobrescrever mapa existente automaticamente ao atualizar o mod.

## 7. Proposed Solution

### Architecture

Fluxo: interface Senkaimon → C2S por ID → política/registro de destinos → preparação limitada dos chunks → pouso seguro → revalidação final → teleporte → persistência/custo/sync → resultado.

Novos componentes propostos, todos sob `com.bleachmod`:

| Componente | Responsabilidade |
|---|---|
| `server/world/dimension/SoulSocietyDimension` | ResourceKey de Level e DimensionType |
| `common/datagen/BleachWorldGenProvider` e subscriber de datagen | Emitir DimensionType e LevelStem; registrar bioma/settings apenas se necessários |
| `server/travel/TravelTargetRegistry` | Preparar e validar destinos em JSON do mundo; snapshot imutável e versão |
| `server/travel/TravelPolicy` | Acesso, custo, cooldown e condições de uso compartilhadas entre lista e execução |
| `server/travel/SafeLandingService` | Validar corpo completo, piso, fluidos, perigos e limites; retornar ausência quando falhar |
| `server/travel/DimensionTravelService` | Orquestrar preparação, teleporte, retorno, cobrança e resultado |
| `common/data/TravelData` | Retorno e cooldown persistentes do jogador |
| `server/events/TravelEvents` | Cancelar pendências no logout, morte ou mudança externa de dimensão; liberar tickets |
| `client/gui/SenkaimonScreen` | Lista, estado de processamento, bloqueios e feedback |

Não criar capability ou canal de rede paralelo. Nenhuma classe de cliente deve ser carregada no dedicado.

### Dimensão versus mapa

Usar `DatapackBuiltinEntriesProvider` para as definições estáticas. A configuração de destinos não registra dimensões por reload; alteração da dimensão exige reinício.

Para o protótipo, usar gerador vanilla simples com área de teste. Para o mapa final, escolher gerador e limites depois de inspecioná-lo: região construída finita favorece vazio fora da área com barreira física/perímetro e restrição de acesso; mapa de terreno explorável pode exigir gerador compatível. `NoiseGeneratorSettings` próprio e `MultiNoiseBiomeSource` não são requisitos universais, apesar do prompt do anexo. Não precisamos gerar novamente construções já salvas em chunks.

Validar minY/height, biome IDs, iluminação, clima, spawn de mobs, camas, âncoras e portais vanilla. Proposta: escala 1:1, sem vínculo automático com portais Nether, sem mudança de respawn para a Soul Society na primeira entrega. A configuração de `DimensionType` deve ser homologada para evitar comportamento destrutivo de camas/âncoras. Não assumir que world border seja independente entre dimensões; se necessário, impor perímetro próprio de acesso e construção.

### Importação do mapa

1. Inspecionar uma cópia: formato (`level.dat`/`.mca`, `.schem`, `.litematic` ou Bedrock), DataVersion, mods, datapacks, command blocks, tamanho, coordenadas e direitos de distribuição.
2. Se for save Java compatível, identificar a dimensão de origem e copiar somente seus dados aprovados para `<save>/dimensions/bleachmod/soul_society/`, incluindo `region`, `entities` e `poi` quando aplicáveis. Preservar coordenadas evita remapeamento de chunks e referências.
3. Não substituir `level.dat`, `playerdata`, avanços, stats ou dados globais do mundo Bleach. Revisar entidades/UUIDs, block entities, referências globais e comandos que apontem para dimensões antigas.
4. Se for schematic/litematic, planejar conversão e colocação offline em mundo de preparação, preservando blocos e entidades necessários. Não tratar esses formatos como pastas de região. Bedrock ou versão mais nova exige estudo de conversão; não prometer downgrade.
5. Validar em save descartável, selecionar chegada e saída, verificar chunks periféricos e dependências faltantes. Só então gerar manifesto com versão/hash e marcar instalação pronta.
6. Não distribuir a cidade inteira no JAR por padrão. Primeira entrega pode ser mod + pacote de mapa/guia de instalação. Distribuição automática para qualquer novo save é fase separada, caso desejada.

### Backend Changes

Pré-checagens não cobram. Obter snapshot do destino, validar acesso e assegurar chunks preparados. Carregamento frio deve usar preparação com timeout, tickets limitados e continuação na thread do servidor; não fazer busca bloqueante ilimitada no clique. Para o primeiro protótipo, manter as áreas fixas preparadas; retorno frio pode exibir estado de preparação.

Após preparação, revalidar jogador, catálogo, energia, origem e pouso. Checar cancelamento de viagem por hooks/eventos aplicáveis do Forge. Executar teleporte e confirmar dimensão/posição antes de confirmar débito e cooldown, na mesma execução da thread principal. O evento de mudança de dimensão pode emitir full sync durante a chamada: enviar sync final após débito para evitar HUD desatualizado. Testar a ordem real dos eventos; não assumir atomicidade entre disco, teleporte e callbacks. Falha parcial deve ser registrada e tratada sem repetir cobrança automaticamente.

Guardar origem como retorno apenas na entrada bem-sucedida; não sobrescrevê-la ao retornar. Se origem foi destruída, tentar somente a alternativa autorizada e validada. Se ambas falham, negar sem custo e disponibilizar resgate administrativo. Não gerar uma plataforma em cima de construções como fallback silencioso.

`SafeLandingService`: bounding box do jogador na posição candidata, sustentação do piso, ausência de colisão/fluidos, hazards em tag configurável (incluindo fogo, lava, cacto, magma e neve fofa), limites verticais e área permitida. Considerar blocos parciais e ocupação por entidades. Nenhum fallback não validado.

### Frontend Changes

Tela simples aberta por keybind configurável, sem reutilizar R/Z. Mostrar destino, custo, cooldown e motivo de bloqueio. Desabilitar execução durante pendência e restaurar em erro/timeout. Strings em `pt_br.json` e `en_us.json`; utilizar o padrão de `ClientPacketHandler` e feedback existente. Partículas/sons simples após sucesso; animação/portal físico fica para evolução posterior.

### Database Changes

Não há banco SQL. Usar capability NBT por jogador e JSON por mundo, descritos abaixo.

### Integrations

Integrar `PlayerData`, `CapabilityEvents`, `NetworkHandler`, `SyncHelper`, `BleachCommands`, inicialização e reload. Quests atuais continuam válidas. Não exigir completar uma quest dentro da Soul Society para poder entrar nela. Descoberta por NPC e objetivo DIMENSION são extensões futuras explícitas.

### Error Handling

Códigos localizados: `disabled`, `map_not_ready`, `unknown_target`, `stale_catalog`, `not_allowed`, `invalid_state`, `cooldown`, `insufficient_reiatsu`, `destination_unavailable`, `preparing`, `timeout`, `unsafe_landing`, `busy`, `travel_cancelled`, `internal_error`. Não enviar stack trace ao cliente. Registrar detalhes técnicos no servidor com identificador da solicitação.

## 8. API Contract

Contrato de pacotes Forge, não HTTP. Acrescentar registros com direção explícita; proposta de protocolo 2.3, confirmando a versão efetiva antes de implementar.

| Pacote | Dados e limites propostos | Tratamento |
|---|---|---|
| `RequestTravelTargetsC2S` | Sem coordenadas/payload de jogador | Limitado por jogador; responde catálogo aplicável |
| `TravelTargetsS2C` | revision; até 32 entradas com id ≤128 caracteres, labelKey, custo finito, reachable, reason, cooldownTicks | Somente informação de UI; não é autorização |
| `TravelRequestC2S` | requestId, targetId ≤128 caracteres, catalogRevision | Sender é identidade; validar tudo novamente |
| `TravelResultS2C` | requestId, estado terminal ou preparando, reason | Correlacionar resposta, concluir estado da tela |

IDs iniciais propostos: `bleachmod:soul_society_entry` e `bleachmod:return`. O segundo é resolvido com dados do próprio jogador. RequestId serve para correlação/deduplicação, não como credencial. Cache recente limitado e trava de pendência evitam duplicatas; cooldown continua necessário para novos IDs.

Comandos propostos: `/bleachdev travel inspect <player>`, `/bleachdev travel validate`, `/bleachdev travel rescue <player>` e `/bleachreload travel`, todos nível 2. Comando normal de viagem, se adicionado, usa o mesmo serviço. Resgate ignora custo/acesso, mas exige pouso seguro.

## 9. Data Model / Migrations

`PlayerData` schema 3 → 4, adicionando `travel`: `returnLocation` opcional com dimensão, XYZ finitos, yaw/pitch; `nextTravelAtGameTime` não negativo. Usar sempre o relógio persistente do Overworld para cooldown, inclusive em outra dimensão. O cooldown não expira enquanto o servidor está parado. Limitar valores absurdos ao carregar.

Schema antigo inicia sem retorno e sem cooldown. Ausência da tag em pacotes parciais não deve apagar dados já carregados; separar inicialização de save antigo do merge de sync. Validar ResourceLocation, números, alturas e tipos de tags. Dimensão inexistente invalida retorno utilizável, sem quebrar login. Clone por morte preserva cooldown e dados persistentes; pendências, tickets e IDs recentes não entram em NBT. Retorno anterior não pode ser usado como teleporte arbitrário fora da Soul Society.

JSON proposto em `<save>/bleachmod/travel/`: configurações de acesso/custo, destinos fixos e manifesto de instalação. Destino contém ID estável, dimensão, posição/orientação, área permitida, flag enabled e política de acesso. Arquivos inválidos não substituem snapshot ativo. Não armazenar XYZ de destinos fixos em cada jogador. Descobertas, se adicionadas depois, persistem apenas IDs.

## 10. Alternatives Considered

| Alternative | Pros | Cons | Decision |
|---|---|---|---|
| Mapa importado em dimensão própria | Preserva trabalho construído e separa mundos | Requer instalação e controle de versão | Recomendada, dependente da inspeção |
| Gerar Soul Society proceduralmente | Novos mundos automáticos | Não reproduz o mapa existente; trabalho de worldgen | Adiar |
| Colocar tudo no Overworld | Menor trabalho inicial | Mistura território e não entrega dimensão | Não recomendada |
| Copiar sistema completo de mestres/party/BP | Mais alvos | Sistemas ausentes e regras de Dragon Ball | Reaproveitar somente os padrões úteis |
| Portal físico antes do serviço | Mais temático | Acrescenta blocos, estados e render antes de validar viagem | Adiar visual; serviço permite integrar depois |
| JSON de dimensão escrito à mão | Simples para protótipo | Validação/manutenção separada do código | Preferir datagen já suportado no build |

## 11. Implementation Plan

Estimativas técnicas preliminares por tarefa, não compromisso de calendário. Conversão complexa do mapa e arte elaborada estão fora das faixas.

| Task | Estimate | Depends on | Done when |
|---|---:|---|---|
| 1. Inspecionar mapa e produzir manifesto preliminar | 2–4 h | Arquivo do mapa | Formato, dependências, área e importação definidos |
| 2. Registrar dimensão, datagen e ambiente de teste | 3–5 h | Pode iniciar sem mapa | JSON no JAR; dimensão acessível em cópia de save |
| 3. Importar cópia do mapa e homologar limites | 4–8 h | 1, 2 | Construções/entidades conferidas e pousos escolhidos |
| 4. Registro de destinos, config e reload validado | 3–5 h | 2 | Erro de JSON preserva snapshot anterior |
| 5. TravelData e migração schema 4 | 2–4 h | Política de retorno | Save antigo, clone e restart preservam estado esperado |
| 6. Pouso seguro e preparação limitada de chunks | 4–8 h | 2, 4 | Falhas negadas; timeout libera tickets |
| 7. Serviço de ida/retorno, custos e cooldown | 4–6 h | 4–6 | Viagem confirmada cobra uma vez; falha não cobra |
| 8. Pacotes, correlação e limites de taxa | 3–5 h | 7 | Cliente forjado não escolhe localização |
| 9. Tela Senkaimon e traduções | 3–5 h | 8 | Ida/volta, pendência e erros utilizáveis |
| 10. Comandos e diagnóstico | 2–3 h | 7 | Operador valida instalação e resgata com segurança |
| 11. Regressões de política/persistência/rede | 3–5 h | 5–8 | Casos negativos e limites cobertos |
| 12. GameTests de pouso e ciclo de vida | 3–5 h | 6–8 | Casos de mundo e cancelamento cobertos |
| 13. Dedicado com dois clientes e mapa real | 3–6 h | 3, 9–12 | Ida, retorno, restart e falhas homologados |
| 14. Guia de instalação e atualização de Docs | 2–3 h | 13 | Instalação/rollback reproduzíveis e docs coerentes |

Total preliminar: **41–72 horas**. Revisar após inspecionar o mapa. Sequência recomendada: provar dimensão + importação; depois serviço por comando; por fim interface e homologação multiplayer. A etapa 2 e os serviços com fixture podem avançar antes de receber o mapa.

Atualizar na implementação: manual do jogador, manual de inicialização, README, planejamento MVP e documentação de capability, rede, NBT, eventos, build e UI. Separar o que é referência DMZ do comportamento Bleach implementado.

## 12. Test Plan

- Regressões: esquema 3 sem travel; round-trip schema 4; NBT inválido; custo zero/exato/insuficiente; valores não finitos; cooldown após dimensão/logout/restart; catálogo antigo e destinos desabilitados.
- GameTests: colisão da bounding box, teto baixo, borda, piso parcial, fluido/perigo, retorno destruído, dimensão ausente, cancelamento, timeout e liberação de ticket. Testar serviço e eventos; FakePlayer sozinho não prova teleporte com cliente real.
- Rede: excesso de consultas, payload inválido, ID inexistente, dois pedidos no mesmo tick, repetição do mesmo ID, dois IDs diferentes durante pendência, reload durante preparação, logout/morte/mudança externa antes de concluir.
- Integração: custo somente após sucesso confirmado, sincronização final após evento de dimensão, carga de transformação cancelada, inventário/progressão intactos.
- Mapa: versão/dependências, baús, entidades, command blocks revisados, iluminação, limites, chunks novos fora da área, Nether/End e comportamento de camas/âncoras.
- Manual: singleplayer e dedicado com dois clientes; ambos no mesmo destino; cliente bloqueado; retorno sem reiatsu; spawn/morte/reconexão; restart com jogador dentro; backup/restore; efeitos e erros traduzidos.
- Build planejado: `./gradlew.bat runData`, `./gradlew.bat build`, `./gradlew.bat runGameTestServer`; `build` já depende de `regressionTest` por `check`. Inspecionar JAR para `data/bleachmod/dimension/soul_society.json` e `dimension_type/soul_society.json`, além dos recursos adicionais realmente utilizados.

Critério final: instalar em cópia limpa, viajar e retornar, reiniciar e repetir sem perda de dados ou cobrança duplicada. Nesta análise não foram executados build, jogo ou testes; não houve alteração de código de gameplay.

## 13. Rollout and Rollback

1. Backup do save e registro das versões de mod/mapa.
2. Disponibilizar definições da dimensão com viagem desabilitada; validar mundo descartável.
3. Com servidor parado, importar mapa em cópia do save. Iniciar para validação administrativa, mantendo entrada de jogadores bloqueada.
4. Configurar destinos, preparar chunks e validar manifesto; habilitar viagem apenas após check positivo.
5. Homologar com dois clientes e acompanhar erros, duração e uso de tickets.

Rollback operacional preferido: desabilitar novas entradas, manter retorno/resgate e a dimensão carregável. Evacuar jogadores online e resolver os offline antes de remover definição/mod. Não apagar regiões nem restaurar chunks parcialmente sobre save ativo. Downgrade de schema/código não tem garantia de preservar tags novas: conservar backup integral e restaurar conjunto compatível quando necessário. Atualização de mapa exige novo procedimento offline; nunca recopia por cima a cada boot.

## 14. Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Mapa em versão/formato incompatível | Perda de blocos ou conversão extensa | Inspeção em cópia antes de fechar importação |
| Dimensão registrada sem mapa instalado | Jogador em vazio/terreno errado | Estado map_not_ready; validação de chegada e manifesto |
| Chunks pré-existentes no destino | Construções conflitantes ou sobrescritas | Destino limpo e instalação offline com backup |
| Destino alterado durante preparação | Teleporte para configuração obsoleta | Revalidar revision/origem antes de concluir |
| Pouso ou retorno degradado | Morte/prisão | Validação no momento; alternativa autorizada; resgate |
| Carga fria ou vazamento de tickets | Travamento/consumo crescente | Orçamento, timeout, cancelamento e medição |
| Callbacks durante teleporte | Sync antigo ou cobrança incoerente | Confirmar resultado, sync final e testes de cancelamento |
| Dependência circular com quests | Jogador não consegue entrar | Regra inicial independente de quest dentro da dimensão |
| Remoção de dimensão com jogadores offline | Save inacessível | Evacuação e backup antes de remoção |
| Mapa com comandos/dependências globais | Comportamento inesperado no save Bleach | Revisão do conteúdo antes de ativar |

## 15. Open Questions

1. Qual é o arquivo/pasta do mapa, versão do Minecraft e lista de mods usados? Esse é o próximo insumo necessário.
2. A viagem deve abrir por habilidade/tecla, item, NPC ou portal físico? Proposta inicial: tela Senkaimon, com serviço reutilizável.
3. Quem pode entrar e em qual ponto da progressão? Proposta para teste: Shinigami já criado.
4. Retorno deve ser ao ponto de partida ou a um portal fixo? Proposta: partida com alternativa fixa segura.
5. O mapa será livre para construir/explorar ou uma área de aventura protegida? Isso define limites, proteção e geração fora das construções.
6. A instalação é apenas no nosso save/modpack ou deve funcionar automaticamente em qualquer mundo novo? A segunda opção exige uma etapa adicional de distribuição/instalação do mapa.

Essas decisões refinam o escopo; não é necessário respondê-las todas para iniciar o protótipo técnico. O mapa é indispensável antes de prometer importação concluída ou homologação da Soul Society.
