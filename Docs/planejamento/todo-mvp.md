# Checklist de pendências do MVP Shinigami

Atualizado em 10/09/2026. Esta lista registra o que falta após a implementação principal. Uma funcionalidade existente no código não significa que já foi validada visualmente ou em multiplayer.

## 1. Correções ainda a implementar

- [ ] **Corrigir a transparência das três espadas:** Asauchi, Shikai e Bankai. Preservar transparência do fundo e tornar lâmina/cabo opacos.
- [ ] **Corrigir a orientação da espada na mão.** Conferir as três formas, mão principal/secundária e primeira/terceira pessoa.
- [ ] **Corrigir o alfa do espadachim na seleção de personagem.** Preservar o recorte sem tornar o corpo translúcido.
- [ ] **Revisar os assets que continuam em uso:** especialmente ícones e toasts. O HUD principal foi redesenhado em código; isso não reparou os PNGs originais.

As tentativas de geração da espada não produziram transparência real e foram descartadas. A alternativa de editar os PNGs por código está aguardando resposta do usuário; não tratar essa edição como já autorizada ou executada.

## 2. Validação técnica pendente

- [ ] Concluir a execução dos dois GameTests adicionados: clonagem após invalidação da capability e resgate em lote sem duplicação. A execução foi iniciada em `run-gametest`, separada dos mundos existentes.
- [ ] Corrigir eventuais falhas desses testes de integração.
- [ ] Executar o build após a última alteração e confirmar o JAR 0.2.0 correspondente ao código final. Os **28 cenários de regressão passaram em um build anterior aos últimos ajustes de tick/GameTests/protocolo**.
- [ ] Testar servidor dedicado com dois clientes: aparência das formas, início de rastreamento, reconexão, morte/respawn e mudança de dimensão.
- [ ] Testar reload válido e inválido durante uma missão ativa e confirmar preservação do estado anterior em erro.
- [ ] Testar resgate em lote sob cliques repetidos/latência e confirmar entrega única, incluindo recompensas de item e transformação.
- [ ] Verificar permissões dos comandos de desenvolvimento com operador e jogador comum.

## 3. Validação do jogo e acabamento

- [ ] Jogar o ciclo completo em mundo novo: escolha de raça → quests → compra da skill → Shikai → mastery → Bankai.
- [ ] Repetir o fluxo em mundo existente, preservando pontos, categorias, mastery e prêmios já recebidos.
- [ ] Confirmar que o treino repetível recupera a economia quando os pontos são gastos em categorias antes das skills.
- [ ] Conferir receita e reposição de Asauchi.
- [ ] Conferir seleção de personagem, diário, status e radial em diferentes resoluções e escalas GUI.
- [ ] Conferir textos longos, paginação com mais de cinco missões, rolagem e tooltips de ações bloqueadas.
- [ ] Conferir HUD, animação da carga, cancelamento e sequência dos toasts em jogo.
- [ ] Fazer ajustes de balanceamento somente se o teste revelar necessidade; atualizar as regras na documentação junto da mudança.

## 4. Documentação e fechamento

- [ ] Atualizar o relatório de implementação com os resultados finais. O bloqueio antigo de build por limite de uso foi superado; ele não descreve mais a situação atual.
- [ ] Atualizar as referências ao protocolo de rede para **2.1**, necessário após introduzir o resgate em lote.
- [ ] Documentar o resgate em lote, os tooltips, a validação adicional dos pré-requisitos de formas e os GameTests.
- [ ] Revisar as referências antigas a “19/26 testes” e distinguir resultados históricos do resultado final.
- [ ] Marcar os critérios T01–T24 do plano com evidência de conclusão, mantendo validações manuais não executadas explicitamente pendentes.
- [ ] Registrar o caminho do JAR final e um roteiro curto para teste pelo usuário.

## 5. Já implementado — não refazer

- [x] Tela de status (K), compra de skills e categorias Poder/Reserva/Controle.
- [x] Seletor radial de formas (Z).
- [x] Comandos de desenvolvimento para pontos, skill, mastery, reiatsu, Asauchi e inspeção.
- [x] Separação entre descoberta de formas, compra de skill e mastery; benefícios de combate.
- [x] Treino repetível e receita de Asauchi.
- [x] Diário com paginação/rolagem, HUD em código e cobertura proporcional do background.
- [x] Correções de rede, persistência, rastreamento, carga, objetivos e validação de conteúdo.
- [x] Resgate em lote com um pedido e sincronização ao final, mantendo controle individual das recompensas.
- [x] Tooltips de saldo insuficiente/saldo restante e restrição da sequência de formas.
- [x] Rejeição de pré-requisitos circulares, posteriores ou com mastery inalcançável.
- [x] Conferência de assinatura também nas rotas de atualização e conclusão de objetivos ITEM.

## 6. Fora desta entrega

- Mob Hollow próprio: estudar depois; continuar com zumbis neste MVP.
- Novas raças, dimensões, NPCs complexos e sistemas de grupo.
- Catálogo de técnicas ativas: integrar ao radial quando houver técnicas reais; não criar opções fictícias.
- Respec e expansão extensa da árvore de habilidades.

Referências: [plano detalhado](plano-implementacao-mvp.md), [relatório](relatorio-implementacao-mvp-2026-09-10.md) e [manual](../jogador/manual-do-jogador.md).
