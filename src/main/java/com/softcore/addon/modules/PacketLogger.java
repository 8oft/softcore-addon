package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;

public class PacketLogger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> logInventoryPackets = sgGeneral.add(new BoolSetting.Builder()
        .name("log-inventory")
        .description("Log inventory-related packets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logClosePackets = sgGeneral.add(new BoolSetting.Builder()
        .name("log-close")
        .description("Log inventory close packets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logClickPackets = sgGeneral.add(new BoolSetting.Builder()
        .name("log-click")
        .description("Log inventory click packets.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> logOpenPackets = sgGeneral.add(new BoolSetting.Builder()
        .name("log-open")
        .description("Log inventory open packets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> logUpdatePackets = sgGeneral.add(new BoolSetting.Builder()
        .name("log-update")
        .description("Log inventory update packets.")
        .defaultValue(false)
        .build()
    );

    public PacketLogger() {
        super(SoftcoreAddon.CATEGORY, "packet-logger", 
            "Logs inventory-related packets for debugging dupe exploits.");
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof CloseHandledScreenC2SPacket && logClosePackets.get()) {
            ChatUtils.info("§c[SEND] §eCloseHandledScreen §7- Inventory closed!");
        }
        
        if (event.packet instanceof ClickSlotC2SPacket && logClickPackets.get()) {
            ChatUtils.info("§c[SEND] §eClickSlot §7- Inventory clicked");
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (event.packet instanceof OpenScreenS2CPacket && logOpenPackets.get()) {
            ChatUtils.info("§a[RECV] §eOpenScreen §7- Inventory opened");
        }
        
        if (event.packet instanceof ScreenHandlerSlotUpdateS2CPacket && logUpdatePackets.get()) {
            ChatUtils.info("§a[RECV] §eSlotUpdate §7- Inventory slot updated");
        }
        
        if (event.packet instanceof InventoryS2CPacket && logInventoryPackets.get()) {
            ChatUtils.info("§a[RECV] §eInventory §7- Full inventory sync");
        }
    }
}
