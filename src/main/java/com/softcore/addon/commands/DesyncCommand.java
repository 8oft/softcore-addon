package com.softcore.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DesyncCommand extends Command {
    public DesyncCommand() {
        super("desync", "Close GUI server-side, keep it open client-side");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            if (mc.player == null || mc.getNetworkHandler() == null) {
                error("Not connected to a server");
                return SINGLE_SUCCESS;
            }
            if (mc.player.currentScreenHandler == null) {
                error("No screen handler");
                return SINGLE_SUCCESS;
            }
            mc.getNetworkHandler().sendPacket(
                new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId)
            );
            info("Sent close packet — GUI now desynced");
            return SINGLE_SUCCESS;
        });
    }
}
