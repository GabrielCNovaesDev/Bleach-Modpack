> **Revisão vigente — fogo ambiental e inicializador, 04/10/2026:** implementados focos temporários de Bankai com persistência/expiração e inicializador clicável na raiz. Katana, Muralha 18 × 2 × 8 e HUD do ciclo anterior preservados. [Arquivos, lógica, limpeza e roteiro de testes](14-fogo-ambiental-inicializador-2026-10-04.md). Os registros abaixo documentam os ciclos anteriores.

# Ryujin — ciclo de acabamento visual e revisão técnica

Data: 04/10/2026. Branch: `Feature-Poderes-bankais`. Base: `3b0bbfd`.
O usuário autorizou o ciclo proposto após encerrar o kit funcional anterior. Solicitou
uma katana de um gume e autorizou reduzir altura/ampliar alcance da Muralha.
Números adotados para este teste: 18 × 2 × 8 blocos, duração mantida em cinco segundos.
Essa autorização não reabre os protótipos de Círculo nem troca as habilidades do kit.

## 1. Katana de um gume, passo a passo

1. Mantido `ryujin_katana_handheld.json`, preservando a pegada corrigida nas duas mãos.
2. Nos modelos `ryujin_jakka.json`, `ryujin_jakka_shikai.json` e `ryujin_jakka_bankai.json`,
   a lâmina antiga foi substituída por quatro faixas com deslocamento progressivo:
   curvatura em segmentos, um fio fino e dorso espesso separados. A ponta é assimétrica.
3. Os elementos nomeados `single_cutting_edge` ficam de um único lado; `blunt_spine`
   fica do outro. Espessura do fio 0,06 contra 0,5 do dorso, em unidades de modelo.
   Cabo, faixas e guarda preservados. São 22 elementos por forma, antes 14.
4. Shikai e Bankai ganharam superfícies PNG opacas próprias. UVs da face larga
   percorrem a imagem, em vez de amostrar um único pixel como uma cor uniforme.
5. Shikai: chamas claras/laranja sobre aço aquecido. Bankai: carvão, queimaduras,
   fissuras rubras e calor concentrado. O fio usa a região clara da superfície;
   o dorso usa a região escura. Selada mantém metal com fio destacado.
6. Geometria e UVs foram renderizados em uma prévia plana de desenvolvimento;
   o parser `BlockModel` valida os modelos. Isso não equivale a testar primeira pessoa
   no cliente, iluminação, inventário, offhand ou movimento de ataque.

Novos assets, criados com o **tool built-in imagegen**, sem CLI/API externa:

- [Shikai](../../src/main/resources/assets/bleachmod/textures/item/ryujin_shikai_surface.png).
- [Bankai](../../src/main/resources/assets/bleachmod/textures/item/ryujin_bankai_surface.png).

PNG gerados de 1254 × 1254, copiados para o projeto sem alterar os sprites originais.
O brilho da textura é cor; este ciclo não adiciona shader emissivo ou renderer customizado.
As partículas da etapa 2 fornecem movimento público ao redor da arma.

## 2. Liberação e aura

Arquivo novo: `src/main/java/com/bleachmod/common/technique/RyujinReleaseEffects.java`.

1. Consome `BleachEvents.FormChangeEvent`, após transformação aceita pelo servidor.
2. Exige grupo Zanpakutō e Ryujin equipada. Dispara em Selada → Shikai ou entrada
   em Bankai; não repete na mesma forma nem na descida Bankai → Shikai.
3. Emite um anel de FLAME com velocidade radial dirigida, fumaça central e som
   `BLAZE_SHOOT`. Shikai tem 20 pontos/raio 1,5; Bankai 32 pontos/raio 3 e som mais grave.
4. Aura esparsa a cada oito ticks: três chamas em Shikai, uma em Bankai, posicionadas
   perto da mão/lâmina segundo yaw. Só funciona com personagem válido, vivo,
   não espectador, Shinigami/Zanpakutō e arma válida; cessa sem manter runtime.
5. Não causa dano, não coloca fogo e não altera mundo. Efeitos são emitidos no servidor
   para observadores, sem novo pacote e sem campos persistentes.

## 3. Muralha mais baixa e longa

Arquivo: `RyujinTechniqueService.java`.

1. `WALL_LENGTH`: 16 → 18. `WALL_HEIGHT`: 15 → 8. Largura continua 2.
2. A geometria existente recebe as novas constantes: origem um bloco à frente,
   direção horizontal, volume orientado fixo. Alcance maior não dispensa linha de visão.
3. Emissão passa de 240 amostras × 5 chamas/2 ticks para 144 × 3/4 ticks.
   São 432 partículas por emissão, aproximadamente 2.160/s em 20 TPS, redução de 82%
   frente às 12.000/s anteriores. São contagens de emissão, não benchmark de FPS/TPS.
4. Mantidos cinco segundos, custo 40, recarga 400 ticks, dano base 18 a cada 20 ticks,
   fogo três segundos e knockback 0,25. Não houve aumento de dano por ampliar alcance.
5. GameTest verifica contato distante, exclusão acima da nova altura e ausência de
   fogo quando o dano é rejeitado. A fixture usa espaço aberto e chunk carregado:
   o mundo de testes possui terreno subterrâneo fora das células escavadas.

## 4. Alvos e aplicação de fogo

Arquivo novo: `TechniqueTargets.java`; alterados `TechniqueService.java`,
`RyujinTechniqueService.java` e `CombatEvents.java`.

1. Centralizada a política: exclui lançador, mortos, espectadores, aliados vanilla,
   NPCs de quest e morcegos marcados. Jogadores respeitam PvP e `canHarmPlayer`.
2. Aplicada a todas as consultas do kit, incluindo Dash, Leque, Corte e Flame Burst.
   Morcegos conservam também o requisito de mob hostil `Enemy`.
3. Fogo dos executores só é aplicado quando `hurt` aceita dano. Knockback já dependia disso.
4. Ignição nos golpes foi movida de `LivingHurtEvent` para `LivingDamageEvent`, com
   quantidade positiva; `applyIgnitionHit` também consulta a política de alvos.
5. Corte conserva destruição sem drops, alcance, ângulo, dano 48 e cooldown 60 s.
   A seleção de entidades foi unificada; não foi criado sistema de proteção de terrenos.

## 5. HUD de recargas sem persistir cooldowns

Arquivos: `StatusData.java`, `SyncHelper.java`, `ClientPacketHandler.java`,
`TickHandler.java`, `ReiatsuHud.java` e idiomas `pt_br.json`/`en_us.json`.

1. `StatusData.techniqueHud()` fornece snapshot separado com cinco recargas e flag
   de teste. `save()` não escreve esses campos: o contrato de NBT transitório permanece.
2. `SyncHelper.full/resources` anexam `techniqueHud` aos pacotes existentes, somente
   para o proprietário. Não muda os executores, schema 3 ou estrutura dos pacotes.
3. `ClientPacketHandler` aplica o snapshot após carregar os dados; contadores visuais
   são separados dos campos autoritativos. O servidor continua decidindo se pode usar.
4. `TickHandler` envia recursos a cada quatro ticks mesmo com reiatsu cheia, evitando
   recarga congelada no HUD por ausência de consumo. Snapshot não é previsão local.
5. `ReiatsuHud` mostra slots 1/2/3/4/X, segundos arredondados para cima ou `OK`.
   Modo sem cooldown mostra `TEST`. Slots correspondem às ações remapeáveis, não
   assumem que H/N/B/C são teclas fixas do usuário.
6. Protocolo permanece 2.3: campo NBT adicional nos pacotes existentes. Usar o JAR
   novo no servidor e cliente para receber este HUD e as superfícies atualizadas.

## 6. Validação e próximos testes

Comando de validação: `gradlew.bat --offline --no-daemon check build runGameTestServer`.
Regressões incluem geometria/UVs de um gume, snapshot sem persistência e transições
que disparam liberação. GameTests incluem alcance/altura/fogo rejeitado e Leque
Bankai respeitando aliado vanilla, além das habilidades e comandos anteriores.
Resultado final: build aprovado, 51 regressões e 16 GameTests passaram. Os 22 JSONs foram validados. JAR gerado: build/libs/bleachmod-0.2.0.jar.

Homologação visual necessária: katana nas duas mãos/GUI/chão, legibilidade das fissuras,
liberação nas entradas/descidas e aura sem excesso, muralha 18 × 2 × 8, HUD em GUI
pequena e multiplayer com dois lançadores. Não há medição formal de desempenho aqui.

## 7. Etapa seguinte: fogo ambiental, separada deste acabamento

Ainda não implementada. A ativação de Bankai não começa incêndios neste JAR.
Antes disso, definir raio, número de focos, frequência, duração e regras de terreno.
O serviço de `spirit_flame` deverá cuidar de proprietário/origem/expiração, sobreposição,
persistência, chunks descarregados, restart e remoção condicional que não sobrescreva
blocos posteriores. Emissões atuais não são esse sistema. Vínculo de Zanpakutō também
permanece escopo futuro, sem migração de save neste ciclo.

## Prompts finais das superfícies

Shikai:

```text
Game asset: one opaque square Minecraft pixel-art texture tile for the broad SIDE FACE of a Shikai flaming katana blade, NOT a picture of a sword. Fill the entire square edge to edge with stylized flowing yellow-orange-white flames on ember red heated steel. Leftmost 10 percent is the single bright cutting edge; rightmost 12 percent is a distinct dark non-cutting spine; center is energetic flame tongues flowing upward. Crisp pixel clusters, no blur, no text, no border, no perspective, no handle, no surrounding background, no checkerboard. Designed to map UVs across a long narrow 3D blade: detailed flames visible along its full length. Opaque material texture only, pixel art Minecraft style.
```

Bankai:

```text
Game asset: opaque square Minecraft pixel-art material texture for the broad SIDE FACE of a carbonized Bankai katana blade. NOT a picture of a sword. Fill entire square edge to edge. Charcoal black burned steel with layered scorched roughness and clearly visible thin branching fissures glowing concentrated orange-red-yellow molten fire, sparse white-hot intersections. Leftmost 10 percent is the ONLY bright cutting edge, dark rightmost 12 percent is the dull non-cutting spine. Cracks across center extend vertically and diagonally, contrasted black charred patches remain dominant, crisp chunky pixel clusters, no blur. Designed for full UV mapping on a long narrow 3D katana. No perspective, no handle, no text, no border, no background, no checkerboard. Consistent with a fiery Shikai but much darker and restrained concentrated internal heat.
```
