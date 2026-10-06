> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

## Planejamento que originou o ciclo 19 — 06/10/2026 (registro histórico)

A Ryujin permanece homologada e encerrada. Esta etapa somente documenta:

1. [Contrato de identidade, vínculo e maestria](16-contrato-identidade-vinculo-maestria-zanpakuto.md): um poder principal ativo, troca especial, progresso próprio e requisitos de liberações; decisões aprovadas distintas de propostas.
2. [Planejamento de Hyōrinmaru / Hitsugaya](17-plano-hyorinmaru-hitsugaya.md): escolha aprovada pelo usuário, gelo/controle de movimento, protótipo gradual e responsabilidades de história.
3. [Revisão de escopo e entrega à equipe](18-revisao-escopo-e-entrega-equipe-2026-10-06.md): dependências, diferenças entre protótipo e visão, e inventário das fontes.

Nenhum vínculo novo, técnica de gelo, migração ou campanha foi implementado por esses documentos. História, consequências da troca e números finais continuam abertos para a equipe.

# Poderes — referência operacional

Branch: `Feature-Poderes-bankais`. Atualização: 03/10/2026.

Estado: feature concluída por confirmação do usuário após `5a02b61`. Última suíte de código: 49 regressões/14 GameTests e build aprovados. Esta entrega atualiza exclusivamente documentos desta pasta.

## Entrega atual de acabamento (04/10)

Comece pelo [relatório 13](13-polimento-katana-liberacao-muralha-2026-10-04.md) para valores vigentes, geometria, superfícies geradas, efeitos, política de alvos, HUD e testes. O ciclo anterior abaixo continua encerrado; esta revisão é um novo acabamento autorizado.

## Ordem de leitura e histórico de implementação

1. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md): motivos, lógica, sequência e arquivos de cada feature. Distingue base H/N/X reaproveitada das entregas novas.
2. [Progresso](progresso.md): tabela de entregas `141205c` → `57966f8` → `5a02b61` e aceite final.
3. [Contrato](08-contrato-ryujin-circulo-tornado-morcegos.md): comportamento vigente, valores e ciclo de vida.
4. [Manual desta pasta](manual-do-jogador.md): modo de teste gratuito/sem recarga e restauração.
5. Relatórios 09/10/11: resultados de cada versão, sem substituir fatos antigos pelos números finais.

## Mapa dos demais documentos

| Documento | Atualização registrada |
| --- | --- |
| [05 — Flame Burst](05-tecnica-piloto-flame-burst.md) | Executor X preservado; integração aos overrides de custo/cooldown |
| [06 — Plano do kit](06-plano-kit-ryujin-jakka.md) | Propostas convertidas em entregas; substituição de Círculo; fechamento |
| [07 — Checklist H](07-checklist-f1-ignicao.md) | Ryujin como requisito; lógica de ignição/Dash e comandos de teste |
| [Migração](Migração-das-técnicas-para-Ryūjin-Jakka.md) | Autorização da arma, modelos próprios e distinção dos números antigos |
| [Auditoria](<Auditoria do snapshot da branch de poderes .md>) | Divergências resolvidas, commits verificáveis e escopo futuro preservado |

Cada documento contém atualização contextual com passos e arquivos; o texto original permanece identificado como histórico quando descreve uma versão anterior.

- [Consolidação da feature Ryūjin, comandos e modelos](11-fechamento-ryujin-comandos-modelos-2026-10-03.md): todas as entregas desta branch, instruções de teste, validação e pendências.
- [Contrato vigente do kit](08-contrato-ryujin-circulo-tornado-morcegos.md): teclas, formas, dimensões, custos, danos, recargas e ciclo de vida. O nome do arquivo conserva o histórico do Círculo, que foi substituído por Muralha.
- [Progresso atual](progresso.md): estado de homologação e registro histórico.
- [Manual do jogador](../jogador/manual-do-jogador.md): controles, progressão e comandos.
- [Entrega inicial](09-relatorio-ciclo-2026-10-03.md) e [revisão após teste](10-revisao-muralha-tornado-morcegos-2026-10-03.md): registros das etapas anteriores.

Planos antigos, auditorias e relatórios históricos não substituem o contrato vigente.
`main` não é branch de entrega desta feature.

## Entrega atual: fogo ambiental e inicializador

[Relatório 14](14-fogo-ambiental-inicializador-2026-10-04.md) documenta focos temporários de Bankai, persistência, limites e limpeza, e o inicializador Iniciar-Teste.bat. Build, 51 regressões e 18 GameTests aprovados; homologação visual e abertura interativa pendentes.

## Inicializador auditado — 04/10/2026

Feature Ryujin homologada e encerrada pelo usuário. Revisão de segurança limitada ao inicializador; correções de caminhos, links, preflight e sinalização de resíduos. [Auditoria e reprodução dos testes](15-auditoria-seguranca-inicializador-2026-10-04.md).

Resultado da auditoria: 18 cenários de segurança aprovados, zero falhas e fixtures temporárias removidas; gameplay permanece igual ao homologado.

## Entrega do ciclo 19 — 06/10/2026

- Ryujin: onda N de quatro blocos na selada/Shikai/Bankai, avanço 8/14 ticks, queda 6 ticks, residual completo 60/80 ticks; dano imediato e custos preservados. Aura contínua da lâmina Bankai com fumaça.
- X Flame Burst retirado; HUD e comandos de teste cobrem os quatro slots atuais. Protocolo 2.4 exige cliente/servidor atualizados juntos.
- Hyōrinmaru: novo item/katana de um gume, três modelos, quatro habilidades provisórias de gelo/controle, maestria e formas separadas. Selada com cortes físicos. Bind de operador mantém recargas, guarda progresso por identidade e retorna à selada. NBT schema 4 preserva saves antigos.
- Recompensas da campanha existente continuam vinculadas à Ryujin. História e desbloqueios definitivos do gelo permanecem com a equipe. Testes gratuitos suspendem ganho passivo de maestria.
- `check build runGameTestServer --offline`: **55 regressões e 24 GameTests aprovados**, JAR gerado. Geometria e integração automatizadas; aparência/FPS e balanceamento aguardam homologação do usuário.
- [Passo a passo, pesquisa, arquivos e comandos](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Outros documentos fora de `Docs/poderes` e inicializador preservados.