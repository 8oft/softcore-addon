package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import com.softcore.addon.util.VaultButtonState;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

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

    private boolean wasMouseDown = false;
    private static final int BTN_W = 40;
    private static final int BTN_H = 12;

    public VaultManager() {
        super(SoftcoreAddon.CATEGORY, "vaults-plugin-dupe", "Vaults Plugin Dupe - Click the LOOT button in Vault GUIs.");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!VaultButtonState.btnVisible) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;
        if (!screen.getTitle().getString().toLowerCase().contains("vault")) return;

        boolean isMouseDown = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (isMouseDown && !wasMouseDown) {
            double mouseX = mc.mouse.getX();
            double mouseY = mc.mouse.getY();

            int btnX = VaultButtonState.lastBtnX;
            int btnY = VaultButtonState.lastBtnY;

            if (mouseX >= btnX && mouseX <= btnX + BTN_W &&
                mouseY >= btnY && mouseY <= btnY + BTN_H) {
                lootScreen(screen);
            }
        }

        wasMouseDown = isMouseDown;
    }

    public void lootScreen(GenericContainerScreen screen) {
        if (mc.interactionManager == null || mc.player == null) return;

        String title = screen.getTitle().getString();
        int slots = screen.getScreenHandler().getRows() * 9;
        int nextSlot = slots - 1;
        int prevSlot = slots - 9;

        boolean hasNextArrow = !screen.getScreenHandler().getSlot(nextSlot).getStack().isEmpty();

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
            info("Going to next page...");
        } else {
            info("All vault pages looted.");
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
        return "Click LOOT button";
    }
}
