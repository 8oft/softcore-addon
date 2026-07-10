package com.softcore.addon.modules.dupes;

import com.softcore.addon.SoftcoreAddon;
import com.softcore.addon.utils.timer.MsTimer;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.Hand;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.concurrent.atomic.AtomicInteger;

public class SoftCloseBackpack extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum TransferMode {
        QuickMove,
        Pickup
    }

    private final Setting<TransferMode> mode = sgGeneral.add(new EnumSetting.Builder<TransferMode>()
        .name("mode")
        .description("Item transfer mode for dump and steal")
        .defaultValue(TransferMode.QuickMove)
        .build()
    );

    private final Setting<Integer> clickDelay = sgGeneral.add(new IntSetting.Builder()
        .name("click-delay")
        .description("Delay in ms between each item transfer click")
        .defaultValue(100)
        .min(0)
        .max(5000)
        .sliderMax(1000)
        .build()
    );

    private final Setting<Integer> actionDelay = sgGeneral.add(new IntSetting.Builder()
        .name("action-delay")
        .description("Delay in ms between each phase (open, close, steal, etc.)")
        .defaultValue(800)
        .min(50)
        .max(10000)
        .sliderMax(3000)
        .build()
    );

    public enum InteractMode {
        RightClick,
        ShiftRightClick,
        InventoryRightClick
    }

    private final Setting<InteractMode> interactMode = sgGeneral.add(new EnumSetting.Builder<InteractMode>()
        .name("interact-mode")
        .description("How to interact with the backpack/shulker in hand")
        .defaultValue(InteractMode.ShiftRightClick)
        .build()
    );

    private final Setting<Boolean> repeat = sgGeneral.add(new BoolSetting.Builder()
        .name("repeat")
        .description("Repeat until inventory is nearly full (only 1 empty main slot left)")
        .defaultValue(false)
        .build()
    );

    private volatile boolean running = false;

    public SoftCloseBackpack() {
        super(SoftcoreAddon.AUTO_DUPE_CATEGORY, "soft-close-backpack", "Auto-dupe cycle using a backpack/shulker in hand (requires server-side plugin)");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) { toggle(); return; }
        running = true;
        info("Starting backpack dupe cycle");
        mc.execute(this::openThenDump);
    }

    @Override
    public void onDeactivate() {
        running = false;
        mc.options.sneakKey.setPressed(false);
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        toggle();
    }

    // --- Step 1: interact → dump ---
    private void openThenDump() {
        if (!running) return;
        info("Opening backpack...");
        pressSneak();
        sendInteract();

        schedule(() -> {
            if (!running) return;
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                error("Backpack GUI did not open — plugin may not be installed");
                toggle();
                return;
            }
            releaseSneak();
            info("Dumping items...");
            execDump(() -> {
                if (!running) return;
                closeThenReopen();
            });
        }, actionDelay.get());
    }

    // --- Step 2: close → interact → steal ---
    private void closeThenReopen() {
        if (!running) return;
        closeNormal();
        info("Backpack closed, reopening...");
        if (interactMode.get() != InteractMode.ShiftRightClick) {
            releaseSneak();
        }
        pressSneak();
        // Wait for close to process, interact, then wait for GUI
        schedule(() -> {
            if (!running) return;
            sendInteract();
            schedule(() -> {
                if (!running) return;
                if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                    error("Backpack did not reopen");
                    toggle();
                    return;
                }
                releaseSneak();
                info("Stealing items (1st)...");
                execSteal(() -> {
                    if (!running) return;
                    softOpenThenSteal();
                });
            }, actionDelay.get());
        }, actionDelay.get());
    }

    // --- Step 3: interact (no close) → steal again ---
    private void softOpenThenSteal() {
        if (!running) return;
        pressSneak();
        sendInteract();
        info("Soft-opening backpack...");

        schedule(() -> {
            if (!running) return;
            releaseSneak();
            info("Stealing items (2nd)...");
            execSteal(() -> {
                if (!running) return;
                finalClose();
            });
        }, actionDelay.get());
    }

    // --- Step 4: final close ---
    private void finalClose() {
        if (!running) return;
        mc.setScreen(null);
        if (repeat.get() && hasInventorySpace()) {
            info("Repeating...");
            schedule(this::openThenDump, actionDelay.get());
        } else {
            if (repeat.get()) info("Inventory full, stopping");
            else info("Done!");
            toggle();
        }
    }

    private boolean hasInventorySpace() {
        if (mc.player == null) return false;
        var inv = mc.player.getInventory();
        int empty = 0;
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isEmpty()) empty++;
        }
        return empty > 1;
    }

    // --- Dump all inv items → container ---
    private void execDump(Runnable onDone) {
        if (!running || mc.player == null) return;
        var handler = mc.player.currentScreenHandler;
        if (handler == null) { toggle(); return; }
        int totalSlots = handler.slots.size();
        int containerSlots = totalSlots - 36;
        if (containerSlots <= 0) { toggle(); return; }

        SlotActionType action = mode.get() == TransferMode.QuickMove ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
        int syncId = handler.syncId;
        AtomicInteger offset = new AtomicInteger(0);
        int delayMs = clickDelay.get();

        for (int i = 0; i < 36; i++) {
            int slot = containerSlots + i;
            if (handler.getSlot(slot).getStack().isEmpty()) continue;
            int s = slot;
            int cur = offset.getAndAdd(delayMs);

            if (action == SlotActionType.PICKUP) {
                MsTimer.schedule(() -> sendPacket(syncId, s, SlotActionType.PICKUP), cur);
                MsTimer.schedule(() -> sendPacket(syncId, 0, SlotActionType.PICKUP), cur + delayMs / 2);
            } else {
                MsTimer.schedule(() -> sendPacket(syncId, s, action), cur);
            }
        }

        schedule(onDone, offset.get() + delayMs + 200);
    }

    // --- Steal all items from container → inv ---
    private void execSteal(Runnable onDone) {
        if (!running || mc.player == null) return;
        var handler = mc.player.currentScreenHandler;
        if (handler == null) { toggle(); return; }
        int totalSlots = handler.slots.size();
        int containerSlots = totalSlots - 36;
        if (containerSlots <= 0) { toggle(); return; }

        SlotActionType action = mode.get() == TransferMode.QuickMove ? SlotActionType.QUICK_MOVE : SlotActionType.PICKUP;
        int syncId = handler.syncId;
        AtomicInteger offset = new AtomicInteger(0);
        int delayMs = clickDelay.get();

        for (int slot = 0; slot < containerSlots; slot++) {
            if (handler.getSlot(slot).getStack().isEmpty()) continue;
            int s = slot;
            int cur = offset.getAndAdd(delayMs);

            if (action == SlotActionType.PICKUP) {
                MsTimer.schedule(() -> sendPacket(syncId, s, SlotActionType.PICKUP), cur);
                MsTimer.schedule(() -> sendPacket(syncId, containerSlots, SlotActionType.PICKUP), cur + delayMs / 2);
            } else {
                MsTimer.schedule(() -> sendPacket(syncId, s, action), cur);
            }
        }

        schedule(onDone, offset.get() + delayMs + 200);
    }

    // --- helpers ---
    private void sendPacket(int syncId, int slot, SlotActionType action) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(
            syncId, 0, (short) slot, (byte) 0, action,
            new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
        ));
    }

    private void sendInteract() {
        if (mc.player == null || mc.interactionManager == null) return;

        if (interactMode.get() == InteractMode.InventoryRightClick) {
            mc.setScreen(new InventoryScreen(mc.player));
        }
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }

    private void pressSneak() {
        if (interactMode.get() == InteractMode.ShiftRightClick) {
            mc.options.sneakKey.setPressed(true);
        }
    }

    private void releaseSneak() {
        if (interactMode.get() == InteractMode.ShiftRightClick) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    private void closeNormal() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.currentScreenHandler == null) return;
        mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        mc.player.closeHandledScreen();
        mc.setScreen(null);
    }

    private void schedule(Runnable task, long delayMs) {
        MsTimer.schedule(() -> mc.execute(task), delayMs);
    }
}
