> **Revisão vigente — 04/10/2026:** ciclo de acabamento autorizado após o kit anterior: katana de um gume/texturas completas, liberação/aura, Muralha 18 × 2 × 8, alvos unificados e HUD de recargas. Build, 51 regressões e 16 GameTests aprovados; visual desta revisão para homologação no cliente. [Implementação passo a passo e arquivos](13-polimento-katana-liberacao-muralha-2026-10-04.md). Fogo ambiental é etapa seguinte, ainda não implementada. O histórico abaixo preserva as entregas anteriores.

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
