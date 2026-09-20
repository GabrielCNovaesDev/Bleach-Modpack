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