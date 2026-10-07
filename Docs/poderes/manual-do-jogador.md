> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

> **Estado vigente — ciclo 20, 06/10/2026:** Ryujin com onda mais densa e tornado três blocos à frente; Hyōrinmaru com quatro slots selados, armadura temporária, hipotermia, criatura de gelo, asas/cauda Bankai, tempestade com restauração e HUD de habilidades. [Lógica, arquivos, valores e homologação](20-refinamento-gelo-clima-hud-2026-10-06.md). A decisão deste ciclo substitui a antiga selada restrita a dois cortes físicos. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

## Novidades de 04/10 para homologação

Ryujin com fio único/dorso espesso e superfícies detalhadas; Shikai em chamas e Bankai carbonizada com fissuras. Entrada em formas liberadas emite anel, fumaça e som; aura acompanha a arma de forma esparsa. Não coloca fogo no mapa.

B base agora forma Muralha 18 × 2 × 8 por 5 s. Custos, dano, cooldown e knockback preservados. O HUD mostra slots 1/2/3/4/X com `OK`, segundos ou `TEST`; servidor continua validando casts. Aliados/equipes, NPCs de quest, invocações e PvP são filtrados em todos os ataques do kit. Corte permanece destrutivo contra terreno comum.

Instale o mesmo JAR novo no cliente e servidor. Teste duas mãos, formas, inventário, liberação, aura, recargas e mundo com dois jogadores. Detalhes no [relatório 13](13-polimento-katana-liberacao-muralha-2026-10-04.md).
# Manual de poderes — referência única

O manual operacional foi consolidado em [Docs/jogador/manual-do-jogador.md](../jogador/manual-do-jogador.md).

Para slots, valores, regras de dano, ciclo de vida e critérios de teste, consulte o
[contrato atual Ryūjin Jakka](08-contrato-ryujin-circulo-tornado-morcegos.md).

Atualizado em 03/10/2026. As versões anteriores deste manual permanecem no histórico Git.

## Fechamento e controles do kit

Feature concluída pelo usuário após `5a02b61`, na `Feature-Poderes-bankais`.
Este complemento mantém comandos e lógica de poderes nesta pasta, sem editar o manual de outra pasta.

| Tecla | Selada / Shikai | Bankai |
| --- | --- | --- |
| H | Ignição | Dash de chamas |
| N | Rajada curta | Leque de fogo |
| B | Muralha de Chamas | Tornado de Chamas |
| C | Cinco morcegos | Corte de Vapor Concentrado |
| X | Flame Burst | Flame Burst |

## Testar sem recarga ou custo, passo a passo

1. Confirme o personagem e equipe Ryujin na mão principal. Comandos exigem operador nível 2; use `@s` ou nome do jogador.
2. Execute `/bleachdev cooldowns disable @s`: zera recargas e efeitos anteriores; novos casts ficam sem espera. Recast de área/enxame substitui a instância anterior.
3. Execute `/bleachdev reiatsu free @s`: dispensa custo/dreno de técnicas, ignição e ativação/manutenção de Shikai/Bankai, inclusive com saldo zero. Não libera formas/skills/domínio.
4. Use `/bleachdev inspect @s` para consultar `cooldowns=disabled` e `reiatsuCosts=free`.
5. Para limpar recargas uma única vez, use `/bleachdev cooldowns clear @s`. Cancela Dash/áreas/morcegos, preservando ignição, forma, saldo, progressão e modos de teste.
6. Restaure com `/bleachdev cooldowns restore @s` e `/bleachdev reiatsu restore @s`. O próximo uso aplica recarga exata do executor; custo/dreno voltam ao normal sem alterar o saldo.
7. Reconectar/reiniciar ou renascer retorna ao modo normal; mudança de dimensão conserva overrides da sessão. Limites de pacotes e requisitos do kit continuam valendo.

## Como esses comportamentos foram implementados

1. `141205c`: `TechniqueService` roteia slots; `RyujinTechniqueService` controla runtime e `StatusData` preserva recargas em transformações.
2. `57966f8`: `TechniqueGeometry` testa parede orientada; o serviço move morcegos com colisão e emite espiral horária do Tornado.
3. `5a02b61`: `BleachCommands` registra comandos; `StatusData` controla setters de cooldown; `ResourcesData` centraliza custos; `FormModeHandler`/`TickHandler` cobrem transformação/dreno. `mutateTest` sincroniza sem desligar a ignição.
4. A katana usa geometria nos três JSONs de forma e transformações em `ryujin_katana_handheld.json`; a Muralha densa é desenhada por `drawArea`. PNGs não foram alterados.

Lógica, arquivos e validação por feature: [histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md). Última suíte: 49 regressões e 14 GameTests aprovados.

## Testar a nova Bankai e iniciar pelo atalho

Feche o cliente anterior e clique duas vezes em Iniciar-Teste.bat, na raiz do checkout/ZIP extraído. É necessário JDK 17; o script limpa build/logs/cache temporário, recompila e abre o cliente. Mundos e configurações permanecem. Ao fechar o jogo, remove o cache temporário. Para somente compilar: Iniciar-Teste.bat -BuildOnly.

Bankai com Ryujin equipada agora acende focos temporários no solo em raio de seis blocos; cada foco dura cinco segundos, não se espalha nem consome blocos. Ao sair da forma, param as emissões e focos existentes expiram. Alvos aliados e PvP respeitam regras do kit. [Regras e testes detalhados](14-fogo-ambiental-inicializador-2026-10-04.md).


## Testes do kit de gelo e onda — ciclo 19

O roteiro completo de comandos, desbloqueios provisórios, troca de identidade e restauração está no [relatório 19](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md#5-roteiro-de-homologação-operador). H/N/B/C são os quatro slots atuais; X foi retirado. A katana de gelo deve corresponder à identidade vinculada. A onda N aparece na Ryujin selada, Shikai e Bankai.

## Revisão autorizada do ciclo 20 — 06/10/2026

A selada passa a ter quatro habilidades básicas de gelo: H/N de alcance seis, B de armadura temporária (25%/6s) e C de controle de alvo único com hipotermia. Isso substitui a proposta anterior de dois cortes sem gelo. Shikai/Bankai recebem mais neve, congelamento, criatura cosmética baseada em Phantom e armadura visual crescente; Bankai inclui asas/cauda e clima com concessão/retorno ao estado anterior. HUD informa habilidades, atalhos reais e requisitos vigentes de formas, sem novos patamares individuais arbitrários. A escala continua 0–100, com história e progressão final sob responsabilidade da equipe. [Implementação e limites do motor vanilla](20-refinamento-gelo-clima-hud-2026-10-06.md).