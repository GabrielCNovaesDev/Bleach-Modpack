> **Revisão vigente — 04/10/2026:** ciclo de acabamento autorizado após o kit anterior: katana de um gume/texturas completas, liberação/aura, Muralha 18 × 2 × 8, alvos unificados e HUD de recargas. Build, 51 regressões e 16 GameTests aprovados; visual desta revisão para homologação no cliente. [Implementação passo a passo e arquivos](13-polimento-katana-liberacao-muralha-2026-10-04.md). Fogo ambiental é etapa seguinte, ainda não implementada. O histórico abaixo preserva as entregas anteriores.

> **Estado vigente — 03/10/2026:** feature Ryūjin Jakka concluída por confirmação do usuário após `5a02b61`, na branch `Feature-Poderes-bankais`. Última validação de código: build, 49 regressões e 14 GameTests aprovados. [Histórico técnico passo a passo](12-historico-tecnico-passo-a-passo-ryujin.md) · [Contrato vigente](08-contrato-ryujin-circulo-tornado-morcegos.md). Esta revisão modifica somente `Docs/poderes`; registros antigos abaixo descrevem suas respectivas versões.


## Aceite final e mapa técnico

Entrega: `5a02b61`. Após recebê-la, o usuário informou que a feature está concluída. Isso encerra o ciclo; roteiros de conferência abaixo ficam para regressões futuras, sem implementação pendente neste fechamento.

1. `BleachCommands` registra modos por jogador; `mutateTest` sincroniza sem desligar ignição incidentalmente.
2. `StatusData` aplica cooldown zero nos setters; `ResourcesData` centraliza autorização/débito e dispensa drenos.
3. `FormModeHandler` usa custo central; `TickHandler` mantém formas gratuitas no saldo zero. Restaurar retoma regras normais sem alterar constantes/JSONs.
4. `RyujinTechniqueService` substitui áreas/enxames em recast de teste e concentra a Muralha.
5. Os quatro JSONs de Ryujin corrigem orientação e constroem a katana longa; PNGs preservados.
6. `MvpRegressionTest`/`RyujinGameTests` verificam estado, parser e comandos reais: 49 regressões/14 GameTests. Aceite geral não constitui medição formal multiplayer.

---

# Ryūjin Jakka — consolidação da feature e ajustes finais

Data: 03/10/2026. Branch exclusiva de entrega: `Feature-Poderes-bankais`.
Base desta revisão: `57966f8`. Minecraft 1.20.1 / Forge 47.4.10 / Java 17.

## Entregas da branch

1. `141205c`: conjunto inicial B/C, área servidor, invocação de cinco morcegos,
   cooldown do slot 3 e preservação de recargas nas transformações. Corte de Vapor
   consolidado com dano base 48, custo 45 e recarga 1.200 ticks, preservando alcance
   100, abertura 25° e destruição sem drops. Manual e contrato unificados.
2. `57966f8`: Círculo substituído por Muralha 16 × 2 × 15 por cinco segundos;
   Tornado ampliado para raio 6 e altura 10 com fluxo horário; morcegos corrigidos
   para deslocamento explícito com colisão e ataque a mobs hostis. Registros:
   [primeiro ciclo](09-relatorio-ciclo-2026-10-03.md) e [revisão](10-revisao-muralha-tornado-morcegos-2026-10-03.md).
3. Revisão atual: comandos contínuos de teste, correção da orientação e novo modelo
   de katana para as três formas, partículas mais concentradas na Muralha.

O usuário confirmou que a entrega `57966f8` funciona bem no jogo. Essa confirmação
vale para a entrega anterior; a orientação e o visual deste novo modelo foram entregues para
homologação no cliente; posteriormente o usuário encerrou a feature.

## Kit vigente em modo normal

| Tecla | Selada / Shikai | Bankai |
| --- | --- | --- |
| H | Ignição | Dash de chamas |
| N | Rajada curta | Leque de fogo |
| B | Muralha de Chamas | Tornado de Chamas |
| C | Cinco morcegos de chamas | Corte de Vapor Concentrado |

X mantém Flame Burst. [Contrato completo de dimensões, valores, alvos e ciclo de vida](08-contrato-ryujin-circulo-tornado-morcegos.md).
Muralha: custo 40, dano base 18 por alvo a cada 20 ticks, knockback 0,25, cooldown
400 ticks. Tornado: custo 40, dano 2 a cada 10 ticks, duração 200 e cooldown 300.
Morcegos: custo 25, dano compartilhado 2 a cada 20 ticks, duração 800 e cooldown 200.

## Comandos e restauração

Exigem operador nível 2 e personagem confirmado. Use nome ou `@s` para o alvo.

```text
/bleachdev cooldowns clear @s
/bleachdev cooldowns disable @s
/bleachdev cooldowns restore @s
/bleachdev reiatsu free @s
/bleachdev reiatsu restore @s
/bleachdev inspect @s
```

- `clear`: limpa recargas e efeitos temporários uma vez. Preserva forma, ignição,
  reiatsu, progressão e os modos de teste. Corrigido para não passar pela normalização
  de progressão que desligava ignição/carga incidentalmente.
- `disable`: limpa recargas/efeitos anteriores e impede novos cooldowns nos quatro
  slots e Flame Burst. Recast de área/enxame substitui a instância anterior, sem duplicar.
- `cooldowns restore`: desativa o override. O próximo cast recebe a duração definida
  pelo executor, incluindo a variante da forma atual. Não força espera em habilidades
  ainda não usadas, nem altera as constantes do código.
- `free`: dispensa saldo mínimo e débito de técnicas, ignição e transformações, e
  suprime dreno contínuo/reversão por saldo baixo das formas. Funciona com zero reiatsu.
  Não altera o saldo nem concede skills, formas ou domínio.
- `reiatsu restore`: volta ao débito e dreno normal a partir da próxima operação;
  respeita também os valores de formas carregados dos JSONs do mundo. Saldo permanece.
- `inspect` e respostas de comandos exibem `cooldowns=disabled/normal` e
  `reiatsuCosts=free/normal`.

Modos são individuais e transitórios, fora do NBT e dos pacotes de jogador. Reconectar,
reiniciar e renascer retornam ao normal. Mudança de dimensão conserva os modos.
Transformar não desliga o modo de teste. O limite de pacotes e demais requisitos
permanecem: sem recarga não significa executar infinitos pacotes no mesmo tick.

## Espada e partículas

O sprite antigo da Ryujin tinha cabo embaixo à esquerda, mas herdava uma rotação de
180° adicional adequada à textura da Asauchi. Isso mostrava o cabo voltado para fora
e a mão na lâmina. Seu pequeno desenho central de 32 × 32 também reduzia o comprimento.

Os modelos agora usam 14 elementos geométricos, com cabo/faixas, guarda, lâmina longa,
borda e ponta em segmentos. O corpo mede 28 unidades de modelo, sendo cerca de 20
para a lâmina; são unidades de modelo, não blocos. Rotação local diagonal e transformações
de mão preservam a convenção de espada vanilla, com pegada na região do cabo. As duas
mãos e perspectivas compartilham o mesmo modelo pai; GUI, chão e moldura têm escala própria.
O pai não usa `builtin/generated`, que substituiria a geometria pelo sprite antigo.
As texturas originais fornecem a paleta, sem alteração de PNGs: Selada metálica,
Shikai com borda vermelha e Bankai escura com borda rubra. Overrides de forma preservados.

Muralha: antes 128 amostras × 3 partículas a cada quatro ticks; agora 240 amostras × 5
a cada dois ticks (1.200 partículas por emissão). A distribuição vertical passou a
uma amostra por bloco. Volume, duração, dano, custo e recarga permanecem iguais.
Partículas não aplicam dano; o intervalo servidor continua independente do visual.
Conferir densidade com a configuração de partículas do cliente e vários lançadores.

## Validação desta revisão

Executado: `gradlew.bat --offline --no-daemon check build runGameTestServer`.

- Build aprovado e JAR `build/libs/bleachmod-0.2.0.jar` gerado.
- 49 regressões passaram, incluindo custos gratuitos/restaurados, todos os slots,
  preservação de override na troca de forma e ausência de persistência em NBT.
- Parser de modelos do próprio Minecraft aceitou as três geometrias; verificadas
  faces, limites de coordenadas, herança e rotações das duas mãos.
- 14 GameTests passaram. Os novos testes executam comandos reais pelo dispatcher:
  permissões, cast repetido com saldo zero, enxame sem duplicação, restauração de
  custo/cooldown, `clear` preservando ignição, ignição gratuita e Shikai/Bankai sem dreno.
- Os testes anteriores de geometria, movimento/ataque dos morcegos, duração da
  Muralha, variantes por forma e cooldown do Corte continuam passando.

Teste automatizado de modelo não confirma pegada visual no cliente. Roteiro preservado para regressões futuras após o aceite final:

1. Comparar com Asauchi em primeira/terceira pessoa, mão direita/esquerda e offhand;
   verificar cabo na mão, comprimento, inventário, item no chão e moldura.
2. Ativar Shikai e Bankai, observar paleta e orientação após cada troca.
3. Usar `disable` e `free`, repetir H/N/B/C/X, inspecionar modos e testar transformações.
4. Restaurar ambos, confirmar cobranças e recargas normais; conferir saldo zero e
   reversão por dreno. Reconectar e verificar modo normal.
5. Comparar a Muralha densa em diferentes ângulos e medir desempenho com dois clientes.

## Limites preservados

Sem alteração de protocolo 2.3, schema 3 ou dependência nova. Corte conserva seu
executor destrutivo. Não foram implementados `spirit_flame`, vínculo de Zanpakutō,
raça Hollow jogável, mentores/facções ou revisão geral de alvos de H/N/Corte.
O modelo não exige renderer customizado. Nenhuma alteração ou commit na `main`.
