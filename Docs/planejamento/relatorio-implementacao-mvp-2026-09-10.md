# Relatório de correções do MVP — 10/09/2026

## Estado da entrega

As correções de lógica, rede, modelos de item e interface descritas abaixo estão implementadas. A transparência dos PNGs e a homologação visual/multiplayer continuam pendentes. Não considerar esta entrega integralmente homologada.

O código atual conserva o rework de sete atributos e Battle Power, schema NBT 3 e balanceamento recente. Não houve retorno ao sistema antigo de três categorias com cinco níveis.

## Correções aplicadas nesta revisão

| Sistema | Alteração |
|---|---|
| Resgates | O diário solicita todas as recompensas em um único pacote. O servidor mantém as flags individuais, respeita cancelamentos e sincroniza ao terminar. Repetir o pedido não entrega novamente prêmios já recebidos. |
| Rede | Protocolo 2.1; pedido de resgate limitado a IDs de 256 caracteres, jogador vivo/personagem criado e intervalo mínimo de quatro ticks. IDs de seleção de forma limitados a 32 caracteres. |
| Missões | Atualização e conclusão de objetivos conferem a assinatura da definição, inclusive objetivos de inventário após reload. |
| Formas | Pré-requisitos precisam apontar para estágios anteriores e ter mastery alcançável. Rejeita dependência de si mesmo ou de estágio posterior. |
| Tick | O ganho periódico de mastery consulta a forma ativa após as transições do tick. |
| Status | Layout de 304 × 228 para a área mínima usual de GUI; preserva sete atributos e BP. Tooltips mostram benefício, saldo restante e insuficiência de pontos. |
| Radial | Formas momentaneamente não selecionáveis explicam o requisito de sequência/domínio. |
| Espadas | Modelo compartilhado `katana_handheld` compensa em 180° a orientação original das três texturas, nas duas mãos, primeira/terceira pessoa e inventário. Posição final ainda precisa de inspeção visual. |
| Toasts | Fundo e marcações desenhados em código, sem depender dos PNGs translúcidos. Texto limitado à largura, subtítulo em até duas linhas e fila preservada. |

## Evidências de validação

- Os quatro testes Forge executaram com sucesso em `run-gametest`, separado dos saves de jogo. O log registra **All 4 required tests passed**.
- Clonagem após invalidar a capability preserva pontos/mastery e remove forma/carga transitória.
- Pedidos repetidos de resgate entregam pontos, dois diamantes e descoberta de Shikai uma única vez; o treino pode reiniciar após receber tudo.
- As raízes `/bleachdev` e `/bleachreload` negam permissão 0 e aceitam permissão 2.
- Reload inválido preserva registry e missão ativa; reload válido posterior mantém compatibilidade do progresso.
- A suíte isolada inclui os cenários existentes dos sete atributos, migração, BP, economia, quests e registries, além de duas regressões de pré-requisitos de formas.
- A execução desses testes não equivale a dois clientes conectados com latência nem à inspeção de renderização.

## Assets: diagnóstico e limite

Contagem dos arquivos originais antes de qualquer alteração de imagem:

| Arquivo | Pixels parcialmente transparentes | Pixels opacos |
|---|---:|---:|
| asauchi.png | 486 | 47 |
| asauchi_shikai.png | 840 | 54 |
| asauchi_bankai.png | 1211 | 109 |
| race_shinigami.png | 317008 | 9 |

O fundo da seleção já é opaco. O ícone de reiatsu original não tem alfa parcial, mas o HUD atual não depende dele. Os PNGs antigos de toast também apresentam alfa parcial; deixaram de ser usados pelo renderizador de notificações.

As tentativas de geração de imagem não produziram alfa real. Os PNGs das espadas e do espadachim **não foram alterados**. Foi solicitada autorização explícita para corrigi-los por código, preservando a arte; ela permanece pendente. Se as texturas forem rotacionadas depois, remover a compensação do modelo para não aplicar duas rotações.

## Pendências de homologação

- Corrigir o alfa das espadas e do espadachim após definir o método autorizado.
- Inspecionar mão principal/secundária, primeira/terceira pessoa e inventário nas três formas.
- Executar cliente real em mundo novo e existente, incluindo progressão, treino repetível, receita, telas e carga.
- Executar dois clientes em dedicado: rastreamento, reconexão, morte/respawn, dimensão e latência.
- Conferir textos longos e mais de cinco missões em escalas GUI distintas.

A tentativa de controle visual falhou na inicialização do runtime de automação (`failed to write kernel assets`, caminho não encontrado). Não foram registrados testes visuais como concluídos. O bloqueio antigo do Gradle por limite de uso foi superado.

## Artefato e roteiro de teste

Artefato esperado da revisão: `build/libs/bleachmod-0.2.0.jar`. Cliente e servidor precisam do protocolo 2.1. Não misturar com builds anteriores que também se chamavam 0.2.0.

1. Fazer backup do mundo e instalar o mesmo JAR nos dois lados.
2. Em mundo novo, escolher Shinigami; abrir J, K e Z e conferir textos/controles.
3. Concluir e resgatar as missões; comprar Zanpakutō, ativar Shikai, treinar mastery e acessar Bankai.
4. Gastar pontos nas categorias e repetir o treino para comprovar recuperação da economia.
5. Conferir a receita da Asauchi e as três aparências da espada.
6. Morrer, reconectar e trocar de dimensão; conferir persistência e cancelamento da carga.
7. Com dois jogadores, conferir aparência remota, resgates repetidos e comandos como operador/jogador comum.
8. Repetir em cópia de save antigo e conferir migração do antigo Poder para Zanjutsu, sem perda de progressão.

Comandos de verificação: `gradlew.bat build --offline` e `gradlew.bat runGameTestServer --offline`, com Java 17 e dependências previamente disponíveis. GameTests criam fixtures no mundo de teste e restauram os arquivos de configuração modificados por seus cenários.

## Resultado final do build

`BUILD SUCCESSFUL in 21s`, com **32 cenários de regressão aprovados**. Os quatro GameTests haviam passado antes dos ajustes finais de apresentação/modelos e limite do pacote. Esses ajustes foram incluídos no build final.

JAR gerado: `build/libs/bleachmod-0.2.0.jar`, 7.079.499 bytes.

SHA-256: `26D51BF32120ED110DF59818D18D2BAC538DD586DA7188038E43EBD39CFC930E`.

Persistem avisos de depreciação de Gradle/API, sem falha de build. O artefato foi gerado; não foi distribuído nem instalado em um servidor externo.
