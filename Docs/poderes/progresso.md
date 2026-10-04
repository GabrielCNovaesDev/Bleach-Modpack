# Ciclo de acabamento — 04/10/2026

O kit anterior continua encerrado. Nova revisão implementada e validada: katana de gume único/UVs completos, liberação/aura, Muralha 18 × 2 × 8 e emissão 82% menor, política de alvos e HUD. 51 regressões/16 GameTests passaram; aguarda homologação visual desta versão. Arquivos e sequência no [relatório 13](13-polimento-katana-liberacao-muralha-2026-10-04.md).

1. Modelos: 22 elementos, quatro seções curvas, um fio fino e dorso espesso; superfícies imagegen próprias de Shikai/Bankai.
2. `RyujinReleaseEffects`: evento de forma produz pulso de chamas/fumaça/som; aura esparsa enquanto válido. Sem terreno/dano.
3. `RyujinTechniqueService`: dimensões 18/2/8, 432 chamas por emissão/4 ticks; valores de combate preservados.
4. `TechniqueTargets`, executores e `CombatEvents`: filtros unificados e aplicação de fogo condicionada ao dano.
5. `StatusData`, rede/client/tick e `ReiatsuHud`: snapshot do proprietário mostra recargas sem persistir em NBT.
6. Regressões e GameTests cobrem geometria, UVs, HUD, transições de liberação, alcance/altura/dano rejeitado e Leque/aliados.

Próxima etapa separada: contrato e fundação de fogo ambiental persistente; não existe incêndio por ativar Bankai neste JAR. Vínculo segue fora do ciclo. Nenhuma alteração em documentos de outras pastas.

---
> **Revisão vigente — 04/10/2026:** ciclo de acabamento autorizado após o kit anterior: katana de um gume/texturas completas, liberação/aura, Muralha 18 × 2 × 8, alvos unificados e HUD de recargas. Build, 51 regressões e 16 GameTests aprovados; visual desta revisão para homologação no cliente. [Implementação passo a passo e arquivos](13-polimento-katana-liberacao-muralha-2026-10-04.md). Fogo ambiental é etapa seguinte, ainda não implementada. O histórico abaixo preserva as entregas anteriores.

# Feature Ryūjin Jakka concluída — 03/10/2026

O usuário confirmou a conclusão após a entrega `5a02b61`, na branch `Feature-Poderes-bankais`. Nenhuma implementação resta neste ciclo. Última validação de código: build, 49 regressões e 14 GameTests aprovados; 22 JSONs válidos. A atualização atual é exclusivamente documental, limitada a `Docs/poderes`.

## Progresso por feature e entrega

| Etapa | Alteração e lógica | Arquivos principais | Estado |
| --- | --- | --- | --- |
| Base existente | H/N resolvidos pela forma servidor; X preservado; Ryujin autoriza o kit | `TechniqueService`, `StatusData`, `TickHandler` | Reaproveitada |
| `141205c` — B/C | Runtime por UUID para área/enxame, expiração e limpeza; cilindro filtra a caixa de consulta | `RyujinTechniqueService`, `TechniqueGeometry`, `RyujinGameTests` | Concluída; Círculo posteriormente substituído |
| `141205c` — recargas/Corte | Slot 3 incluído; mudança de forma preserva cooldown; Corte dano 48/custo 45/60 s | `StatusData`, `FormModeHandler`, `ProgressionService`, `TechniqueService` | Concluída |
| `57966f8` — Muralha | Volume orientado 16 × 2 × 15, fixo por 5 s; contato com intervalo e knockback | `RyujinTechniqueService`, `TechniqueGeometry`, testes | Concluída |
| `57966f8` — Tornado | Raio 6/altura 10, duas hélices com fase horária e velocidade tangencial | `RyujinTechniqueService`, `TechniqueGeometry` | Concluída |
| `57966f8` — morcegos | NoAI com movimento explícito e colisão; alvo `Enemy`; ataque compartilhado por alvo | `RyujinTechniqueService`, `RyujinGameTests` | Concluída |
| `5a02b61` — comandos | Overrides individuais de recarga/custo; restauração das regras; manutenção gratuita das formas | `BleachCommands`, `StatusData`, `ResourcesData`, `FormModeHandler`, `TickHandler` | Concluída |
| `5a02b61` — modelos/visual | 14 elementos de katana, orientação das duas mãos, variantes; Muralha 1.200 chamas/2 ticks | Quatro modelos JSON de Ryujin, `RyujinTechniqueService`, regressões | Entregue; feature encerrada pelo usuário |

## Sequência da implementação e verificação

1. Compararam-se documentos e executores, separando planejamento antigo do contrato autorizado.
2. Criou-se o runtime B/C e preservaram-se recargas por slot nas transformações; consolidou-se o Corte.
3. Após teste do usuário, substituiu-se Círculo, ampliou-se Tornado e corrigiu-se voo/ataque dos morcegos.
4. Implementaram-se comandos contínuos por jogador e sua restauração, incluindo ignição e formas.
5. Corrigiram-se orientação/modelos das três formas e intensificaram-se partículas sem mudar dano.
6. Cada entrega de código passou por build/regressões/GameTests: 43/9, depois 45/11, finalmente 49/14.
7. O usuário encerrou a feature; documentação desta pasta atualizada com lógica, arquivos e histórico.

Detalhes por passo: [histórico técnico completo](12-historico-tecnico-passo-a-passo-ryujin.md). Referências: [contrato](08-contrato-ryujin-circulo-tornado-morcegos.md), [relatório final](11-fechamento-ryujin-comandos-modelos-2026-10-03.md) e [comandos](manual-do-jogador.md).

O aceite geral não registra uma bateria multiplayer formal ou medição de desempenho específica. Esses roteiros permanecem referências para regressões futuras, sem reabrir a feature. `spirit_flame`, vínculo, outras raças e mentores seguem escopo futuro separado.

# Estado operacional — 03/10/2026

Consulte o [contrato atual de Ryūjin Jakka](08-contrato-ryujin-circulo-tornado-morcegos.md). O kit atual usa B para Muralha/Tornado e C para Morcegos/Corte. Corte: dano base 48, custo 45, cooldown 60 segundos, 100 blocos, 25° e destruição sem drops. Branch: Feature-Poderes-bankais. Estado: concluída por confirmação do usuário.

## Registro histórico abaixo — valores e próximas tarefas anteriores não são o contrato vigente

# Progresso do sistema de poderes

## Estado atual

- Branch: `Feature-Bankais-Poderes`

- Gate atual: F1 base, F1 Bankai, F2 base e F2 Bankai homologadas; protótipo do F4 Bankai implementado e aguardando homologação

- Próxima tarefa: compilar e homologar exclusivamente o F4 Bankai — Corte de Vapor Concentrado

- Última atualização: 2026-09-20

```bash
.\gradlew.bat clean
./gradlew runClient
```

## Gates

| Gate | Estado | Evidência |
| --- | --- | --- |
| Pesquisa inicial | CONCLUÍDO | `00-auditoria-snapshot-2026-09-14.md` |
| Visão aprovada | CONCLUÍDO | documentos de game design existentes |
| Contrato de técnica | IMPLEMENTADO COMO PROTÓTIPO | `05-tecnica-piloto-flame-burst.md` |
| Arquitetura | PROTÓTIPO LOCALIZADO | `common/technique/TechniqueService.java` |
| Técnica piloto | IMPLEMENTADA | `flame_burst` |
| Servidor autoritativo | IMPLEMENTADO | `ExecuteTechniqueC2S` + `TechniqueService` |
| Kit Ryūjin Jakka | ESPECIFICADO | `pwr-spec-02-kit-ryujin-jakka.md` recebido e `06-plano-kit-ryujin-jakka.md` |
| F1 base — Ignição | IMPLEMENTADA | `TechniqueService` + `CombatEvents`; homologação visual concluída pelo usuário |
| F1 Bankai — Dash de chamas | HOMOLOGADA | `TechniqueService.tickFlameDash`; múltiplos alvos, dano único por alvo, alcance, colisão e cancelamentos verificados em jogo |
| F2 base — Rajada curta | HOMOLOGADA COM AJUSTE FINAL | `TechniqueService.executeFlameBarrage`; cone server-side de 8 blocos/60 graus, contato, fogo e partículas persistentes sem alteração de blocos |
| F2 Bankai — Leque de fogo | HOMOLOGADA | `TechniqueService.executeFlameFan`; leque server-side de 14 blocos/90 graus, dano maior, partículas azuis densas e dano persistente no solo sem alteração de blocos |
| F4 Bankai — Corte de Vapor Concentrado | AJUSTADO — AGUARDANDO NOVO TESTE | `TechniqueService.executeConcentratedSteamCut`; primeiro segmento à frente do jogador, lâmina vertical de 35 blocos, alcance de 100 blocos/45 graus, vapor intensificado, explosão secundária e cooldown zerado temporariamente |
| Bloco `spirit_flame` | PENDENTE | Deve preceder F3/F4 base |
| F3/F4 base e Bankai | PENDENTE | — |
| Sincronização multiplayer | PARCIAL | efeitos server-side verificados; teste formal com dois clientes ainda pendente |
| Build/compileJava | APROVADO NO SNAPSHOT | `BUILD SUCCESSFUL`; build e testes executados após a atualização do Dash |
| GameTests | PENDENTE | — |
| Teste visual | PENDENTE | — |

## Trabalho realizado

### 2026-09-14 — Flame Burst

- Objetivo: criar uma técnica independente de Bankai para validar o primeiro contrato de habilidade ativa.

- Custo: 20 de reiatsu.

- Cooldown: 15 segundos/300 ticks.

- Área: raio provisório de 3 blocos.

- Dano: 6 pontos provisórios.

- Feedback: partículas FLAME/LAVA e som BLAZE_SHOOT.

- Controle: X, remapeável.

- Arquivos alterados: serviço, pacote C2S, estado de status, tick, keybinds, registro de rede, traduções e documentação.

- Estado: implementado no snapshot; build tentou iniciar, mas falhou antes do `compileJava` ao resolver `cpw.mods:bootstraplauncher:1.1.2` por erro TLS no Maven Forge.

### 2026-09-16 — Dash de chamas: contato e visual Riptide

- O Dash continua autoritativo no servidor e usa somente a direção de visão do jogador.

- Cada ativação pode atingir cada entidade viva no máximo uma vez; o alvo precisa estar no volume percorrido pelo jogador.

- Dano provisório: 4 pontos por entidade, mais 2 segundos de fogo.

- Duração ampliada para 16 ticks a 1,25 bloco/tick, com distância teórica aproximada de 20 blocos quando não há colisão.

- O efeito visual usa `startAutoSpinAttack`, trilha densa de `FLAME`, `SOUL_FIRE_FLAME` e `LAVA`, além do som de Blaze.

- Colisão com blocos continua interrompendo o movimento.

- Testes manuais aprovados: múltiplos alvos, um dano por alvo, distância aproximada, colisão com terreno e parede, perda da Bankai, morte durante o Dash e custo cobrado uma única vez.

- Limitação conhecida: no chão, a direção horizontal pode fazer o Dash encerrar ou prender no primeiro bloco do terreno; o uso aéreo oferece mira mais eficiente. O polimento da movimentação terrestre fica deliberadamente fora deste ciclo.

- Estado: HOMOLOGADO em jogo; polimento futuro aberto, sem bloquear o avanço para F2.

### 2026-09-16 — F2 base: Rajada curta de chamas

- Slot: 2, tecla N; o cliente envia somente o número do slot.

- Disponível em forma Selada ou Shikai; recusada durante Bankai, que permanece reservada para a variante evoluída do slot.

- Requer Asauchi na mão principal, custa 15 de reiatsu e possui cooldown próprio de 4 segundos.

- Cone calculado exclusivamente no servidor, inicialmente com alcance de 3 blocos e abertura total de 60 graus.

- Cada entidade viva dentro do cone recebe 4 pontos de dano e fica em chamas por 3 segundos.

- Partículas de chama são projetadas sobre a superfície do terreno para indicar o cone; nenhum bloco é colocado, substituído ou alterado.

- Estado: implementação concluída; compilação e homologação manual pendentes.

### 2026-09-18 — F2 Bankai: Leque de fogo

- O slot 2 agora resolve para a variante Bankai quando a forma ativa é Bankai; Selada/Shikai continuam usando a Rajada curta.

- A variante exige `ModItems.RYUJIN_JAKKA` na mão principal e usa o cooldown compartilhado do slot 2.

- Alcance: 14 blocos. Abertura total: 90 graus.

- Custo: 25 de reiatsu. Dano direto: 8 pontos. Fogo: 4 segundos.

- Cada alvo válido é atingido no máximo uma vez por ativação; contato imediato é aceito.

- Partículas de trajetória e impacto são server-side. Nenhum bloco é colocado, substituído ou alterado.

- Estado: implementação inicial concluída; polimento visual e dano persistente pendentes.

### 2026-09-18 — Polimento do Leque Bankai: chamas azuis persistentes

- O Leque passou a usar exclusivamente `SOUL_FIRE_FLAME`, removendo partículas vermelhas e fumaça do ataque.

- A densidade foi ampliada nos impactos, na trajetória e nas posições de superfície.

- O estado do solo é exclusivo da Bankai e fica separado das posições da Rajada curta.

- As posições permanecem ativas por 4 segundos, reaplicam 1 ponto de dano a cada 10 ticks e recebem partículas azuis densas a cada 2 ticks.

- Nenhum bloco é colocado, substituído ou alterado.

- Estado: ajuste implementado; compilação e nova homologação manual pendentes.

### 2026-09-20 — F4 Bankai: Corte de Vapor Concentrado

- O slot 4, tecla C, agora resolve para a habilidade apenas quando a forma ativa é Bankai e a Ryūjin Jakka está na mão principal.

- O protótipo é instantâneo, server-side, com alcance de 30 blocos, abertura de 30 graus, 16 pontos de dano, custo de 45 de reiatsu e cooldown próprio de 15 segundos.

- O golpe usa dano mágico indireto para ignorar armadura vanilla enquanto o evento de combate mantém a Resistência do Bleach aplicável aos jogadores.

- A trajetória atravessa paredes e destrói blocos comuns em volume estreito, sem gerar drops. Bedrock, blocos de comando e blocos indestrutíveis são preservados.

- O visual usa `SOUL_FIRE_FLAME`, `CLOUD`, `END_ROD`, `CRIT` e som do Ender Dragon. O carregamento curto fica fora da primeira versão.

- Estado: implementado; compilação e homologação manual pendentes.

### 2026-09-17 — Ajuste da F2 base após migração Ryūjin Jakka

- A exigência da arma permanece `ModItems.RYUJIN_JAKKA`; a Asauchi não autoriza a técnica.

- Naquele estado anterior, a Bankai não havia sido alterada e o slot 2 ainda estava indisponível; a variante Bankai foi implementada posteriormente em 2026-09-18.

- Alcance ampliado para 8 blocos e abertura total para 80 graus.

- Contato à queima-roupa incluído por tolerância geométrica, removendo a zona morta observada no primeiro bloco.

- Partículas de `FLAME`, `SOUL_FIRE_FLAME` e `SMOKE` foram intensificadas ao longo do cone.

- As posições de superfície ficam ativas por 3 segundos; durante esse período reaplicam partículas e dano de 1 ponto a cada 10 ticks, sem alterar blocos.

- Estado: implementação ajustada; compilação e homologação manual pendentes.

## Decisões fechadas em 2026-09-15

1. O kit terá quatro slots, F1–F4, com variantes base e Bankai.

1. O cliente enviará somente o slot; o servidor resolverá a variante pela forma ativa.

1. F1 base exigirá a Zanpakutō do mod na mão principal.

1. F4 Bankai não quebrará blocos.

1. A reversão temporizada de Bankai descerá um degrau e manterá cooldowns.

## Decisões ainda pendentes

1. Definir regra de dano entre aliados e jogadores em grupo; a versão atual atinge qualquer entidade viva no contato.

1. Definir os valores finais de dano, custo e alcance de cada técnica.

1. Definir o indicador visual de cooldown.

1. Decidir quando as técnicas aparecerão em radial ou slots dedicados.

## Riscos conhecidos

| ID | Risco | Impacto | Mitigação | Estado |
| --- | --- | --- | --- | --- |
| PWR-001 | Dano em área atingir aliados ou outros jogadores sem regra definida. | Alto | Definir regra antes da F3 Bankai. | Aberto |
| PWR-002 | Assinatura de API de dano ou partículas divergir no Forge local. | Alto | Executar `./gradlew build` e corrigir compilação. | Controlado — build aprovado |
| PWR-003 | Cooldown não possuir feedback visual contínuo. | Médio | Adicionar HUD após validar o serviço. | Aberto |
| PWR-004 | Efeitos públicos não serem percebidos por observadores. | Médio | Testar dois clientes e adicionar pacote público se necessário. | Aberto |
| PWR-005 | Técnica estar documentada como aceita antes do teste no jogo. | Médio | Manter estado como implementado/não homologado. | Controlado — F1 homologada |
| PWR-006 | `spirit_flame` permanecer após reinício ou sobrescrever bloco do jogador. | Alto | `SavedData`, limpeza no boot e remoção condicional. | Aberto |
| PWR-007 | Cooldown base ser resetado ao entrar em Bankai. | Alto | Estado indexado pelo slot. | Planejado |

| PWR-008 | Dash no chão prender no primeiro bloco do terreno e dificultar a mira horizontal. | Médio | Manter como limitação conhecida; polir movimento terrestre antes de uma futura revisão de F1. | Aberto, não bloqueante |

### 2026-09-20 — Ajuste da lâmina vertical para testes

- A origem deixou de usar a posição dos olhos e passou a usar a posição corporal do jogador.

- A trajetória agora começa no corpo, avança desde o passo zero e mantém 30 blocos de alcance na direção exata da visão.

- A área vertical foi ampliada para 15 blocos abaixo e 20 blocos acima da altura corporal do jogador, totalizando 35 blocos.

- A densidade de `SOUL_FIRE_FLAME` foi reduzida e a de `CLOUD` foi aumentada para priorizar o vapor visual.

- O cooldown do Corte está temporariamente em zero para permitir testes repetidos; o custo de 45 de reiatsu permanece ativo.

- Estado: ajuste implementado; novo teste manual pendente.
