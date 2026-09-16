# Plano de implementação — Kit Ryūjin Jakka

## Decisão geral

A técnica `flame_burst` foi validada em jogo como primeiro protótipo. O próximo ciclo deve evoluir para um kit de quatro slots, com uma variante base e uma variante de Bankai por slot. A implementação será incremental. Cada habilidade será entregue, compilada e testada antes da próxima.

A mesma tecla sempre representa o mesmo slot. O servidor resolve a variante com base na forma ativa. O cliente não escolhe o ID da técnica, o dano, o alcance, o custo ou o cooldown.

## Decisões fechadas para este ciclo

| Tema | Decisão |
| --- | --- |
| Técnica base | Pode funcionar com forma Selada ou Shikai. |
| Técnica evoluída | Só funciona com Bankai ativo do conjunto Ryūjin Jakka. |
| Slots | F1, F2, F3 e F4, todos remapeáveis. |
| Rede | Substituir intenção livre por `ExecuteTechniqueSlotC2S(slot)`. |
| Cooldown | Compartilhado por slot entre base e Bankai. Transformar não reinicia cooldown. |
| Dano | Calculado integralmente no servidor. |
| Efeitos | Partículas e sons primeiro; animação óssea fica fora deste ciclo. |
| F4 evoluído | Apenas dano em entidades; não quebra blocos. |
| F1 base | Exige a Zanpakutō do mod na mão principal. |
| Temporizador de Bankai | Reversão de um degrau: Bankai para Shikai/base, nunca diretamente para Selada. |
| Cooldown na reversão | Continua contando após a reversão. |

A exigência da Zanpakutō na F1 base reforça a identidade do conjunto e evita que qualquer espada vanilla ative o kit.

## Ordem de execução

### Entrega 1 — F1 base: Ignição

A F1 base será o próximo slice. Ela será um toggle ligado/desligado, sem cooldown de ativação. Enquanto estiver ligada, aplicará um bônus de dano de Zanjutsu e fará golpes corpo a corpo aplicarem fogo curto ao alvo.

O estado do toggle será autoritativo no servidor. O custo será um drain contínuo pequeno de reiatsu. Ao chegar a zero, a ignição será desligada automaticamente. O visual inicial será glint vermelho no Asauchi e partículas esparsas nos golpes.

Antes da implementação, será necessário verificar o item usado para representar a Zanpakutō, o cálculo atual de dano corpo a corpo e o ponto correto para aplicar fogo somente quando o toggle estiver ativo.

### Entrega 2 — F1 Bankai: Dash de chamas

O dash usa a direção de visão do jogador, duração curta e trilha intensificada de partículas. O servidor verifica colisão com blocos antes de aplicar cada deslocamento. Entidades vivas no volume percorrido recebem dano de contato uma única vez por ativação, com fogo curto. A habilidade usa a rotação automática nativa do Minecraft como aproximação visual do Riptide, sem introduzir GeckoLib nesta etapa. O custo é fixo e o cooldown é médio.

A invulnerabilidade a knockback externo não será implementada na primeira versão, salvo se o teste demonstrar um problema claro.

### Entrega 3 — F2 base: Rajada curta

Será um cone curto de aproximadamente 3 blocos e 60 graus. O cálculo do cone ocorrerá no servidor. A rajada aplicará dano e fogo curto sem tocar blocos.

### Entrega 4 — F2 Bankai: Leque de fogo

Será o mesmo arquétipo do F2 base, com alcance aproximado de 8 a 10 blocos e dano maior. Não colocará fogo no terreno, porque essa responsabilidade ficará concentrada nas habilidades F3 e F4 base.

### Entrega 5 — `spirit_flame`

Antes das habilidades que alteram o terreno, será criado o bloco próprio `bleachmod:spirit_flame`. Ele não herdará `FireBlock` e não terá propagação vanilla.

Cada posição criada por uma habilidade será registrada com proprietário, origem e expiração em `SavedData` de mundo. O sistema deverá restaurar o registro após reinício e limpar entradas órfãs no boot. A remoção deverá restaurar apenas posições criadas pelo mod que ainda contenham o bloco customizado; nunca poderá sobrescrever um bloco colocado posteriormente pelo jogador.

### Entrega 6 — F3 base: Leque médio e ignição de terreno

Será um cone de aproximadamente 6 blocos e 90 graus. Além do dano, colocará `spirit_flame` nas posições elegíveis. A habilidade só será ligada depois de o bloco customizado possuir testes de expiração, reinício e proteção contra propagação.

### Entrega 7 — F3 Bankai: Círculo de chamas

Será uma área circular de 360 graus com raio aproximado de 5 a 6 blocos. O dano será reduzido em relação ao golpe linear para equilibrar a cobertura total. A primeira versão não terá anel de terreno até o scheduler do `spirit_flame` estar homologado.

Uma regra de PvP e grupo deverá ser definida antes do teste multiplayer. Até essa regra existir, a documentação deve marcar que jogadores aliados podem ser atingidos.

### Entrega 8 — F4 base: Muralha de fogo

Será uma linha ou arco de `spirit_flame` com 7 a 9 blocos de comprimento e 2 de altura, ativa por 15 segundos. O cast deverá guardar exatamente as posições criadas para remover apenas suas próprias instâncias.

### Entrega 9 — F4 Bankai: Corte definitivo

Será um ataque instantâneo em linha estreita, com alcance aproximado de 10 a 12 blocos e abertura de 30 graus. Não quebrará blocos. Ignorará armadura vanilla conforme a regra de design definida, mas manterá Resistência do Bleach quando o alvo for jogador.

O dano e o custo serão os maiores do kit. O visual evitará uma explosão grande de fogo e usará partículas discretas, distorção de calor se viável e som grave.

## Arquitetura prevista

```
ExecuteTechniqueSlotC2S(slot 1..4)
        ↓
TechniqueService
        ↓
forma ativa → BASE ou BANKAI
        ↓
TechniqueRegistry.resolve(slot, tier)
        ↓
CooldownState por slot
        ↓
validação de reiatsu, item, alcance e alvo
        ↓
aplicar dano/efeito no servidor
        ↓
feedback privado + efeito público + sync de cooldown
```

A implementação deve substituir gradualmente o uso da mensagem livre `ExecuteTechniqueC2S`. O pacote antigo pode permanecer temporariamente por compatibilidade durante a migração, mas não deve continuar sendo o caminho de produção após a primeira entrega por slots.

## Regras de servidor

O servidor sempre decide:

- se o personagem está vivo e criado;

- qual é a forma ativa;

- se o slot possui variante disponível;

- se o jogador possui a Zanpakutō exigida;

- se há reiatsu suficiente;

- se o cooldown terminou;

- quais entidades estão na área;

- quais entidades já foram atingidas durante a ativação atual do Dash;

- quais blocos podem receber `spirit_flame`;

- qual dano e qual duração serão aplicados.

O cliente envia somente o número do slot e recebe o resultado autorizado.

## Critérios de cada entrega

Uma entrega só será considerada pronta quando:

1. compilar com Java 17 e Forge 47.4.10;

1. funcionar em cliente de desenvolvimento;

1. respeitar cooldown e custo no servidor;

1. não confiar em alcance ou dano enviados pelo cliente;

1. possuir feedback ao jogador;

1. possuir teste de falha, como falta de reiatsu ou forma incompatível;

1. atualizar `Docs/poderes/progresso.md`;

1. atualizar o manual do jogador quando controles ou comportamento mudarem;

1. registrar arquivos criados e editados;

1. manter o código anterior funcional.

## Riscos de maior prioridade

| Risco | Mitigação |
| --- | --- |
| Blocos temporários permanecerem após reinício | `SavedData`, limpeza no boot e remoção condicional. |
| Técnica base ser liberada durante Bankai ou vice-versa | Resolução de variante no servidor em cada execução. |
| Transformação reiniciar cooldown | Cooldown indexado por slot, não por ID da variante. |
| Área ser calculada pelo cliente | Cliente envia apenas slot. |
| Fogo vanilla espalhar | Bloco customizado sem herança de `FireBlock`. |
| AOE atingir aliados sem intenção | Definir regra de PvP antes das habilidades circulares. |
| Kit crescer antes de haver observabilidade | Entregar uma habilidade por ciclo, com logs e testes manuais. |

## Próxima ação

A próxima implementação será exclusivamente a **F1 base — Ignição**. Nenhum código de `spirit_flame`, F3 ou F4 será criado nessa etapa.