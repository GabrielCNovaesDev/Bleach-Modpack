# [Nome do Mod] — Bleach para Minecraft (Forge)

> ⚠️ **Projeto em desenvolvimento.** Este README descreve o escopo planejado e será atualizado conforme o desenvolvimento avança.

## Aviso legal

Este é um projeto **fã-feito (fan-made), não-oficial e não-comercial**, inspirado no universo de *Bleach*, criado por Tite Kubo. Não há qualquer afiliação com Kubo, Shueisha, Studio Pierrot ou detentores dos direitos da obra. Todo o conteúdo temático (nomes, conceitos, personagens) pertence aos seus respectivos criadores; apenas o código deste mod é de autoria da equipe listada abaixo.

Este projeto é um trabalho derivado do mod **Dragon Mine Z**, distribuído sob licença **GPL-3.0**. Em conformidade com os termos da licença, este projeto também é distribuído sob **GPL-3.0** — o código-fonte completo está disponível publicamente neste repositório.

## Índice da documentação

| Pasta | Para quê | Comece por |
|---|---|---|
| [`game-design/`](game-design/00-mapa-modular.md) | Planejamento modular da experiência, conteúdo e progressão | [Mapa modular](game-design/00-mapa-modular.md) |
| [`desenvolvimento/`](desenvolvimento/manual-inicializacao.md) | Clone, Java 17, JAVA_HOME e `runClient` | [Manual de inicialização](desenvolvimento/manual-inicializacao.md) |
| [`jogador/`](jogador/manual-do-jogador.md) | Jogar o MVP (controles, quests, progressão) | [Manual do jogador](jogador/manual-do-jogador.md) |
| [`planejamento/`](planejamento/plano-implementacao-mvp.md) | Plano de entrega, revisão técnica e especificação da análise | [Plano do MVP](planejamento/plano-implementacao-mvp.md) |
| [`arte/`](arte/prompts-arte-mvp.md) | Briefing e prompts de texturas/UI | [Prompts de arte](arte/prompts-arte-mvp.md) |
| [`arquitetura/`](arquitetura/00-overview.md) | Engenharia reversa do Dragon Mine Z (série 00–12) | [Overview](arquitetura/00-overview.md) |

---

## Sobre o projeto

Um mod de Minecraft que traz o universo de Bleach, com foco no sistema de progressão de poder (raça → estágio) e em missões data-driven. A arquitetura de quests e de evolução é reaproveitada do Dragon Mine Z; o projeto adapta raças, poderes, arcos e personagens de Bleach para uma experiência jogável própria.

A documentação de engenharia reversa está em [`arquitetura/`](arquitetura/00-overview.md). Trechos de lógica não trivial do original estão em `/reference-code` (não entram no compile). O código do mod Bleach vive em `/src`.

## Baseado em

- **Projeto original:** [Dragon Mine Z](https://github.com/DragonMineZ/dragonminez)
- **Licença original:** GPL-3.0 (+ GeckoLib sob MIT; ver [`arquitetura/12-licenciamento-e-creditos.md`](arquitetura/12-licenciamento-e-creditos.md))
- **O que foi reaproveitado:** arquitetura do sistema de quests (JSON no mundo, objetivos, rewards, persistência) e do sistema de evolução/progressão (raça → grupo → estágio + mastery)
- **O que é desenvolvido neste projeto:** adaptação jogável, balanceamento, quests, integração com Minecraft e conteúdo visual; os poderes disponíveis seguem o catálogo canônico decidido nos documentos de game design

## Equipe

| Nome | Função | Contato/GitHub |
|---|---|---|
| [Nome 1] | [Core / Arquitetura] | |
| [Nome 2] | [Gameplay / Conteúdo] | |
| [Nome 3] | [Gameplay / Conteúdo] | |
| [Nome 4] | [Assets / Modelagem] | |
| [Nome 5] | [UI/UX] | |

---

## Funcionalidades

### 🎯 Escopo inicial (MVP)

- [x] **Raça: Shinigami**
  - [x] Sistema de Zanpakutō (Asauchi inicial)
  - [x] Evolução para **Shikai** (quest Soul Society 2 + skill `zanpakuto` 1; hold R para ativar)
  - [x] Evolução para **Bankai** (quest Soul Society 3 + skill `zanpakuto` 2)
- [x] **Sistema de energia (Reiatsu)**
  - [x] Barra de energia com regeneração
  - [x] Custo de energia ao transformar e drain enquanto Shikai/Bankai estiver ativo
- [x] **Atributos de combate e BP**
  - [x] Zanjutsu, Hakuda, Vitalidade, Resistência, Kidou, Reserva e Controle sem teto de nível de gameplay
  - [x] Dano armado/desarmado separado, vida máxima, mitigação física e BP informativo
- [x] **Sistema de Quests (base)**
  - [x] Registro de quests via JSON no mundo (`{world}/bleachmod/`; defaults gerados em Java se a pasta não existir)
  - [x] Objetivos KILL e ITEM
  - [x] Tela de journal (tecla J)
  - [x] Recompensas: pontos espirituais, skill e transformação
- [x] **HUD básico**
  - [x] Indicador de energia/reiatsu
  - [x] Indicador de estágio de evolução atual
- [x] **Persistência**
  - [x] Progresso de quest salvo na capability NBT
  - [x] Estágio de evolução salvo na capability NBT

### 🔜 Fases futuras (fora do MVP)

- [ ] **Raça: Hollow** (evolução: Hollow comum → Menos Grande → Adjuchas → Vasto Lorde → Arrancar)
- [ ] **Raça: Quincy** (arco/reishi → Vollständig)
- [ ] **Raça: Fullbringer**
- [ ] Árvore de quests com pré-requisitos entre missões
- [ ] Sistema de habilidades expandido por raça
- [ ] Novos biomas/dimensões temáticas (ex: Soul Society, Hueco Mundo)
- [ ] Balanceamento multiplayer/PvP
- [ ] [Adicionar outras conforme o grupo decidir]

### ❌ Fora de escopo (por ora)

- [Ex: suporte a versões antigas do Minecraft]
- [Ex: sistema de clima/dimensões complexas]
- [Preencher conforme decisão do grupo]

---

## Requisitos técnicos

Alinhados à plataforma do original para que a doc de capability/packets/eventos continue válida (ver [`arquitetura/10-build-dependencias.md`](arquitetura/10-build-dependencias.md)):

- **Minecraft:** 1.20.1
- **Forge:** 47.4.x (original usa 47.4.10)
- **Java:** 17
- **Build:** Gradle (`./gradlew build`)
- **Dependências mínimas no MVP:** só Forge. GeckoLib / TerraBlender / Curios / MariaDB ficam de fora até fases futuras.

## Como rodar em ambiente de desenvolvimento

Passo a passo do clone até o cliente: [`desenvolvimento/manual-inicializacao.md`](desenvolvimento/manual-inicializacao.md). Java 17 e `JAVA_HOME` são obrigatórios.

```bash
./gradlew runClient
```

Manual de jogo (controles, quests, progressão): [`jogador/manual-do-jogador.md`](jogador/manual-do-jogador.md). Admin: `/bleachreload quests`.

## Estrutura do repositório

```
/Docs
  README.md                         → este arquivo (visão do projeto Bleach)
  desenvolvimento/                  → clone, Java 17, JAVA_HOME e runClient
  jogador/                          → como jogar (controles, quests, levelling)
  planejamento/                     → planos, revisão técnica e especificação da análise
  arte/                             → briefing e prompts de assets do MVP
  arquitetura/                      → referência 00–12 (quests, evolução, rede, persistência)
/reference-code                     → fontes originais pontuais (GPL-3.0), ver README lá
/src                                → código-fonte do mod Bleach (Forge 1.20.1)
```

## Plano de implementação e manutenção da documentação

O plano ativo de estabilização do MVP e das próximas entregas está em [`planejamento/plano-implementacao-mvp.md`](planejamento/plano-implementacao-mvp.md). Ele consolida a [revisão técnica](planejamento/revisao-tecnica-mvp-2026-09-10.md), os bugs visuais relatados em jogo e as novas telas e ferramentas solicitadas.

A estabilização já inclui rede direcionada, persistência versionada, comandos de desenvolvimento, tela de status, categorias de pontos e seletor radial. O rework atual substitui Poder por Zanjutsu/Hakuda, adiciona Vitalidade/Resistência/Kidou, exibe BP e reduz o consumo das formas. A inspeção visual final dos assets e o teste dedicado com dois clientes continuam pendentes.

**Inimigos provisórios:** continuamos usando zumbis, com os esqueletos já presentes nas quests atuais. A criação do mob Hollow ficará para estudo posterior e não bloqueia o MVP. Textos e objetivos devem identificar os alvos reais.

**Regra de conclusão:** toda alteração na lógica do jogo deve atualizar, no mesmo trabalho, todos os arquivos `.md` pertinentes em `Docs`, incluindo manual, arquitetura, regras, rede, persistência e UI quando afetados. Pesquisar referências antigas e eliminar contradições. O plano contém uma matriz de documentos por sistema. Preservar a distinção entre a engenharia reversa de Dragon Mine Z e a implementação Bleach; não apresentar planejamento como funcionalidade disponível.

## Roadmap

| Fase | Descrição | Status |
|---|---|---|
| Fase 0 | Análise do repositório base + documentação | Concluída |
| Fase 1 (MVP) | Raça Shinigami + Quests básicas | Base implementada; estabilização pendente |
| Estabilização do MVP | Correções, status/categorias, radial, comandos e progressão útil | Planejada no plano ativo |
| Fase 2 | Raça Hollow | ⏳ Planejado |
| Fase 3 | Raças Quincy e Fullbringer | ⏳ Planejado |
| Fase 4 | Conteúdo expandido e balanceamento | ⏳ Planejado |

## Contribuindo

[Preencher se o repositório for aberto a contribuições externas, ou remover esta seção se for fechado à equipe]

## Licença

Este projeto é distribuído sob a licença **GPL-3.0** — veja o arquivo [`LICENSE`](../LICENSE) para o texto completo.

Este projeto é um trabalho derivado de Dragon Mine Z (https://github.com/DragonMineZ/dragonminez), Copyright © DragonMine Z 2025, distribuído sob GNU GPL-3.0. Autores originais: Yuseix, ezShokkoh, Bruno e contribuidores listados no repositório original.

O conteúdo temático de Dragon Ball pertence aos respectivos detentores. Este fork reaproveita a arquitetura dos sistemas de quest e de progressão/evolução; todo o conteúdo temático de Bleach é original deste projeto e não é afiliado a Tite Kubo, Shueisha ou Studio Pierrot.

## Créditos

- **Dragon Mine Z** — projeto original que serviu de base para os sistemas de quest e evolução: https://github.com/DragonMineZ/dragonminez
- **Tite Kubo / Shueisha / Studio Pierrot** — criadores do universo de Bleach (sem afiliação com este projeto)

## Estado da estabilização — 10/09/2026

Consulte o [relatório de correções](planejamento/relatorio-implementacao-mvp-2026-09-10.md) e a [checklist atual](planejamento/todo-mvp.md). Build 0.2.0 aprovado com 32 regressões; quatro GameTests aprovados. Protocolo 2.1. A transparência dos PNGs e a homologação visual com dois clientes continuam pendentes. Mantidos sete atributos, BP e schema 3.
