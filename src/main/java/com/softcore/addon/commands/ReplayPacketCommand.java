package com.softcore.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import com.softcore.addon.modules.PacketReplayHelper;

public class ReplayPacketCommand extends Command {
    public ReplayPacketCommand() {
        super("replaypacket", "Replay the last recorded ClickSlot packet.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            PacketReplayHelper.replayLastPacketStatic();
            info("§aTried to replay last ClickSlot packet.");
            return SINGLE_SUCCESS;
        });
    }
}
