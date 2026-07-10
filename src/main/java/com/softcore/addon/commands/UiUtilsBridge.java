package com.softcore.addon.commands;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.packet.Packet;
import net.minecraft.screen.ScreenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class UiUtilsBridge {
    private static final Logger LOG = LoggerFactory.getLogger("softcore-ui-bridge");
    private static final String SHARED_VARS_CLASS = "com.ui_utils.SharedVariables";

    private static Class<?> sharedVarsClass;

    private static Class<?> resolve() {
        if (sharedVarsClass != null) return sharedVarsClass;

        try {
            sharedVarsClass = Class.forName(SHARED_VARS_CLASS, true, Thread.currentThread().getContextClassLoader());
        } catch (Throwable t) {
            try {
                sharedVarsClass = Class.forName(SHARED_VARS_CLASS);
            } catch (Throwable t2) {
                // ui-utils not available — that's okay
            }
        }
        return sharedVarsClass;
    }

    public static boolean isLoaded() {
        return resolve() != null;
    }

    // --- sendUIPackets ---
    public static boolean getSendUIPackets() {
        return getBoolField("sendUIPackets", true);
    }

    public static void setSendUIPackets(boolean val) {
        setBoolField("sendUIPackets", val);
    }

    // --- delayUIPackets ---
    public static boolean getDelayUIPackets() {
        return getBoolField("delayUIPackets", false);
    }

    public static void setDelayUIPackets(boolean val) {
        setBoolField("delayUIPackets", val);
    }

    // --- delayedUIPackets ---
    @SuppressWarnings("unchecked")
    public static ArrayList<Packet<?>> getDelayedUIPackets() {
        Class<?> clazz = resolve();
        if (clazz == null) return new ArrayList<>();
        try {
            Field f = clazz.getField("delayedUIPackets");
            return (ArrayList<Packet<?>>) f.get(null);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void clearDelayedUIPackets() {
        Class<?> clazz = resolve();
        if (clazz == null) return;
        try {
            Field f = clazz.getField("delayedUIPackets");
            Object list = f.get(null);
            if (list instanceof ArrayList<?> arr) {
                arr.clear();
            }
        } catch (Exception e) {
            // ignore
        }
    }

    // --- storedScreen ---
    public static Screen getStoredScreen() {
        Class<?> clazz = resolve();
        if (clazz == null) return null;
        try {
            Field f = clazz.getField("storedScreen");
            return (Screen) f.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setStoredScreen(Screen screen) {
        Class<?> clazz = resolve();
        if (clazz == null) return;
        try {
            Field f = clazz.getField("storedScreen");
            f.set(null, screen);
        } catch (Exception e) {
            // ignore
        }
    }

    // --- storedScreenHandler ---
    public static ScreenHandler getStoredScreenHandler() {
        Class<?> clazz = resolve();
        if (clazz == null) return null;
        try {
            Field f = clazz.getField("storedScreenHandler");
            return (ScreenHandler) f.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setStoredScreenHandler(ScreenHandler handler) {
        Class<?> clazz = resolve();
        if (clazz == null) return;
        try {
            Field f = clazz.getField("storedScreenHandler");
            f.set(null, handler);
        } catch (Exception e) {
            // ignore
        }
    }

    // --- helpers ---
    private static boolean getBoolField(String name, boolean defaultVal) {
        Class<?> clazz = resolve();
        if (clazz == null) return defaultVal;
        try {
            Field f = clazz.getField(name);
            return f.getBoolean(null);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static void setBoolField(String name, boolean val) {
        Class<?> clazz = resolve();
        if (clazz == null) return;
        try {
            Field f = clazz.getField(name);
            f.setBoolean(null, val);
        } catch (Exception e) {
            // ignore
        }
    }
}
