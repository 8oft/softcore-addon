package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.text.Text;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Kick extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum KickMode {
        Disconnect,
        Invalid_Position,
        Self_Hurt,
        Invalid_Chat
    }

    private final Setting<KickMode> mode = sgGeneral.add(new EnumSetting.Builder<KickMode>()
        .name("mode")
        .description("Method to use for kicking.")
        .defaultValue(KickMode.Disconnect)
        .build()
    );

    public Kick() {
        super(SoftcoreAddon.CATEGORY, "kick", "Kicks you from the server using various methods.");
    }

    @Override
    public void onActivate() {
        switch (mode.get()) {
            case Disconnect -> disconnect();
            case Invalid_Position -> invalidPosition();
            case Self_Hurt -> selfHurt();
            case Invalid_Chat -> invalidChat();
        }
        
        // Auto-disable after executing
        toggle();
    }

    private void disconnect() {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().onDisconnect(new DisconnectS2CPacket(Text.literal("Kicked via Softcore Addon")));
        }
    }

    private void invalidPosition() {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                Double.NaN, 
                Double.NEGATIVE_INFINITY, 
                Double.POSITIVE_INFINITY, 
                false,
                false
            ));
        }
    }

    private void selfHurt() {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(mc.player, mc.player.isSneaking()));
        }
    }

    private void invalidChat() {
        ChatUtils.sendPlayerMsg("§0§1§");
    }

    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}
