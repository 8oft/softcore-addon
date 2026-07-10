package com.softcore.addon;

import com.softcore.addon.commands.*;
import com.softcore.addon.hud.HudExample;
import com.softcore.addon.modules.*;
import com.softcore.addon.modules.dupes.*;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class SoftcoreAddon extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Softcore Utils");
    public static final Category AUTO_DUPE_CATEGORY = new Category("Softcore Auto Dupe");
    public static final HudGroup HUD_GROUP = new HudGroup("Softcore");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Softcore Addon");

        // Softcore Utils modules
        Modules.get().add(new PacketLogger());
        Modules.get().add(new SlotViewer());
        Modules.get().add(new PacketDelay());
        Modules.get().add(new BundleDupe());
        Modules.get().add(new GuiMacros());

        // Softcore Auto Dupe modules
        Modules.get().add(new SoftCloseVault());
        Modules.get().add(new SoftCloseBackpack());
        Modules.get().add(new SoftCloseChest());
        Modules.get().add(new SlotChangeBackpack());

        // Commands
        Commands.add(new DesyncCommand());
        Commands.add(new GuiCommand());
        Commands.add(new DisconnectPacketsCommand());
        Commands.add(new DelayPacketsCommand());
        Commands.add(new SendPacketsCommand());
        Commands.add(new ClickSlotCommand());
        Commands.add(new RepeatCommand());
        Commands.add(new WaitCommand());
        Commands.add(new RepeatDelayCommand());
        Commands.add(new ActionCommand());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
        Modules.registerCategory(AUTO_DUPE_CATEGORY);
    }

    @Override
    public String getPackage() {
        return "com.softcore.addon";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("8oft", "meteor-softcore-addon");
    }
}
