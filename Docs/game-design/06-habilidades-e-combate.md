# Módulo 06 — Habilidades e Combate

## Estado atual do módulo

**Status geral:** EM ESPECIFICAÇÃO

Este documento registra as decisões conceituais tomadas até o fechamento do bloco **M06-06 — Defesa, Esquiva e Movimentação**.

O módulo ainda não está completo. Permanecem pendentes os blocos raciais específicos, começando por:

- M06-07 — Estrutura Shinigami
- M06-08 — Hollow / Arrancar
- M06-09 — Quincy
- M06-10 — Fullbringer
- M06-11 — Auditoria e Integração

As decisões registradas aqui constituem a base universal sobre a qual esses conteúdos serão construídos.

---

# 1. Filosofia geral do sistema de combate

O sistema de combate deve oferecer profundidade sem transformar o mod em um fighting game excessivamente técnico.

A base deve ser simples de compreender:

- atacar;
- bloquear;
- esquivar;
- movimentar-se;
- utilizar técnicas.

A profundidade surge progressivamente através do repertório aprendido pelo personagem.

O jogador não recebe dezenas de ações fundamentais diferentes. Em vez disso, treinamento e técnicas modificam, expandem ou aproveitam ações básicas que ele já conhece.

Exemplo:

```text
ação básica:
Dodge

com treinamento:
Dodge + ataque
→ contra-ataque contextual

com outro treinamento:
Dodge lateral + ataque
→ técnica diferente
```

Portanto:

> O sistema fornece uma linguagem básica de combate; o treinamento determina o quanto o personagem consegue fazer com essa linguagem.

---

# 2. Bleach como referência e adaptação para gameplay

Bleach é a principal referência de identidade, fantasia, funcionamento conceitual e diferenciação entre poderes e estilos.

Entretanto, o mod não tentará reproduzir literalmente todas as regras demonstradas na obra.

O processo de adaptação será:

```text
referência em Bleach
↓
identidade e fantasia da capacidade
↓
necessidades do gameplay
↓
sistemas do mod
↓
adaptação final
```

O objetivo é fazer com que uma técnica continue sendo reconhecível e coerente com Bleach, mas funcione bem dentro de Minecraft e da arquitetura do mod.

### M06-P01 — DECIDIDO — Cânone como fundamento, gameplay como adaptação

As habilidades deverão preservar a identidade, fantasia e características reconhecíveis de suas referências em Bleach, mas seu funcionamento será adaptado conscientemente aos sistemas e objetivos do mod.

Fidelidade não significa reprodução literal.

### M06-P02 — DECIDIDO — Mecânicas devem servir à experiência

Custos, Stamina, Energia Espiritual, cooldowns, preparação, recovery, Mastery, condições e demais propriedades serão escolhidos considerando conjuntamente:

- coerência com Bleach;
- identidade da técnica;
- legibilidade;
- diversão;
- balanceamento;
- integração com os demais sistemas.

Nenhuma solução presente na obra possui prioridade automática sobre uma adaptação de gameplay melhor.

### M06-P03 — DECIDIDO — Preservar a fantasia, adaptar a regra

Quando uma mecânica da obra não funcionar adequadamente em Minecraft ou na arquitetura do mod, ela poderá ser adaptada sem receio, desde que sua fantasia e identidade permaneçam reconhecíveis.

---

# M06-01 — Arquitetura Universal de Habilidades

## 3. Categorias sistêmicas

As capacidades do personagem serão diferenciadas por sua função dentro do sistema.

### M06-01A — DECIDIDO

As habilidades serão organizadas conceitualmente em:

1. **Ações fundamentais de combate**
   - ataque;
   - defesa;
   - esquiva;
   - outras ações básicas.

2. **Técnicas ativas**
   - utilizadas deliberadamente pelo jogador;
   - normalmente acessíveis pela Combat Hotbar.

3. **Técnicas contextuais**
   - executadas através de uma ação ou situação específica;
   - podem utilizar ataques, esquivas, movimento, bloqueio e outros contextos sem exigir um slot próprio.

4. **Capacidades passivas**
   - produzem efeitos automaticamente quando suas condições são satisfeitas.

5. **Técnicas do poder principal**
   - pertencentes à Zanpakutō, Resurrección, Schrift, Fullbring ou equivalente do caminho.

6. **Formas e estados superiores**
   - pertencem principalmente ao Módulo 07 e não serão tratados como técnicas comuns da Combat Hotbar.

---

## 4. Infraestrutura comum não significa comportamento igual

As técnicas compartilharão uma infraestrutura sistêmica comum sempre que possível.

Essa infraestrutura poderá tratar elementos como:

- aquisição;
- validação;
- execução;
- custos;
- cooldown;
- atributos;
- Mastery;
- requisitos;
- condições;
- efeitos.

Isso não significa que todas as técnicas funcionem da mesma maneira.

Shunpo, Cero, Kidō, técnicas de Zanpakutō, capacidades Quincy e habilidades Hollow podem compartilhar a mesma linguagem de sistema sem possuir o mesmo comportamento, aquisição ou identidade.

### M06-01A — complemento decidido

> Mesma infraestrutura sistêmica não significa mesmo conteúdo, mesma progressão ou mesma experiência racial.

O objetivo é evitar a criação desnecessária de motores completamente separados para cada categoria de habilidade.

---

# 5. Combat State e Combat Mode

Foi estabelecida uma distinção entre dois sistemas que não devem ser confundidos.

## Combat State

O **Combat State** já definido anteriormente representa o estado interno em que o personagem está envolvido em uma situação de combate.

Pode ser ativado por eventos como:

- causar dano;
- receber dano;
- entrar em uma confrontação relevante.

Ele influencia principalmente regras sistêmicas, como regeneração e prevenção de exploits.

É automático.

## Combat Mode

O **Combat Mode** representa a postura/interface de combate controlada pelo jogador.

### M06-01B — DECIDIDO

Combat State e Combat Mode são sistemas distintos.

É possível estar:

```text
Combat State ON
Combat Mode OFF
```

Exemplo:

O jogador está minerando e recebe um ataque inesperado.

O sistema reconhece que ele está em combate, mas sua interface não deve obrigatoriamente trocar naquele instante.

Também pode existir:

```text
Combat State OFF
Combat Mode ON
```

Exemplo:

O jogador ativa antecipadamente sua interface de combate antes de iniciar uma luta.

### M06-01C — DECIDIDO

O Combat Mode será manual por padrão.

O jogador utilizará uma tecla para entrar ou sair desse modo.

Posteriormente, no módulo de interface/configurações, poderá existir uma opção para entrada automática em Combat Mode ao causar ou receber dano.

Essa automação não será o comportamento padrão.

---

# 6. Combat Hotbar

### M06-01D — DECIDIDO

Ataque, defesa, esquiva e outras ações fundamentais possuirão controles próprios.

Essas ações não ocuparão slots de técnicas.

### M06-01E — DECIDIDO

Ao ativar Combat Mode, a hotbar vanilla será visual e funcionalmente substituída pela **Combat Hotbar**.

Essa alteração não moverá nem substituirá fisicamente os itens reais da hotbar vanilla.

O inventário do jogador permanece intacto.

### M06-01F — DECIDIDO

A Combat Hotbar possuirá inicialmente:

**9 slots de técnicas.**

Controles padrão:

```text
1 → Técnica 1
2 → Técnica 2
3 → Técnica 3
4 → Técnica 4
5 → Técnica 5
6 → Técnica 6
7 → Técnica 7
8 → Técnica 8
9 → Técnica 9
```

Cada controle poderá ser remapeado nas configurações do Minecraft.

O número 9 permanece como decisão atual e só deverá ser revisado caso prototipagem ou playtests revelem um problema real.

### M06-01G — DECIDIDO EM PRINCÍPIO

Capacidades importantes de mobilidade racial ou espiritual poderão utilizar um controle dedicado quando legitimamente disponíveis.

Exemplos futuros podem incluir:

- Shunpo;
- Sonído;
- Hirenkyaku.

Elas não precisam ocupar um dos nove slots comuns de técnicas.

### M06-01H — DECIDIDO

Formas e transformações importantes não ocuparão slots comuns da Combat Hotbar.

O Módulo 07 definirá sua forma de ativação.

---

# M06-02 — Combate Básico

# 7. Filosofia do combate fundamental

### M06-02A — DECIDIDO

O combate fundamental será deliberadamente simples.

O jogador não deverá precisar dominar uma quantidade excessiva de comandos universais para ser funcional em combate.

A profundidade marcial surgirá principalmente através das técnicas aprendidas.

---

# 8. Contextos reconhecidos pelo combate

### M06-02B — DECIDIDO

O sistema reconhecerá contextos básicos que poderão produzir ações comuns ou servir como gatilho para técnicas aprendidas.

Exemplos:

```text
sequência normal de ataques

correr + atacar

ataque no ar

ataque após esquiva

direção + ataque
```

Esses contextos podem inicialmente executar apenas ações básicas.

Quando o jogador aprende uma técnica compatível, ela poderá modificar, substituir ou expandir aquela resposta.

---

# 9. Formas de ativação de técnicas

### M06-02C — DECIDIDO

As técnicas poderão possuir três modelos principais de ativação:

### Ativa

Executada diretamente pelo jogador, normalmente através da Combat Hotbar.

### Contextual

Executada quando determinada combinação de situação e input é satisfeita.

Exemplo:

```text
Dodge + ataque
```

Não precisa obrigatoriamente ocupar um slot.

### Passiva

Ativada automaticamente quando suas condições são satisfeitas.

---

# 10. Sequência básica de ataques

### M06-02D — DECIDIDO EM PRINCÍPIO

O ataque básico poderá possuir uma sequência simples e natural de golpes, potencialmente terminando em um finalizador mais forte.

Não existe necessidade atual de criar um botão universal exclusivo para "Heavy Attack".

Exemplo conceitual:

```text
ataque
↓
ataque
↓
ataque
↓
finisher
```

Estilos e técnicas aprendidas poderão modificar ou expandir essas sequências.

---

# 11. Uso da Stamina no combate básico

### M06-02I — DECIDIDO

Stamina representa esforço físico e defensivo de curto prazo, mas não deverá funcionar como um imposto constante sobre qualquer ação.

Ataques leves e triviais podem possuir custo inexistente ou desprezível.

Stamina deverá ter peso principalmente em ações relevantes, como:

- esquiva;
- técnicas físicas exigentes;
- determinados finalizadores;
- movimentos especiais;
- ações defensivas específicas;
- outras ações que realmente representem esforço.

Objetivo:

> Stamina cria ritmo e limita abuso, não obriga o jogador a parar de lutar a cada poucos segundos.

Modelo conceitual:

```text
Stamina cheia
→ alta liberdade

Stamina intermediária
→ escolhas começam a importar

Stamina baixa
→ risco

Stamina esgotada
→ vulnerabilidade
```

Os valores exatos pertencem ao balanceamento futuro.

---

# 12. Impacto, Stagger e deslocamento

### M06-02N — DECIDIDO

Ataques relevantes que acertam devem produzir feedback proporcional de impacto mesmo quando não interrompem mecanicamente o alvo.

### M06-02O — DECIDIDO

Stagger real será seletivo.

Ataques básicos comuns não deverão interromper continuamente o adversário.

Stagger poderá surgir de:

- ataques realmente impactantes;
- finalizadores;
- técnicas específicas;
- Guard Break;
- outras condições apropriadas.

### M06-02P — DECIDIDO EM PRINCÍPIO

O sistema deverá possuir proteção contra stunlock.

A implementação poderá utilizar:

- resistência temporária;
- diminishing returns;
- outro método equivalente.

A solução definitiva será determinada por protótipo e balanceamento.

### M06-02Q — DECIDIDO

Knockback forte não será um efeito universal dos ataques básicos.

O combate corpo a corpo deverá preservar proximidade suficiente para permitir continuidade.

### M06-02R — DECIDIDO EM PRINCÍPIO

Técnicas poderão possuir propriedades de controle quando apropriado:

- knockback;
- launch;
- knockdown;
- outros deslocamentos.

Essas propriedades não constituem requisitos universais para combos básicos.

---

# 13. Dano, interrupção e Super Armor

Foi estabelecida uma distinção fundamental:

```text
receber dano
≠
sofrer stagger
≠
ser interrompido
```

### M06-02S — DECIDIDO — REVISADO

Receber dano sozinho não interrompe automaticamente:

- preparação;
- carregamento;
- execução.

O jogador continuará sofrendo dano normalmente.

Ele poderá decidir continuar executando a técnica mesmo sob ataque.

Cancelamentos voluntários serão permitidos quando a própria técnica autorizar.

Interrupções forçadas ocorrerão principalmente através de efeitos de controle apropriados, como:

- stagger;
- knockdown;
- launch;
- outros definidos pela capacidade.

### M06-02T — DECIDIDO

**Super Armor** representa resistência adicional a efeitos de controle durante fases determinadas de uma técnica.

Não concede:

- invulnerabilidade;
- imunidade a dano;
- preservação de Vida;
- preservação de recursos.

O personagem continua podendo morrer durante Super Armor.

Sua função é permitir que determinadas ações resistam melhor a interrupções.

### M06-02U — DECIDIDO EM PRINCÍPIO

Técnicas com preparação ou carregamento poderão declarar individualmente:

- possibilidade de cancelamento voluntário;
- movimento permitido durante execução;
- tempo máximo de carga;
- resistência a controle;
- outras propriedades relevantes.

---

# 14. Zanjutsu, Hakuda e fundamentos de combate

### M06-02V — DECIDIDO

Combate armado e desarmado utilizarão a mesma arquitetura universal de combate.

Diferenças surgirão através de:

- movesets;
- propriedades;
- técnicas aprendidas;
- equipamento;
- contexto.

### M06-02W — DECIDIDO

Utilizar uma espada não significa automaticamente utilizar uma técnica de Zanjutsu.

Da mesma forma, atacar desarmado não significa automaticamente executar Hakuda avançado.

Existem fundamentos básicos de combate disponíveis antes do treinamento sofisticado.

Zanjutsu e Hakuda representam expansão do repertório através de treinamento.

Exemplo:

```text
possuir espada
→ permite combate armado básico

aprender Zanjutsu
→ expande e aprofunda esse combate
```

```text
estar desarmado
→ permite combate corporal básico

aprender Hakuda
→ expande e aprofunda esse combate
```

### M06-02X — DECIDIDO

Técnicas poderão possuir requisitos físicos ou contextuais de execução.

Exemplos:

```text
requer espada

requer estar desarmado

requer arma espiritual

requer estar no ar

requer bloqueio

requer alvo

requer movimento
```

Conhecer ou equipar uma técnica não significa necessariamente que ela possa ser executada em qualquer situação.

### M06-02Y — DECIDIDO

Zanjutsu e Hakuda não possuirão um nível numérico genérico que substitua as técnicas.

Não haverá, por exemplo:

```text
Zanjutsu Level 57
Hakuda Level 34
```

O desenvolvimento ocorrerá principalmente através de:

- aquisição de técnicas;
- Mastery individual das técnicas;
- atributos fundamentais.

### M06-02Z — DECIDIDO EM PRINCÍPIO

Zanjutsu tende a aprofundar combate armado.

Hakuda tende a aprofundar combate corporal.

Essas tendências não serão tratadas como moldes absolutamente rígidos caso Bleach ou uma boa adaptação de gameplay justifiquem exceções.

---

# 15. Disciplinas gerais e estilos de personagens canônicos

Uma disciplina não representa um moveset idêntico entre todos os seus praticantes.

Personagens diferentes podem utilizar a mesma disciplina de maneiras muito distintas.

O jogador deve conseguir construir um personagem original que incorpore elementos de seus personagens favoritos sem precisar selecionar uma classe fechada baseada naquele personagem.

### M06-02AA — DECIDIDO — Disciplina e expressão individual

Uma disciplina representa um campo compartilhado de treinamento.

Praticantes diferentes podem desenvolver:

- repertórios diferentes;
- preferências;
- aplicações;
- combinações;
- estilos pessoais.

### M06-02AB — DECIDIDO — Técnicas gerais e associadas

O catálogo futuro distinguirá:

- técnicas gerais;
- técnicas institucionalmente ensináveis;
- técnicas avançadas;
- técnicas conhecidas principalmente através de determinados praticantes;
- técnicas pessoais ou não generalizáveis.

Uma habilidade não será considerada universal apenas porque pertence a uma disciplina geral.

### M06-02AC — DECIDIDO — Estilos sem classes fechadas

O jogador poderá incorporar aspectos do estilo de combate de personagens canônicos através das técnicas que aprende.

Não existirá obrigação de escolher:

```text
Estilo Yamamoto

Estilo Byakuya

Estilo Kenpachi
```

como classes permanentes.

O estilo final do personagem surgirá de seu repertório.

### M06-02AD — DECIDIDO — Poder exclusivo permanece separado

Técnicas que dependem diretamente de:

- Zanpakutō específica;
- Schrift;
- Resurrección;
- Fullbring;
- outro poder individual;

permanecem ligadas ao respectivo poder.

Elas não se tornam técnicas gerais apenas por envolverem espada, corpo, movimento ou energia.

### M06-02AE — DECIDIDO — Adaptação de estilos

Quando Bleach demonstrar um estilo característico sem formalizar técnicas nomeadas ou ensináveis, o mod poderá traduzir esse comportamento em gameplay.

Quando isso ocorrer, a implementação deverá ser tratada como adaptação do mod, e não apresentada como uma técnica canônica oficialmente nomeada.

---

# M06-03 — Aquisição e Aprendizado

# 16. Acesso, aprendizado e domínio

### M06-03A — DECIDIDO

A progressão de uma técnica será separada em três conceitos:

```text
ACESSO
↓
APRENDIZADO
↓
MASTERY
```

### Acesso

Determina se o personagem possui condições para tentar aprender a técnica.

Pode depender de:

- raça;
- estado;
- disciplina;
- storyline;
- mentor;
- local;
- técnica anterior;
- equipamento;
- conhecimento;
- Mastery;
- condições próprias da habilidade.

### Aprendizado

Momento em que a técnica efetivamente entra para o repertório.

### Mastery

Representa quanto o personagem domina aquela técnica após adquiri-la.

---

# 17. Múltiplos métodos de aquisição

### M06-03B — DECIDIDO

Não existirá uma única receita universal para adquirir todas as técnicas.

Uma técnica poderá ser:

- ensinada;
- descoberta;
- desenvolvida através de treinamento;
- derivada de fundamentos anteriores;
- liberada por acontecimentos narrativos;
- inerente a um poder;
- adquirida de outra maneira coerente com sua natureza.

---

# 18. Mentores não são lojas

### M06-03C — DECIDIDO

Mentores não funcionarão simplesmente como vendedores de habilidades.

Modelo desejado quando apropriado:

```text
mentor reconhece condições
↓
introduz conceito
↓
treinamento
↓
teste/aplicação
↓
técnica aprendida
```

Nem toda técnica precisará obrigatoriamente utilizar todas essas etapas.

Spiritual Points não funcionarão como moeda universal para comprar técnicas.

---

# 19. Repertórios diferentes entre mestres

### M06-03D — DECIDIDO

Instrutores diferentes poderão oferecer repertórios diferentes dentro da mesma disciplina.

Exemplo conceitual:

```text
Academia
→ fundamentos

instrutor experiente
→ técnicas intermediárias

especialista
→ repertório específico

mestre excepcional
→ técnicas avançadas
```

O objetivo é dar significado à exploração, aos personagens e às oportunidades de treinamento.

---

# 20. Pré-requisitos flexíveis

### M06-03E — DECIDIDO

Técnicas poderão exigir:

- fundamentos;
- outras técnicas;
- Mastery;
- narrativa;
- equipamento;
- estado;
- treinamento;
- outras condições pertinentes.

Entretanto, o sistema não presumirá árvores lineares rígidas para todo repertório.

Evitar:

```text
Skill A
↓
Skill B
↓
Skill C
↓
Skill D
```

apenas porque essa é a estrutura tradicional de skill tree.

Requisitos devem existir porque fazem sentido para aquela técnica.

---

# 21. Mastery como requisito, não moeda

### M06-03F — DECIDIDO

Mastery poderá servir como evidência de preparo para uma técnica avançada.

Ela não será consumida.

Exemplo:

```text
domínio do fundamento
+
mentor apropriado
+
treinamento
↓
nova técnica
```

---

# 22. Treinamento jogável

### M06-03G — DECIDIDO

Sempre que apropriado, o treinamento deverá ensinar simultaneamente:

- o personagem;
- o jogador.

Uma técnica contextual de contra-ataque, por exemplo, deveria preferencialmente exigir que o jogador pratique sua execução real em vez de apenas completar um objetivo genérico como:

```text
mate 10 inimigos
```

O treinamento pode envolver:

- timing;
- input;
- posicionamento;
- alvo;
- uso real da mecânica.

---

# 23. Técnicas do poder principal

### M06-03H — DECIDIDO

Técnicas vinculadas ao poder principal poderão possuir métodos próprios de aquisição.

Elas não precisam seguir o modelo institucional utilizado por disciplinas gerais.

Seu aprendizado poderá envolver:

- relação com o poder;
- descoberta;
- domínio;
- storyline;
- treino;
- outras condições próprias.

Formas e grandes despertares continuam pertencendo principalmente ao M07.

---

# 24. Construção livre de estilo

### M06-03I — DECIDIDO

Aprender uma técnica associada a determinado mestre ou personagem não vincula o jogador permanentemente ao estilo daquele personagem.

Dentro das compatibilidades legítimas do caminho, o jogador poderá combinar técnicas adquiridas de fontes diferentes.

Modelo desejado:

```text
aprendeu técnica A com um instrutor
+
técnica B com outro
+
técnica C através de treinamento
+
possui poder principal D
↓
estilo próprio
```

---

# M06-04 — Mastery das Técnicas

# 25. Função da Mastery

Mastery representa o domínio individual de uma técnica.

Ela responde:

> Quão bem meu personagem consegue executar esta técnica específica?

Não representa:

- atributo fundamental;
- nível de disciplina;
- poder bruto geral.

### M06-04A — DECIDIDO

Mastery poderá melhorar aspectos coerentes com a técnica, como:

- execução;
- precisão;
- eficiência;
- estabilidade;
- velocidade de preparação;
- recovery;
- controle;
- propriedades específicas.

Nem toda técnica precisa melhorar todos esses aspectos.

---

# 26. Mastery e dano

### M06-04B — DECIDIDO

Mastery influencia moderadamente o dano ou eficácia quando aplicável.

Entretanto:

> Potência, demais atributos relevantes e estados de poder continuam responsáveis pela maior parte da capacidade bruta.

Mastery melhora o quanto o personagem consegue extrair daquela técnica, mas não substitui sua base.

Modelo conceitual:

```text
atributos fundamentais
↓↓↓↓↓

estado / poder
↓↓↓

Mastery
↓
```

A influência exata será calibrada posteriormente.

---

# 27. Benefícios específicos por técnica

### M06-04C — DECIDIDO

Cada técnica poderá declarar quais propriedades sua Mastery melhora.

Exemplo conceitual:

```text
Cero:
- pequeno aumento de dano
- precisão
- eficiência
- carregamento
```

```text
técnica de Zanjutsu:
- pequeno aumento de dano
- timing
- recuperação
- consistência
```

```text
Shunpo:
- controle
- eficiência
- execução
```

Não existirá obrigação de aplicar o mesmo pacote de bônus a todas.

---

# 28. Limites da Mastery

### M06-04D — DECIDIDO

Mastery não deverá remover características fundamentais da técnica.

Exemplos:

```text
Mastery alta
≠
custo zero

Mastery alta
≠
toda técnica instantânea

Mastery alta
≠
eliminação total de risco
```

Ela representa refinamento.

Não representa remoção das regras da habilidade.

---

# 29. Escala universal

### M06-04E — DECIDIDO

Todas as técnicas que utilizarem Mastery compartilharão a mesma escala de progressão.

O valor máximo numérico ainda não foi decidido.

Exemplo meramente conceitual:

```text
Cero         X / MAX
Shunpo       X / MAX
Kidō         X / MAX
Zanjutsu X   X / MAX
```

A escala representa quanto aquela técnica foi dominada.

O mesmo percentual de Mastery não significa os mesmos bônus absolutos em técnicas diferentes.

---

# 30. Como Mastery cresce

### M06-04F — DECIDIDO

Mastery será desenvolvida principalmente por:

```text
uso válido
+
treinamento apropriado
+
desafios relevantes quando fizer sentido
```

Uso real da técnica deve constituir a fonte natural de progresso.

---

# 31. Anti-farm

### M06-04G — DECIDIDO

Spam vazio não deverá produzir progressão eficiente.

Exemplos de situações a evitar:

```text
disparar técnica centenas de vezes contra uma parede

atacar alvo irrelevante infinitamente

farm AFK

loops artificiais sem contexto
```

O sistema poderá considerar relevância e contexto internamente para reduzir ou anular formas triviais de exploração.

---

# 32. Retornos decrescentes de Mastery

### M06-04H — DECIDIDO EM PRINCÍPIO

Níveis elevados de Mastery poderão exigir proporcionalmente mais desenvolvimento.

A intenção é permitir:

```text
aprendizado inicial
→ relativamente perceptível

domínio intermediário
→ exige prática consistente

domínio extremo
→ conquista significativa
```

Isso não deverá se transformar em grind vazio baseado apenas em repetir a mesma ação milhares de vezes.

---

# 33. Simplicidade para o jogador

### M06-04I — DECIDIDO

O sistema interno de Mastery poderá considerar:

- contexto;
- relevância;
- retornos decrescentes;
- anti-exploit.

Entretanto, a lógica apresentada ao jogador deve permanecer simples:

> Praticar legitimamente uma técnica desenvolve seu domínio.

Não serão criadas várias moedas, barras ou subníveis paralelos para a mesma Mastery.

Profundidade interna não deve significar complexidade desnecessária para o jogador.

---

# M06-05 — Custos, Cooldowns e Execução

# 34. Recursos utilizados pelas técnicas

### M06-05G — DECIDIDO

Regra conceitual:

```text
STAMINA
→ esforço físico relevante

ENERGIA ESPIRITUAL
→ manifestação/utilização direta de poder espiritual

AMBOS
→ técnica cuja execução justifique claramente as duas exigências

NENHUM
→ quando outros mecanismos já limitarem adequadamente a ação
```

A classificação definitiva de cada técnica dependerá de sua identidade e de seu gameplay.

---

# 35. Duplo custo

### M06-05H — DECIDIDO

Consumir Stamina + Energia Espiritual simultaneamente não será o padrão para habilidades fortes.

Duplo custo deve existir porque a técnica realmente representa dupla exigência.

Não deverá ser utilizado apenas como punição genérica de balanceamento.

---

# 36. Técnicas sem custo direto

### M06-05I — DECIDIDO

Uma técnica poderá não possuir custo direto quando outros fatores já limitarem adequadamente seu uso.

Exemplos:

- condição de execução;
- recovery;
- timing;
- posição;
- cooldown;
- risco.

---

# 37. Evitar custo redundante

### M06-05J — DECIDIDO

Técnicas contextuais não cobrarão automaticamente novamente um recurso que já foi utilizado pela ação que as originou.

Exemplo:

```text
Dodge
→ Stamina

Dodge + contra-ataque
→ não significa automaticamente outro custo completo de Stamina
```

O custo deve representar o esforço real da sequência.

---

# 38. Balanceamento multidimensional

### M06-05K — DECIDIDO

Uma técnica não será balanceada apenas através de custo.

Ferramentas disponíveis incluem:

- recurso;
- preparação;
- execução;
- recovery;
- cooldown;
- alcance;
- precisão;
- condição;
- vulnerabilidade;
- risco;
- outras propriedades.

Isso permite que técnicas diferentes possuam perfis diferentes.

---

# 39. Modelos de consumo

### M06-05L — DECIDIDO

As técnicas poderão possuir:

### Custo instantâneo

Recurso consumido no momento apropriado da execução.

### Drenagem

Recurso consumido continuamente enquanto a capacidade permanece ativa.

### Híbrido

Custo de ativação + custo de manutenção.

---

# 40. Carregamento e pagamento

### M06-05M — DECIDIDO

Técnicas carregáveis poderão definir individualmente quando o recurso é consumido.

Possibilidades incluem:

- início;
- durante carregamento;
- execução;
- combinação dessas etapas.

Não existirá uma única regra obrigatória para todas.

---

# 41. Recovery

### M06-05R — DECIDIDO — REVISADO

Recovery representa o intervalo posterior à execução de uma técnica que regula a cadência entre skills.

Sua principal função é impedir encadeamento indiscriminado e spam excessivo de técnicas.

Fluxo:

```text
Técnica A
↓
execução
↓
Recovery
↓
outra técnica pode ser iniciada
```

A duração poderá variar conforme a técnica.

Recovery não significa obrigatoriamente que o personagem fica completamente imóvel ou incapaz de realizar qualquer ação.

Restrições adicionais dependerão da técnica.

---

# 42. Cooldown

### M06-05O — DECIDIDO — REFINADO

Ao ser utilizada, uma técnica entra em seu próprio cooldown.

Durante esse período:

```text
Técnica utilizada
→ indisponível

outras técnicas
→ continuam potencialmente disponíveis
```

Assim:

```text
Recovery
→ quando posso utilizar outra técnica

Cooldown
→ quando ESTA técnica pode ser reutilizada
```

Essa distinção é fundamental.

---

# 43. Sem Global Cooldown

### M06-05N — DECIDIDO

Não existirá um Global Cooldown universal que bloqueie todas as técnicas após qualquer uso.

O Recovery já regula a cadência geral entre habilidades.

---

# 44. Cooldown compartilhado

### M06-05P — DECIDIDO

Cooldowns poderão ser compartilhados quando duas capacidades forem variações ou manifestações da mesma habilidade e alterná-las permitiria ignorar artificialmente sua limitação.

Não haverá cooldown compartilhado simplesmente por pertencerem à mesma categoria.

Evitar:

```text
usou um Kidō
→ todos os Kidō bloqueados
```

ou:

```text
usou Zanjutsu
→ todo Zanjutsu bloqueado
```

sem uma justificativa específica.

---

# 45. Cargas

### M06-05Q — DECIDIDO EM PRINCÍPIO

O sistema poderá suportar técnicas com múltiplas cargas recuperáveis quando isso melhorar seu gameplay.

Cargas serão excepcionais.

Não constituem uma mecânica obrigatória para o repertório.

---

# 46. Mastery e cooldown

### M06-05S — DECIDIDO

Mastery não reduzirá universalmente cooldown.

Uma técnica específica poderá receber alguma melhoria limitada relacionada à disponibilidade se isso fizer sentido para sua identidade e balanceamento.

---

# 47. Atributos e cooldown

### M06-05T — DECIDIDO

Nenhum atributo universal reduzirá automaticamente todos os cooldowns.

Alterações de cooldown devem pertencer às regras específicas das capacidades quando necessário.

---

# M06-06 — Defesa, Esquiva e Movimentação

# 48. Defesa básica universal

### M06-06A — DECIDIDO

Todo personagem terá acesso a uma ação básica de bloqueio.

A eficiência da defesa poderá variar conforme:

- ataque recebido;
- meio utilizado para bloquear;
- equipamento;
- estado;
- técnica;
- treinamento;
- propriedades pertinentes.

Bloqueio universal não significa bloqueio igualmente eficiente em todas as situações.

---

# 49. Postura

A decisão anterior de utilizar Stamina diretamente para determinar Guard Break foi revisada.

Postura existirá como um medidor próprio.

Sua função será propositalmente extremamente limitada.

### M06-06B — DECIDIDO — REVISADO

O bloqueio utilizará um medidor de **Postura** separado da Stamina.

Sua função é representar:

> Quanto impacto a guarda ainda suporta antes de quebrar?

Ataques bloqueados reduzem Postura.

---

# 50. Guard Break

### M06-06C — DECIDIDO — REVISADO

Quando Postura chega a zero:

```text
Postura = 0
↓
Guard Break
↓
abertura temporária
```

O defensor sofre uma abertura apropriada que pode ser aproveitada pelo atacante.

---

# 51. Função exclusiva da Postura

### M06-06C.1 — DECIDIDO

Postura existe exclusivamente para o sistema de Guard Break.

Ela NÃO será utilizada como:

- recurso de técnicas;
- Stamina alternativa;
- medidor de Stagger;
- recurso de movimentação;
- atributo ofensivo;
- sistema de progressão;
- moeda;
- requisito geral de habilidades.

Sua existência resolve apenas um problema:

**quando a guarda deve quebrar.**

---

# 52. Recuperação de Postura

### M06-06C.2 — DECIDIDO EM PRINCÍPIO

Após deixar de sofrer pressão defensiva por tempo apropriado, a Postura deverá recuperar-se naturalmente.

Tempo e velocidade exatos serão definidos posteriormente através de protótipo e balanceamento.

Não será necessário consumir itens ou recursos apenas para recuperar Postura em condições normais.

---

# 53. Pressão diferente por ataque

### M06-06C.3 — DECIDIDO

Ataques e técnicas poderão causar quantidades diferentes de dano à Postura quando bloqueados.

Isso permite distinguir:

```text
ataque leve
→ baixa pressão

finisher
→ pressão maior

golpe pesado
→ pressão elevada

técnica especializada em quebrar guarda
→ pressão muito alta
```

Isso não transforma Postura em outro sistema ofensivo.

É apenas a forma de determinar quão bem determinado ataque pressiona uma guarda.

---

# 54. Stamina e Postura

Stamina e Postura possuem responsabilidades diferentes:

```text
STAMINA
→ esforço físico

POSTURA
→ estabilidade da guarda
```

Bloquear um golpe não consumirá automaticamente Postura e Stamina simultaneamente.

Custos adicionais só existirão quando uma situação ou técnica específica justificar.

O objetivo da Postura é justamente impedir que bloquear constantemente sobrecarregue a função da Stamina.

---

# 55. Parry

### M06-06D — DECIDIDO

Parry não será uma ferramenta avançada universal entregue gratuitamente a todos os personagens.

Será uma capacidade adquirida através de:

- técnica;
- treinamento;
- estilo;
- outro contexto apropriado.

Diferentes caminhos podem possuir formas diferentes de defesa avançada.

Não é necessário criar um equivalente racial artificial para todos.

---

# 56. Esquiva universal

### M06-06E — DECIDIDO

Todo personagem possuirá uma esquiva curta e responsiva.

Ela deverá:

- utilizar Stamina;
- priorizar reposicionamento;
- retirar o personagem da trajetória do ataque.

O sistema não deverá depender principalmente de longas janelas artificiais de invulnerabilidade.

Pequenas tolerâncias técnicas poderão ser utilizadas caso necessárias para produzir bom gameplay.

---

# 57. Esquiva e mobilidade especial são diferentes

### M06-06F — DECIDIDO

Dodge não é equivalente a:

- Shunpo;
- Sonído;
- Hirenkyaku;
- outras técnicas de deslocamento.

Dodge é uma ferramenta defensiva fundamental.

Mobilidade especial é uma capacidade aprendida.

---

# 58. Mobilidade dentro e fora do combate

### M06-06G — DECIDIDO

Técnicas de movimentação legitimamente disponíveis poderão ser utilizadas tanto em combate quanto fora dele.

Possíveis usos:

- combate;
- exploração;
- perseguição;
- fuga;
- travessia;
- reposicionamento.

A implementação deverá respeitar limitações técnicas do Minecraft, desempenho e balanceamento.

A sensação de velocidade e mobilidade possui prioridade sobre tentar reproduzir literalmente distâncias ou velocidades do anime.

---

# 59. Técnicas contextuais a partir de ações básicas

### M06-06H — DECIDIDO

Ações fundamentais poderão alimentar técnicas contextuais aprendidas.

Exemplos:

```text
Dodge + ataque

Block + ataque

movimento + ataque

movimentação especial + técnica
```

Isso permite aumentar a profundidade sem aumentar excessivamente a quantidade de teclas.

---

# 60. Cancelamentos

### M06-06I — DECIDIDO

Esquiva, bloqueio e movimentação não cancelarão universalmente qualquer técnica em qualquer momento.

Cada técnica poderá declarar em quais fases permite:

- movimentação;
- bloqueio;
- esquiva;
- cancelamento voluntário.

Isso preserva comprometimento e risco das ações.

---

# 61. Controle

### M06-06J — DECIDIDO

Controle será composto por propriedades específicas quando necessárias.

Exemplos:

```text
Stagger
Knockback
Launch
Knockdown
```

Esses efeitos não serão aplicados automaticamente a todo ataque.

---

# 62. Anti-stunlock

### M06-06K — DECIDIDO EM PRINCÍPIO

O sistema deverá impedir cadeias excessivas de controle que removam continuamente a capacidade de resposta do alvo.

A implementação exata permanece pendente.

Possibilidades futuras:

- diminishing returns;
- resistência temporária;
- outro método equivalente.

O multiplayer poderá exigir refinamentos adicionais posteriormente.

---

# 63. Esquiva direcional

### M06-06L — DECIDIDO

A esquiva deverá seguir a direção indicada pelo jogador.

Modelo esperado:

```text
W + Dodge
→ frente

S + Dodge
→ trás

A + Dodge
→ esquerda

D + Dodge
→ direita
```

O comportamento exato quando nenhuma direção é fornecida poderá ser validado em protótipo.

---

# 64. Controle manual da direção

### M06-06M — DECIDIDO

A esquiva não escolherá automaticamente a melhor direção para evitar um ataque.

A decisão espacial permanece responsabilidade do jogador.

O objetivo é preservar previsibilidade e controle manual.

---

# 65. Contexto direcional

### M06-06N — DECIDIDO

Técnicas aprendidas poderão considerar a direção da esquiva como parte de suas condições.

Exemplo conceitual:

```text
esquiva lateral + ataque
→ Técnica A

esquiva para trás + ataque
→ Técnica B

esquiva para frente + ataque
→ Técnica C
```

Não existe obrigação de criar essas três variações.

Elas poderão existir quando um estilo ou técnica específica justificar.

---

# 66. Arquitetura consolidada até M06-06

Até este ponto, o combate universal possui a seguinte organização:

```text
COMBATE
│
├── Ações fundamentais
│   ├── ataque
│   ├── bloqueio
│   └── esquiva
│
├── Técnicas
│   ├── ativas
│   ├── contextuais
│   ├── passivas
│   └── poder principal
│
├── Combat Mode
│   └── Combat Hotbar 1–9
│
├── Recursos
│   ├── Stamina
│   └── Energia Espiritual
│
├── Medidor defensivo
│   └── Postura
│       └── exclusivamente Guard Break
│
├── Execução
│   ├── preparação
│   ├── carregamento quando aplicável
│   ├── execução
│   ├── Recovery
│   └── Cooldown individual
│
├── Desenvolvimento
│   ├── aquisição
│   ├── aprendizado
│   └── Mastery individual
│
├── Defesa avançada
│   └── aprendida através de técnicas
│
├── Mobilidade
│   ├── Dodge universal
│   └── mobilidade espiritual/racial aprendida
│
└── Controle
    ├── Stagger
    ├── Knockback
    ├── Launch
    └── Knockdown
```

---

# 67. Princípios consolidados

O sistema desenvolvido até este ponto segue os seguintes princípios:

### Simples na base, profundo no desenvolvimento

O jogador começa com uma linguagem de combate pequena e compreensível.

Seu repertório aumenta conforme aprende.

### Técnica não é atributo

Atributos representam quem o personagem é em sua fundação.

Técnicas representam o que ele aprendeu.

Mastery representa o quanto domina aquilo.

### Poder bruto não substitui conhecimento

Possuir atributos altos não concede automaticamente técnicas.

### Conhecimento não substitui poder bruto

Mastery melhora a técnica, inclusive moderadamente sua eficácia, mas não substitui atributos fundamentais.

### Flexibilidade com limites

O jogador poderá combinar repertórios e inspirações de personagens diferentes quando legitimamente compatíveis.

Isso não significa acesso irrestrito a qualquer capacidade.

### Sem classes fechadas de personagem canônico

O jogador poderá se tornar parecido com determinado personagem através de seu repertório, sem precisar selecionar permanentemente uma classe baseada nele.

### Identidade racial sem simetria artificial

Raças utilizarão a arquitetura universal quando útil, mas poderão possuir experiências e repertórios profundamente diferentes.

### Complexidade somente quando possui função

Novos medidores, propriedades ou regras só deverão existir quando resolverem um problema claro.

A Postura exemplifica esse princípio:

```text
Postura
→ existe somente para Guard Break
```

### Bleach como referência, mod como adaptação

A obra orienta fantasia e coerência.

O gameplay determina a implementação final.

---

# 68. Pendências deliberadas

Os seguintes valores e detalhes permanecem propositalmente indefinidos:

- escala numérica máxima de Mastery;
- fórmulas de dano;
- influência exata de Mastery no dano;
- custos específicos de técnicas;
- valores de Stamina;
- valores de Energia Espiritual;
- durações de Recovery;
- cooldowns;
- velocidade de recuperação de Postura;
- dano de Postura;
- duração de Guard Break;
- detalhes de anti-stunlock;
- distâncias de Dodge;
- velocidades de movimentação especial;
- tempos de carregamento;
- curvas de progressão;
- balanceamento PvE/PvP.

Esses elementos deverão ser definidos posteriormente através de:

```text
arquitetura
+
conteúdo racial
+
protótipo
+
simulação
+
playtest
+
balanceamento
```

Não deverão ser fechados prematuramente apenas para preencher números.

---

# M06-07 — Estrutura Shinigami

## Estado do bloco

**Status:** ESPECIFICADO EM NÍVEL ESTRUTURAL

Este bloco define a estrutura de habilidades e combate do caminho Shinigami dentro do Módulo 06.

O objetivo não é catalogar neste momento todas as técnicas existentes em Bleach, mas estabelecer uma arquitetura capaz de receber posteriormente esse conteúdo sem exigir reformulação dos sistemas fundamentais.

A estrutura Shinigami é baseada em quatro grandes campos de formação:

- Zanjutsu;
- Hakuda;
- Hohō;
- Kidō.

Essas disciplinas compartilham os sistemas universais já definidos no M06, mas não precisam possuir a mesma quantidade de conteúdo, a mesma progressão concreta ou o mesmo comportamento.

---

# 1. Princípio de implementação incremental

## M06-P04 — DECIDIDO — Complexidade incremental

Os sistemas do mod devem ser projetados para permitir aprofundamento futuro, mas sua primeira implementação não precisa conter toda a profundidade prevista pela arquitetura.

A primeira versão deve priorizar:

- funcionamento sólido;
- identidade reconhecível;
- diversão;
- legibilidade;
- possibilidade de expansão futura.

Uma implementação simples não será considerada provisória ou inadequada apenas por possuir menos profundidade.

O objetivo é:

```text
V1
→ simples
→ funcional
→ divertida
→ coerente
→ extensível

versões posteriores
→ refinamentos
→ novas técnicas
→ novas interações
→ maior profundidade
```

Não será necessário desenvolver todas as nuances possíveis antes de validar o núcleo do combate.

---

## M06-P05 — DECIDIDO — Escopo inicial das disciplinas

Zanjutsu, Hakuda, Hohō e Kidō poderão possuir arquiteturas que suportem repertórios, aplicações contextuais e interações mais avançadas do que aquelas presentes em suas primeiras implementações.

A V1 deverá entregar um núcleo suficientemente interessante de cada disciplina sem exigir imediatamente:

- todo repertório canônico;
- todas as variações de estilo;
- todas as técnicas contextuais imaginadas;
- todas as interações avançadas entre disciplinas.

A expansão deverá aproveitar a mesma arquitetura sempre que possível.

---

# 2. Identidade geral do caminho Shinigami

## M06-07A — DECIDIDO — Formação ampla sem classes permanentes

Zanjutsu, Hakuda, Hohō e Kidō constituem campos gerais de formação Shinigami.

Eles não serão tratados como classes mutuamente exclusivas.

Um jogador poderá concentrar sua progressão inicialmente em determinada disciplina sem perder permanentemente acesso às demais.

Exemplo:

```text
Jogador A
→ foco inicial em Zanjutsu

Jogador B
→ foco inicial em Kidō + Hohō

Jogador C
→ foco inicial em Hakuda

todos continuam capazes de desenvolver
outras disciplinas posteriormente
```

A escolha principal será:

> no que o personagem investirá primeiro?

e não:

> qual disciplina ficará permanentemente bloqueada?

---

## M06-07B — DECIDIDO — REVISADO — Proficiência das disciplinas

As disciplinas possuirão uma progressão geral de **Proficiência**.

Essa decisão substitui propostas anteriores de utilizar apenas técnicas + Mastery ou de criar sistemas separados de Rank.

A Proficiência responde:

> Qual é o desenvolvimento geral do personagem nessa disciplina?

Exemplo conceitual:

```text
Zanjutsu
Proficiência X

Hakuda
Proficiência Y

Hohō
Proficiência Z

Kidō
Proficiência W
```

A escala numérica definitiva ainda não foi estabelecida.

---

## M06-07B.1 — DECIDIDO — Proficiência ≠ atributos ≠ Mastery

Cada camada possui responsabilidade própria.

```text
ATRIBUTOS
→ capacidade fundamental do personagem

PROFICIÊNCIA
→ desenvolvimento geral em uma disciplina

TÉCNICAS
→ conhecimentos e ações efetivamente aprendidos

MASTERY
→ domínio individual de uma técnica específica
```

Exemplo:

Um jogador pode possuir grande Proficiência de Zanjutsu e aprender uma nova técnica.

Nesse momento:

```text
Zanjutsu
→ Proficiência alta

nova técnica
→ Mastery baixa
```

O personagem é um espadachim experiente, mas ainda precisa dominar aquela nova execução.

---

## M06-07B.2 — DECIDIDO — Sem segundo nível individual

Técnicas individuais não possuirão simultaneamente:

```text
Level
+
Mastery
```

Após uma técnica ser adquirida, sua progressão individual será representada pela Mastery.

Evitar:

```text
Cero Level 27
Cero Mastery 53
```

ou:

```text
Técnica de Zanjutsu Level 18
Mastery 71
```

Esse tipo de duplicação foi descartado.

---

# 3. Como a Proficiência progride

## M06-07G — DECIDIDO — Progressão orgânica

A Proficiência será desenvolvida através de:

- utilização válida da disciplina;
- treinamento;
- técnicas relacionadas;
- combate relevante;
- desafios apropriados.

Não será criada uma moeda separada utilizada para comprar Proficiência.

Fluxo conceitual:

```text
prática legítima
+
treinamento
+
uso real
↓
progresso da disciplina
↓
Proficiência aumenta
```

---

## M06-07H — DECIDIDO — Progressão moderada

A progressão deverá exigir investimento real, mas não deverá depender de grind excessivo.

O objetivo é permitir que Proficiência e Mastery cresçam naturalmente enquanto o jogador:

- avança a storyline;
- enfrenta inimigos;
- realiza quests;
- treina;
- utiliza seu repertório.

Não é desejado exigir horas de repetição desconectadas do restante do jogo apenas para alcançar um marco necessário.

---

## M06-07I — DECIDIDO — Progressão simultânea

Uma utilização legítima de determinada técnica poderá desenvolver simultaneamente:

```text
Proficiência da disciplina
+
Mastery da técnica utilizada
```

As progressões não são redundantes.

Uma mede experiência geral naquele campo.

A outra mede domínio específico daquela execução.

---

## M06-07J — DECIDIDO — Anti-farm contextual

O sistema deverá reduzir ou impedir progressão através de:

- ações AFK;
- alvos completamente irrelevantes;
- repetição artificial;
- spam sem contexto;
- loops criados apenas para gerar progresso.

Entretanto, essas verificações deverão permanecer majoritariamente internas.

O jogador comum deverá compreender uma regra simples:

> praticar legitimamente a disciplina faz o personagem evoluir.

---

# 4. Aquisição das disciplinas e das técnicas

## M06-03C — DECIDIDO — REFINADO — Papel dos mestres

Mentores não funcionarão como lojas de habilidades.

Também não será necessário encontrar um NPC diferente ou retornar ao mesmo NPC para aprender individualmente toda técnica existente.

Mentores poderão ensinar:

- uma disciplina;
- seus fundamentos;
- um repertório especializado;
- uma técnica específica;
- um conceito necessário para progressão.

A granularidade dependerá da importância do conteúdo.

---

## M06-03D — DECIDIDO — REFINADO — Desenvolvimento autônomo

Depois que o personagem receber formação suficiente em determinada disciplina, parte de seu repertório poderá ser desenvolvida através da própria progressão.

Modelo:

```text
encontra mestre
↓
aprende fundamentos
↓
disciplina desbloqueada
↓
Proficiência cresce
↓
novas técnicas gerais tornam-se disponíveis
↓
personagem continua evoluindo sem retornar
ao mestre para cada capacidade
```

Isso evita transformar a progressão em uma sequência constante de viagens burocráticas entre NPCs.

---

## M06-07K — DECIDIDO — Técnicas especiais continuam especiais

Nem toda capacidade será adquirida automaticamente através de Proficiência.

Técnicas específicas poderão exigir:

- mentor;
- treinamento especial;
- evento;
- quest;
- descoberta;
- condição narrativa;
- conhecimento anterior;
- outra forma coerente de aquisição.

A regra geral será progressão natural.

Exceções existirão quando agregarem valor.

---

# 5. Repertórios especializados

Um repertório especializado representa uma maneira particular de aplicar uma disciplina.

Pode estar associado a:

- treinamento específico;
- escola;
- personagem;
- mestre;
- organização;
- tradição;
- aplicação avançada.

Ele não constitui uma nova classe.

---

## M06-07C — DECIDIDO — Formação geral e especialização

A progressão Shinigami possuirá uma base relativamente ampla de formação, enquanto conhecimentos intermediários, avançados ou especializados poderão estar distribuídos pelo mundo.

Mestres, instituições e organizações orientam a progressão sem aprisionar permanentemente o jogador a um único estilo.

---

## M06-07J.1 — DECIDIDO — Mestres podem ensinar repertórios

Um treinamento especializado poderá desbloquear conhecimento suficiente para desenvolver múltiplas técnicas relacionadas.

Exemplo:

```text
treinamento com mestre especializado
↓
repertório conhecido
↓
Técnica A passa a ser desenvolvível
Técnica B passa a ser desenvolvível
Técnica C exige maior Proficiência
```

Não é necessário:

```text
NPC → Técnica A

volta ao NPC → Técnica B

volta ao NPC → Técnica C
```

quando isso não acrescentar nada à experiência.

---

## M06-07T — DECIDIDO — Repertório não é classe

Aprender um repertório especializado não impede o personagem de posteriormente aprender outro repertório compatível.

O estilo final surge da combinação de conhecimentos desenvolvidos pelo próprio jogador.

---

## M06-07T.1 — DECIDIDO — Sem Proficiência adicional para repertórios

Repertórios especializados não possuirão, por padrão, uma segunda progressão de Proficiência.

Modelo:

```text
Zanjutsu
→ Proficiência geral

Repertório especializado
→ conhecido ou não conhecido

Técnicas desse repertório
→ Mastery individual
```

Evitar:

```text
Zanjutsu Proficiência
+
Repertório A Proficiência
+
Técnica X Mastery
```

sem necessidade real.

---

# 6. Visualização da progressão

## M06-07L — DECIDIDO — Lista/repertório em vez de Skill Tree

Uma Skill Tree tradicional não será utilizada como estrutura universal de aquisição.

A proposta de Skill Tree foi analisada e posteriormente descartada.

O motivo principal é que técnicas não serão simplesmente compradas através de um menu.

A visualização futura deverá funcionar mais como um **repertório da disciplina**, podendo mostrar estados como:

```text
APRENDIDO
✓ Técnica A
  Mastery ...

DISPONÍVEL PARA DESENVOLVIMENTO
○ Técnica B

REQUER CONHECIMENTO ESPECIAL
◇ Técnica C

BLOQUEADO
🔒 Técnica D

NÃO DESCOBERTO
???
```

A interface definitiva pertence ao M14.

---

# 7. Técnicas híbridas

## M06-07D — DECIDIDO

Uma técnica poderá combinar fundamentos de múltiplas disciplinas.

Exemplo conceitual:

```text
Hakuda
+
Kidō
↓
técnica híbrida
```

A técnica híbrida continua sendo uma capacidade individual.

Não é necessário criar:

- uma quinta disciplina;
- uma Proficiência híbrida;
- outra árvore de progressão.

Ela poderá simplesmente exigir desenvolvimento apropriado nas disciplinas relacionadas.

---

# 8. Zanjutsu

## Identidade

Zanjutsu representa desenvolvimento geral no combate com espada.

Ele não representa automaticamente o poder específico da Zanpakutō.

Distinção fundamental:

```text
ZANJUTSU
→ como o personagem luta com a espada

ZANPAKUTŌ
→ o que aquela espada/poder específico consegue fazer
```

---

## M06-07E — DECIDIDO — Zanjutsu ≠ poder da Zanpakutō

Técnicas oriundas diretamente da habilidade específica de uma Zanpakutō pertencem ao conjunto do poder principal.

Elas não serão tratadas como técnicas gerais de Zanjutsu apenas porque são executadas utilizando uma espada.

---

## M06-07N — DECIDIDO — Fontes de Proficiência de Zanjutsu

A Proficiência de Zanjutsu poderá crescer através de:

- combate armado válido;
- utilização legítima de técnicas de Zanjutsu;
- treinamentos apropriados;
- desafios relacionados.

Kills poderão ajudar a determinar relevância de um combate, mas não serão a única ou principal medida de progresso.

Participação válida importa mais do que o último golpe.

---

## M06-07O — DECIDIDO — Mastery e Proficiência simultâneas

Utilizar validamente uma técnica de Zanjutsu poderá desenvolver:

```text
Mastery da técnica
+
Proficiência geral de Zanjutsu
```

---

## M06-07P — DECIDIDO — Anti-farm

Combater repetidamente alvos completamente irrelevantes, atacar objetos indefinidamente ou executar loops artificiais não deverá constituir estratégia eficiente de progressão.

---

## M06-07Q — DECIDIDO — Fundamentos evoluem

Nem toda melhoria no combate de espada precisa constituir uma técnica individual.

A Proficiência poderá ampliar ou refinar elementos fundamentais, como:

- sequências;
- finalizadores;
- transições;
- possibilidades contextuais;
- fluidez geral do moveset.

Isso não significa conceder grandes multiplicadores automáticos de dano.

---

## M06-07R — DECIDIDO — Técnicas gerais por progressão

Parte das técnicas gerais poderá tornar-se disponível através de marcos de Proficiência.

Dependendo da importância da capacidade:

```text
marco alcançado
→ fundamento incorporado automaticamente
```

ou:

```text
marco alcançado
→ técnica disponível para treinamento
→ processo simples de desenvolvimento
→ técnica adquirida
```

A granularidade não será obrigatoriamente igual para todo conteúdo.

---

## M06-07S — DECIDIDO — Proficiência como preparo

Determinados mestres, técnicas ou repertórios poderão exigir desenvolvimento mínimo de Zanjutsu.

Proficiência suficiente representa preparo.

Ela não concede automaticamente conhecimento especializado.

---

## M06-07U — DECIDIDO — Zanjutsu e Zanpakutō independentes

Alta Proficiência de Zanjutsu não concede automaticamente:

- Shikai;
- Bankai;
- técnicas específicas da Zanpakutō;
- Mastery dessas técnicas.

Essas progressões permanecem separadas.

---

# 9. Zanpakutō como poder vinculado

## M06-07V — DECIDIDO — Uma Zanpakutō vinculada

Cada personagem Shinigami possuirá uma identidade de Zanpakutō principal vinculada.

Essa identidade determina qual conjunto canônico de poder pertence àquele personagem.

A regra normal será:

```text
1 personagem
↓
1 Zanpakutō principal
```

O jogador não colecionará Zanpakutō para alternar poderes livremente.

---

## M06-07W — DECIDIDO — Espada como portadora técnica

A Zanpakutō será representada por um item/espada que carrega tecnicamente sua identidade.

Esse item poderá permitir ao sistema identificar:

- modelo;
- nome;
- poder;
- técnicas;
- liberações;
- estados;
- propriedades relacionadas.

Entretanto, a identidade não dependerá exclusivamente da posse física do item.

O personagem também possuirá registro de seu vínculo.

Conceitualmente:

```text
PLAYER
bound_zanpakuto = ryujin_jakka

ITEM
zanpakuto_id = ryujin_jakka
```

Os dois elementos trabalham em conjunto.

---

## M06-07X — DECIDIDO — Exclusividade do vínculo

Empunhar uma Zanpakutō incompatível não transfere automaticamente seu poder.

Exemplo:

```text
Jogador
→ vinculado a Senbonzakura

pega item de Ryūjin Jakka
↓
não passa a possuir Ryūjin Jakka
```

Trocar de item no inventário não substitui o poder principal do personagem.

---

## M06-07Y — DECIDIDO — Zanpakutō não é loot comum

A Zanpakutō vinculada não será tratada como uma espada comum cuja:

- perda;
- morte;
- roubo;
- duplicação;
- transferência;

passa automaticamente seu poder para outra pessoa.

O vínculo real permanece associado ao personagem.

A implementação deverá ser resiliente a problemas de inventário e multiplayer.

---

## M06-07Z — DECIDIDO EM PRINCÍPIO — Relação adaptada com a Zanpakutō

O relacionamento entre Shinigami e Zanpakutō continuará sendo parte importante da fantasia do caminho.

Entretanto, ele será adaptado às necessidades do mod.

Não será necessário reproduzir literalmente todas as formas de interação mostradas na obra.

O jogo poderá representar essa relação através de:

- quests;
- treinamentos;
- desafios;
- eventos;
- mundo interior;
- meditação;
- outras abstrações apropriadas.

Os detalhes pertencem ao M07.

---

## PENDENTE PARA M07

O M06 não fechará:

- como a Zanpakutō é atribuída;
- como ocorre o vínculo inicial;
- como seu nome é revelado;
- como Shikai é conquistada;
- como técnicas da Shikai progridem;
- como Bankai é conquistada;
- como o domínio das liberações funciona;
- quais requisitos existem entre essas etapas.

Esses elementos pertencem ao módulo de formas e poderes superiores.

---

# 10. Hakuda

## Identidade

Hakuda representa o desenvolvimento do corpo como instrumento de combate.

Ele compartilha a infraestrutura universal de combate, mas não será tratado simplesmente como “Zanjutsu sem espada”.

---

## M06-07HA — DECIDIDO — Proficiência de Hakuda

Hakuda utilizará a mesma arquitetura geral:

```text
Proficiência de Hakuda
+
técnicas de Hakuda
+
Mastery individual
```

Sua Proficiência poderá crescer através de:

- combate corporal válido;
- técnicas de Hakuda;
- treinamentos;
- desafios apropriados.

---

## M06-07HB — DECIDIDO — Moveset corporal

A evolução de Hakuda poderá modificar de maneira significativa o moveset desarmado.

Exemplos:

- sequências de socos;
- chutes;
- finalizadores;
- ataques contextuais;
- entradas;
- respostas após Dodge.

Nem todo movimento corporal será tratado como técnica individual.

---

## M06-07HC — DECIDIDO — Fundamento versus técnica

Movimentos que constituem partes naturais do combate corporal permanecerão como fundamentos.

Capacidades suficientemente distintas poderão tornar-se técnicas individuais.

Uma técnica individual poderá possuir:

- aquisição;
- custo;
- Recovery;
- Cooldown;
- requisitos;
- Mastery;
- efeitos próprios.

---

# 11. Hakuda e estado da Zanpakutō

## M06-07HD — DECIDIDO EM PRINCÍPIO — Empunhar/embainhar

A Zanpakutō poderá possuir estados de:

```text
empunhada
↕
embainhada
```

Essa alteração não exige mudar manualmente de slot ou manipular o inventário vanilla.

Seu objetivo é permitir transição natural entre combate armado e corporal.

O controle exato será definido posteriormente.

---

## M06-07HE — DECIDIDO EM PRINCÍPIO — Moveset por estado

Como regra fundamental:

```text
Zanpakutō empunhada
→ moveset armado/Zanjutsu

Zanpakutō embainhada
→ moveset corporal/Hakuda
```

Isso afeta principalmente ataques fundamentais.

---

## M06-07HF — DECIDIDO — Técnicas podem atravessar a fronteira

Hakuda não será rigidamente proibido enquanto uma arma estiver empunhada.

Uma técnica individual poderá declarar:

```text
requer combate desarmado
```

ou:

```text
permitida armado/desarmado
```

quando apropriado.

Isso possibilita estilos híbridos sem eliminar a identidade dos movesets fundamentais.

---

# 12. Stamina e Hakuda

## M06-07HG — DECIDIDO EM PRINCÍPIO

Hakuda possui relação natural com Stamina.

Entretanto:

```text
soco básico
chute básico
sequência fundamental
```

não deverão consumir grandes quantidades apenas por serem ataques corporais.

Stamina deverá possuir peso principalmente em:

- técnicas físicas exigentes;
- combos especiais;
- projeções;
- movimentos explosivos;
- ações de alto esforço.

---

# 13. Técnicas híbridas de Hakuda

## M06-07HH — DECIDIDO

Técnicas que misturam Hakuda com outra disciplina poderão exigir desenvolvimento relevante em ambas.

Ter alta Proficiência em apenas uma disciplina não concede automaticamente uma técnica híbrida importante.

---

# 14. Repertórios especializados de Hakuda

## M06-07HI — DECIDIDO

Hakuda poderá possuir repertórios especializados ensinados por mestres ou outras fontes apropriadas.

Esses repertórios representam aplicações particulares da disciplina.

Eles não constituem classes permanentes.

---

# 15. Técnicas de combo em Hakuda

## M06-07HJ — DECIDIDO — Combos como técnicas

Hakuda poderá possuir técnicas que representam sequências completas de golpes.

Exemplo:

```text
ativa Técnica X
↓
soco
↓
chute
↓
avanço
↓
novo impacto
↓
finalizador
```

A sequência inteira constitui uma única técnica.

Cada soco ou chute não precisa ser uma skill diferente.

---

## M06-07HK — DECIDIDO — Combos não substituem combate fundamental

Mesmo com técnicas de combo, o jogador continuará possuindo um moveset corporal funcional.

Hakuda não deverá se transformar em:

```text
pressiona skill
↓
personagem luta sozinho
↓
espera próximo cooldown
```

Técnicas complementam o combate fundamental.

---

## M06-07HL — DECIDIDO EM PRINCÍPIO — Comprometimento dos combos

Técnicas de combo poderão variar em:

- duração;
- mobilidade;
- cancelamento;
- Recovery;
- Stamina;
- controle;
- risco.

Combos mais longos ou fortes poderão exigir maior comprometimento.

---

# 16. Hohō

## M06-07HO-A — DECIDIDO — Hohō é uma disciplina

Hohō representa formação geral em movimentação e deslocamento avançado Shinigami.

Ele não será reduzido exclusivamente ao Shunpo.

---

## M06-07HO-B — DECIDIDO — Shunpo como técnica fundamental

Shunpo será uma capacidade fundamental aprendida dentro de Hohō.

Ele possuirá Mastery própria.

Distinção:

```text
Hohō Proficiência
→ experiência geral de movimentação Shinigami

Shunpo Mastery
→ domínio específico do Shunpo
```

---

## M06-07HO-C — DECIDIDO EM PRINCÍPIO — Acesso relativamente inicial ao Shunpo

Shunpo deverá ser adquirido em uma etapa relativamente inicial do desenvolvimento de Hohō, pois constitui parte importante da identidade Shinigami.

Aplicações mais sofisticadas permanecem posteriores.

Nenhum marco numérico está definido.

---

## M06-07HO-D — DECIDIDO — Progressão relevante

Hohō poderá progredir através de:

- Shunpo utilizado de maneira relevante;
- mobilidade aplicada ao combate;
- perseguição/evasão relevante;
- treinamentos;
- técnicas avançadas.

Simplesmente percorrer longas distâncias ou repetir Shunpo sem contexto não deverá constituir método eficiente de progressão.

---

## M06-07HO-E — DECIDIDO — Fundamento versus técnica

Direções e pequenas variações naturais de movimentação não serão transformadas em skills individuais.

Evitar:

```text
Shunpo esquerda
Shunpo direita
Shunpo trás
Shunpo frente
```

como quatro habilidades independentes.

Aplicações suficientemente distintas poderão existir como técnicas próprias.

---

## M06-07HO-F — DECIDIDO — Dodge ≠ Hohō

Dodge universal e Hohō permanecerão sistemas separados.

```text
Dodge
→ reposicionamento defensivo básico
→ universal
→ Stamina

Shunpo
→ mobilidade avançada
→ aprendida
→ Hohō
```

Mesmo usuários avançados de Hohō continuam possuindo Dodge.

---

# 17. Evolução do Shunpo

## M06-07HO-G — DECIDIDO

Mastery de Shunpo poderá melhorar moderadamente propriedades da própria técnica.

Possibilidades incluem:

- eficiência;
- execução;
- controle;
- Recovery;
- distância dentro de limites;
- velocidade dentro de limites.

Entretanto:

> Mastery melhora Shunpo; ela não concede automaticamente novas técnicas de Hohō.

Exemplo:

```text
Mastery alta de Shunpo
→ Shunpo melhor

não automaticamente:
→ Utsusemi
→ técnicas contextuais especiais
→ novas aplicações avançadas
```

Esses elementos permanecem capacidades independentes.

---

## M06-07HO-H — DECIDIDO — V1 simplificada

A primeira implementação de Shunpo poderá ser relativamente simples.

Modelo inicial aceitável:

```text
input dedicado
+
direção
+
deslocamento rápido
+
custo
+
Recovery
+
Cooldown apropriado
```

Sua Mastery inicialmente poderá modificar apenas alguns parâmetros.

A arquitetura deverá permitir aprofundamento posterior.

---

# 18. Kidō

## Identidade

Kidō representa o campo geral das artes espirituais Shinigami.

Sua estrutura principal será:

```text
KIDŌ
│
├── Hadō
├── Bakudō
├── Kaidō
└── técnicas especiais/não convencionais
```

Essas categorias não precisam possuir mecânicas idênticas.

---

## M06-07KI-A — DECIDIDO — Proficiência única

Kidō possuirá uma única Proficiência geral.

Não serão criadas, por padrão:

```text
Proficiência de Kidō
+
Proficiência de Hadō
+
Proficiência de Bakudō
+
Proficiência de Kaidō
```

Hadō, Bakudō e Kaidō representam categorias/conhecimentos dentro da disciplina.

---

## M06-07KI-B — DECIDIDO — Mastery individual

Cada Kidō suficientemente distinto será uma técnica individual e possuirá sua própria Mastery.

---

## M06-07KI-C — DECIDIDO — Repertório por Proficiência

Grande parte do repertório geral poderá ser disponibilizada através do desenvolvimento da Proficiência de Kidō.

Não será necessário retornar a um instrutor para cada feitiço.

Kidō especiais poderão continuar exigindo aquisição própria.

---

# 19. Numeração de Kidō

## M06-07KI-D — DECIDIDO — REFINADO

A numeração canônica será preservada quando aplicável.

Ela serve como importante referência de:

- identidade;
- dificuldade;
- complexidade;
- progressão.

Entretanto:

```text
Hadō #4
≠
Proficiência obrigatória 4

Hadō #90
≠
Proficiência obrigatória 90
```

Os requisitos serão definidos pelo design do mod.

Em geral, Kidō de numeração elevada tenderão a exigir maior desenvolvimento, sem existir conversão matemática obrigatória.

---

# 20. Kidō de nível baixo não se torna automaticamente inútil

## M06-07KI-H — DECIDIDO

Kidō de numeração menor poderá continuar útil para personagens avançados.

Sua relevância poderá permanecer através de:

- custo;
- velocidade;
- função;
- Mastery;
- atributos;
- aplicação tática.

Desbloquear Kidō de numeração maior não deverá necessariamente transformar todos os anteriores em versões obsoletas.

---

# 21. Execução e encantamento

## M06-07KI-E — DECIDIDO — REVISADO

Kidō poderá possuir diferentes modelos de execução.

Possibilidades:

```text
cast imediato

preparação

quick cast
versus
encantamento completo

canalização

execução específica
```

Nenhum modelo será obrigatório para toda a disciplina.

---

## Quick Cast × Encantamento

Quando apropriado, uma técnica poderá utilizar:

```text
TAP
→ execução rápida
→ encantamento omitido/reduzido
→ menor eficácia

HOLD
→ personagem realiza encantamento
→ maior preparação
→ versão mais completa
```

O jogador não precisará:

- falar no microfone;
- recitar pessoalmente;
- digitar encantamentos;
- memorizar textos.

A recitação pertence ao personagem.

Pode ser representada por:

- texto;
- animação;
- efeitos;
- áudio;
- tempo de preparação.

Essa estrutura não é apenas uma simplificação da V1; poderá permanecer como solução definitiva.

---

## M06-07KI-F — DECIDIDO — Mastery e encantamento

Mastery poderá melhorar a capacidade de utilizar determinado Kidō de forma abreviada.

Entretanto, Mastery elevada não elimina automaticamente a utilidade de uma execução completa quando esta fizer parte da técnica.

---

# 22. Recursos e atributos de Kidō

## M06-07KI-G — DECIDIDO

Energia Espiritual será o recurso primário esperado para a maior parte dos Kidō.

Outros elementos poderão variar conforme a técnica.

Atributos podem contribuir de formas diferentes.

Exemplo conceitual:

```text
Potência
→ capacidade ofensiva quando aplicável

Reserva
→ quantidade de energia disponível

Controle
→ precisão / eficiência / estabilidade

Mastery
→ domínio do Kidō específico
```

As fórmulas permanecem pendentes.

---

# 23. Hadō

Hadō representa principalmente a vertente ofensiva de Kidō.

Ele não deverá ser reduzido a uma sequência linear de:

```text
projétil pequeno
↓
projétil médio
↓
projétil grande
```

Diferentes Hadō poderão possuir funções distintas em:

- alcance;
- área;
- velocidade;
- pressão;
- dano;
- comportamento;
- preparação;
- custo.

O sistema de quick cast versus encantamento provavelmente terá grande utilidade em Hadō, mas continua dependente da técnica.

---

# 24. Bakudō

## M06-07KI-I — DECIDIDO — Identidade ampla

Bakudō não será tratado simplesmente como categoria de stun.

Poderá incluir funções como:

- contenção;
- controle;
- barreiras;
- defesa;
- selamento;
- rastreamento;
- utilidade;
- suporte.

Cada técnica preservará sua própria identidade.

---

## M06-07KI-J — DECIDIDO — Controle balanceado

Bakudō poderá produzir controle forte.

Entretanto, técnicas que retiram capacidade de resposta do alvo deverão respeitar:

- duração apropriada;
- anti-stunlock;
- resistência relevante;
- regras próprias da técnica;
- balanceamento PvP/PvE.

Controle deve gerar vantagem e oportunidade.

Não deve remover indefinidamente o direito de resposta do adversário.

---

## M06-07KI-N — DECIDIDO — Sem carregamento artificial

Bakudō não será obrigado a possuir:

```text
tap = versão fraca
hold = versão forte
```

Barreiras, contenções, defesas ou capacidades utilitárias poderão ser ativadas diretamente através de um único input quando isso produzir a melhor adaptação.

Uma barreira poderá simplesmente:

```text
input
↓
consumo de Energia
↓
barreira criada
↓
Cooldown
```

Preparação, encantamento ou canalização só serão adicionados quando fizerem sentido para aquela técnica.

---

# 25. Kaidō

## M06-07KI-K — DECIDIDO — Formação especializada

Kaidō utiliza a base geral de Kidō, mas conhecimento de cura deverá exigir formação especializada.

Alta Proficiência em Kidō não significa automaticamente saber curar.

Exemplo:

```text
Kidō desenvolvido
+
treinamento específico de Kaidō
↓
capacidade de utilizar cura
```

Não será criada obrigatoriamente uma segunda Proficiência exclusiva de Kaidō.

---

## M06-07KI-L — DECIDIDO EM PRINCÍPIO — Canalização

O comportamento fundamental de Kaidō poderá utilizar canalização.

Modelo conceitual:

```text
ativa/segura técnica
↓
canalização começa
↓
Energia Espiritual é consumida
↓
Vida do alvo é restaurada
↓
input é liberado
↓
canalização termina
```

Técnicas específicas poderão posteriormente possuir comportamentos diferentes.

---

## M06-07KI-M — DECIDIDO — Vida como função primária

A função primária de Kaidō será recuperação de Vida.

Kaidō básico não restaurará automaticamente:

- Vida;
- Stamina;
- Energia Espiritual;
- todas as condições negativas;

ao mesmo tempo.

Efeitos adicionais dependerão de capacidades específicas.

Isso evita loops ou excesso de funções concentradas numa única técnica.

---

# 26. Kidō de suporte

Não será criada uma categoria universal adicional denominada:

```text
Support Kidō
```

Grande parte das funções de suporte poderá permanecer dentro de Bakudō ou Kaidō conforme sua natureza.

Kidō que não se encaixem adequadamente nessas categorias poderão ser tratados individualmente como técnicas especiais.

---

# 27. Kidō especiais

Técnicas avançadas ou formas excepcionais de utilizar Kidō poderão futuramente incluir mecanismos próprios.

Exemplos conceituais:

- formas avançadas de encantamento;
- utilização simultânea;
- técnicas proibidas;
- execuções excepcionais;
- conhecimentos especiais.

Esses elementos não precisam estar presentes na primeira versão.

**Status:** BACKLOG / CONTEÚDO FUTURO

A arquitetura atual deverá apenas evitar impedir sua implementação posterior.

---

# 28. Estrutura Shinigami consolidada

```text
                         SHINIGAMI
                             │
             ┌───────────────┼───────────────┐
             │               │               │
        DISCIPLINAS      ZANPAKUTŌ       ATRIBUTOS
             │               │
   ┌─────────┼─────────┐     └── poder principal
   │         │         │
Zanjutsu   Hakuda    Hohō     Kidō
   │         │         │        │
   └─────────┴─────────┴────────┘
             │
        PROFICIÊNCIA
             │
             ↓
     REPERTÓRIO / TÉCNICAS
             │
             ↓
          MASTERY
```

Mais precisamente:

```text
ZANJUTSU
→ combate com espada

HAKUDA
→ combate corporal

HOHŌ
→ movimentação avançada

KIDŌ
→ artes espirituais

ZANPAKUTŌ
→ poder individual principal
```

Essas camadas podem interagir, mas não serão fundidas em um único sistema.

---

# 29. Relação entre disciplinas

Um Shinigami poderá construir seu próprio estilo combinando diferentes desenvolvimentos.

Exemplo:

```text
Zanjutsu alto
+
Hohō alto
+
Kidō moderado
+
Hakuda baixo
```

ou:

```text
Hakuda alto
+
Hohō alto
+
Kidō especializado
+
Zanjutsu moderado
```

A progressão não exige simetria entre as quatro disciplinas.

O personagem se diferencia principalmente pelo que decidiu desenvolver primeiro e pelo repertório que efetivamente adquiriu.

---

# 30. Relação entre disciplina, técnica e Mastery

Regra consolidada:

```text
DISCIPLINA
→ área geral de formação

PROFICIÊNCIA
→ experiência geral naquela área

TÉCNICA
→ capacidade específica aprendida

MASTERY
→ domínio individual daquela capacidade
```

Nenhum desses elementos deverá substituir os demais.

---

# 31. Regra de granularidade

Não haverá uma obrigação universal para decidir que toda ação precisa ser uma técnica.

A pergunta será:

> Esta ação possui identidade suficiente para justificar aquisição e Mastery próprias?

Se não:

```text
fundamento / moveset
```

Se sim:

```text
técnica individual
```

Essa regra se aplica a todas as disciplinas.

---

# 32. Primeira versão recomendada

A arquitetura permite uma primeira implementação relativamente enxuta.

## Zanjutsu V1

Pode começar com:

- moveset armado funcional;
- sequência básica;
- finalizador;
- bloqueio;
- Proficiência;
- pequeno conjunto de técnicas;
- Mastery dessas técnicas.

## Hakuda V1

Pode começar com:

- moveset desarmado;
- socos e chutes;
- sequência corporal;
- finalizador;
- algumas técnicas de combo;
- Proficiência;
- Mastery.

## Hohō V1

Pode começar com:

- Proficiência;
- Shunpo;
- movimentação direcional;
- custo;
- Recovery;
- Cooldown;
- Mastery de Shunpo.

## Kidō V1

Pode começar com:

- Proficiência;
- pequeno conjunto de Hadō;
- pequeno conjunto de Bakudō;
- sistema básico de encantamento quando necessário;
- pequena implementação de Kaidō caso esteja no escopo inicial;
- Mastery individual.

A V1 não precisa demonstrar todo o potencial da arquitetura para validá-la.

---

# 33. Decisões revisadas ou superadas

Para evitar conflitos futuros, ficam explicitamente registradas as propostas abandonadas.

## SUPERADO — Desenvolvimento apenas por atributos + técnicas + Mastery

Foi considerada insuficiente por não fornecer um indicador geral claro de desenvolvimento em cada disciplina.

**Substituído por:**

```text
atributos
+
Proficiência da disciplina
+
técnicas
+
Mastery
```

---

## SUPERADO — Níveis/Ranks separados de disciplina

Foi analisado um sistema de Ranks.

Posteriormente, concluiu-se que a **Proficiência direta da disciplina** oferece uma progressão mais simples e suficientemente clara.

Rank poderá existir futuramente apenas como rótulo visual derivado da Proficiência caso seja útil, não como uma segunda progressão obrigatória.

---

## SUPERADO — Skill Tree como estrutura de progressão

Foi analisada a utilização de Skill Trees.

A proposta foi descartada porque técnicas não serão adquiridas principalmente através de compra em menu.

**Substituído por:**

> repertório/lista da disciplina.

---

## SUPERADO — NPC individual para toda técnica

A ideia de exigir mentor para cada habilidade foi considerada excessivamente burocrática para o tamanho potencial do repertório de Bleach.

**Substituído por:**

- formação inicial;
- progressão por Proficiência;
- desenvolvimento autônomo;
- mestres para repertórios e técnicas quando apropriado.

---

## SUPERADO — Tap/Hold obrigatório para todo Kidō

O modelo:

```text
tap = versão rápida
hold = versão completa
```

continua disponível e importante, mas não será obrigatório.

Cada Kidō utiliza apenas as formas de execução que realmente agreguem valor.

---

# 34. Pendências deliberadas

Ainda não serão fechados:

- escala máxima das Proficiências;
- velocidade de progressão;
- marcos exatos de desbloqueio;
- catálogo completo de técnicas;
- valores de custo;
- valores de dano;
- duração de Recovery;
- duração de Cooldown;
- fórmulas;
- repertórios canônicos completos;
- todos os mestres;
- todos os métodos especiais de aquisição;
- detalhes de UI;
- detalhes de Shikai e Bankai;
- progressão das técnicas específicas da Zanpakutō;
- balanceamento final PvE/PvP.

Esses elementos dependem de conteúdo, protótipo, playtest e módulos posteriores.

---

# 35. Auditoria final do M06-07

Após revisão das decisões do bloco Shinigami:

**Não foram identificadas contradições estruturais impeditivas.**

A arquitetura possui responsabilidades claras:

```text
Atributos
→ capacidade fundamental

Proficiência
→ desenvolvimento geral

Repertório
→ conhecimentos disponíveis

Técnicas
→ capacidades específicas

Mastery
→ domínio individual

Zanpakutō
→ poder principal vinculado

M07
→ formas e grandes liberações
```

Também não existe dependência obrigatória de Skill Tree, progressão baseada exclusivamente em NPCs ou grind excessivo.

A estrutura suporta uma primeira versão simples e expansão posterior sem exigir redesenho completo.

## Resultado da auditoria

**M06-07 — Estrutura Shinigami: APROVADO EM NÍVEL ESTRUTURAL**

O próximo bloco do Módulo 06 é:

# M06-08 — Hollow / Arrancar