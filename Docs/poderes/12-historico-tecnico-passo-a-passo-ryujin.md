# Ryūjin Jakka — histórico técnico passo a passo

Atualizado em 03/10/2026. Feature concluída por confirmação do usuário após a entrega
`5a02b61`, na branch `Feature-Poderes-bankais`. Esta atualização altera somente a
documentação de `Docs/poderes`; não modifica código nem documentos de outras pastas.

## Como ler o histórico

Os commits `141205c`, `57966f8` e `5a02b61` identificam as três entregas desta branch.
O ponto de partida foi `46ae419`, da antiga `Feature-Bankais-Poderes`. O kit H/N/X
já existia nesse ponto; sua lógica é descrita abaixo como base reaproveitada, sem
atribuir sua criação aos novos commits. Planos, auditorias e relatórios anteriores
mantêm os fatos de suas respectivas versões. O [contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md)
e este histórico explicam quais propostas foram implementadas, substituídas ou preservadas.

Os caminhos de código abaixo são relativos à raiz do repositório. Cada etapa informa
o motivo, a sequência lógica, os arquivos e o resultado observável.

## 1. Base reaproveitada: autorização, slots e técnicas H/N/X

1. O cliente informa a ação/slot; não escolhe dano, custo, alcance ou variante.
2. `TechniqueService.executeSlot` resolve H/N/B/C usando a forma ativa do servidor.
3. `TechniqueService.isRyujinJakkaEquipped` identifica `ModItems.RYUJIN_JAKKA` na mão
   principal. Asauchi permanece arma de combate básico, sem autorizar o kit da Ryujin.
4. H em Selada/Shikai alterna ignição. `StatusData.ignitionActive` guarda o toggle;
   `tickIgnition` cobra 0,05 reiatsu/tick, cancela com arma/forma inválida ou saldo
   insuficiente. `ignitionDamageBonus` fornece bônus 0,15; `applyIgnitionHit` aplica fogo.
5. H em Bankai executa Dash: custo 20, cooldown 100 ticks, duração 16 ticks, velocidade
   1,25 por tick e dano de contato base 4. `markFlameDashHit` registra UUIDs para impedir
   múltiplos impactos no mesmo alvo durante o Dash. O tick trata colisão e cancelamento.
6. N resolve Rajada curta na base e Leque no Bankai. O executor seleciona alvos por
   alcance/direção, cobra uma vez e registra superfícies temporárias em `StatusData`.
   Os ticks repetem partículas/dano periódico; não colocam blocos de fogo. O alcance
   vigente da Rajada é 8, com meia-abertura π/6 (60° totais); o antigo texto de 80°
   pertence ao histórico da migração. Leque usa alcance 14 e 90° totais. Ambos usam
   a recarga de 80 ticks no executor atual.
7. X continua a técnica piloto `flame_burst`, fora dos quatro slots: custo 20,
   cooldown 300 ticks, raio 3 e dano base 6. `executeFlameBurst` valida e executa no servidor.

Arquivos de referência: [TechniqueService.java](../../src/main/java/com/bleachmod/common/technique/TechniqueService.java),
[StatusData.java](../../src/main/java/com/bleachmod/common/data/StatusData.java),
[TickHandler.java](../../src/main/java/com/bleachmod/server/events/TickHandler.java).
Essa base foi preservada nas entregas novas; os overrides de teste da etapa 7 também a cobrem.

## 2. `141205c`: novo conjunto B/C e runtime servidor

Problema: faltava o conjunto aprovado de área/invocação, e o planejamento misturava
variantes e números divergentes do Corte.

1. `TechniqueService.executeSlot` passou a encaminhar B para `executeArea` e C base
   para `executeBats`. C Bankai continua encaminhado ao executor existente do Corte.
2. Criou-se `RyujinTechniqueService`, com mapa estático `ACTIVE` por UUID do proprietário.
   Cada `Runtime` mantém jogador, mundo, área e enxame; esses efeitos não integram o save.
3. Cada cast valida personagem, vida/espectador, raça/grupo/forma, arma, cooldown e saldo.
   Custo e cooldown são aplicados no servidor, após as validações.
4. A primeira versão de B base era Círculo; B Bankai era Tornado. Esse Círculo foi
   substituído na etapa 4 e não é uma habilidade adicional do kit atual.
5. Áreas usam duração absoluta em ticks do mundo, consulta de entidades e intervalo
   de dano por UUID. Partículas são apresentação; não aplicam dano por amostra.
6. `TechniqueGeometry.intersectsCylinder` verifica hitboxes no cilindro finito para
   excluir cantos da caixa de consulta e entidades acima/abaixo da área.
7. Morte, logout, dimensão, transformação, arma/personagem inválido, unload e parada
   cancelam o runtime. Iteração por snapshot evita invalidar o mapa durante mortes
   provocadas pelo próprio dano.

Arquivos criados: [RyujinTechniqueService.java](../../src/main/java/com/bleachmod/common/technique/RyujinTechniqueService.java),
[TechniqueGeometry.java](../../src/main/java/com/bleachmod/common/technique/TechniqueGeometry.java),
[RyujinGameTests.java](../../src/main/java/com/bleachmod/gametest/RyujinGameTests.java).
Também alterados: `TechniqueService.java`, `StatusData.java`, `FormModeHandler.java`,
`ProgressionService.java`, `BleachCommands.java`, idiomas `en_us.json`/`pt_br.json` e
`MvpRegressionTest.java`. Relatório daquela versão: [09](09-relatorio-ciclo-2026-10-03.md).

## 3. `141205c`: recargas por slot e consolidação do Corte

1. Adicionou-se `techniqueSlot3CooldownTicks`, com leitura, atribuição e decremento
   em `StatusData`. Variantes da mesma tecla compartilham a recarga do slot.
2. `clearTransformationState` guarda recargas, limpa efeitos de transformação e
   restaura os contadores. Assim, mudar forma não libera um slot ainda em cooldown.
3. `FormModeHandler` e `ProgressionService.normalize` passaram a usar essa limpeza
   preservando recargas, inclusive na normalização após comandos/compras.
4. O Corte teve constantes consolidadas em `TechniqueService`: dano base 48,
   custo 45 e cooldown `60 * 20` = 1.200 ticks.
5. Preservou-se o executor instantâneo pela mira: alcance 100, meia-abertura 12,5°,
   extensão vertical 15 abaixo/20 acima, um impacto por alvo e destruição sem drops.
   Ar, bedrock, command blocks e blocos de dureza negativa conservam as proteções existentes.
6. `BleachCommands.cooldowns clear` passou também a cancelar o novo runtime, evitando
   efeitos antigos bloqueando a próxima tentativa após limpeza administrativa.

Arquivos: [StatusData.java](../../src/main/java/com/bleachmod/common/data/StatusData.java),
[FormModeHandler.java](../../src/main/java/com/bleachmod/server/events/FormModeHandler.java),
[ProgressionService.java](../../src/main/java/com/bleachmod/common/ProgressionService.java),
`TechniqueService.java` e `BleachCommands.java`. Cooldowns continuam transitórios,
sem persistência em NBT. Não foi adicionado sistema de proteção de região ao Corte.

## 4. `57966f8`: Círculo substituído por Muralha

Motivo: o usuário considerou o Círculo repetitivo e definiu um paredão reto temporário.

1. Em `RyujinTechniqueService`, constantes e feedback de Círculo foram substituídos
   por Muralha: comprimento 16, largura 2, altura 15, duração 100 ticks, custo 40,
   dano base 18 e cooldown 400 ticks.
2. O cast calcula direção horizontal pelo yaw, sem inclinação vertical, e origem
   um bloco à frente. A área guarda direção/origem, permanecendo fixa durante cinco segundos.
3. `TechniqueGeometry.wallBounds` calcula a caixa de consulta; `intersectsWall` usa
   projeções de eixos no plano horizontal e intervalo vertical para testar o volume
   orientado contra o AABB do alvo. Uma muralha diagonal não atinge os cantos externos.
4. `damageArea` verifica contato e linha de visão. `nextDamage` limita cada alvo a
   um impacto por 20 ticks; o primeiro contato já é verificado no cast.
5. Em mobs com dano aceito, aplica knockback lateral de força 0,25 para o lado mais
   próximo, além de fogo por três segundos. Filtros excluem lançador, aliados, NPCs,
   invocações e espectadores; PvP respeita as regras do servidor para essa área.
6. `drawArea` desenha a folha vertical de chamas. O tick expira a área em 100 ticks;
   não existe parede sólida e nenhum bloco é alterado.
7. `RyujinGameTests` e `MvpRegressionTest` verificam contato, exclusão por geometria,
   cobrança única, falta de saldo, orientação diagonal e expiração.

Arquivos alterados: `RyujinTechniqueService.java`, `TechniqueGeometry.java`,
`RyujinGameTests.java`, `MvpRegressionTest.java`, idiomas e documentação do kit.
Relatório: [10](10-revisao-muralha-tornado-morcegos-2026-10-03.md).

## 5. `57966f8`: imponência e movimento do Tornado

1. Raio passou de 5 para 6; altura passou de 8 para 10. Custo 40, dano base 2,
   intervalo de dano 10 ticks, duração 200 e cooldown 300 foram preservados.
2. O centro continua sendo a posição atual do jogador, recalculada no tick.
3. `drawArea` passou a desenhar duas hélices de 48 amostras. A altura vai de 0 a 10;
   o raio visual cresce de 1 a 6 ao subir.
4. `TechniqueGeometry.tornadoAngle` calcula fase `age * 0.3 + sample * 0.5 + arm * π`.
   O avanço é horário visto de cima nas coordenadas horizontais do Minecraft.
5. `sendParticles` com contagem zero transmite uma chama dirigida: vetor tangencial
   `(-sin(angle), 0.18, cos(angle))` e velocidade 0,14. Assim, há deslocamento de
   partículas e avanço da espiral, além da mudança de posição das emissões.
6. Cada terceira amostra adiciona três chamas densas: total 192 partículas por emissão
   a cada dois ticks, contra 64 na versão anterior. Dano não depende dessa quantidade.

Arquivos: `RyujinTechniqueService.java`, `TechniqueGeometry.java`, regressões e
GameTests. A geometria de dano continua cilíndrica, com linha de visão e filtros da área.

## 6. `141205c` e `57966f8`: invocação e correção dos morcegos

1. A entrega inicial cria cinco `Bat` vanilla, com vida máxima 4, `NoAI`, sem gravidade,
   marcadores de proprietário e duração 800 ticks. Custo 25 é cobrado somente após
   criar o enxame completo em posições livres; cooldown normal é 200 ticks.
2. O usuário observou que os morcegos ficavam parados. A versão inicial dependia de
   `setDeltaMovement`, mas `NoAI` não executava o deslocamento normal esperado.
3. Em `57966f8`, `tickSwarm` passou a zerar a velocidade vanilla e chamar
   `bat.move(MoverType.SELF, velocity)` explicitamente, usando colisão vanilla,
   velocidade máxima 0,35/tick e orientação atualizada conforme o voo.
4. A seleção procura `Mob` que implementa `Enemy`, em caixa de oito blocos ao redor
   do dono, com linha de visão. Escolhe o mais próximo de cada morcego. Sem alvo,
   calcula pontos de acompanhamento ao redor do proprietário.
5. O ataque exige distância até 1,5 e linha de visão. Um gate compartilhado por alvo
   limita o enxame a dano base 2 a cada 20 ticks, com fogo por dois segundos.
6. Bat distante mais de 24 blocos pode retornar para um ponto livre perto do dono;
   o movimento é calculado depois desse retorno para não usar um vetor desatualizado.
7. Mortos/removidos saem da lista; limpeza descarta o enxame. O evento de entrada no
   mundo recusa morcegos marcados sem runtime proprietário válido, evitando órfãos.
8. GameTest acompanha ticks reais, compara cada UUID com sua própria posição inicial,
   confirma dano em zumbi e exclusão de animal passivo. Zumbis usam capacete para
   impedir dano solar de falsear a verificação de ataque.

Arquivo principal: `RyujinTechniqueService.java`; integração: `RyujinGameTests.java`.
No modo normal, um enxame vivo impede nova invocação mesmo depois da recarga.

## 7. `5a02b61`: comandos contínuos de cooldown e reiatsu

1. `BleachCommands` registra `cooldowns disable/restore`, `reiatsu free/restore`,
   preserva `cooldowns clear` e mostra os modos em respostas e `inspect`. Todos
   exigem permissão 2 e personagem confirmado; o alvo é um jogador individual.
2. `StatusData.cooldownsDisabled` controla todos os setters de recarga dos quatro
   slots e Flame Burst. Ativar o modo zera recargas; novos casts atribuem zero.
   Restaurar desliga o override: o próximo uso recebe o tempo exato do executor.
3. `ResourcesData.costsDisabled` é consultado por `canAffordReiatsu` e `consumeReiatsu`.
   O modo gratuito aceita saldo zero sem débito, mas continua rejeitando valores
   inválidos. `addReiatsu` ignora drenos negativos enquanto esse modo estiver ativo.
4. `RyujinTechniqueService.ready` e `FormModeHandler` passaram a usar a checagem
   central de custo. `TickHandler` não reverte formas por saldo baixo no modo gratuito;
   ignição continua pelo mesmo consumidor central. Ativação e manutenção ficam gratuitas.
5. `mutateTest` sincroniza sem chamar a normalização de progressão. Isso corrige o
   efeito incidental de `clear` desligar ignição; a limpeza cancela área/enxame/Dash,
   preservando forma, ignição, saldo e progressão.
6. Em modo sem cooldown, recasts de área/enxame substituem a instância anterior,
   sem acumular invocações. Arma, raça, desbloqueio, domínio e limite de pacotes permanecem.
7. Overrides não entram no NBT. `load` e login retornam ao normal; renascimento
   recebe os dados serializados sem overrides. Mudança de dimensão conserva o modo da sessão.
8. GameTests executam comandos pelo dispatcher, com `@s`, validando recasts gratuitos,
   restauração, permissões, ignição e Shikai/Bankai no saldo zero.

Arquivos: [BleachCommands.java](../../src/main/java/com/bleachmod/server/commands/BleachCommands.java),
[ResourcesData.java](../../src/main/java/com/bleachmod/common/data/ResourcesData.java),
`StatusData.java`, `RyujinTechniqueService.java`, `FormModeHandler.java`, `TickHandler.java`
e os dois arquivos de testes. [Instruções de uso](manual-do-jogador.md).

## 8. `5a02b61`: katana e densidade final da Muralha

1. Comparou-se a orientação com Asauchi: Ryujin tinha cabo no extremo oposto do sprite,
   mas as mesmas rotações, incluindo 180° adicionais. O desenho central também era curto.
2. `ryujin_katana_handheld.json` recebeu rotações corrigidas para as duas mãos e
   perspectivas, além de escala de GUI, chão e moldura. Retirou-se a herança de
   `builtin/generated`, que reconstruiria a espada a partir do sprite curto.
3. `ryujin_jakka.json`, `ryujin_jakka_shikai.json` e `ryujin_jakka_bankai.json` receberam
   14 elementos explícitos: cabo, cinco faixas, guarda, corpo/borda da lâmina e ponta.
   Corpo total de 28 unidades de modelo, cerca de 20 de lâmina; não são blocos.
4. UVs amostram cores dos PNGs existentes, sem editar esses arquivos. Selada metálica,
   Shikai com borda vermelha e Bankai escura/rubra; predicates de forma preservados.
5. `drawArea` da Muralha passou de 128 amostras × 3 chamas a cada quatro ticks para
   240 × 5 a cada dois ticks. Espaçamento vertical de um bloco e menor dispersão
   vertical concentram o paredão. Dano, volume, duração, custo e recarga não mudaram.
6. Regressão usa `BlockModel.fromString` do Minecraft para validar as três geometrias,
   faces, limites e herança. Prévia geométrica foi inspecionada; ela não substitui
   a observação da pegada no cliente. O usuário encerrou a feature após essa entrega.

Arquivos de modelos em `src/main/resources/assets/bleachmod/models/item/`:
[pai compartilhado](../../src/main/resources/assets/bleachmod/models/item/ryujin_katana_handheld.json),
[Selada](../../src/main/resources/assets/bleachmod/models/item/ryujin_jakka.json),
[Shikai](../../src/main/resources/assets/bleachmod/models/item/ryujin_jakka_shikai.json),
[Bankai](../../src/main/resources/assets/bleachmod/models/item/ryujin_jakka_bankai.json).
Código de partículas: `RyujinTechniqueService.java`. Teste: `MvpRegressionTest.java`.

## 9. Validação e fechamento

| Entrega | Regressões | GameTests | Resultado |
| --- | --- | --- | --- |
| `141205c` | 43 | 9 | Build aprovado; protótipo inicial |
| `57966f8` | 45 | 11 | Build aprovado; Muralha, Tornado e morcegos revisados |
| `5a02b61` | 49 | 14 | Build aprovado; comandos e modelos finais |

Última execução: `gradlew.bat --offline --no-daemon check build runGameTestServer`;
22 JSONs validados; JAR gerado em `build/libs/bleachmod-0.2.0.jar`.
Nesta entrega exclusivamente documental, conferem-se links, escopo e consistência;
não se reapresentam os testes anteriores como uma nova execução.

Aceite: o usuário confirmou funcionamento da entrega intermediária e, após a entrega
final, informou «concluído». Progresso atualizado para concluído; não há implementação
restante autorizada neste ciclo. Essa confirmação não é evidência de uma medição
formal de desempenho ou de uma bateria multiplayer específica. Tais roteiros ficam
como referência para regressões futuras, sem reabrir a feature aceita.

Protocolo 2.3, schema 3 e dependências foram preservados. `spirit_flame`, vínculo de
Zanpakutō, novas raças jogáveis e sistemas de mentores/facções continuam planejamento
futuro, fora do fechamento da Ryujin. Nenhum commit desta entrega vai para `main`.
