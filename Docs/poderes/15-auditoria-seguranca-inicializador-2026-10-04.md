> **Estado vigente — ciclo 19, 06/10/2026:** identidade e maestria separadas, protótipo Hyōrinmaru em quatro slots, N da Ryujin com onda de quatro blocos na selada/Shikai/Bankai, aura Bankai de fumaça e retirada do X. [Implementação, arquivos, números, migração e homologação](19-hyorinmaru-identidade-onda-fogo-2026-10-06.md). Os estados e valores anteriores abaixo são históricos; história e balanceamento final continuam em desenvolvimento.

# Auditoria de segurança do Iniciar-Teste

Data: 04/10/2026. Branch: `Feature-Poderes-bankais`. Base: `987dc96`.
O usuário homologou a Ryujin e encerrou a feature de poderes. Este ciclo revisa
exclusivamente o inicializador e seus testes; não altera gameplay, assets ou saves.

## Resultado e escopo

Revisados `Iniciar-Teste.bat` e `tools/iniciar-teste.ps1`. A limpeza continua restrita
a `build`, `.test-launcher-cache`, `run/logs` e `run/crash-reports` do próprio projeto.
Mundos, opções, configurações, screenshots, fontes, Git e downloads compartilhados
não entram nessa lista. Nenhum teste excluiu ou alterou os arquivos de controle externos.

O primeiro teste da versão anterior recusou um junction interno e preservou o arquivo
externo. A revisão encontrou melhorias necessárias na validação e no tratamento de
falhas, e um problema reproduzido com nomes de pastas contendo colchetes.

## Correções, lógica e arquivos

1. **Simulação consistente:** anteriormente `DryRun` retornava antes da inspeção de
   links internos. Agora usa `Assert-TestOutput`, a mesma validação da exclusão real,
   e só imprime os caminhos depois de verificar todos. Não cria lock, roda wrapper
   ou altera arquivos.
2. **Inspeção sem seguir links:** substituída a enumeração recursiva por uma pilha
   de diretórios. Cada filho é inspecionado antes de entrar nele. Qualquer reparse
   point é recusado; isso inclui junctions, ciclos e links quebrados detectáveis
   pelo provedor. `Get-TestPathItem` usa caminho literal e tolera apenas inexistência;
   erros de acesso continuam interrompendo a operação.
3. **Validação antes de apagar:** todos os destinos são verificados antes da primeira
   exclusão, e novamente imediatamente antes de cada remoção. Um arquivo comum
   com nome `build` é recusado, em vez de ser tratado como diretório gerado.
   Validação que falha antes da limpeza não remove o cache em `finally`.
4. **Lock protegido:** `.test-launcher.lock` deve ser um arquivo comum, sem links.
   A abertura exclusiva impede outro inicializador de limpar uma build em uso.
   Links ou diretórios com esse nome são recusados antes de abrir o handle.
5. **Cache bloqueado não significa sucesso:** falha na limpeza final agora define
   código de saída 1 e informa que o cache não foi removido. O BAT conserva esse
   código. Uma execução posterior limpa os resíduos quando o bloqueio termina.
6. **Caminhos literais:** `Push-Location -LiteralPath` corrige o erro reproduzido com
   colchetes. Espaços, `&` e `!` também integram as fixtures. O BAT desativa expansão
   atrasada para preservar `!` ao expandir o caminho da própria pasta.

Somente os dois arquivos do inicializador mudaram em produção. Criado
`tools/test-iniciar-teste.ps1` para repetir a verificação automaticamente.

## Testes isolados e reproduzíveis

Comando:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File tools/test-iniciar-teste.ps1 -ReportPath build/launcher-security-report.json
```

Resultado final: **18 cenários aprovados, zero falhas, todas as fixtures temporárias removidas**.

Executado com Windows PowerShell 5.1.26100.9444. As fixtures usam uma pasta exclusiva em TEMP,
com nomes contendo caracteres especiais. O wrapper simulado registra chamadas e
cria arquivos de teste; **não compila o mod, baixa dependências ou abre Minecraft**.
O caso de concorrência mantém um inicializador real executando esse wrapper simulado.
Um helper temporário mantém um arquivo aberto para simular falha de limpeza final.
Todos os helpers terminam, e a rotina final remove as fixtures sem seguir seus links.

Cobertura:

- DryRun sem mutação; entrada BAT sem entrada interativa; BuildOnly/Offline.
- Limpeza restrita, remoção de cache antigo, arquivos protegidos byte a byte.
- Ordem build → cliente, duas execuções sequenciais e cache removido nas duas.
- Falha de build impede cliente; falha de cliente retorna erro e remove cache.
- Junctions no build, cache e `run`, link interno, ciclo, link quebrado e lock.
- Preflight inválido não limpa parcialmente o build antes de recusar o link.
- Arquivo comum no lugar de build; projeto incompleto sem wrapper.
- Duas execuções simultâneas: segunda recusada, build ativa preservada.
- Arquivo de saída aberto/bloqueado: interrupção sem chamar wrapper.
- Cache bloqueado ao terminar: erro visível; próxima execução recupera a limpeza.

O relatório JSON gerado em `build` registra o resultado de cada cenário. Essa pasta
é um artefato de verificação, ignorado no Git e substituído no próximo teste/build.
As verificações do mod homologadas no ciclo anterior permanecem: 51 regressões e
18 GameTests. Não foram repetidas porque este ciclo modifica somente scripts/documentos.

## Resíduos esperados e limites

- **Saída normal:** cache de projeto temporário removido e lock liberado. Permanece
  um único arquivo `.test-launcher.lock` de zero bytes, para coordenar execuções.
  Não é processo, não mantém memória e não acumula dados.
- **Artefatos úteis:** build/JAR/relatórios e logs da sessão permanecem para uso e
  diagnóstico, e são limpos antes da próxima execução. Saves/configurações são dados
  do jogador e permanecem deliberadamente.
- **Downloads compartilhados:** Gradle/Forge/JDK podem ocupar disco e são reutilizados.
  O inicializador não apaga caches globais nem tenta limpar outras aplicações.
- **Encerramento forçado/quebra de energia:** `finally` não pode ser garantido quando
  o processo é morto. Cache temporário pode permanecer até a próxima execução;
  o teste de cache antigo/recuperação valida a limpeza posterior. Não afirmamos
  que o disco ficará sem qualquer artefato imediatamente após um encerramento forçado.
- **Processos externos:** lock coordena este inicializador, não builds da IDE ou um
  Minecraft iniciado manualmente. Feche esses processos antes de iniciar o teste.
  Arquivos bloqueados causam erro; não são encerrados à força pelo script.
- **Políticas do Windows:** `ExecutionPolicy Bypass` vale apenas para o processo
  iniciado. Não altera política persistente, Registro, serviços, inicialização do
  Windows ou instalação de Java. Não exige execução como administrador.
- **Modelo de segurança:** valida acidentes e configurações locais comuns; não é
  sandbox para um repositório malicioso. O Gradle executa o código do projeto.
  Também não oferece isolamento contra outro processo malicioso que troque diretórios
  entre a validação e a exclusão. Nenhum cenário desse tipo foi certificado.

Conclusão operacional: as proteções testadas preservam os dados de controle e tornam
falhas de limpeza visíveis. A feature Ryujin permanece homologada e encerrada.
