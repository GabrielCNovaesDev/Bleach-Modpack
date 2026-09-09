# [Nome do Mod] — Bleach para Minecraft (Forge)

> ⚠️ **Projeto em desenvolvimento.** Este README descreve o escopo planejado e será atualizado conforme o desenvolvimento avança.

## Aviso legal

Este é um projeto **fã-feito (fan-made), não-oficial e não-comercial**, inspirado no universo de *Bleach*, criado por Tite Kubo. Não há qualquer afiliação com Kubo, Shueisha, Studio Pierrot ou detentores dos direitos da obra. Todo o conteúdo temático (nomes, conceitos, personagens) pertence aos seus respectivos criadores; apenas o código deste mod é de autoria da equipe listada abaixo.

Este projeto é um trabalho derivado do mod **Dragon Mine Z**, distribuído sob licença **GPL-3.0**. Em conformidade com os termos da licença, este projeto também é distribuído sob **GPL-3.0** — o código-fonte completo está disponível publicamente neste repositório.

---

## Sobre o projeto

Um mod de Minecraft que traz o universo de Bleach, com foco no sistema de progressão de poder (raça → estágio) e em missões data-driven. A arquitetura de quests e de evolução é reaproveitada do Dragon Mine Z; o conteúdo (Shinigami, Zanpakutō, Shikai/Bankai, reiatsu, arcos) é original deste projeto.

A documentação de engenharia reversa que permite recriar esses dois sistemas sem reabrir o original está em `Docs/00-overview.md` … `Docs/12-licenciamento-e-creditos.md`. Trechos de lógica não trivial do original estão em `/reference-code`.

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

- [ ] **Raça: Shinigami**
  - [ ] Sistema de Zanpakutō (arma inicial genérica)
  - [ ] Evolução para **Shikai** (requisito: [definir — ex: quest específica + energia acumulada])
  - [ ] Evolução para **Bankai** (requisito: [definir])
- [ ] **Sistema de energia (Reiatsu)**
  - [ ] Barra de energia com regeneração
  - [ ] Custo de energia ao usar habilidades
- [ ] **Sistema de Quests (base)**
  - [ ] Registro de quests via JSON no mundo (schema documentado em `03-sistema-quests.md`; defaults gerados em Java se a pasta não existir)
  - [ ] Ao menos 2 tipos de objetivo (ex: derrotar mob específico, coletar item)
  - [ ] Tela de acompanhamento de progresso de quests
  - [ ] Recompensas: XP e/ou desbloqueio de habilidade
- [ ] **HUD básico**
  - [ ] Indicador de energia/reiatsu
  - [ ] Indicador de estágio de evolução atual
- [ ] **Persistência**
  - [ ] Progresso de quest salvo corretamente entre sessões
  - [ ] Estágio de evolução salvo corretamente entre sessões

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
# Clonar o repositório
git clone [url-do-repositorio]

# Rodar ambiente de desenvolvimento do Forge
./gradlew runClient
```

[Ajustar conforme configuração final do projeto]

## Estrutura do repositório

```
/Docs
  README.md                         → este arquivo (visão do projeto Bleach)
  plano-analise-dragon-mine-z.md    → especificação da análise
  00-overview.md … 12-*.md          → referência de arquitetura para recriar quests + evolução
/reference-code                     → fontes originais pontuais (GPL-3.0), ver README lá
/dragonminez                        → clone do upstream (não é dependência de build do fork)
/src                                → código-fonte do mod Bleach (ainda a criar)
```

## Roadmap

| Fase | Descrição | Status |
|---|---|---|
| Fase 0 | Análise do repositório base + documentação | 🔄 Em andamento |
| Fase 1 (MVP) | Raça Shinigami + Quests básicas | ⏳ Planejado |
| Fase 2 | Raça Hollow | ⏳ Planejado |
| Fase 3 | Raças Quincy e Fullbringer | ⏳ Planejado |
| Fase 4 | Conteúdo expandido e balanceamento | ⏳ Planejado |

## Contribuindo

[Preencher se o repositório for aberto a contribuições externas, ou remover esta seção se for fechado à equipe]

## Licença

Este projeto é distribuído sob a licença **GPL-3.0** — veja o arquivo [`LICENSE`](./LICENSE) para o texto completo.

Este projeto incorpora conceitos de arquitetura do **Dragon Mine Z**, também sob GPL-3.0. Algumas dependências de terceiros estão sob licença MIT — avisos de copyright preservados em [`THIRD_PARTY_LICENSES`](./THIRD_PARTY_LICENSES).

## Créditos

- **Dragon Mine Z** — projeto original que serviu de base para os sistemas de quest e evolução: https://github.com/DragonMineZ/dragonminez
- **Tite Kubo / Shueisha / Studio Pierrot** — criadores do universo de Bleach (sem afiliação com este projeto)
