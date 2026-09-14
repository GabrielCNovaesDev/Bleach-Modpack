# Soul Society — instalação do mapa e viagens

Implementação 0.3.0, protocolo 2.3, schema de jogador 4. A tecla **H** abre o Senkaimon. Sem mapa instalado, o mod inicia normalmente e informa que a entrada está indisponível.

## Colocar o save

Feche o mundo e o servidor. Copie o **conteúdo da pasta de um save Java 1.20.1** para `bleachmod-maps/soul_society/`, dentro da pasta da instância do Minecraft ou do servidor. A pasta correta contém diretamente `level.dat` e `region`, sem outra pasta de save no meio. Não é um ZIP nem um schematic.

No ambiente de desenvolvimento deste repositório:

```text
Bleach-Modpack/
  run/
    bleachmod-maps/
      soul_society/
        level.dat
        region/
          r.0.0.mca
          ...
        entities/       (se existir)
        poi/            (se existir)
```

Abra seu mundo Bleach. A importação acontece automaticamente **antes de carregar as dimensões**, uma vez por mundo. No dedicado, só o servidor precisa do mapa; clientes recebem os chunks normalmente. Todos precisam do mod 0.3.0.

O destino é `<save Bleach>/dimensions/bleachmod/soul_society/`. A origem continua disponível para instalar em outros mundos. Inventários e personagens do save de origem não são importados. O `level.dat` do mundo Bleach não é substituído.

O save precisa ter sido salvo em Minecraft **Java 1.20.1 / DataVersion 3465**, com os mods de conteúdo necessários presentes na instância Bleach. Este instalador não converte Bedrock, versões novas, `.schem` ou `.litematic`, nem instala automaticamente mods/datapacks. O mapa real ainda precisa ser conferido quanto a entidades, comandos e dependências; uma cópia de arquivos não consegue validar toda a lógica de um mapa de aventura.

## Ponto de chegada

O spawn do mundo de origem (`SpawnX`, `SpawnY`, `SpawnZ`, `SpawnAngle` em `level.dat`) vira a chegada inicial. O servidor procura piso seguro num raio de quatro blocos, até três blocos acima/abaixo. Não cria plataforma nem remove construções para abrir espaço.

Se o mapa tiver um spawn ruim, ajuste-o no mundo original antes de importar ou configure `arrivalOverride` no mundo Bleach:

```json
{
  "enabled": true,
  "entryCost": 20.0,
  "cooldownTicks": 100,
  "arrivalOverride": {
    "dimension": "bleachmod:soul_society",
    "x": 100.5,
    "y": 70.0,
    "z": -50.5,
    "yaw": 90.0,
    "pitch": 0.0
  }
}
```

Arquivo: `<save Bleach>/bleachmod/travel/settings.json`. Ele é criado automaticamente. Omita `arrivalOverride` para usar o spawn importado. `/bleachtravel reload` aplica configurações validadas; em caso de erro, a configuração anterior continua ativa. O reload não importa mapas nem registra novas dimensões.

## Regras de viagem

- H abre a tela; o botão só habilita após resposta do servidor.
- Shinigami com personagem criado pode entrar a partir do Overworld. Não exige Shikai/Bankai.
- Entrada: 20 reiatsu por padrão; retorno: gratuito. Cooldown padrão: cinco segundos de servidor em execução.
- O servidor salva a posição de partida e tenta voltar a ela. Se ela estiver insegura, tenta o spawn atual do Overworld. Se nenhum local for seguro, nega a viagem sem custo.
- Não é permitido viajar morto, dormindo, como spectator, montado ou com passageiros. Creative segue as regras normais.
- Energia é reservada após validar o pouso; cancelamento do teleporte devolve a reserva. A cobrança efetiva acontece apenas se o teleporte for confirmado.
- Pedidos duplicados e pendentes não acumulam cobranças. Há uma viagem pendente por jogador e até oito no servidor; preparação tem timeout de dez segundos.
- Quests, atributos e inventário continuam no mesmo personagem. Carga de transformação e velocidade de queda são limpas; a forma ativa não é trocada pela viagem. Morte continua seguindo o reset existente.
- Camas não alteram respawn para a Soul Society. Portais vanilla não permitem atravessá-la; a passagem usa o Senkaimon. Âncoras de respawn não funcionam nessa dimensão e seguem comportamento explosivo vanilla: não usá-las no mapa.
- Fora dos chunks importados, o gerador produz vazio. Não há proteção de construção, limite próprio de exploração nem portal físico nesta versão. Respeitar os limites construídos do mapa.

## Administração e falhas

Comandos exigem operador nível 2:

| Comando | Função |
|---|---|
| `/bleachtravel inspect` | Mostra presença/hash do mapa, habilitação, revisão e viagens pendentes |
| `/bleachtravel reload` | Recarrega configurações de viagem |
| `/bleachtravel rescue <jogador>` | Leva ao spawn seguro do Overworld sem custo/requisito de raça; ainda exige jogador vivo, sem montaria e pouso válido |

Não usar o resgate como confirmação de conclusão: o comando agenda a preparação, e o jogador recebe o resultado. Se aparecer “Nenhum local seguro”, preparar um piso seguro no spawn do Overworld.

`bleach-map.json` na dimensão marca a instalação concluída e guarda spawn, contagem, tamanho e SHA-256 do conteúdo copiado. Não editar/remover esse arquivo para forçar atualização. A presença do marcador impede nova cópia, preservando as mudanças feitas jogando. O hash registra a importação; não é recalculado contra chunks modificados a cada início.

A importação rejeita save de versão errada, origem aberta, links de filesystem, regiões truncadas e destino já ocupado sem marcador. Limites: 32 GiB e 100 mil arquivos. Erro de instalação interrompe a abertura com detalhes no log, sem publicar uma cópia incompleta. Os dados são preparados numa pasta temporária; o destino só é publicado no final. Não misturar manualmente regiões sobre uma dimensão em uso.

Você pode jogar antes de fornecer o mapa: arquivos `.dat` de metadados de uma dimensão ainda sem chunks são preservados durante a importação. A pasta `.soul-society-before-import`, ao lado da dimensão, guarda a cópia desses metadados anteriores; uma interrupção antes da publicação restaura essa pasta na próxima tentativa.

Se o destino já tem chunks de testes e nenhum marcador, faça backup e use um novo save Bleach para importar. O instalador não apaga chunks existentes automaticamente.

## Atualização e rollback

Faça backup do save inteiro antes da primeira importação e antes de atualizar mapa/mod. Atualizações de JAR não recopiariam o mapa. Para substituir construções, planejar uma atualização offline separada, preservando o que foi construído jogando.

Para suspender entradas, use `enabled: false` e reload. O retorno/resgate continua disponível. Antes de remover o mod/dimensão, evacue jogadores online e resolva os personagens offline que estejam nela. Não reabra save schema 4 com código antigo sem backup compatível.

## Verificação de desenvolvimento

Java 17, comandos na raiz do projeto:

```powershell
./gradlew.bat runData --offline
./gradlew.bat build --offline
./gradlew.bat runGameTestServer --offline
```

Datagen: `TravelWorldGen`; outputs versionados em `src/generated/resources`. A dimensão usa gerador plano vazio, biome vanilla e tipo próprio; não precisa de TerraBlender, NoiseSettings próprios ou chunks do mapa dentro do JAR. GameTests usam fixtures descartáveis e a dimensão do ambiente `run-gametest`; não criam plataformas no mapa normal do jogador.

Para testar em outra pasta sem alterar o ambiente padrão: `./gradlew.bat runGameTestServer -PgameTestDir=build/travel-test --offline`. O cache interno do datagen é excluído do JAR.
