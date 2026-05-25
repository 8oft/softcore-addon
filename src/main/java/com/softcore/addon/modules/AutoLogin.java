package com.softcore.addon.modules;

import com.softcore.addon.SoftcoreAddon;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.util.Timer;
import java.util.TimerTask;

public class AutoLogin extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay in ms before executing the command.")
        .defaultValue(1000)
        .min(0)
        .sliderMax(10000)
        .build()
    );

    private final Setting<Boolean> smart = sgGeneral.add(new BoolSetting.Builder()
        .name("smart")
        .description("Auto-detect login commands from chat.")
        .defaultValue(false)
        .build()
    );

    private final Setting<String> loginCommand = sgGeneral.add(new StringSetting.Builder()
        .name("login-command")
        .description("Command to run when joining server.")
        .defaultValue("/login password123")
        .build()
    );

    private final Setting<String> registerCommand = sgGeneral.add(new StringSetting.Builder()
        .name("register-command")
        .description("Command to run for registration.")
        .defaultValue("/register password123 password123")
        .build()
    );

    private final Timer timer = new Timer();

    public AutoLogin() {
        super(SoftcoreAddon.CATEGORY, "auto-login", "Automatically logs you in when joining servers.");
    }

    @EventHandler
    private void onGameJoined(GameJoinedEvent event) {
        if (!isActive()) return;
        
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mc.player != null) {
                    ChatUtils.sendPlayerMsg(loginCommand.get());
                }
            }
        }, delay.get());
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Send event) {
        if (!smart.get()) return;

        if (event.packet instanceof ChatMessageC2SPacket packet) {
            String message = packet.chatMessage().toLowerCase();
            String[] parts = message.split(" ");
            
            if (parts.length >= 2) {
                if (message.startsWith("/login ") || message.startsWith("/l ") || message.startsWith("/log ")) {
                    String password = parts[1];
                    loginCommand.set("/login " + password);
                    info("Detected login command: /login " + password);
                } else if (message.startsWith("/register ") || message.startsWith("/reg ")) {
                    if (parts.length >= 3) {
                        String password = parts[1];
                        registerCommand.set("/register " + password + " " + password);
                        info("Detected register command: /register " + password + " " + password);
                    }
                }
            }
        }
    }
}
