# Herobrine - Forge 1.20.1 (47.2.0)

## Teste rápido (precisa de cheats / OP)
1. `/herobrine scare @s`            - demo de sons, chat falso, neblina, tremor e flash
2. `/herobrine appear @s stalker`   - aparição imediata do Espreitador
3. `/herobrine appear @s corrupted` - aparição do Corrompido (teste em SOBREVIVÊNCIA: mobs hostis ignoram jogadores no criativo)
Também: `/herobrine haunt <jogador> [stalker|corrupted]` e `/herobrine dread <jogador> <0-30>`.

## Automático
Em locais escuros (noite ao ar livre ou cavernas) no Overworld, em média a cada ~2 min (config). Cada aparição aumenta o nível de pavor, elevando a chance do Corrompido.
Se você atualizou de uma versão antiga, APAGUE `config/herobrine-common.toml` para pegar os novos padrões.

## Texturas / modelo
`python3 tools/gen_assets.py` regenera as texturas e o `CorruptedHerobrineModel.java`.
Para usar outra skin no Espreitador, substitua `textures/entity/herobrine.png` (64x64) e mantenha `herobrine_eyes.png`.
