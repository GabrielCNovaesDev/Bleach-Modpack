# Auditoria do snapshot da branch de poderes — 14/09/2026

## Estado da análise

| Campo | Resultado |
| --- | --- |
| Fonte | `Bleach-Modpack-Feature-Bankais-Poderes.zip` |
| Tipo de fonte | Snapshot sem histórico Git verificável |
| Código Java | 86 arquivos na árvore analisada |
| Pacote de poderes dedicado | Não existe |
| Sistema de formas | Existe em `common/evolution` |
| Técnicas ativas | Não existem no código atual |
| Bankai | Existe como forma configurável, não como técnica executável |
| Documentação `Docs/poderes` | Não estava presente no ZIP recebido |
| Próxima etapa | Documentar contrato e escolher um slice mínimo |

## Conclusão

O snapshot enviado ainda corresponde essencialmente ao MVP Shinigami estabilizado. A branch recebeu o nome de poderes, mas não contém uma implementação nova de Bankai, técnica ativa ou sistema de habilidades executáveis.

O projeto já possui três formas dentro do grupo `zanpakuto`: `sealed`, `shikai` e `bankai`. Essas formas são estados de manifestação com requisitos, mastery, custo de entrada e drain contínuo. Elas não são ainda um sistema geral de poderes.

A arquitetura atual não deve ser reescrita neste momento. O primeiro passo seguro é criar um contrato de técnica ativa que reutilize as validações existentes, sem confundir quatro conceitos diferentes:

| Conceito | Estado no snapshot | Responsabilidade recomendada |
| --- | --- | --- |
| Forma | Implementado | Estado ativo da Zanpakutō e progressão de Selada/Shikai/Bankai. |
| Skill | Implementado | Nível comprado ou concedido que libera formas. |
| Técnica ativa | Não implementado | Ação executável com custo, cooldown, alvo e efeito. |
| Efeito temporário | Parcial/conceitual | Alteração com duração, cancelamento e sincronização. |

## Fluxo existente de formas

```
JSON do mundo ou defaults
    → FormRegistry.prepare/parse
    → FormData + FormGroup
    → PlayerData/CharacterData guarda forma selecionada e ativa
    → FormWheelScreen ou tecla G seleciona uma forma
    → tecla R carrega transformação
    → FormModeHandler valida e transforma no servidor
    → SyncHelper envia aparência, recursos e estado ao cliente
    → TickHandler aplica drain e reversão por reiatsu baixo
```

### Definição

`FormData` contém atualmente:

- nome;

- nível de skill necessário;

- requisito de forma anterior;

- tipo de combinação de requisitos;

- mastery necessária para desbloqueio;

- mastery para transformação instantânea;

- mastery para transformação livre;

- drain de energia;

- máximo de mastery;

- ganho passivo de mastery.

Isso é suficiente para representar a cadeia atual de formas. Não é suficiente para representar uma técnica ativa, porque não há campos para cooldown, duração, alvo, alcance, dano, área, animação ou efeito.

### Registro

`FormRegistry` carrega arquivos JSON por mundo, restringe o MVP à raça `shinigami` e ao grupo `zanpakuto`, valida a ordem `sealed → shikai → bankai` e mantém estados separados para servidor e cliente.

Essa separação deve ser preservada. Um futuro registry de técnicas não deve permitir que o cliente publique definições no servidor.

### Seleção e ativação

`TransformationsHelper` distingue formas desbloqueadas de formas selecionáveis. A seleção não ativa a forma. `FormModeHandler` revalida a seleção no servidor, verifica cadeia, mastery, personagem criado, vida e reiatsu, cobra custo e gera o evento de mudança.

Esse padrão é reutilizável para técnicas:

```
cliente solicita uma intenção
    → servidor revalida disponibilidade
    → servidor confere custo/cooldown/alvo
    → servidor aplica o efeito
    → servidor sincroniza o resultado autorizado
```

## Achado importante: Skill não é Technique

A documentação de engenharia reversa já registra que o projeto original diferenciava `Skill` e `Technique`. O código enviado confirma apenas a primeira parte:

- `SkillsData` guarda níveis e compra por pontos;

- `FormData.unlockOnSkillLevel` usa skill como gate de forma;

- `ExecuteActionC2S` suporta somente `INSTANT_TRANSFORM` e `FORCE_DESCEND`;

- não há `TechniqueData`, `TechniqueRegistry`, slots de técnicas ou serviço de execução de técnica.

Portanto, adicionar Bankai como mais campos em `FormData` seria um erro arquitetural. A forma deve continuar descrevendo o estado de manifestação. A técnica deve ser um contrato separado, mesmo que sua primeira implementação reutilize `FormModeHandler`, `PlayerData` e `SyncHelper`.

## Recomendação de arquitetura

### Primeira etapa: não criar `common/power` completo

Não recomendo criar imediatamente oito classes genéricas para poderes. O projeto ainda não possui um caso real implementado que prove a necessidade dessa abstração.

O primeiro slice deve introduzir o mínimo necessário para uma técnica ativa simples:

```
common/technique/
├── TechniqueDefinition.java
├── TechniqueRegistry.java
└── TechniqueService.java
```

A estrutura é uma proposta, não uma decisão final. A implementação só deve ocorrer após escolher o primeiro caso de uso.

### Responsabilidades candidatas

| Classe | Responsabilidade |
| --- | --- |
| `TechniqueDefinition` | Dados declarativos: ID, skill/requisito, custo, cooldown, duração, alcance e efeito. |
| `TechniqueRegistry` | Carregamento e validação de definições no servidor; snapshot para cliente se a UI precisar. |
| `TechniqueService` | Validação autoritativa e execução; nunca confiar no custo ou dano enviados pelo cliente. |
| `TechniqueState` | Cooldowns e estados transitórios por jogador; só persistir se o design exigir. |
| `ExecuteActionC2S` | Transportar uma intenção de executar uma ação identificada. |
| `ActionFeedbackS2C` | Informar sucesso ou bloqueio ao dono. |
| Sync público | Enviar efeitos visuais/eventos aos observadores, sem dados privados de progressão. |

### Por que não colocar tudo em `FormData`

Misturar técnica e forma produziria problemas concretos:

1. cada Bankai passaria a conter campos de ataque e campos de transformação no mesmo objeto;

1. técnicas compartilhadas entre formas duplicariam dados;

1. cooldown seria confundido com drain contínuo;

1. o registry de formas ficaria responsável por ações de combate;

1. a UI radial não conseguiria distinguir seleção de forma e execução de técnica;

1. testes de transformação e testes de ataque ficariam acoplados;

1. futuras raças precisariam copiar campos irrelevantes.

## Primeiro slice recomendado

O primeiro slice não deve ser um Bankai completo com animação. Deve ser uma técnica mínima associada a uma forma já existente.

### Critérios do slice

- usar o Bankai atual como requisito de forma ativa;

- receber uma intenção do cliente;

- validar no servidor;

- cobrar reiatsu no servidor;

- recusar durante cooldown;

- aplicar um único efeito de combate mensurável;

- emitir partículas e som;

- enviar feedback ao jogador;

- comunicar o evento aos observadores;

- limpar o estado se o jogador morrer, desconectar ou trocar de dimensão;

- possuir teste automatizado da regra de custo/cooldown;

- possuir teste manual dentro do Minecraft.

O primeiro efeito pode ser simples, como um golpe em cone curto ou um impulso de dano em área pequena. O objetivo inicial é validar o contrato, não representar visualmente todo o Bankai canônico.

## O que ainda não deve ser feito

- não adicionar GeckoLib;

- não criar animação óssea;

- não criar vários Bankais;

- não adicionar novas raças;

- não alterar o fluxo de criação de personagem;

- não alterar a economia de pontos;

- não refatorar toda a capability;

- não persistir partículas, carga ou tecla pressionada;

- não aceitar dano, custo ou cooldown vindos do cliente;

- não declarar a feature pronta só porque o build compila.

## Riscos a acompanhar

| ID | Risco | Prioridade | Mitigação |
| --- | --- | --- | --- |
| PWR-001 | Confundir forma Bankai com técnica ativa. | Alta | Contrato separado e testes separados. |
| PWR-002 | Cliente solicitar custo/dano arbitrário. | Alta | `TechniqueService` calcula tudo no servidor. |
| PWR-003 | Evento visual não chegar aos observadores. | Alta | Separar sync privado do dono e evento público. |
| PWR-004 | Cooldown ficar salvo ou compartilhado indevidamente. | Alta | Estado por jogador e regra explícita de persistência. |
| PWR-005 | Partículas excessivas prejudicarem servidor. | Média | Limite de área, duração, frequência e quantidade. |
| PWR-006 | A primeira abstração criar mais complexidade que valor. | Média | Implementar um único caso antes de generalizar. |
| PWR-007 | Documentação indicar implementação inexistente. | Média | Marcar estados como proposta, implementado, testado e aceito. |

## Próxima tarefa segura

A próxima tarefa é fechar a especificação do contrato de `TechniqueDefinition` para uma única técnica piloto. Nenhum código Java precisa ser alterado antes dessa especificação.

A especificação deve decidir:

1. ID da técnica;

1. forma necessária;

1. skill ou mastery necessários;

1. custo de reiatsu;

1. cooldown;

1. duração, se houver;

1. alvo e alcance;

1. efeito de combate;

1. partículas e som;

1. mensagem de bloqueio;

1. dados privados e públicos;

1. cancelamentos;

1. GameTest possível;

1. critério de aceitação visual.