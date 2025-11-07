package com.anchormacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON config persisted in config/anchormacro.json
 */
public class AnchorMacroConfig {
    public boolean anchorMacroEnabled = true;

    public boolean autoTotemEnabled = true;
    public boolean totemHitEnabled = true;
    public boolean hitboxEnabled = true;

    // hitbox settings
    public float hitboxExpand = 0.5f;
    public float hitboxDistance = 6.0f;

    private static final Path CONFIG_PATH = Path.of("config", "anchormacro.json");
    private static AnchorMacroConfig INSTANCE;
    private static final Gson G = new GsonBuilder().setPrettyPrinting().create();

    public static AnchorMacroConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                    INSTANCE = G.fromJson(r, AnchorMacroConfig.class);
                }
            } else {
                INSTANCE = new AnchorMacroConfig();
                INSTANCE.save();
            }
        } catch (Exception e) {
            AnchorMacroClient.log("Failed to load config: " + e.getMessage());
            INSTANCE = new AnchorMacroConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                G.toJson(this, w);
            }
        } catch (Exception e) {
            AnchorMacroClient.log("Failed to save config: " + e.getMessage());
        }
    }
}
