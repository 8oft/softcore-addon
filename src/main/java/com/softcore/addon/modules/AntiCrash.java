package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;

public class AntiCrash extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> log = sgGeneral.add(new BoolSetting.Builder()
        .name("log")
        .description("Logs when crash packet is detected.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> blockExploits = sgGeneral.add(new BoolSetting.Builder()
        .name("block-exploits")
        .description("Blocks known crash exploits.")
        .defaultValue(true)
        .build()
    );

    public AntiCrash() {
        super(SoftcoreAddon.CATEGORY, "anti-crash", "Prevents client crashes from malicious packets.");
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (!blockExploits.get()) return;

        if (event.packet instanceof ExplosionS2CPacket) {
            // Block all explosion packets as safety measure
            cancelEvent(event, "Explosion packet (potential crash)");
        } else if (event.packet instanceof ParticleS2CPacket packet) {
            if (packet.getCount() > 100_000) {
                cancelEvent(event, "Excessive particle count: " + packet.getCount());
            }
        }
    }

    private void cancelEvent(PacketEvent.Receive event, String reason) {
        if (log.get()) {
            warning("Blocked crash attempt: " + reason);
        }
        event.cancel();
    }
}
