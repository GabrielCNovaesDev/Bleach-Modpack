# Build e dependências (Dragon Mine Z)

## Atualização Bleach — 10/09/2026

Build 0.2.0 aprovado com 32 regressões. Quatro GameTests aprovados em run-gametest. Executar gradlew.bat build e gradlew.bat runGameTestServer; dependências locais permitem --offline. Relatórios anteriores com 19/26/28 testes são históricos.

[Relatório atual](../planejamento/relatorio-implementacao-mvp-2026-09-10.md). As seções de Dragon Mine Z abaixo são referência do original.


Documento de referência para o fork Bleach. Fonte analisada: clone em `dragonminez/` (versão 2.1.3).

## Plataforma

| Item | Valor |
|---|---|
| Minecraft | **1.20.1** (`minecraft_version_range=[1.20.1]`) |
| Forge | **47.4.10** (`forge_version_range=[47,)`) |
| Java | **17** (toolchain + `--release`) |
| Mappings | Parchment `2023.09.03-1.20.1` |
| Build | Gradle Kotlin DSL (`build.gradle.kts`) |
| Mod id | `dragonminez` |
| Pacote raiz | `com.dragonminez` |
| Licença declarada | GNU GPL v3.0 |

Arquivos: `dragonminez/gradle.properties`, `dragonminez/build.gradle.kts`, `dragonminez/src/main/resources/META-INF/mods.toml`.

## Dependências de runtime (obrigatórias)

| Biblioteca | Versão | Papel | Licença típica |
|---|---|---|---|
| Minecraft Forge | 1.20.1-47.4.10 | Loader | LGPL / Minecraft EULA (jogo) |
| GeckoLib | 4.8.3+ | Animações de entidade/jogador | MIT (ver `THIRD_PARTY_LICENSES`) |
| mclib | 20 | Dependência transitiva do GeckoLib | — |
| TerraBlender | 1.20.1-3.0.1.10 | Biomas/regiões custom | LGPL |
| Curios | 5.14.1+1.20.1 | Slots de acessório (scouter etc.) | LGPL / MIT conforme artefato |

O `mods.toml` marca GeckoLib, TerraBlender e Curios como `mandatory=true`.

## Dependências empacotadas (jarJar)

O jar de distribuição inclui:

- MariaDB JDBC 3.5.9+ — backend opcional de persistência
- HikariCP 7.1.0+ — pool de conexões

O jar “fino” recebe classifier `-slim`. Para o Bleach, **não é necessário** MariaDB no MVP: o original já persiste quests/evolução no NBT do jogador.

## Dependências opcionais / compile-only

| Biblioteca | Uso no original | Precisa no Bleach MVP? |
|---|---|---|
| Lombok 1.18.46 | Anotações em data classes | Conveniência, não obrigatório |
| JEI 15.20.0.128 | Plugin de receitas | Não (conteúdo DB) |
| Weapon Leveling / Apotheosis / Placebo | Compat de armas | Não |
| WorldEdit, Spark, Cyanide | Dev tools | Não |

## Incompatibilidades explícitas

O construtor de `DragonMineZ` recusa carregar se estes mods estiverem presentes:

- `legendarytooltips`
- `epicfight`
- `bettercombat`

Declaradas também em `mods.toml` com `type="incompatible"`. Motivo observado: conflito de rendering de tooltip e de pipeline de combate melee.

## Mixin e access transformer

- Plugin Mixin: SpongePowered 0.7.38 / processor 0.8.7
- Config: `src/main/resources/dragonminez.mixins.json`
- Refmap: `dragonminez.refmap.json`
- ~9 mixins common + ~33 client
- Access transformer: `src/main/resources/META-INF/accesstransformer.cfg`

Para o Bleach, mixins só devem ser reintroduzidos se um sistema de quest/evolução realmente precisar interceptar vanilla. A maior parte de quest/evolução **não depende de mixin** — vive em capability, packets e eventos Forge.

## Passos de build

```bash
# na pasta do projeto Forge gerado
./gradlew genIntellijRuns   # ou genEclipseRuns / genVSCodeRuns
./gradlew runClient
./gradlew build
```

Observações do original que **não** precisam ser copiadas no MVP:

- `org.gradle.offline=true` no `gradle.properties` (quebra o primeiro clone)
- PackSquash para otimizar resources (CI)
- Datagen obrigatório no ciclo de build (`runData` → `src/generated/resources`)
- jarJar de MariaDB/HikariCP

## Configurações Gradle relevantes para recriar

1. Java 17 toolchain.
2. ForgeGradle com Parchment.
3. `runs.client` / `runs.server` padrão do MDK 1.20.1.
4. Se houver GeckoLib (só se o Bleach for usar geo-models no MVP): dependência `AFTER` no `mods.toml`.
5. Channel de rede próprio (`ResourceLocation(modid, "network")`) com protocolo versionado — ver `06-rede-sincronizacao.md`.

## Recomendação para o fork Bleach

Reusar a **mesma linha de plataforma** (MC 1.20.1 / Forge 47.x / Java 17) para que a documentação de capabilities, packets e eventos continue válida. Mudar de versão implica reescrever APIs de capability e de rede.

Dependências mínimas sugeridas no MVP Bleach:

- Forge 1.20.1
- Nenhuma lib de animação no MVP (HUD + itens vanilla bastam)
- Sem TerraBlender / Curios / MariaDB até as fases futuras

## Bleach 0.3.0 — dimensão e datagen

TravelWorldGen usa DatapackBuiltinEntriesProvider/RegistrySetBuilder no evento GatherData do MOD bus para emitir DimensionType e LevelStem. `runData` gera dois JSONs versionados em src/generated/resources; build inclui esses arquivos no JAR. Gerador vanilla flat sem camadas/estruturas preserva chunks importados e produz vazio fora deles. Java 17/Forge 47.4.10 mantidos; nenhuma dependência nova.
