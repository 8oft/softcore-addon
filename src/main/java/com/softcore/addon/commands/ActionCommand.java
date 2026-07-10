package com.softcore.addon.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ActionCommand extends Command {
    public ActionCommand() {
        super("action", "Interact with blocks in the world");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("open").executes(context -> {
            if (mc.player == null || mc.getNetworkHandler() == null) {
                error("Not in game");
                return SINGLE_SUCCESS;
            }

            if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
                error("Not looking at a block");
                return SINGLE_SUCCESS;
            }

            BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
            PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(
                Hand.MAIN_HAND, hit, 0
            );
            mc.getNetworkHandler().sendPacket(packet);
            info("Opened " + hit.getBlockPos().toShortString());
            return SINGLE_SUCCESS;
        }));
    }
}
