package com.example.sbadditions;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class Commands {

    private record CE(Supplier<String> getter, Function<String, String> setter) {}

    private static final LinkedHashMap<String, CE> CFG = buildCfg();

    private static LinkedHashMap<String, CE> buildCfg() {
        var m = new LinkedHashMap<String, CE>();

        // Wind HUD
        b(m, "wind.enabled",               () -> SBAdditionsConfig.INSTANCE.windEnabled,              v -> SBAdditionsConfig.INSTANCE.windEnabled = v);
        b(m, "wind.hud",                   () -> SBAdditionsConfig.INSTANCE.windHudEnabled,           v -> SBAdditionsConfig.INSTANCE.windHudEnabled = v);
        b(m, "wind.autoaim",               () -> SBAdditionsConfig.INSTANCE.windAutoAim,              v -> SBAdditionsConfig.INSTANCE.windAutoAim = v);
        d(m, "wind.speed",                 () -> SBAdditionsConfig.INSTANCE.windAutoSpeed,            v -> SBAdditionsConfig.INSTANCE.windAutoSpeed = v);
        b(m, "wind.invert",                () -> SBAdditionsConfig.INSTANCE.windInvert,               v -> SBAdditionsConfig.INSTANCE.windInvert = v);
        b(m, "wind.only_crouching",        () -> SBAdditionsConfig.INSTANCE.windOnlyCrouching,        v -> SBAdditionsConfig.INSTANCE.windOnlyCrouching = v);
        b(m, "wind.only_breaking",         () -> SBAdditionsConfig.INSTANCE.windOnlyBreaking,         v -> SBAdditionsConfig.INSTANCE.windOnlyBreaking = v);
        b(m, "wind.arrow_autoaim",         () -> SBAdditionsConfig.INSTANCE.windArrowAutoAim,          v -> SBAdditionsConfig.INSTANCE.windArrowAutoAim = v);
        d(m, "wind.arrow_speed",           () -> SBAdditionsConfig.INSTANCE.windArrowAutoSpeed,         v -> SBAdditionsConfig.INSTANCE.windArrowAutoSpeed = v);
        b(m, "wind.arrow_only_crouching",  () -> SBAdditionsConfig.INSTANCE.windArrowOnlyCrouching,    v -> SBAdditionsConfig.INSTANCE.windArrowOnlyCrouching = v);
        b(m, "wind.arrow_only_breaking",   () -> SBAdditionsConfig.INSTANCE.windArrowOnlyBreaking,     v -> SBAdditionsConfig.INSTANCE.windArrowOnlyBreaking = v);

        // HUD
        b(m, "hud.fps",                    () -> SBAdditionsConfig.INSTANCE.hudFpsEnabled,            v -> SBAdditionsConfig.INSTANCE.hudFpsEnabled = v);
        b(m, "hud.border",                 () -> SBAdditionsConfig.INSTANCE.hudBorderEnabled,          v -> SBAdditionsConfig.INSTANCE.hudBorderEnabled = v);
        b(m, "hud.tps",                    () -> SBAdditionsConfig.INSTANCE.hudTpsEnabled,            v -> SBAdditionsConfig.INSTANCE.hudTpsEnabled = v);
        b(m, "hud.clock",                  () -> SBAdditionsConfig.INSTANCE.hudClockEnabled,          v -> SBAdditionsConfig.INSTANCE.hudClockEnabled = v);
        b(m, "hud.clock_12hour",           () -> SBAdditionsConfig.INSTANCE.hudClock12Hour,             v -> SBAdditionsConfig.INSTANCE.hudClock12Hour = v);
        b(m, "hud.locked",                 () -> SBAdditionsConfig.INSTANCE.hudLocked,                v -> SBAdditionsConfig.INSTANCE.hudLocked = v);
        i(m, "hud.alpha",                  () -> SBAdditionsConfig.INSTANCE.hudAlpha,                 v -> SBAdditionsConfig.INSTANCE.hudAlpha = Math.max(0, Math.min(255, v)));

        // Tool Swap
        b(m, "toolswap.enabled",           () -> SBAdditionsConfig.INSTANCE.toolSwapEnabled,          v -> SBAdditionsConfig.INSTANCE.toolSwapEnabled = v);
        b(m, "toolswap.wildcard",          () -> SBAdditionsConfig.INSTANCE.toolSwapWildcard,         v -> SBAdditionsConfig.INSTANCE.toolSwapWildcard = v);
        b(m, "toolswap.only_breaking",     () -> SBAdditionsConfig.INSTANCE.toolSwapOnlyBreaking,     v -> SBAdditionsConfig.INSTANCE.toolSwapOnlyBreaking = v);
        b(m, "toolswap.only_crouching",    () -> SBAdditionsConfig.INSTANCE.toolSwapOnlyCrouching,    v -> SBAdditionsConfig.INSTANCE.toolSwapOnlyCrouching = v);

        b(m, "toolswap.prevent_on_weapon",        () -> SBAdditionsConfig.INSTANCE.toolSwapPreventOnWeapon,        v -> SBAdditionsConfig.INSTANCE.toolSwapPreventOnWeapon = v);
        b(m, "toolswap.pause_mining",             () -> SBAdditionsConfig.INSTANCE.toolSwapPauseMining,            v -> SBAdditionsConfig.INSTANCE.toolSwapPauseMining = v);
        b(m, "toolswap.weapon_enabled",           () -> SBAdditionsConfig.INSTANCE.toolSwapWeaponEnabled,          v -> SBAdditionsConfig.INSTANCE.toolSwapWeaponEnabled = v);
        b(m, "toolswap.weapon_only_breaking",     () -> SBAdditionsConfig.INSTANCE.toolSwapWeaponOnlyBreaking,     v -> SBAdditionsConfig.INSTANCE.toolSwapWeaponOnlyBreaking = v);
        b(m, "toolswap.weapon_only_crouching",    () -> SBAdditionsConfig.INSTANCE.toolSwapWeaponOnlyCrouching,    v -> SBAdditionsConfig.INSTANCE.toolSwapWeaponOnlyCrouching = v);
        b(m, "toolswap.weapon_attack_on_swap",    () -> SBAdditionsConfig.INSTANCE.weaponSwapAttackOnSwap,          v -> SBAdditionsConfig.INSTANCE.weaponSwapAttackOnSwap = v);

        // No Break
        b(m, "nobreak.enabled",            () -> SBAdditionsConfig.INSTANCE.noBreakEnabled,           v -> SBAdditionsConfig.INSTANCE.noBreakEnabled = v);
        b(m, "nobreak.whitelist",          () -> SBAdditionsConfig.INSTANCE.noBreakWhitelist,         v -> SBAdditionsConfig.INSTANCE.noBreakWhitelist = v);
        // noBreakRules and noInteractRules are managed via /ssbu nobreakrule and /ssbu nointeractrule list commands

        // No Interact
        b(m, "nointeract.enabled",         () -> SBAdditionsConfig.INSTANCE.noInteractEnabled,        v -> SBAdditionsConfig.INSTANCE.noInteractEnabled = v);
        b(m, "nointeract.prevent_use",     () -> SBAdditionsConfig.INSTANCE.noInteractPreventUse,     v -> SBAdditionsConfig.INSTANCE.noInteractPreventUse = v);
        b(m, "nointeract.prevent_ability", () -> SBAdditionsConfig.INSTANCE.noInteractPreventAbility, v -> SBAdditionsConfig.INSTANCE.noInteractPreventAbility = v);
        b(m, "nointeract.whitelist",       () -> SBAdditionsConfig.INSTANCE.noInteractWhitelist,      v -> SBAdditionsConfig.INSTANCE.noInteractWhitelist = v);

        return m;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static void b(LinkedHashMap<String, CE> m, String key,
            BooleanSupplier get, Consumer<Boolean> set) {
        m.put(key, new CE(
            () -> Boolean.toString(get.getAsBoolean()),
            raw -> {
                String lo = raw.trim().toLowerCase();
                Boolean val = switch (lo) {
                    case "true",  "on",  "1", "yes" -> true;
                    case "false", "off", "0", "no"  -> false;
                    case "toggle"                   -> !get.getAsBoolean();
                    default                         -> null;
                };
                if (val == null) return "Expected true/false/toggle, got: " + raw;
                set.accept(val);
                return null;
            }
        ));
    }

    private static void d(LinkedHashMap<String, CE> m, String key,
            DoubleSupplier get, DoubleConsumer set) {
        m.put(key, new CE(
            () -> String.valueOf(get.getAsDouble()),
            raw -> {
                try { set.accept(Double.parseDouble(raw.trim())); return null; }
                catch (NumberFormatException e) { return "Expected a number, got: " + raw; }
            }
        ));
    }

    @SuppressWarnings("unused")
    private static void i(LinkedHashMap<String, CE> m, String key,
            IntSupplier get, IntConsumer set) {
        m.put(key, new CE(
            () -> String.valueOf(get.getAsInt()),
            raw -> {
                try { set.accept(Integer.parseInt(raw.trim())); return null; }
                catch (NumberFormatException e) { return "Expected an integer, got: " + raw; }
            }
        ));
    }

    // ── command registration ──────────────────────────────────────────────────

    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("ssbu")
                    .then(blockListBranch("nobreak",       () -> SBAdditionsConfig.INSTANCE.noBreakBlocks,    "No Break block list"))
                    .then(blockListBranch("nobreakrule",   () -> SBAdditionsConfig.INSTANCE.noBreakRules,     "No Break rules (block tool)"))
                    .then(blockListBranch("nointeract",    () -> SBAdditionsConfig.INSTANCE.noInteractBlocks, "No Interact block list"))
                    .then(blockListBranch("nointeractrule",() -> SBAdditionsConfig.INSTANCE.noInteractRules,  "No Interact rules (block tool)"))
                    .then(blockListBranch("toolswap",       () -> SBAdditionsConfig.INSTANCE.toolSwapRules,        "Tool Swap rules"))
                    .then(blockListBranch("toolswapweapon", () -> SBAdditionsConfig.INSTANCE.toolSwapWeaponRules, "Tool Swap weapon rules"))
                    .then(configBranch())
                    .then(debugBranch())
            )
        );
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> debugBranch() {
        return ClientCommandManager.literal("debug")
            .then(ClientCommandManager.literal("sb")
                .executes(ctx -> {
                    net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
                    if (mc.world == null) { ctx.getSource().sendFeedback(Text.literal("[Debug] No world.")); return 0; }
                    net.minecraft.scoreboard.Scoreboard sb = mc.world.getScoreboard();
                    net.minecraft.scoreboard.ScoreboardObjective obj =
                            sb.getObjectiveForSlot(net.minecraft.scoreboard.ScoreboardDisplaySlot.SIDEBAR);
                    if (obj == null) { ctx.getSource().sendFeedback(Text.literal("[Debug] No sidebar scoreboard.")); return 0; }
                    ctx.getSource().sendFeedback(Text.literal("[Debug] Sidebar scoreboard lines:"));
                    int i = 0;
                    for (var entry : sb.getScoreboardEntries(obj)) {
                        if (entry.hidden()) continue;
                        String name = entry.owner();
                        net.minecraft.scoreboard.Team team = sb.getScoreHolderTeam(name);
                        String prefix = team != null ? team.getPrefix().getString() : "";
                        String suffix = team != null ? team.getSuffix().getString() : "";
                        String plain  = (prefix + name + suffix).replaceAll("§.", "");
                        String raw    = (prefix + name + suffix).replace("§", "[§]");
                        ctx.getSource().sendFeedback(Text.literal(
                            "  [" + i + "] plain=\"" + plain + "\"  raw=\"" + raw + "\""));
                        i++;
                    }
                    return 1;
                }));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> configBranch() {

        return ClientCommandManager.literal("config")
            .then(ClientCommandManager.literal("list")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal("[Config] All settings:"));
                    CFG.forEach((k, ce) ->
                        ctx.getSource().sendFeedback(Text.literal("  " + k + " = " + ce.getter().get())));
                    return 1;
                }))
            .then(ClientCommandManager.literal("get")
                .then(ClientCommandManager.argument("key", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String prefix = builder.getRemaining().toLowerCase();
                        CFG.keySet().stream().filter(k -> k.startsWith(prefix)).forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String key = StringArgumentType.getString(ctx, "key");
                        CE ce = CFG.get(key);
                        if (ce == null) {
                            ctx.getSource().sendFeedback(Text.literal("[Config] Unknown key: " + key + "  (use /ssbu config list)"));
                            return 0;
                        }
                        ctx.getSource().sendFeedback(Text.literal("[Config] " + key + " = " + ce.getter().get()));
                        return 1;
                    })))
            .then(ClientCommandManager.literal("set")
                .then(ClientCommandManager.argument("key", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        String prefix = builder.getRemaining().toLowerCase();
                        CFG.keySet().stream().filter(k -> k.startsWith(prefix)).forEach(builder::suggest);
                        return builder.buildFuture();
                    })
                    .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String key = StringArgumentType.getString(ctx, "key");
                            String val = StringArgumentType.getString(ctx, "value");
                            CE ce = CFG.get(key);
                            if (ce == null) {
                                ctx.getSource().sendFeedback(Text.literal("[Config] Unknown key: " + key + "  (use /ssbu config list)"));
                                return 0;
                            }
                            String err = ce.setter().apply(val);
                            if (err != null) {
                                ctx.getSource().sendFeedback(Text.literal("[Config] Error: " + err));
                                return 0;
                            }
                            SBAdditionsConfig.save();
                            ctx.getSource().sendFeedback(Text.literal("[Config] " + key + " = " + ce.getter().get()));
                            return 1;
                        }))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<
            net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> blockListBranch(
            String name, java.util.function.Supplier<List<String>> listGetter, String label) {

        return ClientCommandManager.literal(name)
            .then(ClientCommandManager.literal("list")
                .executes(ctx -> {
                    List<String> list = listGetter.get();
                    if (list.isEmpty()) {
                        ctx.getSource().sendFeedback(Text.literal("[" + label + "] Empty."));
                    } else {
                        ctx.getSource().sendFeedback(Text.literal("[" + label + "] " + list.size() + " entries:"));
                        for (int i = 0; i < list.size(); i++)
                            ctx.getSource().sendFeedback(Text.literal("  " + i + ": " + list.get(i)));
                    }
                    return 1;
                }))
            .then(ClientCommandManager.literal("add")
                .then(ClientCommandManager.argument("pattern", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String pattern = StringArgumentType.getString(ctx, "pattern").trim();
                        List<String> list = listGetter.get();
                        if (list.contains(pattern)) {
                            ctx.getSource().sendFeedback(Text.literal("[" + label + "] Already contains: " + pattern));
                            return 0;
                        }
                        list.add(pattern);
                        SBAdditionsConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("[" + label + "] Added: " + pattern));
                        return 1;
                    })))
            .then(ClientCommandManager.literal("remove")
                .then(ClientCommandManager.argument("pattern", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String pattern = StringArgumentType.getString(ctx, "pattern").trim();
                        List<String> list = listGetter.get();
                        if (list.remove(pattern)) {
                            SBAdditionsConfig.save();
                            ctx.getSource().sendFeedback(Text.literal("[" + label + "] Removed: " + pattern));
                            return 1;
                        }
                        ctx.getSource().sendFeedback(Text.literal("[" + label + "] Not found: " + pattern));
                        return 0;
                    })))
            .then(ClientCommandManager.literal("set")
                .then(ClientCommandManager.argument("patterns", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        String[] tokens = StringArgumentType.getString(ctx, "patterns").trim().split("\\s+");
                        List<String> list = listGetter.get();
                        list.clear();
                        for (String t : tokens) if (!t.isBlank()) list.add(t);
                        SBAdditionsConfig.save();
                        ctx.getSource().sendFeedback(Text.literal("[" + label + "] Set to: " + list));
                        return 1;
                    })))
            .then(ClientCommandManager.literal("clear")
                .executes(ctx -> {
                    listGetter.get().clear();
                    SBAdditionsConfig.save();
                    ctx.getSource().sendFeedback(Text.literal("[" + label + "] Cleared."));
                    return 1;
                }));
    }
}
