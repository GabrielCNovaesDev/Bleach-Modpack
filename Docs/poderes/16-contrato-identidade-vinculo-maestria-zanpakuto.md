# Contrato de identidade, vínculo e maestria da Zanpakutō

## 1. Estado, autoridade e revisão

Data: **06/10/2026**. Base analisada: `fa57dfe`, branch `Feature-Poderes-bankais`.
**Estado: EM ESPECIFICAÇÃO.** Documento de contrato para desenvolvimento pela equipe;
não representa uma funcionalidade implementada nem aprovação integral do futuro M07.

O usuário decidiu nesta conversa: Ryujin e outros poderes principais são escolhas de
identidade; podem ser trocados; cada identidade tem vínculo próprio, relacionado à
maestria; desenvolver esse vínculo amplia acesso a habilidades e participa dos
requisitos de Shikai e Bankai. Escolheu Hitsugaya para o próximo conjunto.

As regras marcadas **DECIDIDO** abaixo vêm dessa decisão ou dos documentos de design
já aprovados. **PROPOSTA** é recomendação revisável. **PENDENTE** exige decisão da equipe.
Não transformar exemplos, nomes de campos ou etapas em autorização para implementar.

## 2. Objetivo

Separar identidade principal, arma física, relacionamento, domínio e liberações.
Evitar que equipar uma espada, copiar seu item ou atingir uma maestria genérica
conceda automaticamente o poder de outro personagem. Fornecer um contrato comum
para desenvolver Ryujin e Hyōrinmaru sem fechar as campanhas de aquisição.

## 3. Fantasia do jogador

O jogador conquista uma Zanpakutō reconhecível, aprende a relacionar-se com ela e
desenvolve suas manifestações. Shikai e Bankai são conquistas pessoais, não recompensas
imediatas por receber o item. A escolha possui peso, mas não é uma prisão irreversível.

## 4. Base documental e canônica

| Referência | Regra ou limite herdado |
| --- | --- |
| [M01 — visão](../game-design/01-visao-e-pilares.md), D04/D06 | Descoberta, prova, investimento quando exigido e domínio; múltiplos jogadores podem conquistar o mesmo poder |
| [M02 — trilhas](../game-design/02-trilhas-de-progressao.md), T4/T5, PRG-06/07 | Vínculo pessoal e maestria prática têm papéis distintos; contrato comum com expressão racial própria |
| [M03 — storyline](../game-design/03-storyline-macro.md) | História registra autorizações; não substitui a validação de desbloqueio; progresso pessoal distinto da era do mundo |
| [M04 — raças](../game-design/04-racas-e-origens.md), RAC-20/23/24/25 | Poder canônico real adquirido em questline, um principal por vez, troca especial, treino útil sem comprar grandes marcos apenas por quantidade |
| [M05 — atributos](../game-design/05-atributos-e-recursos.md) | Sem nível global; atributos, recursos, domínio e identidade não são o mesmo sistema |
| [M06 — combate](../game-design/06-habilidades-e-combate.md), M06-07V–Z | Registro no personagem + identidade no item; espada incompatível não transfere poder; morte/roubo/duplicação não transferem vínculo |
| [Mapa modular](../game-design/00-mapa-modular.md) | M07 resolve formas/domínio; M11 itens; M12 quests; M13 campanhas; M14 feedback; M15 multiplayer |

A aquisição é uma adaptação de gameplay: o jogador conquista o poder canônico, sem
receber a biografia, patente ou relações de seu portador na obra. Vários personagens
podem possuir Ryujin; “única” significa **uma identidade principal ativa por personagem**,
e não exclusividade mundial de uma única espada entre todos os jogadores.

## 5. Adaptação para Minecraft e decisões consolidadas

- **DECIDIDO — ID-01:** um poder principal ativo por personagem; disciplinas raciais
  e capacidades gerais continuam independentes desse poder.
- **DECIDIDO — ID-02:** possuir/equipar o item não muda a identidade principal.
- **DECIDIDO — ID-03:** troca existe como processo especial, não como alternância de
  hotbar/loadout durante combate. Quest, custo e consequências ainda são pendentes.
- **DECIDIDO — ID-04:** vínculo e maestria são pessoais à identidade. A maestria de
  Ryujin não dá acesso à Shikai/Bankai de Hyōrinmaru.
- **DECIDIDO — ID-05:** maestria contribui para aprofundar o vínculo e abrir repertório;
  Shikai/Bankai também exigem os marcos/contexto que a equipe definir.
- **DECIDIDO — ID-06:** treino/grind apropriado gera domínio real; pontos não compram
  diretamente vínculo ou maestria; AFK não deve bastar para dominar um poder.
- **DECIDIDO — ID-07:** morte, perda, roubo e duplicação não transferem vínculo nem
  apagam automaticamente a identidade do personagem.

## 6. Escopo e responsabilidades

Este contrato define identidade, separação de progresso, condições conceituais de
acesso, troca e fronteiras entre sistemas. Não determina capítulos, diálogos, bosses,
patentes, custos finais, curvas de experiência ou os acontecimentos que concedem o poder.

| Decisão | Responsabilidade a desenvolver | Saída esperada |
| --- | --- | --- |
| Identidade e formas | Equipe de poderes / M07 | Catálogo de poderes, estados e regras de acesso |
| Jornada e provas | Equipe de história / M03 e M13 | Autorizações e marcos com IDs estáveis |
| Quest e recompensa | Equipe de quests / M12 | Validação de conclusão e concessão idempotente |
| Domínio e números | Combate/balanceamento / M05, M06 e M15 | Fontes válidas, teto, ritmo e efeitos |
| Perda/recuperação da espada | Itens/economia / M11 | Regra de inventário, reposição e duplicação |
| Explicação ao jogador | Interface / M14 | Progresso, próximos requisitos e motivo de bloqueio |
| Saves e rede | Engenharia | Modelo versionado, migração e autoridade do servidor |

Nenhum membro ou responsável nominal é atribuído neste documento.

## 7. Modelo conceitual proposto

**PROPOSTA — MD-01:** manter um registro de progressão por identidade e um ponteiro
para a identidade ativa. Não escolher classes, pacotes ou nomes NBT definitivos agora.

| Informação conceitual | Local de autoridade | Papel |
| --- | --- | --- |
| Identidade principal ativa | Personagem | Qual repertório principal pode ser utilizado |
| Identidades conquistadas e histórico | Personagem | Registros de aquisição/troca; histórico não autoriza uso simultâneo |
| Marcos de vínculo por identidade | Personagem | Conhecimento/compreensão/provas pessoais |
| Maestria do vínculo | Personagem + identidade | Prática relacionada à fonte de poder; contribuição para maturidade |
| Maestria de Selada/Shikai/Bankai | Personagem + identidade + forma | Controle prático de cada etapa adquirida |
| Domínio e desbloqueios de técnicas | Personagem + identidade + técnica | Acesso/variantes próprios, sem equivalência automática entre kits |
| Identidade e proprietário da espada | Item, conferido contra o personagem | Modelo e compatibilidade; item não é autoridade de progresso |
| Recursos/cooldowns/efeitos ativos | Runtime no servidor | Estado de uso; separado da progressão persistente |

**PROPOSTA — MD-02:** representar vínculo como marcos pessoais com requisito de maestria.
Isso relaciona vínculo ao treino, conforme o pedido, sem substituir T4/T5 por uma única
barra universal. Não criar duas barras numéricas independentes e redundantes por padrão.
Escala, fontes, agregação e interface dessa maestria permanecem **PENDENTES**.

Exemplo: prática válida melhora controle; um marco pessoal reconhece maturidade e
permite uma prova; concluir a prova e cumprir requisitos libera uma manifestação.
Um jogador com alto domínio, mas sem a prova, vê o requisito pendente em vez de ganhar
Bankai automaticamente. Repetir a prova não duplica concessão nem reseta domínio.

### Estados de acesso

**PROPOSTA:** perseguida → conquistada e vinculada → contato/compreensão → Shikai
adquirida → Shikai desenvolvida → prova de Bankai disponível → Bankai adquirida →
Bankai desenvolvida. São estados de planejamento, não nomes finais de quests.

“Conhecido”, “disponível para aprender”, “adquirido”, “selecionável” e “utilizável agora”
continuam distintos. Uma habilidade adquirida pode estar bloqueada por arma incompatível,
forma atual, custo ou cooldown. Selecionar uma forma não equivale a ativá-la.

## 8. Regras de acesso, treino, troca e ciclo de vida

### Liberações e técnicas

**PROPOSTA — AC-01:** para adquirir Shikai, combinar identidade vinculada, requisito
de domínio da etapa anterior e marcos/prova pessoais. Para Bankai, exigir Shikai dessa
mesma identidade, domínio relevante e seus próprios marcos/prova. Investimento em pontos
entra apenas quando o contrato de conteúdo exigir. Nenhum limiar numérico está aprovado.

**PROPOSTA — AC-02:** no uso, servidor valida personagem/raça, identidade ativa,
compatibilidade da arma, acesso à forma/técnica, estado de combate, recursos e cooldown.
Revalidar no momento da execução, inclusive se a seleção ocorreu antes de uma troca.

**PROPOSTA — AC-03:** os benefícios de domínio podem incluir novas técnicas, variantes,
controle, eficiência e potência. Curvas de dano, custos mínimos e caps precisam de
balanceamento por habilidade; não prometer aumento ilimitado nem multiplicadores
universais. Fórmulas de dano permanecem pendentes de decisão e balanceamento.

**PROPOSTA — TR-01:** prática deve registrar consequência válida: combate apropriado,
controle aplicado ou desafio de treino reconhecido. Declarar fontes e limites por técnica;
considerar repetição, aliados, invulneráveis, invocações próprias e treino autorizado.
Nem ausência de dano nem alvo de treino tornam automaticamente toda ação inválida:
proteção/controle e desafios legítimos também podem gerar domínio.

**PROPOSTA — TR-02:** modos administrativos de cooldown/custo devem continuar
dispensando apenas tempo/custo. Não autorizar identidade, Shikai ou Bankai implicitamente.
Ganho de domínio em modo de teste deve ficar desabilitado por padrão, até decisão da equipe.

### Troca especial

**DECIDIDO:** uma troca não transporta maestria nem desbloqueios para outra identidade.
Fundamentos raciais e atributos gerais não são resetados por trocar o poder principal.

**PROPOSTA — TC-01:** validar elegibilidade fora de combate, explicar consequências e
realizar operação única no servidor: encerrar forma anterior, cancelar charge/efeitos/
invocações, atribuir nova identidade e voltar à Selada correspondente. Troca não zera
reiatsu, cura, restaura recursos ou contorna recargas. Definir recarga por técnica e/ou
bloqueio comum de troca antes de permitir alternância com cooldowns pendentes.

**PROPOSTA — TC-02:** arquivar progresso da identidade anterior, sem transferi-lo.
Se o jogador voltar por outro processo especial, preservar esse histórico é a recomendação;
perda parcial, readaptação ou reset são **PENDENTES**, não decisões aprovadas.
O histórico arquivado não torna a antiga identidade utilizável pela simples troca de item.

**PROPOSTA — TC-03:** falha de pré-condição não muda o personagem nem cobra custo.
Aquisição/troca/recuperação devem ser idempotentes, resistir a pedidos repetidos e possuir
tratamento de falha entre alteração do personagem e entrega/retirada da arma.

### Arma, morte e recuperação

**DECIDIDO:** vínculo permanece no personagem e não se transfere com o item.
**PENDENTE:** retenção na morte, queda recuperável, reposição, custo e ritual.
**PROPOSTA:** identificação de proprietário + identidade e, se necessário, uma versão
de vínculo/arma para invalidar cópias antigas após recuperação. Não decidir que todo
item Ryujin de qualquer origem já pertence automaticamente ao portador.

Perda do objeto pode suspender o uso, mas deve existir recuperação planejada. Mesmo
se uma cópia circular, dois jogadores não dividem o mesmo registro de maestria.
Logout, mudança de dimensão e morte limpam runtime; não apagam conquistas pessoais.
Fogo espiritual já criado no mundo continua seguindo seu prazo e limpeza próprios;
uma troca não deve apagar focos de outros proprietários.

## 9. Integrações e contrato com história

| Sistema | Entrada/saída conceitual |
| --- | --- |
| História | Publica marcos/autorizações pessoais; não escreve maestria arbitrária |
| Quests | Solicita aquisição/desbloqueio ao serviço responsável; valida concessão uma vez |
| NPCs | Oferecem oportunidades contextualizadas; personagem canônico não precisa ser mentor amistoso |
| Formas | Avaliam identidade e acesso específicos; mantém seleção separada de ativação |
| Combate | Reporta uso válido e consulta progressão; não conhece IDs de capítulos |
| UI | Exibe identidade, domínio e requisitos ausentes; não autoriza mudanças |
| Multiplayer | Progresso pessoal; party não concede vínculo automaticamente a todos |
| Mundo | Era global/locais não substituem a progressão individual; mudanças temporárias têm registro próprio |

IDs de marcos devem descrever resultados estáveis, não depender de um diálogo ou NPC
específico que poderá ser reescrito. Para protótipos, usar concessão administrativa
explícita e isolada; não apresentar comandos de teste como campanha final.

## 10. Conteúdo inicial para validar o contrato

Ryujin permanece a referência homologada. Hyōrinmaru será o segundo perfil para testar:
um personagem por vez, dois jogadores com a mesma identidade e personagens com
identidades diferentes. Primeiro validar vínculo/acesso e uma técnica de cada perfil;
expandir repertório após a prova de que progresso e execuções não se misturam.

Plano: [Hyōrinmaru / Hitsugaya](17-plano-hyorinmaru-hitsugaya.md).

## 11. Visão completa e prioridades

Prioridade proposta: contrato de dados/acesso → migração segura → ciclo de vínculo e
troca → dois perfis no dispatcher → protótipo de gelo → conteúdo narrativo/valores →
liberações e repertório completos → homologação multiplayer.
O princípio de identidade pode atender outros caminhos no futuro; não obrigar Hollow,
Quincy e Fullbringer a usar espada, mundo interior ou os mesmos estágios Shinigami.

## 12. Decisões abertas para a equipe

| Ponto | Alternativas / recomendação inicial | Quem fecha |
| --- | --- | --- |
| Ritmo e escala de domínio | Escala legível com teto e fontes válidas por identidade; números não aprovados | M07 e balanceamento |
| Relação vínculo/maestria | Marcos + requisito de prática, em vez de uma barra global única | M02/M07 |
| Conquistar Shikai/Bankai | Provas pessoais + domínio + investimento quando apropriado | M07/M13 |
| Troca | Processo especial contextual; custo, restrições e readaptação a decidir | M07/M11/M13 |
| Voltar a poder antigo | Preservar histórico é recomendação; não garantir retenção integral ainda | M07/balanceamento |
| Recuperar arma | Retenção, reposição ou recuperação física; impedir transferência de poder | M11 |
| Potência por domínio | Benefícios específicos com cap; definir se dano, eficiência ou ambos | M05/M06/M15 |
| Recompensas em cooperação | Elegibilidade/participação pessoal; sem conceder progressão por presença | M12/M15 |
| Saves existentes | Migração explícita sem atribuir legado a todas as futuras Zanpakutō | Engenharia + responsáveis pelo design |

## 13. Critérios de aceitação futuros

1. Equipar Hyōrinmaru enquanto vinculado a Ryujin não ativa gelo nem altera identidade.
2. Dois jogadores com Ryujin mantêm maestria, marcos e técnicas independentes.
3. Item duplicado/roubado não concede identidade ou domínio a terceiro.
4. Maestria alta sem prova não desbloqueia automaticamente Bankai.
5. Quest de Shikai não concede domínio completo nem Bankai por efeito colateral.
6. Troca não transporta liberações, repõe recursos ou limpa cooldown para explorar spam.
7. Falha/repetição de troca não cobra duas vezes, perde arma ou cria duas identidades ativas.
8. Saves, morte, reconexão e dimensões preservam progresso; runtime termina corretamente.
9. AFK e spam em modo de teste não produzem domínio automaticamente.
10. UI explica o requisito pendente e distingue adquirido/selecionável/utilizável.
11. Migração preserva dados antigos sem dar Shikai/Bankai a toda nova identidade.

São critérios de projeto, **não testes já executados**.

## 14. Impacto técnico conhecido e riscos atuais

Inspeção somente de leitura, sem editar código:

- `CharacterData.java`: maestria atual indexada por `grupo:forma`, limitada a 0–100;
  descobertas usam nome de forma. Ainda não distingue `ryujin_jakka` de `hyorinmaru`.
- `TransformationsHelper.java`: checa descoberta, nível de skill e requisitos de
  maestria. Essas regras não implementam vínculo pessoal por Zanpakutō.
- `TransformationReward.java`: pode conceder descoberta e maestria configurada.
  Futuro conteúdo deve separar revelar, adquirir e dominar, sem usar reward de
  maestria alta como substituto da jornada inteira.
- `TickHandler.java`: possui ganho passivo de maestria a cada cinco segundos em forma
  ativa. Não representa ainda a política futura contra domínio por AFK.
- `TechniqueService.java`: autorização Ryujin hoje identifica o item na mão principal.
  Um dispatcher por identidade deverá adicionar vínculo sem regressão no kit fechado.
- `PlayerData.java`: schema 3 e compatibilidade de descobertas antigas por skills/
  maestria genéricas. Definir migração antes de mudar chaves; não bump de schema neste ciclo.
- Rede atual 2.3; novas mensagens/modelo precisarão de revisão e sincronização validada
  no servidor. Este documento não cria pacotes nem muda versão.
- `RyujinTechniqueService` mantém áreas/enxames transitórios; `SpiritFlameService`
  persiste focos do mundo. Não colocar esses runtimes dentro do vínculo do jogador.

As notas históricas de arquitetura Dragon Mine Z e o protótipo atual não são promessa
de que o contrato futuro já existe. A introdução antiga do M06 também não substitui
as decisões posteriores do bloco M06-07 presentes no mesmo documento.

## 15. Próximos passos e entrega à equipe

Fechar MD/AC/TR/TC propostos, principalmente dados/migração e consequências da troca;
desenhar os marcos e provas sem depender de implementação antecipada. Em paralelo,
refinar a proposta de Hyōrinmaru e coletar referências visuais/canônicas para cada técnica.
Somente depois iniciar uma fatia de código autorizada, com testes de acesso e separação
de progresso antes de efeitos complexos. Ryujin permanece encerrada e homologada.
