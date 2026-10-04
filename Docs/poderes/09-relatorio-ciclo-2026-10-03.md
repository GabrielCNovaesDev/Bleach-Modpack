> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

> **Estado vigente — 03/10/2026:** feature Ryūjin Jakka concluída por confirmação do usuário após `5a02b61`, na branch `Feature-Poderes-bankais`. Última validação de código: build, 49 regressões e 14 GameTests aprovados. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md) · [Contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md). Esta revisão modifica somente `Docs/poderes`; registros antigos abaixo descrevem suas respectivas versões.


## Desdobramentos da entrega inicial

1. Este relatório corresponde a `141205c`: novo runtime/áreas/enxame em `RyujinTechniqueService`, cilindro em `TechniqueGeometry`, testes em `RyujinGameTests`, recarga F3 em `StatusData` e preservação por forma/normalização.
2. `57966f8` substituiu Círculo, corrigiu NoAI por `move(MoverType.SELF, ...)` e ampliou Tornado; características iniciais abaixo não representam o visual final.
3. `5a02b61` acrescentou comandos contínuos, custos centralizados e modelos geométricos. Contagens de testes abaixo são históricas; a suíte final passou com 49 regressões/14 GameTests.
4. Feature encerrada pelo usuário após a entrega final; a sequência completa está no histórico técnico vinculado acima.

---

# Histórico da primeira entrega — substituída após teste do usuário

A revisão atual substitui Círculo por Muralha, amplia o Tornado e corrige o movimento dos morcegos. Consulte o [relatório da revisão](10-revisao-muralha-tornado-morcegos-2026-10-03.md). Os números e testes abaixo correspondem somente ao commit inicial.

# Entrega inicial — Círculo, Tornado e Morcegos

Data: 03/10/2026. Branch: `Feature-Poderes-bankais`, criada a partir de
`46ae41938b04ef4865c98708ad62c413d814f0e1` da branch `Feature-Bankais-Poderes`.
Não houve alteração ou commit na main.

## Resultado

- B executa Círculo em Selada/Shikai e Tornado em Bankai.
- C executa cinco morcegos em Selada/Shikai e preserva o Corte em Bankai.
- Corte: dano base 48 e recarga 60 segundos; geometria/destruição preservadas.
- Cooldown compartilhado por slot não é apagado ao transformar ou normalizar progressão.
- As novas técnicas possuem custo único, regras de alvo e runtime com limpeza explícita.
- Não há alteração de terreno pelas técnicas novas; spirit_flame continua futuro.
- Manual operacional único e [contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md).
- Os registros anteriores foram identificados como histórico, preservando os valores antigos.

## Validação executada

Ambiente: Windows, Eclipse Adoptium Java 17.0.20.1, Minecraft 1.20.1, Forge 47.4.10.

Comando final:

```powershell
.\gradlew.bat --offline --no-daemon check build runGameTestServer
```

Resultado: **BUILD SUCCESSFUL**, 43 cenários de regressão aprovados e todos os
9 GameTests obrigatórios aprovados (4 MVP + 5 deste ciclo). Os 22 JSONs em src
foram validados com ConvertFrom-Json. Revisão de diff e `git diff --check` sem erros.
JAR gerado: `build/libs/bleachmod-0.2.0.jar`.
Log local: `build/ryujin-validation.log`; logs de integração: `run-gametest/logs/latest.log`.

Os testes novos cobrem interseção do cilindro (contato/borda/cantos/altura), preservação
e decremento de recargas, não persistência, reset administrativo, cast/custo do Círculo,
falhas de arma/reiatsu, variante Bankai do F3, cooldown compartilhado, quantidade/vida
dos morcegos, bloqueio de enxame duplicado, remoção/reuso e bloqueio do Corte em recarga.

A primeira tentativa sandbox não acessou o cache/distribuição do Gradle. A execução
com acesso autorizado ao cache compilou. Foi corrigida uma assinatura de LazyOptional
no novo fixture. Uma regressão antiga esperava rejeição de spawn QUEST, apesar de o
código atual já suportar quests de boss. Ela passou a verificar o roundtrip desse modo
e a rejeição de modo desconhecido, sem mudar QuestParser ou QuestService.
O Gradle/Forge emitiram avisos de APIs/recursos/depreciações; não houve falha final.

## Limitações e homologação pendente

Implementado, compilado e testado automaticamente; **não homologado visualmente**.
Não foi iniciado runClient e não houve sessão real com dois jogadores.

Ainda precisam de teste manual: duração contínua real das áreas/enxame, movimento do
tornado, seguimento/combate dos morcegos, linha de visão/equipes/PvP, limpeza em
morte/logout/dimensão/reinício e carga de partículas com múltiplos jogadores.
Os GameTests de cast não substituem esses cenários de longa duração.
O dano base 48 do Corte foi autorizado; o impacto final contra armadura/Resistência,
destruição e performance do seu volume preservado precisam de teste em mundo separado.
Cooldowns continuam transitórios; morte/logout/dimensão limpam recargas conforme o MVP.
Os custos/danos/cooldowns das três técnicas novas são valores de protótipo documentados.

## Arquivos de implementação

- `common/technique/TechniqueService.java`: slot 3, variante base do 4 e valores do Corte.
- `common/technique/RyujinTechniqueService.java`: áreas/enxames e lifecycle servidor.
- `common/technique/TechniqueGeometry.java`: cilindro finito por hitbox.
- `common/data/StatusData.java`: cooldown 3 e transformação sem reset de recarga.
- `server/events/FormModeHandler.java`, `common/ProgressionService.java`: preservar cooldowns.
- `server/commands/BleachCommands.java`: limpeza das áreas/enxames no comando de teste.
- `gametest/RyujinGameTests.java`, `src/test/.../MvpRegressionTest.java`: validação.
- `assets/bleachmod/lang/pt_br.json`, `en_us.json`: feedback e nomes dos slots.

Paths Java acima são relativos a `src/main/java/com/bleachmod`.
README, manuais, planos e notas de arquitetura foram reconciliados com o contrato.
Os módulos de design de cinco atributos/Stamina e vínculo de Zanpakutō permanecem
planejamento; este ciclo não migra saves nem redefine a economia do MVP.
