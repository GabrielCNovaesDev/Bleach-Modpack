# [Nome do Mod] — Bleach para Minecraft (Forge)

> ⚠️ **Projeto em desenvolvimento.** Este README descreve o escopo planejado e será atualizado conforme o desenvolvimento avança.

## Aviso legal

Este é um projeto **fã-feito (fan-made), não-oficial e não-comercial**, inspirado no universo de *Bleach*, criado por Tite Kubo. Não há qualquer afiliação com Kubo, Shueisha, Studio Pierrot ou detentores dos direitos da obra. Todo o conteúdo temático (nomes, conceitos, personagens) pertence aos seus respectivos criadores; apenas o código deste mod é de autoria da equipe listada abaixo.

Este projeto é um trabalho derivado do mod **Dragon Mine Z**, distribuído sob licença **GPL-3.0**. Em conformidade com os termos da licença, este projeto também é distribuído sob **GPL-3.0** — o código-fonte completo está disponível publicamente neste repositório.

---

## Sobre o projeto

Um mod de Minecraft que traz o universo de Bleach, com foco no sistema de progressão de poder (raça → estágio) e em missões data-driven. A arquitetura de quests e de evolução é reaproveitada do Dragon Mine Z; o conteúdo (Shinigami, Zanpakutō, Shikai/Bankai, reiatsu, arcos) é original deste projeto.

A documentação de engenharia reversa está em `Docs/00-overview.md` … `Docs/12-licenciamento-e-creditos.md`. Trechos de lógica não trivial do original estão em `/reference-code` (não entram no compile). O código do mod Bleach vive em `/src`.

## Baseado em

- **Projeto original:** [Dragon Mine Z](https://github.com/DragonMineZ/dragonminez)
- **Licença original:** GPL-3.0 (+ GeckoLib sob MIT; ver `12-licenciamento-e-creditos.md`)
- **O que foi reaproveitado:** arquitetura do sistema de quests (JSON no mundo, objetivos, rewards, persistência) e do sistema de evolução/progressão (raça → grupo → estágio + mastery)
- **O que é original deste projeto:** todo o conteúdo temático de Bleach, raças, habilidades, balanceamento e conteúdo visual

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

Alinhados à plataforma do original para que a doc de capability/packets/eventos continue válida (ver `10-build-dependencias.md`):

- **Minecraft:** 1.20.1
- **Forge:** 47.4.x (original usa 47.4.10)
- **Java:** 17
- **Build:** Gradle (`./gradlew build`)
- **Dependências mínimas no MVP:** só Forge. GeckoLib / TerraBlender / Curios / MariaDB ficam de fora até fases futuras.

## Como rodar em ambiente de desenvolvimento

```bash
./gradlew genIntellijRuns
./gradlew runClient
./gradlew build
```

Java 17 é obrigatório. Manual de jogo (controles, quests, progressão): [`manual-do-jogador.md`](manual-do-jogador.md). Admin: `/bleachreload quests`.

## Estrutura do repositório

```
/Docs
  README.md                         → este arquivo (visão do projeto Bleach)
  manual-do-jogador.md              → como jogar (controles, quests, levelling) — atualizar a cada mecânica nova
  plano-analise-dragon-mine-z.md    → especificação da análise
  00-overview.md … 12-*.md          → referência de arquitetura para recriar quests + evolução
/reference-code                     → fontes originais pontuais (GPL-3.0), ver README lá
/src                                → código-fonte do mod Bleach (Forge 1.20.1)
```

## Roadmap

| Fase | Descrição | Status |
|---|---|---|
| Fase 0 | Análise do repositório base + documentação | Concluída |
| Fase 1 (MVP) | Raça Shinigami + Quests básicas | Implementada |
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
