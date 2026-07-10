package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class PacketDelay extends Module {
    SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final Queue<Packet<?>> packets = new LinkedList<>();
    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("packets")
        .description("Client-to-server packets to delay.")
        .filter((aClass) -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    private final Setting<Boolean> logPacketNames = sgGeneral.add(new BoolSetting.Builder()
        .name("log-packets-on-delay")
        .description("Log the names of packets when delayed")
        .defaultValue(false)
        .build()
    );

    public PacketDelay() {
        super(SoftcoreAddon.CATEGORY, "packet-delay", "Delays selected packets for exploits.");
    }


    @Override
    public void onDeactivate() {
        int i = packets.size();
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            mc.getNetworkHandler().sendPacket(packet);
        }
        ChatUtils.info("Sent %d Packets!", i);
    }

    @Override
    public void onActivate() {
        packets.clear();
    }

    @EventHandler(priority = 999)
    private void onPacket(PacketEvent.Send event) {
        @SuppressWarnings("unchecked")
        Class<? extends Packet<?>> clazz = (Class<? extends Packet<?>>) event.packet.getClass();
        if (c2sPackets.get().contains(clazz)) {
            packets.add(event.packet);
            event.cancel();
            if(logPacketNames.get())
                ChatUtils.info("Delaying Packet: " + event.packet.getPacketType());
        }

    }
}
