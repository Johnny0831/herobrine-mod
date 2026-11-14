package com.herobrinemod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /herobrine scare  <jogador>                       -> demo imediata: sons, chat, neblina, tremor e flash
 * /herobrine haunt  <jogador> [stalker|corrupted]   -> presságios e depois a aparição
 * /herobrine appear <jogador> [stalker|corrupted]   -> aparição imediata
 * /herobrine dread  <jogador> <0-30>                -> define o nível de pavor
 */
public class HerobrineCommands {
    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("herobrine")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("scare")
                        .then(Commands.argument("target", EntityArgument.player()).executes(this::scare)))
                .then(Commands.literal("haunt")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(c -> haunt(c, null, false))
                                .then(Commands.literal("stalker").executes(c -> haunt(c, false, false)))
                                .then(Commands.literal("corrupted").executes(c -> haunt(c, true, false)))))
                .then(Commands.literal("appear")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(c -> haunt(c, null, true))
                                .then(Commands.literal("stalker").executes(c -> haunt(c, false, true)))
                                .then(Commands.literal("corrupted").executes(c -> haunt(c, true, true)))))
                .then(Commands.literal("dread")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("value", IntegerArgumentType.integer(0, 30))
                                        .executes(this::dread)))));
    }

    private int scare(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        HorrorDirector.INSTANCE.demoScare(p);
        c.getSource().sendSuccess(() -> Component.literal("Demo de susto -> " + p.getName().getString()), true);
        return 1;
    }

    private int haunt(CommandContext<CommandSourceStack> c, Boolean corrupted, boolean immediate)
            throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        HorrorDirector.INSTANCE.startHaunt(p, corrupted, immediate);
        c.getSource().sendSuccess(() -> Component.literal("Herobrine -> " + p.getName().getString()), true);
        return 1;
    }

    private int dread(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        int v = IntegerArgumentType.getInteger(c, "value");
        HorrorDirector.setDread(p, v);
        c.getSource().sendSuccess(() -> Component.literal("Nivel de pavor de " + p.getName().getString() + " = " + v), true);
        return 1;
    }
}
