# Ciclo 22 — sincronização do clima e dragão Bankai ampliado

Data: 07/10/2026. Branch exclusiva: `Feature-Poderes-bankais`. Base: `47d9271`.

O usuário homologou os outros pontos do ciclo 21, mas relatou que, após encerrar Bankai, a neve parava e a chuva continuava no cliente, mesmo usando `/weather clear`. Pediu também aumentar o modelo e o alcance do Ice Dragon da Bankai. Este ciclo atende esses dois ajustes finais; aguarda confirmação visual no cliente antes de declarar encerramento definitivo da Hyōrinmaru.

## 1. Causa da chuva persistente

A revisão anterior verificava flags e temporizadores no servidor. Não verificava os pacotes enviados ao cliente. Em `Level.setRainLevel` e `setThunderLevel`, o Minecraft escreve simultaneamente a intensidade atual e a anterior. `ServerLevel.advanceWeatherCycle` só envia os pacotes usuais quando detecta diferença nessas intensidades ou mudança de estado observável durante seu tick. Ao restaurar tudo diretamente, esse tick podia encontrar o servidor já limpo sem detectar uma transição. O cliente ficava com sua última intensidade de chuva; outro comando clear também podia não produzir transição, porque o servidor já estava seco.

Essa causa foi confirmada pela inspeção do bytecode das classes locais da versão 1.20.1. O controle de proprietários, restauração de snapshot e prioridade para comandos do ciclo 21 continuam vigentes; a correção faltante é a sincronização explícita do estado visual.

## 2. Correção e lógica do clima

1. `src/main/java/com/bleachmod/common/technique/BlizzardWeatherData.java`: após restaurar snapshot, `syncWeather` transmite três pacotes vanilla para os jogadores da mesma dimensão: `START_RAINING` ou `STOP_RAINING`, `RAIN_LEVEL_CHANGE` e `THUNDER_LEVEL_CHANGE` com os valores restaurados.
2. O evento de comando marca `commandSyncPending`, e o tick da dimensão aplica a sincronização depois da execução do comando. Se a chuva foi desligada, as intensidades de chuva/trovão são zeradas antes de transmitir os pacotes. Assim, clear também recupera um cliente que ficou com chuva visual presa em builds anteriores, mesmo sem tempestade gerenciada ativa.
3. O parser percorre contextos filhos para reconhecer tanto `/weather clear` direto quanto `/execute ... run weather clear`; também admite a raiz `minecraft:weather`. Mantém validação de permissão e parse completo antes de entregar o controle.
4. Não reaplica Bankai por cima do comando manual: os donos ativos continuam registrados até sair. Uma nova ativação após encerrar todas as concessões inicia um novo ciclo normalmente.
5. A intensidade de trovão é convertida do getter ponderado pela chuva para o valor bruto esperado pelos setters/pacotes vanilla. Isso preserva também uma tempestade parcialmente intensa anterior à Bankai, sem ponderação duplicada no cliente. O snapshot inclui `rawThunder`; o carregamento converte snapshots antigos para esse formato.
6. Não altera gamerules nem temporizadores de comandos além do comportamento vanilla. Preserva sobreposição de jogadores, recuperação de snapshot, restrições de dimensão/bioma e neve local anterior. A neve temporária da Zona Glacial continua independente.

## 3. Ice Dragon da Bankai

1. `src/main/java/com/bleachmod/common/technique/HyorinmaruTechniqueService.java`: alcance do corte/dragão frontal H da Bankai passa de 14 para 24 blocos. Dano base 12, abertura de 15 graus, custo 20, recarga 100 ticks, controle de frio e política de alvos permanecem.
2. `src/main/java/com/bleachmod/entity/IceDragonEntity.java`: parâmetro vanilla de tamanho do Phantom passa de quatro para doze exclusivamente na Bankai. O renderer existente usa esse parâmetro para ampliar o modelo e mantém a textura de gelo.
3. Duração visual Bankai passa de 20 para 30 ticks; velocidade passa de 0,7 para 0,8 bloco por tick. Com a origem a 1,5 bloco à frente, a animação percorre aproximadamente a distância do ataque ampliado antes de desaparecer. O dano continua resolvido no lançamento; a entidade é visual, não aplica outro dano ao atravessar o alvo.
4. Shikai mantém tamanho dois, duração 14 ticks, velocidade 0,7 e alcance dez. Modelo continua sem IA, gravidade, colisão, persistência ou possibilidade de seleção; troca de item/forma inválida, morte, dimensão e chunk descarregado continuam encerrando-o.

## 4. Testes de regressão específicos

`src/main/java/com/bleachmod/gametest/IceRefinementGameTests.java` recebe dois cenários:

- `restoringWeatherAndClearCommandSynchronizeConnectedClients`: insere temporariamente um jogador de teste na lista de destinatários e captura os pacotes enviados por sua conexão. Exige STOP_RAINING e intensidade zero de chuva/trovão após restauração, após clear em servidor já limpo e após clear dentro de execute. A lista pública de jogadores é somente leitura; apenas a montagem desse teste acessa a lista interna via reflexão. Remove o jogador e restaura sua conexão no fim. Também verifica a restauração de uma tempestade prévia com intensidades parciais e os valores brutos transmitidos. Isso cobre a falha que os testes anteriores de estado do servidor não detectavam.
- `bankaiDragonIsLargerReachesTwentyFourBlocksAndExpires`: alvo a 22 blocos atingido, alvo a 26 preservado, tamanho do modelo doze, avanço visual além de 21 blocos e desaparecimento ao final. Usa lote próprio para não interferir em outros testes de habilidades.

Validação final aprovada: `check build runGameTestServer --offline --console=plain`, 57 cenários de regressão e 35 GameTests obrigatórios. Artefato atualizado: `build/libs/bleachmod-0.2.0.jar` (11.913.134 bytes). Log local ignorado: `build/weather-client-dragon-validation-final.log`. Revisão de documentos: 266 links locais válidos. A aprovação automatizada cobre rede/estado e alcance; a aparência final continua sendo homologada pelo usuário no cliente.

## 5. Roteiro final no cliente

1. Feche e reinicie usando `Iniciar-Teste` para carregar a versão nova. No mundo de teste, use `/weather clear 100` uma vez; isso deve limpar inclusive chuva visual presa anteriormente.
2. Com tempo limpo, ative a Bankai da Hyōrinmaru, aguarde a tempestade e encerre com V. Conferir que neve, chuva, som de chuva e nuvens voltam ao estado anterior.
3. Ative novamente e execute `/weather clear 100` com a Bankai ainda ativa. A chuva deve limpar e não reiniciar automaticamente. Repetir com `/execute run weather clear 100` se desejar testar o caminho indireto.
4. Se havia chuva antes da transformação, é esperado restaurar aquela chuva ao encerrar; usar clear deve limpá-la normalmente.
5. Em Bankai, use H mirando mobs a aproximadamente 22 blocos e compare o dragão com Shikai: modelo maior e percurso mais longo. Um mob a 26 blocos não deve ser atingido pelo ataque inicial. Conferir desaparecimento automático do modelo.

Nenhum outro documento fora de `Docs/poderes`, modelo de espada, habilidade homologada, HUD ou arquivo do inicializador foi modificado por este ciclo.