# Herobrine - Forge 1.20.1 (47.2.0)

## Mobs
- **Herobrine (Espreitador)** - fica parado te encarando; só anda quando você não olha. Encarar demais: some, ou aparece colado na sua cara (jumpscare).
- **Herobrine Corrompido** - 3,2 blocos, 4 braços, mandíbula articulada, olhos brilhantes. Caça, teleporta para suas costas, escurece a visão, enfurece abaixo de 50% de vida.

## Terror psicológico (sistema de eventos)
IDLE -> PRESSÁGIOS (20-40 s) -> APARIÇÃO -> DESCANSO. Presságios: "Herobrine joined the game", passos atrás de você (com o som do bloco em que pisa), portas abrindo sozinhas, tochas apagando, sons de caverna/creeper/warden/enderman só para você, neblina fechada, batimento cardíaco crescente, tremor de câmera, fala falsa no chat. Cada aparição aumenta o *nível de pavor* do jogador (salvo), aumentando a chance do Corrompido.

## Comandos (OP nível 2)
- `/herobrine haunt <jogador> [stalker|corrupted]`
- `/herobrine appear <jogador> [stalker|corrupted]`  (imediato)
- `/herobrine dread <jogador> <0-30>`
Ovos de geração: aba Spawn Eggs do criativo.

## Config
`herobrine-common.toml` (frequência, cooldown, chat falso, mexer no mundo, jumpscares, Corrompido) e
`herobrine-client.toml` (acessibilidade: tremor, flashes, neblina).
