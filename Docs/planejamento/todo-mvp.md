# Checklist atual do MVP

## Implementação e verificações concluídas

- [x] Resgate em lote e proteção individual contra duplicação.
- [x] Protocolo 2.3 e limites adicionais dos pedidos (inclui viagens).
- [x] Assinatura de quest nas rotas de atualização/conclusão.
- [x] Rejeição de pré-requisitos de forma circulares/posteriores e mastery inalcançável.
- [x] Modelo compartilhado para corrigir a orientação das três espadas.
- [x] Status compacto, tooltips e motivos de bloqueio no radial.
- [x] Toasts em código, sem os antigos assets translúcidos.
- [x] Quatro GameTests aprovados: clone, lote, permissões e reload.
- [x] Preservar sete atributos, BP e balanceamento; schema 4 acrescenta viagens sobre os dados do schema 3.
- [x] Atualizar relatório, regras e roteiro de homologação.

## Ainda pendente

- [ ] Corrigir alfa dos PNGs das espadas e do espadachim; autorização para edição por código solicitada.
- [ ] Homologar orientação das espadas visualmente nas duas mãos e três formas.
- [ ] Testar ciclo completo em mundo novo e existente, receita e recuperação de pontos pelo treino.
- [ ] Testar UI em diferentes escalas, textos longos, paginação, HUD e carga.
- [ ] Testar dois clientes reais: aparência, rastreamento, reconexão, morte, dimensão e latência.

Os testes visuais estão pendentes porque o runtime de automação não inicializou. Resultados e limites: [relatório](relatorio-implementacao-mvp-2026-09-10.md). Hollow próprio, novas raças e técnicas ativas continuam fora desta entrega.

## Entrega Soul Society — 11/09/2026

- [x] Código da dimensão, datagen, instalação automática e viagens server-side.
- [x] Tela H, retorno, cooldown, pacotes e migração schema 4/protocolo 2.3.
- [x] Guia de instalação e fixtures automatizadas.
- [ ] Receber mapa real e conferir spawn, dependências e limites.
- [ ] Homologação visual em singleplayer e dedicado com dois clientes reais.
