package com.softcore.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class DisconnectPacketsCommand extends Command {
    public DisconnectPacketsCommand() {
        super("disconnectpackets", "Send all delayed packets then disconnect");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            UiUtilsBridge.setDelayUIPackets(false);
            var delayed = UiUtilsBridge.getDelayedUIPackets();
            int count = delayed.size();
            if (mc.getNetworkHandler() != null) {
                for (Packet<?> p : delayed) {
                    mc.getNetworkHandler().sendPacket(p);
                }
                mc.getNetworkHandler().getConnection().disconnect(Text.of("Disconnecting (softcore)"));
            }
            delayed.clear();
            info("Sent " + count + " packets and disconnected");
            return SINGLE_SUCCESS;
        });
    }
}
