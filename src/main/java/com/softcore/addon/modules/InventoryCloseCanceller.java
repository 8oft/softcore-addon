package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class InventoryCloseCanceller extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> blockClosePackets = sgGeneral.add(new BoolSetting.Builder()
        .name("block-close-packets")
        .description("Prevents sending inventory close packets to the server.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> notifyOnBlock = sgGeneral.add(new BoolSetting.Builder()
        .name("notify-on-block")
        .description("Sends a chat message when a close packet is blocked.")
        .defaultValue(true)
        .build()
    );

    private int blockedPackets = 0;

    public InventoryCloseCanceller() {
        super(SoftcoreAddon.CATEGORY, "inventory-close-canceller", 
            "Blocks inventory close packets to test server-side dupe exploits (Death Chest/Backpack).");
    }

    @Override
    public void onActivate() {
        blockedPackets = 0;
        info("Inventory close canceller activated. Blocked packets: 0");
    }

    @Override
    public void onDeactivate() {
        info("Inventory close canceller deactivated. Total blocked: " + blockedPackets);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof CloseHandledScreenC2SPacket && blockClosePackets.get()) {
            event.cancel();
            blockedPackets++;
            
            if (notifyOnBlock.get()) {
                ChatUtils.info("Blocked inventory close packet! Total: " + blockedPackets);
            }
        }
    }
}
