# Design técnico: estabilização e evolução do MVP Shinigami

## 1. Resumo

Plano consolidado a partir da revisão técnica de 10/09/2026 e do teste de jogo relatado pelo usuário com quatro imagens. Em 10/09/2026, T16–T19 receberam o rework de sete atributos sem cap, fórmulas de combate/reiatsu, migração e BP; a validação visual e multiplayer segue em T24.

Estado: **planejado; funcionalidades e correções ainda não implementadas**. A aprovação da direção de trabalho não significa que todos os números, nomes de atributos e atalhos estejam definidos. Esta entrega organiza a implementação; não altera o comportamento do jogo.

A revisão detalhada continua em [revisao-tecnica-mvp-2026-09-10.md](revisao-tecnica-mvp-2026-09-10.md). Este plano passa a ser a referência de execução e ordem de entregas; a revisão conserva a evidência e os achados R01–R18.

## 2. Contexto e problemas

O MVP contém Shinigami, Asauchi, Selada/Shikai/Bankai, energia, mastery e quatro quests. A revisão identificou problemas de sincronização, rede, carga, interface, persistência e conteúdo JSON. O teste do usuário acrescentou evidência visual e três funcionalidades prioritárias.

### Evidências do teste e rastreabilidade

| ID | Origem | Relato observado | Relação com revisão | Encaminhamento |
|---|---|---|---|---|
| V01 | Imagem 1 | Background de escolha de personagem mal dimensionado; mundo visível nas laterais | Layout responsivo | Corrigir cobertura, proporção e área segura dos controles |
| V02 | Imagem 1 | Arte do espadachim com problema de transparência | Assets | Investigar alpha, recorte e renderização separadamente |
| V03 | Imagem 2 | Ícone de reiatsu com transparência incorreta | Assets | Verificar arquivo e composição no HUD em fundos claros/escuros |
| V04 | Imagem 2 | HUD com organização e alinhamento ruins | UI e R04 | Reorganizar grade, espaçamentos, texto e barras |
| V05 | Imagem 3 | Marcações/status de missão fora de posição | Diário e R03 | Alinhar status, área clicável, seleção e texto |
| V06 | Imagem 3 | Carregamento de transformação com animação ruim | R04/R06 | Corrigir estado/sync antes de suavizar representação visual |
| V07 | Observação textual | Espada empunhada ao contrário | Novo | Investigar orientação da arte e transforms dos modelos handheld |
| V08 | Observação textual | Espada transparente | Novo, relacionado a V02/V03 | Inspecionar alpha e renderização nas três formas |
| C01 | Imagem 3 + orientação textual | Missão Hollow Hunt deve informar que pede zumbis | R09 | Mostrar objetivo real e esclarecer substituto provisório |
| F01 | Imagem 4 | Tela de status unificada para skills e distribuição de pontos | Novo | Tela própria ligada a serviços de progressão |
| F02 | Imagem 4 | Tool circle bar com formas e skills desbloqueadas | Novo | Seletor radial com ações válidas e selecionadas visíveis |
| F03 | Imagem 4 | Comandos para conceder pontos, habilidades e itens no dev testing | Novo | Comandos administrativos, validação e feedback |

As imagens demonstram sintomas visuais. A causa de transparência e orientação da espada ainda exige inspeção dos assets e reprodução no jogo. Os modelos atuais usam minecraft:item/handheld; isso, sozinho, não demonstra se o erro está no PNG ou nas transformações do modelo.

A instrução textual mais recente prevalece sobre a sugestão de criar Hollow que aparece na imagem 3: **continuamos com zumbis; o mob Hollow ficará para estudo futuro**. A quest 2 também usa esqueletos e carne podre: seus objetivos reais precisam continuar explícitos.

## 3. Objetivos

- Tornar confiável o ciclo criar personagem → quests → recompensas → evolução → combate → salvar/continuar.
- Resolver todos os achados R01–R18, respeitando diferenças entre conteúdo padrão, multiplayer e configurações opcionais.
- Corrigir V01–V08 com verificação visual dentro do jogo.
- Unificar progressão na tela de status, sem obrigar o diário a funcionar como tela de personagem.
- Permitir gasto de pontos em poucas categorias úteis e compreensíveis.
- Facilitar seleção de transformações e skills já desbloqueadas.
- Facilitar testes reproduzíveis por comandos de operador.
- Atualizar toda documentação afetada em Docs junto de cada mudança de lógica.

## 4. Fora do escopo atual

- Mob Hollow próprio, suas variantes e IA temática.
- Raças Hollow, Quincy e Fullbringer.
- Dimensões, NPCs complexos, party e grande catálogo de técnicas.
- Árvore extensa de atributos, classes ou builds.
- PvP competitivo e infraestrutura externa.
- Reescrita geral, troca de loader ou inclusão de biblioteca pesada sem necessidade demonstrada.

Multiplayer básico com servidor dedicado e duas pessoas continua necessário para validar o MVP. Assets podem ser corrigidos sem introduzir modelos animados complexos.

## 5. Premissas

- Minecraft 1.20.1, Forge 47.4.10, Java 17.
- Preservar progresso existente; não resetar saves silenciosamente.
- Reutilizar registries, capability e serviços atuais, corrigindo fronteiras e responsabilidades.
- A tela de status assume compras; o diário concentra missões e pode oferecer atalho para status.
- O radial seleciona forma; executar transformação continua sendo ação distinta, preservando carga/custo.
- As sete categorias confirmadas estão implementadas; coeficientes ainda devem ser observados em teste dedicado/PvP.
- Novos atalhos devem ser remapeáveis e escolhidos verificando conflitos. Não fixar teclas arbitrárias no manual antes de implementar.
- Comportamentos planejados não devem ser descritos como disponíveis no manual do jogador.

## 6. Requisitos

### Funcionais

1. Estado do cliente deve refletir o servidor, incluindo recursos, formas, progressão e aparência pública.
2. Tela de status deve mostrar raça, forma ativa/selecionada, skill, mastery, pontos, categorias e custos.
3. Compra deve mostrar resultado esperado, custo, saldo restante e motivo de bloqueio.
4. Radial deve apresentar transformações desbloqueadas e habilidades ativas utilizáveis; passivas ficam na tela de status.
5. Formas desbloqueadas mas momentaneamente não selecionáveis devem explicar o motivo.
6. Comandos de teste devem conceder pontos, elevar skill, fornecer Asauchi e preparar mastery/energia para testes.
7. Todo objetivo de quest deve informar entidade/item real.
8. Transformações devem oferecer benefício de combate limitado e verificável.
9. Asauchi deve ter forma normal de reposição.
10. JSON inválido deve produzir erro acionável e preservar o último estado válido.

### Não funcionais

- UI legível em pt_br/en_us, janelas pequenas e múltiplas escalas de GUI.
- Sem mudanças de regra dependentes do framerate.
- Sem flood de pacotes/mensagens por manter tecla pressionada.
- Assets legíveis no tamanho efetivo de renderização, com fontes de arte preservadas.
- Snapshot e atualizações parciais devem ter contratos distintos e testáveis.

### Segurança

- Direção de pacote explícita e validação no servidor.
- Cliente envia intenção/ID; nunca saldo final, nível concedido, dano ou custo definitivo.
- Operações de compra/resgate não duplicam efeitos por repetição.
- Comandos exigem permissão de operador; sem bypass por estar em modo criativo.
- Aparência pública não distribui histórico privado de quests.
- Encontros QUEST, se habilitados, validam dono e tentativa.

### Operacionais e documentação

Cada alteração na lógica deve revisar **todos os arquivos .md pertinentes em Docs**, não apenas o manual. O trabalho só termina com código, testes e documentação coerentes.

Os documentos 00–12 descrevem principalmente a engenharia reversa do Dragon Mine Z. Quando afetados, preservar essa origem e adicionar comparação/nota do Bleach; não reescrever o histórico como se descrevesse uma implementação que nunca existiu no original.

## 7. Solução proposta

### Arquitetura e servidor

- Separar snapshots de definições entre servidor e cliente; enviar formas no login/reload.
- Restringir direções C2S/S2C e centralizar validações de transição.
- Tratar início, cancelamento, falha e conclusão da carga explicitamente.
- Centralizar concessão/compra de progressão em serviço reutilizado por UI, quests e comandos.
- Sincronizar somente se necessário, em frequência limitada; eventos terminais enviam estado imediatamente.
- Carregar/validar JSON em estruturas temporárias antes de publicá-las.
- Dar identidade/versionamento a objetivos/recompensas para evolução do conteúdo.

### Interface e correções visuais

**Seleção de personagem:** fundo com cobertura da tela preservando proporção, corte controlado e personagem/controles dentro de área segura. Verificar telas largas e estreitas; nunca esticar arbitrariamente a arte. Mostrar estado de confirmação pendente até resposta do servidor.

**Transparência:** inspecionar PNG real, canais alpha e pixels de borda, testar sobre fundo sólido claro/escuro e então conferir blend/renderização. Não remover transparência globalmente: fundo do recorte deve ser transparente, corpo/arma devem permanecer legíveis.

**Espada:** verificar orientação do cabo/lâmina na textura e transforms de mão direita/esquerda, primeira/terceira pessoa e inventário. Validar Asauchi selado, Shikai e Bankai. Ajustar a causa correta, sem rotacionar a arte e o modelo duas vezes.

**HUD:** reservar áreas para energia, forma ativa, alvo selecionado, carga e pontos. Uniformizar baseline, margens e espaçamento; evitar colisão com hotbar, chat e outros painéis vanilla. Não sacrificar legibilidade para conservar uma textura.

**Diário:** alinhar ícones de status e seleção, usar nomes reais dos objetivos, atualizar botões por estado, dimensionar conteúdo pela altura do texto e adicionar rolagem antes de superar cinco missões.

**Carga:** animação visual interpolada entre valores autorizados, com indicação de alvo e cancelamento. A interpolação não antecipa desbloqueio, custo ou aplicação da forma.

### Tela de status e categorias

Categorias implementadas:

| Categoria | Efeito |
|---|---|
| Zanjutsu / Hakuda | +10% por nível com Zanpakutō / desarmado |
| Vitalidade / Resistência | +2 de vida máxima / mitigação de golpe direto com retorno decrescente |
| Kidou | +10% por nível para futuros ataques de feitiço |
| Reserva / Controle | +20 de máximo / drain dividido por `1 + 0,10 × nível` |

Não há teto de nível de gameplay. O custo cresce em 100 por nível e satura em 1.000.000 por compra. Controle nunca torna o drain zero/negativo. BP é a soma dos sete níveis multiplicada pela reiatsu máxima/10 e não altera combate.

A revisão anterior propôs pontos como custo de desbloqueio para evitar moeda sem uso. Com a solicitação explícita de categorias, **a economia precisa ser reavaliada**: pontos passam a financiar melhorias e eventualmente skills; não manter compra redundante de algo que a quest concede grátis sem deixar o benefício claro. A implementação deve fechar uma tabela única de desbloqueios, custos e recompensas.

Quests podem despertar formas; mastery começa baixo e cresce com uso; pontos sustentam os aprimoramentos. O contrato de TransformationReward deve aceitar zero real e não convertê-lo para 100.

### Radial de transformações e habilidades

- Abrir com tecla remapeável; selecionar por cursor e confirmar de modo consistente.
- Mostrar forma ativa, selecionada, custo/restrição e ícones legíveis.
- Selecionar transformação não deve ativá-la instantaneamente nem ignorar gates.
- Listar somente ações implementadas; não inventar slots funcionais para técnicas ainda ausentes.
- Skills passivas e upgrades não aparecem como ações executáveis.
- Se não houver técnica ativa no momento da entrega, o radial funciona com transformações e explica o estado vazio da seção de técnicas.
- Manter atalhos diretos compatíveis e impedir ativação involuntária ao fechar, abrir menu ou desconectar.
- Quando uma primeira técnica curta entrar, usar o mesmo serviço de validação de energia/cooldown.

### Comandos de desenvolvimento

Família proposta: /bleachdev, com permissão 2, parâmetro de jogador e feedback ao operador. Os nomes abaixo são contratos planejados, ainda indisponíveis.

- points add <player> <amount>
- skill set <player> <skill> <level>
- mastery set <player> <group> <form> <value>
- reiatsu fill <player>
- asauchi give <player>
- inspect <player>

Usar sugestões de IDs válidos, rejeitar valores fora de faixa e sincronizar após mutação. Conceder Asauchi cobre o item do MVP; o /give vanilla continua suficiente para itens genéricos. inspect deve mostrar o estado necessário para diagnóstico, sem despejar todo NBT.

Comandos de set podem reduzir nível/mastery, ao contrário de recompensas que só elevam. Essa operação precisa de API explícita de administração e deve revalidar forma ativa/selecionada e bônus. Não reutilizar setter monotônico e anunciar sucesso quando nada mudou.

Reset geral de personagem/quests não entra automaticamente nesta família; não é necessário para a solicitação de concessões e pode apagar progresso. Preferir mundos de teste e fixtures reprodutíveis.

### Banco de dados e integrações

Não há banco externo, API HTTP ou integração nova. Persistência continua em NBT; conteúdo configurável em JSON local. Novas telas e comandos são clientes dos serviços internos.

### Erros

Usar resultado estruturado: sucesso, pontos insuficientes, nível máximo, ID inválido, forma bloqueada, mastery insuficiente, energia insuficiente, ação indisponível. UI deve apresentar mensagem contextual sem exposição de detalhes internos irrelevantes.

## 8. Contratos de rede planejados

| Intenção | Dados enviados pelo cliente | Validação/resultado |
|---|---|---|
| Comprar skill | skillId | Servidor calcula custo, valida nível/saldo e retorna estado |
| Comprar categoria | categoryId | Servidor compra somente o próximo nível permitido |
| Selecionar forma | groupId, formId | Desbloqueio e possibilidade de seleção; sem ativação |
| Selecionar habilidade ativa | abilityId, se houver técnica implementada | Habilidade existe, desbloqueada e selecionável |
| Executar/cancelar ação | Tipo/ID da ação permitido | Estado atual, forma, energia e cooldown no servidor |
| Sincronizar definições | Exclusivamente servidor → cliente | Snapshot validado de forms/quests/categorias relevantes |
| Sincronizar progressão | Exclusivamente servidor → dono | Estado privado necessário à tela e HUD |
| Sincronizar aparência | Exclusivamente servidor → observadores/dono | Somente dados públicos da forma |

Tamanho de IDs, quantidade de entradas e direção devem ser limitados. Mudanças incompatíveis exigem nova versão de protocolo. Respostas confirmam o estado autorizado; não confiar em alteração otimista de saldo no cliente.

## 9. Dados e migrações

- Adicionar schemaVersion ao estado persistido.
- Guardar níveis comprados por categoryId, não bônus calculados acumulados.
- Saves antigos recebem nível zero em categorias, preservando pontos, skills, quests e mastery existentes.
- Não reduzir automaticamente mastery 100 de saves antigos ao rebalancear recompensas novas.
- Separar valores base, modificadores permanentes e efeitos temporários de forma.
- Definir como atualiza currentReiatsu ao aumentar máximo; proposta conservadora: preservar energia atual e permitir regeneração, sem cura gratuita implícita.
- Persistir seleção de habilidade apenas se houver ações ativas; limpar referências inválidas com fallback seguro.
- Carga pressionada não é estado durável: reset em eventos de ciclo de vida apropriados.
- Versionar quests e garantir identidade estável de objetivos/recompensas.
- Migração deve ser testada com save antigo e backup; versões antigas do mod não devem abrir saves migrados sem compatibilidade comprovada.

## 10. Alternativas

| Alternativa | Benefício | Custo | Decisão |
|---|---|---|---|
| Concentrar compras no diário | Reutiliza UI | Mistura personagem e missões | Migrar para status, oferecer atalho |
| Árvore grande de atributos | Muitas builds | Amplia balanceamento e persistência | Adiar; poucas categorias |
| Radial ativa forma ao selecionar | Menos ações | Conflita com carga e seleção atual | Preservar seleção separada da execução |
| Criar Hollow agora | Identidade temática | IA, modelos, spawn, balanceamento | Adiar conforme usuário; manter zumbis |
| Comandos alteram NBT diretamente | Implementação aparente rápida | Ignora invariantes e sync | Usar serviços e validação |
| Recriar toda arte | Liberdade visual | Pode repetir erro de renderização | Diagnosticar e corrigir primeiro |

## 11. Plano de implementação

As faixas abaixo são dimensionamento inicial por tarefa, não compromisso de prazo. Dividir tarefas que ultrapassarem oito horas depois da reprodução. Dependências de configuração local e testes visuais podem alterar esforço.

| ID | Tarefa | Faixa inicial | Depende de | Concluída quando |
|---|---|---:|---|---|
| T01 | Restringir direções e separar registries lógicos | 4–8 h | — | R02/R12 cobertos e pacotes invertidos rejeitados |
| T02 | Sincronizar forms e aparência de observadores | 4–8 h | T01 | R01/R08 funcionam em dedicado com dois clientes |
| T03 | Serviço de concessão e comandos points/skill | 3–6 h | T01 | F03 parcial; permissões, limites e sync testados |
| T04 | Comandos mastery/reiatsu/asauchi/inspect | 2–4 h | T03 | Fixtures de progressão reproduzíveis sem editar NBT |
| T05 | Corrigir recursos, carga e transições | 4–8 h | T01 | R04/R06/R07; sem spam, salto indevido ou estado preso |
| T06 | Corrigir rastreamento e ciclo de capability | 3–6 h | T01 | R05/R18; respawn, logout e dimensão verificados |
| T07 | Corrigir botões e objetivos do diário | 3–6 h | T01 | R03/R09/C01; alvo real e ações atualizadas |
| T08 | Diagnosticar e corrigir transparência/orientação | 3–6 h | — | V02/V03/V07/V08 verificados nas três formas |
| T09 | Corrigir background e confirmação | 2–4 h | T01 | V01; cobertura responsiva e confirmação sem reabrir |
| T10 | Reorganizar HUD, marcações e layout do diário | 4–8 h | T05,T07,T08 | V04/V05; conteúdo legível nas escalas escolhidas |
| T11 | Suavizar carga e organizar toasts | 2–4 h | T05,T10 | V06; animação segue estado real e avisos não se atropelam |
| T12 | Corrigir conclusão ITEM e kills sequenciais | 3–6 h | T03 | R13/R14 com regressões verificadas |
| T13 | Completar ou restringir spawn QUEST | 3–6 h | T12 | R15; dono/tentativa/configurações suportadas explícitos |
| T14 | Validar conteúdo e implementar reload atômico | 4–8 h | T01 | R10/R16; configuração anterior preservada em erro |
| T15 | Versionar progresso de quest e resgates | 4–8 h | T14 | R11/R17; sem perdas/duplicação por reordenação |
| T16 | Fechar tabela de economia/categorias/mastery | 2–4 h | T03,T05 | Papéis e números documentados, sem compra redundante |
| T17 | Implementar categorias e migração NBT | 4–8 h | T06,T16 | Níveis/custos validados; save antigo preservado |
| T18 | Aplicar benefícios das formas e categorias | 4–8 h | T05,T17 | Combate útil, bônus não acumulam nem sobrevivem indevidamente |
| T19 | Criar tela de status unificada | 4–8 h | T17,T18 | F01; leitura/compra/status de bloqueio completos |
| T20 | Criar radial de formas | 4–8 h | T02,T05,T19 | F02 parcial; seleção sem ativação acidental |
| T21 | Integrar ações ativas ao radial, se implementadas | 2–4 h | T20 | Apenas habilidades reais, sem passivas executáveis |
| T22 | Reposição de Asauchi e quests didáticas | 3–6 h | T16,T18 | Ciclo do MVP completo usando mobs provisórios |
| T23 | Textos, rolagem, tamanhos de assets e limpeza dirigida | 4–8 h | T10,T19,T20 | UI/docs consistentes; sexto item navegável |
| T24 | Regressão final, build e teste com duas pessoas | 4–8 h | T01–T23 | Critérios do MVP atendidos e pendências explicitadas |

F03 foi antecipado porque acelera teste de Shikai/Bankai, custos e regressões. F01/F02 são prioridades de produto, mas dependem de estado e regras corretos. A tarefa T21 não autoriza criar um catálogo novo de skills; uma primeira técnica limitada permanece evolução posterior ao combate básico, conforme a revisão.

### Cobertura de recomendações anteriores

- R01/R02/R08/R12 → T01/T02.
- R03/R09 → T07/T10.
- R04/R06/R07 → T05/T11.
- R05/R18 → T06.
- R10/R16 → T14.
- R11/R17 → T15.
- R13/R14 → T12.
- R15 → T13.
- Combate, economia, mastery, reposição → T16/T17/T18/T22.
- Usabilidade, assets, traduções, manutenção → T08–T11/T19/T20/T23.
- Testes e documentação → requisito de cada tarefa, além da rodada T24.

### Matriz de documentação obrigatória

| Mudança | Documentos a revisar no mesmo trabalho |
|---|---|
| Qualquer gameplay | `jogador/manual-do-jogador.md`, `Docs/README.md` e status deste plano |
| Quests/rewards/defaults | `arquitetura/03-sistema-quests.md`, manual, README e exemplos pertinentes |
| Formas/mastery/custos/atributos | `arquitetura/04-sistema-evolucao.md`, `arquitetura/05-sistema-habilidades.md`, manual |
| Estado/NBT/migração | `arquitetura/02-capability-system.md`, `arquitetura/07-persistencia-nbt.md` |
| Rede e autoridade | `arquitetura/06-rede-sincronizacao.md`, `arquitetura/01-arquitetura-geral.md` quando aplicável |
| UI/atalhos/radial/status | `arquitetura/08-ui-hud.md`, manual, `arte/prompts-arte-mvp.md` quando assets mudarem |
| Eventos/ciclo do jogador | `arquitetura/09-eventos-forge.md` e documentos dos sistemas afetados |
| Build/testes/toolchain | `arquitetura/10-build-dependencias.md`, README |
| Termos/créditos/assets | `arquitetura/11-glossario.md` e `arquitetura/12-licenciamento-e-creditos.md` quando houver mudança real |

A matriz é ponto de partida, não lista exaustiva: pesquisar referências antigas em todos os .md de Docs e corrigir contradições relacionadas. Não reescrever arquivos sem relação com a mudança. Documentar explicitamente limites ainda não implementados.

## 12. Testes

- Reutilizar toda a matriz da revisão R01–R18.
- Imagens antes/depois para V01–V08 em primeira/terceira pessoa, mão principal/secundária e três formas.
- Conferir transparência em neve/fundo claro e cenário escuro; silhueta opaca, recorte transparente correto.
- Testar UI em 16:9, 4:3 e janela estreita, nas escalas disponíveis; não cobrir controles fora da tela.
- Abrir/fechar status e radial, cancelar, pressionar teclas simultâneas e repetir ações sob latência.
- Comprar com saldo exato, insuficiente, nível máximo, ID inválido e requisições repetidas.
- Alterar skill/mastery por comando para cima e para baixo; forma ativa inválida deve ser normalizada.
- Jogador sem permissão não executa /bleachdev; operador recebe confirmação do alvo e efeito.
- Confirmar dano/energia antes e depois de transformação, morte, reload e reconexão, sem acumulação.
- Migrar save anterior sem apagar pontos/mastery/progresso; testar JSON inválido e versão incompatível.
- Build, testes de regras e integração; rodada real dedicado + dois clientes.
- Verificar manual e demais .md contra a implementação concluída.

## 13. Entrega e reversão

- Implementar em entregas pequenas na ordem de dependência; atualizar status deste plano.
- Antes de migrações, guardar backup de mundo/configuração de teste.
- Validar primeiro serviços e estados, depois telas e conteúdo rebalanceado.
- Preservar comportamento/configuração antiga quando leitura falhar; indicar migração necessária.
- Reverter código isoladamente só quando formato de save permanecer compatível. Em mudança incompatível, restaurar backup correspondente.
- Defaults novos não sobrescrevem automaticamente JSON existente: explicar como migrar worlds antigos.
- Não publicar nem declarar funcionalidade entregue apenas porque a documentação está planejada.

## 14. Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Tratar bug de alpha como arte ruim sem verificar renderer | Retrabalho e persistência do defeito | Comparar asset isolado e render em jogo |
| Categorias somadas a mastery/forma sem fórmula clara | Bônus excessivo ou duplicado | Tabela única de cálculo e testes de ciclo |
| Radial confundir escolher com transformar | Consumo/ativação inesperados | Intenções distintas e feedback explícito |
| /bleachdev ignorar invariantes | Estado inválido nos testes | Serviço central, limites e revalidação |
| Atualizar só manual e deixar referências conflitantes | Manutenção guiada por informação errada | Matriz e busca em todos os .md |
| Rebalancear saves antigos silenciosamente | Perda de conquistas | Migração explícita, preservar mastery adquirido |
| UI crescer junto com catálogo inexistente | Expansão desnecessária | Somente conteúdo implementado e poucas categorias |

## 15. Decisões ainda a fechar durante implementação

Não impedem registrar o plano; devem ser fechadas antes das tarefas dependentes.

- Reavaliar coeficientes após teste dedicado/PvP; nomes, papéis, ausência de cap e custos das sete categorias já estão definidos.
- Papel final da compra de Zanpakutō frente às recompensas de quest.
- Política de reembolso/respec: não prometida no MVP; evitar botão sem regra definida.
- Atalhos e gesto de confirmação/cancelamento do radial após verificar conflitos.
- Benefício exato de Shikai/Bankai e eventual primeira técnica ativa.
- Receita ou recuperação do Asauchi.
- Se habilidades exibidas no radial são selecionadas para uso posterior ou executadas diretamente; formas sempre conservam seleção distinta da carga nesta proposta.
- Futuro mob Hollow: estudo separado, sem bloquear a implementação atual com zumbis.

