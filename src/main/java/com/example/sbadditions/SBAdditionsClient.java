package com.example.sbadditions;

import com.example.sbadditions.modules.NoInteract;
import com.example.sbadditions.modules.ToolSwapper;
import com.example.sbadditions.modules.TpsHud;
import com.example.sbadditions.modules.WindHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SBAdditionsClient implements ClientModInitializer {

    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        SBAdditionsConfig.load();
        Commands.init();
        TpsHud.init();
        WindHud.init();
        NoInteract.init();
        ToolSwapper.init();

        KeyBinding configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.sb-additions.config-overview",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_9,
            new KeyBinding.Category(Identifier.of("sb-additions", "category"))
        ));
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            while (configKey.wasPressed()) {
                if (mc.player == null) return;
                SBAdditionsConfig c = SBAdditionsConfig.INSTANCE;
                mc.player.sendMessage(Text.literal("=== SB Utilities — click to toggle ===").formatted(Formatting.GOLD), false);
                mc.player.sendMessage(row("Wind HUD",     "wind.enabled",            c.windEnabled)
                    .append("  ").append(row("Tool Swap",    "toolswap.enabled",        c.toolSwapEnabled)), false);
                mc.player.sendMessage(row("Weapon Swap",  "toolswap.weapon_enabled",  c.toolSwapWeaponEnabled)
                    .append("  ").append(row("No Break",     "nobreak.enabled",         c.noBreakEnabled)), false);
                mc.player.sendMessage(row("No Interact",  "nointeract.enabled",       c.noInteractEnabled)
                    .append("  ").append(row("TPS HUD",      "hud.tps",                 c.hudTpsEnabled)), false);
            }
        });
    }

    private static MutableText row(String label, String key, boolean value) {
        String cmd = "/ssbu config set " + key + " toggle";
        Style style = Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(cmd));
        MutableText status = value
            ? Text.literal("[ON]").formatted(Formatting.GREEN)
            : Text.literal("[OFF]").formatted(Formatting.RED);
        return Text.literal(label + ": ").formatted(Formatting.WHITE).append(status.setStyle(style));
    }
}
