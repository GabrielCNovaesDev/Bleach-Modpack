# Manual de inicialização — Bleach Mod

Atualizado em 10/09/2026. Cobre o caminho do clone até o cliente de desenvolvimento com o mod carregado.

Ambiente alvo: **Minecraft 1.20.1**, **Forge 47.4.10**, **Java 17**, **Gradle 8.8** (via wrapper). Não instale Gradle à parte. Não use Java 21 ou 23 para este projeto.

Depois que o jogo abrir, o [manual do jogador](../jogador/manual-do-jogador.md) descreve controles, quests e progressão.

## 1. O que você precisa

| Item | Detalhe |
|---|---|
| Git | Para clonar o repositório |
| JDK **17** (64-bit) | Temurin, Microsoft OpenJDK ou equivalente. O `java` do PATH deve ser 17 |
| Internet | A primeira execução baixa o Gradle 8.8, o Forge e o Minecraft (~vários GB no cache)|
| RAM | O Gradle está configurado com `-Xmx3G` |

Não é necessário instalar Minecraft nem Forge pelo launcher da Mojang. O `runClient` baixa e monta o ambiente de userdev sozinho.

Repositório: `https://github.com/GabrielCNovaesDev/Bleach-Modpack.git`  
Branch padrão: `main`

## 2. Instalar o Java 17

Confira se já existe um JDK 17:

```powershell
java -version
```

A primeira linha deve parecer com `openjdk version "17.x.x"`. Se aparecer 21, 23 ou `java` não for reconhecido, instale o 17.

Opções comuns:

- [Eclipse Temurin 17](https://adoptium.net/temurin/releases/?version=17) (Windows x64, JDK)
- [Microsoft OpenJDK 17](https://learn.microsoft.com/java/openjdk/download)

Instale o **JDK**, não só o JRE. Anote a pasta de instalação; no Windows costuma ser algo como:

```
C:\Users\<você>\.jdks\jdk-17.0.xx
C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot
C:\Program Files\Microsoft\jdk-17.x.x
```

Dentro dessa pasta deve existir `bin\java.exe` (Windows) ou `bin/java` (macOS/Linux).

## 3. Configurar JAVA_HOME

O wrapper do Gradle (`gradlew`) **exige** `JAVA_HOME` apontando para a pasta do JDK 17, não para `bin`.

### Windows (PowerShell)

Substitua o caminho pelo JDK 17 real desta máquina:

```powershell
$jdk = "C:\Users\<você>\.jdks\jdk-17.0.xx"
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk, "User")
$bin = Join-Path $jdk "bin"
$userPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$bin*") {
    [System.Environment]::SetEnvironmentVariable("Path", "$bin;$userPath", "User")
}
```

Feche o terminal (e o Cursor/IDE, se já estava aberto) e abra de novo. Variável de ambiente nova só entra em sessão nova.

Confirme:

```powershell
echo $env:JAVA_HOME
java -version
```

`JAVA_HOME` deve ser a pasta do JDK. `java -version` deve mostrar 17.

### macOS / Linux

```bash
# exemplo Temurin
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"   # macOS
# ou: export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

Para persistir, coloque as duas linhas no `~/.zshrc` ou `~/.bashrc` e abra um terminal novo.

## 4. Clonar o repositório

```bash
git clone https://github.com/GabrielCNovaesDev/Bleach-Modpack.git
cd Bleach-Modpack
```

Se o repositório já estiver na máquina, entre na pasta do projeto e atualize:

```bash
git pull origin main
```

Confirme que existem `gradlew`, `gradlew.bat`, `build.gradle` e `src/`.

## 5. Primeira verificação do Gradle

Na raiz do projeto:

**Windows (PowerShell):**

```powershell
./gradlew --version
```

**macOS / Linux:**

```bash
chmod +x gradlew
./gradlew --version
```

A saída deve incluir `Gradle 8.8` e `JVM: 17.x`. Na primeira vez o wrapper baixa `gradle-8.8-bin.zip`; isso é esperado.

Se aparecer `JAVA_HOME is not set and no 'java' command could be found in your PATH`, volte ao passo 3 e **abra um terminal novo**.

## 6. Rodar o cliente com o mod

Na raiz do projeto:

```powershell
./gradlew runClient
```

A primeira execução pode demorar vários minutos: ForgeGradle baixa Minecraft 1.20.1, mappings e o Forge 47.4.10, depois compila o mod. Execuções seguintes são bem mais rápidas.

Quando o launcher de desenvolvimento abrir:

1. Crie ou entre num mundo (qualquer modo serve para testar o MVP).
2. Na primeira entrada, confirme a raça **Shinigami**.
3. Você deve receber o Asauchi e ver a barra de reiatsu no HUD.

O mundo de desenvolvimento fica em `run/` (ignorado pelo Git). Apagar essa pasta reseta saves de teste; não mexe no código.

Para um servidor dedicado de desenvolvimento, em outro terminal:

```powershell
./gradlew runServer
```

## 7. Comandos úteis

| Comando | Função |
|---|---|
| `./gradlew runClient` | Sobe o Minecraft com o mod |
| `./gradlew runServer` | Sobe o servidor de userdev |
| `./gradlew build` | Gera o jar em `build/libs/` |
| `./gradlew genIntellijRuns` | Cria as run configs do IntelliJ (`runClient` / `runServer`) |
| `./gradlew --stop` | Encerra daemons, se houver algum preso |

No IntelliJ IDEA / Cursor com plugin Gradle: abra a pasta do repositório como projeto Gradle, selecione JDK 17 no Project SDK e, se quiser o botão de Run da IDE, rode `./gradlew genIntellijRuns` uma vez.

## 8. Problemas comuns

**`JAVA_HOME is not set`**  
A sessão não viu a variável. Confirme `echo $env:JAVA_HOME` (PowerShell) ou `echo $JAVA_HOME` (bash). Feche o terminal e o editor e abra de novo.

**`java -version` mostra 21 ou 23**  
O PATH está pegando outro JDK. `JAVA_HOME` e o `bin` do 17 precisam vir **antes** dos outros Java no PATH.

**`JAVA_HOME is set to an invalid directory`**  
A variável aponta para `bin` ou para uma pasta que não contém `bin/java`. Aponte para a raiz do JDK.

**Download falha / `Connection reset` na primeira run**  
O wrapper baixa `gradle-8.8-bin.zip` (~132 MB) de `services.gradle.org`. Essa conexão às vezes cai no meio; o timeout do wrapper neste projeto é 120 s. Rode `./gradlew --version` de novo. Se o zip ficar incompleto em `%USERPROFILE%\.gradle\wrapper\dists\`, apague essa pasta `gradle-8.8-bin` e tente outra vez. Com o Gradle já no cache, `runClient` ainda precisa de rede para o Forge/Minecraft.

**A janela do Minecraft não abre na hora**  
A primeira `./gradlew runClient` ainda não é o jogo: o Forge baixa e processa o Minecraft (`downloadClient`, `listLibraries`, `compileJava`). A janela só aparece quando a tarefa chegar em `:runClient`. Não cancele no meio (Ctrl+C / “finalizar o arquivo em lotes”). Pode levar 10–20 minutos na primeira vez; as seguintes são bem mais rápidas.

**O jogo abre, mas o mod não aparece**  
Confirme que o comando foi executado **na raiz** do repositório (onde está o `build.gradle`). O mod id é `bleachmod`.

**Pouca memória / GC excessivo**  
O projeto já pede 3 GB ao Gradle (`org.gradle.jvmargs=-Xmx3G` em `gradle.properties`). Feche outros clientes Minecraft e IDEs pesadas se a máquina tiver pouca RAM.

## 9. Depois que o cliente abrir

- Controles, missões e progressão: [manual do jogador](../jogador/manual-do-jogador.md)
- Visão do repositório e escopo: [README da documentação](../README.md)
- Plano de correções do MVP: [plano de implementação](../planejamento/plano-implementacao-mvp.md)
