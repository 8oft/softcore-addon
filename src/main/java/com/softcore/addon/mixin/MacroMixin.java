package com.softcore.addon.mixin;

import com.softcore.addon.modules.GuiMacros;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Macro.class)
public abstract class MacroMixin {
    @Redirect(
        method = "onAction",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;"
        )
    )
    private Screen redirectCurrentScreen(MinecraftClient mc) {
        return Modules.get().isActive(GuiMacros.class) && mc.player != null ? null : mc.currentScreen;
    }
}
