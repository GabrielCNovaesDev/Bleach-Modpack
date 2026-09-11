# Revisão técnica do MVP Shinigami — 10/09/2026

Esta revisão registra os achados anteriores à implementação. Consulte o [relatório de implementação](relatorio-implementacao-mvp-2026-09-10.md) para correções entregues, validação e pendências; as referências de linha abaixo são históricas.


> **Atualização de planejamento:** todos os achados e recomendações desta revisão foram incorporados ao [plano de implementação do MVP](plano-implementacao-mvp.md), juntamente com os bugs visuais relatados pelo usuário, tela de status, categorias de pontos, radial e comandos de desenvolvimento. O plano passa a orientar a execução. A proposta de economia abaixo deve ser lida junto à nova solicitação de categorias; números e papéis serão fechados antes da implementação. O mob Hollow permanece adiado, mantendo zumbis como substitutos. Este documento conserva os achados originais e não indica que foram corrigidos.

## Conclusão e escopo

O projeto já tem uma base aproveitável para um MVP pequeno: estado do jogador dividido por responsabilidade, regras de quests centralizadas, operações principais no servidor, registros JSON por mundo e UI própria. O próximo investimento deve ser fechar o ciclo de jogo com confiabilidade: criar personagem → entender missão → cumprir → receber recompensa → transformar → perceber benefício → salvar e continuar.

O escopo desta revisão é Shinigami, Asauchi, Selada/Shikai/Bankai, reiatsu, mastery, pontos, quatro quests, UI e multiplayer básico. Hollow, Quincy, Fullbringer, dimensões, party, NPCs complexos e grandes árvores de habilidades ficam fora. Suportar duas pessoas no mesmo mundo é diferente de balancear PvP competitivo.

Os problemas abaixo foram identificados por leitura e cruzamento dos fluxos. Cenários descritos são roteiros de reprodução, não relatos de testes executados no jogo. Não foi feito teste de invasão nem teste visual dentro do Minecraft.

## Verificações realizadas

- Inventário: 73 arquivos Java; aproximadamente 4.237 linhas não vazias contadas pelo PowerShell; 30 PNGs, somando 6,58 MiB.
- Revisão dos sistemas de dados, quests, formas, eventos, rede, interfaces, recursos e build.
- Build offline concluído com sucesso usando Gradle 8.8 instalado em cache. compileJava e processResources estavam UP-TO-DATE; jar e reobfJar foram executados.
- Artefato gerado: bleachmod-0.1.0.jar, aproximadamente 6,71 MiB.
- compileTestJava, processTestResources e test retornaram NO-SOURCE: não existe suíte automatizada exercitando comportamento.
- JSONs de recursos analisados sem erro de sintaxe; pt_br e en_us têm o mesmo conjunto de chaves.
- Conferência do código-fonte local do Forge 47.4.10: registro de mensagens sem direção usa Optional.empty; o dispatcher de capabilities reconhece INBTSerializable e invalida pelos listeners registrados.
- A primeira tentativa pelo wrapper falhou ao tentar baixar a distribuição. O build foi possível usando a instalação local e o cache, sem modificar a configuração do projeto.
- Não foram alteradas regras de jogo ou arquivos-fonte. Este documento é o produto da revisão.

Build aprovado confirma empacotamento com os insumos locais. Não confirma funcionamento do multiplayer, persistência após respawn ou qualidade visual.

## O que preservar

1. PlayerData separa personagem, recursos, skills, status e quests sem exigir banco de dados.
2. QuestService centraliza início, progresso, conclusão, falha e resgate.
3. Ações C2S usam o remetente real do contexto; resgate valida status, índice e se já foi recebido.
4. Criação de personagem tem proteção contra confirmação repetida.
5. Compra de skill confere saldo e limite no servidor.
6. Existência de defaults permite iniciar um mundo sem configuração manual.
7. Separação conceitual entre forma selecionada, forma ativa e mastery é útil.
8. Mod apenas com Forge é adequado ao escopo. Não há motivo identificado para trocar de plataforma ou incorporar bibliotecas grandes agora.

## Problemas prioritários

Prioridades: P1 = corrigir antes de considerar o fluxo afetado pronto; P2 = corrigir na estabilização; P3 = acabamento. Não identifiquei evidência de um P0 universal. Impactos dependentes de configuração ou multiplayer estão identificados.

### R01 — P1: cliente remoto não recebe definições de formas

Evidência: [FormRegistry.java:22](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/evolution/FormRegistry.java:22), [ForgeCommonEvents.java:17](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/ForgeCommonEvents.java:17), [ClientForgeEvents.java:73](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/client/events/ClientForgeEvents.java:73) e [SyncHelper.java:50](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/network/SyncHelper.java:50).

FormRegistry é carregado no início do servidor. O cliente usa esse registry para calcular formas selecionáveis ao apertar G, mas não existe recepção das definições de formas no cliente. FormRegistry.serialize existe sem completar esse fluxo.

Em um cliente recém-aberto conectado a servidor dedicado, o mapa pode permanecer vazio e G não encontrar nenhuma forma. Se o cliente antes abriu um mundo local, pode usar definições antigas. Singleplayer mascara a omissão porque os lados lógicos compartilham o processo.

Correção: sincronizar um snapshot validado de formas no login e no reload; separar armazenamento de cliente e servidor; limpar dados da sessão no disconnect. Apenas registrar defaults no cliente não resolve JSON personalizado no servidor.

Aceitação: entrar diretamente em servidor dedicado, concluir progressão e ciclar as três formas; repetir com drain/mastery personalizados e após trocar de servidor.

### R02 — P1 em LAN: falta restringir direção de pacotes

Evidência: [NetworkHandler.java:38](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/network/NetworkHandler.java:38) e [SyncQuestRegistryS2C.java:21](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/network/s2c/SyncQuestRegistryS2C.java:21).

Todas as mensagens são registradas sem NetworkDirection explícita. Os handlers S2C usam Dist.CLIENT, que verifica o lado físico. O host de um mundo aberto em LAN também roda em um cliente físico.

Consequência inferida do fluxo: um participante com cliente modificado pode tentar enviar uma mensagem de sincronização no sentido inverso; no host integrado, o handler pode alcançar ClientPacketHandler e o registry estático compartilhado. Isso cria uma via de alteração indevida de estado/definições. Não executei esse cenário de abuso.

Correção: registrar PLAY_TO_SERVER e PLAY_TO_CLIENT explicitamente, rejeitar direção incorreta antes do decoder/handler e manter estado do servidor isolado. Não confiar em nomes C2S/S2C ou DistExecutor como controle de autorização.

Aceitação: mensagens de direção incorreta não são processadas; um cliente remoto não consegue modificar definições nem estado do host.

### R03 — P1: botões do diário não acompanham o estado recebido

Evidência: [JournalScreen.java:58](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/client/gui/JournalScreen.java:58) e [JournalScreen.java:75](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/client/gui/JournalScreen.java:75).

refreshActionButtons só é chamado na inicialização e na seleção de um slot. Dados chegam por rede, mas não atualizam os estados active dos botões.

Cenário: abrir uma missão, iniciar, esperar a resposta e tentar rastrear; ou completar uma missão ITEM com o diário aberto e tentar receber. O texto pode mostrar progresso novo enquanto os botões mantêm o estado anterior. Clicar no slot novamente ou reabrir a tela contorna o problema.

Correção: atualizar ações no tick da tela ou em evento de atualização de estado. O botão Evoluir também deve refletir custo, saldo e nível máximo. Desabilitar temporariamente uma ação pendente melhora o comportamento sob latência; validação do servidor continua obrigatória.

### R04 — P1: regen/drain não chegam continuamente ao HUD

Evidência: [TickHandler.java:30](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/TickHandler.java:30) e [SyncHelper.java:23](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/network/SyncHelper.java:23).

O servidor altera reiatsu a cada tick. A sincronização periódica de recursos está dentro do ramo de carregamento; as demais chamadas são pontuais, como transformação e respawn.

Cenário: transformar e aguardar sem apertar teclas. A energia real cai até reverter, enquanto o cliente pode conservar o valor enviado na transformação. Em selada, a regeneração também fica sem atualização regular.

Correção: sincronizar alterações de recursos em frequência limitada, por exemplo a cada 4–5 ticks enquanto houver mudança, e imediatamente em início/fim de carga e transformação. A frequência sugerida é um ponto de partida, não um benchmark.

### R05 — P2: rastreamento não é apagado pelo snapshot NBT

Evidência: [PlayerQuestData.java:97](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/PlayerQuestData.java:97) e [PlayerQuestData.java:119](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/PlayerQuestData.java:119).

Ao concluir/falhar, o servidor define trackedQuestId como null. save omite a chave. load só modifica o campo quando a chave existe. O cliente conserva o identificador antigo.

Correção: snapshots completos da seção devem representar ausência explicitamente, ou load deve limpar o campo quando ausente. Separar a semântica de snapshot completo e atualização parcial evita repetir esse erro. O HUD também deve conferir se a missão ainda está aceita.

Aceitação: concluir, falhar e remover rastreamento fazem o painel desaparecer sem reabrir o mundo.

### R06 — P2: carga fica presa e falhas podem repetir por tick

Evidência: [FormModeHandler.java:75](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/FormModeHandler.java:75), [TickHandler.java:40](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/TickHandler.java:40), [UpdateStatC2S.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/network/c2s/UpdateStatC2S.java).

Ao chegar em 100, o tick tenta transformar continuamente. Vários retornos por falha não encerram actionCharging nem zeram actionCharge. Alvo já ativo, alvo inválido, bloqueio ou energia insuficiente podem manter a tentativa a cada tick. Soltar R zera no servidor, mas esse pacote não sincroniza a alteração imediatamente.

O intervalo de sync de carga é de 10 ticks, enquanto a carga completa em 4–10 ticks; assim, a barra pode nem aparecer em várias tentativas válidas.

Correção: resultado explícito da tentativa, finalização comum da carga e feedback emitido uma vez por tentativa. Sincronizar começo/fim, inclusive ao cancelar. Troca de alvo durante carga deve reiniciar ou cancelar conforme regra definida.

### R07 — P2: forma selecionada anteriormente permite contornar cadeia

Evidência: [TransformationsHelper.java:96](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/evolution/TransformationsHelper.java:96) e [FormModeHandler.java:84](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/FormModeHandler.java:84).

A seleção valida isSelectable, mas a transformação revalida isUnlocked e uma condição de troca de grupo. needsFreeTransformMastery retorna false quando os grupos coincidem; isso não garante cadeia dentro de zanpakuto.

Cenário sem mastery máximo: ter skill 2 e mastery 25 em Shikai; em Shikai selecionar Bankai; descer para selada mantendo Bankai selecionado; carregar. A forma continua desbloqueada e o grupo é igual, permitindo saltar a cadeia sem mastery 50 de Bankai.

Correção: validar a transição ativa → selecionada no momento da execução. O estado pode ter mudado desde G. Testar também reversão automática e reload.

### R08 — P2: aparência só é enviada ao próprio jogador

Evidência: [SyncHelper.java:42](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/network/SyncHelper.java:42) e [BleachClient.java:46](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/client/BleachClient.java:46).

O modelo do Asauchi de um jogador remoto consulta a capability desse jogador, mas appearance envia somente para o dono. Não há sync de aparência ao começar a observar outro jogador.

Correção: transmitir somente aparência pública aos observadores e ao próprio jogador; inicializar novos observadores. Pontos, skills e histórico de quests não precisam ser enviados aos demais.

O fallback do predicado do item também usa o jogador local quando não há portador Player: um Asauchi em contexto sem dono pode herdar a aparência de quem observa. Definir aparência neutra nesses casos.

### R09 — P2: nomes dos objetivos estão ausentes na UI

Evidência: [JournalScreen.java:186](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/client/gui/JournalScreen.java:186), [TrackedQuestHud.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/client/hud/TrackedQuestHud.java) e [pt_br.json](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/resources/assets/bleachmod/lang/pt_br.json).

Diário e HUD mostram ícone e quantidade; não mostram qual entidade ou item. As descrições falam em Hollows/resíduo espiritual, enquanto os objetivos pedem zumbis, esqueletos e carne podre.

Um jogador sem o manual não tem informação suficiente para completar o conteúdo.

Correção: mostrar nomes traduzidos e quantidades, como “Esqueletos: 1/3” e “Carne podre: 4/8”, distinguindo substitutos vanilla do tema narrativo. Reaproveitar uma descrição de objetivo, evitando apresentar IDs técnicos na interface final.

## Integridade de conteúdo e persistência

### R10 — P2: reload aplica estado parcial e anuncia sucesso

Evidência: [QuestRegistry.java:34](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestRegistry.java:34), [FormRegistry.java:27](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/evolution/FormRegistry.java:27) e [BleachCommands.java:24](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/commands/BleachCommands.java:24).

Os registries são limpos antes da leitura completa; erros por arquivo são registrados e ignorados. O comando termina anunciando sucesso mesmo com arquivos rejeitados. O fallback de formas pode substituir configuração inválida por defaults sem explicar isso ao operador.

Correção: ler e validar em estrutura temporária; publicar o novo snapshot apenas após sucesso; retornar contagens e erros por arquivo/campo. Manter último estado válido quando o reload falhar.

Também validar dependências: previousSaga inexistente ou vazia hoje pode ser tratada como satisfeita. Quest inválida não deve abrir caminho silenciosamente para outro conteúdo.

### R11 — P2: progresso usa índices de objetivos/recompensas sem versão

Evidência: [QuestProgress.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestProgress.java) e [QuestService.java:154](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestService.java:154).

Objetivos, requisitos e resgates são associados à posição na lista. Reordenar JSON troca o significado do mesmo índice. Uma recompensa nova pode aparecer como recebida, e uma antiga movida pode voltar a ficar disponível. Requisitos antigos são congelados, mas o tipo/alvo do objetivo continua vindo da definição atual.

Correção mínima: versão de quest e recusa explícita de alterações estruturais incompatíveis em quests existentes. Evolução pequena seguinte: IDs estáveis para objetivos/recompensas e política de migração. Não resetar progresso automaticamente.

### R12 — P2: cliente e servidor compartilham registry mutável no singleplayer

Evidência: [QuestRegistry.java:54](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestRegistry.java:54).

replaceFromNetwork limpa os mesmos mapas static que o servidor integrado usa. Além de concorrência e estado transitório, a reconstrução de SAGA_QUEST_ORDER na rede não repete a ordenação numérica do carregamento.

Cenário de expansão: arquivos 1.json, 2.json e 10.json são inseridos lexicalmente como 1,10,2; o servidor ordena a lista para 1,2,10, mas a reconstrução de rede usa a ordem de inserção. As três quests atuais não expõem essa divergência.

Correção: snapshots separados e imutáveis, mesma validação/ordenação nos dois lados ou envio explícito da ordem.

### R13 — P2: conclusão ITEM usa contagem possivelmente desatualizada

Evidência: [QuestEvents.java:25](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/QuestEvents.java:25) e [QuestService.java:102](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestService.java:102).

Inventário é contado uma vez por segundo. A conclusão causada por kill consulta o progresso armazenado dos demais objetivos.

Cenário da quest 2: contagem já registrou 8 carnes; jogador larga as carnes e mata o último esqueleto antes da próxima contagem. A missão pode concluir sem os itens exigidos naquele instante.

Correção: validar objetivos de posse diretamente no inventário antes de marcar SUCCESS. Definir claramente se o item é apenas verificado, consumido ou se a etapa fica conquistada permanentemente. O modo atual anunciado é posse, sem consumo.

### R14 — P2, JSON com objetivos sequenciais: um kill pode concluir duas etapas

Evidência: [QuestEvents.java:83](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/QuestEvents.java:83) e [QuestService.java:160](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestService.java:160).

O loop percorre os objetivos e altera progresso durante a mesma morte. Se a primeira etapa KILL termina, a seguinte torna-se ativa dentro do loop e pode contar a mesma entidade se o alvo coincidir.

Correção: determinar objetivos ativos no início do evento; em modo sequencial creditar apenas a etapa ativa original. Contagem paralela entre quests diferentes pode continuar, se for regra desejada.

### R15 — P2, spawn QUEST: propriedade e ciclo de vida incompletos

Evidência: [QuestService.java:173](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestService.java:173) e [KillObjective.java:48](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/objectives/KillObjective.java:48).

O spawn grava UUID do dono, porém o matcher verifica somente quest e índice. Outro jogador com a mesma quest pode consumir esses mobs e receber crédito. Também não há ID de tentativa, limpeza no restart/falha nem validação de posição segura.

Entidades são criadas em offsets no mesmo Y, sem verificação de chão, colisão ou persistência. Tags de tipo servem para matching, mas não definem qual entidade criar. NATURAL com QUEST_SPAWNED_ONLY é uma combinação que não gera o próprio alvo necessário.

As quests padrão usam NATURAL: não é motivo para construir um sistema grande agora. Recomendo desabilitar/rejeitar configurações de spawn não suportadas até completar propriedade, tentativa, limpeza, posição e limite de quantidade.

### R16 — P2: contratos de validação são permissivos demais

Evidência: [QuestParser.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestParser.java), [FormData.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/evolution/FormData.java) e [TransformationsHelper.java:123](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/evolution/TransformationsHelper.java:123).

Há desserialização, mas falta validação semântica consistente: IDs duplicados/nulos, referências inexistentes, formas fora de ordem, custos/drain negativos, valores não finitos, mastery além do máximo e quests sem objetivos.

Pré-requisito de forma malformado sem ponto é ignorado; no modo all isso pode liberar em vez de rejeitar. Divergência entre chave da forma e campo name também pode impedir lookup.

Correção: validar no carregamento e retornar arquivo/campo/motivo. Rejeitar entradas inválidas em vez de corrigir silenciosamente count negativo para 1. Validar caminhos questFolder dentro da raiz prevista; é proteção contra erro de configuração local, não uma acusação de acesso remoto a arquivos.

### R17 — P2: resgate pode marcar uma recompensa que não foi entregue

Evidência: [QuestService.java:154](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/QuestService.java:154), [ItemReward.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/rewards/ItemReward.java) e [SkillReward.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/quest/rewards/SkillReward.java).

give retorna void; após retorno, o serviço marca recebida. Uma skill inexistente é ignorada, e uma recompensa de item sem resolução válida pode não entregar nada. Validação de conteúdo deve impedir isso antes do jogo; um resultado de entrega explícito evita confirmação falsa.

Não foi identificada duplicação normal por duplo clique: o status por índice e execução no servidor já bloqueiam a segunda solicitação após a primeira. O risco de índice vem da edição de conteúdo descrita em R11.

Reclamar tudo hoje envia um pacote por recompensa e sincroniza o jogador várias vezes. Uma ação única de resgatar pendências, preservando garantias por recompensa, seria mais simples para UI e tráfego.

### R18 — P2: ciclo de vida da capability e estados temporários

Evidência: [PlayerProvider.java](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/common/data/PlayerProvider.java) e [CapabilityEvents.java:33](C:/Users/Gabriel/Desktop/PROJETOS/Bleach-Modpack/src/main/java/com/bleachmod/server/events/CapabilityEvents.java:33).

PlayerProvider possui invalidate, mas attach não registra esse método como listener. O LazyOptional pode permanecer válido além do ciclo de vida da entidade. Registrar listener e testar clonagem: não basta adicionar invalidate sem conferir que a cópia após morte continua acessível.

Respawn atualmente envia apenas recursos. É necessário testar a inicialização da capability do novo jogador cliente e enviar snapshot completo após o respawn. Possível perda visual de raça/skills/forma nessa troca exige reprodução no jogo; não foi confirmada em execução.

Carga e actionCharging são persistidos/copiados, embora sejam temporários. Definir resets em morte, respawn, logout e troca de dimensão. O reset de login está separado do envio full em outro subscriber; consolidar ordem evita snapshot anterior ao reset.

ResourcesData.load e CharacterData.load também precisam normalizar valores e IDs. Uma versão de schema NBT e tratamento de entradas inválidas tornam evoluções do save previsíveis.

## Melhorias de experiência ainda dentro do MVP

### Diário e HUD

- Mostrar forma ativa e forma selecionada separadamente. Hoje G altera um alvo que o HUD não apresenta.
- Exibir skill atual, mastery por forma, custo da próxima compra e motivo específico de bloqueio.
- Trocar “Reclamar” por “Receber” e “Liberar estágio” por “Retornar estágio”, se a intenção for português mais claro.
- Atualizar botões após respostas e manter seleção por questKey, não só índice.
- Adicionar rolagem/paginação antes de ultrapassar cinco quests. O sexto conteúdo hoje fica inacessível pelos slots.
- Dimensionar layout pela altura real do texto e área disponível. A descrição quebra linhas, mas o cursor avança 36 pixels fixos; títulos/recompensas não têm contenção.
- Testar GUI scale e janelas pequenas: painel 320×200, cabeçalho e botões precisam caber juntos.
- Oferecer “Parar de rastrear”, já compatível com a intenção do pacote.
- Indicar recompensa pendente após conclusão, em vez de depender exclusivamente do toast.
- Na confirmação de personagem, aguardar resposta do servidor antes de fechar. Hoje a tela fecha imediatamente e pode reabrir enquanto a confirmação ainda está em trânsito.
- Pequena fila de toasts ou agregação: o último toast substitui o anterior, inclusive objetivo/conclusão e múltiplos resgates.
- Limpar lastCharging/toasts/registries de cliente no fim da sessão; separar “R pressionado” de “carga iniciada”. Shift+R seguido de soltar apenas Shift não inicia a carga normal porque lastCharging continua true.
- Corrigir acentuação pt_br, traduzir nomes usados nas recompensas e uniformizar termos.

### Assets

Há ícones perto de 1000×1000 desenhados em 10×10 ou 16×16. Exemplo: icon_objective_kill tem 1022×1022 e é desenhado em 10×10. Os PNGs somam 6,58 MiB.

Criar versões de runtime nas dimensões adequadas, preservar fontes de arte separadas e avaliar legibilidade em escala nativa. Isso reduz memória/texturas e facilita consistência visual; não foi medido ganho de FPS. Não é necessário um novo sistema de renderização.

## Progressão: escolhas de design que precisam ser fechadas

### Transformar precisa ter utilidade perceptível

Asauchi é um SwordItem de ferro fixo. Não há handler aplicando aumento de dano, velocidade ou técnica ao mudar de forma. Atualmente o jogador paga energia por estado/textura/mastery.

Proposta pequena: Shikai ganha um benefício claro de combate com Asauchi; Bankai melhora temporariamente esse benefício com custo maior. Aplicar e remover modificadores de forma determinística, sem acumular bônus ao alternar, morrer ou reconectar. Definir se é necessário segurar a espada. Acrescentar uma técnica somente depois de o combate básico estar correto.

### Desbloqueio e domínio não deveriam se encerrar juntos por acidente

Quests 2 e 3 entregam mastery 100. Isso libera instantâneo e seleção livre imediatamente, tornando treino passivo e vários gates quase irrelevantes no caminho normal.

Proposta: quest desperta a forma, mastery cresce com uso legítimo e uma missão posterior certifica Bankai. Começar com mastery baixo é uma mudança de design, não mera correção. O construtor TransformationReward converte mastery <= 0 para 100; corrigir esse contrato antes de configurar recompensa com zero.

TransformationReward também sobe skills e mastery da forma anterior por nomes hardcoded, além de o JSON já conceder SkillReward. Tornar os efeitos explícitos e evitar dependências ocultas; manter um plano de compatibilidade para saves/JSONs existentes.

### Pontos precisam ter um papel claro

As quatro quests somam 1.350 pontos. Os dois níveis custam 700 no total, mas as quests também entregam os mesmos níveis gratuitamente. Depois disso não há outro gasto implementado.

Dois caminhos pequenos coerentes: quests autorizam a evolução e pontos compram o desbloqueio; ou quests concedem formas e pontos compram poucos aprimoramentos. Recomendo o primeiro pela menor ampliação de sistemas. Se compra antecipada for intencional, explicitar o benefício e evitar que pareça desperdício.

A regra atual não exige a quest 2 para usar Shikai: os 200 pontos da quest 1 compram skill 1. Bankai também pode seguir compra e treino em vez da recompensa correspondente. Documentar a escolha ou adicionar um gate explícito de desbloqueio.

### Calibrar duração com experiência, não só fórmulas

Com 100 de reiatsu, transformação carregada e sem outra ação, a duração aproximada até 5% é:
- Shikai: (100 − 4 − 5) / 8 ≈ 11,4 segundos.
- Bankai: (100 − 8 − 5) / 16 ≈ 5,4 segundos.
- Recuperar de 5 a 100 em selada: aproximadamente 19 segundos.

São estimativas a 20 ticks/s. Um Bankai de cerca de cinco segundos pode fazer sentido como explosão de poder, mas hoje não há benefício de combate correspondente. Primeiro escolher o papel da forma, depois ajustar custo, duração e cooldown. O instantâneo custa menos que o carregado; isso pode ser prêmio de mastery, mas precisa ser decisão explícita.

### Asauchi precisa de reposição

A espada tem durabilidade, é concedida uma vez e não há receita/reposição normal no conteúdo atual. É possível perder a arma por morte, lava ou desgaste.

Adicionar uma receita simples ou recuperação controlada. Se houver futura identidade por jogador, evitar duplicação de propriedades; para o MVP basta uma regra de obtenção clara.

### Quests devem ensinar a mecânica que desbloqueiam

Manter quatro a seis missões curtas. Elas podem ensinar aceitar/rastrear/receber, usar Shikai, administrar reiatsu e retornar à selada. Reutilizar mobs vanilla é suficiente, desde que a UI diga quais são.

Uma pequena missão de uso de transformação pode vir depois da estabilização; não é necessário importar todo o motor de objetivos do mod original. Definir também se morrer falha qualquer missão com KILL ou só enquanto a etapa KILL estiver ativa; hoje a primeira regra prevalece.

## Manutenção e desempenho proporcionais

- Preservar arquitetura geral; extrair regras puras apenas onde isso permitir testes de transição, custos e pré-requisitos.
- Separar carregamento de snapshot completo, atualização parcial de rede e validação do estado.
- Reduzir sync duplicado: completar objetivo e completar quest podem enviar progression mais de uma vez no mesmo fluxo; resgate múltiplo envia full por recompensa.
- Não otimizar prematuramente as quatro quests. Contagem de inventário uma vez por segundo é pequena; quando aumentar conteúdo, agregar por item e enviar um snapshot por alteração.
- Evitar métodos públicos retornando coleções mutáveis dos registries.
- Avaliar remoção ou documentação de estado sem uso efetivo: sagaUnlocks, Skill.active, helpers redundantes. Não apagar extensões futuras sem verificar intenção.
- Unificar nomes Spirit Points/trainingPoints/TPS apenas com compatibilidade NBT/JSON. Renomear chaves de save sem migração perderia dados.
- Fixar versão validada de ForgeGradle para builds reproduzíveis e documentar toolchain Java 17. JAVA_HOME atual aponta para 21; o build usa a toolchain declarada e não falhou por isso.
- Reduzir boilerplate de exemplo do build.gradle, adicionar README de entrada e instruções mínimas de execução/validação.
- Separar documentação da implementação Bleach da engenharia reversa de Dragon Mine Z. O README ainda tem placeholders e pré-requisitos de quests marcados como futuros, embora existam.
- Ao mexer em regras, atualizar manual e exemplos JSON no mesmo trabalho.

## Segurança proporcional ao MVP

Ativos: integridade do save, pontos/resgates, propriedade de encontros e disponibilidade do host. Atores: jogador normal, cliente modificado e operador que edita JSON. Fronteiras: cliente → servidor; JSON local → definições ativas; NBT → estado; servidor → interface.

| Ameaça | Controle | Teste |
|---|---|---|
| Pacote S2C enviado por cliente ao host LAN | Direção explícita e armazenamento separado | Pacote invertido rejeitado antes do handler |
| Repetir resgate/compra/criação | Validação de estado no servidor e idempotência | Repetir ação não duplica ganho |
| Consumir encontro de outro jogador | UUID do dono e ID de tentativa | Dois jogadores com mesma quest mantêm isolamento |
| Spam de ação/feedback | Rejeitar transições inválidas e limitar frequência por ação | Manter botão/pacote repetido não gera flood |
| JSON inválido ou volume exagerado | Limites pequenos, validação e reload atômico | Estado anterior permanece após erro |

Já há boas proteções de resgate e criação. Não há necessidade de sistema externo de autenticação ou infraestrutura de segurança para este mod. A proteção de rede deve acontecer antes de abrir LAN a clientes não confiáveis; análise estática não substitui o teste específico.

## Testes que dão retorno imediato

| Teste | Comportamento esperado |
|---|---|
| Salvar/carregar rastreamento removido | null substitui o valor anterior |
| Resgatar duas vezes | Recompensa recebida uma vez |
| Saldo exatamente igual ao custo | Compra bem-sucedida, saldo zero |
| Saldo insuficiente e skill máxima | Sem alteração parcial |
| Forma desbloqueada, mas salto proibido | Execução rejeita seleção antiga |
| Carga com alvo ativo/energia baixa/cancelamento | Encerra coerentemente, sem spam |
| Mastery na fronteira 24/25, 39/40, 49/50 | Gates corretos |
| Tirar ITEM e completar KILL entre contagens | Não conclui por inventário antigo |
| Dois objetivos sequenciais do mesmo mob | Um kill afeta só a etapa original |
| Reordenar recompensas em quest existente | Migração ou rejeição explícita |
| Reload com JSON quebrado | Último snapshot válido preservado |
| Spawn QUEST de outro jogador | Sem crédito indevido |
| Login/respawn/dimensão/reconexão | Estado completo consistente |
| Servidor dedicado e dois clientes | G funciona; aparência pública correta |
| Mundo local seguido de servidor remoto | Nenhum registry antigo reaproveitado |
| pt_br e en_us, GUI scale alto | Alvos, textos e botões legíveis |

Regras puras podem receber testes unitários. Inventário, eventos, pacotes e ciclo de jogador precisam de integração/GameTest e testes reais com cliente. Não tratar build verde como evidência desses cenários.

## Plano recomendado de entregas

| Ordem | Entrega pequena | Critério para encerrar |
|---|---|---|
| 1 | Sync e fronteiras de rede | Formas em dedicado, direção dos pacotes e dados públicos/privados corretos |
| 2 | UI e carga confiáveis | Botões reativos, rastreamento limpo, reiatsu/carga visíveis, cancelamento correto |
| 3 | Persistência e transições | Respawn/reconexão corretos, gates revalidados, testes de regras |
| 4 | Conteúdo robusto | Reload atômico, validação, versão de quest, resgate seguro |
| 5 | Progressão Shinigami útil | Bônus de combate limitado, papel de pontos/mastery definido, Asauchi recuperável |
| 6 | Acabamento do MVP | Quests didáticas, textos, assets dimensionados e rodada com duas pessoas |

Não iniciar novas raças antes de fechar essas entregas. A prioridade é conseguir jogar o pequeno conteúdo existente do início ao fim sem consultar código, contornar UI ou depender de comportamentos acidentais do singleplayer.

## Referências técnicas consultadas

- [Forge 1.20.1 — Capabilities](https://docs.minecraftforge.net/en/1.20.1/datastorage/capabilities/): sincronização própria e invalidação do provider.
- [Forge 1.20.1 — Sides](https://docs.minecraftforge.net/en/1.20.1/concepts/sides/): lados físicos/lógicos e perigos de estado estático compartilhado.
- [Forge 1.20.1 — SimpleImpl](https://docs.minecraftforge.net/en/1.20.1/networking/simpleimpl/): execução de handlers e validação de dados recebidos.
- Código-fonte do Forge 47.4.10 instalado em cache: SimpleChannel, IndexedMessageCodec e CapabilityDispatcher.
