> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

# Ryujin: fogo ambiental temporário e inicializador de testes

Data: 04/10/2026. Branch exclusiva: `Feature-Poderes-bankais`. Base: `32024a2`.
O usuário autorizou executar a etapa restante de fogo ambiental e acrescentou um
inicializador clicável com limpeza e nova compilação. Documentação alterada somente
em `Docs/poderes`; os documentos das outras pastas continuam intactos.

## Contrato do fogo ambiental adotado neste ciclo

- Bankai com Ryujin na mão principal: oito tentativas na entrada e três a cada 40 ticks.
- Raio horizontal de seis blocos, excluindo os dois blocos próximos do lançador.
  Uma tentativa pode falhar por terreno, sorteio, limite ou regra do mundo.
- Cada foco dura 100 ticks (cinco segundos de tempo de jogo). Sobreposição renova
  apenas o prazo do proprietário que emitiu o foco, sem apagar os demais.
- Máximo de 32 posições por proprietário e 512 por dimensão, contando registros
  pendentes de chunks descarregados. Não força carregamento de chunks.
- Coloca `bleachmod:spirit_flame` somente em ar, sobre uma face superior sólida,
  até dois blocos acima/abaixo da altura dos pés. Não substitui vegetação, água,
  construções ou fogo comum. Não se espalha e não consome o bloco de apoio.
- Respeita `doFireTick` para novas colocações; a limpeza de focos existentes continua.
- Dano base de contato: quatro pontos, no máximo uma tentativa a cada 20 ticks
  da entidade. Usa a fonte de ataque do proprietário e passa pelas regras de combate
  existentes. Não aplica fogo vanilla que continuaria queimando fora do foco.
- Exclui lançador, aliados, espectadores, mortos, NPCs de quest e morcegos invocados;
  jogadores respeitam PvP. Sem proprietário conectado na mesma dimensão, não dá dano.
- Ao sair de Bankai, desequipar, morrer ou desconectar, cessa a emissão; focos já
  emitidos terminam pelo prazo. Sair do jogo não pausa o prazo se o servidor continuar.

Este é o primeiro efeito persistente no terreno do kit. Não é um incêndio vanilla
descontrolado nem um sistema geral de destruição de biomas. O Corte continua com
seu contrato destrutivo anterior. Vínculo de Zanpakutō continua fora deste ciclo.

## Implementação passo a passo e arquivos

1. `init/ModBlocks.java`: registro Forge do bloco espiritual sem colisão, sem loot,
   luz 15; contato encaminha para o serviço. Pistões destroem o foco em vez de movê-lo, evitando blocos sem registro de prazo. `BleachCommon.java` registra os blocos
   no barramento de inicialização, sem criar item de inventário.
2. `assets/bleachmod/blockstates/spirit_flame.json` e `models/block/spirit_flame.json`:
   usam o modelo/textura animada vanilla de fogo com renderização `cutout`.
   Não exigem novas imagens, shaders ou pacotes de rede.
3. `common/technique/SpiritFlameService.java`: `SavedData` por dimensão, com mapa de
   posição e mapa de UUID → expiração. Nome no save: `bleachmod_spirit_flames`.
   Registra posição como long, proprietários e prazos em NBT. Não altera schema da
   capability do jogador ou o protocolo 2.3.
4. `place`: valida chunk carregado, gamerule, ar/apoio, sobreposição e limites antes
   de colocar o bloco. Calcula prazo usando `ServerLevel.getGameTime()`.
5. `expire`: a cada 20 ticks, revisa somente posições carregadas. Remove proprietários
   vencidos; remove o bloco somente se ainda for o bloco espiritual e não houver
   outro proprietário ativo. Um bloco colocado posteriormente é preservado.
   Posições descarregadas permanecem no save e são limpas quando carregadas novamente.
   Contato também consulta o prazo, impedindo dano de um foco expirado antes da limpeza.
6. `contact`: procura proprietário conectado, consulta `TechniqueTargets`, aplica
   dano aceito pelo pipeline de combate. Sobreposição não soma dano por proprietário.
7. `RyujinReleaseEffects.java`: entrada Bankai e aura periódica chamam `emit`;
   Selada/Shikai continuam com efeitos visuais sem criar esses focos.

## Inicializador na raiz, passo a passo

Arquivos: `Iniciar-Teste.bat`, `tools/iniciar-teste.ps1`, `.gitignore`.

1. Extraia o ZIP inteiro ou abra o checkout local. Feche o cliente de testes anterior
   e builds da IDE antes de clicar duas vezes em **Iniciar-Teste.bat**.
2. O BAT abre o script usando Windows PowerShell, a partir da própria pasta do projeto,
   inclusive quando o caminho contém espaços. Mantém o terminal aberto ao terminar.
3. O script exige um **JDK 17**, encontrado por JAVA_HOME, PATH ou instalações locais
   usuais/Gradle. Falta de JDK produz orientação e encerra, sem limpeza.
4. Um lock exclusivo impede dois inicializadores simultâneos. Lista fechada de limpeza:
   `build`, `.test-launcher-cache`, `run/logs` e `run/crash-reports`. Caminhos são
   resolvidos e conferidos dentro da raiz antes de exclusão recursiva; links/junctions
   são recusados, incluindo links dentro da pasta alvo.
5. Roda `check build` com `--no-daemon --no-build-cache --rerun-tasks`, usando cache
   de projeto temporário próprio. Uma build com falha impede a abertura do cliente.
6. Roda `runClient`, aguarda o jogo fechar e remove `.test-launcher-cache` no bloco
   `finally`. Próxima execução também limpa resíduos de uma interrupção abrupta.
7. Mundos (`run/saves`), opções, configurações, screenshots, fontes e Git são
   preservados. Downloads compartilhados do Gradle/JDK/Forge e a `.gradle` convencional
   ficam preservados: apagar esses arquivos repetidamente exigiria novos downloads
   grandes. O inicializador limita seus próprios artefatos, não zera caches do computador.
8. Opções: `Iniciar-Teste.bat -BuildOnly` só compila; `-Offline` usa downloads já
   disponíveis; `-DryRun` lista as ações sem apagar nem executar Gradle. Na primeira
   execução normal é necessária internet para obter dependências ausentes.

Não é instalador de Minecraft nem executável independente. Precisa Windows, JDK 17
e repositório completo com wrapper. Para distribuir um ZIP, inclua os arquivos
versionados, sem `build`, `.gradle`, `run`, caches e `.git`. A build termina com JAR em
`build/libs/bleachmod-0.2.0.jar`; o cliente aberto é o ambiente de desenvolvimento Forge.

## Validação e homologação

- Inicializador: simulação dos caminhos e execução real `-BuildOnly -Offline` aprovada;
  cache temporário removido ao concluir, build limpa recompilada e regressões executadas.
- GameTests novos: serialização/recuperação, dois proprietários, expiração parcial/total,
  preservação de bloco posterior, recusa de terreno sólido e expiração pelo tick do mundo.
- Validação final: `gradlew.bat --offline --no-daemon check build runGameTestServer`.
  Build aprovado, 51 regressões e 18 GameTests passaram; 24 JSONs validados.
- Abertura interativa do cliente pelo BAT e visual do fogo devem ser homologados no jogo.
  Nenhum benchmark de FPS/TPS foi feito; limites evitam crescimento ilimitado de focos.

Para homologar: libere Bankai em terreno aberto, observe focos acenderem e sumirem;
saia da forma e confirme fim das emissões; sobreponha dois lançadores; substitua um foco
por um bloco e confirme que ele permanece; teste salvar/reabrir e descarregar/recarregar
chunks. Confira contato com hostis, aliados e jogador com PvP desligado.

## Revisão posterior: inicializador auditado

Depois da homologação do usuário, inicializador ganhou validação única para DryRun/execução, enumeração sem entrar em links, preflight de todos os destinos, lock sem links, caminhos literais e código de erro quando a limpeza final falha. [Relatório de segurança](15-auditoria-seguranca-inicializador-2026-10-04.md). O contrato de poderes acima permanece igual.
