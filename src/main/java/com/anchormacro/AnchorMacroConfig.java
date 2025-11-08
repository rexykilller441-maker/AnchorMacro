package com.anchormacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Central JSON-backed config for AnchorMacro.
 * Contains all fields referenced throughout the mod (keeps compatibility with executor, GUI, test runner).
 */
public class AnchorMacroConfig {
    private static final File CONFIG_FILE = new File("config", "anchormacro.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static AnchorMacroConfig INSTANCE;

    // =========================
    // Anchor macro core fields
    // =========================
    public boolean anchorMacroEnabled = true;
    public int anchorSlot = 0;      // GUI hotbar number 1..9 mapped via guiToInternalSlot
    public int glowstoneSlot = 1;
    public int totemSlot = 2;

    public boolean autoSearchHotbar = true;    // whether auto-search only checks hotbar 1-9
    public boolean showNotifications = true;
    public boolean explodeOnlyIfTotemPresent = false;
    public boolean safeAnchorMode = true;

    // delays measured in ticks (or whatever your executor expects)
    public int delayPlaceAnchor = 3;
    public int delaySwitchToGlowstone = 3;
    public int delayChargeAnchor = 3;
    public int delaySwitchToTotem = 3;
    public int delayExplodeAnchor = 3;

    // hitbox module settings (used by Modules GUI)
    public boolean hitboxEnabled = true;
    public float hitboxExpand = 0.5f;
    public float hitboxDistance = 6.0f;

    // Add any future config fields here...

    // =========================
    // Singleton access + IO
    // =========================
    public static synchronized AnchorMacroConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static synchronized void load() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) CONFIG_FILE.getParentFile().mkdirs();

            if (CONFIG_FILE.exists()) {
                try (FileReader fr = new FileReader(CONFIG_FILE)) {
                    AnchorMacroConfig loaded = GSON.fromJson(fr, AnchorMacroConfig.class);
                    if (loaded != null) {
                        INSTANCE = loaded;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AnchorMacro] Failed to load config: " + e.getMessage());
        }
        // Fallback: create default
        INSTANCE = new AnchorMacroConfig();
        INSTANCE.save();
    }

    public synchronized void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter fw = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, fw);
            }
        } catch (Exception e) {
            System.err.println("[AnchorMacro] Failed to save config: " + e.getMessage());
        }
    }

    /**
     * Convert GUI slot (1..9 or 0..8 depending on usage) to internal container/hotbar slot index.
     * Many parts of the mod expect hotbar slots as 0..8; GUI often shows 1..9.
     * This helper ensures safe conversion — adjust if your GUI uses different indexing.
     */
    public static int guiToInternalSlot(int guiSlot) {
        // If GUI uses 1-9, convert to 0-8:
        if (guiSlot >= 1 && guiSlot <= 9) return guiSlot - 1;
        // If GUI already passed 0-8, return as-is (clamp)
        if (guiSlot >= 0 && guiSlot < 9) return guiSlot;
        // fallback default to 0
        return 0;
    }
}
