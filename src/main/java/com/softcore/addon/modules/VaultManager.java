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
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    // Cached reflection data to avoid repeated lookups
    private static Constructor<?> packetCtor;
    private static Class<?>[] packetParamTypes;
    private static Object emptyStackValue;
    private static boolean reflectionInit = false;

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

        initReflection();

        // Build ALL packets first
        List<Packet<?>> packets = new ArrayList<>();
        int lootedCount = 0;
        for (int slot = 0; slot < slots; slot++) {
            if (slot == nextSlot || slot == prevSlot) continue;

            ItemStack stack = screen.getScreenHandler().getSlot(slot).getStack();
            if (!stack.isEmpty()) {
                for (int i = 0; i < packetRepeat.get(); i++) {
                    Packet<?> p = buildRawPacket(screen, slot, SlotActionType.QUICK_MOVE);
                    if (p != null) packets.add(p);
                }
                lootedCount++;
            }
        }
        info("Quick-moved " + lootedCount + " items (" + packets.size() + " packets)");

        if (hasNextArrow) {
            Packet<?> p = buildRawPacket(screen, nextSlot, SlotActionType.PICKUP);
            if (p != null) packets.add(p);
        }

        // Send all packets in a single flush
        sendPacketBatch(packets);

        if (hasNextArrow) {
            lastTitle = "";
            info("Going to next page...");
        } else {
            info("All vault pages looted.");
            toggle();
        }
    }

    private void initReflection() {
        if (reflectionInit) return;
        reflectionInit = true;
        try {
            packetCtor = ClickSlotC2SPacket.class.getConstructors()[0];
            packetParamTypes = packetCtor.getParameterTypes();
            // Find the empty stack type (last non-primitive, non-enum, non-map param)
            for (int i = packetParamTypes.length - 1; i >= 0; i--) {
                Class<?> type = packetParamTypes[i];
                if (!type.isPrimitive() && !type.isEnum() && !type.getName().contains("Int2Object")) {
                    emptyStackValue = getEmptyStack(type);
                    break;
                }
            }
        } catch (Exception e) {
            error("Failed to init packet reflection: " + e.getMessage());
        }
    }

    private Packet<?> buildRawPacket(GenericContainerScreen screen, int slotId, SlotActionType actionType) {
        var handler = screen.getScreenHandler();
        if (packetCtor == null || mc.getNetworkHandler() == null) return null;

        try {
            Object[] args = new Object[packetParamTypes.length];
            for (int i = 0; i < packetParamTypes.length; i++) {
                Class<?> type = packetParamTypes[i];
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
                    map.put(slotId, emptyStackValue);
                    args[i] = map;
                } else {
                    args[i] = emptyStackValue;
                }
            }
            return (Packet<?>) packetCtor.newInstance(args);
        } catch (Exception e) {
            error("Failed to build packet: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    private void sendPacketBatch(List<Packet<?>> packets) {
        if (packets.isEmpty()) return;

        // Try to batch via channel.write + single flush for true same-tick sending
        try {
            ClientConnection connection = mc.getNetworkHandler().getConnection();
            Field channelField = findChannelField(connection);
            if (channelField != null) {
                channelField.setAccessible(true);
                Object channel = channelField.get(connection);
                if (channel != null) {
                    java.lang.reflect.Method write = channel.getClass().getMethod("write", Object.class);
                    java.lang.reflect.Method flush = channel.getClass().getMethod("flush");
                    for (Packet<?> packet : packets) {
                        write.invoke(channel, packet);
                    }
                    flush.invoke(channel);
                    info("Sent " + packets.size() + " packets via channel batch");
                    return;
                }
            }
            info("Channel field not found, falling back to sendPacket");
        } catch (Exception e) {
            info("Channel batch failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        // Fallback: send individually
        for (Packet<?> packet : packets) {
            mc.getNetworkHandler().sendPacket(packet);
        }
        info("Sent " + packets.size() + " packets individually (fallback)");
    }

    private Field findChannelField(Object connection) {
        Class<?> clazz = connection.getClass();
        while (clazz != null) {
            for (Field f : clazz.getDeclaredFields()) {
                String typeName = f.getType().getName();
                if (typeName.contains("Channel") || typeName.contains("channel") || typeName.startsWith("io.netty")) {
                    return f;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private Object getEmptyStack(Class<?> stackType) {
        if (stackType == ItemStack.class) {
            return ItemStack.EMPTY;
        }
        try {
            for (Field f : stackType.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == stackType) {
                    return f.get(null);
                }
            }
            for (Field f : stackType.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == stackType) {
                    f.setAccessible(true);
                    return f.get(null);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return ItemStack.EMPTY;
    }

    @Override
    public String getInfoString() {
        return isActive() ? "Active" : "Inactive";
    }
}
