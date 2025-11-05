package com.anchormacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-backed config stored in config/anchormacro.json
 */
public class AnchorMacroConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("anchormacro.json");
    private static AnchorMacroConfig INSTANCE;

    // hotbar slots 0..8
    public int anchorSlot = 0;
    public int glowstoneSlot = 1;
    public int totemSlot = 8;

    // delays (in ticks). 1 tick = 50 ms
    public int delayPlaceAnchor = 4;
    public int delaySwitchToGlowstone = 2;
    public int delayChargeAnchor = 3;
    public int delaySwitchToTotem = 2;
    public int delayExplodeAnchor = 2;

    // safety & explosion behavior
    public boolean safeAnchorMode = false; // place glowstone in front after charging
    public boolean explodeOnlyIfTotemPresent = false;

    public boolean showNotifications = true;

    private AnchorMacroConfig() {}

    public static AnchorMacroConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static AnchorMacroConfig load() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                AnchorMacroConfig cfg = gson.fromJson(json, AnchorMacroConfig.class);
                if (cfg == null) throw new IOException("Invalid config JSON, using defaults.");
                return cfg;
            } else {
                AnchorMacroConfig defaultCfg = new AnchorMacroConfig();
                saveStatic(defaultCfg, gson);
                return defaultCfg;
            }
        } catch (Exception e) {
            // fallback to defaults if anything fails
            AnchorMacroClient.log("Failed to load config, using defaults: " + e.getMessage());
            AnchorMacroConfig def = new AnchorMacroConfig();
            try { saveStatic(def, gson); } catch (Exception ignored) {}
            return def;
        }
    }

    public void save() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            saveStatic(this, gson);
        } catch (IOException e) {
            AnchorMacroClient.log("Failed to save config: " + e.getMessage());
        }
    }

    private static void saveStatic(AnchorMacroConfig cfg, Gson gson) throws IOException {
        Path dir = CONFIG_PATH.getParent();
        if (!Files.exists(dir)) Files.createDirectories(dir);
        String json = gson.toJson(cfg);
        Files.writeString(CONFIG_PATH, json);
    }

    // utility to reset to defaults
    public void resetToDefaults() {
        AnchorMacroConfig def = new AnchorMacroConfig();
        this.anchorSlot = def.anchorSlot;
        this.glowstoneSlot = def.glowstoneSlot;
        this.totemSlot = def.totemSlot;
        this.delayPlaceAnchor = def.delayPlaceAnchor;
        this.delaySwitchToGlowstone = def.delaySwitchToGlowstone;
        this.delayChargeAnchor = def.delayChargeAnchor;
        this.delaySwitchToTotem = def.delaySwitchToTotem;
        this.delayExplodeAnchor = def.delayExplodeAnchor;
        this.safeAnchorMode = def.safeAnchorMode;
        this.explodeOnlyIfTotemPresent = def.explodeOnlyIfTotemPresent;
        this.showNotifications = def.showNotifications;
    }

    /** Reloads the configuration from disk safely */
    public static void reload() {
        try {
            AnchorMacroConfig newCfg = load();
            INSTANCE = newCfg;
            AnchorMacroClient.log("Config reloaded successfully.");
        } catch (Exception e) {
            AnchorMacroClient.log("Failed to reload config, keeping old settings: " + e.getMessage());
        }
    }
}
