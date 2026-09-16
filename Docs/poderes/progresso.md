# Progresso do sistema de poderes

## Estado atual

- Branch: `Feature-Bankais-Poderes`

- Gate atual: F1 base e F1 Bankai implementadas; Dash recebeu dano de contato e visual de rotação/trilha intensificada

- Próxima tarefa: homologar o Dash melhorado e corrigir a preservação de cooldown durante transformação

- Última atualização: 2026-09-16

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
| F1 Bankai — Dash de chamas | IMPLEMENTADA / EM HOMOLOGAÇÃO | `TechniqueService.tickFlameDash`; movimento, partículas e dano de contato |
| F2 base — Rajada curta | PENDENTE | — |
| F2 Bankai — Leque de fogo | PENDENTE | — |
| Bloco `spirit_flame` | PENDENTE | Deve preceder F3/F4 base |
| F3/F4 base e Bankai | PENDENTE | — |
| Sincronização multiplayer | PARCIAL | feedback e efeitos emitidos pelo servidor; falta teste com dois clientes |
| Build/compileJava | APROVADO NO SNAPSHOT | `BUILD SUCCESSFUL`; 36 cenários de regressão aprovados; validar oficialmente com Java 17 |
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

- O efeito visual usa `startAutoSpinAttack`, trilha densa de `FLAME`, `SOUL_FIRE_FLAME` e `LAVA`, além do som de Blaze.

- Colisão com blocos continua interrompendo o movimento.

- Estado: código implementado; build e teste manual da nova colisão ainda pendentes.

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
| PWR-002 | Assinatura de API de dano ou partículas divergir no Forge local. | Alto | Executar `./gradlew build` e corrigir compilação. | Aberto |
| PWR-003 | Cooldown não possuir feedback visual contínuo. | Médio | Adicionar HUD após validar o serviço. | Aberto |
| PWR-004 | Efeitos públicos não serem percebidos por observadores. | Médio | Testar dois clientes e adicionar pacote público se necessário. | Aberto |
| PWR-005 | Técnica estar documentada como aceita antes do teste no jogo. | Médio | Manter estado como implementado/não homologado. | Controlado |
| PWR-006 | `spirit_flame` permanecer após reinício ou sobrescrever bloco do jogador. | Alto | `SavedData`, limpeza no boot e remoção condicional. | Aberto |
| PWR-007 | Cooldown base ser resetado ao entrar em Bankai. | Alto | Estado indexado pelo slot. | Planejado |