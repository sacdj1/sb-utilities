package com.example.sbadditions.modules;

import com.example.sbadditions.PingTracker;
import com.example.sbadditions.SBAdditionsConfig;
import com.example.sbadditions.mixin.ClientPlayNetworkHandlerAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TpsHud {

    private static double measuredTps = 20.0;
    private static int    tickCount   = 0;
    private static long   lastMeasure = 0;
    private static int    pingTick    = 0;

    private static boolean dragging    = false;
    private static double  dragOffsetX = 0;
    private static double  dragOffsetY = 0;

    @SuppressWarnings("deprecation")
    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(TpsHud::onTick);
        HudRenderCallback.EVENT.register(TpsHud::render);
    }

    private static void onTick(MinecraftClient mc) {
        if (mc.world == null) { tickCount = 0; lastMeasure = 0; pingTick = 0; PingTracker.reset(); return; }
        tickCount++;
        long now = System.currentTimeMillis();
        if (lastMeasure == 0) { lastMeasure = now; return; }
        long elapsed = now - lastMeasure;
        if (elapsed >= 1000) {
            double rawTps = tickCount * 1000.0 / elapsed;
            measuredTps = measuredTps * 0.7 + Math.min(20.0, rawTps) * 0.3;
            tickCount   = 0;
            lastMeasure = now;
        }
        if (mc.getNetworkHandler() instanceof ClientPlayNetworkHandlerAccessor acc) {
            pingTick++;
            if (pingTick >= 40) {
                pingTick = 0;
                acc.getPingMeasurer().ping();
            }
        }
    }

    private static int colored(int a, int rgb) { return (a << 24) | (rgb & 0xFFFFFF); }

    private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
        SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;
        if (!cfg.hudTpsEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        int ping = PingTracker.get();

        String tpsLine   = String.format("TPS:  %.1f", measuredTps);
        String pingLine  = ping >= 0 ? String.format("Ping: %dms", ping) : "Ping: --";
        String fpsLine   = cfg.hudFpsEnabled ? String.format("FPS:  %d", mc.getCurrentFps()) : null;
        String clockLine = cfg.hudClockEnabled
                         ? LocalTime.now().format(DateTimeFormatter.ofPattern(cfg.hudClock12Hour ? "hh:mm:ss a" : "HH:mm:ss")) : null;

        int lineH  = mc.textRenderer.fontHeight;
        int padX   = 5, padY = 4;
        int innerW = Math.max(mc.textRenderer.getWidth(tpsLine),
                              mc.textRenderer.getWidth(pingLine));
        if (fpsLine   != null) innerW = Math.max(innerW, mc.textRenderer.getWidth(fpsLine));
        if (clockLine != null) innerW = Math.max(innerW, mc.textRenderer.getWidth(clockLine));
        int lines  = 2 + (fpsLine != null ? 1 : 0) + (clockLine != null ? 1 : 0);
        int boxW   = innerW + padX * 2;
        int boxH   = lineH * lines + padY * 2 + (lines - 1) * 2;

        if (!cfg.hudLocked && mc.currentScreen == null) {
            long    win  = mc.getWindow().getHandle();
            boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            double  sc   = mc.getWindow().getScaleFactor();
            int     mx   = (int)(mc.mouse.getX() / sc);
            int     my   = (int)(mc.mouse.getY() / sc);

            boolean over = mx >= cfg.hudTpsX && mx <= cfg.hudTpsX + boxW
                        && my >= cfg.hudTpsY && my <= cfg.hudTpsY + boxH;

            if (down && (over || dragging)) {
                if (!dragging) {
                    dragOffsetX = mx - cfg.hudTpsX;
                    dragOffsetY = my - cfg.hudTpsY;
                    dragging    = true;
                }
                int sw = mc.getWindow().getScaledWidth();
                int sh = mc.getWindow().getScaledHeight();
                cfg.hudTpsX = Math.max(0, Math.min(sw - boxW, (int)(mx - dragOffsetX)));
                cfg.hudTpsY = Math.max(0, Math.min(sh - boxH, (int)(my - dragOffsetY)));
            } else if (!down && dragging) {
                dragging = false;
                SBAdditionsConfig.save();
            }
        }

        int x = cfg.hudTpsX;
        int y = cfg.hudTpsY;

        ctx.fill(x, y, x + boxW, y + boxH, 0x90000000);

        if (!cfg.hudLocked && cfg.hudBorderEnabled && mc.currentScreen == null) {
            int rgb    = cfg.hudBorderColor & 0xFFFFFF;
            int border = dragging ? (0xFF000000 | rgb) : (0x80000000 | rgb);
            ctx.fill(x,          y,          x + boxW,     y + 1,        border);
            ctx.fill(x,          y + boxH-1, x + boxW,     y + boxH,     border);
            ctx.fill(x,          y,          x + 1,        y + boxH,     border);
            ctx.fill(x + boxW-1, y,          x + boxW,     y + boxH,     border);
        }

        int tpsColor  = measuredTps >= 18 ? colored(cfg.hudAlpha, cfg.hudColorGood)
                      : measuredTps >= 14 ? colored(cfg.hudAlpha, cfg.hudColorWarn)
                      : colored(cfg.hudAlpha, cfg.hudColorBad);
        int pingColor = ping < 0   ? colored(cfg.hudAlpha, cfg.hudColorUnknown)
                      : ping < 80  ? colored(cfg.hudAlpha, cfg.hudColorGood)
                      : ping < 150 ? colored(cfg.hudAlpha, cfg.hudColorWarn)
                      : colored(cfg.hudAlpha, cfg.hudColorBad);

        int row = 0;
        ctx.drawText(mc.textRenderer, tpsLine,  x + padX, y + padY + (lineH + 2) * row++, tpsColor,  true);
        ctx.drawText(mc.textRenderer, pingLine, x + padX, y + padY + (lineH + 2) * row++, pingColor, true);
        if (fpsLine != null)
            ctx.drawText(mc.textRenderer, fpsLine, x + padX, y + padY + (lineH + 2) * row++, colored(cfg.hudAlpha, cfg.hudColorGood), true);
        if (clockLine != null)
            ctx.drawText(mc.textRenderer, clockLine, x + padX, y + padY + (lineH + 2) * row, colored(cfg.hudAlpha, cfg.hudColorGood), true);
    }
}
