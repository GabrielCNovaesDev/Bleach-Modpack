# Relatório de implementação do MVP — 10/09/2026

## 1. Situação da entrega

**O plano completo ainda não está finalizado.** A implementação principal está no código, mas a validação da última revisão, as correções dos PNGs e os testes visuais/multiplayer continuam pendentes. Este relatório distingue código implementado de comportamento comprovado em jogo.

Escopo mantido: Shinigami, Asauchi, Shikai/Bankai, missões e progressão. Zumbis continuam como inimigos provisórios; o mob Hollow próprio permanece para estudo futuro.

## 2. O que foi implementado

| Área | Alterações no código | Efeito esperado |
|---|---|---|
| Status e compras | Tela K; compra de Zanpakutō e categorias Poder, Reserva e Controle | Unificar consulta e investimento de pontos |
| Categorias | Cinco níveis; custo de 100 × próximo nível; Poder +10% de dano por nível, Reserva +20 de reiatsu, Controle −8% do dreno | Dar utilidade aos pontos sem ampliar para uma árvore extensa |
| Seleção | Radial Z para formas disponíveis | Selecionar a transformação sem ativá-la automaticamente |
| Progressão | Descoberta de formas separada da compra da skill; novos prêmios de despertar com mastery zero | Evitar conceder desbloqueio, compra e domínio máximo juntos |
| Combate | Bônus com Asauchi: Shikai +20%, Bankai +50%, somados ao bônus de Poder | Tornar a transformação útil sem acumular modificadores persistentes |
| Carga | Revalidação da seleção, cancelamento e sincronização; velocidade de 2 a 5 pontos por tick conforme mastery | Impedir saltos indevidos de forma e tentativas repetidas após falhas |
| Economia | Treino básico repetível após receber todas as recompensas; receita de Asauchi | Permitir recuperar pontos e obter outra espada |
| Missões | Assinatura de definição e versão, validação de IDs/limites, recontagem de itens e correção de objetivos sequenciais | Proteger progresso contra alterações incompatíveis e contagem indevida |
| Registros | Snapshots separados por lado lógico; preparação de quests e formas antes da instalação no reload | Evitar interferência entre cliente e servidor e preservar configuração anterior inválida |
| Rede | Direções explícitas dos pacotes; protocolo 2.0; sincronização de formas; aparência separada dos dados privados | Reduzir exposição de dados e inconsistências entre jogadores |
| Persistência | Schema 2, categorias e formas descobertas; normalização de valores; clonagem e reset do estado transitório | Preservar evolução e encerrar cargas em mudanças de ciclo de vida |
| Interface | Diário com paginação/rolagem e botões reativos, HUD redesenhado em código, rastreamento e interpolação visual da carga | Melhorar alinhamento, legibilidade e resposta às atualizações |
| Seleção de personagem | Fundo preenchendo a tela com proporção preservada; confirmação aguarda resposta | Corrigir dimensionamento e fechamento prematuro da tela |
| Desenvolvimento | Comandos `/bleachdev` de pontos, skill, mastery, reiatsu, Asauchi e inspeção; permissão 2 | Facilitar testes controlados |

As regras, os atalhos e os exemplos de comandos estão no [manual do jogador](../jogador/manual-do-jogador.md). A radial não contém técnicas ativas novas: elas ainda não existem neste escopo.

## 3. Verificação executada e seus limites

- Houve compilação e um build completo bem-sucedido com **19 cenários de regressão aprovados** durante a implementação.
- Depois desse build, foram feitos ajustes adicionais e adicionados sete cenários. A suíte atual contém **26 cenários**, mas a execução dessa versão final está pendente.
- Os cenários cobrem compras, saldo, limites, NBT, descoberta, recompensas, alterações estruturais de quests, ordenação e validação de formas. Os sete adicionais incluem repetição de missões, limites de mastery, seleção antiga, preservação de snapshots, provider invalidado e isolamento dos lados lógicos.
- A tarefa `regressionTest` foi ligada ao `check` do Gradle. Testes isolados não substituem teste real do ciclo de vida Forge e multiplayer.
- O código declara versão **0.2.0**. O último build bem-sucedido ocorreu antes dessa mudança; não está comprovada a geração de um JAR 0.2.0 atualizado.
- Não foi concluída uma rodada visual em jogo, nem uma sessão em servidor dedicado com dois clientes.

A tentativa de build da última revisão foi rejeitada pela revisão automática de aprovação por limite de uso da conta. Não foi executado um caminho alternativo para contornar a rejeição. Portanto, não considerar a revisão atual liberada para distribuição.

## 4. Bugs visuais: estado real

| Relato | Estado |
|---|---|
| Background mal dimensionado | Algoritmo de cobertura alterado; conferir em resoluções e escalas GUI diferentes |
| HUD desalinhado e ícone de reiatsu | HUD passou a usar desenho em código; PNG original não foi reparado |
| Marcações do diário | Layout substituído; conferir seleção, paginação e textos longos em jogo |
| Animação da transformação | Carga e interpolação alteradas; fluidez ainda exige teste visual |
| Espadachim com transparência incorreta | PNG original permanece pendente |
| Espada transparente e empunhada ao contrário | Correção final de textura/orientação permanece pendente |

Uma tentativa de geração da textura da espada retornou fundo quadriculado opaco, inadequado para o jogo, e foi descartada. Foi solicitada autorização para ajustar os PNGs existentes por processamento determinístico; essa alternativa ainda não recebeu resposta. Não houve substituição das artes por esse resultado.

## 5. Compatibilidade e documentação

- Os JSONs existentes no mundo não são sobrescritos pelos novos defaults; mundos antigos podem conservar recompensas de skill/mastery anteriores.
- O treino `rukia_basic_training` sem campo `repeatable` passa a ser interpretado como repetível. Um `false` explícito é respeitado.
- Progresso legado sem assinatura é vinculado ao conteúdo no primeiro carregamento atualizado; mudanças anteriores a essa vinculação não podem ser detectadas retroativamente.
- Cliente e servidor precisam da mesma versão de protocolo. Fazer backup antes de migrar mundos/configurações.
- O manual foi reescrito para as regras implementadas. O índice e o plano apontam para este relatório; os documentos de arquitetura distinguem a referência Dragon Mine Z das alterações Bleach.

## 6. Pendências para encerrar o plano

1. Executar o build da revisão atual e os 26 cenários; corrigir eventuais falhas e gerar o artefato correspondente.
2. Resolver os PNGs da espada/personagem e validar empunhadura nas duas mãos, inventário e primeira/terceira pessoa.
3. Testar o fluxo completo em mundo novo e existente: escolher raça, concluir/resgatar quests, comprar skills/categorias, transformar e repetir treino.
4. Testar morte/respawn, reconexão, mudança de dimensão, rastreamento e reload válido/inválido com dois clientes em servidor dedicado.
5. Validar diário, status e radial com escalas GUI variadas, textos longos e estados sem saldo ou sem desbloqueio.
6. Revisar os critérios restantes do plano, incluindo feedback contextual de ações, otimização do resgate múltiplo e limites de sincronização. Não foram marcados como concluídos apenas pela existência das telas.

## 7. Critério de conclusão

Encerrar a entrega quando o build atual passar, os problemas visuais relatados forem conferidos em jogo e os fluxos de persistência/multiplayer forem exercitados. Atualizar este relatório com evidências e resultado de cada pendência. O [plano de implementação](plano-implementacao-mvp.md) permanece como backlog detalhado, e não como lista de funcionalidades já homologadas.
