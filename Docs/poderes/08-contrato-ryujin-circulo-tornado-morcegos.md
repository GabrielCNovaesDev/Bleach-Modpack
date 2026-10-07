> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

> **Estado vigente — ciclo 20, 06/10/2026:** Ryujin com onda mais densa e tornado três blocos à frente; Hyōrinmaru com quatro slots selados, armadura temporária, hipotermia, criatura de gelo, asas/cauda Bankai, tempestade com restauração e HUD de habilidades. [Lógica, arquivos, valores e homologação](20-refinamento-gelo-clima-hud-2026-10-06.md). A decisão deste ciclo substitui a antiga selada restrita a dois cortes físicos. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

> **Estado vigente — 03/10/2026:** feature Ryūjin Jakka concluída por confirmação do usuário após `5a02b61`, na branch `Feature-Poderes-bankais`. Última validação de código: build, 49 regressões e 14 GameTests aprovados. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md) · [Contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md). Esta revisão modifica somente `Docs/poderes`; registros antigos abaixo descrevem suas respectivas versões.


## Rastreabilidade do contrato encerrado

1. `141205c`: roteamento B/C em `TechniqueService`, runtime em `RyujinTechniqueService`, recarga F3 e preservação de cooldown em `StatusData`. Corte: dano 48 e 1.200 ticks.
2. `57966f8`: Círculo substituído por Muralha; `wallBounds/intersectsWall` tratam o volume orientado. `tornadoAngle` define a fase horária; `tickSwarm` aplica movimento com colisão e alvo hostil.
3. `5a02b61`: flags centralizadas em `StatusData`/`ResourcesData`, comandos em `BleachCommands`, integração em forma/tick, geometrias de katana e 1.200 partículas por emissão da Muralha.
4. O usuário concluiu a feature. Tabelas abaixo são o estado vigente normal; overrides não reescrevem constantes. Roteiros ficam para regressões futuras.

---

# Contrato atual — Ryūjin Jakka

Atualizado em 03/10/2026. Branch de entrega: `Feature-Poderes-bankais`.
Minecraft 1.20.1, Forge 47.4.10, Java 17, protocolo 2.3, schema de jogador 3.

Este contrato substitui o plano anterior de Leque médio, Círculo Bankai e Muralha.
Essas propostas ficam como histórico, não como tarefas deste ciclo.
Revisão após teste do usuário: Círculo substituído por Muralha; Tornado ampliado; morcegos corrigidos. O usuário autorizou implementar o novo conjunto, manter o Corte destrutivo,
restaurar sua recarga para um minuto e aumentar seu dano base para 48.
Os números das três habilidades novas são os valores vigentes da feature concluída pelo usuário.

## Slots e requisitos

| Tecla / slot | Selada / Shikai | Bankai |
| --- | --- | --- |
| H / 1 | Ignição | Dash de chamas |
| N / 2 | Rajada curta | Leque de fogo |
| B / 3 | Muralha de Chamas | Tornado de Chamas |
| C / 4 | Invocação de Morcegos | Corte de Vapor Concentrado |

F1 e F2 mantêm variantes e valores; a revisão de 04/10 unifica os filtros de alvos. X continua sendo Flame Burst, fora dos slots.
O cliente envia apenas o slot pelo pacote já existente. O servidor resolve a variante.
As técnicas novas exigem personagem Shinigami criado, vivo, não espectador, grupo
`zanpakuto`, forma válida e Ryūjin Jakka na mão principal. Asauchi não autoriza o kit.

Cooldowns são compartilhados entre variantes do mesmo slot. Transformações e
normalização por compras/comandos não reiniciam cooldown. Morte, logout e troca de
dimensão mantêm a política transitória do MVP: limpam cooldowns. Não há persistência
de cooldown neste ciclo; um minuto significa 1.200 ticks de servidor durante a sessão.

## Muralha de Chamas — B base

Substitui o Círculo após teste do usuário. A forma base deixou de ser uma área circular.

| Parâmetro | Contrato / protótipo |
| --- | --- |
| Origem | 1 bloco à frente da posição corporal no cast |
| Direção | Mira horizontal (yaw); fixa, independentemente do pitch |
| Comprimento | 18 blocos, revisão de alcance autorizada em 04/10 |
| Largura / altura | 2 / 8 blocos |
| Duração | 100 ticks / 5 segundos |
| Custo | 40 reiatsu |
| Cooldown F3 | 400 ticks / 20 segundos |
| Dano base por contato | 18 por alvo, no máximo a cada 20 ticks |
| Knockback em mobs | Força 0,25 para o lado mais próximo da muralha, somente com dano aceito |
| Fogo | 3 segundos por aplicação |
| Visual | Paredão vertical temporário de FLAME, 144 amostras com 3 partículas cada a cada 4 ticks |

Dano, custo e recarga são valores iniciais para o teste da Muralha. O intervalo de 20 ticks
impede dano por amostra/partícula. O hitbox precisa intersectar o volume orientado de
18 × 2 × 8; não basta estar na caixa que envolve uma muralha diagonal.
A muralha fica fixa no ponto do cast por 5 segundos. Ela não bloqueia passagem como
bloco sólido: quem entra recebe dano, fogo e knockback leve. Não altera terreno.
Alvos que entrarem depois são avaliados durante a duração. O cast já verifica contato.

## Tornado de Chamas — B Bankai

| Parâmetro | Protótipo |
| --- | --- |
| Centro | Posição atual do jogador, recalculada a cada tick |
| Raio | 6 blocos |
| Altura | Da altura dos pés até 10 blocos acima |
| Duração | 200 ticks / 10 segundos |
| Custo | 40 reiatsu, uma vez |
| Cooldown F3 | 300 ticks / 15 segundos |
| Dano base | 2 por alvo a cada 10 ticks |
| Fogo | 2 segundos por aplicação |
| Visual | Somente FLAME, duas espirais com 48 amostras cada a cada 2 ticks, movimento horário visto de cima |

A caixa de consulta não é a área final de dano: os cantos fora do cilindro são excluídos.
Não existe entidade invisível para representar o tornado. O terreno não é alterado. As partículas dirigidas usam count=0, velocidade tangencial 0,14 e componente ascendente; a fase avança 0,3 rad/tick. Além das 96 partículas dirigidas, 32 amostras emitem três chamas densas. São 192 partículas a cada dois ticks, antes 64; o dano não aumenta com a densidade.

## Invocação de Morcegos — C base

| Parâmetro | Protótipo |
| --- | --- |
| Quantidade | Exatamente 5 entidades vanilla Bat |
| Vida máxima | 4 pontos cada |
| Duração máxima | 800 ticks / 40 segundos |
| Custo | 25 reiatsu, uma vez por enxame |
| Cooldown F4 | 200 ticks / 10 segundos |
| Busca de alvo | Mobs hostis (interface Enemy) na caixa de 8 blocos ao redor do proprietário |
| Ataque | Até 1,5 bloco de distância, com linha de visão |
| Dano base | 2 por alvo a cada 20 ticks, compartilhado pelo enxame |
| Fogo | 2 segundos por ataque |
| Visual | FLAME nos morcegos e no impacto |

O controlador servidor substitui a IA de voo/combate vanilla. Bat continua sendo uma entidade vanilla com NoAI; o serviço agora aplica move(MoverType.SELF, deslocamento) com colisão, em vez de depender apenas de setDeltaMovement. Os morcegos seguem o
proprietário quando não há alvo, com velocidade limitada e checagem de colisão.
Um morcego distante mais de 24 blocos pode voltar para um ponto livre próximo do dono.
Morcegos não atacam mobs passivos, jogadores, NPCs de quest, aliados nem invocações marcadas.
O dano é travado por alvo para impedir cinco impactos simultâneos sobre a mesma entidade.

Em modo normal, não é possível invocar novamente enquanto algum morcego registrado estiver vivo,
mesmo após o cooldown. Entidades mortas/removidas são retiradas da lista.
O cast falha sem cobrar recursos se não conseguir criar o enxame completo em espaço livre.
Os marcadores `bleachmod_flame_bat` e `bleachmod_flame_bat_owner` identificam entidades
temporárias; entidades recarregadas sem runtime válido são descartadas, evitando órfãos.

## Corte de Vapor Concentrado — C Bankai

| Parâmetro | Contrato autorizado |
| --- | --- |
| Dano base | 48, antes da mitigação do alvo |
| Custo | 45 reiatsu |
| Cooldown F4 | 1.200 ticks / 60 segundos |
| Alcance | 100 blocos |
| Abertura horizontal | 25° totais, meia-abertura 12,5° |
| Altura | 15 abaixo e 20 acima da altura corporal |
| Direção | Vetor exato da visão; execução instantânea |
| Entidades | Cada alvo recebe no máximo um impacto por cast |
| Terreno | Destrói blocos comuns sem drops e atravessa paredes |
| Proteções existentes | Ar, bedrock, command blocks e dureza negativa |

A geometria, seleção de alvos, partículas, sons e destruição permanecem no executor
anterior. Não foram adicionadas proteções de região ao Corte. Na revisão de 04/10, os filtros de aliados, NPCs, invocações e PvP foram unificados em TechniqueTargets, inclusive para o Corte.
O damage source permanece `indirectMagic(player, player)`: ignora armadura vanilla.
O filtro atual de `CombatEvents` também aplica Resistência Bleach porque as entidades
direta e atacante são o mesmo jogador; essa regra precisa de homologação em combate real.
Os valores anteriores (16 de dano, 0/300 ticks, 30/45°) são histórico substituído.

## Dano, aliados e limpeza

As três técnicas novas usam `playerAttack`: os bônus existentes de Zanjutsu/forma e
Ignição podem alterar o dano efetivo, e armadura/Resistência podem mitigá-lo.
Os números das tabelas são dano base, não dano final garantido.
A Muralha e o Tornado excluem o lançador, NPCs de quest, invocações marcadas, espectadores
e aliados reconhecidos pela equipe vanilla. Contra jogadores também respeitam PvP do
servidor e `canHarmPlayer`. Esses filtros são política inicial das técnicas novas;
a revisão de 04/10 aplica a mesma política de alvos também a F1, F2, Corte e Flame Burst.
O dano das novas áreas exige linha de visão a partir do lançador; não atravessa paredes.

As áreas e os enxames são memória do servidor, fora do NBT de progressão. Expiram por
tempo do mundo e são cancelados em morte, logout, troca de dimensão, mudança de forma,
arma inválida, personagem inválido, unload do mundo e parada do servidor.
A muralha não acompanha o jogador; o tornado acompanha. A troca de forma cancela ambos
sem reiniciar cooldown. O comando `/bleachdev cooldowns clear Player` cancela também
as novas áreas/enxames, além de limpar os cooldowns e o estado anterior de testes.

## Modelo da katana e comandos de teste

Revisão final: [relatório consolidado](11-fechamento-ryujin-comandos-modelos-2026-10-03.md).
Desde 04/10, as três formas usam 22 elementos, fio único, dorso espesso, curva segmentada e UVs completos de superfície em Shikai/Bankai. A geometria conserva cabo com faixas, guarda e lâmina
alongada com ponta em segmentos. As rotações da antiga Ryujin estavam invertidas
para a orientação de seu cabo. O modelo compartilhado corrige as duas mãos, primeira
e terceira pessoa, e ajusta inventário/chão/moldura. Selada usa metal, Shikai borda
vermelha e Bankai lâmina escura com borda rubra; preserva as texturas e o predicate de forma.

Comandos de operador (permissão 2, personagem criado), por jogador:

| Comando | Resultado |
| --- | --- |
| `/bleachdev cooldowns clear Player` | Zera recargas atuais uma vez, cancela áreas/enxame/Dash; preserva o modo atual e a ignição |
| `/bleachdev cooldowns disable Player` | Zera recargas e efeitos anteriores; novos casts ficam sem cooldown |
| `/bleachdev cooldowns restore Player` | Restaura recargas normais, aplicadas no próximo uso conforme o executor |
| `/bleachdev reiatsu free Player` | Custo zero nas técnicas, ignição, ativação e manutenção das formas, mesmo com saldo zero |
| `/bleachdev reiatsu restore Player` | Restaura os custos e drenos normais, sem alterar o saldo |
| `/bleachdev inspect Player` | Mostra também os modos de cooldown e custos |

Não se alteram constantes de técnicas nem JSONs de formas. `restore` não inventa uma
recarga para uma técnica que ainda não foi usada; o próximo cast atribui seu tempo exato.
No modo sem cooldown, recastar área/enxame substitui a instância anterior, sem acumular
entidades ou multiplicar dano. O limite de pacotes e requisitos de arma, raça, skill,
desbloqueio, domínio e seleção de forma continuam valendo. Overrides não são salvos:
reconectar/reiniciar ou renascer restaura o modo normal; trocar de dimensão conserva
os modos de teste da sessão. Comandos de teste sincronizam sem normalizar progressão
nem desligar a ignição. Nada muda no protocolo ou schema.

## Arquitetura e terreno futuro

- `TechniqueService`: roteamento dos slots existentes e executor preservado do Corte.
- `RyujinTechniqueService`: casts, áreas, invocações, regras de alvo e limpeza servidor.
- `TechniqueGeometry`: interseção de hitboxes com cilindros finitos.
- `StatusData`: cooldown F3 e cancelamento de transformação preservando recargas.
- `FormModeHandler` / `ProgressionService`: preservação dos cooldowns.
- Não há pacote novo, mudança de protocolo, renderer customizado ou dependência nova.

`spirit_flame` continua pendente e não bloqueia essas versões visuais.
Sua entrega futura exige SavedData, proprietário/origem/expiração, regra de sobreposição,
remoção condicional, chunks descarregados, reinício e preservação de blocos posteriores.
Nunca substituir esse contrato por um `setBlock(Blocks.FIRE)` simples.

## Validação e aceite

O estado de compilação/testes é registrado no relatório deste ciclo. Implementação e
teste automatizado não equivalem a homologação visual ou multiplayer.

Roteiro de referência para regressões futuras da feature concluída:

1. H/N preservados em Selada, Shikai e Bankai; Asauchi recusa o kit.
2. B base forma muralha 18 × 2 × 8, permanece fixa por 100 ticks, causa dano de contato/knockback e exclui fora/aliado.
3. B Bankai acompanha corrida/voo, dura 200 ticks, usa raio 6/altura 10 e espiral horária com partículas em movimento.
4. C base cria 5 morcegos, segue/ataca, recusa duplicação e expira em 800 ticks.
5. Matar morcegos permite novo cast após cooldown; morte/logout/dimensão não deixam órfãos.
6. Perda de arma ou forma cancela efeitos; transformar/comprar não libera recarga.
7. C Bankai causa dano base 48 e só permite novo cast após 1.200 ticks.
8. Corte conserva destruição sem drops, alcance, ângulo, indestrutíveis e vapor.
9. Servidor dedicado e dois clientes veem efeitos públicos, sem duplicar custo/dano.
10. Medir partículas e tick com múltiplos jogadores antes de marcar homologado.

## Divergências maiores preservadas como planejamento

NPCs e `quest_giver` já estão implementados como protótipo, assim como Hollow e
Hollow Boss; isso não significa raça Hollow jogável nem mentores/facções completos.
O módulo 05 de game design prevê cinco atributos e Stamina, enquanto o MVP usa sete
categorias. O módulo 06 prevê vínculo de Zanpakutō, ainda não implementado. Esses
contratos futuros não foram convertidos em mudanças de save/economia neste ciclo.

## Extensão do contrato: fogo ambiental — 04/10/2026

Bankai equipada emite spirit_flame: raio 6, duração 100 ticks, oito tentativas na entrada/três a cada 40 ticks, limite 32 posições por proprietário/512 por dimensão. Somente ar sobre apoio sólido, sem propagação ou consumo de terreno; respeita doFireTick para colocação. Contato: dano base 4, intervalo 20 ticks, regras comuns de aliados/PvP. SavedData guarda proprietários/prazos; chunks descarregados não são forçados, limpeza posterior preserva blocos substituídos e sobreposição. [Detalhes e inicializador de testes](14-fogo-ambiental-inicializador-2026-10-04.md).
