> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

> **Estado vigente — ciclo 20, 06/10/2026:** Ryujin com onda mais densa e tornado três blocos à frente; Hyōrinmaru com quatro slots selados, armadura temporária, hipotermia, criatura de gelo, asas/cauda Bankai, tempestade com restauração e HUD de habilidades. [Lógica, arquivos, valores e homologação](20-refinamento-gelo-clima-hud-2026-10-06.md). A decisão deste ciclo substitui a antiga selada restrita a dois cortes físicos. Registros anteriores abaixo são históricos.

# Ciclo 19 — Hyōrinmaru, identidade e onda de fogo

Data: 06/10/2026. Branch exclusiva: `Feature-Poderes-bankais`.
Implementação autorizada pelo usuário após os contratos 16–18 e a confirmação de que a onda deve funcionar na selada, Shikai e Bankai. Este relatório substitui o estado de planejamento desses pontos; os relatos anteriores permanecem históricos. Homologação estética e balanceamento do novo kit dependem dos testes do usuário.

## 1. Pesquisa e referências visuais

A [página oficial de Rebirth of Souls / Bandai Namco](https://bleach-ros.bn-ent.net/character/?chara=toushiro-hitsugaya) descreve Hyōrinmaru como uma Zanpakutō de gelo/neve, ataques a distância, um dragão de gelo frontal, ataques de gelo ao redor do adversário e fortalecimento em Daiguren Hyōrinmaru. A [página oficial de Brave Souls / KLab](https://www.bleach-bravesouls.com/en/character/hitsugaya.html) apresenta Hitsugaya e sua evolução por treinamento.

Essas fontes fundamentam a identidade de gelo e o ataque frontal. Corrente, Barreira e Zona, nomes do protótipo, áreas, danos, lentidão, duração e custos abaixo são adaptações do projeto, não números ou reproduções exatas do anime. As quatro artes enviadas pelo usuário orientam cabo azul/claro, amarrações contrastantes, guarda de quatro pontas e katana longa. Asas, armadura completa, voo e forma adulta não entram nesta entrega.

## 2. Sequência da implementação e arquivos

1. `CharacterData.java`: acrescentado `zanpakutoIdentity`, com uma identidade ativa (`ryujin_jakka` ou `hyorinmaru`). Formas descobertas e maestrias são consultadas no contexto dessa identidade. Ryujin mantém suas chaves antigas para evitar perda; Hyōrinmaru usa `hyorinmaru|shikai` e `hyorinmaru|zanpakuto:shikai`, por exemplo. Trocar guarda o progresso anterior e retorna à selada. Essa retenção é a política do protótipo; custos e consequências narrativas de troca permanecem abertos.
2. `PlayerData.java`: schema NBT 4. Saves antigos recebem identidade Ryujin e mantêm seus desbloqueios/maestria. A migração por níveis genéricos não concede formas à Hyōrinmaru. Identidade também entra em `saveAppearance`, para o modelo correto nos observadores.
3. `TransformationReward.java`: recompensas de transformação do roteiro existente continuam concedendo progresso à Ryujin, mesmo com gelo ativo. O progresso é arquivado na identidade correta; não desbloqueia gelo ao reivindicar uma missão antiga. A equipe deverá definir recompensas próprias de Hyōrinmaru em um ciclo futuro.
4. `BleachCommands.java`: `/bleachdev zanpakuto bind <jogador> <identidade>`, permissão de operador nível 2. Valida identidade/Shinigami, volta à selada, cancela efeitos, zera carga de transformação, mantém recargas e sincroniza aparência/dados. Não é o fluxo de aquisição do jogador.
5. `ModItems.java`, `ModCreativeTabs.java`, `BleachClient.java` e modelos `hyorinmaru*.json`: novo item; ambas as Zanpakutō no criativo; propriedade de forma exige identidade compatível. Modelos usam a empunhadura já corrigida e geometria de gume único, com novo cabo azul, cinco amarrações claras, pomo dourado e guarda em cruz de quatro pontas. Shikai acrescenta duas peças de gelo; Bankai acrescenta quatro. Materiais nativos de Minecraft mantêm tamanho pequeno e integração simples, sem gerar novos bitmaps.
6. `HyorinmaruTechniqueService.java`: servidor escolhe habilidade pela identidade, item, raça, grupo, forma desbloqueada, cooldown e custo. Dois cortes físicos na selada, quatro slots de gelo nas liberações. `CombatEvents.java` inclui a nova katana na aplicação de Zanjutsu e no indicador de dano; a tabela informa dano base, sujeito aos bônus de atributo/forma existentes. Filtros comuns de aliados/NPC/PvP e linha de visão. Só um campo temporário por proprietário; repetição em modo gratuito substitui o campo anterior. Efeitos não congelam blocos do mundo.
7. `TechniqueService.java`: encaminha slots de gelo ao novo serviço. A Ryujin exige identidade correspondente além do item. Removido executor do Flame Burst; removidas emissões instantâneas de toda a superfície do N, substituídas por `FlameWaveService`.
8. `FlameWaveService.java`: salva origem, direção horizontal, forma e dimensão da ativação. A frente avança um bloco por tick e alarga conforme o cone vigente. Altura visual de quatro blocos, amostras verticais de meio bloco e duas partículas por amostra. No alcance máximo, baixa durante seis ticks. Ao terminar inicia o residual: 60 ticks na selada/Shikai e 80 na Bankai. Procura chão em uma faixa local (+2 a −3), sem transportar o efeito ao teto do heightmap nem carregar chunks. Só superfícies apoiadas recebem residual. Cada proprietário possui no máximo uma onda.
9. `RyujinReleaseEffects.java`: aura contínua da lâmina Bankai passa a fumaça; Shikai continua com chamas. Pulso de liberação e focos ambientais temporários de calor da Bankai permanecem como homologados.
10. `ModKeybinds.java`, `BleachClient.java`, `ClientForgeEvents.java`, `StatusData.java`, `ReiatsuHud.java`, idiomas: retirado X do mapeamento, envio, mensagens e cooldowns. HUD agora possui quatro slots. `ExecuteTechniqueC2S` permanece registrado como pacote legado sem ação para não deslocar os discriminadores seguintes. `Reference.NETWORK_PROTOCOL` passa de 2.3 para 2.4: cliente e servidor precisam da mesma versão.
11. `TickHandler.java` e `RyujinTechniqueService.java`: ticks dos novos efeitos; cancelamento por morte, logout, dimensão, transformação, reset administrativo, desequipar, expiração, descarregamento de nível e parada do servidor. O ganho passivo de maestria fica suspenso enquanto recargas ou custos estão desativados. O treino passivo normal ainda existe; regras de vínculo narrativo/anti-AFK não foram substituídas por um sistema novo.
12. `MvpRegressionTest.java` e `HyorinmaruGameTests.java`: regressões de identidade, NBT antigo, recompensas, geometria e colapso; integração de autorização, custos, cooldowns, modo gratuito, barreira, aliados, expiração e onda nas três formas.

## 3. Onda N da Ryujin

| Forma | Alcance / meia abertura | Altura | Avanço + queda | Residual | Custo / recarga |
|---|---|---|---|---|---|
| Selada / Shikai | 8 blocos / 30° | 4 blocos | 8 + 6 ticks | 60 ticks | 15 / 80 ticks |
| Bankai | 14 blocos / 45° | 4 blocos | 14 + 6 ticks | 80 ticks | 25 / 80 ticks |

O dano de impacto continua sendo calculado no momento da ativação (4 na base e 8 na Bankai), com os filtros existentes. A frente é visual: não soma acertos nem cria uma hitbox vertical de quatro blocos. O dano residual de 1 e sua cadência permanecem; sua contagem começa **após** a queda, garantindo os tempos completos pedidos. Isso prolonga a duração total visual do ataque em 14/20 ticks. Chamas residuais não são blocos de fogo real. Muralha, Tornado, morcegos e Corte de Vapor não receberam rebalanceamento neste ciclo.

## 4. Hyōrinmaru — parâmetros provisórios

| Slot / tecla | Selada | Shikai | Bankai | Recarga |
|---|---|---|---|---|
| 1 / H | Corte frontal: alcance 3, dano 3, custo 5 | Dragão de Gelo: alcance 10, dano 8, custo 15; lentidão II por 60 ticks | Alcance 14, dano 12, custo 20; lentidão III por 60 ticks | 100 ticks |
| 2 / N | Corte amplo: alcance 3, dano 3, custo 5 | Corrente de Gelo: alcance 6, dano 4, custo 20; lentidão II por 80 ticks | Alcance 8, dano 6, custo 25; lentidão III por 80 ticks | 160 ticks |
| 3 / B | Indisponível | Barreira de Geada: 6 de largura, visual de 3 de altura e consulta vertical ±3; centro 3 à frente; 60 ticks; lentidão II por 30 ticks renováveis e empurrão leve; custo 25 | Mesma barreira, custo 30 | 240 ticks |
| 4 / C | Indisponível | Zona Glacial: raio 4, 80 ticks, dano 2 por pulso aceito; lentidão II por 30 ticks; custo 30 | Raio 6, dano 3, lentidão III; custo 35 | 300 ticks |

Pulsos de campos a cada 10 ticks, respeitando a invulnerabilidade normal de Minecraft. Barreira é controle de área: não cria parede sólida, não bloqueia projéteis e não oferece imunidade. Corrente é uma faixa/cone com lentidão, sem corrente física ou imobilização. Dragão usa partículas provisórias, sem modelo de entidade/dragão animado. O movimento não é bloqueado completamente. Lentidões já aplicadas expiram naturalmente, mesmo se o campo for cancelado. Equilíbrio, nomes e arte final ficam sujeitos à homologação.

## 5. Roteiro de homologação (operador)

Com personagem Shinigami criado:

```text
/bleachdev zanpakuto bind @s hyorinmaru
/give @s bleachmod:hyorinmaru
/bleachdev skill set @s zanpakuto 2
/bleachdev mastery set @s zanpakuto shikai 100
/bleachdev mastery set @s zanpakuto bankai 100
/bleachdev reiatsu free @s
/bleachdev cooldowns disable @s
```

Segurar a espada na mão principal. Testar H/N selada; selecionar/liberar Shikai e Bankai com G/R e testar H/N/B/C. Esses comandos habilitam o protótipo para teste; não representam progressão narrativa. Testar mobs, aliados, parede entre jogador e alvo, troca da espada, mudança de forma, morte e reconexão. Restaurar limites com `/bleachdev cooldowns restore @s` e `/bleachdev reiatsu restore @s`.

Para validar Ryujin, usar `/bleachdev zanpakuto bind @s ryujin_jakka` e segurar seu item. Testar N nas três formas olhando horizontalmente: frente crescente, altura quatro, queda e chão pelos tempos da tabela. A direção é fixada na ativação. Conferir aura da lâmina: chamas no Shikai, fumaça na Bankai. X não ativa nenhuma técnica e não deve aparecer no HUD ou nas configurações de teclas do mod.

## 6. Validação e limites da entrega

Validação final aprovada: `check build runGameTestServer --offline` — **55 regressões e 24 GameTests**, build gerada sem erros. Avisos de APIs/Gradle obsoletos já existentes não impediram a execução. Artefato: `build/libs/bleachmod-0.2.0.jar`. A saída local está em `build/powers-validation.log` (arquivo de build não versionado). Modelos são carregados pelo parser real de Minecraft nos testes. A aparência em primeira/terceira pessoa, densidade visual e FPS ainda requerem homologação no cliente pelo usuário.

História, aquisição legítima, marcos do vínculo, custo/consequências de troca, identidade única de cada item físico, defesa contra duplicação do item e escalonamento final de dano por maestria continuam pendentes de desenho da equipe. A maestria por identidade e os requisitos existentes de liberação são a fundação implementada; não há um contrato narrativo completo nem campanha de Hitsugaya pronta.

Documentos de outras pastas e `Iniciar-Teste` não foram alterados. Nenhum commit na `main`.
