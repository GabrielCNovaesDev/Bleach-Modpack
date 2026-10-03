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
