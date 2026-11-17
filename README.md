# Hollow Vigil - Forge 1.20.1 (47.2.0)

Uma presença oca observa você. Tudo acontece SOZINHO (sem comandos).

## Sanidade (barra com ícone de olho)
- **Só cai** ao **olhar para uma entidade** (O Oco, Ceifador, Alucinação) ou ao ficar em lugar **sem fonte de luz**.
- **Sobe** com **tocha/lanterna na mão** (até no escuro), em **ambientes iluminados**, **dormindo**, com o Amuleto da Sanidade, o Coração Oco e a Lanterna Oca.
- Abaixo de 70 ouve coisas; 50 chat falso e cinzas; 30 silhuetas, neblina, sussurros, palavras na tela; 20 animais possuídos te observam; 12 a câmera pode ser puxada à força;
  10 investidas; **0 = alucinações REAIS que podem matar** (somem quando a sanidade volta a 25).

## Honra (barra com ícone variável)
- Sobe: **alimentar animais (+1)**, reproduzir (+2), domar (+5), matar saqueadores/monstros. Desce: matar animais, aldeões, golems de vila.
- **Pecador (≤25):** aparições 3x mais, Ceifador quase sempre, penitência personalizada, Ceifador mata com UM golpe.
- **Honrado (≥70):** nunca é atacado; recebe presentes (Amuleto, Coração Oco, Lanterna Oca, maçãs, Totem).
- **0 = ASSASSINO:** sem medo, +vida, força, velocidade, fome lenta, visão vermelha, Faca do Assassino. Missão: caçar as entidades (só ele consegue feri-las).
- **100 = ANJO:** as entidades nunca aparecem, cura todas as criaturas, hostis não o atacam, voa, Herói da Vila.

## Teste rápido (cheats)
`/hollow scare @s` | `/hollow status @s` | `/hollow honor @s 0` (assassino) | `/hollow honor @s 100` (anjo) | `/hollow sanity @s 0` | `/hollow appear @s stalker|reaper|gift`

## Regenerar assets
`python3 tools/gen_sounds.py` (sons .ogg), `gen_gui.py` (HUD/itens), `gen_text.py` (falas/lang), `gen_assets.py` (texturas/modelo).
