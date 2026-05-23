package com.example.sbadditions.modules;

import com.example.sbadditions.SBAdditionsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class WindHud {

    private static float   windOffset = 0f;
    private static boolean windFound  = false;
    private static boolean darkGreen  = false;
    private static boolean arrowLeft  = false;
    private static boolean arrowRight = false;

    private static KeyBinding toggleKey;

    @SuppressWarnings("deprecation")
    public static void init() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sb-additions.wind-hud",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_7,
            new KeyBinding.Category(Identifier.of("sb-additions", "category"))
        ));

        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            while (toggleKey.wasPressed()) {
                SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
                cfg.windEnabled = !cfg.windEnabled;
                SBAdditionsConfig.save();
                if (mc.player != null)
                    mc.player.sendMessage(
                        Text.literal("[Wind] " + (cfg.windEnabled ? "ON" : "OFF")), true);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(WindHud::onTick);
        HudRenderCallback.EVENT.register(WindHud::render);
    }

    private static void onTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            windFound = arrowLeft = arrowRight = false;
            return;
        }
        SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;

        // Always read scoreboard so arrow auto-aim works even when windAutoAim is off.
        String rawLine = findLine(mc, "≈");
        if (rawLine == null) {
            windFound = false;
        } else {
            windFound  = true;
            darkGreen  = rawLine.contains("§2");
            windOffset = parseOffset(rawLine, "≈");
            if (cfg.windInvert) windOffset = -windOffset;
        }
        arrowLeft  = findLine(mc, "⋖") != null;
        arrowRight = findLine(mc, "⋗") != null;

        if (mc.currentScreen != null) return;

        // Proportional auto-aim based on ≈ position.
        if (cfg.windEnabled && cfg.windAutoAim && windFound && !darkGreen) {
            if ((!cfg.windOnlyCrouching || mc.player.isSneaking())
                    && (!cfg.windOnlyBreaking || mc.options.attackKey.isPressed())) {
                mc.player.setYaw(mc.player.getYaw() + (float)(windOffset * cfg.windAutoSpeed));
            }
        }

        // Constant-speed arrow auto-aim — direction from ‹/› symbols, falls back to ≈ offset.
        if (cfg.windEnabled && cfg.windArrowAutoAim
                && !(cfg.windArrowOnlyCrouching && !mc.player.isSneaking())
                && !(cfg.windArrowOnlyBreaking  && !mc.options.attackKey.isPressed())) {
            boolean goLeft  = arrowLeft  && !arrowRight;
            boolean goRight = arrowRight && !arrowLeft;
            if (!goLeft && !goRight && windFound && !darkGreen) {
                goLeft  = windOffset < -0.05f;
                goRight = windOffset >  0.05f;
            }
            if (goLeft)  mc.player.setYaw(mc.player.getYaw() - (float) cfg.windArrowAutoSpeed);
            if (goRight) mc.player.setYaw(mc.player.getYaw() + (float) cfg.windArrowAutoSpeed);
        }
    }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!SBAdditionsConfig.INSTANCE.windHudEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cy = sh / 2 - 4;

        if (!windFound && !arrowLeft && !arrowRight) return;

        if (!windFound) {
            if (arrowLeft)  drawArrow(ctx, mc, "⋖", 2, cy, 0xFF55FF55);
            if (arrowRight) drawArrow(ctx, mc, "⋗", sw - 14, cy, 0xFF55FF55);
            return;
        }

        if (darkGreen) {
            ctx.fill(sw / 2 - 2, cy + 2, sw / 2 + 2, cy + 6, 0xFF2D7A27);
            return;
        }

        float abs   = Math.abs(windOffset);
        int   color = abs > 0.5f ? 0xFF55FF55 : 0xFF2D7A27;

        if (windOffset < -0.05f) {
            int x = Math.max(2, (int)(10 - abs * 8));
            drawArrow(ctx, mc, "⋖", x, cy, color);
            if (abs > 0.5f) drawArrow(ctx, mc, "⋖", x + 8, cy, color);
        } else if (windOffset > 0.05f) {
            int x = sw - 14 + (int)(abs * 8);
            drawArrow(ctx, mc, "⋗", x - 8, cy, color);
            if (abs > 0.5f) drawArrow(ctx, mc, "⋗", x, cy, color);
        }
    }

    private static void drawArrow(DrawContext ctx, MinecraftClient mc, String sym, int x, int y, int color) {
        ctx.fill(x - 2, y - 2, x + mc.textRenderer.getWidth(sym) + 2, y + mc.textRenderer.fontHeight + 2, 0xAA000000);
        ctx.drawText(mc.textRenderer, sym, x, y, color, false);
    }

    private static String findLine(MinecraftClient mc, String search) {
        Scoreboard sb  = mc.world.getScoreboard();
        ScoreboardObjective obj = sb.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (obj == null) return null;
        for (var entry : sb.getScoreboardEntries(obj)) {
            if (entry.hidden()) continue;
            String name = entry.owner();
            Team   team = sb.getScoreHolderTeam(name);
            String raw  = team != null
                    ? team.getPrefix().getString() + name + team.getSuffix().getString()
                    : name;
            if (stripCodes(raw).contains(search)) return raw;
        }
        return null;
    }

    private static String stripCodes(String s) {
        return s.replaceAll("§.", "");
    }

    private static float parseOffset(String rawLine, String search) {
        String plain = stripCodes(rawLine);
        int idx = plain.indexOf(search);
        if (idx < 0) return 0f;
        int len = plain.length();
        if (len <= search.length()) return 0f;
        float pos = (float) idx / (len - search.length());
        return (pos - 0.5f) * 2f;
    }
}
