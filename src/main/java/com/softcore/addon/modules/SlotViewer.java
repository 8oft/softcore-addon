package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class SlotViewer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Color of the slot ID numbers")
        .defaultValue(new SettingColor(255, 255, 255, 180))
        .build()
    );

    public SlotViewer() {
        super(SoftcoreAddon.CATEGORY, "slot-viewer", "Shows slot ID numbers on every GUI slot", "slot-ids");
    }
}
