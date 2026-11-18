"""Gera lang pt_br/en_us (inclui ~145 falas por idioma) e ChatLines.java (contagens por categoria)."""
import json, os
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
LANG = ROOT + "/src/main/resources/assets/hollowvigil/lang/"
JAVA = ROOT + "/src/main/java/com/hollowvigil/ChatLines.java"

CHAT = {
"general": (
 ["atrás de você","eu vejo você","não olhe para trás","você não está sozinho aqui","por que parou de olhar?","fique comigo...",
  "a luz não vai durar","eles também estão olhando","conte os passos","você já devia ter ido embora","ouviu isso?","estou mais perto agora",
  "não corra. eu gosto quando corre","a porta estava aberta antes?","alguém moveu suas coisas","o silêncio também respira",
  "você sente o frio nas costas?","eu estava aqui antes de você","olhe de novo. eu mudei de lugar","seu nome soa bonito na escuridão",
  "não confie na sombra mais longa","este mundo nunca foi vazio","pisque devagar","alguém está batendo do lado de dentro",
  "o mundo lembra de tudo que você fez","não vire. ainda não.","há mais de um de nós","a noite é só o começo",
  "eu aprendi a andar como você","ainda dá tempo de se esconder"],
 ["behind you","i can see you","don't look back","you are not alone here","why did you stop looking?","stay with me...",
  "the light will not last","they are watching too","count the footsteps","you should have left already","did you hear that?","i am closer now",
  "don't run. i like it when you run","was the door open before?","someone moved your things","silence breathes too",
  "do you feel the cold on your back?","i was here before you","look again. i moved","your name sounds beautiful in the dark",
  "do not trust the longest shadow","this world was never empty","blink slowly","someone is knocking from the inside",
  "the world remembers everything you did","don't turn around. not yet.","there is more than one of us","night is only the beginning",
  "i learned to walk like you","there is still time to hide"]),
"dark": (
 ["a escuridão tem dentes","acenda algo. rápido.","sem luz, eu fico mais forte","sua tocha está tremendo","o escuro aqui é antigo",
  "você não enxerga, mas eu enxergo você","as paredes estão ouvindo","quanto tempo você aguenta sem luz?","a luz atrai... algo pior",
  "feche os olhos. não muda nada","o breu fala baixinho","tem alguém respirando junto com você"],
 ["the dark has teeth","light something. quickly.","without light, i grow stronger","your torch is trembling","the dark here is ancient",
  "you cannot see, but i see you","the walls are listening","how long can you last without light?","light attracts... something worse",
  "close your eyes. it changes nothing","the blackness speaks softly","someone is breathing along with you"]),
"low": (
 ["%1$s... %1$s... %1$s...","ei, %1$s. aqui.","%1$s, o que é real?","você está acordado, %1$s?","as paredes sabem seu nome, %1$s",
  "%1$s não consegue mais distinguir","isto é um sonho, %1$s?","eu sou você, %1$s","%1$s, o chão está sangrando","olhe para as suas mãos, %1$s",
  "quem está respirando, %1$s?","%1$s, não confie no que vê","a mente racha, %1$s","falta pouco, %1$s","volte para a luz, %1$s... ou não"],
 ["%1$s... %1$s... %1$s...","hey, %1$s. over here.","%1$s, what is real?","are you awake, %1$s?","the walls know your name, %1$s",
  "%1$s can no longer tell the difference","is this a dream, %1$s?","i am you, %1$s","%1$s, the floor is bleeding","look at your hands, %1$s",
  "who is breathing, %1$s?","%1$s, don't trust what you see","the mind cracks, %1$s","almost there, %1$s","go back to the light, %1$s... or don't"]),
"sin_animals": (
 ["%2$s animais gritam seu nome, %1$s","os %2$s que você matou ainda estão aqui","%1$s... %2$s vidas nas suas mãos",
  "o gado lembra. %2$s deles te esperam","cada um dos %2$s tinha um rosto","o campo tem %2$s túmulos por sua causa"],
 ["%2$s animals scream your name, %1$s","the %2$s you killed are still here","%1$s... %2$s lives in your hands",
  "the cattle remember. %2$s of them wait for you","each of the %2$s had a face","the field has %2$s graves because of you"]),
"sin_villagers": (
 ["%2$s aldeões não perdoam você, %1$s","as casas vazias guardam %2$s nomes","o sino da vila toca por %2$s mortos",
  "%1$s, %2$s famílias sem pai","eles ainda cozinham para %2$s fantasmas","%2$s... e você ainda dorme"],
 ["%2$s villagers do not forgive you, %1$s","the empty houses hold %2$s names","the village bell tolls for %2$s dead",
  "%1$s, %2$s families without a father","they still cook for %2$s ghosts","%2$s... and you still sleep"]),
"sin_generic": (
 ["seus pecados têm cheiro","a balança está pesada, %1$s","eu anotei tudo","você não parece culpado. isso é pior",
  "a culpa te segue como uma sombra","alguém vai cobrar","o mundo está cansado de você","nem o fogo apaga o que você fez"],
 ["your sins have a smell","the scales are heavy, %1$s","i wrote everything down","you don't look guilty. that is worse",
  "guilt follows you like a shadow","someone will collect","the world is tired of you","not even fire erases what you did"]),
"honored": (
 ["o mundo está melhor com você","continue assim, %1$s","a bondade ainda existe","estou de olho... com carinho","os animais confiam em você",
  "poucos chegam tão longe sem sujar as mãos","a luz te reconhece","descanse. você merece","você me lembra algo que perdi","mantenha a chama acesa"],
 ["the world is better with you","keep going, %1$s","kindness still exists","i am watching... fondly","the animals trust you",
  "few get this far without dirty hands","the light recognizes you","rest. you deserve it","you remind me of something i lost","keep the flame alive"]),
"gift": (
 ["você foi justo. leve isto.","a bondade também deixa rastros.","guarde isso. você vai precisar.","um presente de quem observa.",
  "não é pagamento. é reconhecimento.","isto vai te manter inteiro.","use quando o escuro apertar.","poucos merecem. você merece."],
 ["you were fair. take this.","kindness leaves a trail too.","keep this. you will need it.","a gift from the one who watches.",
  "not payment. recognition.","this will keep you whole.","use it when the dark closes in.","few deserve it. you do."]),
"reaper": (
 ["corra.","eu cansei de esperar","agora você me vê","não adianta se esconder","olhe para mim","o fim tem pernas longas",
  "vamos ver quanto você aguenta","seus passos me chamaram","eu sou o que sobrou","pare de fugir. é pior."],
 ["run.","i am tired of waiting","now you see me","hiding is useless","look at me","the end has long legs",
  "let's see how much you can take","your footsteps called me","i am what is left","stop running. it is worse."]),
"killer": (
 ["você já foi alguém, %1$s","o caçador virou caça?","mate-nos. se conseguir.","faca na mão, medo nenhum","cada um que cai nos deixa mais famintos",
  "você carrega o que fez","sangue lava sangue?","não somos todos... só o que sobrou","continue cortando, %1$s","quando acabar, a honra volta... talvez"],
 ["you used to be someone, %1$s","has the hunter become the prey?","kill us. if you can.","knife in hand, no fear at all","every one that falls makes us hungrier",
  "you carry what you did","does blood wash blood?","not all of us... only what is left","keep cutting, %1$s","when you finish, honor returns... maybe"]),
"angel": (
 ["não me vejo... em você","ninguém pode te assombrar","a luz pesa em você, %1$s","voe, %1$s","as criaturas sentem seu calor",
  "eu não pertenço a este lugar de luz","vá. cuide deles","nunca mais vou aparecer para você"],
 ["i don't see myself... in you","no one can haunt you","the light weighs on you, %1$s","fly, %1$s","the creatures feel your warmth",
  "i do not belong in this place of light","go. take care of them","i will never appear to you again"]),
"verdict": (["pecados pagos.","a conta chegou.","sem perdão.","justiça."],
            ["sins paid.","the bill has come.","no forgiveness.","justice."]),
"ghost": (
 ["alguém me ajuda?","não olhem para o céu","ele está atrás de mim","eu morri aqui antes","quem ainda está online?","tem algo no meu porão",
  "fujam enquanto dá","eu vi o rosto dele"],
 ["can anyone help me?","don't look at the sky","it is behind me","i died here before","who is still online?","something is in my basement",
  "run while you can","i saw its face"]),
}
GHOST_NAMES = (["Vazio_7","Ninguém","Esquecido","Voz"], ["Void_7","Nobody","Forgotten","Voice"])
ADV = (["[Atrás de Você]","[Não Olhe]","[Companhia Indesejada]","[Escuro Demais]","[Nunca Sozinho]","[Pisque Devagar]","[Voz na Parede]","[Culpado]","[Sono Leve]","[Olhos Brancos]"],
       ["[Behind You]","[Do Not Look]","[Unwanted Company]","[Too Dark]","[Never Alone]","[Blink Slowly]","[Voice in the Wall]","[Guilty]","[Light Sleeper]","[White Eyes]"])
WHISPERS = (["atrás de você","não olhe","ele vê","corra","ainda aqui","eu sei","pisque","sozinho?","ouça","vazio"],
            ["behind you","don't look","it sees","run","still here","i know","blink","alone?","listen","empty"])
SUBS = {
 "heartbeat":("Batimento cardíaco","Heartbeat"),"heartbeat_fast":("Batimento acelerado","Racing heartbeat"),"breath":("Respiração","Breathing"),
 "whisper":("Sussurros","Whispers"),"drone":("Zumbido grave","Low drone"),"stinger":("Impacto sinistro","Ominous hit"),
 "scream_far":("Grito distante","Distant scream"),"scrape":("Arranhão metálico","Metal scrape"),"static_burst":("Estática","Static"),
 "creak":("Rangido","Creak"),"knock":("Batida","Knock"),"angel_choir":("Coro angelical","Angelic choir"),"chime":("Sino cristalino","Crystal chime"),
 "rage":("Fúria","Rage"),"hollow_idle":("Respiração oca","Hollow breathing"),"reaper_roar":("Rugido","Roar"),"jumpscare":("Grito agudo","Screech"),
 "toll":("Dobre de sino","Bell toll")}

BASE = {
 "itemGroup.hollowvigil": ("Hollow Vigil", "Hollow Vigil"),
 "entity.hollowvigil.hollow": ("O Oco", "The Hollow"),
 "entity.hollowvigil.hollow_reaper": ("Ceifador Oco", "Hollow Reaper"),
 "entity.hollowvigil.hallucination": ("Alucinação", "Hallucination"),
 "item.hollowvigil.hollow_spawn_egg": ("Ovo de Geração de O Oco", "The Hollow Spawn Egg"),
 "item.hollowvigil.hollow_reaper_spawn_egg": ("Ovo de Geração de Ceifador Oco", "Hollow Reaper Spawn Egg"),
 "item.hollowvigil.hallucination_spawn_egg": ("Ovo de Geração de Alucinação", "Hallucination Spawn Egg"),
 "item.hollowvigil.sanity_charm": ("Amuleto da Sanidade", "Sanity Charm"),
 "item.hollowvigil.sanity_charm.desc": ("Botão direito acalma a mente (+30 sanidade). No inventário, a sanidade cai mais devagar.",
                                        "Right-click to calm your mind (+30 sanity). While in your inventory, sanity drains slower."),
 "item.hollowvigil.hollow_heart": ("Coração Oco", "Hollow Heart"),
 "item.hollowvigil.hollow_heart.desc": ("Comer restaura toda a sua sanidade.", "Eating it fully restores your sanity."),
 "item.hollowvigil.hollow_lantern": ("Lanterna Oca", "Hollow Lantern"),
 "item.hollowvigil.hollow_lantern.desc": ("Segurada na mão, afasta o medo: a sanidade sobe rápido, até no escuro total.",
                                          "Held in hand it wards off fear: sanity rises fast, even in total darkness."),
 "item.hollowvigil.killer_knife": ("Faca do Assassino", "Killer's Knife"),
 "item.hollowvigil.killer_knife.desc": ("Infinita e cada vez mais forte a cada caça. Só quem perdeu toda a honra fere as entidades.", "Endless and stronger with every hunt. Only one who lost all honor can wound the entities."),
 "effect.hollowvigil.possessed": ("Possuído", "Possessed"),
 "hud.hollowvigil.sanity": ("Sanidade", "Sanity"),
 "hud.hollowvigil.honor": ("Honra", "Honor"),
 "hud.hollowvigil.killer": ("ASSASSINO", "KILLER"),
 "hud.hollowvigil.angel": ("ANJO", "ANGEL"),
 "hud.hollowvigil.mission": ("Caçada", "Hunt"),
 "hollow.welcome": ("[Hollow Vigil] Mod carregado. Algo vai observar você em breve. (Teste: /hollow scare @s - precisa de cheats)",
                    "[Hollow Vigil] Mod loaded. Something will watch you soon. (Test: /hollow scare @s - needs cheats)"),
 "hollow.madness.start": ("As paredes estão respirando. Agora elas são reais.", "The walls are breathing. They are real now."),
 "hollow.sinner.warn": ("Algo antigo agora conta os seus pecados.", "Something old now counts your sins."),
 "hollow.honored.notice": ("Uma presença calma agora vela por você.", "A calm presence now watches over you."),
 "hollow.killer.title": ("ASSASSINO", "KILLER"),
 "hollow.killer.subtitle": ("Sua honra acabou. Mate todas as entidades.", "Your honor is gone. Kill every entity."),
 "hollow.killer.msg.1": ("Sua honra chegou a zero. Você não teme mais nada.", "Your honor reached zero. You fear nothing now."),
 "hollow.killer.msg.2": ("Missão: elimine as entidades. Só você pode feri-las agora.", "Mission: eliminate the entities. Only you can hurt them now."),
 "hollow.killer.msg.3": ("Uma faca foi colocada em suas mãos. Sua vida e sua força aumentaram.", "A knife was placed in your hands. Your health and strength grew."),
 "hollow.killer.progress": ("Caçada: %1$s/%2$s", "Hunt: %1$s/%2$s"),
 "hollow.killer.done.title": ("ABSOLVIDO", "ABSOLVED"),
 "hollow.killer.done.subtitle": ("A caçada terminou. Sua honra renasce.", "The hunt is over. Your honor is reborn."),
 "hollow.killer.done.msg": ("As entidades se calaram. Você recuperou parte da honra.", "The entities fell silent. You regained some honor."),
 "hollow.killer.fading": ("A fúria enfraquece... mate para mantê-la acesa.", "The fury is fading... kill to keep it burning."),
 "hollow.killer.exhausted.title": ("EXAUSTO", "EXHAUSTED"),
 "hollow.killer.exhausted.subtitle": ("A fúria passou. Seu corpo cobra o preço.", "The fury is gone. Your body demands its price."),
 "hollow.killer.exhausted.msg": ("Você ficou tempo demais sem matar. A honra voltou e seu corpo está lento e exausto.",
                                 "You went too long without killing. Your honor returned and your body is slow and exhausted."),
 "hud.hollowvigil.rage": ("Fúria", "Fury"),
 "hud.hollowvigil.knife": ("Faca Nv. %1$s", "Knife Lv. %1$s"),
 "item.hollowvigil.killer_knife.level": ("Nível %1$s (+%2$s de dano) - não quebra nunca", "Level %1$s (+%2$s damage) - never breaks"),
 "hollow.angel.title": ("ANJO", "ANGEL"),
 "hollow.angel.subtitle": ("Honra perfeita. Nenhuma entidade vai te encontrar.", "Perfect honor. No entity will find you."),
 "hollow.angel.msg": ("Asas de luz te sustentam. As criaturas sentem seu calor e as entidades nunca mais aparecerão para você.",
                      "Wings of light hold you up. The creatures feel your warmth and the entities will never appear to you again."),
 "hollow.angel.fall": ("A luz se apagou. Você voltou a ser mortal.", "The light went out. You are mortal again."),
}

pt, en = {}, {}
for k, (a, b) in BASE.items(): pt[k], en[k] = a, b
counts = {}
for cat, (lp, le) in CHAT.items():
    assert len(lp) == len(le), cat
    counts[cat] = len(lp)
    for i, (a, b) in enumerate(zip(lp, le), 1):
        pt[f"hollow.chat.{cat}.{i}"], en[f"hollow.chat.{cat}.{i}"] = a, b
for i, (a, b) in enumerate(zip(*GHOST_NAMES), 1): pt[f"hollow.ghost.name.{i}"], en[f"hollow.ghost.name.{i}"] = a, b
for i, (a, b) in enumerate(zip(*ADV), 1): pt[f"hollow.adv.{i}"], en[f"hollow.adv.{i}"] = a, b
for i, (a, b) in enumerate(zip(*WHISPERS), 1): pt[f"hud.hollowvigil.whisper.{i}"], en[f"hud.hollowvigil.whisper.{i}"] = a, b
for k, (a, b) in SUBS.items(): pt["subtitles.hollowvigil." + k], en["subtitles.hollowvigil." + k] = a, b
json.dump(pt, open(LANG + "pt_br.json", "w"), ensure_ascii=False, indent=1)
json.dump(en, open(LANG + "en_us.json", "w"), ensure_ascii=False, indent=1)

body = "".join(f'        COUNTS.put("{c}", {n});\n' for c, n in counts.items())
open(JAVA, "w").write('''package com.hollowvigil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/** GERADO por tools/gen_text.py (contagens). Falas de O Oco, falas fantasmas e conquistas falsas. */
public final class ChatLines {
    private ChatLines() {}

    private static final Map<String, Integer> COUNTS = new HashMap<>();
    static {
''' + body + '''    }

    private static Component name() {
        return Component.translatable("entity.hollowvigil.hollow");
    }

    /** Linha aleatória da categoria; %1$s = nome do jogador, %2$s... = argumentos extras. */
    public static Component line(String cat, ServerPlayer sp, Object... extra) {
        int n = COUNTS.getOrDefault(cat, 1);
        Object[] args = new Object[extra.length + 1];
        args[0] = sp.getName();
        System.arraycopy(extra, 0, args, 1, extra.length);
        return Component.translatable("hollow.chat." + cat + "." + (1 + sp.getRandom().nextInt(n)), args);
    }

    public static void hollowSays(ServerPlayer sp, String cat, Object... extra) {
        sp.sendSystemMessage(Component.literal("<").append(name()).append(Component.literal("> ")).append(line(cat, sp, extra)));
    }

    /** Um "jogador" que não existe fala no chat. */
    public static void ghost(ServerPlayer sp) {
        int who = 1 + sp.getRandom().nextInt(4);
        sp.sendSystemMessage(Component.literal("<").append(Component.translatable("hollow.ghost.name." + who))
                .append(Component.literal("> ")).append(line("ghost", sp)));
    }

    /** Conquista falsa, no formato do jogo. */
    public static void advancement(ServerPlayer sp) {
        int n = 1 + sp.getRandom().nextInt(10);
        sp.sendSystemMessage(Component.translatable("chat.type.advancement.task", sp.getDisplayName(),
                Component.translatable("hollow.adv." + n).withStyle(ChatFormatting.GREEN)));
    }

    /** Escolhe sozinho a fala certa para o estado do jogador (honra, sanidade, escuro, modo). */
    public static void say(ServerPlayer sp) {
        double r = sp.getRandom().nextDouble();
        if (r < 0.07D) { advancement(sp); return; }
        if (r < 0.16D) { ghost(sp); return; }
        float sanity = PlayerStats.getSanity(sp);
        int light = ((ServerLevel) sp.level()).getMaxLocalRawBrightness(sp.blockPosition());
        double q = sp.getRandom().nextDouble();
        if (PlayerStats.isKiller(sp)) { hollowSays(sp, "killer"); return; }
        if (PlayerStats.isSinner(sp) && q < 0.6D) {
            int animals = PlayerStats.getAnimalsKilled(sp);
            int villagers = PlayerStats.getVillagersKilled(sp);
            if (villagers > 0 && q < 0.3D) hollowSays(sp, "sin_villagers", villagers);
            else if (animals > 0) hollowSays(sp, "sin_animals", animals);
            else hollowSays(sp, "sin_generic");
            return;
        }
        if (sanity < 40.0F && q < 0.5D) { hollowSays(sp, "low"); return; }
        if (light <= 4 && q < 0.4D) { hollowSays(sp, "dark"); return; }
        if (PlayerStats.isHonored(sp) && q < 0.5D) { hollowSays(sp, "honored"); return; }
        hollowSays(sp, "general");
    }
}
''')
print("falas por categoria:", counts, "| total", sum(counts.values()))
