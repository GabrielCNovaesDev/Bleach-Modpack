# Checklist da próxima entrega — F1 base: Ignição

## Objetivo

Criar a primeira técnica do kit Ryūjin Jakka. A técnica será um toggle que funciona em Selada ou Shikai e não funciona com Bankai ativo, porque nesse estado o slot F1 será resolvido para o Dash de chamas.

## Comportamento obrigatório

- [ ] Ativação e desativação pelo slot F1.

- [ ] Servidor resolve a variante pela forma ativa.

- [ ] Exige personagem criado e jogador vivo.

- [ ] Exige Asauchi/Zanpakutō do mod na mão principal.

- [ ] Não aceita qualquer espada vanilla.

- [ ] Aplica bônus de dano em Zanjutsu enquanto ativa.

- [ ] Golpes corpo a corpo aplicam fogo curto ao alvo.

- [ ] Possui drain contínuo pequeno de reiatsu.

- [ ] Desliga sozinha quando a reiatsu chega a zero.

- [ ] Possui partículas esparsas nos golpes.

- [ ] Possui glint visual vermelho no item ou alternativa equivalente compatível com a renderização atual.

- [ ] Não cria bloco.

- [ ] Não possui animação óssea.

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

A entrega só passa quando compilar com Java 17, funcionar em cliente de desenvolvimento e tiver os comportamentos obrigatórios verificados manualmente. O progresso e o manual do jogador devem ser atualizados na mesma entrega.