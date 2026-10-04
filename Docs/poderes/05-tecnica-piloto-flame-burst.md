> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

> **Estado vigente — 03/10/2026:** feature Ryūjin Jakka concluída por confirmação do usuário após `5a02b61`, na branch `Feature-Poderes-bankais`. Última validação de código: build, 49 regressões e 14 GameTests aprovados. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md) · [Contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md). Esta revisão modifica somente `Docs/poderes`; registros antigos abaixo descrevem suas respectivas versões.


## Evolução do Flame Burst

1. X foi preservado em `TechniqueService.executeFlameBurst`: servidor cobra 20, aplica dano base 6 em raio 3 e cooldown 300 ticks.
2. `141205c`: `StatusData.clearTransformationState` preserva a recarga ao transformar, sem alterar o executor de X.
3. `5a02b61`: `StatusData.setFlameBurstCooldownTicks` atribui zero no modo de teste; `ResourcesData.consumeReiatsu` dispensa débito no modo gratuito; `BleachCommands` restaura as regras por jogador.
4. A última suíte passou com 49 regressões e 14 GameTests. O estado «não compilado» abaixo é histórico; não foi adicionado GameTest exclusivo de combate para X nesta revisão.

---

# Estado operacional — 03/10/2026

Consulte o [contrato atual de Ryūjin Jakka](08-contrato-ryujin-circulo-tornado-morcegos.md). O kit atual usa B para Muralha/Tornado e C para Morcegos/Corte. Corte: dano base 48, custo 45, cooldown 60 segundos, 100 blocos, 25° e destruição sem drops. Branch: Feature-Poderes-bankais. Feature concluída pelo usuário após `5a02b61`; histórico técnico e arquivos descritos nesta atualização.

## Registro histórico abaixo — valores e próximas tarefas anteriores não são o contrato vigente

# Técnica piloto — Flame Burst

## Estado

`IMPLEMENTADO — NÃO COMPILADO NESTE AMBIENTE — NÃO HOMOLOGADO`

## Decisões desta versão

| Propriedade | Valor |
| --- | --- |
| Identificador | `flame_burst` |
| Nome provisório | Flame Burst / Explosão de chamas |
| Requisito de Bankai | Nenhum |
| Requisito de personagem | Personagem criado e jogador vivo |
| Custo | 20 de reiatsu |
| Cooldown | 15 segundos, equivalentes a 300 ticks |
| Área | Raio de 3 blocos ao redor do jogador |
| Dano base provisório | 6 pontos de dano |
| Feedback | Partículas FLAME e LAVA + som de disparo de Blaze |
| Animação óssea | Não utilizada |
| Tecla inicial | X, remapeável no menu de controles |
| Persistência do cooldown | Não persiste em NBT; é estado transitório |

O dano base de 6 pontos e o raio de 3 blocos são suposições provisórias, pois a solicitação definiu custo, cooldown e área curta, mas não definiu o valor numérico do dano.

## Comportamento

A tecla X envia somente o identificador `flame_burst` ao servidor. O servidor verifica personagem criado, vida, espectador, intervalo mínimo de ações, cooldown e reiatsu. O servidor calcula e consome o custo, inicia o cooldown, cria os efeitos e aplica dano às entidades vivas dentro do raio.

A técnica pode ser usada com a forma Selada, Shikai ou Bankai ativa. Ela não exige Bankai.

## Cancelamento e estado

O cooldown é armazenado em `StatusData` apenas durante a sessão. Ele é reduzido no tick do servidor. Morte, logout e inicialização do personagem limpam o estado transitório. O cooldown não é salvo em NBT para evitar que uma sessão interrompida preserve uma penalidade transitória.

## Limitações conhecidas

- O cooldown ainda não possui indicador visual dedicado no HUD.

- O efeito público para outros jogadores usa partículas e som gerados no servidor; não há pacote visual customizado separado.

- Não há animação de modelo ou animação óssea.

- A área pode atingir outras entidades vivas, inclusive jogadores, porque uma regra de PvP/friendly fire ainda não foi definida.

- A técnica ainda não possui GameTest dedicado.

- A implementação precisa ser compilada e testada no Minecraft antes de ser considerada aceita.

## Critérios de aceitação

1. X funciona sem Bankai ativo.

1. X falha com mensagem contextual quando há menos de 20 reiatsu.

1. Uma execução consome exatamente 20 reiatsu.

1. Uma segunda execução dentro de 15 segundos não aplica custo, dano ou efeitos.

1. Após 15 segundos, a técnica pode ser executada novamente.

1. O dano ocorre apenas dentro da área curta definida.

1. Partículas e som aparecem na execução.

1. Morte e reconexão não deixam cooldown preso no estado persistido.

1. O cliente não consegue escolher dano, custo ou cooldown.

1. O build e o teste manual confirmam que a tecla pode ser remapeada.