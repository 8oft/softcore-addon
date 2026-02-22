package com.softcore.addon;

import com.softcore.addon.commands.CommandExample;
import com.softcore.addon.hud.HudExample;
import com.softcore.addon.modules.*;
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
    public static final Category CATEGORY = new Category("Softcore");
    public static final HudGroup HUD_GROUP = new HudGroup("Softcore");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Softcore Addon");

        // Modules
        Modules.get().add(new ModuleExample());
        Modules.get().add(new InventoryCloseCanceller());
        Modules.get().add(new AutoDisconnect());
        Modules.get().add(new ItemDupeHelper());
        Modules.get().add(new PacketLogger());

        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
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
