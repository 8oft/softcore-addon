package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
        int repeats = packetRepeat.get();
        for (int i = 0; i < repeats; i++) {
            sendRawClickSlot(screen, slotId, SlotActionType.QUICK_MOVE);
        }
    }

    private void clickSlot(GenericContainerScreen screen, int slotId) {
        sendRawClickSlot(screen, slotId, SlotActionType.PICKUP);
    }

    private void sendRawClickSlot(GenericContainerScreen screen, int slotId, SlotActionType actionType) {
        var handler = screen.getScreenHandler();
        if (mc.getNetworkHandler() == null) return;

        try {
            @SuppressWarnings("unchecked")
            Constructor<Object> ctor = (Constructor<Object>) ClickSlotC2SPacket.class.getConstructors()[0];
            Class<?>[] paramTypes = ctor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> type = paramTypes[i];
                if (type == int.class) {
                    args[i] = (i == 0) ? handler.syncId : handler.getRevision();
                } else if (type == short.class) {
                    args[i] = (short) slotId;
                } else if (type == byte.class) {
                    args[i] = (byte) 0;
                } else if (type.isEnum()) {
                    args[i] = actionType;
                } else if (type.getName().contains("Int2ObjectMap") || type.getName().contains("Int2ObjectOpenHashMap")) {
                    Int2ObjectOpenHashMap<Object> map = new Int2ObjectOpenHashMap<>();
                    Object empty = getEmptyStack(paramTypes[paramTypes.length - 1]);
                    map.put(slotId, empty);
                    args[i] = map;
                } else {
                    args[i] = getEmptyStack(type);
                }
            }

            Object packet = ctor.newInstance(args);
            mc.getNetworkHandler().sendPacket((Packet<?>) packet);
        } catch (Exception e) {
            error("Failed to send click slot packet: " + e.getMessage());
        }
    }

    private Object getEmptyStack(Class<?> stackType) throws Exception {
        if (stackType == ItemStack.class) {
            return ItemStack.EMPTY;
        }
        Class<?> itemStackHashClass = Class.forName("net.minecraft.screen.sync.ItemStackHash", false, getClass().getClassLoader());
        if (stackType == itemStackHashClass) {
            Field emptyField = itemStackHashClass.getField("EMPTY");
            return emptyField.get(null);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String getInfoString() {
        return isActive() ? "Active" : "Inactive";
    }
}
