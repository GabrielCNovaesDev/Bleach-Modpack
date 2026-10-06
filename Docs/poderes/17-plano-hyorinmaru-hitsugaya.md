# Planejamento do segundo poder principal — Hyōrinmaru / Hitsugaya

## 1. Estado e decisões

Data: **06/10/2026**. Branch: `Feature-Poderes-bankais`. Base: `fa57dfe`.
**Estado: IDEALIZADO / EM ESPECIFICAÇÃO**, sem implementação, assets novos ou valores finais.
O usuário escolheu **Hitsugaya — gelo e controle de movimento** como próximo conjunto.
Essa escolha é aprovada; as técnicas propostas neste documento ainda são revisáveis.
História e contratos mais detalhados serão desenvolvidos por outros membros da equipe.

Contrato sistêmico: [identidade, vínculo e maestria](16-contrato-identidade-vinculo-maestria-zanpakuto.md).
Contexto de escopo: [revisão documental](18-revisao-escopo-e-entrega-equipe-2026-10-06.md).

## 2. Objetivo

Criar um segundo poder que valide a arquitetura de identidades e ofereça um papel
distinto da Ryujin: preparar o espaço, controlar deslocamento e explorar oportunidades.
Não copiar o kit de fogo trocando a cor das partículas e acrescentando lentidão.

## 3. Fantasia do jogador

Conquistar Hyōrinmaru, desenvolver domínio do gelo e avançar até sua Bankai pelo próprio
vínculo. O jogador continua sendo um personagem original; não recebe automaticamente
a patente, aparência corporal ou história de Hitsugaya.

## 4. Referências e limites de cânone

O [perfil oficial de Tōshirō Hitsugaya no anime](https://bleach-anime.com/en/character/)
identifica sua Zanpakutō como Hyorinmaru. O nome Daiguren Hyorinmaru e seu contexto de
Bankai também aparecem no [material oficial licenciado da KLab](https://www.klab.com/en/press/release/2024/0229/bleach_brave_souls_85_million_downloads.html).
Consultados em 06/10/2026. Essas fontes confirmam identidade/nomenclatura, não os
números, hitboxes, progressão ou catálogo de ataques propostos abaixo.

Em conformidade com RAC-20, o conteúdo jogável será **Hyōrinmaru**, e não uma espada
procedural arbitrária “parecida com Hitsugaya”. Nomes técnicos abaixo descrevem funções
do protótipo; não são apresentados como nomes canônicos de golpes.
Antes de fechar cada técnica, a equipe deve registrar sua referência no manga/anime,
o que será representado e o que foi adaptado. Não usar outro jogo licenciado como
autoridade para balanceamento do mod nem copiar seus assets.

## 5. Adaptação para Minecraft

**Direção proposta:** gelo com antecipação visual, consequências de movimento claras
e limites contra controle contínuo. Dano não deve ser o único valor da escolha.
Ryujin mantém sua identidade de pressão por fogo; Hyōrinmaru precisa de preparação,
posicionamento e resposta possível do oponente. Gelo não anula automaticamente todo fogo.

O primeiro protótipo representa gelo por efeito e renderização, sem substituir terreno.
Barreiras físicas, gelo no solo/água, asas e fenômenos ambientais ficam em etapas próprias.
Desbloquear Bankai não depende de instalar Soul Society ou um mapa ainda não entregue.
Isso permite testar o sistema por ferramentas administrativas enquanto a história é escrita.

## 6. Escopo

### Fatia inicial proposta

- Um perfil de identidade, arma compatível e acesso isolado do progresso da Ryujin.
- Katana selada com silhueta legível e pegada correta; referências da guarda/ornamentos
  devem ser conferidas antes da modelagem.
- Shikai como primeiro estágio liberado de testes, concedida explicitamente no protótipo.
- Duas técnicas: ataque direcionado e controle de movimento, com parâmetros configuráveis.
- Política comum de alvos, efeitos com dono/prazo e feedback de bloqueio.
- Dois jogadores, duas identidades e retomada de save como validação obrigatória.

### Etapas posteriores propostas

- Bankai Daiguren Hyōrinmaru, variações do repertório e efeitos de liberação.
- Manifestação visual de gelo/dragão e equipamentos visuais coerentes com a referência.
- Barreira e zona de frio, se as duas técnicas iniciais sustentarem o papel do conjunto.
- Interações temporárias com mundo, após contrato compartilhado de registro/limpeza.
- Campanha própria, provas e curvas de maestria, quando aprovadas pela equipe.

### Fora desta primeira fatia

Forma madura/avançada, asas com voo, clima global, congelamento massivo do mapa,
roubo de Bankai, sistema Quincy de medalhões, nova dimensão e campanha final.
São possibilidades futuras, não tarefas implicitamente autorizadas por este plano.

## 7. Modelo conceitual

Perfil proposto: `hyorinmaru`. Formas: Selada, Shikai, Bankai. IDs são sugestões para
um catálogo futuro, não registries já existentes. Formas/técnicas precisam ser identificadas
também pelo perfil; `shikai` global não poderá abrir a Shikai de toda Zanpakutō.

| Etapa | Papel proposto | Estado de desenvolvimento |
| --- | --- | --- |
| Selada | Arma e fundamentos de espada; não emitir gelo automaticamente | Planejamento |
| Shikai | Manifestação direcionada e controle inicial | Primeira fatia proposta |
| Bankai | Controle de espaço mais amplo, repertório avançado e custo correspondente | Segunda fatia proposta |
| Domínio avançado | Variantes e eficiência específicas | Depende de M07 e balanceamento |

**Mudança importante:** o precedente de técnicas Ryujin disponíveis em Selada não
obriga Hyōrinmaru a oferecer técnicas elementais antes de Shikai. A equipe deve definir
quais manifestações pertencem a cada etapa, sem impor simetria numérica entre kits.

## 8. Repertório proposto e regras principais

Os números de slots são referências do protótipo atual de quatro slots, não fechamento
da futura Combat Hotbar de M06. Teclas continuam remapeáveis.

| Ação conceitual | Shikai proposta | Bankai proposta | Prioridade |
| --- | --- | --- | --- |
| 1 — Manifestação direcionada de gelo | Onda/dragão visual em direção ao alvo, dano e frio limitado | Trajetória/volume ampliados com aviso visual e custo maior | Protótipo inicial |
| 2 — Corrente ou contenção gélida | Controle temporário de um alvo com confirmação de acerto | Contenção mais forte, condicionada às regras de resistência | Protótipo inicial; imobilização não aprovada |
| 3 — Barreira de gelo | Defesa localizada com duração/capacidade limitadas | Variante de defesa/controle de passagem | Etapa posterior |
| 4 — Zona de frio concentrado | Área curta de pressão/controle | Ataque de área mais ameaçador e claramente anunciado | Etapa posterior |

Cada linha exige pesquisa canônica adicional antes de receber nome final. O X atual
é Flame Burst da Ryujin; não será concedido a Hyōrinmaru por acidente. Definir X e
ações de disciplinas comuns em contrato próprio, sem copiar obrigatoriamente um quinto ataque.

### Controle de movimento

- **PROPOSTA:** começar por lentidão temporária com teto, aviso e prazo claros.
- **PENDENTE:** imobilização completa, duração, redução contra bosses e PvP.
- **PROPOSTA:** impedir soma ilimitada de lentidão/renovação. Definir resistência ou
  janela de recuperação para que múltiplos jogadores não prendam o alvo indefinidamente.
- **PROPOSTA:** resistência não torna todo boss imune ao papel do gelo; alternativas
  incluem controle reduzido ou consequência apropriada sem anular sua IA para sempre.
- **PROPOSTA:** aplicar dano e controle segundo regras explícitas de aceitação;
  aliados, NPCs de quest, espectadores e PvP desabilitado permanecem protegidos.
- **PROPOSTA:** controle de outro sistema não deve ser apagado ao terminar o gelo.
  Efeitos precisam de origem/dono/prazo e política de sobreposição, não um comando geral
  que remove toda lentidão ou descongela indiscriminadamente qualquer efeito.
- **PROPOSTA:** recuperação, dodge, movimento e reação do alvo continuam relevantes;
  não criar uma execução garantida sem possibilidade de resposta normal.

### Parâmetros ainda não aprovados

| Parâmetro | Situação |
| --- | --- |
| Dano base e tipo/fórmula de dano | A definir com M05/M06; gelo não implica automaticamente usar Kidō |
| Alcance, largura, altura e número de alvos | A definir por técnica e benchmarks |
| Reiatsu, dreno, cooldown, tempo de preparação e recovery | A definir; não herdar valores Ryujin automaticamente |
| Intensidade/prazo do controle e resistência | A definir com PvE/PvP |
| Fontes e marcos de maestria | Contrato 16 + M07, sem limiar numérico fechado |
| Colisão/vida da barreira e dano a ela | Depende do protótipo e regra de terreno |
| Orçamento de partículas, updates e runtimes | A medir com múltiplos usuários |

## 9. Integrações e mundo temporário

Reutilizar validação comum de alvos e mecanismos de custo/cooldown, sem acoplar gelo
a métodos que presumem Ryujin equipada. Dispatcher deve escolher identidade autorizada,
não simplesmente qualquer espada presente no inventário.

Se uma etapa posterior criar blocos, definir antes: dono, expiração, estados anteriores,
chunk descarregado, reinício, sobreposição, interação fogo/gelo, pistões, água, block
entities e remoção condicional. Não reutilizar diretamente o bloco `spirit_flame` como gelo.
O serviço existente fornece uma referência de ciclo de vida, não um contrato completo
de restauração de terreno. Repor um bloco antigo sobre construção posterior é proibido.

Proposta conservadora para o primeiro ensaio de mundo: apenas efeitos temporários em
espaços permitidos, sem modificar inventários/block entities, sem drops farmáveis ou
carga forçada de chunks. Interação com fogo é regra separada; gelo não apaga focos de
outro dono ou água de construção sem política aprovada.

## 10. Conteúdo inicial e ordem de entrega

| Fase | Entrega concreta futura | Condição para avançar |
| --- | --- | --- |
| H0 | Contrato de dados/acesso e separação de duas identidades | Migração e regras aceitas pela equipe |
| H1 | Arma/perfil, Shikai de teste e uma técnica direcionada | Progresso não se mistura; custo/alvos funcionam |
| H2 | Segunda técnica e controle temporário | Sem controle infinito; lifecycle e multiplayer aprovados |
| H3 | Katana/efeitos refinados e Bankai com repertório gradual | Referências e parâmetros por técnica revisados |
| H4 | Barreira/zona e eventual interação com mundo | Desempenho e contrato de terreno definidos |
| H5 | Questline, vínculo, provas e ritmo de progressão | Conteúdo narrativo aprovado e serviços estáveis |

História pode ser escrita em paralelo desde H0. H5 é integração jogável de conteúdo,
não autorização para adiar toda análise narrativa até depois de programar os poderes.

## 11. Visão completa

Mais manifestações, formas avançadas e interação entre elementos podem ser estudadas
depois da fatia inicial. O objetivo é construir um repertório coerente e especializado,
sem prometer reproduzir todos os acontecimentos ou poderes do personagem de uma vez.

## 12. Entrega para história e decisões abertas

A equipe receberá a seguinte estrutura de resultados, com texto/quests ainda abertos:

| Resultado necessário | Equipe pode desenvolver | Não assumir automaticamente |
| --- | --- | --- |
| Poder conhecido | Evento, encontro, diálogo ou descoberta ligados a Hitsugaya | Mentor obrigatório ou acesso imediato à 10ª Divisão |
| Identidade conquistada | Questline e prova de aquisição | Patente de capitão ou biografia do personagem |
| Vínculo inicial | Contexto pessoal e primeiro marco | Shikai junto com o item |
| Shikai disponível/adquirida | Nome/compreensão/prova/requisitos | Maestria completa de todas as técnicas |
| Bankai disponível/adquirida | Prova própria e domínio necessário | Forma avançada e repertório inteiro no mesmo reward |
| Troca ou retorno | Processo especial com consequências informadas | Alternar pela hotbar |

História decide era, locais, interlocutores, diálogos, provas e ritmo. Hitsugaya não
está entre os oito NPCs atuais documentados; sua eventual entidade, serviços e skin
serão uma entrega futura, não dependência já satisfeita. A primeira validação técnica
pode usar ferramentas de desenvolvimento sem inventar uma campanha pronta.

## 13. Critérios de aceitação futuros

1. Ryujin homologada mantém comportamento e não executa técnicas de gelo.
2. Maestria/Bankai Ryujin não autoriza Hyōrinmaru, e vice-versa.
3. Selada não manifesta gelo antes do requisito de acesso definido.
4. Ataques direcionados respeitam geometria, obstrução e política comum de alvos.
5. Controle termina, respeita resistência e não deixa alvo permanentemente imóvel.
6. Bosses/PvP têm contra-jogo; dois lançadores não criam prisão indefinida.
7. Morte/logout/dimensão/troca/unload encerram runtime e preservam progressão pessoal.
8. Mudanças temporárias futuras no mundo não geram drops ou apagam blocos posteriores.
9. Visual comunica gelo e fases sem exceder orçamento de efeitos em multiplayer.
10. Quests concedem acesso específico uma vez; UI explica requisitos pendentes.

São propostas de teste, não resultados de implementação.

## 14. Impacto técnico e documentação

Pontos futuros de integração: `CharacterData`, `PlayerData`, `FormRegistry`,
`TransformationsHelper`, `TechniqueService`, `TechniqueTargets`, comandos de teste,
rewards de quest, sincronização e apresentação da arma. Classes/registries próprios
do gelo somente serão desenhados depois de aprovar a fatia e não constam hoje do código.

O reuso deve preservar os executores/limpeza da Ryujin e separar entidades/alvos, efeitos
aplicados e persistência individual. Não adicionar bibliotecas de animação/renderização
apenas por associação com outros mods; avaliar necessidade quando os assets forem definidos.

## 15. Próximos passos

Equipe refina referência de cada manifestação e fecha H0/controle/dados. Depois propõe
o contrato de H1 com números e migração revisáveis, mantendo quests finais em desenvolvimento
paralelo. **Nenhuma implementação de Hyōrinmaru foi iniciada neste ciclo.**
