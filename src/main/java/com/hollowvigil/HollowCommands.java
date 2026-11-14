package com.hollowvigil;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /hollow scare   <jogador>                          demo de sons/chat/efeitos
 * /hollow haunt   <jogador> [stalker|reaper|gift]    presságios e depois a aparição
 * /hollow appear  <jogador> [stalker|reaper|gift]    aparição imediata
 * /hollow possess <jogador>                          animais próximos atacam
 * /hollow sanity  <jogador> <0-100>   /hollow honor <jogador> <0-100>   /hollow dread <jogador> <0-30>
 */
public class HollowCommands {
    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("hollow")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("scare")
                        .then(Commands.argument("target", EntityArgument.player()).executes(this::scare)))
                .then(Commands.literal("haunt")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(c -> haunt(c, HorrorDirector.Kind.AUTO, false))
                                .then(Commands.literal("stalker").executes(c -> haunt(c, HorrorDirector.Kind.STALKER, false)))
                                .then(Commands.literal("reaper").executes(c -> haunt(c, HorrorDirector.Kind.REAPER, false)))
                                .then(Commands.literal("gift").executes(c -> haunt(c, HorrorDirector.Kind.GIFT, false)))))
                .then(Commands.literal("appear")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(c -> haunt(c, HorrorDirector.Kind.AUTO, true))
                                .then(Commands.literal("stalker").executes(c -> haunt(c, HorrorDirector.Kind.STALKER, true)))
                                .then(Commands.literal("reaper").executes(c -> haunt(c, HorrorDirector.Kind.REAPER, true)))
                                .then(Commands.literal("gift").executes(c -> haunt(c, HorrorDirector.Kind.GIFT, true)))))
                .then(Commands.literal("possess")
                        .then(Commands.argument("target", EntityArgument.player()).executes(this::possess)))
                .then(stat("sanity", 100))
                .then(stat("honor", 100))
                .then(stat("dread", 30)));
    }

    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> stat(String name, int max) {
        return Commands.literal(name)
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, max))
                                .executes(c -> setStat(c, name))));
    }

    private int setStat(CommandContext<CommandSourceStack> c, String name) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        int v = IntegerArgumentType.getInteger(c, "value");
        switch (name) {
            case "sanity" -> PlayerStats.setSanity(p, v);
            case "honor" -> PlayerStats.setHonorRaw(p, v);
            default -> PlayerStats.setDread(p, v);
        }
        PlayerStats.sync(p);
        c.getSource().sendSuccess(() -> Component.literal(name + " de " + p.getName().getString() + " = " + v), true);
        return 1;
    }

    private int scare(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        HorrorDirector.INSTANCE.demoScare(p);
        c.getSource().sendSuccess(() -> Component.literal("Demo de susto -> " + p.getName().getString()), true);
        return 1;
    }

    private int possess(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        int n = HorrorDirector.INSTANCE.possess((ServerLevel) p.level(), p, 4, 1, 40);
        c.getSource().sendSuccess(() -> Component.literal(n + " animais possuidos"), true);
        return n;
    }

    private int haunt(CommandContext<CommandSourceStack> c, HorrorDirector.Kind kind, boolean immediate)
            throws CommandSyntaxException {
        ServerPlayer p = EntityArgument.getPlayer(c, "target");
        HorrorDirector.INSTANCE.startHaunt(p, kind, immediate);
        c.getSource().sendSuccess(() -> Component.literal("O Oco -> " + p.getName().getString()), true);
        return 1;
    }
}
