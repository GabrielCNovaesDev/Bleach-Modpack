# Handoff — Game Design do Mod Bleach

> Documento de transferência para continuar o trabalho de game design em outro chat. Leia este arquivo inteiro antes de propor novas decisões.

## 1. Objetivo do handoff

Este projeto é um mod completo de **Bleach para Minecraft Java Edition**, inspirado na qualidade estrutural de Dragon Mine Z: progressão por quests, NPCs que ensinam habilidades, campanhas alinhadas à história, ranks, treinamento, pontos, mobs, dimensões e multiplayer.

O trabalho atual é de **idealização e especificação de game design**, não de implementação de código. O mod está no início; as decisões estão sendo tomadas por módulos, em blocos pequenos, para evitar um escopo confuso.

O próximo chat deve exercer o papel de coautor/lead de game design: pesquisar o cânone quando necessário, separar fato de proposta, recomendar soluções, discordar quando houver risco de design e só alterar documentos depois que as decisões forem aprovadas.

## 2. Regras de colaboração com o usuário

- Conversar em português claro e natural.
- Não concordar automaticamente: explicar evidências, riscos e trade-offs.
- Dar uma recomendação explícita e os próximos passos em cada etapa.
- Trabalhar incrementalmente; não tentar definir o mod inteiro de uma vez.
- Separar sempre:
  - **CÂNONE**: o que existe em Bleach;
  - **ADAPTAÇÃO**: como isso pode virar mecânica de Minecraft;
  - **DECIDIDO**: aprovado pelo usuário;
  - **PENDENTE/ABERTO**: ainda precisa de decisão;
  - **BACKLOG**: ideia válida, mas fora do bloco atual.
- Não transformar uma ideia em decisão sem aprovação do usuário.
- Não implementar código, criar quests ou fechar números de balanceamento antes de o módulo correspondente ser especificado.
- Preservar alterações existentes. Não usar reset, clean, stash, checkout destrutivo, commit, push ou deploy sem autorização explícita.
- Manter separados os estados **IDEALIZADO, ESPECIFICADO, IMPLEMENTADO, TESTADO, AUDITADO, ACEITO, PENDENTE e NÃO INICIADO**. Documentação de design não prova implementação.

## 3. Local do projeto e estado de trabalho

Repositório principal:

`/Users/tarsopassos/Bleach mobpack/Bleach-Modpack`

Documentos de game design já criados:

- [00-mapa-modular.md](/Users/tarsopassos/Bleach%20mobpack/Bleach-Modpack/Docs/game-design/00-mapa-modular.md)
- [01-visao-e-pilares.md](/Users/tarsopassos/Bleach%20mobpack/Bleach-Modpack/Docs/game-design/01-visao-e-pilares.md)
- [02-trilhas-de-progressao.md](/Users/tarsopassos/Bleach%20mobpack/Bleach-Modpack/Docs/game-design/02-trilhas-de-progressao.md)
- [03-storyline-macro.md](/Users/tarsopassos/Bleach%20mobpack/Bleach-Modpack/Docs/game-design/03-storyline-macro.md)
- [04-racas-e-origens.md](/Users/tarsopassos/Bleach%20mobpack/Bleach-Modpack/Docs/game-design/04-racas-e-origens.md)

Estado conhecido do Git no momento do handoff:

- Branch: `chore/remove-run-gametest`
- `Docs/README.md`: modificado durante a documentação
- `gradlew`: modificação preexistente, não relacionada a este trabalho; preservar
- `Docs/game-design/`: documentos criados neste fluxo, ainda não necessariamente commitados

Antes de editar, faça uma verificação factual de branch, HEAD, status e árvore. Não apague nem sobrescreva trabalho preexistente.

## 4. Mapa dos módulos

| Módulo | Tema | Estado |
|---|---|---|
| 00 | Mapa modular e governança | DECIDIDO |
| 01 | Visão, promessa e pilares | DECIDIDO |
| 02 | Trilhas de progressão | DECIDIDO nos princípios; detalhes abertos |
| 03 | Storyline macro e campanhas | DECIDIDO nos princípios; detalhamento aberto |
| 04 | Raças e origens | RAC-01 a RAC-12 decididos; bloco 3 ainda aberto |
| 05 | Atributos, recursos e balanceamento | NÃO INICIADO |
| 06 | Poderes, técnicas e conjuntos canônicos | NÃO INICIADO |
| 07 | Transformações e estados de combate | NÃO INICIADO |
| 08 | NPCs, mentores e relações | NÃO INICIADO |
| 09 | Quests e objetivos | NÃO INICIADO |
| 10 | Mobs, chefes e encontros | NÃO INICIADO |
| 11 | Itens, armas e objetos canônicos | NÃO INICIADO |
| 12 | Dimensões, biomas e estruturas | NÃO INICIADO |
| 13 | Diálogos, guia e apresentação narrativa | NÃO INICIADO |
| 14 | Multiplayer, facções e progressão social | NÃO INICIADO |
| 15 | Economia, pontos e recompensas | NÃO INICIADO |
| 16 | Escopo por fases e critérios de aceite | NÃO INICIADO |

O horizonte é o **produto completo**. Quando falarmos de MVP ou primeira fatia jogável, isso deve ser explicitamente marcado como recorte de entrega, não como limite do design do mod.

## 5. Decisões macro já aprovadas (D01–D06)

### D01 — Protagonista

O jogador cria e controla um personagem próprio, paralelo aos personagens canônicos. Ele não é Ichigo nem substitui sua biografia. Pode escolher nome, aparência, raça, construção, relações, afiliação e trajetória.

### D02 — Relação com os eventos canônicos

Os acontecimentos centrais de Bleach existem e podem ser jogados. O jogador pode enfrentar diretamente chefes e conflitos canônicos, sozinho ou em grupo. Personagens canônicos não são spawnados como aliados para derrotar Aizen ou outro chefe principal. A quest pode iniciar o encontro e apresentar os estados/formas corretos do inimigo, como no modelo de Cell do Dragon Mine Z.

O jogador participa do acontecimento como combatente original; não herda a identidade, decisões ou relações do personagem canônico.

### D03 — Estrutura de mundo

Campanha guiada em um mundo Minecraft aberto, com exploração, NPCs, quests, combate, treinamento e espaços especiais.

### D04 — Poderes de alto impacto

Uma habilidade grande exige combinação de autorização narrativa/contextual, aprendizado com mentor ou método válido, prova prática, pontos disponíveis e domínio. Pontos sozinhos não devem entregar Bankai, Resurrección, Vollständig ou equivalente.

### D05 — Fontes de cânone

Base principal no mangá. Anime oficial, arco Thousand-Year Blood War e materiais/licenciados podem complementar. Fillers não devem ser tratados como fundamento obrigatório; entram somente se forem úteis e explicitamente marcados.

### D06 — Multiplayer

Personagens originais coexistem no mesmo mundo e período. Cada jogador segue sua trajetória, podendo cooperar, competir, pertencer a grupos diferentes ou encontrar versões jogáveis dos mesmos eventos.

## 6. Política aprovada para poderes canônicos

O usuário rejeitou a criação livre de poderes originais. **Todas as raças jogáveis usam conjuntos de poderes existentes em personagens reais de Bleach.**

Isso significa:

- Shinigami escolhe/recebe um conjunto de Zanpakuto canônico, com as manifestações disponíveis daquele conjunto (selada, Shikai, Bankai quando aplicável).
- Hollow/Arrancar usa perfil, habilidades e eventual Resurrección de um Hollow/Arrancar canônico compatível.
- Quincy usa repertório Quincy e especialização/arma/Schrift canônico quando liberado.
- Fullbringer usa objetos e manifestações de Fullbring de personagens canônicos; não pode escolher qualquer objeto arbitrário do Minecraft.
- O mesmo conjunto pode aparecer em mais de um jogador ou NPC. Isso é uma adaptação necessária para jogabilidade multiplayer, não uma afirmação de que o personagem canônico tem várias cópias no cânone.
- O jogador não herda nome, aparência, patente, biografia, laços ou decisões do personagem que originou o conjunto.
- A forma de seleção ainda está aberta para os módulos 06 e 07: escolha direta, recomendação no prólogo, rolagem limitada, mentor ou combinação dessas opções.

## 7. Trilhas de progressão (PRG-01 a PRG-08)

- Existem sete trilhas conceituais que podem ser reveladas gradualmente: fundamentos físicos/espirituais, disciplina de combate, mobilidade/controle, poder racial, técnica de assinatura, transformação/estado avançado e posição/reputação.
- O sistema usa **Training Points** universais para comprar melhorias e técnicas, independentemente da raça. A divisão exata de custos ainda será balanceada.
- Pontos vêm de quests, mentores, minigames, derrotar mobs relevantes, provas de domínio e locais especiais.
- Locais especiais aprovados como multiplicadores/ambientes de treinamento: sala do Urahara, caverna de treinamento da Yoruichi/Tenshintai e Dangai para o treinamento do Getsuga Tensho Final, entre outros. A implementação detalhada fica para dimensões/estruturas/quests.
- Cada disciplina também evolui pelo uso moderado e contínuo, como barras de experiência. Isso não substitui a compra de níveis com pontos.
- Um Shinigami pode aumentar Zanjutsu usando sua Zanpakuto; Kidō pode evoluir pelo uso de Kidō e aprendizado com usuários/mentores. O mesmo princípio vale para outras raças.
- Rank social, posição, reputação e afinidade são camadas separadas do poder de combate. Um jogador pode ter alta força e baixa posição, ou posição alta sem domínio máximo.
- As classificações devem respeitar cruzamentos canônicos: um oficial sentado pode ter Bankai; um Gillian não deve receber Resurrección; uma classificação social não determina automaticamente toda transformação.

## 8. Storyline e campanhas (STR-01 a STR-07)

- Há uma **campanha principal** e campanhas raciais próprias.
- A campanha principal também contém missões secundárias, investigação, treinamento, exploração, tarefas de facção e conflitos menores. Ela não é apenas uma sequência de batalhas centrais do anime.
- Campanhas raciais dependem do momento adequado da campanha principal, mas não exigem que o jogador pertença a outra raça nem que jogue a campanha de outra raça.
- O jogador participa de eventos canônicos e luta contra seus antagonistas. Não deve depender de personagens canônicos como ajudantes controlados pelo jogo.
- Encontros podem reproduzir estados específicos de um antagonista conforme a fase da história (por exemplo, diferentes formas/estados de um chefe), iniciados por quest.
- Ramificações grandes que reescrevam a linha do tempo ou substituam resultados canônicos ficam em backlog até que o escopo narrativo seja mais maduro.

## 9. Raças e origens (RAC-01 a RAC-12)

O módulo 04 está **idealizado**, com RAC-01 a RAC-12 aprovados. A aprovação dos princípios não significa que números, tabelas de atributos, quests ou implementação estejam fechados.

### 9.1 Identidade em camadas

Cada personagem deve ser descrito por origem, natureza espiritual, estado atual, aspectos de poder, afiliação e trajetória. Isso evita que “raça”, “rank” e “transformação” sejam confundidos.

### 9.2 Shinigami

- Próprio prólogo conduz à Academia/Shinigami.
- Começa com uma **shinai**.
- O caminho deve passar por fundamentos e, quando apropriado, Asauchi/Zanpakuto antes de Shikai.
- Ser Shinigami não dá automaticamente acesso a Shikai ou Bankai.
- O status de Substitute Shinigami é uma possível suborigem/afiliação posterior, não a definição de todo Shinigami.

### 9.3 Hollow/Arrancar

- O jogador começa no Mundo Humano, mas a introdução racial trata uma vida anterior breve como Plus e a formação como Hollow novo.
- Hollow comum não recebe Resurrección por padrão; Gillian também não deve receber uma transformação que contradiga o cânone.
- A vantagem inicial aprovada é qualitativa: eficácia corporal e sobrevivência maiores no começo para compensar a falta de ferramenta de combate. Não deve virar superioridade permanente em todos os atributos.
- Exactos números, estágios e condições ficam para atributos, poderes e transformações.

### 9.4 Quincy

- O jogador é um humano independente com herança Quincy oculta/fragmentada.
- Não nasce automaticamente como membro do Wandenreich.
- A entrada em estruturas Quincy, Sternritter e patentes superiores deve ser conquistada por campanha, afinidade, provas e contexto.

### 9.5 Fullbringer

- O jogador começa como humano independente com potencial Fullbring.
- Não pode inventar um objeto/poder arbitrário.
- Deve usar objetos, conceitos e manifestações de Fullbring presentes em personagens canônicos. Exemplos de referência: pingente/cross de Kūgo Ginjō, console de Yukio, marcador/livro de Tsukishima, botas de Jackie.
- A progressão enfatiza manipulação de almas da matéria, ativação, foco e domínio do objeto/manifestações.
- Chad e Orihime podem servir como referências de sistema/afinidade, mas a catalogação de conjuntos canônicos será feita no módulo de poderes.

### 9.6 Origem e convergência

- Todos os jogadores aparecem tecnicamente no Mundo Humano/Overworld.
- A convergência é funcional: os prólogos divergem imediatamente por raça e contexto; não significa que todos começam na mesma sala ou recebem a mesma experiência.
- Raças híbridas não são jogáveis no início. Entram no produto completo somente depois de o núcleo de raças e balanceamento estar estável.
- “Morte” do personagem comum é tratada como derrota/retorno de gameplay, sem transformar cada respawn em evento de lore.

## 10. Alma-guia (RAC-12)

Foi aprovada a alternativa A: uma pequena alma/item vinculado ao jogador.

- O jogador clica/usa o item para abrir uma tela de diálogo.
- A alma fala com o jogador e pode reagir à raça, origem, quest atual, lacunas de fundamento e progresso.
- Deve existir uma casca de interface comum, mas fala, comportamento e conteúdo podem ser específicos por raça.
- Ela orienta e contextualiza; não substitui professores da Academia, mentores canônicos, líderes de facção nem NPCs que ensinam técnicas.
- Preferência de design: item não consumível, não negociável, não perdido permanentemente e com estado salvo por jogador.
- Dependências futuras: módulo de itens/inventário, tela/histórico, diálogos, gatilhos de quest e identidade do jogador.

## 11. Roadmap de NPCs recebido

O arquivo enviado pelo usuário foi lido integralmente:

`/Users/tarsopassos/.codex/attachments/f2cd906c-a50f-40b9-9f70-7af59eba45e7/pasted-text.txt`

Ele propõe cadeias de NPCs por raça e cinco patamares de orientação. Deve ser usado como **entrada de conteúdo** para módulos 08 (NPCs/mentores), 09 (quests) e 13 (diálogos), não como contrato final de sistema.

Referências de conteúdo do roadmap:

- Shinigami: Academia, Urahara, Yoruichi e Yamamoto.
- Quincy: Ryūken, Uryū, Bazz-B, Askin e Yhwach.
- Hollow/Arrancar: Nel, Grimmjow, Aizen, Szayelaporro e Ulquiorra.
- Fullbringer: Orihime, Tessai, Ginjō, Riruka, Yukio, Chad e Tsukishima.

Correções de cânone e design já registradas:

- Shinai/Asauchi vêm antes da relação plenamente manifesta com a Zanpakuto; não descrever Shikai como “desbloqueio da forma selada”.
- NPC não entrega automaticamente Bankai, Resurrección, Vollständig ou Fullbring completo: o desbloqueio exige contexto, aprendizado, prova, pontos e domínio.
- Letzt Stil deve ser separado de Vollständig; Vollständig é sucessor/estado posterior, não sinônimo.
- Segunda Etapa não é uma etapa final universal de Arrancar; tratar como caso específico, associado a Ulquiorra, até decisão posterior.
- Fullbring deve ser descrito como manipulação/afinidade com almas da matéria e do objeto, não literalmente como uma alma “presa” no objeto.

## 12. Ponto exato de continuação

Continuar no **módulo 04 — bloco 3**, antes de abrir outro módulo.

Tema do bloco 3:

1. diferenças conceituais entre as quatro raças;
2. vantagens iniciais e limitações de cada uma;
3. interações hostis, neutras e amigáveis entre raças;
4. perfil qualitativo de status inicial, sem fechar números antes do módulo 05;
5. como conjuntos de poderes canônicos interagem com cada raça;
6. implicações para multiplayer e coexistência.

A próxima resposta do novo chat deve:

- confirmar que leu este handoff e os cinco documentos do módulo;
- resumir o que está decidido e o que ainda está aberto;
- apresentar recomendações e aproximadamente 5–10 perguntas focadas para o bloco 3;
- indicar claramente a alternativa recomendada em cada pergunta;
- aguardar as decisões do usuário antes de editar o documento;
- não criar quests detalhadas, tabelas numéricas, código ou NPCs implementados nesta etapa.

## 13. Prompt pronto para iniciar o outro chat

Copie o texto abaixo junto com este documento:

> Você é o coautor/lead de game design do mod Bleach para Minecraft Java Edition. Leia primeiro o arquivo `Docs/game-design/00-handoff-para-outro-chat.md` e, em seguida, leia integralmente `00-mapa-modular.md`, `01-visao-e-pilares.md`, `02-trilhas-de-progressao.md`, `03-storyline-macro.md` e `04-racas-e-origens.md`.
>
> Continue exatamente do ponto descrito no handoff: módulo 04, bloco 3, sobre diferenças, vantagens, limitações, interações e coexistência das raças. Não pule para NPCs, quests, itens, mobs ou implementação. Não invente poderes originais: a decisão vigente é que todas as raças usam conjuntos de poderes canônicos de personagens reais de Bleach, com duplicação permitida como adaptação multiplayer.
>
> Trabalhe em português, de forma incremental e crítica. Separe CÂNONE, ADAPTAÇÃO, DECIDIDO e PENDENTE. Recomende uma alternativa, explique riscos e faça perguntas objetivas. Não trate uma proposta como aprovada até o usuário confirmar. Preserve o estado do Git e não faça commits, pushes, resets ou alterações de código sem autorização. Ao final de cada bloco, atualize apenas a documentação correspondente e mantenha os estados IDEALIZADO, ESPECIFICADO, IMPLEMENTADO, TESTADO, AUDITADO e ACEITO separados.

## 14. Critério de continuidade

O novo chat estará desempenhando corretamente o papel quando não repetir decisões já aprovadas, não misturar módulos, não criar poderes originais contra a decisão vigente, não usar personagens canônicos como aliados automáticos nas batalhas centrais e mantiver o usuário no controle das aprovações.

