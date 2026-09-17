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

## Homologação pendente

Testar com `/give @s bleachmod:ryujin_jakka`, transformação, H/N, troca para Asauchi e mão vazia. O build deve ser executado com Java 17.