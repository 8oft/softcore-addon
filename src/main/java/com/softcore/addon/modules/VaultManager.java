package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.Set;

public class VaultManager extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> packetRepeat = sgGeneral.add(new IntSetting.Builder()
        .name("packet-repeat")
        .description("How many times to send each QUICK_MOVE packet per slot.")
        .defaultValue(5)
        .min(1)
        .max(67)
        .build()
    );

    private String lastTitle = "";
    private Set<String> lootedTitles = new HashSet<>();

    public VaultManager() {
        super(SoftcoreAddon.CATEGORY, "vaults-plugin-dupe", "Vaults Plugin Dupe - Toggle on, open vault, auto-loot.");
    }

    @Override
    public void onActivate() {
        lastTitle = "";
        lootedTitles.clear();
        info("Vaults Plugin Dupe activated. Open a Vault GUI to auto-loot.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        String title = screen.getTitle().getString();
        if (!title.toLowerCase().contains("vault")) return;

        int slots = screen.getScreenHandler().getRows() * 9;
        int nextSlot = slots - 1;
        int prevSlot = slots - 9;

        boolean hasNextArrow = !screen.getScreenHandler().getSlot(nextSlot).getStack().isEmpty();

        if (title.equals(lastTitle)) return;
        if (lootedTitles.contains(title)) return;

        lastTitle = title;
        lootedTitles.add(title);

        info("Looting Vault: " + title);

        int lootedCount = 0;
        for (int slot = 0; slot < slots; slot++) {
            if (slot == nextSlot || slot == prevSlot) continue;

            ItemStack stack = screen.getScreenHandler().getSlot(slot).getStack();
            if (!stack.isEmpty()) {
                quickMoveSlot(screen, slot);
                lootedCount++;
            }
        }
        info("Quick-moved " + lootedCount + " items");

        if (hasNextArrow) {
            clickSlot(screen, nextSlot);
            lastTitle = "";
            info("Going to next page...");
        } else {
            info("All vault pages looted.");
            toggle();
        }
    }

    private void quickMoveSlot(GenericContainerScreen screen, int slotId) {
        var handler = screen.getScreenHandler();
        int repeats = packetRepeat.get();
        for (int i = 0; i < repeats; i++) {
            mc.interactionManager.clickSlot(
                handler.syncId,
                slotId,
                0,
                SlotActionType.QUICK_MOVE,
                mc.player
            );
        }
    }

    private void clickSlot(GenericContainerScreen screen, int slotId) {
        var handler = screen.getScreenHandler();
        mc.interactionManager.clickSlot(
            handler.syncId,
            slotId,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
    }

    @Override
    public String getInfoString() {
        return isActive() ? "Active" : "Inactive";
    }
}
