# Plano de Análise do Repositório Dragon Mine Z
### Especificação de trabalho para o modelo de IA responsável pela engenharia reversa

## 0. Objetivo geral

Analisar o repositório completo do Dragon Mine Z (mod Forge) e produzir uma pasta `/Docs/arquitetura` tão completa que **um segundo modelo (ou um humano) consiga recriar um mod equivalente — com o sistema de Quests e Evolução — sem precisar reabrir o repositório original**, exceto pelos arquivos de lógica explicitamente preservados como referência (ver seção 6).

A saída final deve ter dois componentes:
1. `/Docs/arquitetura/*` — documentação estruturada, em texto, sem código copiado literalmente (isso é importante pela licença GPL-3.0 — documentar o *comportamento e a arquitetura*, não colar blocos inteiros do código original)
2. `/reference-code/*` — um subconjunto pequeno e deliberado de arquivos-fonte originais, preservados como referência técnica direta (permitido sob GPL-3.0, já que o fork mantém a mesma licença)

---

## 1. Escopo da análise

**Dentro do escopo (prioridade alta):**
- Sistema de Quests (registro, progresso, recompensas, persistência)
- Sistema de Evolução/Progressão (raças/classes, estágios, requisitos de transição)
- Capability system (estrutura de dados do jogador)
- Sincronização client-server (packets relacionados aos dois sistemas acima)
- Persistência em NBT desses dados
- UI/HUD relacionada a quests e evolução

**Dentro do escopo (prioridade média — documentar de forma mais resumida):**
- Sistema de habilidades/skills (se acoplado à evolução)
- Sistema de eventos do Forge usados como gatilho de quests (dano, kill, bloco quebrado, etc.)
- Sistema de registro de itens/blocos/entidades relacionados aos dois sistemas centrais

**Fora do escopo (não precisa de documentação detalhada, só menção):**
- Conteúdo específico de Dragon Ball (transformações, biomas, itens temáticos) — só interessa a *mecânica*, não o *tema*
- Assets visuais (texturas, modelos, sons)
- Sistemas totalmente desconectados de quest/evolução (ex: sistema de clima, se existir)

---

## 2. Estrutura de pastas de saída

Os documentos gerados por esta especificação ficam em `/Docs/arquitetura/` após a reorganização da pasta `Docs`. A listagem abaixo é a nomenclatura original.

```
/Docs/arquitetura
  00-overview.md
  01-arquitetura-geral.md
  02-capability-system.md
  03-sistema-quests.md
  04-sistema-evolucao.md
  05-sistema-habilidades.md
  06-rede-sincronizacao.md
  07-persistencia-nbt.md
  08-ui-hud.md
  09-eventos-forge.md
  10-build-dependencias.md
  11-glossario.md
  12-licenciamento-e-creditos.md
/reference-code
  (arquivos-fonte originais preservados, ver seção 6)
```

---

## 3. Especificação de cada documento

Cada arquivo abaixo deve seguir o **template da seção 4** (não é opcional — precisa ser uniforme pra facilitar o uso pelo próximo modelo).

### 00-overview.md
- Resumo do que o mod original faz (2-3 parágrafos)
- Lista das mecânicas centrais que serão reaproveitadas (quest + evolução) vs. as que não serão
- Diagrama textual (pode ser em Mermaid) mostrando como os sistemas principais se conectam entre si

### 01-arquitetura-geral.md
- Estrutura de pacotes do projeto original (árvore de diretórios comentada — o que cada pacote contém)
- Ponto de entrada do mod (`@Mod` class, `FMLCommonSetupEvent`, etc.)
- Lista de todas as Capabilities registradas e o que cada uma armazena
- Lista de todos os `SimpleChannel`/packets de rede existentes (nome, direção client↔server, quando é disparado)
- Convenções de nomenclatura observadas no projeto (isso ajuda o próximo modelo a escrever código consistente)

### 02-capability-system.md
Para a(s) Capability(ies) relacionada(s) a quest/evolução:
- Nome da interface e da implementação
- Cada campo: nome, tipo, o que representa, valor padrão
- Como é anexada ao jogador (`AttachCapabilitiesEvent`)
- Como é serializada/deserializada (NBT)
- Getters/setters relevantes e quem os chama

### 03-sistema-quests.md — o mais importante, documentar em detalhe máximo
- Modelo de dados de uma Quest: campos, tipos, valores possíveis
- Como uma quest é **definida** (hardcoded em Java? JSON? registry?) — se for JSON, incluir o schema completo com um exemplo real (sem copiar conteúdo temático, só estrutura)
- Como o **progresso** é rastreado (o que dispara incremento — evento de kill, coleta de item, etc. — listar cada tipo de objetivo suportado)
- Como uma quest é **completada** (condição de finalização) e o que acontece no momento da conclusão
- Como a **recompensa** é entregue (itens, XP, desbloqueio de habilidade)
- Relação entre quests (pré-requisitos, quests em sequência/árvore, se existir)
- Fluxo completo passo a passo, do registro da quest até a recompensa, em formato de lista numerada
- Qualquer edge case tratado no código original (ex: o que acontece se o jogador morre com quest em progresso)

### 04-sistema-evolucao.md — segundo mais importante
- Modelo de dados de "estágio de evolução" (enum? int? classe própria?)
- Lista completa dos estágios existentes e a ordem entre eles
- Requisitos de transição entre cada estágio (XP, quest específica, item consumido, tempo, combinação de fatores)
- O que muda no jogador ao evoluir (atributos, habilidades desbloqueadas, aparência/modelo, se aplicável)
- Como a transição é **disparada** no código (verificação a cada tick? evento específico? ação do jogador?)
- Como isso é comunicado ao jogador (UI, mensagem, som)
- Fluxo completo passo a passo, do acúmulo de progresso até a mudança de estágio

### 05-sistema-habilidades.md
- Como uma habilidade é modelada (interface, classe abstrata)
- Ciclo de vida de uma habilidade (ativação, cooldown, custo de energia)
- Como habilidades são associadas a estágios de evolução ou a quests completadas

### 06-rede-sincronizacao.md
- Cada packet relacionado a quest/evolução: nome, payload (campos), direção, quando é enviado
- Estratégia de sincronização (a cada mudança? periodicamente? sob demanda ao logar?)

### 07-persistencia-nbt.md
- Estrutura NBT exata usada para salvar quest progress e evolution stage
- Onde esse NBT é lido/escrito (save do mundo, save do jogador)

### 08-ui-hud.md
- Telas existentes relacionadas a quest/evolução (inventário de quests, tela de status/evolução)
- Como a tela busca os dados (lê da Capability local? pede ao servidor?)
- Elementos visuais principais de cada tela (sem precisar documentar pixel a pixel, só a estrutura funcional)

### 09-eventos-forge.md
- Lista de todos os `@SubscribeEvent` usados pelos sistemas de quest/evolução
- O que cada handler faz, resumidamente

### 10-build-dependencias.md
- Versão do Minecraft/Forge usada
- Dependências externas relevantes (libs de terceiros sob MIT, mapeadas na seção de licença)
- Passos de build (`gradlew build`, configurações especiais do `build.gradle`)

### 11-glossario.md
- Termos específicos do domínio usados no código original (ex: nomes de eventos customizados, nomenclatura própria do autor) traduzidos/explicados

### 12-licenciamento-e-creditos.md
- Cópia do texto da licença GPL-3.0 aplicável
- Lista de dependências MIT identificadas e seus avisos de copyright
- Texto de crédito ao projeto original, pronto pra colar no README do fork

---

## 4. Template obrigatório para cada documento de sistema (02 a 09)

```markdown
# <Nome do Sistema>

## Resumo
(1 parágrafo do que o sistema faz e por que existe)

## Modelo de dados
(estruturas, campos, tipos — em tabela)

## Fluxo de funcionamento
(passo a passo numerado, do início ao fim)

## Pontos de integração
(quais outros sistemas ele lê ou escreve dados)

## Decisões de design observadas
(por que parece ter sido feito assim — inferências do modelo, marcadas como tal)

## Riscos/complexidades para reimplementar
(o que provavelmente vai dar trabalho ao portar pra Bleach)
```

---

## 5. Arquivos de código-fonte a preservar em `/reference-code`

Critério: só preservar arquivos onde a **lógica de cálculo/algoritmo** é complexa o suficiente para que reescrever do zero, só com a documentação em texto, tenha alto risco de erro sutil. Exemplos típicos:
- Classe que calcula requisitos de transição de estágio (se envolver fórmulas)
- Classe que resolve conflitos de progresso de quest (se houver lógica não trivial)
- Serializador/deserializador NBT customizado, se não for boilerplate padrão do Forge

**Não preservar**: classes de registro simples, boilerplate de setup do Forge, getters/setters, classes de UI puramente visuais — essas devem ser recriadas do zero a partir da documentação, não copiadas.

Cada arquivo preservado precisa vir acompanhado de um comentário no topo indicando: origem, licença (GPL-3.0), e por que foi preservado como referência direta.

---

## 6. Metodologia de execução (ordem sugerida para o modelo)

1. Ler o `build.gradle` e estrutura geral do projeto → gerar `10-build-dependencias.md`
2. Mapear pacotes e pontos de entrada → gerar `01-arquitetura-geral.md`
3. Localizar e documentar Capabilities → gerar `02-capability-system.md`
4. Seguir o fluxo de quests do registro até a recompensa → gerar `03-sistema-quests.md`
5. Seguir o fluxo de evolução do progresso até a transição de estágio → gerar `04-sistema-evolucao.md`
6. Documentar habilidades, rede, persistência, UI e eventos → gerar `05` a `09`
7. Compilar termos técnicos encontrados → gerar `11-glossario.md`
8. Confirmar licença e gerar `12-licenciamento-e-creditos.md`
9. Selecionar e copiar os arquivos de `/reference-code` conforme critério da seção 5
10. Gerar `00-overview.md` por último (só faz sentido depois de já ter mapeado tudo)

---

## 7. Checklist de "pronto para recriar sem o repositório original"

- [x] Todo campo de dado usado por quest/evolução está documentado com tipo e propósito
- [x] O fluxo completo de uma quest, do início ao fim, pode ser seguido só lendo `03-sistema-quests.md`
- [x] O fluxo completo de uma evolução, do início ao fim, pode ser seguido só lendo `04-sistema-evolucao.md`
- [x] Nenhum documento contém blocos de código copiados literalmente do original (paráfrase técnica, não cópia)
- [x] Todo arquivo em `/reference-code` tem justificativa registrada de por que foi preservado
- [x] A licença e os créditos estão documentados e prontos para uso no fork
- [x] Um desenvolvedor que nunca viu o Dragon Mine Z consegue, só com `/docs`, desenhar o diagrama de classes do sistema de quests e evolução de cabeça

