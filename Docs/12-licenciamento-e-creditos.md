# Licenciamento e créditos

## Licença aplicável (GPL-3.0)

O repositório original **Dragon Mine Z** é distribuído sob a **GNU General Public License v3.0**.

- Texto completo no clone: `dragonminez/LICENSE`
- Copyright no cabeçalho do LICENSE: `Copyright © DragonMine Z 2025`
- README do original: “2026, DragonMine Z” + GPL-3.0 or later
- Autores listados no original: Yuseix300, ezShokkoh (e créditos a Bruno, Kyo, e outros)

O fork Bleach **deve** permanecer sob GPL-3.0 (ou GPL-3.0-or-later, se o grupo optar pela cláusula “or later” do README original). Isso inclui:

1. Arquivo `LICENSE` com o texto completo da GPL-3.0 na raiz do fork.
2. Aviso de copyright do original preservado.
3. Código-fonte completo publicado (obrigação copyleft).
4. Qualquer arquivo em `/reference-code` carrega o aviso de origem + GPL-3.0 no topo.

O texto integral da GPL-3.0 tem ~600 linhas e não é duplicado aqui de propósito: copie `dragonminez/LICENSE` para a raiz do fork. Não altere o texto da licença.

Trecho de aviso pronto para cabeçalhos de arquivo:

```
This file is part of a work derived from Dragon Mine Z
(https://github.com/DragonMineZ/dragonminez), licensed under GNU GPL-3.0.
Copyright © DragonMine Z 2025
```

## Dependências de terceiros (MIT e outras)

### GeckoLib — MIT

Arquivo original: `dragonminez/THIRD_PARTY_LICENSES`

```
MIT License
Copyright (c) 2024 GeckoThePecko
```

Se o Bleach **não** usar GeckoLib no MVP, este aviso não precisa ir para o binário — mas deve permanecer em `THIRD_PARTY_LICENSES` se o código de referência ou um módulo futuro o incluir.

O README original atribui também: “This project includes code from GeckoLib (https://github.com/bernie-g/geckolib)”.

### Outras libs (não MIT, mas a declarar se forem usadas)

| Lib | Uso no original | Ação no Bleach |
|---|---|---|
| TerraBlender | Biomas | Só se houver dimensão/bioma custom |
| Curios | Slots de acessório | Só se houver scouter/sensor como curio |
| MariaDB JDBC + HikariCP | Storage opcional | Não usar no MVP |
| Lombok | Compile-only | Sem obrigação de redistribuir |
| JEI | Compat | Não usar no MVP |

Sons de terceiros citados no README original (Zapsplat, Freesound, CC-BY) são **tema Dragon Ball** e não devem ser copiados.

## Texto de crédito pronto para o README do fork

Cole no `Docs/README.md` (seção Créditos / Baseado em):

```
Este projeto é um trabalho derivado de Dragon Mine Z
(https://github.com/DragonMineZ/dragonminez),
Copyright © DragonMine Z 2025, distribuído sob GNU GPL-3.0.

Autores originais: Yuseix, ezShokkoh, Bruno e contribuidores listados no
repositório original.

O conteúdo temático de Dragon Ball pertence aos respectivos detentores.
Este fork reaproveita a arquitetura dos sistemas de quest e de
progressão/evolução; todo o conteúdo temático de Bleach é original deste
projeto e não é afiliado a Tite Kubo, Shueisha ou Studio Pierrot.
```

## O que foi copiado vs. o que foi só documentado

| Artefato | Política |
|---|---|
| `/Docs/00`–`12` | Documentação de comportamento e arquitetura. **Sem** blocos de código copiados do original. |
| `/reference-code/*` | Subconjunto deliberado de fontes originais, cada um com justificativa + GPL-3.0 no topo. Ver `reference-code/README.md`. |
| Assets, modelos, sons, JSON de sagas DB | **Não** copiar. Só a mecânica interessa. |
| `QuestDefaults` / nomes de Raditz, Roshi, etc. | Não copiar conteúdo temático. Recriar quests Bleach do zero usando o schema. |

## Checklist legal do fork

- [ ] `LICENSE` GPL-3.0 na raiz
- [ ] `THIRD_PARTY_LICENSES` se houver GeckoLib (ou outra MIT)
- [ ] Crédito ao Dragon Mine Z no README
- [ ] Aviso fan-made / não-comercial / não-oficial (Bleach)
- [ ] Todo arquivo em `reference-code` com cabeçalho de origem
- [ ] Código-fonte do fork público (obrigação GPL)
