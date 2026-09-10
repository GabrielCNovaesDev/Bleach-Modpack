# Manual do jogador — Bleach Mod (MVP Shinigami)

> **Documento vivo.** Sempre que uma mecânica nova entrar no jogo (controles, quests, progressão, HUD, itens, raças), este arquivo deve ser atualizado no mesmo PR. Não deixe o código na frente do manual.

| Campo | Valor |
|---|---|
| Última atualização | 2026-09-09 |
| Minecraft / Forge | 1.20.1 / 47.4.10 |
| Raça jogável | Shinigami |
| Estágios | Selada → Shikai → Bankai |

Fan-made, não-oficial, não-comercial. Inspirado em *Bleach* (Tite Kubo). Sem afiliação com Kubo, Shueisha ou Studio Pierrot.

---

## 1. Primeiros passos

1. Entre em um mundo com o mod carregado.
2. Na primeira vez, abre a tela **Escolha seu caminho**. Confirme **Tornar-se Shinigami** (Esc não fecha essa tela).
3. Você recebe um **Asauchi** (espada de ferro do mod) e começa com **100 de reiatsu**.
4. Abra o diário com **J** e aceite a primeira missão do arco Soul Society.

Mundos antigos que ainda tiverem o item `bleachmod:asuchi` no inventário não migram sozinhos. O id correto é `bleachmod:asauchi`.

---

## 2. Controles

Remapeáveis em **Opções → Controles → Bleach**.

| Tecla | Ação | Como usar |
|---|---|---|
| **J** | Abrir o diário de missões | Lista quests, inicia, rastreia, reclama recompensa e gasta pontos na skill Zanpakutō |
| **G** | Ciclar o estágio *selecionado* | Escolhe para qual forma o carregamento vai (não muda a forma ativa na hora) |
| **R** (segurar) | Carregar transformação | Enche a barra de carga. Ao chegar em 100, tenta soltar a forma selecionada |
| **Shift+R** | Transformação instantânea | Só funciona se o *mastery* da forma selecionada for ≥ 40 |
| **V** | Descer um estágio | Bankai → Shikai → Selada. Zera a carga |

O Asauchi na mão muda de textura conforme a **forma ativa** (selada / shikai / bankai), não a selecionada.

---

## 3. Interface

### HUD (canto inferior esquerdo)

- Ícone + barra de **reiatsu** (`atual/máximo`)
- Ícone + nome do **estágio ativo**
- Barra de **carga** (só aparece enquanto você segura R ou ainda tem carga)
- **Pontos espirituais** (a “moeda” de progressão)

### Painel de missão rastreada (canto superior direito)

Aparece depois de **Rastrear** no diário, ou automaticamente ao aceitar uma quest. Mostra o título e até dois objetivos.

### Toasts (topo da tela)

Avisam início, objetivo concluído, missão completa, falha e recompensa reclamada.

### Diário (J)

- Esquerda: até 5 missões, com ícone de status (não iniciada / aceita / sucesso / falha)
- Direita: descrição, objetivos e recompensas
- Botões embaixo: **Iniciar**, **Reclamar**, **Rastrear**, **Evoluir**

---

## 4. Reiatsu

Recurso de pressão espiritual. Começa em **100 / 100**.

| Situação | O que acontece |
|---|---|
| Forma **selada** | Regenera **0,25 por tick** (~5 por segundo). Enche em ~20 s |
| **Shikai** ativo | Consome **0,4 por tick** (~8/s) |
| **Bankai** ativo | Consome **0,8 por tick** (~16/s) |
| Cai para **5% ou menos** do máximo | A Zanpakutō volta sozinha para **selada** |

Custo ao transformar (carregando R), com 100 de reiatsu máxima:

- Shikai: **4** reiatsu (`10% do máximo × drain 0,4`)
- Bankai: **8** reiatsu (`10% do máximo × drain 0,8`)

Custo instantâneo (Shift+R): `drain × 4` (Shikai ~1,6 / Bankai ~3,2). Se não tiver reiatsu suficiente, a transformação não sai.

---

## 5. Sistema de progressão

Não há “nível de personagem” único. O poder sobe em **três trilhas** que se cruzam.

```
Quests  →  pontos espirituais + skill + mastery de forma
Pontos  →  compra da skill Zanpakutō (se você não pegou o nível na quest)
Skill   →  destrava o estágio (Shikai no nível 1, Bankai no nível 2)
Mastery →  transforma mais rápido, permite instantâneo e pular na cadeia
```

### 5.1 Pontos espirituais (Spirit Points)

Moeda. Aparece no HUD. Vêm das **recompensas de quest** (depois de **Reclamar**).

Gasta no diário, botão **Evoluir**, para comprar o próximo nível da skill `zanpakuto`.

| Próximo nível da skill | Custo |
|---|---|
| 1 (desbloqueia Shikai) | 200 |
| 2 (desbloqueia Bankai) | 500 |
| Já no máximo (2) | Não compra |

O botão **não** substitui a quest: ele só sobe a skill. Sem a recompensa de transformação da missão, o mastery da forma nova continua baixo — você ainda precisa carregar com R e treinar.

### 5.2 Skill Zanpakutō

Gate da evolução. Nível máximo: **2**. Sobe de dois jeitos:

1. **Recompensa de quest** (caminho principal — ao reclamar, o nível é definido se for maior que o atual)
2. **Evoluir** no diário, gastando pontos

| Nível | O que destrava |
|---|---|
| 0 | Só a forma **selada** |
| 1 | **Shikai** (e a quest 2 também entrega mastery 100 de Shikai) |
| 2 | **Bankai** (a quest 3 também entrega mastery 100 de Bankai e 25 de Shikai) |

### 5.3 Mastery de forma

Número **0–100** por estágio (`zanpakuto.sealed`, `zanpakuto.shikai`, `zanpakuto.bankai`).

Como sobe:

- **Passivo:** enquanto Shikai ou Bankai estiver ativo, **+1 a cada 5 segundos**
- **Quest:** a recompensa `TRANSFORMATION` coloca o mastery no valor da recompensa (100 no MVP)

Para que serve:

| Limiar | Efeito |
|---|---|
| **40** | Shift+R (instantâneo) naquela forma |
| **50** | Pode selecionar a forma com G mesmo fora da cadeia imediata |
| Bankai precisa de **25** de mastery em Shikai | Gate extra além da skill 2 |

A velocidade da barra de R também escala com o mastery da forma *selecionada*: `10 + min(15, mastery × 0,2)` por tick. Com mastery 0, a barra enche em ~0,5 s.

### 5.4 Formas (estágios)

Há **forma selecionada** (alvo do R / G) e **forma ativa** (a que está valendo agora, HUD e textura da espada).

Cadeia: **selada → shikai → bankai**.

**G** só cicla formas que você já desbloqueou. Sem mastery 50, só dá para selecionar o próximo degrau depois da forma ativa.

**V** desce um degrau na cadeia (não pula para selada de uma vez, a menos que você já esteja em Shikai).

---

## 6. Como transformar na prática

1. Desbloqueie o estágio (skill + gates de mastery).
2. Aperte **G** até o HUD/alvo ser o estágio que você quer.
3. Segure **R** até a barra encher, ou **Shift+R** se o mastery for ≥ 40.
4. Para voltar, **V** — ou deixe a reiatsu acabar.

Se a forma estiver “selada” demais, o jogo avisa: estágio bloqueado, mastery insuficiente para pulo, ou reiatsu baixa.

---

## 7. Quests

As missões padrão são gravadas em `{mundo}/bleachmod/` na **primeira** carga do mundo. Editar esses JSON muda o conteúdo daquele save. Defaults de código só preenchem arquivos que ainda não existem.

### Como jogar uma missão

1. **J** → selecione a missão → **Iniciar** (ela vira a rastreada).
2. Cumpra os objetivos. Toasts avisam cada etapa.
3. Ao completar, volte no diário e **Reclamar** (as recompensas **não** caem sozinhas).
4. Missão com objetivo de **matar** falha se você **morrer**. Dá para iniciar de novo.

### Tipos de objetivo

| Tipo | O que conta | Observação |
|---|---|---|
| **KILL** | Matar a entidade pedida (ex.: zumbi, esqueleto) | No MVP o spawn é `NATURAL`: vale mob do mundo, não spawn especial da quest |
| **ITEM** | **Ter** o item no inventário | Não consome. Se largar os itens, o progresso pode cair |

Objetivos em sequência, salvo quando a quest marca `parallelObjectives` (Soul Society 2: carne podre e esqueletos ao mesmo tempo).

### Status

| Status | Significado |
|---|---|
| Não iniciada | Pode começar se os pré-requisitos fecharem |
| Aceita | Em andamento |
| Sucesso | Completa; ainda precisa **Reclamar** |
| Falha | Morreu com KILL ativo; **Iniciar** de novo |

Saga em ordem: a missão N só libera depois da N−1 estar em **sucesso**.

### Missões do MVP

#### Arco Soul Society (`soul_society`)

| # | Nome | Objetivos | Recompensas (após Reclamar) |
|---|---|---|---|
| 1 | Hollow Hunt | Matar **5 zumbis** | 200 pontos espirituais |
| 2 | Name Your Blade | Ter **8 carnes podres** e matar **3 esqueletos** (em paralelo). Requer a 1 | 400 pontos + skill Zanpakutō 1 + Shikai (mastery 100) |
| 3 | Bankai Threshold | Matar **8 zumbis**. Requer a 2 | 600 pontos + skill Zanpakutō 2 + Bankai (mastery 100) |

#### Sidequest de treino

| Id | Nome | Objetivos | Recompensas |
|---|---|---|---|
| `rukia_basic_training` | Basic Combat Drill | Matar **10 zumbis** | 150 pontos espirituais |

A sidequest não tem pré-requisito de saga. Pode ser feita a qualquer momento depois de criar o personagem.

### Rota sugerida

1. Aceitar **Hollow Hunt**, matar 5 zumbis, reclamar **200** pontos.
2. Opcional: **Evoluir** no diário (gasta os 200 e sobe a skill para 1) *ou* seguir a quest 2 e pegar a skill de graça no claim.
3. **Name Your Blade**: 8 carnes + 3 esqueletos → reclamar. Shikai fica disponível; selecione com **G** e solte com **R**.
4. **Bankai Threshold**: 8 zumbis → reclamar. Bankai destrava.
5. Sidequest de treino quando quiser pontos extras.

---

## 8. Comandos (operador)

Permissão 2 (cheat / op):

```
/bleachreload quests
```

Recarrega quests e forms do JSON do mundo e sincroniza o registry com os jogadores online. **Não** apaga o progresso salvo na capability.

---

## 9. O que o MVP ainda não tem

- Outras raças (Hollow, Quincy, Fullbringer)
- NPCs, diálogos, party
- Técnicas com nome próprio além da troca de estágio
- Dimensões (Soul Society, Hueco Mundo)
- Subir o máximo de reiatsu por treino (fica em 100)

---

## 10. Como atualizar este manual

Quando o PR mexer em gameplay, altere **este arquivo na mesma mudança**. Checklist:

- [ ] Controles novos ou tecla padrão diferente → seção 2
- [ ] HUD / diário / toast → seção 3
- [ ] Números de reiatsu, drain, regen → seção 4
- [ ] Custos de skill, mastery, formas, raças → seção 5
- [ ] Quests, objetivos, recompensas, falha → seção 7
- [ ] Comandos → seção 8
- [ ] Data no topo da página

Não copie texto de `00-overview.md` … `12-*.md` para cá: aqueles arquivos são de arquitetura. Este é o contrato com quem vai jogar.
