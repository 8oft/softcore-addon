package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class SoftClose extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify")
        .description("Notify when closing GUIs")
        .defaultValue(true)
        .build()
    );
    
    private final Setting<Boolean> instant = sgGeneral.add(new BoolSetting.Builder()
        .name("instant")
        .description("Instantly close without animation")
        .defaultValue(true)
        .build()
    );
    
    private boolean isInValidGui = false;

    public SoftClose() {
        super(SoftcoreAddon.CATEGORY, "soft-close", "Close chest/inventory GUIs instantly.");
    }

    @Override
    public void onActivate() {
        updateGuiStatus();
        // Auto-close current GUI if in valid screen
        if (isInValidGui) {
            closeCurrentGui();
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        updateGuiStatus();
    }

    private void updateGuiStatus() {
        Screen screen = mc.currentScreen;
        isInValidGui = isValidGuiScreen(screen);
    }

    private boolean isValidGuiScreen(Screen screen) {
        return screen instanceof GenericContainerScreen ||    // Chests, shulker boxes, etc.
               screen instanceof InventoryScreen ||          // Player inventory
               screen instanceof CraftingScreen ||           // Crafting table
               screen instanceof AnvilScreen ||              // Anvil
               screen instanceof EnchantmentScreen ||        // Enchantment table
               screen instanceof BrewingStandScreen ||       // Brewing stand
               screen instanceof FurnaceScreen ||            // Furnace
               screen instanceof HopperScreen ||             // Hopper
               screen instanceof ShulkerBoxScreen ||         // Shulker box
               screen instanceof BeaconScreen ||             // Beacon
               screen instanceof LecternScreen ||            // Lectern
               screen instanceof LoomScreen ||               // Loom
               screen instanceof CartographyTableScreen ||   // Cartography table
               screen instanceof GrindstoneScreen ||         // Grindstone
               screen instanceof SmithingScreen ||           // Smithing table
               screen instanceof MerchantScreen ||           // Villager trading
               screen instanceof HorseScreen ||              // Horse/donkey inventory
               screen instanceof CraftingScreen;           // Alternative crafting table
    }

    // Simple activation-based close instead of keybind
    public void closeGui() {
        closeCurrentGui();
    }

    private void closeCurrentGui() {
        if (mc.currentScreen == null || !isInValidGui) {
            if (notify.get()) {
                ChatUtils.error("Not in a valid GUI to close!");
            }
            return;
        }

        try {
            if (instant.get()) {
                // Send close packet directly for instant close
                if (mc.getNetworkHandler() != null && mc.player != null) {
                    mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
                }
            } else {
                // Normal close
                mc.player.closeScreen();
            }
            
            if (notify.get()) {
                ChatUtils.info("Closed " + mc.currentScreen.getClass().getSimpleName());
            }
        } catch (Exception e) {
            ChatUtils.error("Failed to close GUI: " + e.getMessage());
        }
    }

    public boolean canUseSoftClose() {
        return isActive() && isInValidGui;
    }

    // Keybind functionality removed - use module activation instead

    @Override
    public String getInfoString() {
        if (!isInValidGui) {
            return "No GUI";
        }
        return "Ready";
    }
}
