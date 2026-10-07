> **Estado vigente — ciclo 22, 07/10/2026:** restauração do clima agora sincroniza explicitamente os clientes e `/weather clear` corrige chuva visual presa; Ice Dragon Bankai com modelo maior e alcance 24. [Causa, lógica, arquivos, testes e roteiro final](22-clima-sincronizado-dragon-bankai-2026-10-07.md). Demais pontos homologados pelo usuário; encerramento definitivo aguarda este teste visual. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

> **Estado vigente — ciclo 20, 06/10/2026:** Ryujin com onda mais densa e tornado três blocos à frente; Hyōrinmaru com quatro slots selados, armadura temporária, hipotermia, criatura de gelo, asas/cauda Bankai, tempestade com restauração e HUD de habilidades. [Lógica, arquivos, valores e homologação](20-refinamento-gelo-clima-hud-2026-10-06.md). A decisão deste ciclo substitui a antiga selada restrita a dois cortes físicos. Registros anteriores abaixo são históricos.

> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

> **Estado vigente — 03/10/2026:** feature Ryūjin Jakka concluída por confirmação do usuário após `5a02b61`, na branch `Feature-Poderes-bankais`. Última validação de código: build, 49 regressões e 14 GameTests aprovados. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md) · [Contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md). Esta revisão modifica somente `Docs/poderes`; registros antigos abaixo descrevem suas respectivas versões.


## Evolução da migração até o fechamento

1. `TechniqueService.isRyujinJakkaEquipped` exige `ModItems.RYUJIN_JAKKA`; Asauchi mantém combate básico. Servidor escolhe a variante pela forma.
2. `141205c` integra B/C e preservação de recarga; `57966f8` consolida Muralha/Tornado/morcegos em `RyujinTechniqueService` e geometria em `TechniqueGeometry`.
3. `5a02b61` troca sprite curto por 14 elementos nos três modelos, com pai compartilhado. UVs usam PNGs de Ryujin existentes; a antiga observação de fallback Asauchi abaixo é histórica. Predicates preservados.
4. `StatusData`/`ResourcesData` integram testes; autorização de arma/forma continua. `restore` devolve as regras normais sem desfazer a migração.
5. Rajada vigente: alcance 8 e meia-abertura π/6 (60° totais) no código; os 80° do registro antigo não são o valor atual executado.
6. Usuário encerrou a feature após `5a02b61`; arquivos e testes completos no histórico técnico.

---

# Estado operacional — 03/10/2026

Consulte o [contrato atual de Ryūjin Jakka](08-contrato-ryujin-circulo-tornado-morcegos.md). O kit atual usa B para Muralha/Tornado e C para Morcegos/Corte. Corte: dano base 48, custo 45, cooldown 60 segundos, 100 blocos, 25° e destruição sem drops. Branch: Feature-Poderes-bankais. Feature concluída pelo usuário após `5a02b61`; histórico técnico e arquivos descritos nesta atualização.

## Registro histórico abaixo — valores e próximas tarefas anteriores não são o contrato vigente

# Entrega 2 — Migração das técnicas para Ryūjin Jakka

## Estado

Implementação preparada para build e homologação. A autorização server-side de Ignição, Dash e F2 agora exige `ModItems.RYUJIN_JAKKA` na mão principal. A Asauchi continua válida como arma de Zanjutsu básico, mas não recebe o bônus específico de Ignição nem executa o kit Ryūjin Jakka.

## Regras

| Cenário | Resultado |
| --- | --- |
| Ryūjin Jakka + Selada/Shikai + H | Ignição |
| Ryūjin Jakka + Bankai + H | Dash |
| Ryūjin Jakka + Selada/Shikai + N | F2 base |
| Asauchi padrão + qualquer slot Ryūjin | Rejeitado |

O cliente continua enviando somente o slot. O servidor valida item, forma, custo, cooldown e efeito.

## Correção visual incluída

O snapshot enviado tinha `ryujin_jakka.json` sem `overrides` e com a textura Bankai como textura raiz. Os três modelos foram corrigidos para usar a cadeia Selada/Shikai/Bankai. As texturas continuam sendo fallback da Asauchi até a entrega de arte própria.

## Ajuste da F2 base

A Rajada curta permanece exclusiva de Selada/Shikai e continua exigindo `ModItems.RYUJIN_JAKKA`. O alcance foi ampliado de 3 para 8 blocos, a abertura passou para 80 graus totais e o contato imediato foi incluído para evitar a zona morta à queima-roupa. O ataque agora mantém partículas densas e posições de superfície por 3 segundos, reaplicando partículas e dano periódico sem colocar, substituir ou alterar blocos. A Bankai não foi alterada nesta revisão.

## Homologação pendente

Testar com `/give @s bleachmod:ryujin_jakka`, transformação, H/N, troca para Asauchi e mão vazia. O build deve ser executado com Java 17.