package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;

public class VaultManager extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoNextPage = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-next-page")
        .description("Automatically go to the next page after looting.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoPrevPage = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-prev-page")
        .description("Automatically go to the previous page after looting.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between clicks in ticks.")
        .defaultValue(1)
        .min(0)
        .max(20)
        .build()
    );

    private final Setting<Integer> pageDelay = sgGeneral.add(new IntSetting.Builder()
        .name("page-delay")
        .description("Delay between page switches in ticks.")
        .defaultValue(5)
        .min(1)
        .max(40)
        .build()
    );

    private int tickCounter = 0;
    private int currentSlot = 0;
    private boolean isLooting = false;
    private boolean waitingForPageSwitch = false;
    private int pageSwitchCooldown = 0;
    private String lastTitle = "";
    private Map<String, Boolean> lootedPages = new HashMap<>();

    public VaultManager() {
        super(SoftcoreAddon.CATEGORY, "vault-manager", "Automatically loot items from Vault plugin GUIs.");
    }

    @Override
    public void onActivate() {
        tickCounter = 0;
        currentSlot = 0;
        isLooting = false;
        waitingForPageSwitch = false;
        pageSwitchCooldown = 0;
        lastTitle = "";
        lootedPages.clear();
        info("VaultManager activated. Open a Vault GUI to start looting.");
    }

    @Override
    public void onDeactivate() {
        isLooting = false;
        waitingForPageSwitch = false;
        info("VaultManager deactivated.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();

            // Check if this is a Vault GUI
            if (!title.toLowerCase().contains("vault")) {
                // Not a vault screen, reset state
                if (isLooting) {
                    isLooting = false;
                    waitingForPageSwitch = false;
                }
                return;
            }

            int rows = screen.getScreenHandler().getRows();
            int slots = rows * 9;
            int nextSlot = slots - 1;
            int prevSlot = slots - 9;

            // If we just switched pages, reset slot counter
            if (!title.equals(lastTitle)) {
                lastTitle = title;
                currentSlot = 0;
                isLooting = true;
                waitingForPageSwitch = false;
                pageSwitchCooldown = 0;
                info("Detected Vault: " + title + " (" + slots + " slots)");
            }

            // Handle page switch cooldown
            if (waitingForPageSwitch) {
                pageSwitchCooldown--;
                if (pageSwitchCooldown <= 0) {
                    waitingForPageSwitch = false;
                    currentSlot = 0;
                } else {
                    return;
                }
            }

            // Main looting logic
            if (isLooting) {
                tickCounter++;
                if (tickCounter < delay.get()) return;
                tickCounter = 0;

                // Find next slot to loot
                while (currentSlot < slots) {
                    // Skip navigation slots
                    if (currentSlot == nextSlot || currentSlot == prevSlot) {
                        currentSlot++;
                        continue;
                    }

                    // Check if slot has an item
                    ItemStack stack = screen.getScreenHandler().getSlot(currentSlot).getStack();
                    if (!stack.isEmpty()) {
                        // Quick move this slot to inventory
                        quickMoveSlot(screen, currentSlot);
                        currentSlot++;
                        return; // Wait for next tick
                    }

                    currentSlot++;
                }

                // All items on this page have been looted
                info("Page looted: " + title);
                lootedPages.put(title, true);

                // Try to go to next page
                if (autoNextPage.get()) {
                    ItemStack nextArrow = screen.getScreenHandler().getSlot(nextSlot).getStack();
                    if (!nextArrow.isEmpty()) {
                        info("Going to next page...");
                        clickSlot(screen, nextSlot);
                        waitingForPageSwitch = true;
                        pageSwitchCooldown = pageDelay.get();
                        return;
                    }
                }

                // Try to go to previous page
                if (autoPrevPage.get()) {
                    ItemStack prevArrow = screen.getScreenHandler().getSlot(prevSlot).getStack();
                    if (!prevArrow.isEmpty()) {
                        info("Going to previous page...");
                        clickSlot(screen, prevSlot);
                        waitingForPageSwitch = true;
                        pageSwitchCooldown = pageDelay.get();
                        return;
                    }
                }

                // No more pages
                info("All pages looted.");
                toggle();
            } else if (!lootedPages.containsKey(title)) {
                // Start looting this vault
                isLooting = true;
                currentSlot = 0;
                tickCounter = 0;
                info("Starting to loot: " + title);
            }
        } else {
            // Not in any GUI
            if (isLooting) {
                isLooting = false;
                waitingForPageSwitch = false;
            }
        }
    }

    private void quickMoveSlot(GenericContainerScreen screen, int slotId) {
        if (mc.interactionManager == null) return;

        var handler = screen.getScreenHandler();
        mc.interactionManager.clickSlot(
            handler.syncId,
            slotId,
            0,
            SlotActionType.QUICK_MOVE,
            mc.player
        );
    }

    private void clickSlot(GenericContainerScreen screen, int slotId) {
        if (mc.interactionManager == null) return;

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
        if (isLooting) {
            return "Looting slot " + currentSlot;
        }
        return "Idle";
    }
}
