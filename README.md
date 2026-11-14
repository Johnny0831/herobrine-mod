# Hollow Vigil - Forge 1.20.1 (47.2.0)

Uma presença oca observa você. Tudo acontece SOZINHO (sem comandos).

## Sistemas
- **Sanidade** (barra roxa): cai no escuro, durante aparições e perto das criaturas. Sobe com luz do dia, tochas na mão, dormindo e itens especiais.
  Abaixo de 70 ouve coisas; 50 chat falso; 30 silhuetas e neblina; 20 animais possuídos te observam; 10 investidas; **0 = alucinações REAIS que podem matar**.
- **Honra** (barra dourada/vermelha): matar animais, aldeões e golems tira honra; reproduzir/domar animais, matar monstros e saqueadores dá honra.
  **Pecador (≤25):** aparições 3x mais frequentes, Ceifador quase sempre, penitência (gritos das vítimas, falas com a contagem dos seus pecados), Ceifador mata com UM golpe.
  **Honrado (≥70):** nunca é atacado; O Oco entrega presentes (Amuleto da Sanidade, Coração Oco, Adaga Oca, maçãs douradas).
- **Possessão:** O Oco possui animais (olhos/brilho vermelho): observam, se aproximam quando você não olha e, no limite, atacam.

## Teste rápido (cheats)
`/hollow scare @s` | `/hollow appear @s stalker|reaper|gift` | `/hollow possess @s` | `/hollow sanity @s 0` | `/hollow honor @s 10`
Teste o Ceifador/alucinações em SOBREVIVÊNCIA.

## Regenerar texturas/modelo
`python3 tools/gen_assets.py`
