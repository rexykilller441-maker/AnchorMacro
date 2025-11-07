package com.anchormacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AnchorMacroConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/anchormacro.json");

    // ===============================
    // Default configuration values
    // ===============================
    public static int anchorSlot = 0;
    public static int glowstoneSlot = 1;
    public static int totemSlot = 2;

    public static boolean autoSearchHotbar = true;
    public static boolean showNotifications = true;
    public static boolean explodeOnlyIfTotemPresent = false;
    public static boolean safeAnchorMode = true;

    public static int delayPlaceAnchor = 3;
    public static int delaySwitchToGlowstone = 3;
    public static int delayChargeAnchor = 3;
    public static int delaySwitchToTotem = 3;
    public static int delayExplodeAnchor = 3;

    // ===============================
    // Config IO
    // ===============================
    public static void load() {
        try {
            if (!FILE.exists()) {
                save();
                return;
            }
            AnchorMacroConfig loaded = GSON.fromJson(new FileReader(FILE), AnchorMacroConfig.class);
            if (loaded != null) {
                anchorSlot = loaded.anchorSlot;
                glowstoneSlot = loaded.glowstoneSlot;
                totemSlot = loaded.totemSlot;
                autoSearchHotbar = loaded.autoSearchHotbar;
                showNotifications = loaded.showNotifications;
                explodeOnlyIfTotemPresent = loaded.explodeOnlyIfTotemPresent;
                safeAnchorMode = loaded.safeAnchorMode;
                delayPlaceAnchor = loaded.delayPlaceAnchor;
                delaySwitchToGlowstone = loaded.delaySwitchToGlowstone;
                delayChargeAnchor = loaded.delayChargeAnchor;
                delaySwitchToTotem = loaded.delaySwitchToTotem;
                delayExplodeAnchor = loaded.delayExplodeAnchor;
            }
        } catch (Exception e) {
            System.err.println("Failed to load AnchorMacro config: " + e.getMessage());
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(FILE)) {
                GSON.toJson(new AnchorMacroConfig(), w);
            }
        } catch (IOException e) {
            System.err.println("Failed to save AnchorMacro config: " + e.getMessage());
        }
    }

    public static int guiToInternalSlot(int guiSlot) {
        return (guiSlot >= 0 && guiSlot < 9) ? guiSlot : 0;
    }
}
