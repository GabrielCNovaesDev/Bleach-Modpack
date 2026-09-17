# Checklist de homologação — F1 base: Ignição e F1 Bankai: Dash de chamas

## Objetivo

Registrar a homologação da primeira técnica do kit Ryūjin Jakka. A Ignição funciona em Selada ou Shikai e não funciona com Bankai ativo, porque nesse estado o slot F1 é resolvido para o Dash de chamas.

O Dash de chamas também foi homologado em jogo, incluindo múltiplos alvos, dano único por alvo, alcance, colisão e cancelamentos. A mira no chão permanece como limitação conhecida e não bloqueante.

## Comportamento obrigatório

- [x] Ativação e desativação pelo slot F1.

- [x] Servidor resolve a variante pela forma ativa.

- [x] Exige personagem criado e jogador vivo.

- [x] Exige Asauchi/Zanpakutō do mod na mão principal.

- [x] Não aceita qualquer espada vanilla.

- [x] Aplica bônus de dano em Zanjutsu enquanto ativa.

- [x] Golpes corpo a corpo aplicam fogo curto ao alvo.

- [x] Possui drain contínuo pequeno de reiatsu.

- [x] Desliga sozinha quando a reiatsu chega a zero.

- [x] Possui partículas esparsas nos golpes.

- [x] Possui glint visual vermelho no item ou alternativa equivalente compatível com a renderização atual.

- [x] Não cria bloco.

- [x] Não possui animação óssea.

## Questões técnicas para verificar antes de editar

- [ ] Confirmar como o Asauchi é identificado no código.

- [ ] Confirmar onde o dano melee recebe o bônus de Zanjutsu.

- [ ] Confirmar onde o alvo é incendiado sem duplicar lógica de dano.

- [ ] Escolher a chave do estado do toggle.

- [ ] Escolher o drain por tick.

- [ ] Reaproveitar o pacote de slot; não criar novo pacote que aceite ID enviado pelo cliente.

- [ ] Definir o feedback de ativação, desativação, falta de Zanpakutō e falta de reiatsu.

- [ ] Definir se o toggle é limpo ao morrer, desconectar, trocar de dimensão ou reverter.

## Teste manual

1. Com Asauchi na mão e forma Selada, pressionar F1.

1. Confirmar feedback e visual de ignição.

1. Atacar um alvo e confirmar bônus e fogo curto.

1. Manter a técnica ativa até a reiatsu chegar a zero.

1. Confirmar desligamento automático.

1. Tentar ativar sem Asauchi.

1. Tentar ativar com espada vanilla.

1. Ativar em Shikai.

1. Transformar em Bankai e confirmar que F1 passa a representar Dash, não Ignição.

1. Reverter para Shikai e confirmar que F1 volta a representar Ignição.

1. Testar com dois jogadores para confirmar que o dano é calculado no servidor.

## Critério de conclusão

A entrega F1 passa com a compilação aprovada, funcionamento em cliente de desenvolvimento e comportamentos obrigatórios verificados manualmente. O Dash também teve múltiplos alvos, um dano por alvo, distância, colisão, cancelamentos e custo único verificados. A homologação multiplayer formal permanece como etapa adicional de sincronização, não como bloqueio da F2 base.