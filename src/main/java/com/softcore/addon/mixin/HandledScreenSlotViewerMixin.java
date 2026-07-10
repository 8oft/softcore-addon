package com.softcore.addon.mixin;

import com.softcore.addon.modules.SlotViewer;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenSlotViewerMixin {
    @Shadow protected int x;
    @Shadow protected int y;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        SlotViewer mod = Modules.get().get(SlotViewer.class);
        if (mod == null || !mod.isActive()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;

        var handler = ((HandledScreen<?>) (Object) this).getScreenHandler();
        int color = mod.textColor.get().getPacked();

        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.getSlot(i);
            int slotX = x + slot.x;
            int slotY = y + slot.y;

            context.drawText(textRenderer, String.valueOf(i), slotX + 1, slotY + 1, color, true);
        }
    }
}
