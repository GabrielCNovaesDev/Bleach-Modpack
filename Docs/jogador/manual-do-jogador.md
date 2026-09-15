# Manual do jogador — Bleach Mod (MVP Shinigami)

Atualizado em 14/09/2026. Minecraft 1.20.1 / Forge 47.4.10 / Java 17.

Este manual descreve o código atual. A verificação visual em jogo e com dois clientes ainda está pendente; consulte o [plano de implementação](../planejamento/plano-implementacao-mvp.md) para o estado de cada entrega. Toda mudança de lógica exige atualizar este manual e os demais `.md` afetados em `Docs`.

## 1. Começar

Confirme Shinigami na primeira entrada; aguarde a resposta do servidor. Você recebe Asauchi e 100 de reiatsu. Não existe mob Hollow próprio: usamos zumbis e, em uma missão, esqueletos. A interface informa os alvos e itens reais.

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
| X | Executar a técnica piloto Flame Burst / Explosão de chamas |

A técnica Flame Burst não exige Bankai ativo. Ela custa 20 de reiatsu, possui cooldown de 15 segundos e causa dano em uma área curta ao redor do jogador. O dano base provisório é 6 pontos. A tecla X é remapeável.

Selecionar uma forma não a ativa. O HUD distingue forma ativa e alvo. A técnica piloto ainda não possui animação óssea nem slot no radial.

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

- HUD de recursos usa o painel conceitual `bleach_hud_concept.png` em escala 0.45×, com três barras empilhadas: **Vida** (substitui os corações vanilla), **Reiatsu** e **Transformação** (progresso da carga do R). Spiritual Points e o estágio da Zanpakutō ficam dentro da barra de Transformação. Os corações vanilla somem quando o personagem é criado; fome, hotbar, XP e vignette continuam do Minecraft.

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

Aumentar reserva preserva a energia atual; não recarrega a barra gratuitamente. Vitalidade aumenta o máximo, mas não reduz o dano recebido; esse é o papel de Resistência. Controle tem retorno decrescente e nunca converte drain em regeneração.

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

Bônus de combate se aplica ao golpe direto de jogador com Asauchi na mão principal:

- Shikai: +20%.

- Bankai: +50%.

- Zanjutsu: +10% por nível, somado ao percentual da forma.

Exemplo: Bankai com Zanjutsu 2 multiplica o dano original por 1,7. Hakuda usa a mesma progressão de 10% quando o golpe é desarmado, sem bônus de forma. O dano original mantém cooldown, crítico e encantamentos; os percentuais entram no evento de dano antes das etapas posteriores de mitigação. Não há bônus persistente empilhado ao alternar formas.

Morte retorna à selada e respawn recupera reiatsu. Carga é cancelada ao abrir telas, mudar alvo, morrer, desconectar ou trocar de dimensão. Falhas de transformação não devem ficar repetindo a tentativa por tick.

## 7. Missões

| Missão | Objetivos | Novas recompensas padrão |
| --- | --- | --- |
| Caça aos Hollows | 5 zumbis | 200 pontos |
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
/bleachdev asauchi give Player
/bleachdev inspect Player
```

skill set aceita 0–2 e pode reduzir nível; valores >=1/2 também descobrem as formas para teste. mastery set não compra skill nem descobre a forma. Os comandos normalizam formas inválidas, cancelam carga e sincronizam o estado.

Não existe reset geral. Para obter itens vanilla de teste, use /give.

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

- O parser aceita somente NATURAL + ANY_MATCHING neste MVP; configurações QUEST incompletas são rejeitadas.

- Cliente e servidor precisam usar protocolo 2.1; atualizar ambos.

Faça backup antes de adaptar os JSONs de um mundo existente. A nova economia não é aplicada silenciosamente aos arquivos já configurados.

## 10. Limites atuais

Sem mob Hollow próprio, outras raças, dimensões, party, NPCs complexos ou técnicas ativas adicionais. A técnica piloto Flame Burst existe como primeiro protótipo de ação, mas ainda não possui homologação visual, GameTest dedicado, indicador de cooldown ou animação óssea. Sem respec. Transparência/orientação dos assets e inspeção visual final devem acompanhar o estado real no plano de implementação.

## 11. Manutenção

Toda alteração de lógica deve revisar os .md relacionados em Docs, além deste manual. Atualize controles, UI, custos, quests, migrações e limites no mesmo trabalho. Os documentos numerados mantêm a referência do Dragon Mine Z e incluem notas separadas da implementação Bleach.