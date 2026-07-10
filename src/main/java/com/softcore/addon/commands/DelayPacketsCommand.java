package com.softcore.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DelayPacketsCommand extends Command {
    public DelayPacketsCommand() {
        super("delaypackets", "Toggle GUI packet delaying on/off");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("on").executes(context -> {
            UiUtilsBridge.setDelayUIPackets(true);
            info("Packet delaying ON");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("off").executes(context -> {
            UiUtilsBridge.setDelayUIPackets(false);
            var delayed = UiUtilsBridge.getDelayedUIPackets();
            if (mc.getNetworkHandler() != null) {
                for (var p : delayed) {
                    mc.getNetworkHandler().sendPacket(p);
                }
                info("Sent " + delayed.size() + " delayed packets");
                delayed.clear();
            }
            info("Packet delaying OFF");
            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            boolean on = UiUtilsBridge.getDelayUIPackets();
            info("Packet delaying: " + (on ? "ON" : "OFF"));
            return SINGLE_SUCCESS;
        });
    }
}
