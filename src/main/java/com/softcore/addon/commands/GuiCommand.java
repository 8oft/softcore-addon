package com.softcore.addon.commands;

import com.softcore.addon.utils.timer.MsTimer;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.sync.ItemStackHash;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;

import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class GuiCommand extends Command {
    public GuiCommand() {
        super("gui", "GUI utilities: save, load, steal, dump, offhand, drop");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // ---- save ----
        builder.then(literal("save").executes(context -> {
            if (mc.player == null) {
                error("Not in game");
                return SINGLE_SUCCESS;
            }
            UiUtilsBridge.setStoredScreen(mc.currentScreen);
            UiUtilsBridge.setStoredScreenHandler(mc.player.currentScreenHandler);
            info("GUI saved");
            return SINGLE_SUCCESS;
        }));

        // ---- load ----
        builder.then(literal("load").executes(context -> {
            var screen = UiUtilsBridge.getStoredScreen();
            if (screen == null || mc.player == null) {
                error("No saved GUI found");
                return SINGLE_SUCCESS;
            }
            mc.setScreen(screen);
            mc.player.currentScreenHandler = UiUtilsBridge.getStoredScreenHandler();
            info("GUI restored");
            return SINGLE_SUCCESS;
        }));

        // ---- steal <pickup|quickmove> [delay] ----
        builder.then(literal("steal")
            .then(argument("mode", StringArgumentType.word())
                .executes(ctx -> execSteal(ctx.getArgument("mode", String.class), 100))
                .then(argument("delay", IntegerArgumentType.integer(0, 5000))
                    .executes(ctx -> execSteal(
                        ctx.getArgument("mode", String.class),
                        ctx.getArgument("delay", Integer.class))
                    )
                )
            )
        );

        // ---- dump <pickup|quickmove> [delay] ----
        builder.then(literal("dump")
            .then(argument("mode", StringArgumentType.word())
                .executes(ctx -> execDump(ctx.getArgument("mode", String.class), 100))
                .then(argument("delay", IntegerArgumentType.integer(0, 5000))
                    .executes(ctx -> execDump(
                        ctx.getArgument("mode", String.class),
                        ctx.getArgument("delay", Integer.class))
                    )
                )
            )
        );

        // ---- offhand <slot> ----
        builder.then(literal("offhand")
            .then(argument("slot", IntegerArgumentType.integer(0, 255))
                .executes(ctx -> execOffhand(ctx.getArgument("slot", Integer.class)))
            )
        );

        // ---- drop <slot> [amount] ----
        builder.then(literal("drop")
            .then(argument("slot", IntegerArgumentType.integer(0, 255))
                .executes(ctx -> execDrop(ctx.getArgument("slot", Integer.class), false))
                .then(argument("amount", IntegerArgumentType.integer(1))
                    .executes(ctx -> execDrop(ctx.getArgument("slot", Integer.class), false))
                )
                .then(literal("all")
                    .executes(ctx -> execDrop(ctx.getArgument("slot", Integer.class), true))
                )
            )
        );

        // ---- close ----
        builder.then(literal("close").executes(context -> {
            if (mc.player == null) {
                error("Not in game");
                return SINGLE_SUCCESS;
            }
            mc.player.closeHandledScreen();
            info("GUI closed");
            return SINGLE_SUCCESS;
        }));

        // ---- softclose ----
        builder.then(literal("softclose").executes(context -> {
            mc.setScreen(null);
            info("GUI closed without packet");
            return SINGLE_SUCCESS;
        }));
    }

    private int execSteal(String modeStr, int delayMs) {
        if (mc.player == null || mc.interactionManager == null) {
            error("Not in game");
            return SINGLE_SUCCESS;
        }

        var handler = mc.player.currentScreenHandler;
        int totalSlots = handler.slots.size();
        int containerSlots = totalSlots - 36; // player inv = 27 main + 9 hotbar
        if (containerSlots <= 0) {
            error("Not a container GUI");
            return SINGLE_SUCCESS;
        }

        SlotActionType action = parseAction(modeStr);
        if (action == null) {
            error("Invalid mode: " + modeStr + " (use pickup or quickmove)");
            return SINGLE_SUCCESS;
        }

        int syncId = handler.syncId;
        AtomicInteger offset = new AtomicInteger(0);
        int count = 0;

        for (int slot = 0; slot < containerSlots; slot++) {
            var stack = handler.getSlot(slot).getStack();
            if (stack.isEmpty()) continue;
            count++;

            int s = slot;
            int currentOffset = offset.getAndAdd(delayMs);

            if (action == SlotActionType.PICKUP) {
                // PICKUP: need 2 clicks — pickup from container, place into player inv
                int targetSlot = containerSlots; // first player inv slot
                MsTimer.schedule(() -> {
                    if (mc.player == null || mc.getNetworkHandler() == null) return;
                    ClickSlotC2SPacket pickup = new ClickSlotC2SPacket(
                        syncId, 0, (short) s, (byte) 0, SlotActionType.PICKUP,
                        new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
                    );
                    mc.getNetworkHandler().sendPacket(pickup);

                    ClickSlotC2SPacket place = new ClickSlotC2SPacket(
                        syncId, 0, (short) targetSlot, (byte) 0, SlotActionType.PICKUP,
                        new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
                    );
                    mc.getNetworkHandler().sendPacket(place);
                }, currentOffset);
            } else {
                // QUICK_MOVE: 1 click
                MsTimer.schedule(() -> {
                    if (mc.player == null || mc.getNetworkHandler() == null) return;
                    ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                        syncId, 0, (short) s, (byte) 0, action,
                        new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
                    );
                    mc.getNetworkHandler().sendPacket(packet);
                }, currentOffset);
            }
        }

        info("Stealing " + count + " items (" + action.name() + ", delay=" + delayMs + "ms)");
        return SINGLE_SUCCESS;
    }

    private int execDump(String modeStr, int delayMs) {
        if (mc.player == null || mc.interactionManager == null) {
            error("Not in game");
            return SINGLE_SUCCESS;
        }

        var handler = mc.player.currentScreenHandler;
        int totalSlots = handler.slots.size();
        int containerSlots = totalSlots - 36;
        if (containerSlots <= 0) {
            error("Not a container GUI");
            return SINGLE_SUCCESS;
        }

        SlotActionType action = parseAction(modeStr);
        if (action == null) {
            error("Invalid mode: " + modeStr + " (use pickup or quickmove)");
            return SINGLE_SUCCESS;
        }

        int syncId = handler.syncId;
        AtomicInteger offset = new AtomicInteger(0);
        int count = 0;

        // player main inv + hotbar = 36 slots starting at containerSlots
        for (int i = 0; i < 36; i++) {
            int slot = containerSlots + i;
            var stack = handler.getSlot(slot).getStack();
            if (stack.isEmpty()) continue;
            count++;

            int s = slot;
            int currentOffset = offset.getAndAdd(delayMs);

            if (action == SlotActionType.PICKUP) {
                // PICKUP: pickup from player inv, place into container slot 0
                MsTimer.schedule(() -> {
                    if (mc.player == null || mc.getNetworkHandler() == null) return;
                    ClickSlotC2SPacket pickup = new ClickSlotC2SPacket(
                        syncId, 0, (short) s, (byte) 0, SlotActionType.PICKUP,
                        new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
                    );
                    mc.getNetworkHandler().sendPacket(pickup);

                    ClickSlotC2SPacket place = new ClickSlotC2SPacket(
                        syncId, 0, (short) 0, (byte) 0, SlotActionType.PICKUP,
                        new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
                    );
                    mc.getNetworkHandler().sendPacket(place);
                }, currentOffset);
            } else {
                MsTimer.schedule(() -> {
                    if (mc.player == null || mc.getNetworkHandler() == null) return;
                    ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                        syncId, 0, (short) s, (byte) 0, action,
                        new Int2ObjectArrayMap<>(), ItemStackHash.EMPTY
                    );
                    mc.getNetworkHandler().sendPacket(packet);
                }, currentOffset);
            }
        }

        info("Dumping " + count + " items (" + action.name() + ", delay=" + delayMs + "ms)");
        return SINGLE_SUCCESS;
    }

    private int execOffhand(int slot) {
        if (mc.player == null || mc.interactionManager == null) {
            error("Not in game");
            return SINGLE_SUCCESS;
        }
        var handler = mc.player.currentScreenHandler;
        if (slot >= handler.slots.size()) {
            error("Slot " + slot + " out of range (max " + (handler.slots.size() - 1) + ")");
            return SINGLE_SUCCESS;
        }
        int syncId = mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> screen
            ? screen.getScreenHandler().syncId : handler.syncId;
        mc.interactionManager.clickSlot(syncId, slot, 40, SlotActionType.SWAP, mc.player);
        info("Moved slot " + slot + " to offhand");
        return SINGLE_SUCCESS;
    }

    private int execDrop(int slot, boolean all) {
        if (mc.player == null || mc.interactionManager == null) {
            error("Not in game");
            return SINGLE_SUCCESS;
        }
        var handler = mc.player.currentScreenHandler;
        if (slot >= handler.slots.size()) {
            error("Slot " + slot + " out of range (max " + (handler.slots.size() - 1) + ")");
            return SINGLE_SUCCESS;
        }
        int syncId = mc.currentScreen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?> screen
            ? screen.getScreenHandler().syncId : handler.syncId;
        int button = all ? 1 : 0;
        mc.interactionManager.clickSlot(syncId, slot, button, SlotActionType.THROW, mc.player);
        info(all ? "Dropped entire stack from slot " + slot : "Dropped 1 item from slot " + slot);
        return SINGLE_SUCCESS;
    }

    private SlotActionType parseAction(String s) {
        return switch (s.toLowerCase()) {
            case "pickup" -> SlotActionType.PICKUP;
            case "quickmove" -> SlotActionType.QUICK_MOVE;
            default -> null;
        };
    }
}
