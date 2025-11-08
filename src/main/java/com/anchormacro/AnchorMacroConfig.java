package com.anchormacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class AnchorMacroConfig {

    private static final File CONFIG_FILE = new File("config/anchormacro.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Settings
    public boolean autoSearchHotbar = true;
    public boolean explodeOnlyIfTotemPresent = true;
    public boolean hitboxEnabled = true;
    public float hitboxExpand = 0.4f;
    public float hitboxDistance = 10.0f;
    public boolean safeAnchorMode = false;

    private static AnchorMacroConfig INSTANCE;

    public static AnchorMacroConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (!CONFIG_FILE.exists()) {
                INSTANCE = new AnchorMacroConfig();
                save();
                return;
            }
            try (FileReader r = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(r, AnchorMacroConfig.class);
            }
        } catch (Exception e) {
            System.err.println("[AnchorMacro] Failed to load config: " + e.getMessage());
            INSTANCE = new AnchorMacroConfig();
        }
    }

    public static void save() {
        try {
            if (INSTANCE == null) INSTANCE = new AnchorMacroConfig();
            CONFIG_FILE.getParentFile().mkdirs();
            try (FileWriter w = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(INSTANCE, w);
            }
        } catch (Exception e) {
            System.err.println("[AnchorMacro] Failed to save config: " + e.getMessage());
        }
    }
}
