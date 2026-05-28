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

    private final Setting<Integer> packetRepeat = sgGeneral.add(new IntSetting.Builder()
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
        super(SoftcoreAddon.CATEGORY, "vaults-plugin-dupe", "Vaults Plugin Dupe - Auto-loot items from the Vaults plugin.");
    }

    @Override
    public void onActivate() {
        lastTitle = "";
        lootedTitles.clear();
        info("Vaults Plugin Dupe activated. Open a Vault GUI to start looting.");
    }

    @Override
    public void onDeactivate() {
        info("Vaults Plugin Dupe deactivated.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        String title = screen.getTitle().getString();

        // Only process Vault GUIs
        if (!title.toLowerCase().contains("vault")) return;

        int slots = screen.getScreenHandler().getRows() * 9;
        int nextSlot = slots - 1;   // slot 53 for 54-slot
        int prevSlot = slots - 9;   // slot 45 for 54-slot

        // Detect page by checking nav arrows
        boolean hasNextArrow = !screen.getScreenHandler().getSlot(nextSlot).getStack().isEmpty();
        boolean hasPrevArrow = !screen.getScreenHandler().getSlot(prevSlot).getStack().isEmpty();

        int pageNum = 1;
        if (hasPrevArrow) pageNum = 2; // or higher if we track state
        if (hasNextArrow && hasPrevArrow) pageNum = 2; // middle page

        // Skip if we already looted this exact title
        if (title.equals(lastTitle)) return;
        if (lootedTitles.contains(title)) return;

        lastTitle = title;
        lootedTitles.add(title);

        info("Detected Vault: " + title + " (slots=" + slots + ", page=" + pageNum + ", next=" + hasNextArrow + ", prev=" + hasPrevArrow + ")");

        // ---- SAME TICK: Loot all non-empty slots ----
        int lootedCount = 0;
        for (int slot = 0; slot < slots; slot++) {
            // Skip nav slots
            if (slot == nextSlot || slot == prevSlot) continue;

            // Only quick-move if slot has items
            ItemStack stack = screen.getScreenHandler().getSlot(slot).getStack();
            if (!stack.isEmpty()) {
                quickMoveSlot(screen, slot);
                lootedCount++;
            }
        }
        info("Quick-moved " + lootedCount + " items on same tick");

        // ---- SAME TICK: Always go to next page if arrow exists ----
        if (hasNextArrow) {
            info("Clicking next page (slot " + nextSlot + ") on same tick");
            clickSlot(screen, nextSlot);
            lastTitle = "";
            return;
        }

        // No more pages
        info("All vault pages looted.");
        toggle();
    }

    private void quickMoveSlot(GenericContainerScreen screen, int slotId) {
        if (mc.interactionManager == null || mc.player == null) return;

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
        if (mc.interactionManager == null || mc.player == null) return;

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
        return "Vaults Dupe";
    }
}
