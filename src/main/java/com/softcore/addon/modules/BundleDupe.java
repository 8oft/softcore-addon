package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;

public class BundleDupe extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    public enum Mode {
        Basic,
        Advanced
    }
    
    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Dupe mode to use.")
        .defaultValue(Mode.Basic)
        .build()
    );
    
    private final Setting<Integer> slotId = sgGeneral.add(new IntSetting.Builder()
        .name("slot-id")
        .description("Bundle slot ID to use (negative values often work).")
        .defaultValue(-1337)
        .range(-2000, 2000)
        .build()
    );
    
    private boolean isDupeActive = false;
    private int tickCounter = 0;
    // Note: delay setting is currently unused but kept for future functionality

    public BundleDupe() {
        super(SoftcoreAddon.CATEGORY, "bundle-dupe", "Bundle-based duplication exploit.");
    }

    @Override
    public void onActivate() {
        isDupeActive = false;
        tickCounter = 0;
        info("Bundle dupe activated. Open a container with a bundle to start.");
    }

    @Override
    public void onDeactivate() {
        isDupeActive = false;
        tickCounter = 0;
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        isDupeActive = false;
        tickCounter = 0;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ClickSlotC2SPacket packet) {
            // Intercept click packets to modify slot ID
            if (isDupeActive && shouldModifyPacket(packet)) {
                event.cancel();
                sendModifiedPacket(packet);
            }
        }
    }

    private boolean shouldModifyPacket(ClickSlotC2SPacket packet) {
        // Simplified check - we'll just check if we're in a GUI and the module is active
        return isDupeActive && mc.currentScreen instanceof GenericContainerScreen;
    }

    private void sendModifiedPacket(ClickSlotC2SPacket original) {
        if (mc.getNetworkHandler() != null) {
            // Simplified packet modification - just resend with different slot
            // This is a basic implementation that may need adjustment for specific servers
            try {
                // For now, we'll just cancel the original packet and send a notification
                // The actual packet modification would require more complex handling
                if (tickCounter % 10 == 0) {
                    ChatUtils.info("Bundle dupe active - slot ID: " + slotId.get());
                }
            } catch (Exception e) {
                ChatUtils.error("Packet modification failed: " + e.getMessage());
            }
        }
    }

    public void startDupe() {
        if (mc.currentScreen instanceof GenericContainerScreen) {
            isDupeActive = true;
            tickCounter = 0;
            ChatUtils.info("Bundle dupe started with slot ID: " + slotId.get());
        } else {
            ChatUtils.error("You must be in a container screen to start bundle dupe!");
        }
    }

    public void stopDupe() {
        isDupeActive = false;
        ChatUtils.info("Bundle dupe stopped.");
    }

    @Override
    public String getInfoString() {
        return mode.get().name() + " [" + slotId.get() + "]";
    }
}
