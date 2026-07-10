package com.softcore.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class SendPacketsCommand extends Command {
    public SendPacketsCommand() {
        super("sendpackets", "Toggle UI packet sending on/off");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("on").executes(context -> {
            UiUtilsBridge.setSendUIPackets(true);
            info("Packet sending ON");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("off").executes(context -> {
            UiUtilsBridge.setSendUIPackets(false);
            info("Packet sending OFF");
            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            boolean on = UiUtilsBridge.getSendUIPackets();
            info("Packet sending: " + (on ? "ON" : "OFF"));
            return SINGLE_SUCCESS;
        });
    }
}
