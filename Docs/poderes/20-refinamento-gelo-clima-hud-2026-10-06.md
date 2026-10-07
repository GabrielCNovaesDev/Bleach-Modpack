> **Estado vigente — ciclo 21, 07/10/2026:** tornado centrado e móvel com miolo livre de três/raio externo nove; Zona Glacial Shikai dez/Bankai trinta com neve temporária no interior; clima com restauração e prioridade para comando manual; passivas de fogo Ryujin e resistência Hyōrinmaru. [Passo a passo, lógica, arquivos, valores e testes](21-correcao-tornado-zona-clima-passivas-2026-10-07.md). Os ciclos abaixo são históricos; esta revisão substitui o deslocamento frontal do tornado do ciclo 20.

# Ciclo 20 — gelo, proteção, hipotermia, clima e HUD

Data: 06/10/2026. Branch exclusiva: `Feature-Poderes-bankais`. Base: `1376ca0`.
O usuário homologou a onda e as auras da Ryujin, pediu mais partículas e afastamento do tornado, e autorizou o refinamento da Hyōrinmaru com quatro habilidades seladas, armadura, hipotermia, criatura visual, estruturas de Bankai, tempestade e guia de habilidades. Confirmou que o clima deve durar enquanto a Bankai estiver ativa e retornar ao anterior ao encerrar.

## 1. Ryujin — acabamento homologado com dois ajustes

- `FlameWaveService.java`: cinco partículas por amostra vertical, antes duas (+150% na frente). Dispersão passa a 0,22 horizontal/0,16 vertical e velocidade 0,022. Altura quatro, avanço, abertura, dano, alcance e residual permanecem como no ciclo 19.
- `RyujinTechniqueService.java`: centro do tornado passa a três blocos à frente, na direção horizontal registrada na ativação. Emissões e dano usam o mesmo centro deslocado. Continua acompanhando a posição do jogador, com essa direção fixada; raio seis, altura dez, duração/custo/recarga anteriores. Isso afasta o miolo da espiral da câmera.
- Aura de chamas Shikai, fumaça Bankai, Muralha, morcegos e Corte de Vapor permanecem homologados.

## 2. Hyōrinmaru selada — novo contrato aprovado pelo usuário

A decisão anterior de limitar a selada a dois cortes físicos é substituída pelo pedido deste ciclo: quatro habilidades básicas de gelo. Não há bloqueio adicional de maestria nesses slots; ainda é necessário personagem Shinigami criado, identidade Hyōrinmaru vinculada e seu item na mão principal.

| Tecla | Habilidade | Parâmetros vigentes |
|---|---|---|
| H | Corte Glacial | Alcance 6 (antes 3), dano base 3, lentidão I por 40 ticks; custo 8; recarga 100 ticks |
| N | Leque de Neve | Alcance 6, abertura maior, dano base 3, lentidão I por 40 ticks; custo 8; recarga 160 ticks |
| B | Armadura de Gelo | Proteção temporária de 25% por 120 ticks/6 segundos; custo 12; recarga 240 ticks |
| C | Choque Glacial | Um alvo: o mais próximo no cone frontal de alcance 5, com linha de visão; empurrão 0,45 horizontal/0,1 vertical, lentidão IV por 60 ticks e hipotermia vanilla; custo 18; recarga 300 ticks |

Os danos base continuam sujeitos aos atributos/formas normais. A proteção reduz o dano recebido pelo evento `LivingHurtEvent`, antes da resolução final normal. Não bloqueia dano marcado para ignorar invulnerabilidade (ex.: mecanismos de remoção/void). Não cria equipamento no inventário nem modificadores permanentes.

O "atordoamento" de C é controle por lentidão forte e empurrão: não bloqueia teclas, ataques ou todos os movimentos do alvo. O dano contínuo vem do congelamento vanilla; respeita imunidades/equipamentos do Minecraft e vulnerabilidades especiais de tipos de entidade. É normalmente 1 de dano a cada 40 ticks enquanto totalmente congelado. Ao terminar, a hipotermia descongela naturalmente; lentidão já aplicada também expira naturalmente.

## 3. Shikai / Bankai — combate e visual

Os alcances, danos base, custos e recargas dos slots liberados do ciclo 19 continuam. H/N agora têm partículas de neve e poeira azul, sons de corte/vidro, e aplicam hipotermia além da lentidão quando o dano é aceito. B aplica hipotermia aos alvos que cruzam a barreira; C renova o frio nos pulsos aceitos. A barreira continua sendo controle visual, sem blocos sólidos ou bloqueio de projéteis.

Emissões por ponto do corte: selada 8 partículas de neve; Shikai 12; Bankai 18, mais poeira azul. Campos: 8 por ponto no Shikai, 14 na Bankai, a cada quatro ticks. A transição de liberação produz anel de neve e som; Bankai usa pulso maior e som grave. A aura corporal mantém neve esparsa e aumenta na Bankai.

O ataque H liberado invoca uma **criatura visual com o esqueleto/modelo nativo de Phantom** e material de gelo compacto. Tamanho maior na Bankai. Move-se para frente por 14/20 ticks, deixa neve e desaparece. Sem IA, dano próprio, colisão ativa, seleção por mira, spawn natural, salvamento ou saque. Há no máximo uma por proprietário; novo disparo substitui a anterior. O dano do golpe continua calculado na ativação, sem segundo acerto da criatura. Não é um modelo autoral de dragão do anime.

As camadas sobre a skin usam geometria nativa e material de gelo compacto:

1. B selada ativo: placas no tronco e braço esquerdo.
2. Shikai: tronco, dois braços e duas canelas.
3. Bankai: conjunto anterior, duas estruturas de asas nas costas e cauda segmentada, com oscilação leve.

Camadas preservam a skin entre as placas e acompanham partes do modelo do jogador. Compatíveis com renderizadores padrão/skin fina. São observáveis em terceira pessoa e por outros jogadores. As camadas de transformação são visuais; não adicionam uma segunda redução de dano passiva à Bankai. A proteção mecânica de B selada é temporária e cancelada ao desequipar, mudar forma, morrer, trocar identidade ou expirar.

## 4. Tempestade da Bankai e restauração

`BlizzardWeatherData.java` usa `SavedData` por nível para guardar o clima original (chuva/trovão, tempos e níveis visuais) e concessões temporárias por proprietário. Enquanto houver Bankai válida, ativa chuva/nuvens vanilla sem trovão/raios. Ao terminar o último proprietário, sair da dimensão, morrer, trocar identidade ou entrar em bioma incompatível, restaura o clima registrado. Múltiplos jogadores compartilham o estado; um encerrar não encerra a tempestade de outro.

- Somente Overworld com céu aberto de dimensão e sem teto; Nether/End não recebem mudança climática.
- Bioma precisa permitir precipitação. Deserto e outros biomas secos não geram a neve local e não ativam a tempestade por esse jogador.
- Neve de partículas aparece em raio aproximado de 12 blocos ao redor do portador, sob céu aberto e em posições de chunks já carregados. Não aparece dentro de construções fechadas.
- **Limite do motor vanilla:** chuva/nuvens são globais por dimensão; neve nativa e seus acúmulos naturais aparecem nos biomas frios. Em biomas temperados, a precipitação nativa continua chuva, acompanhada da neve local da habilidade. Desertos não recebem precipitação, embora possam compartilhar o céu nublado global quando outro portador estiver em bioma permitido.
- Não muda biomas/temperaturas nem coloca blocos de neve manualmente. Neve/gelo naturais seguem as regras normais do Minecraft; não são apagados ao encerrar a Bankai.
- Não modifica `doWeatherCycle`; a ativação explícita pode controlar o clima mesmo com ciclo natural desativado.
- Se outro comando/sistema modificar o clima depois, a limpeza não desfaz essa alteração. A Bankai que perdeu a concessão não reaplica a tempestade a cada tick; uma nova ativação poderá adquiri-la.
- Estado original é persistido para recuperar uma tempestade sem proprietários após reinício. A parada normal também restaura o estado. Sem proprietários, a concessão expira no máximo em 40 ticks caso um evento de saída não seja entregue.

O clima depende da identidade/formação Bankai, não de manter a espada equipada. Desequipar cancela campos/armadura temporária e criatura; a tempestade continua enquanto a forma Bankai válida permanecer.

## 5. HUD pequeno de habilidades

`ZanpakutoHud.java` registra painel à direita, acima da hotbar: nome da Zanpakutō, forma e maestria atual, quatro nomes curtos e **teclas reais das configurações**. Trocar forma/identidade atualiza os nomes, incluindo B/C da selada. Idiomas português/inglês recebem rótulos próprios, sem frases longas de execução.

Requisitos são derivados de `FormRegistry`: selada M.0; formas liberadas exigem seu desbloqueio e os pré-requisitos de maestria configurados. O rodapé indica a próxima liberação (por exemplo, Bankai requer maestria de Shikai 25 na configuração padrão, além do desbloqueio). A escala atual é 0–100; o "400" da imagem enviada é somente referência visual. Não foram inventados patamares de 90/160/250 nem travas individuais novas para cada ataque. História e desbloqueios definitivos continuam com a equipe.

## 6. Arquivos e lógica passo a passo

1. `HyorinmaruTechniqueService.java`: remove bloqueio B/C selada; novos custos básicos, alcance 6, sons/neve, roteamento para armadura/duelo, invocação cosmética e hipotermia em golpes/campos. Pulsos de campo somente no tick normal, evitando emissão dupla na ativação. Limite de um campo/uma criatura por jogador.
2. `IceArmorService.java` + `CombatEvents.java`: concessão por UUID com expiração por tempo do nível e redução de 25%; cancelamento centralizado e atualização visual. `CharacterData.java` envia flag temporária em aparência, sem gravar no progresso NBT.
3. `IceControlService.java`: lentidão e congelamento vanilla com concessões por alvo/proprietário; o tick do servidor mantém o estado pelo prazo. Até 32 alvos por proprietário e 512 no servidor. Imunidade ao frio respeitada; sem dano próprio extra que duplicaria o dano vanilla.
4. `IceDragonEntity.java`, `ModEntities.java`, `ModEntityEvents.java`, `ModEntityRenderers.java`: entidade transitória baseada em Phantom, atributos registrados, renderizador com material vanilla de gelo e retirada da camada de olhos original. `TechniqueTargets.java` exclui a criatura cosmética dos ataques dos kits.
5. `IceArmorLayer.java` + eventos de renderização: partes independentes para corpo, braços, pernas, asas e cauda; somente cliente. Nenhuma nova textura PNG necessária.
6. `HyorinmaruReleaseEffects.java`: pulso na liberação e aura corporal proporcional à forma, somente partículas/som.
7. `BlizzardWeatherData.java`: concessão climática, registro do estado, recuperação, elegibilidade de dimensão/bioma, partículas locais, expiração e restauração cooperativa. Eventos do nível e tick do portador atualizam o estado.
8. `RyujinTechniqueService.java`: ciclo de vida comum cancela novos efeitos e restaura clima na parada. `FlameWaveService.java`: densidade da frente; centro do tornado ajustado em criação e tick.
9. `ZanpakutoHud.java`, `BleachClient.java`, idiomas: guia responsivo, nomes/atalhos e requisitos reais. `Reference.java`: protocolo **2.5**, cliente e servidor atualizados juntos por causa da nova entidade e apresentação.
10. `MvpRegressionTest.java`, `IceRefinementGameTests.java`, `RyujinGameTests.java`: geometria da armadura, flag transitória, catálogo/traduções, redução/expiração, controle de um alvo, dano de hipotermia vanilla, alcance, criatura temporária, restauração do clima/overlap/reinício/deserto/Nether e área deslocada do tornado.

## 7. Homologação sugerida

Usar os comandos de vínculo e desbloqueio do [ciclo 19](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md#5-roteiro-de-homologação-operador). H/N/B/C agora funcionam também na selada. Primeiro testar custos/recargas gratuitos; depois restaurar para medir o balanceamento real.

1. Ryujin: N nas três formas com frente mais concentrada; B Bankai com miolo três blocos à frente. Comparar visão e FPS.
2. Hyōrinmaru selada: H/N atingem alvo a cinco/seis blocos, com neve/som; B exibe placas em F5 e reduz dano por seis segundos; C controla somente o alvo mais próximo no cone, com lentidão, empurrão e hipotermia. Jogadores podem observar a borda de congelamento vanilla.
3. Shikai: conferir mais partículas e criatura de gelo em H. Atravessar B com um inimigo e verificar congelamento. Conferir que aliados/NPCs protegidos não recebem efeitos.
4. Bankai: conferir armadura ampliada, asas/cauda em F5 e por outro jogador, pulso de liberação, criatura maior e campo mais intenso.
5. Clima: ativar sob céu aberto no Overworld; testar bioma frio, temperado e deserto, depois Nether/End. Encerrar com V e verificar clima anterior. Com dois portadores, encerrar um e depois o outro. Testar `/weather clear` durante a concessão.
6. HUD: mudar identidade/forma, remapear uma tecla e mudar escala da GUI; confirmar rótulos/atalhos/requisitos sem cobrir as barras ou a hotbar.
7. Remover espada, morrer, reconectar e trocar dimensão: nenhum campo, criatura ou proteção temporária deve ficar permanente. Hipotermia/lentidão aplicadas terminam pelo descongelamento/duração normal.

Validação final: `check build runGameTestServer --offline` — **57 regressões e 29 GameTests aprovados**, JAR gerado. Resultados também registrados em `progresso.md`. Aparência, FPS, legibilidade do HUD e equilíbrio de duelo continuam sujeitos à homologação do usuário no cliente. Inicializador e documentação de outras pastas preservados. Nenhum commit na `main`.
