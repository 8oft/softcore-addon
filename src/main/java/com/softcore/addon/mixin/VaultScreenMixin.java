package com.softcore.addon.mixin;

import com.softcore.addon.modules.VaultManager;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class VaultScreenMixin {
    @Shadow protected int x;
    @Shadow protected int y;

    private static final int BTN_W = 40;
    private static final int BTN_H = 12;

    private static int lastBtnX = 0;
    private static int lastBtnY = 0;
    private static boolean btnVisible = false;

    public static int getLastBtnX() { return lastBtnX; }
    public static int getLastBtnY() { return lastBtnY; }
    public static boolean isBtnVisible() { return btnVisible; }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        btnVisible = false;

        if (!((Object) this instanceof GenericContainerScreen screen)) return;

        VaultManager vm = Modules.get().get(VaultManager.class);
        if (vm == null || !vm.isActive()) return;

        String title = screen.getTitle().getString();
        if (!title.toLowerCase().contains("vault")) return;

        int btnX = this.x + 5;
        int btnY = this.y - BTN_H - 2;
        lastBtnX = btnX;
        lastBtnY = btnY;
        btnVisible = true;

        boolean hovered = mouseX >= btnX && mouseX <= btnX + BTN_W &&
                          mouseY >= btnY && mouseY <= btnY + BTN_H;

        int bgColor = hovered ? 0xFF33CC33 : 0xFF008800;
        context.fill(btnX, btnY, btnX + BTN_W, btnY + BTN_H, bgColor);

        int border = 0xFFFFFFFF;
        context.fill(btnX, btnY, btnX + BTN_W, btnY + 1, border);
        context.fill(btnX, btnY + BTN_H - 1, btnX + BTN_W, btnY + BTN_H, border);
        context.fill(btnX, btnY, btnX + 1, btnY + BTN_H, border);
        context.fill(btnX + BTN_W - 1, btnY, btnX + BTN_W, btnY + BTN_H, border);

        String text = "LOOT";
        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(text);
        int textX = btnX + (BTN_W - textWidth) / 2;
        int textY = btnY + 2;
        context.drawText(textRenderer, text, textX, textY, 0xFFFFFFFF, true);
    }
}
