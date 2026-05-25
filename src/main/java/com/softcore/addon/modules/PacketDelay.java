package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.Packet;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class PacketDelay extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Queue<Packet<?>> packets = new LinkedList<>();
    
    private final Setting<Set<Class<? extends Packet<?>>>> c2sPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("packets")
        .description("Client-to-server packets to delay.")
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
        int count = packets.size();
        
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(packet);
            }
        }
        
        if (count > 0) {
            ChatUtils.info("Sent %d delayed packets!", count);
        }
    }

    @Override
    public void onActivate() {
        packets.clear();
    }

    @EventHandler(priority = 999)
    private void onPacketSend(PacketEvent.Send event) {
        @SuppressWarnings("unchecked")
        Class<? extends Packet<?>> clazz = (Class<? extends Packet<?>>) event.packet.getClass();
        if (c2sPackets.get().contains(clazz)) {
            packets.add(event.packet);
            event.cancel();
            
            if (logPacketNames.get()) {
                ChatUtils.info("Delaying packet: " + clazz.getSimpleName());
            }
        }
    }
}
