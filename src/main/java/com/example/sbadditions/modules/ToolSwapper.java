package com.example.sbadditions.modules;

import com.example.sbadditions.SBAdditionsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class ToolSwapper {

    // State machine for inventory-based swaps:
    // 0 = idle, 1 = inventory opened (wait 1 tick), 2 = perform click + close
    private static int swapPhase      = 0;
    private static int pendingInvSlot = -1; // handler slot index (9-35)
    private static int pendingHotbar  = -1; // hotbar slot to swap into (0-8)

    private static int pauseMiningTicks = 0; // ticks remaining where block-break is suppressed after a swap

    private static boolean blockNextAttack = false;
    private static net.minecraft.entity.Entity pendingAttackTarget = null;

    public static boolean consumeBlockNextAttack() {
        if (blockNextAttack) { blockNextAttack = false; return true; }
        return false;
    }

    private static KeyBinding toggleKey;
    private static KeyBinding weaponSwapKey;

    @SuppressWarnings("deprecation")
    public static void init() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sb-additions.tool-swap",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_WORLD_1,
            new KeyBinding.Category(Identifier.of("sb-additions", "category"))
        ));
        weaponSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sb-additions.weapon-swap",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_0,
            new KeyBinding.Category(Identifier.of("sb-additions", "category"))
        ));

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            while (toggleKey.wasPressed()) {
                SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
                cfg.toolSwapEnabled = !cfg.toolSwapEnabled;
                SBAdditionsConfig.save();
                if (mc.player != null)
                    mc.player.sendMessage(
                        Text.literal("[Tool Swap] " + (cfg.toolSwapEnabled ? "ON" : "OFF")), true);
            }
            while (weaponSwapKey.wasPressed()) {
                SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
                cfg.toolSwapWeaponEnabled = !cfg.toolSwapWeaponEnabled;
                SBAdditionsConfig.save();
                if (mc.player != null)
                    mc.player.sendMessage(
                        Text.literal("[Weapon Swap] " + (cfg.toolSwapWeaponEnabled ? "ON" : "OFF")), true);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(ToolSwapper::onTick);
    }

    private static void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        if (pendingAttackTarget != null) {
            net.minecraft.entity.Entity target = pendingAttackTarget;
            pendingAttackTarget = null;
            if (!target.isRemoved()) mc.interactionManager.attackEntity(mc.player, target);
        }

        if (pauseMiningTicks > 0) {
            pauseMiningTicks--;
            if (mc.crosshairTarget instanceof BlockHitResult bhr
                    && mc.options.attackKey.isPressed()
                    && mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                    new net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket(
                        net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                        bhr.getBlockPos(),
                        bhr.getSide()
                    )
                );
            }
        }

        if (swapPhase > 0) {
            tickSwap(mc);
            return;
        }

        SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
        if (!cfg.toolSwapEnabled) return;
        if (mc.currentScreen != null) return;
        if (cfg.toolSwapOnlyBreaking  && !mc.options.attackKey.isPressed()) return;
        if (cfg.toolSwapOnlyCrouching && !mc.player.isSneaking()) return;
        if (cfg.toolSwapPreventOnWeapon && isWeapon(mc.player.getInventory().getStack(mc.player.getInventory().selectedSlot))) return;

        // Weapon swap: only skip block rules if a weapon rule actually matched.
        if (mc.crosshairTarget instanceof net.minecraft.util.hit.EntityHitResult ehr
                && cfg.toolSwapWeaponEnabled
                && (!cfg.toolSwapWeaponOnlyBreaking  || mc.options.attackKey.isPressed())
                && (!cfg.toolSwapWeaponOnlyCrouching || mc.player.isSneaking())) {
            net.minecraft.entity.Entity entity = ehr.getEntity();
            for (String ruleStr : cfg.toolSwapWeaponRules) {
                if (ruleStr == null || ruleStr.isBlank()) continue;
                String[] parts = ruleStr.trim().split("\\s+", 2);
                if (parts.length < 2) continue;
                String entityPat = parts[0].toLowerCase().replace('_', ' ');
                String weaponPat = parts[1].trim();
                if (!matchesEntityPat(entity, entityPat, mc)) continue;
                int curSlot = mc.player.getInventory().selectedSlot;
                if (!matchesItem(mc.player.getInventory().getStack(curSlot), weaponPat, cfg.toolSwapWildcard)) {
                    for (int i = 0; i < 9; i++) {
                        if (matchesItem(mc.player.getInventory().getStack(i), weaponPat, cfg.toolSwapWildcard)) {
                            mc.player.getInventory().selectedSlot = i;
                            if (cfg.toolSwapPauseMining) pauseMiningTicks = 1;
                            if (cfg.weaponSwapAttackOnSwap) { blockNextAttack = true; pendingAttackTarget = entity; }
                            break;
                        }
                    }
                }
                return; // rule matched — skip block rules
            }
            // no weapon rule matched — fall through to block rules
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
        if (bhr.getType() != HitResult.Type.BLOCK) return;

        net.minecraft.block.Block blk = mc.world.getBlockState(bhr.getBlockPos()).getBlock();
        String blockId   = Registries.BLOCK.getId(blk).toString();
        String blockName = blk.getName().getString().toLowerCase();

        String currentArea = getCurrentArea(mc);

        for (String ruleStr : cfg.toolSwapRules) {
            if (ruleStr == null || ruleStr.isBlank()) continue;
            String[] parts = ruleStr.trim().split("\\s+", 3);
            if (parts.length < 2) continue;
            String blockPat = parts[0];
            String toolPat  = parts[1].trim();
            if (parts.length >= 3) {
                String areaPat = parts[2].trim().toLowerCase().replace('_', ' ');
                if (!currentArea.contains(areaPat)) continue;
            }

            if (!matchesBlock(blockId, blockName, blockPat, cfg.toolSwapWildcard)) continue;

            int curSlot = mc.player.getInventory().selectedSlot;
            if (matchesItem(mc.player.getInventory().getStack(curSlot), toolPat, cfg.toolSwapWildcard)) break;

            boolean found = false;
            for (int i = 0; i < 9; i++) {
                if (matchesItem(mc.player.getInventory().getStack(i), toolPat, cfg.toolSwapWildcard)) {
                    mc.player.getInventory().selectedSlot = i;
                    if (cfg.toolSwapPauseMining) pauseMiningTicks = 1;
                    found = true;
                    break;
                }
            }

            if (!found && cfg.toolSwapSearchInventory) {
                for (int i = 9; i < 36; i++) {
                    if (matchesItem(mc.player.getInventory().getStack(i), toolPat, cfg.toolSwapWildcard)) {
                        pendingInvSlot = i;
                        pendingHotbar  = curSlot;
                        mc.setScreen(new InventoryScreen(mc.player));
                        swapPhase = 1;
                        break;
                    }
                }
            }
            break;
        }
    }

    private static void tickSwap(MinecraftClient mc) {
        if (swapPhase == 1) {
            swapPhase = 2;
        } else if (swapPhase == 2) {
            mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                pendingInvSlot,
                pendingHotbar,
                SlotActionType.SWAP,
                mc.player
            );
            mc.player.closeHandledScreen();
            swapPhase      = 0;
            pendingInvSlot = -1;
            pendingHotbar  = -1;
        }
    }

    private static String getCurrentArea(MinecraftClient mc) {
        if (mc.world == null) return "";
        Scoreboard sb = mc.world.getScoreboard();
        ScoreboardObjective obj = sb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (obj == null) return "";
        StringBuilder result = new StringBuilder();
        for (var entry : sb.getScoreboardEntries(obj)) {
            if (entry.hidden()) continue;
            String name = entry.owner();
            Team team = sb.getScoreHolderTeam(name);
            String raw = team != null
                ? team.getPrefix().getString() + name + team.getSuffix().getString()
                : name;
            result.append(stripCodes(raw).toLowerCase()).append(' ');
        }
        return result.toString();
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem) return true;
        String path = Registries.ITEM.getId(stack.getItem()).getPath();
        if (path.contains("sword")) return true;
        // Axes and prismarine shards can be weapons or mining tools — check lore for damage stats
        if (path.endsWith("_axe") || path.equals("prismarine_shard")) return loreMentionsDamage(stack);
        return false;
    }

    private static boolean loreMentionsDamage(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return false;
        boolean hasDamage = false;
        for (Text line : lore.lines()) {
            String lower = stripCodes(line.getString()).toLowerCase();
            if (lower.contains("mining speed") || lower.contains("breaking power") || lower.contains("mining fortune")) return false;
            if (lower.contains("damage")) hasDamage = true;
        }
        return hasDamage;
    }

    private static String stripCodes(String s) {
        return s.replaceAll("§.", "");
    }

    private static boolean matchesEntityPat(net.minecraft.entity.Entity entity, String pat, MinecraftClient mc) {
        return switch (pat) {
            case "monsters", "hostile", "hostiles" ->
                entity instanceof net.minecraft.entity.mob.HostileEntity;
            case "angry", "targeting me", "targeting_me", "all angry" -> {
                // mob.getTarget() is not synced to the client on Hypixel; use head yaw instead.
                // Also works for player-entity NPCs that Hypixel renders as players.
                if (entity == mc.player) yield false;
                boolean isMobOrPlayerNpc = entity instanceof net.minecraft.entity.mob.MobEntity
                        || entity instanceof net.minecraft.entity.player.PlayerEntity;
                if (!isMobOrPlayerNpc) yield false;
                if (mc.player == null) yield false;
                double dx = mc.player.getX() - entity.getX();
                double dz = mc.player.getZ() - entity.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist < 0.5) yield true;
                double toPlayer = Math.toDegrees(Math.atan2(-dx, dz));
                double diff = Math.abs(((toPlayer - entity.getHeadYaw()) % 360 + 540) % 360 - 180);
                yield diff < 60;
            }
            case "mobs", "all mobs", "all" ->
                entity instanceof net.minecraft.entity.mob.MobEntity;
            case "players", "player", "player mobs" ->
                entity instanceof net.minecraft.entity.player.PlayerEntity && entity != mc.player;
            default -> {
                String tn = stripCodes(entity.getType().getName().getString()).toLowerCase();
                String dn = stripCodes(entity.getDisplayName().getString()).toLowerCase();
                yield tn.contains(pat) || dn.contains(pat);
            }
        };
    }

    private static final java.util.Map<String, String> BLOCK_ALIASES = java.util.Map.of(
        "mithril_all", "mithril|gray_wool|cyan_terracotta|light_blue_wool",
        "mithril",     "mithril|gray_wool|cyan_terracotta|light_blue_wool",
        "titanium",    "polished_diorite"
    );

    private static boolean matchesBlock(String blockId, String blockName, String pattern, boolean wildcard) {
        String resolved = BLOCK_ALIASES.getOrDefault(pattern.toLowerCase(), pattern);
        if (resolved.contains("|")) {
            for (String sub : resolved.split("\\|"))
                if (matchesBlockSingle(blockId, blockName, sub.trim(), wildcard)) return true;
            return false;
        }
        return matchesBlockSingle(blockId, blockName, resolved, wildcard);
    }

    private static boolean matchesBlockSingle(String blockId, String blockName, String pattern, boolean wildcard) {
        String lowerPat  = pattern.toLowerCase();
        String lowerName = lowerPat.replace('_', ' ');
        if (wildcard) return blockId.toLowerCase().contains(lowerPat) || blockName.contains(lowerName);
        String fullPat = lowerPat.contains(":") ? lowerPat : "minecraft:" + lowerPat;
        return blockId.toLowerCase().equals(fullPat) || blockName.equals(lowerName);
    }

    private static boolean matchesItem(ItemStack stack, String pattern, boolean wildcard) {
        if (stack.isEmpty()) return false;
        String displayName = stripCodes(stack.getName().getString()).toLowerCase();
        String normPat     = pattern.toLowerCase().replace('_', ' ');
        String registryId  = Registries.ITEM.getId(stack.getItem()).toString().toLowerCase();

        // Always use contains for display names — Hypixel items often have decorative
        // prefix characters (e.g. "✦ Mithril Pickaxe") that would break an equals check.
        if (displayName.contains(normPat)) return true;
        if (wildcard) return registryId.contains(pattern.toLowerCase());
        String fullPat = pattern.toLowerCase().contains(":")
                ? pattern.toLowerCase()
                : "minecraft:" + pattern.toLowerCase();
        return registryId.equals(fullPat);
    }
}
