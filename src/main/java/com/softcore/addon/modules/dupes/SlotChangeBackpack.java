package com.softcore.addon.modules.dupes;

import com.softcore.addon.SoftcoreAddon;
import com.softcore.addon.utils.timer.MsTimer;
import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;
import net.minecraft.util.Hand;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.concurrent.atomic.AtomicInteger;

public class SlotChangeBackpack extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum TransferMode {
        QuickMove,
        Pickup
    }

    private final Setting<TransferMode> mode = sgGeneral.add(new EnumSetting.Builder<TransferMode>()
        .name("mode")
        .description("Item transfer mode")
        .defaultValue(TransferMode.QuickMove)
        .build()
    );

    public enum DupeMethod {
        Drop,
        Move
    }

    private final Setting<DupeMethod> dupeMethod = sgGeneral.add(new EnumSetting.Builder<DupeMethod>()
        .name("dupe-method")
        .description("Drop backpack on ground or move it to another slot")
        .defaultValue(DupeMethod.Drop)
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
        .description("Delay in ms between each phase")
        .defaultValue(800)
        .min(50)
        .max(10000)
        .sliderMax(3000)
        .build()
    );

    private final Setting<Integer> pickupTimeout = sgGeneral.add(new IntSetting.Builder()
        .name("pickup-timeout")
        .description("Max time in ms to wait for backpack item on ground (Drop mode)")
        .defaultValue(5000)
        .min(1000)
        .max(30000)
        .sliderMax(15000)
        .build()
    );

    public enum InteractMode {
        RightClick,
        ShiftRightClick,
        InventoryRightClick
    }

    private final Setting<InteractMode> interactMode = sgGeneral.add(new EnumSetting.Builder<InteractMode>()
        .name("interact-mode")
        .description("How to interact with the backpack in hand")
        .defaultValue(InteractMode.ShiftRightClick)
        .build()
    );

    public enum SwapTarget {
        Slot1, Slot2, Slot3, Slot4, Slot5, Slot6, Slot7, Slot8,
        Hotbar1, Hotbar2, Hotbar3, Hotbar4, Hotbar5, Hotbar6, Hotbar7, Hotbar8, Hotbar9,
        Offhand
    }

    private final Setting<SwapTarget> swapTarget = sgGeneral.add(new EnumSetting.Builder<SwapTarget>()
        .name("swap-target")
        .description("Where to move the backpack inside the GUI (Move mode)")
        .defaultValue(SwapTarget.Hotbar2)
        .build()
    );

    private final Setting<Boolean> repeat = sgGeneral.add(new BoolSetting.Builder()
        .name("repeat")
        .description("Repeat until inventory is nearly full (only 1 empty main slot left)")
        .defaultValue(false)
        .build()
    );

    private volatile boolean running = false;
    private int originalSlot = -1;

    public SlotChangeBackpack() {
        super(SoftcoreAddon.AUTO_DUPE_CATEGORY, "slot-change-backpack", "Duplicate items by dropping or moving backpack inside its own GUI");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) { toggle(); return; }
        originalSlot = mc.player.getInventory().getSelectedSlot();
        running = true;
        info("Starting gui dupe backpack (%s mode%s)", dupeMethod.get().name(), repeat.get() ? ", repeat on" : "");
        mc.execute(this::openThenDump);
    }

    @Override
    public void onDeactivate() {
        running = false;
        originalSlot = -1;
        mc.options.sneakKey.setPressed(false);
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        toggle();
    }

    // === STEP 1-2: Open backpack → dump items INTO it ===
    private void openThenDump() {
        if (!running) return;
        info("[1/12] Opening backpack...");
        pressSneak();
        sendInteract();

        schedule(() -> {
            if (!running) return;
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                error("Backpack GUI didn't open");
                toggle();
                return;
            }
            releaseSneak();
            info("[2/12] Dumping items into backpack...");
            execDump(() -> {
                if (!running) return;
                closeThenReopen();
            });
        }, actionDelay.get());
    }

    // === STEP 3-6: Close → look down → reopen → take items OUT ===
    private void closeThenReopen() {
        if (!running) return;
        info("[3/12] Closing backpack...");
        closeNormal();

        if (dupeMethod.get() == DupeMethod.Drop) {
            info("[4/12] Looking down...");
            if (mc.player != null) mc.player.setPitch(90);
        }

        info("[5/12] Reopening backpack...");
        if (interactMode.get() != InteractMode.ShiftRightClick) {
            releaseSneak();
        }
        pressSneak();
        // Wait for close to process, then interact, then wait for GUI
        schedule(() -> {
            if (!running) return;
            sendInteract();
            schedule(() -> {
                if (!running) return;
                if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                    error("Backpack didn't reopen");
                    toggle();
                    return;
                }
                releaseSneak();
                info("[6/12] Taking items out of backpack...");
                execSteal(() -> {
                    if (!running) return;
                    if (dupeMethod.get() == DupeMethod.Drop) {
                        dropInGui();
                    } else {
                        moveBackpack();
                    }
                });
            }, actionDelay.get());
        }, actionDelay.get());
    }

    // ==================== DROP BRANCH (Steps 7-12) ====================

    // Step 7: Drop backpack via THROW while GUI is still open
    private void dropInGui() {
        if (!running || mc.player == null || mc.getNetworkHandler() == null) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen)) {
            error("Not in backpack GUI for drop");
            toggle();
            return;
        }

        info("[7/12] Dropping backpack...");

        var handler = mc.player.currentScreenHandler;
        if (handler == null) { toggle(); return; }

        int containerSlots = handler.slots.size() - 36;
        int heldSlot = containerSlots + 27 + mc.player.getInventory().getSelectedSlot();
        int syncId = handler.syncId;

        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(
            syncId, 0, (short) heldSlot, (byte) 1, SlotActionType.THROW,
            new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
        ));

        // Step 8: close AFTER drop so server processes THROW first
        schedule(() -> {
            if (!running) return;
            info("[8/12] Closing after drop...");
            closeNormal();

            schedule(() -> {
                if (!running) return;
                if (mc.player != null && !mc.player.getMainHandStack().isEmpty()) {
                    info("Backpack picked up!");
                    step10();
                } else {
                    schedule(() -> {
                        if (!running) return;
                        step10();
                    }, 1500);
                }
            }, actionDelay.get());
        }, 300);
    }

    // Step 10-12: Reopen → take duped items → finish
    private void step10() {
        if (!running) return;
        info("[10/12] Reopening backpack for duped items...");
        pressSneak();
        sendInteract();

        schedule(() -> {
            if (!running) return;
            if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                // Maybe pickup took a bit, retry
                schedule(() -> {
                    if (!running) return;
                    if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                        error("Couldn't reopen after pickup");
                        toggle();
                        return;
                    }
                    releaseSneak();
                    info("[11/12] Taking duped items...");
                    execSteal(() -> {
                        if (!running) return;
                        finish();
                    });
                }, actionDelay.get());
                return;
            }
            releaseSneak();
            info("[11/12] Taking duped items...");
            execSteal(() -> {
                if (!running) return;
                finish();
            });
        }, actionDelay.get());
    }

    // ==================== MOVE BRANCH ====================

    private void moveBackpack() {
        if (!running) return;
        info("Moving backpack to slot " + swapTarget.get());
        execMoveBackpackInGui(() -> {
            if (!running) return;
            moveBackAndSteal();
        });
    }

    private void moveBackAndSteal() {
        if (!running) return;
        info("Closing after move...");
        closeNormal();
        mc.setScreen(null);
        info("Switching to target hotbar slot...");
        int targetHotbar = swapTargetHotbar();
        if (targetHotbar >= 0 && targetHotbar <= 8 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(targetHotbar);
        }
        if (interactMode.get() != InteractMode.ShiftRightClick) {
            releaseSneak();
        }
        pressSneak();
        schedule(() -> {
            if (!running) return;
            sendInteract();
            schedule(() -> {
                if (!running) return;
                if (!(mc.currentScreen instanceof GenericContainerScreen)) {
                    error("Backpack didn't open after move");
                    toggle();
                    return;
                }
                releaseSneak();
                info("Taking duped items...");
                execSteal(() -> finish());
            }, actionDelay.get());
        }, actionDelay.get());
    }

    private void execMoveBackpackInGui(Runnable onDone) {
        if (!running || mc.player == null) return;
        var handler = mc.player.currentScreenHandler;
        if (handler == null) { toggle(); return; }
        int syncId = handler.syncId;
        int fromSlot = 0;
        int toSlot = swapTargetSlot();
        if (toSlot < 0) { toggle(); return; }
        sendPacket(syncId, fromSlot, SlotActionType.PICKUP);
        schedule(() -> sendPacket(syncId, toSlot, SlotActionType.PICKUP), 50);
        schedule(onDone, 200);
    }

    private int swapTargetHotbar() {
        return switch (swapTarget.get()) {
            case Hotbar1, Slot1 -> 0;
            case Hotbar2, Slot2 -> 1;
            case Hotbar3, Slot3 -> 2;
            case Hotbar4, Slot4 -> 3;
            case Hotbar5, Slot5 -> 4;
            case Hotbar6, Slot6 -> 5;
            case Hotbar7, Slot7 -> 6;
            case Hotbar8, Slot8 -> 7;
            case Hotbar9 -> 8;
            case Offhand -> originalSlot;
        };
    }

    private int swapTargetSlot() {
        int containerSlots = 0;
        if (mc.player != null && mc.player.currentScreenHandler != null) {
            containerSlots = mc.player.currentScreenHandler.slots.size() - 36;
        }
        return switch (swapTarget.get()) {
            case Slot1 -> 1; case Slot2 -> 2; case Slot3 -> 3;
            case Slot4 -> 4; case Slot5 -> 5; case Slot6 -> 6;
            case Slot7 -> 7; case Slot8 -> 8;
            case Hotbar1 -> containerSlots;     case Hotbar2 -> containerSlots + 1;
            case Hotbar3 -> containerSlots + 2; case Hotbar4 -> containerSlots + 3;
            case Hotbar5 -> containerSlots + 4; case Hotbar6 -> containerSlots + 5;
            case Hotbar7 -> containerSlots + 6; case Hotbar8 -> containerSlots + 7;
            case Hotbar9 -> containerSlots + 8;
            case Offhand -> containerSlots + 9;
        };
    }

    // ==================== COMMON ====================

    private void finish() {
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
        if (interactMode.get() == InteractMode.ShiftRightClick && mc.options != null) {
            mc.options.sneakKey.setPressed(true);
        }
    }

    private void releaseSneak() {
        if (interactMode.get() == InteractMode.ShiftRightClick && mc.options != null) {
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
