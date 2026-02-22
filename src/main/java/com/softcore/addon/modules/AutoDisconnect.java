package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

public class AutoDisconnect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTriggers = settings.createGroup("Triggers");

    private final Setting<Integer> delayTicks = sgGeneral.add(new IntSetting.Builder()
        .name("delay-ticks")
        .description("Delay in ticks before disconnecting.")
        .defaultValue(0)
        .min(0)
        .max(200)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> onInventoryOpen = sgTriggers.add(new BoolSetting.Builder()
        .name("on-inventory-open")
        .description("Disconnect when opening an inventory.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onDeath = sgTriggers.add(new BoolSetting.Builder()
        .name("on-death")
        .description("Disconnect immediately on death.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> inventoryTitle = sgTriggers.add(new StringSetting.Builder()
        .name("inventory-title")
        .description("Disconnect when inventory with this title opens (case-insensitive, partial match).")
        .defaultValue("Death Chest")
        .build()
    );

    private final Setting<Boolean> useManualTrigger = sgTriggers.add(new BoolSetting.Builder()
        .name("manual-trigger")
        .description("Disconnect on next tick when this is enabled.")
        .defaultValue(false)
        .onChanged(val -> {
            if (val && isActive()) {
                scheduleDisconnect();
            }
        })
        .build()
    );

    private int disconnectTimer = -1;
    private boolean shouldDisconnect = false;

    public AutoDisconnect() {
        super(SoftcoreAddon.CATEGORY, "auto-disconnect", 
            "Automatically disconnects when specific conditions are met. Useful for packet-based dupes.");
    }

    @Override
    public void onActivate() {
        disconnectTimer = -1;
        shouldDisconnect = false;
        info("Auto-disconnect activated");
    }

    @Override
    public void onDeactivate() {
        disconnectTimer = -1;
        shouldDisconnect = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (shouldDisconnect) {
            if (disconnectTimer > 0) {
                disconnectTimer--;
                return;
            }
            
            disconnect();
            shouldDisconnect = false;
            disconnectTimer = -1;
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!isActive()) return;

        // Check for death screen
        if (onDeath.get() && event.screen instanceof DeathScreen) {
            ChatUtils.warning("Death detected! Disconnecting...");
            scheduleDisconnect();
            return;
        }

        // Check for specific inventory
        if (onInventoryOpen.get() && event.screen instanceof GenericContainerScreen) {
            GenericContainerScreen screen = (GenericContainerScreen) event.screen;
            String title = screen.getTitle().getString().toLowerCase();
            String targetTitle = inventoryTitle.get().toLowerCase();
            
            if (title.contains(targetTitle)) {
                ChatUtils.warning("Target inventory opened: " + screen.getTitle().getString() + ". Disconnecting...");
                scheduleDisconnect();
            }
        }
    }

    private void scheduleDisconnect() {
        shouldDisconnect = true;
        disconnectTimer = delayTicks.get();
        
        if (disconnectTimer > 0) {
            ChatUtils.info("Disconnecting in " + disconnectTimer + " ticks...");
        }
    }

    private void disconnect() {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().onDisconnect(
                new DisconnectS2CPacket(Text.literal("Auto-Disconnect triggered"))
            );
            ChatUtils.info("Disconnected!");
        }
    }
}
