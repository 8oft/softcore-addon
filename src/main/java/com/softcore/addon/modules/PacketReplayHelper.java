package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

public class PacketReplayHelper extends Module {
    private static ClickSlotC2SPacket lastClickSlotPacketStatic = null;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> notifyOnReplay = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-on-replay")
        .description("Notify when a packet is replayed.")
        .defaultValue(true)
        .build()
    );

    private ClickSlotC2SPacket lastClickSlotPacket = null;

    public PacketReplayHelper() {
        super(SoftcoreAddon.CATEGORY, "packet-replay-helper",
            "Records and replays the last ClickSlot (inventory transaction) packet sent. Use to test duplicate transaction exploits.");
    }

    public static void replayLastPacketStatic() {
        if (lastClickSlotPacketStatic != null && meteordevelopment.meteorclient.MeteorClient.mc.getNetworkHandler() != null) {
            meteordevelopment.meteorclient.MeteorClient.mc.getNetworkHandler().sendPacket(lastClickSlotPacketStatic);
        } else {
            ChatUtils.info("§cNo ClickSlot packet recorded yet.");
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof ClickSlotC2SPacket) {
            lastClickSlotPacket = (ClickSlotC2SPacket) event.packet;
            lastClickSlotPacketStatic = lastClickSlotPacket;
            if (notifyOnReplay.get()) {
                ChatUtils.info("§eRecorded ClickSlot packet.");
            }
        }
    }

    public void replayLastPacket() {
        if (lastClickSlotPacket != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(lastClickSlotPacket);
            if (notifyOnReplay.get()) {
                ChatUtils.info("§aReplayed last ClickSlot packet!");
            }
        } else {
            ChatUtils.info("§cNo ClickSlot packet recorded yet.");
        }
    }

    @Override
    public void onActivate() {
        ChatUtils.info("§ePacket Replay Helper activated. Use .replaypacket command to resend last ClickSlot packet.");
    }
}
