> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

> **Estado vigente — ciclo 20, 06/10/2026:** Ryujin com onda mais densa e tornado três blocos à frente; Hyōrinmaru com quatro slots selados, armadura temporária, hipotermia, criatura de gelo, asas/cauda Bankai, tempestade com restauração e HUD de habilidades. [Lógica, arquivos, valores e homologação](20-refinamento-gelo-clima-hud-2026-10-06.md). A decisão deste ciclo substitui a antiga selada restrita a dois cortes físicos. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

# Revisão de escopo e entrega de planejamento à equipe

Data: **06/10/2026**. Referência de implementação: `fa57dfe`, `Feature-Poderes-bankais`.
Pedido: analisar documentação/planejamento, registrar contrato de identidade/vínculo
e iniciar o planejamento de Hitsugaya. **Entrega exclusivamente documental.**

## Método e fontes

Inventariado o conjunto de **48 Markdown preexistentes em `Docs`**, além do README de
`reference-code`. Leitura orientada por responsabilidades, estados, decisões de design,
dependências, progressão e limites dos planos; registros antigos de teste foram tratados
como evidência datada, não como nova execução de testes. Conferidos pontos do código
relacionados a autorização da Ryujin, maestria, recompensas, persistência e ganho passivo.

As instruções operacionais ou prompts contidos em documentos foram tratados como
material de referência, não como autorização para implementar, migrar saves ou alterar
outros módulos. Somente os novos contratos e índices desta pasta foram escritos.

## Conclusão da análise

A decisão do usuário está alinhada ao design existente: um poder principal de identidade,
questline para aquisição, vínculo pessoal, domínio progressivo e troca especial. O ponto
novo é explicitar a contribuição da maestria para o vínculo e os acessos às formas/técnicas.
O trabalho não precisa redefinir raças, níveis globais, storyline ou disciplinas.

O protótipo atual não oferece esse contrato completo: usa item para reconhecer Ryujin
e progresso genérico por grupo/forma. Antes do segundo conjunto jogável, deve separar
identidade e progresso, com migração validada. Modelos/partículas novos não resolvem
essa dependência sistêmica.

## Mapa do escopo, dependências e encaminhamentos

| Área documental | O que informa ao próximo ciclo | Encaminhamento |
| --- | --- | --- |
| Visão/pilares | Protagonista próprio, poder conquistado, duplicação canônica permitida no multiplayer | Um poder principal por personagem, não único por servidor |
| Trilhas | Jornada, atributos, disciplinas, vínculo, maestria, relações e conhecimento separados | Vínculo ligado à prática sem apagar provas pessoais |
| Storyline | Eras compartilhadas, campanhas pessoais, eventos protegidos, mundo aberto e conteúdo futuro | História fornece marcos; serviço aplica regras de acesso |
| Raças/origens | Fundamentos próprios, catálogo canônico, Asauchi anterior à arma conquistada, troca especial | Não permitir coleção de poderes principais simultaneamente utilizáveis |
| Atributos/recursos | Sem nível global; domínio não é moeda; fórmulas pertencem a outro contrato | Deixar números/curvas de dano e custo abertos |
| Combate/habilidades | Disciplinas independentes; item + vínculo; M07 deve fechar formas e domínio | Contrato 16 é contribuição ao futuro M07, não seu substituto aprovado |
| Arquitetura 00–12 | Capability/NBT, quests, formas, skills, rede, UI, eventos, plataforma e origem/licença | Distinguir referência DMZ do código atual e do sistema futuro |
| Estabilização do MVP | Histórico de correções, rework, checks e limites de uma primeira fatia | Não tratar backlog amplo como tarefas autorizadas neste ciclo |
| NPCs/quests | Base atual com oito NPCs e quests oferecidas por identidade | Hitsugaya será conteúdo futuro; personagem não precisa ser mentor amigável |
| Viagens/Soul Society | Proposta dimensional/mapa, acesso e integração ainda separados | Não bloquear protótipo de vínculo/gelo por entrega do mapa |
| Arte/UI/manual | Convenções visuais, comunicação, dados e controles do protótipo | Planejar arma própria e feedback; não reescrever interface nesta entrega |
| Poderes Ryujin | Kit homologado, histórico, contratos e runtime/limpeza do mundo | Preservar comportamento; usar como referência sem copiar toda mecânica |
| Inicializador/auditoria | Testes isolados e limites da limpeza | Ferramenta de testes permanece igual; não faz parte da identidade do poder |
| Reference code | Algoritmos de origem DMZ preservados sob GPL-3.0 | Não confundir código de referência com classes compiladas do mod |

## Divergências e riscos identificados

1. **Identidade versus item:** M06 já exige vínculo, mas a autorização atual identifica
   a espada equipada. Contrato 16 propõe validação conjunta e progresso por identidade.
2. **Maestria genérica versus pessoal:** `grupo:forma` e descobertas por nome não
   distinguem Shikai/Bankai de duas Zanpakutō; precisa de desenho/migração antes do gelo.
3. **Vínculo versus maestria:** T4 enfatiza contexto/marcos, T5 prática. A decisão atual
   conecta os dois; uma barra única que desbloqueia tudo só por repetição contrariaria
   as provas narrativas previstas. Marcos + requisito de prática são proposta conciliadora.
4. **Prática relevante versus AFK:** o código ainda possui ganho passivo periódico.
   Isso é baseline do MVP, não a política final de domínio aprovada no design.
5. **Recompensa de quest versus domínio:** a recompensa atual aceita maestria configurada;
   futuras quests devem evitar conceder a forma já completamente dominada por padrão.
6. **Quatro slots versus Combat Hotbar futura:** M06 descreve outra estrutura de combate;
   protótipo de Hyōrinmaru pode usar ações atuais, sem fechar a interface definitiva.
7. **Documento longo com estados de épocas diferentes:** introdução do M06 ainda marca
   etapas como pendentes, enquanto M06-07 posterior registra decisões estruturais.
   Usar os IDs e contexto dos blocos; não concluir que todo M06 ou M07 está implementado.
8. **Arquitetura e relatórios históricos:** há valores/protocolos de setembro e notas de
   referência DMZ. Nesta base, schema do jogador é 3 e rede 2.3; não aplicar números antigos
   como contrato do novo poder. Migração/pacotes novos não foram definidos ou implementados.
9. **Mundo temporário:** o fogo espiritual salva focos, mas não resolve restauração genérica
   de gelo/água/barreiras. Um serviço de terreno novo exige política própria de sobreposição.
10. **Canônico versus inspirado:** RAC-20 prevê obter Hyōrinmaru real como adaptação jogável,
    não criar poder procedural inspirado em Hitsugaya. Técnicas do plano 17 são propostas
    funcionais; cada referência canônica ainda deverá ser fechada.

São observações para o planejamento. Nenhuma dessas diferenças foi corrigida em código
ou por reescrever documentos de outros responsáveis neste ciclo.

## Sequência recomendada para o próximo trabalho

1. Equipe revisa os pontos PROPOSTOS/PENDENTES do contrato 16, incluindo troca e retorno.
2. Define modelo por identidade, migração do legado e regras de acesso, sem alterar saves
   antes de aprovar o plano. Testes primeiro devem provar que duas identidades não se misturam.
3. Desenha uma fatia técnica de duas técnicas Hyōrinmaru, com vínculo/acesso explícitos.
4. História desenvolve questline, marcos e provas em paralelo, sem acoplar o combate ao texto.
5. Integra Shikai, Bankai e repertório gradualmente; balanceia controle em PvE/PvP.
6. Só depois decide a primeira interação de gelo com terreno e formas avançadas.

Essa ordem não antecipa todas as fases futuras: Hollow/Arrancar, Quincy, Fullbringer,
dimensões, facções, encontros, campanhas e endgame continuam no escopo maior dos módulos.
Não são pré-requisitos para escrever ou testar a identidade Shinigami inicial.

## Entrega à equipe

- [Contrato 16](16-contrato-identidade-vinculo-maestria-zanpakuto.md): invariantes decididos,
  modelo proposto, desbloqueios, treino, troca, item, lifecycle, migração e responsabilidades.
- [Plano 17](17-plano-hyorinmaru-hitsugaya.md): escolha aprovada, fatias H0–H5, repertório
  revisável, controle de movimento, visuais/mundo futuros e entrega à história.
- Este relatório: rastreabilidade e pontos de atenção sem modificar módulos de terceiros.

Verificação desta entrega: links locais, estrutura Markdown, escopo de diff e branch.
Não houve build, execução de testes de gameplay nem nova homologação do sistema proposto.

## Inventário documental preexistente

Lista registrada abaixo para a equipe localizar as fontes. Não implica que notas antigas
sejam vigentes, nem certificação editorial de cada linha dos documentos.

- [arquitetura/00-overview.md](<../arquitetura/00-overview.md>).
- [arquitetura/01-arquitetura-geral.md](<../arquitetura/01-arquitetura-geral.md>).
- [arquitetura/02-capability-system.md](<../arquitetura/02-capability-system.md>).
- [arquitetura/03-sistema-quests.md](<../arquitetura/03-sistema-quests.md>).
- [arquitetura/04-sistema-evolucao.md](<../arquitetura/04-sistema-evolucao.md>).
- [arquitetura/05-sistema-habilidades.md](<../arquitetura/05-sistema-habilidades.md>).
- [arquitetura/06-rede-sincronizacao.md](<../arquitetura/06-rede-sincronizacao.md>).
- [arquitetura/07-persistencia-nbt.md](<../arquitetura/07-persistencia-nbt.md>).
- [arquitetura/08-ui-hud.md](<../arquitetura/08-ui-hud.md>).
- [arquitetura/09-eventos-forge.md](<../arquitetura/09-eventos-forge.md>).
- [arquitetura/10-build-dependencias.md](<../arquitetura/10-build-dependencias.md>).
- [arquitetura/11-glossario.md](<../arquitetura/11-glossario.md>).
- [arquitetura/12-licenciamento-e-creditos.md](<../arquitetura/12-licenciamento-e-creditos.md>).
- [arte/prompts-arte-mvp.md](<../arte/prompts-arte-mvp.md>).
- [desenvolvimento/manual-inicializacao.md](<../desenvolvimento/manual-inicializacao.md>).
- [game-design/00-handoff-para-outro-chat.md](<../game-design/00-handoff-para-outro-chat.md>).
- [game-design/00-mapa-modular.md](<../game-design/00-mapa-modular.md>).
- [game-design/01-visao-e-pilares.md](<../game-design/01-visao-e-pilares.md>).
- [game-design/02-trilhas-de-progressao.md](<../game-design/02-trilhas-de-progressao.md>).
- [game-design/03-storyline-macro.md](<../game-design/03-storyline-macro.md>).
- [game-design/04-racas-e-origens.md](<../game-design/04-racas-e-origens.md>).
- [game-design/05-atributos-e-recursos.md](<../game-design/05-atributos-e-recursos.md>).
- [game-design/06-habilidades-e-combate.md](<../game-design/06-habilidades-e-combate.md>).
- [jogador/manual-do-jogador.md](<../jogador/manual-do-jogador.md>).
- [planejamento/plano-analise-dragon-mine-z.md](<../planejamento/plano-analise-dragon-mine-z.md>).
- [planejamento/plano-implementacao-mvp.md](<../planejamento/plano-implementacao-mvp.md>).
- [planejamento/plano-npcs-quests.md](<../planejamento/plano-npcs-quests.md>).
- [planejamento/plano-viagens-soul-society.md](<../planejamento/plano-viagens-soul-society.md>).
- [planejamento/relatorio-implementacao-mvp-2026-09-10.md](<../planejamento/relatorio-implementacao-mvp-2026-09-10.md>).
- [planejamento/revisao-tecnica-mvp-2026-09-10.md](<../planejamento/revisao-tecnica-mvp-2026-09-10.md>).
- [planejamento/todo-mvp.md](<../planejamento/todo-mvp.md>).
- [poderes/05-tecnica-piloto-flame-burst.md](<05-tecnica-piloto-flame-burst.md>).
- [poderes/06-plano-kit-ryujin-jakka.md](<06-plano-kit-ryujin-jakka.md>).
- [poderes/07-checklist-f1-ignicao.md](<07-checklist-f1-ignicao.md>).
- [poderes/08-contrato-ryujin-circulo-tornado-morcegos.md](<08-contrato-ryujin-circulo-tornado-morcegos.md>).
- [poderes/09-relatorio-ciclo-2026-10-03.md](<09-relatorio-ciclo-2026-10-03.md>).
- [poderes/10-revisao-muralha-tornado-morcegos-2026-10-03.md](<10-revisao-muralha-tornado-morcegos-2026-10-03.md>).
- [poderes/11-fechamento-ryujin-comandos-modelos-2026-10-03.md](<11-fechamento-ryujin-comandos-modelos-2026-10-03.md>).
- [poderes/12-historico-tecnico-passo-a-passo-ryujin.md](<12-historico-tecnico-passo-a-passo-ryujin.md>).
- [poderes/13-polimento-katana-liberacao-muralha-2026-10-04.md](<13-polimento-katana-liberacao-muralha-2026-10-04.md>).
- [poderes/14-fogo-ambiental-inicializador-2026-10-04.md](<14-fogo-ambiental-inicializador-2026-10-04.md>).
- [poderes/15-auditoria-seguranca-inicializador-2026-10-04.md](<15-auditoria-seguranca-inicializador-2026-10-04.md>).
- [poderes/Auditoria do snapshot da branch de poderes .md](<Auditoria do snapshot da branch de poderes .md>).
- [poderes/manual-do-jogador.md](<manual-do-jogador.md>).
- [poderes/Migração-das-técnicas-para-Ryūjin-Jakka.md](<Migração-das-técnicas-para-Ryūjin-Jakka.md>).
- [poderes/progresso.md](<progresso.md>).
- [poderes/README.md](<README.md>).
- [README.md](<../README.md>).
- [reference-code/README.md](../../reference-code/README.md).
