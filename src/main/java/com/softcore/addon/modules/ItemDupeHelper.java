package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class ItemDupeHelper extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgActions = settings.createGroup("Actions");

    private final Setting<String> targetInventory = sgGeneral.add(new StringSetting.Builder()
        .name("target-inventory")
        .description("Target inventory title (e.g., 'Death Chest', 'Backpack').")
        .defaultValue("Death Chest")
        .build()
    );

    private final Setting<List<Item>> targetItems = sgGeneral.add(new ItemListSetting.Builder()
        .name("target-items")
        .description("Items to track and help duplicate.")
        .defaultValue(new ArrayList<>())
        .build()
    );

    private final Setting<Boolean> trackAllItems = sgGeneral.add(new BoolSetting.Builder()
        .name("track-all-items")
        .description("Track all items instead of specific ones.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoTakeItems = sgActions.add(new BoolSetting.Builder()
        .name("auto-take-items")
        .description("Automatically take target items from opened inventory.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> takeDelay = sgActions.add(new IntSetting.Builder()
        .name("take-delay")
        .description("Delay in ticks between taking items.")
        .defaultValue(2)
        .min(0)
        .max(20)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> notifyOnOpen = sgActions.add(new BoolSetting.Builder()
        .name("notify-on-open")
        .description("Notify when target inventory is opened.")
        .defaultValue(true)
        .build()
    );

    private GenericContainerScreen currentScreen = null;
    private int tickCounter = 0;
    private List<Integer> itemsToTake = new ArrayList<>();
    private int itemCountBefore = 0;

    public ItemDupeHelper() {
        super(SoftcoreAddon.CATEGORY, "item-dupe-helper", 
            "Helps automate item duplication processes. Tracks items and can auto-take from inventories.");
    }

    @Override
    public void onActivate() {
        currentScreen = null;
        tickCounter = 0;
        itemsToTake.clear();
        itemCountBefore = countTargetItems();
        info("Item dupe helper activated. Current item count: " + itemCountBefore);
    }

    @Override
    public void onDeactivate() {
        int itemCountAfter = countTargetItems();
        int difference = itemCountAfter - itemCountBefore;
        
        if (difference > 0) {
            info("§aGained " + difference + " items!");
        } else if (difference < 0) {
            info("§cLost " + Math.abs(difference) + " items!");
        } else {
            info("No change in item count.");
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!isActive()) return;

        if (event.screen instanceof GenericContainerScreen) {
            GenericContainerScreen screen = (GenericContainerScreen) event.screen;
            String title = screen.getTitle().getString();
            String target = targetInventory.get();

            if (title.toLowerCase().contains(target.toLowerCase())) {
                currentScreen = screen;
                tickCounter = 0;
                
                if (notifyOnOpen.get()) {
                    ChatUtils.info("§eTarget inventory opened: §f" + title);
                }

                if (autoTakeItems.get()) {
                    scheduleItemTaking();
                }
            }
        } else {
            currentScreen = null;
            itemsToTake.clear();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (currentScreen == null || !autoTakeItems.get()) return;

        tickCounter++;
        
        if (tickCounter >= takeDelay.get() && !itemsToTake.isEmpty()) {
            int slot = itemsToTake.remove(0);
            takeItemFromSlot(slot);
            tickCounter = 0;
            
            if (itemsToTake.isEmpty()) {
                ChatUtils.info("§aFinished taking items!");
            }
        }
    }

    private void scheduleItemTaking() {
        itemsToTake.clear();
        
        if (currentScreen == null || currentScreen.getScreenHandler() == null) return;

        // Scan top inventory slots
        int slots = currentScreen.getScreenHandler().slots.size();
        int topInvSize = slots - 36; // Subtract player inventory size

        for (int i = 0; i < topInvSize; i++) {
            ItemStack stack = currentScreen.getScreenHandler().getSlot(i).getStack();
            
            if (!stack.isEmpty() && shouldTakeItem(stack)) {
                itemsToTake.add(i);
            }
        }

        if (!itemsToTake.isEmpty()) {
            ChatUtils.info("§eFound " + itemsToTake.size() + " items to take.");
        }
    }

    private boolean shouldTakeItem(ItemStack stack) {
        if (trackAllItems.get()) {
            return true;
        }

        return targetItems.get().contains(stack.getItem());
    }

    private void takeItemFromSlot(int slot) {
        if (currentScreen == null || mc.interactionManager == null) return;

        mc.interactionManager.clickSlot(
            currentScreen.getScreenHandler().syncId,
            slot,
            0,
            SlotActionType.QUICK_MOVE,
            mc.player
        );
    }

    private int countTargetItems() {
        if (mc.player == null) return 0;

        int count = 0;
        
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            
            if (!stack.isEmpty() && (trackAllItems.get() || targetItems.get().contains(stack.getItem()))) {
                count += stack.getCount();
            }
        }

        return count;
    }
}
