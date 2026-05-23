package com.example.sbadditions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SBAdditionsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("sb-additions.json");

    public static SBAdditionsConfig INSTANCE = new SBAdditionsConfig();

    // Wind HUD
    public boolean windEnabled            = false;
    public boolean windHudEnabled         = true;
    public boolean windAutoAim            = false;
    public double  windAutoSpeed          = 1.5;
    public boolean windInvert             = false;
    public boolean windOnlyCrouching      = false;
    public boolean windOnlyBreaking       = false;
    public boolean windArrowAutoAim       = false;
    public double  windArrowAutoSpeed     = 1.5;
    public boolean windArrowOnlyCrouching = false;
    public boolean windArrowOnlyBreaking  = false;

    // TPS / Ping HUD
    public boolean hudTpsEnabled   = true;
    public boolean hudClockEnabled    = false;
    public boolean hudClock12Hour     = false;
    public boolean hudFpsEnabled   = false;
    public boolean hudLocked       = false;
    public int     hudAlpha        = 255;
    public int     hudTpsX         = 5;
    public int     hudTpsY         = 5;
    public boolean hudBorderEnabled = true;
    public int     hudBorderColor  = 0xFFFFFF;
    public int     hudColorGood    = 0x55FF55;
    public int     hudColorWarn    = 0xFFFF55;
    public int     hudColorBad     = 0xFF5555;
    public int     hudColorUnknown = 0xAAAAAA;

    // Tool Swapper
    public boolean toolSwapEnabled         = false;
    public boolean toolSwapWildcard        = true;
    public boolean toolSwapOnlyBreaking    = false;
    public boolean toolSwapOnlyCrouching   = false;
    public boolean toolSwapSearchInventory = false;
    public boolean toolSwapPreventOnWeapon = false;
    public boolean toolSwapPauseMining    = false;
    public List<String> toolSwapRules        = new ArrayList<>();
    public List<String> toolSwapWeaponRules       = new ArrayList<>(); // "entityPat weaponPat"
    public boolean toolSwapWeaponEnabled       = true;  // master toggle for weapon swap
    public boolean toolSwapWeaponOnlyBreaking  = false;
    public boolean toolSwapWeaponOnlyCrouching = false;
    public boolean weaponSwapAttackOnSwap   = false;

    // No Break
    public boolean noBreakEnabled   = false;
    public boolean noBreakWhitelist = true;
    public List<String> noBreakBlocks = new ArrayList<>(Arrays.asList(
        "stained_glass", "gemstone",
        "minecraft:prismarine", "minecraft:prismarine_bricks",
        "minecraft:cyan_terracotta", "minecraft:gray_wool"
    ));
    public List<String> noBreakTools  = new ArrayList<>(Arrays.asList("Pickonimbus"));
    public List<String> noBreakRules  = new ArrayList<>(Arrays.asList("gemstone pickonimbus")); // "blockPat toolPat"

    // No Interact
    public boolean noInteractEnabled        = false;
    public boolean noInteractPreventUse     = false;
    public boolean noInteractPreventAbility = false;
    public boolean noInteractWhitelist      = true;
    public List<String> noInteractBlocks = new ArrayList<>(Arrays.asList(
        "stained_glass", "gemstone",
        "minecraft:prismarine", "minecraft:prismarine_bricks",
        "minecraft:cyan_terracotta", "minecraft:gray_wool"
    ));
    public List<String> noInteractTools  = new ArrayList<>();
    public List<String> noInteractRules  = new ArrayList<>(); // "blockPat toolPat"

    public static void load() {
        if (!Files.exists(PATH)) { save(); return; }
        try (Reader r = Files.newBufferedReader(PATH)) {
            SBAdditionsConfig loaded = GSON.fromJson(r, SBAdditionsConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (IOException e) {
            System.err.println("[sb-additions] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        try (Writer w = Files.newBufferedWriter(PATH)) {
            GSON.toJson(INSTANCE, w);
        } catch (IOException e) {
            System.err.println("[sb-additions] Failed to save config: " + e.getMessage());
        }
    }
}
