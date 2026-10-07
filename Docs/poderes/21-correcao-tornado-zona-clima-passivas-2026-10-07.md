# Ciclo 21 — tornado centrado, território glacial e passivas

Data: 07/10/2026. Branch exclusiva: `Feature-Poderes-bankais`. Base: `a190264`.

O usuário homologou os visuais do ciclo 20 e corrigiu a interpretação do tornado: deve continuar ao redor do jogador e acompanhá-lo, com mais espaço no miolo. Solicitou corrigir o clima ao encerrar Bankai, distribuir neve/dano pelo interior da Zona Glacial, ampliar a Bankai até 30 blocos e adicionar passivas às liberações das duas identidades. Os modelos, texturas, asas, cauda, HUD e inicializador não foram alterados neste ciclo.

## 1. Tornado da Ryujin

1. `RyujinTechniqueService.java`: origem e centro de cada tick voltam à posição atual do jogador, sem deslocamento frontal. O raio externo passa de seis para nove blocos; a espiral começa a três blocos do centro em todos os níveis, abrindo espaço na câmera. Altura dez, duração 200 ticks, custo 40, recarga 300 e dano base dois permanecem.
2. `TechniqueGeometry.java`: `intersectsAnnulus` intersecta a caixa do alvo com o cilindro externo e exclui caixas inteiramente dentro do miolo de raio três. Dano e desenho acompanham o mesmo centro. O dano continua sujeito a aliados, PvP, linha de visão e intervalos anteriores.
3. `RyujinGameTests.java`: alvos na frente e atrás, miolo vazio, limite externo e deslocamento do jogador cobrem a correção.

## 2. Clima da Bankai

1. `BlizzardWeatherData.java`: a propriedade do clima usa os estados de chuva/trovão/tempo limpo, sem exigir que o contador de chuva acompanhe exatamente o relógio do mundo. Essa exigência poderia descartar o snapshot e impedir a restauração. O bytecode local de `ServerLevel.advanceWeatherCycle` foi consultado: o Minecraft controla esses contadores separadamente.
2. A última Bankai encerrada restaura os estados, temporizadores e intensidade registrados na entrada. Sobreposição de jogadores mantém a tempestade até o último sair; snapshot continua persistido para recuperação após reinício.
3. Comando `/weather` válido de operador entrega o controle ao administrador. As concessões existentes permanecem até seus donos saírem, impedindo reativação automática no tick seguinte, inclusive ao chegar um segundo dono. Partículas locais da tempestade também param quando o controle é entregue. Outra ativação após todas as liberações encerradas pode iniciar uma nova tempestade.
4. A detecção de mudança externa de estado continua respeitando `clear`, trovões e outros controladores. Overworld, dimensão com céu e bioma com precipitação continuam obrigatórios; desertos e dimensões excluídas não recebem a tempestade.

## 3. Zona Glacial — tecla C nas liberações

1. `HyorinmaruTechniqueService.java`: raio Shikai dez, Bankai trinta. Origem fica no ponto da ativação; a zona dura 80 ticks/quatro segundos, recarga 300, custo 30/35. A mudança de raio não altera dano base dois/três por pulso a cada dez ticks.
2. Os pulsos verificam todo o cilindro, com interseção de caixas e tolerância vertical quatro, em vez de depender do extremo. Linha de visão, aliados, PvP, imunidades e dano aceito continuam necessários. Hipotermia e lentidão usam o controlador já existente, limitado a 32 alvos por dono.
3. Emissões antes restritas a 16 pontos do perímetro passam a 32 amostras Shikai/64 Bankai distribuídas pelo disco a cada quatro ticks. Bankai usa mais altura e neve. Somente partículas não decidem dano.
4. `snowPatches` forma tapetes de 3 × 3 em posições distribuídas no território, procurando apoio até quatro blocos abaixo/três acima. São seis tentativas de tapete Shikai/doze Bankai por emissão. A neve tem uma camada Shikai e de uma a três Bankai. A cobertura é esparsa, não preenche integralmente os 2.800 blocos do disco.
5. Novo `GlacialSnowData.java`: coloca `Blocks.SNOW` apenas em ar e com apoio válido, nunca substitui construções, água ou neve natural. Limites de 384 posições por dono e 2.048 por dimensão; interrompe novas tentativas quando o dono alcança sua cota. Não carrega chunks para colocar ou limpar.
6. Expiração e encerramento removem apenas o estado exato de neve criado. Se alguém substituiu o bloco ou alterou suas camadas, a limpeza preserva o novo estado e abandona aquele registro. Donos sobrepostos compartilham a posição até o último encerrar. Posições, camadas e prazos ficam em SavedData; registros em chunks descarregados aguardam carregamento para limpeza. Neve que derrete naturalmente também é removida do registro.
7. Expirar a zona continua independente da tempestade da Bankai. Troca de forma/item, morte, logout e cancelamento liberam sua neve; prazo persistido garante limpeza posterior também após descarregamento ou reinício.

## 4. Passivas das liberações

1. Novo `ReleasePassives.java` deriva os efeitos de personagem criado, raça Shinigami, grupo Zanpakuto, identidade e forma atuais. Não grava encantamentos, atributos permanentes ou progresso.
2. Hyōrinmaru: 15% de redução Shikai e 30% Bankai em `LivingHurtEvent`. Funciona enquanto transformado, inclusive sem espada na mão, como a armadura visual. A armadura temporária B selada permanece 25%; os valores não se acumulam. Dano que ignora invulnerabilidade fica fora dessa proteção.
3. Ryujin: ao aplicar dano de ataque do jogador com a Ryujin vinculada na mão principal, a Shikai aplica quatro segundos de fogo e Bankai oito. `CombatEvents.damageApplied` exige dano positivo e a política comum de alvos. Os ataques de técnica que usam `playerAttack` participam desse mesmo evento. A aura Shikai continua chama e Bankai continua fumaça; nenhum encantamento permanente é adicionado ao item.
4. Retornar à selada, mudar identidade ou deixar de cumprir os requisitos elimina as passivas imediatamente. Outros sistemas de atributos e efeitos vanilla continuam aplicando suas próprias regras após esse fator de redução.

## 5. Verificação automatizada

Validação aprovada: `check build runGameTestServer --offline --console=plain`, 57 cenários de regressão e 33 GameTests obrigatórios. Artefato: `build/libs/bleachmod-0.2.0.jar`. Log local ignorado pelo Git: `build/zone-passives-validation-3.log`. Os testes de zonas amplas usam lotes próprios: no mundo compartilhado, o novo alcance atingia fixtures vizinhas e invalidava controles negativos de hipotermia/barreira. Essa separação corrige a montagem dos testes, preservando o alcance solicitado.

## 6. Homologação manual

1. Reinicie usando `Iniciar-Teste` para carregar o JAR atualizado. Use os comandos de teste do manual para vincular cada identidade e liberar suas formas.
2. Ryujin Bankai: B com mobs à frente, atrás e nos lados, entre três e nove blocos; ande e confirme que a área segue o jogador, mantendo a visão livre no miolo. Conferir altura/espiral anteriores.
3. Ryujin Shikai/Bankai: acertar um mob com a espada e verificar fogo. Voltar à selada sem H/ignição e confirmar que a passiva desaparece. Fumaça Bankai deve continuar na lâmina.
4. Hyōrinmaru: antes da Bankai, definir `/weather clear 100`; ativar, aguardar vários segundos, descer com V e verificar retorno. Repetir começando com chuva. Com Bankai ativa, `/weather clear 100` deve prevalecer e interromper as partículas de tempestade; testar dois jogadores se disponível.
5. C Shikai e Bankai em terreno apoiado: conferir neve no interior e vários tapetes, intensificação Bankai, mob próximo ao centro, mob a 28 e controle externo a 32 blocos. A zona é fixa no ponto da ativação, enquanto o tornado segue o personagem.
6. Após quatro segundos ou troca de forma, os tapetes devem desaparecer. Substituir uma posição temporária por um bloco de construção e conferir que fica preservado. Não confundir neve natural do bioma com a neve temporária da habilidade.
7. Comparar o mesmo golpe recebido na selada, Shikai e Bankai Hyōrinmaru, com os mesmos atributos/equipamentos; verificar a proteção e seu fim ao voltar à selada.

Balanceamento de raios/passivas é inicial e revisável pela equipe. Vínculo narrativo, história e progressão final continuam nas responsabilidades já documentadas; esta entrega não redefine esses contratos.