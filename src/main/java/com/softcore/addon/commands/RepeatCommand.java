package com.softcore.addon.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

public class RepeatCommand extends Command {
    public RepeatCommand() {
        super("repeat", "Repeats a Command X Times");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("times", IntegerArgumentType.integer(1))
               .then(argument("cmd", StringArgumentType.greedyString())
               .executes((context) -> {
                   int times = context.getArgument("times", Integer.class);
                   String cmd = context.getArgument("cmd", String.class);

                   for (int i = 0; i < times; i++) {
                       ChatUtils.sendPlayerMsg(cmd.replaceAll("(?i)%index%", String.valueOf(i)));
                   }

                   return SINGLE_SUCCESS;
               })));
    }
}
