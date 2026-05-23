package com.example.sbadditions.modules;

import com.example.sbadditions.SBAdditionsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class NoInteract {

    private static KeyBinding breakToggleKey;
    private static KeyBinding interactToggleKey;

    @SuppressWarnings("deprecation")
    public static void init() {
        breakToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sb-additions.no-break",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_WORLD_2,
            new KeyBinding.Category(Identifier.of("sb-additions", "category"))
        ));

        interactToggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sb-additions.no-interact",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_8,
            new KeyBinding.Category(Identifier.of("sb-additions", "category"))
        ));

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            while (breakToggleKey.wasPressed()) {
                SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
                cfg.noBreakEnabled = !cfg.noBreakEnabled;
                SBAdditionsConfig.save();
                if (mc.player != null)
                    mc.player.sendMessage(
                        Text.literal("[No Break] " + (cfg.noBreakEnabled ? "ON" : "OFF")), true);
            }
            while (interactToggleKey.wasPressed()) {
                SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
                cfg.noInteractEnabled = !cfg.noInteractEnabled;
                SBAdditionsConfig.save();
                if (mc.player != null)
                    mc.player.sendMessage(
                        Text.literal("[No Interact] " + (cfg.noInteractEnabled ? "ON" : "OFF")), true);
            }
        });

        // ── No Break — left-click mining ──────────────────────────────────────
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClient()) return ActionResult.PASS;
            SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
            if (!cfg.noBreakEnabled) return ActionResult.PASS;
            net.minecraft.block.Block blk = world.getBlockState(pos).getBlock();
            String blockId   = Registries.BLOCK.getId(blk).toString();
            String blockName = blk.getName().getString().toLowerCase();
            if (isBreakActive(MinecraftClient.getInstance(), cfg) && shouldBreakBlock(blockId, blockName, cfg))
                return ActionResult.FAIL;
            if (matchesRule(blockId, blockName, player.getMainHandStack(), cfg.noBreakRules))
                return ActionResult.FAIL;
            return ActionResult.PASS;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
            if (!cfg.noBreakEnabled) return;
            if (mc.player == null || mc.world == null || !mc.options.attackKey.isPressed()) return;
            if (mc.crosshairTarget instanceof BlockHitResult bhr) {
                net.minecraft.block.Block blk = mc.world.getBlockState(bhr.getBlockPos()).getBlock();
                String blockId   = Registries.BLOCK.getId(blk).toString();
                String blockName = blk.getName().getString().toLowerCase();
                boolean block = (isBreakActive(mc, cfg) && shouldBreakBlock(blockId, blockName, cfg))
                             || matchesRule(blockId, blockName, mc.player.getMainHandStack(), cfg.noBreakRules);
                if (block) mc.interactionManager.cancelBlockBreaking();
            }
        });

        // ── No Interact — right-click interactions ────────────────────────────
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;
            SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
            if (!cfg.noInteractEnabled) return ActionResult.PASS;
            net.minecraft.block.Block blk = world.getBlockState(hitResult.getBlockPos()).getBlock();
            String blockId   = Registries.BLOCK.getId(blk).toString();
            String blockName = blk.getName().getString().toLowerCase();
            if (matchesRule(blockId, blockName, player.getMainHandStack(), cfg.noInteractRules))
                return ActionResult.FAIL;
            if (!isInteractActive(MinecraftClient.getInstance(), cfg)) return ActionResult.PASS;
            if (cfg.noInteractPreventAbility) return ActionResult.FAIL;
            if (!cfg.noInteractPreventUse) return ActionResult.PASS;
            return shouldInteractBlock(blockId, blockName, cfg) ? ActionResult.FAIL : ActionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient()) return ActionResult.PASS;
            SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
            if (!isInteractActive(MinecraftClient.getInstance(), cfg) || !cfg.noInteractPreventAbility)
                return ActionResult.PASS;
            return ActionResult.FAIL;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient()) return ActionResult.PASS;
            SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
            if (!isInteractActive(MinecraftClient.getInstance(), cfg) || !cfg.noInteractPreventAbility)
                return ActionResult.PASS;
            return ActionResult.FAIL;
        });
    }

    private static boolean isBreakActive(MinecraftClient mc, SBAdditionsConfig cfg) {
        return cfg.noBreakEnabled && mc.player != null;
    }

    private static boolean isInteractActive(MinecraftClient mc, SBAdditionsConfig cfg) {
        return cfg.noInteractEnabled && mc.player != null;
    }

    private static boolean shouldBreakBlock(String blockId, String blockName, SBAdditionsConfig cfg) {
        boolean inList = matchesAny(blockId, blockName, cfg.noBreakBlocks);
        return cfg.noBreakWhitelist ? !inList : inList;
    }

    private static boolean shouldInteractBlock(String blockId, String blockName, SBAdditionsConfig cfg) {
        boolean inList = matchesAny(blockId, blockName, cfg.noInteractBlocks);
        return cfg.noInteractWhitelist ? !inList : inList;
    }

    private static boolean matchesRule(String blockId, String blockName,
                                       ItemStack held, List<String> rules) {
        if (rules == null || rules.isEmpty()) return false;
        for (String rule : rules) {
            if (rule == null || rule.isBlank()) continue;
            String[] parts = rule.trim().split("\\s+", 3);
            if (parts.length < 2) continue;

            // New format: toolPat W|B block1, block2, ...
            if (parts.length >= 3 && (parts[1].equalsIgnoreCase("W") || parts[1].equalsIgnoreCase("B"))) {
                if (!matchesToolName(held, List.of(parts[0]))) continue;
                boolean whitelist = parts[1].equalsIgnoreCase("W");
                boolean inList = false;
                for (String bp : parts[2].split(",")) {
                    if (matchesBlockPattern(blockId, blockName, bp.trim())) { inList = true; break; }
                }
                if (whitelist ? inList : !inList) return true;
                continue;
            }

            // Legacy format: blockPat toolPat
            if (!matchesBlockPattern(blockId, blockName, parts[0])) continue;
            if (matchesToolName(held, List.of(parts[1]))) return true;
        }
        return false;
    }

    private static boolean matchesBlockPattern(String blockId, String blockName, String pattern) {
        String lp = pattern.toLowerCase();
        return blockId.toLowerCase().contains(lp) || blockName.contains(lp.replace('_', ' '));
    }

    private static boolean matchesToolName(ItemStack stack, List<String> patterns) {
        if (stack.isEmpty()) return false;
        String displayName = stack.getName().getString().toLowerCase();
        for (String p : patterns) {
            if (p != null && !p.isBlank() && displayName.contains(p.toLowerCase().replace('_', ' ')))
                return true;
        }
        return false;
    }

    private static boolean matchesAny(String blockId, String blockName, List<String> patterns) {
        if (patterns == null) return false;
        String lowerId   = blockId.toLowerCase();
        String lowerName = blockName.toLowerCase();
        for (String p : patterns) {
            if (p == null || p.isBlank()) continue;
            String lowerP = p.toLowerCase();
            if (lowerId.contains(lowerP) || lowerName.contains(lowerP.replace('_', ' '))) return true;
        }
        return false;
    }
}
