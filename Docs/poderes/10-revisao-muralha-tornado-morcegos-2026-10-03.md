> **Estado vigente — ciclo 22, 07/10/2026:** restauração do clima agora sincroniza explicitamente os clientes e `/weather clear` corrige chuva visual presa; Ice Dragon Bankai com modelo maior e alcance 24. [Causa, lógica, arquivos, testes e roteiro final](22-clima-sincronizado-dragon-bankai-2026-10-07.md). Demais pontos homologados pelo usuário; encerramento definitivo aguarda este teste visual. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

> **Estado vigente — ciclo 20, 06/10/2026:** Ryujin com onda mais densa e tornado três blocos à frente; Hyōrinmaru com quatro slots selados, armadura temporária, hipotermia, criatura de gelo, asas/cauda Bankai, tempestade com restauração e HUD de habilidades. [Lógica, arquivos, valores e homologação](20-refinamento-gelo-clima-hud-2026-10-06.md). A decisão deste ciclo substitui a antiga selada restrita a dois cortes físicos. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

> **Estado vigente — 03/10/2026:** feature Ryūjin Jakka concluída por confirmação do usuário após `5a02b61`, na branch `Feature-Poderes-bankais`. Última validação de código: build, 49 regressões e 14 GameTests aprovados. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md) · [Contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md). Esta revisão modifica somente `Docs/poderes`; registros antigos abaixo descrevem suas respectivas versões.


## Evolução desta revisão até o aceite

1. Este relatório registra `57966f8`: Muralha orientada em `TechniqueGeometry`, área/tornado e movimento de morcegos em `RyujinTechniqueService`, verificados em integração/regressões.
2. O usuário confirmou essa entrega. `5a02b61` preservou combate e aumentou apenas a densidade da Muralha: 240 amostras × 5 chamas a cada dois ticks.
3. A etapa final acrescentou geometrias longas de Ryujin, flags em `StatusData`/`ResourcesData` e comandos em `BleachCommands`; detalhes no [relatório 11](11-fechamento-ryujin-comandos-modelos-2026-10-03.md).
4. O aceite final encerrou a feature. Os 45/11 testes abaixo permanecem resultados desta etapa, não da entrega final.

---

# Revisão após teste em jogo — 03/10/2026

Branch: `Feature-Poderes-bankais`. Base: commit `141205c`.

## Feedback recebido e alteração

O usuário testou a versão inicial: morcegos eram invocados com chamas, mas ficavam
parados e não atacavam; Círculo foi rejeitado por ser repetitivo; Tornado funcionava,
mas precisava de maior imponência. Esses relatos não homologam o conjunto inteiro.

- Círculo removido do slot B base, substituído por Muralha de Chamas.
- Muralha: 16 blocos de comprimento (confirmado), 2 de largura, 15 de altura,
  duração de 100 ticks, direção horizontal da mira e origem 1 bloco à frente.
- Dano base 18 por contato, no máximo a cada 20 ticks por alvo; knockback lateral
  leve 0,25 em mobs com dano aceito; fogo 3 segundos. Custo 40 e recarga 20 segundos.
  Esses valores de balanceamento são iniciais para teste.
- Tornado: raio ampliado de 5 para 6 e altura de 8 para 10, mantendo custo/dano/duração.
  Duas espirais com 48 amostras cada. Fase avança em sentido horário visto de cima.
  Partículas dirigidas FLAME têm velocidade tangencial e componente ascendente;
  192 partículas a cada dois ticks, em vez das 64 anteriores.
- Morcegos: deslocamento explícito `move(MoverType.SELF, ...)`, com colisão vanilla,
  substitui depender apenas de velocidade de um Bat com NoAI. Seguem o dono e
  selecionam apenas mobs hostis (`Enemy`), sem atacar animais/NPCs/jogadores/aliados.
- Corte continua com 48 de dano base e cooldown 60 segundos; seu executor é preservado.
- H/N, recargas compartilhadas por slot e limpeza dos runtimes permanecem.
- Nenhum bloco é colocado ou destruído pelas três técnicas alteradas.

## Verificação

```powershell
.\gradlew.bat --offline --no-daemon check build runGameTestServer
```

Windows / Java 17 / Forge 47.4.10. **Build aprovado; 45 regressões e 11 GameTests
obrigatórios aprovados.** JSONs de recursos e diff conferidos.

Os testes exercitam geometria orientada da Muralha, cantos da caixa envolvente,
contato, largura/altura/posição atrás, custo/recarga, expiração após 105 ticks,
movimento real dos morcegos por 40 ticks, dano em hostil e exclusão de animal passivo.
As posições dos morcegos são comparadas individualmente por UUID, não entre morcegos.
A regressão da fase verifica o sentido horário em coordenadas horizontais Minecraft.

JAR: `build/libs/bleachmod-0.2.0.jar`.
Log: `build/ryujin-revision-validation.log`.

## Novo teste manual

1. B Selada/Shikai: observar parede 16 × 2 × 15, fixa por 5 s; testar alvos dentro,
   fora, acima, atrás e em orientação diagonal; conferir knockback e recarga de 20 s.
2. B Bankai: observar altura 10/raio 6, densidade e fluxo horário da espiral;
   verificar movimento com o jogador e que o dano manteve seu intervalo.
3. C Selada/Shikai: verificar voo/seguimento e ataque a zumbi/Hollow hostil; animal,
   NPC e jogador não devem virar alvo. Repetir perto de obstáculos.
4. Confirmar limpeza em morte/logout/dimensão/perda de arma, custo único e enxame único.
5. Testar com dois clientes e medir impacto de partículas antes de homologar o novo visual.

Build e testes automáticos aprovados não significam homologação visual desta revisão.
O visual novo e seus valores de balanceamento aguardam retorno do usuário.

Contrato vigente: [Ryūjin Jakka](08-contrato-ryujin-circulo-tornado-morcegos.md).
Manual único: [jogador](../jogador/manual-do-jogador.md).
