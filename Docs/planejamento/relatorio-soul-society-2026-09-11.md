# Implementação Soul Society — 11/09/2026

## Entrega

Versão 0.3.0, Forge 1.20.1/47.4.10, Java 17. Código da dimensão `bleachmod:soul_society`, instalador de save, Senkaimon na tecla H, serviço de viagem com validação e retorno gratuito. [Guia operacional](../desenvolvimento/soul-society-instalacao.md).

O pedido de colocar um save numa pasta ampliou o plano inicial: agora há instalação automática antes de carregar os níveis. A origem é `<instância>/bleachmod-maps/soul_society`; a chegada é o spawn de `level.dat`, com override opcional no JSON do mundo. A instalação só aceita save Java 1.20.1 e importa o Overworld de origem como Soul Society.

## Decisões implementadas

- Gerador vanilla flat vazio, tipo próprio e biome plains, gerados por `TravelWorldGen`. O mapa fornece os chunks; não há NoiseSettings próprios/TerraBlender.
- Dois destinos fixos, entrada e retorno. `TravelConfig` substitui o registro genérico inicialmente sugerido. Nenhum teleporte para NPC/jogador foi adicionado.
- Configuração por mundo com reload validado; custo 20, cooldown 100 ticks, retorno gratuito. Sem skill nova ou mudança nas quests existentes.
- Carga por futuros de chunk e tickets limitados; conclusão na thread principal, timeout, cancelamento e revalidação. Até oito viagens globais pendentes.
- Débito reservado só após validar pouso, com devolução em caso de teleporte cancelado. Sync final inclui retorno/cooldown.
- Provider da capability corrigido: recria LazyOptional após reviveCaps, preservando a mesma instância de PlayerData. A referência antiga permanece invalidada; a entidade bloqueia acesso enquanto invalidada. Isso era necessário para retorno funcional depois da primeira troca de dimensão.
- Schema 4 e protocolo 2.3. Novos pacotes adicionados ao final do canal.
- Comandos `/bleachtravel inspect|reload|rescue`, nível 2. Não há comando que copie regiões com o mundo aberto.
- Não há instalação de mods/datapacks do mapa, conversor de formatos, proteção de território, animação elaborada ou portal físico.

## Threat Model Summary

Riscos principais: cliente adulterado escolhendo posição/custo, spam de preparação e cópia sobre um save existente. Controles estão no servidor e na publicação do diretório importado.

## Scope

Viagem, persistência e importação de arquivos locais escolhidos pelo operador. Código de outros mods e lógica de command blocks no mapa não são isolados pelo instalador.

## Assets

| Ativo | Sensibilidade | Motivo |
|---|---|---|
| Save Bleach e construções | Alta | Não sobrescrever dados em uso |
| Inventário, progressão e reiatsu | Alta | Preservar estado e impedir cobrança repetida |
| Thread do servidor/chunks | Média | Evitar preparação ilimitada |

## Actors

| Ator | Confiança | Capacidade |
|---|---|---|
| Cliente | Não confiável | Envia consulta/ID/revisão |
| Operador | Autoriza conteúdo local | Coloca save e configura chegada/custo |
| Outros mods | Compartilham processo | Podem cancelar eventos e alterar recursos |

## Trust Boundaries

Cliente → handler C2S → serviço/política → nível/chunks. Pasta de origem → staging → diretório da dimensão. NBT persistido → validação de retorno/cooldown.

## Threats and Mitigations

| STRIDE | Ameaça | Impacto | Probabilidade | Mitigação | Teste |
|---|---|---|---|---|---|
| Spoofing/Tampering | Cliente informa outro alvo/posição | Teleporte arbitrário | Média | IDs fixos, sender autoritativo, sem XYZ no pacote | Codec limitado e roundtrip |
| Denial of Service | Spam/pendências sem fim | Lag/tickets retidos | Média | Taxa por jogador, limite global, timeout e limpeza | Fluxo de viagem e inspeção do ciclo de vida |
| Tampering | Destino inseguro ou removido | Morte/prisão | Média | Colisão, perigos, borda, piso e fallback validado | GameTests de pouso e vazio |
| Elevation of Privilege | Resgate/reload por jogador comum | Bypass de regras | Média | Operador nível 2 | GameTest de comando |
| Tampering | Importação sobre dimensão ocupada | Perda de construções | Média | Recusa de merge, staging e marcador | Fixture de instalação/reinstalação |
| Repudiation | Cobrança sem viagem | Perda de recurso | Média | Reserva reembolsável e confirmação final | Viagem cancelada por evento |

## Security Requirements

Limitar IDs a 128 caracteres. Validar dimensão e números finitos no retorno. Não seguir links de filesystem. Não importar origem aberta. Não publicar destino incompleto. Não remover dados existentes para “consertar” uma instalação. Revalidar configuração ao terminar preparação.

## Abuse Cases

Repetir pedido durante recarga, trocar revisão/ID no pacote, selecionar destino sem mapa, abrir save incompatível, colocar pasta com links e tentar executar resgate sem permissão. Falhas de configuração mantêm snapshot anterior; falhas de instalação interrompem startup.

## Implementation Checklist

- [x] Sender, política e destinos resolvidos no servidor.
- [x] Migração NBT, cooldown e revive da capability.
- [x] Importação sem sobrescrita e com marcador/hash.
- [x] Datagen e recursos empacotados.
- [x] Fixtures de importação, pouso, ciclo de vida e viagem.
- [ ] Homologar mapa real e interface com dois clientes reais.

## Tests to Add / verificação contínua

Suite de regressões inclui migração, roundtrip, cooldown corrompido, config e codecs. GameTests incluem dimensão carregada, pouso, vazio, importação, comandos, revive e ida/volta com conexão Netty em memória. O teste com conexão em memória não substitui verificação visual ou handshake de dois clientes reais. Confirmar logs da execução final antes de publicar o artefato.

## Residual Risk

O mapa real não foi fornecido. O instalador valida o formato externo/versão, mas não certifica toda entidade, bloco customizado, datapack ou command block do conteúdo. Mods necessários devem estar instalados. Spawn sem piso seguro exige ajuste no save ou override. Fora dos chunks importados há vazio. Medição de desempenho com mapa grande e dois clientes reais permanece pendente.

## Revisão de código

### Critical

Nenhum achado crítico pendente na revisão final. Corrigidos durante validação: LazyOptional permanentemente invalidado na troca de dimensão, recusa indevida dos metadados criados antes do mapa e recusa de regiões vazias de entidades/POI que o Minecraft cria normalmente.

### Important

Homologação do mapa real e de dois clientes reais continua pendente. O instalador não converte formatos nem reproduz datapacks/mods ausentes. Limites de exploração/claims não foram implementados e o exterior dos chunks é vazio.

### Minor

A tela inicial usa componentes vanilla e feedback localizado; acabamento visual/animação pode evoluir sem mudar o serviço.

### Questions

Nenhuma decisão do usuário bloqueia o código entregue. Ao fornecer o mapa, confirmar spawn seguro e dependências compatíveis com Java 1.20.1.

## Validação final executada

- `runData`: dois JSONs da dimensão gerados e conferidos no JAR, junto das tags de hazards; cache excluído.
- `build`: aprovado em Java 17, incluindo 34 regressões existentes e sete novas (41 cenários).
- `runGameTestServer`: 11 testes obrigatórios aprovados, incluindo ida/volta, devolução de custo no cancelamento, revive, instalação e recuperação de publicação interrompida.
- Startup isolado com save de teste: importou oito arquivos / 6.393.856 bytes, leu spawn (2000.5, 80, 2000.5) e preservou metadados anteriores.
- Segunda abertura: instalação reutilizada apesar de origem de teste corrompida propositalmente após a primeira importação; arquivo alterado no destino manteve seu SHA-256.
- Artefato: `build/libs/bleachmod-0.3.0.jar`. Logs das verificações ficam em `build/travel-verification.log`, `build/travel-import-startup.log` e `build/travel-import-restart.log`.

O mapa real e a verificação visual em dois clientes não foram executados, pois o mapa não foi fornecido e os testes de rede aqui usam conexão em memória.
