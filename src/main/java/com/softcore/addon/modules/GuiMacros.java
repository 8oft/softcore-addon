package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class GuiMacros extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    
    private final Setting<String> macro1 = sgGeneral.add(new StringSetting.Builder()
        .name("macro-1")
        .description("Macro 1 command (use .macro1 to execute)")
        .defaultValue("")
        .build()
    );
    
    private final Setting<String> macro2 = sgGeneral.add(new StringSetting.Builder()
        .name("macro-2")
        .description("Macro 2 command (use .macro2 to execute)")
        .defaultValue("")
        .build()
    );
    
    private final Setting<String> macro3 = sgGeneral.add(new StringSetting.Builder()
        .name("macro-3")
        .description("Macro 3 command (use .macro3 to execute)")
        .defaultValue("")
        .build()
    );
    
    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify")
        .description("Notify when macros are executed")
        .defaultValue(true)
        .build()
    );
    
    private boolean isInValidGui = false;

    public GuiMacros() {
        super(SoftcoreAddon.CATEGORY, "gui-macros", "Allows macros in inventory/chest GUIs only.");
    }

    @Override
    public void onActivate() {
        updateGuiStatus();
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        updateGuiStatus();
    }

    private void updateGuiStatus() {
        Screen screen = mc.currentScreen;
        isInValidGui = isValidGuiScreen(screen);
        
        if (isInValidGui && isActive()) {
            info("GUI Macros enabled in " + screen.getClass().getSimpleName());
        }
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
               screen instanceof SmithingScreen;             // Smithing table
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!isActive() || !isInValidGui) return;
        
        if (event.packet instanceof ChatMessageC2SPacket packet) {
            String message = packet.chatMessage().toLowerCase();
            
            if (message.equals(".macro1") && !macro1.get().isEmpty()) {
                event.cancel();
                executeMacro(macro1.get(), "1");
            } else if (message.equals(".macro2") && !macro2.get().isEmpty()) {
                event.cancel();
                executeMacro(macro2.get(), "2");
            } else if (message.equals(".macro3") && !macro3.get().isEmpty()) {
                event.cancel();
                executeMacro(macro3.get(), "3");
            }
        }
    }

    private void executeMacro(String command, String macroNum) {
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        
        ChatUtils.sendPlayerMsg(command);
        
        if (notify.get()) {
            info("Executed macro " + macroNum + ": " + command);
        }
    }

    public boolean canUseMacros() {
        return isActive() && isInValidGui;
    }

    @Override
    public String getInfoString() {
        if (!isInValidGui) {
            return "No GUI";
        }
        int activeMacros = 0;
        if (!macro1.get().isEmpty()) activeMacros++;
        if (!macro2.get().isEmpty()) activeMacros++;
        if (!macro3.get().isEmpty()) activeMacros++;
        return activeMacros + " macros";
    }
}
