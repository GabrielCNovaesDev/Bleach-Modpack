# Manual do jogador — Bleach Mod (MVP Shinigami)

Atualizado em 03/10/2026. Minecraft 1.20.1 / Forge 47.4.10 / Java 17.

Este manual descreve o código atual. A verificação visual em jogo e com dois clientes ainda está pendente; consulte o [plano de implementação](../planejamento/plano-implementacao-mvp.md) para o estado de cada entrega. Toda mudança de lógica exige atualizar este manual e os demais `.md` afetados em `Docs`.

## 1. Começar

Confirme Shinigami na primeira entrada; aguarde a resposta do servidor. Você recebe Asauchi e 100 de reiatsu. Hollow e Hollow Boss já existem como mobs de teste; isso não libera a raça Hollow. Quests em mundos antigos podem manter alvos anteriores. Consulte os objetivos reais no diário.

O Asauchi pode ser fabricado em uma bancada:

```
 I
PIP
 S
```

I = lingote de ferro, P = papel, S = graveto. A receita usa dois ferros, dois papéis e um graveto. A arma continua tendo durabilidade.

## 2. Controles

Todos podem ser remapeados em Opções → Controles → Bleach.

| Tecla | Ação |
| --- | --- |
| J | Diário: iniciar, receber recompensas, rastrear/desmarcar |
| K | Personagem: pontos, categorias, compra de Zanpakutō e mastery |
| Z | Abrir seletor radial; clique numa forma para selecionar, Esc para cancelar |
| G | Ciclar formas selecionáveis |
| R, segurar | Carregar e tentar transformar na forma selecionada |
| Shift+R | Transformação instantânea, se mastery da forma atingir o limiar |
| V | Retornar um estágio |
| X | Executar Flame Burst / Explosão de chamas |
| H | F1: Ignição em Selada/Shikai; Dash em Bankai |
| N | F2: Rajada curta em Selada/Shikai; Leque em Bankai |
| B | F3: Muralha de Chamas em Selada/Shikai; Tornado em Bankai |
| C | F4: Morcegos em Selada/Shikai; Corte de Vapor em Bankai |

A técnica Flame Burst não exige Bankai ativo. Ela custa 20 de reiatsu, possui cooldown de 15 segundos e causa dano em uma área curta ao redor do jogador. O dano base provisório é 6 pontos. A tecla X é remapeável.

Selecionar uma forma não a ativa. O HUD distingue forma ativa e alvo. A técnica piloto ainda não possui animação óssea nem slot no radial.

### Kit Ryūjin Jakka — contrato atual

Todas as técnicas do kit exigem Ryūjin Jakka na mão principal. Asauchi não autoriza o kit.

| Técnica | Custo de reiatsu | Dano base | Recarga |
| --- | ---: | --- | --- |
| Ignição (H base) | 0,05/tick | Bônus +15% Zanjutsu e fogo nos golpes | Toggle |
| Dash (H Bankai) | 20 | 4 por alvo, uma vez por cast | 5 s |
| Rajada (N base) | 15 | 4 inicial; 1/10 ticks no solo | 4 s |
| Leque (N Bankai) | 25 | 8 inicial; 1/10 ticks no solo | 4 s |
| Muralha (B base) | 40 | 18 por contato, no máximo a cada 20 ticks | 20 s |
| Tornado (B Bankai) | 40 | 2/10 ticks por alvo | 15 s |
| Morcegos (C base) | 25 | 2/20 ticks por alvo, compartilhado pelo enxame | 10 s + enxame encerrado |
| Corte de Vapor (C Bankai) | 45 | 48 | 60 s |

**Muralha:** comprimento 16, largura 2, altura 15. Começa 1 bloco à frente do jogador, na direção horizontal da mira, fica fixa por 5 s e aplica knockback leve (0,25) em mobs atingidos.
**Tornado:** raio 6, altura 10, segue o jogador durante 10 s. Duas espirais horárias vistas de cima, chamas FLAME mais densas e com velocidade tangencial para criar sensação de vento.
**Morcegos:** 5 entidades vanilla com 4 de vida, seguem com deslocamento controlado pelo servidor e atacam apenas mobs hostis; desaparecem após 40 s.
O enxame bloqueia outra invocação enquanto algum morcego válido estiver vivo. Espaço bloqueado
recusa o cast sem cobrar recursos. Muralha, Tornado e Morcegos não alteram blocos.

As novas áreas exigem linha de visão, excluem o lançador e aliados da mesma equipe e respeitam PvP.
Morcegos não atacam jogadores nem NPCs de quest. A troca de forma cancela efeitos sem zerar
cooldowns; perder a arma, morrer, desconectar ou mudar de dimensão remove áreas/enxames.
Cooldowns continuam transitórios: morte, logout e dimensão limpam recargas segundo o MVP.

**Corte:** mantém 100 blocos, abertura total 25°, 15 blocos abaixo e 20 acima da altura corporal,
direção exata da visão, vapor e execução instantânea. Atravessa paredes e destrói blocos comuns
sem drops; preserva bedrock, command blocks e blocos de dureza negativa. Usa dano mágico indireto
para ignorar armadura vanilla. O filtro atual também aplica Resistência Bleach; verificar no teste
real. Seu dano base passou para 48 e a recarga para 1.200 ticks. A regra antiga de aliados do Corte
permanece; os filtros das novas áreas não foram aplicados retroativamente ao executor existente.

Rajada: 8 blocos / 60°; Leque: 14 blocos / 90°, chamas azuis SOUL_FIRE_FLAME.
Os danos base das técnicas com playerAttack passam pelos bônus de forma/Zanjutsu/Ignição e
pela mitigação existente; não representam dano final garantido. Valores das três técnicas novas
são protótipos para homologação. Detalhes e roteiro: [contrato atual](../poderes/08-contrato-ryujin-circulo-tornado-morcegos.md).

## 3. Diário e interface

- Lista paginada de cinco missões; < e > mudam a página.

- Marcadores: > em andamento, + concluída, ! falhada.

- Detalhes à direita incluem alvo real, progresso, recompensas e aviso de pré-requisitos.

- Use a roda do mouse para rolar detalhes longos.

- Iniciar, Receber e Rastrear se atualizam com o estado recebido.

- Rastrear alterna para Desmarcar quando a missão já é a rastreada.

- Personagem abre a tela de compras; as compras deixaram o diário.

- Missões rastreadas desaparecem ao concluir/falhar. O painel resume até oito linhas; o diário contém os detalhes completos.

- Notificações são enfileiradas com limite para evitar acúmulo.

- HUD de recursos usa um painel compacto e responsivo no canto superior esquerdo, com três barras empilhadas: **Vida** (substitui os corações vanilla), **Reiatsu** e **Transformação** (progresso da carga do R). Reiatsu numérica, alvo/estágio e Spiritual Points aparecem no canto inferior esquerdo, próximos à hotbar. Os corações vanilla somem quando o personagem é criado; fome, hotbar, XP e vignette continuam do Minecraft.

## 4. Progressão em mundos novos

As missões despertam as formas. Pontos compram a skill e aprimoramentos. Mastery cresce com uso.

| Requisito | Shikai | Bankai |
| --- | --- | --- |
| Descoberta | Receber transformação da missão 2 | Receber transformação da missão 3 |
| Zanpakutō | Nível 1, custa 200 pontos | Nível 2, custa mais 500 pontos |
| Domínio prévio | Sem exigência adicional | Pelo menos 25 em Shikai |

As recompensas padrão novas começam com mastery 0, sem baixar valores já conquistados. Os custos são cobrados na tela K. Não é possível comprar o próximo nível antes de despertar sua forma.

Mastery padrão vai de 0 a 100:

- +1 a cada cinco segundos enquanto a forma não selada está ativa.

- 40 permite Shift+R naquela forma.

- 50 permite selecioná-la fora do próximo degrau imediato.

- A transição é verificada novamente ao transformar, mesmo se o alvo tiver sido selecionado antes de descer.

A seleção pode permanecer visível mesmo quando uma exigência ainda falta; use a tela K para conferir a progressão e G/Z para selecionar uma opção válida.

## 5. Categorias de pontos

As categorias não têm mais limite de nível. O próximo nível custa `100 × (nível atual + 1)` pontos; por segurança numérica, o preço individual deixa de crescer ao atingir 1.000.000. Não há respec implementado.

| Categoria | Efeito por nível |
| --- | --- |
| Zanjutsu | +10% do dano físico com Zanpakutō selada ou liberada |
| Hakuda | +10% do dano físico quando a mão principal está vazia |
| Vitalidade | +2 pontos de vida máxima (um coração) |
| Resistência | Reduz golpes físicos diretos pela fórmula `dano ÷ (1 + 0,05 × nível)` |
| Kidou | +10% no dano de ataques de feitiço; a fórmula está pronta, mas ainda não existem ataques Kidou |
| Reserva | +20 de reiatsu máxima |
| Controle | Reduz o consumo contínuo pela fórmula `drain base ÷ (1 + 0,10 × nível)` |

Aumentar Reserva preserva a energia atual se ela estiver abaixo do máximo; se estiver cheia, também preenche o novo máximo. Vitalidade aumenta o máximo, mas não reduz o dano recebido; esse é o papel de Resistência. Controle tem retorno decrescente e nunca converte drain em regeneração.

O BP (Battle Power) é informativo e aparece na tela K. A fórmula atual é `(soma dos níveis de Zanjutsu, Hakuda, Vitalidade, Resistência, Kidou, Reserva e Controle) × reiatsu máxima ÷ 10`. BP não concede bônus por si só.

## 6. Reiatsu, combate e técnica piloto

Valores base das formas continuam configuráveis nos JSONs do mundo:

| Situação | Valor base |
| --- | --- |
| Selada | +0,25 reiatsu/tick, cerca de 5/s |
| Shikai | -0,08/tick, cerca de 1,6/s |
| Bankai | -0,16/tick, cerca de 3,2/s |
| Reiatsu chega a 5% do máximo | Reversão automática à selada |

Controle afeta apenas drain contínuo: `drain efetivo = drain base ÷ (1 + 0,10 × nível de Controle)`.

Custo de transformação carregada = máximo de reiatsu × 10% × drain base; instantânea = drain base × 4. Com máximo 100: Shikai custa 0,8 carregado/0,32 instantâneo; Bankai custa 1,6/0,64.

A técnica piloto Flame Burst funciona em qualquer forma ativa, inclusive Selada. Ao pressionar X, o servidor valida a ação, consome 20 de reiatsu, inicia um cooldown de 15 segundos, emite partículas de fogo e som de Blaze e causa 6 pontos de dano provisório às entidades vivas em um raio de 3 blocos. O cooldown não é salvo em NBT.

Bônus de combate se aplica ao golpe direto de jogador com Asauchi ou Ryūjin Jakka na mão principal:

- Shikai: +20%.

- Bankai: +50%.

- Zanjutsu: +10% por nível, somado ao percentual da forma.

Exemplo: Bankai com Zanjutsu 2 multiplica o dano original por 1,7. Hakuda usa a mesma progressão de 10% quando o golpe é desarmado; o código atual também soma o bônus de forma. O dano original mantém cooldown, crítico e encantamentos; os percentuais entram no evento de dano antes das etapas posteriores de mitigação. Não há bônus persistente empilhado ao alternar formas.

Morte retorna à selada e respawn recupera reiatsu. Carga é cancelada ao abrir telas, mudar alvo, morrer, desconectar ou trocar de dimensão. Falhas de transformação não devem ficar repetindo a tentativa por tick.

## 7. Missões

| Missão | Objetivos | Novas recompensas padrão |
| --- | --- | --- |
| Caça aos Hollows | 6 Hollows | 200 pontos |
| Nomeie sua lâmina | Ter 8 carnes podres e matar 3 esqueletos, em paralelo | 400 pontos e despertar Shikai |
| Limiar do Bankai | 8 zumbis | 600 pontos e despertar Bankai |
| Treino básico | 10 zumbis | 150 pontos; repetível após receber tudo |

Missões da saga seguem ordem. Receber é uma ação explícita: completar não deposita automaticamente os prêmios. Missões com objetivo KILL falham se o jogador morrer, mesmo que a etapa KILL já tenha terminado.

ITEM é posse, sem consumo: o inventário é conferido periodicamente e revalidado antes da conclusão. Uma morte não conta para duas etapas sequenciais da mesma quest; pode contar para quests distintas ou objetivos paralelos.

Rota: concluir missão 1 → concluir/receber missão 2 → comprar Zanpakutō 1 na tela K → usar Shikai e treinar → concluir/receber missão 3 → comprar Zanpakutō 2 → atingir domínio 25 de Shikai → selecionar/ativar Bankai.

## 8. Operador e desenvolvimento

Exigem permissão 2 e personagem já confirmado. Troque Player pelo nome do jogador:

```
/bleachreload quests
/bleachdev points add Player 1000
/bleachdev skill set Player zanpakuto 2
/bleachdev mastery set Player zanpakuto shikai 25
/bleachdev mastery set Player zanpakuto bankai 50
/bleachdev reiatsu fill Player
/bleachdev cooldowns clear Player
/bleachdev asauchi give Player
/bleachdev inspect Player
```

skill set aceita 0–2 e pode reduzir nível; valores >=1/2 também descobrem as formas para teste. mastery set não compra skill nem descobre a forma. Os comandos normalizam formas inválidas, cancelam carga e sincronizam o estado.

cooldowns clear cancela áreas, Dash e morcegos e limpa recargas sem apagar progresso. Não existe reset geral. Para obter itens vanilla de teste, use /give.

Reload prepara quests e formas antes de substituí-las. Erros preservam os registries anteriores e são informados ao operador e no log.

## 9. JSONs e saves existentes

Os arquivos ficam em {mundo}/bleachmod/. Defaults só criam arquivos ausentes; não sobrescrevem configurações existentes.

- Mundos antigos podem conservar recompensas de skill/mastery 100; consulte as recompensas reais do diário.

- Hakuda, Vitalidade, Resistência e Kidou começam em zero. O antigo Poder migra integralmente para Zanjutsu; Reserva, Controle, pontos, skill, quests e mastery são preservados.

- A descoberta de formas de saves legados é inferida do nível da skill/mastery.

- O estado usa schemaVersion 3. Não abra save migrado com versão antiga sem backup compatível.

- JSONs de forma existentes não são sobrescritos. Para adotar o novo balanceamento, ajuste `energyDrain` de Shikai/Bankai para `0.08`/`0.16`; mundos novos já usam esses valores.

- Progresso de quest passa a guardar assinatura dos objetivos/recompensas e versão.

- No primeiro login atualizado, quests antigas sem assinatura vinculam-se ao conteúdo carregado naquele momento. Não é possível detectar retroativamente edições feitas antes dessa vinculação.

- Depois disso, mudanças estruturais incompatíveis bloqueiam início/progresso/resgate, preservando os dados. Restaure o JSON compatível ou faça migração explícita.

- Alterações de título/descrição não invalidam progresso.

- O parser suporta NATURAL/QUEST e ANY_MATCHING/QUEST_SPAWNED_ONLY; quests de boss já usam spawn por quest. Modos desconhecidos e contagens inválidas são rejeitados.

- Cliente e servidor precisam usar protocolo 2.3; atualizar ambos.

Faça backup antes de adaptar os JSONs de um mundo existente. A nova economia não é aplicada silenciosamente aos arquivos já configurados.

## 10. Limites atuais

Hollow/Hollow Boss e oito NPCs de quest existem como protótipos. Outras raças jogáveis, dimensões, party, facções e mentores completos ainda não existem. O kit Ryūjin possui os quatro slots; as três técnicas novas e o Corte ajustado aguardam homologação visual/multiplayer. Há regressões e GameTests específicos deste ciclo, mas não indicador contínuo de cooldown nem animação óssea. Sem respec.

## 11. Manutenção

Toda alteração de lógica deve revisar os .md relacionados em Docs, além deste manual. Atualize controles, UI, custos, quests, migrações e limites no mesmo trabalho. Os documentos numerados mantêm a referência do Dragon Mine Z e incluem notas separadas da implementação Bleach.
