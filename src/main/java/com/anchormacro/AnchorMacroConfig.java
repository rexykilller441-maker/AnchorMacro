package com.anchormacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AnchorMacroConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("anchormacro.json");
    private static AnchorMacroConfig INSTANCE;

    // GUI-exposed slot values: 1..9 (users), internally converted to 0..8
    public int anchorSlot = 1;
    public int glowstoneSlot = 2;
    public int totemSlot = 9;

    // delays in ticks (1 tick = 50 ms)
    public int delayPlaceAnchor = 4;
    public int delaySwitchToGlowstone = 2;
    public int delayChargeAnchor = 3;
    public int delaySwitchToTotem = 2;
    public int delayExplodeAnchor = 2;

    // flags
    public boolean safeAnchorMode = false;
    public boolean explodeOnlyIfTotemPresent = false;
    public boolean autoSearchHotbar = true;
    public boolean showNotifications = true;

    // test key (GLFW key code)
    public int testKey = org.lwjgl.glfw.GLFW.GLFW_KEY_K;

    private AnchorMacroConfig() {}

    public static AnchorMacroConfig get() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = null;
        get();
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
                AnchorMacroConfig def = new AnchorMacroConfig();
                saveStatic(def, gson);
                return def;
            }
        } catch (Exception e) {
            AnchorMacroClient.log("Failed to load config: " + e.getMessage());
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
        this.autoSearchHotbar = def.autoSearchHotbar;
        this.showNotifications = def.showNotifications;
        this.testKey = def.testKey;
    }

    // convenience: convert GUI slot (1..9) to internal index (0..8)
    public static int guiToInternalSlot(int guiSlot) {
        int s = guiSlot - 1;
        if (s < 0) s = 0;
        if (s > 8) s = 8;
        return s;
    }
}
