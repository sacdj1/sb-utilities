package com.example.sbadditions;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ModMenuApiImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            SBAdditionsConfig cfg = SBAdditionsConfig.INSTANCE;

            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("SB Utilities"))
                .setSavingRunnable(SBAdditionsConfig::save);

            ConfigEntryBuilder eb = builder.entryBuilder();

            // ── Wind HUD ──────────────────────────────────────────────────────
            ConfigCategory wind = builder.getOrCreateCategory(Text.literal("Wind HUD"));

            wind.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), cfg.windEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Master switch for wind auto-aim. Keybind toggles this. Does not affect the HUD display."))
                .setSaveConsumer(v -> cfg.windEnabled = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Show HUD"), cfg.windHudEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show directional arrows at screen edges for the wind compass."))
                .setSaveConsumer(v -> cfg.windHudEnabled = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Auto-Aim"), cfg.windAutoAim)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Automatically rotate camera toward the wind direction (when not dark green = centred)."))
                .setSaveConsumer(v -> cfg.windAutoAim = v)
                .build());

            wind.addEntry(eb.startDoubleField(Text.literal("Auto-Aim Speed"), cfg.windAutoSpeed)
                .setDefaultValue(1.5)
                .setTooltip(Text.literal("Degrees per tick per unit of offset. Higher = snappier."))
                .setSaveConsumer(v -> cfg.windAutoSpeed = Math.max(0.1, Math.min(10.0, v)))
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Invert Direction"), cfg.windInvert)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Flip left/right if the arrows point the wrong way."))
                .setSaveConsumer(v -> cfg.windInvert = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Only When Crouching"), cfg.windOnlyCrouching)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Auto-aim only activates while sneaking (Shift)."))
                .setSaveConsumer(v -> cfg.windOnlyCrouching = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Only When Breaking"), cfg.windOnlyBreaking)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Auto-aim only activates while left-click (mining) is held."))
                .setSaveConsumer(v -> cfg.windOnlyBreaking = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Arrow Auto-Aim (‹ ›)"), cfg.windArrowAutoAim)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Constant-speed aim correction using scoreboard arrow direction. Works independently of Auto-Aim."))
                .setSaveConsumer(v -> cfg.windArrowAutoAim = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Arrow Auto-Aim Only When Crouching"), cfg.windArrowOnlyCrouching)
                .setDefaultValue(false)
                .setSaveConsumer(v -> cfg.windArrowOnlyCrouching = v)
                .build());

            wind.addEntry(eb.startBooleanToggle(Text.literal("Arrow Auto-Aim Only When Breaking"), cfg.windArrowOnlyBreaking)
                .setDefaultValue(false)
                .setSaveConsumer(v -> cfg.windArrowOnlyBreaking = v)
                .build());

            // ── TPS / Ping HUD ────────────────────────────────────────────────
            ConfigCategory hud = builder.getOrCreateCategory(Text.literal("HUD"));

            hud.addEntry(eb.startBooleanToggle(Text.literal("TPS & Ping HUD"), cfg.hudTpsEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show server TPS and your ping. Drag it while no screen is open."))
                .setSaveConsumer(v -> cfg.hudTpsEnabled = v)
                .build());

            hud.addEntry(eb.startBooleanToggle(Text.literal("FPS"), cfg.hudFpsEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Show current FPS in the HUD."))
                .setSaveConsumer(v -> cfg.hudFpsEnabled = v)
                .build());

            hud.addEntry(eb.startBooleanToggle(Text.literal("IRL Clock"), cfg.hudClockEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Show real-world time in the HUD."))
                .setSaveConsumer(v -> cfg.hudClockEnabled = v)
                .build());

            hud.addEntry(eb.startBooleanToggle(Text.literal("12-Hour Clock"), cfg.hudClock12Hour)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Show the clock in 12-hour format (hh:mm:ss AM/PM) instead of 24-hour."))
                .setSaveConsumer(v -> cfg.hudClock12Hour = v)
                .build());

            hud.addEntry(eb.startBooleanToggle(Text.literal("Lock HUD Position"), cfg.hudLocked)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Prevent the HUD from being dragged while in-game. When unlocked a border appears around the HUD — click and drag to reposition it."))
                .setSaveConsumer(v -> cfg.hudLocked = v)
                .build());

            hud.addEntry(eb.startBooleanToggle(Text.literal("Show Border When Unlocked"), cfg.hudBorderEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Draw a colored border around the HUD when it is unlocked and draggable."))
                .setSaveConsumer(v -> cfg.hudBorderEnabled = v)
                .build());

            hud.addEntry(eb.startColorField(Text.literal("Border Color"), cfg.hudBorderColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the HUD border shown when the HUD is unlocked."))
                .setSaveConsumer(v -> cfg.hudBorderColor = v)
                .build());

            hud.addEntry(eb.startIntSlider(Text.literal("HUD Text Alpha"), cfg.hudAlpha, 0, 255)
                .setDefaultValue(255)
                .setTooltip(Text.literal("Transparency of all HUD text. 255 = fully opaque, 0 = invisible."))
                .setSaveConsumer(v -> cfg.hudAlpha = v)
                .build());

            hud.addEntry(eb.startColorField(Text.literal("Good Color"), cfg.hudColorGood)
                .setDefaultValue(0x55FF55)
                .setTooltip(Text.literal("Color for good TPS (≥18) and low ping (<80ms)."))
                .setSaveConsumer(v -> cfg.hudColorGood = v)
                .build());

            hud.addEntry(eb.startColorField(Text.literal("Warn Color"), cfg.hudColorWarn)
                .setDefaultValue(0xFFFF55)
                .setTooltip(Text.literal("Color for medium TPS (14–18) and medium ping (80–150ms)."))
                .setSaveConsumer(v -> cfg.hudColorWarn = v)
                .build());

            hud.addEntry(eb.startColorField(Text.literal("Bad Color"), cfg.hudColorBad)
                .setDefaultValue(0xFF5555)
                .setTooltip(Text.literal("Color for bad TPS (<14) and high ping (>150ms)."))
                .setSaveConsumer(v -> cfg.hudColorBad = v)
                .build());

            hud.addEntry(eb.startColorField(Text.literal("Unknown Ping Color"), cfg.hudColorUnknown)
                .setDefaultValue(0xAAAAAA)
                .setTooltip(Text.literal("Color shown when ping is unavailable."))
                .setSaveConsumer(v -> cfg.hudColorUnknown = v)
                .build());

            hud.addEntry(eb.startIntField(Text.literal("HUD X"), cfg.hudTpsX)
                .setDefaultValue(5)
                .setTooltip(Text.literal("Horizontal position (drag in-game to reposition)."))
                .setSaveConsumer(v -> cfg.hudTpsX = Math.max(0, v))
                .build());

            hud.addEntry(eb.startIntField(Text.literal("HUD Y"), cfg.hudTpsY)
                .setDefaultValue(5)
                .setTooltip(Text.literal("Vertical position (drag in-game to reposition)."))
                .setSaveConsumer(v -> cfg.hudTpsY = Math.max(0, v))
                .build());

            // ── No Break ──────────────────────────────────────────────────────
            ConfigCategory noBreak = builder.getOrCreateCategory(Text.literal("No Break"));

            noBreak.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), cfg.noBreakEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Prevent left-click mining of blocks not in your list. Toggle with the ½ key."))
                .setSaveConsumer(v -> cfg.noBreakEnabled = v)
                .build());

            noBreak.addEntry(eb.startBooleanToggle(Text.literal("Whitelist Mode"), cfg.noBreakWhitelist)
                .setDefaultValue(true)
                .setTooltip(Text.literal("ON = only listed blocks can be broken.\nOFF = listed blocks cannot be broken (blacklist)."))
                .setSaveConsumer(v -> cfg.noBreakWhitelist = v)
                .build());

            noBreak.addEntry(eb.startStrList(Text.literal("Always-Protected Blocks"), new ArrayList<>(cfg.noBreakBlocks))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Blocks that can NEVER be broken regardless of what tool you hold.\nSubstring patterns matched against block registry IDs.\nAliases: mithril, titanium, gemstone\n\nExamples:\n  stained_glass\n  mithril\n  titanium\n  gemstone"))
                .setSaveConsumer(v -> cfg.noBreakBlocks = new ArrayList<>(v))
                .build());

            noBreak.addEntry(eb.startStrList(Text.literal("Per-Tool Rules"), new ArrayList<>(cfg.noBreakRules))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("One rule per line. Two formats:\n\nNew: toolPattern W|B block1, block2, ...\n  W = whitelist: only protect the listed blocks when holding this tool\n  B = blacklist: protect ALL blocks EXCEPT the listed ones when holding this tool\n\nLegacy: blockPattern toolPattern\n  (still works — protects that block when holding that tool)\n\nExamples:\n  Pickonimbus W stained_glass, prismarine, mithril\n  Drill B dirt, stone, gravel\n  mithril Pickonimbus"))
                .setSaveConsumer(v -> cfg.noBreakRules = new ArrayList<>(v))
                .build());

            // ── No Interact ───────────────────────────────────────────────────
            ConfigCategory noInt = builder.getOrCreateCategory(Text.literal("No Interact"));

            noInt.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), cfg.noInteractEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Prevent right-click interactions with blocks/entities/items."))
                .setSaveConsumer(v -> cfg.noInteractEnabled = v)
                .build());

            noInt.addEntry(eb.startBooleanToggle(Text.literal("Block Use (Right-Click)"), cfg.noInteractPreventUse)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Prevent right-clicking blocks not in the list (chests, buttons, etc.)."))
                .setSaveConsumer(v -> cfg.noInteractPreventUse = v)
                .build());

            noInt.addEntry(eb.startBooleanToggle(Text.literal("Prevent Ability (All Right-Click)"), cfg.noInteractPreventAbility)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Cancel ALL right-click actions while holding a matching tool — block use, entity interact, and item use (pickaxe abilities etc.). Ignores block list; use Tool Filter to control which tools trigger this."))
                .setSaveConsumer(v -> cfg.noInteractPreventAbility = v)
                .build());

            noInt.addEntry(eb.startBooleanToggle(Text.literal("Whitelist Mode"), cfg.noInteractWhitelist)
                .setDefaultValue(true)
                .setTooltip(Text.literal("ON = only listed blocks can be right-clicked.\nOFF = listed blocks cannot be right-clicked (blacklist)."))
                .setSaveConsumer(v -> cfg.noInteractWhitelist = v)
                .build());

            noInt.addEntry(eb.startStrList(Text.literal("Always-Blocked Blocks"), new ArrayList<>(cfg.noInteractBlocks))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("Blocks that can NEVER be right-clicked regardless of what tool you hold.\nSubstring patterns matched against block registry IDs.\nAliases: mithril, titanium, gemstone\n\nExamples:\n  chest\n  button\n  mithril"))
                .setSaveConsumer(v -> cfg.noInteractBlocks = new ArrayList<>(v))
                .build());

            noInt.addEntry(eb.startStrList(Text.literal("Per-Tool Rules"), new ArrayList<>(cfg.noInteractRules))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("One rule per line. Two formats:\n\nNew: toolPattern W|B block1, block2, ...\n  W = whitelist: only block right-click on the listed blocks when holding this tool\n  B = blacklist: block right-click on ALL blocks EXCEPT the listed ones\n\nLegacy: blockPattern toolPattern\n  (still works)\n\nExamples:\n  Pickonimbus W chest, crafting_table\n  chest Pickonimbus"))
                .setSaveConsumer(v -> cfg.noInteractRules = new ArrayList<>(v))
                .build());

            // ── Tool Swap ─────────────────────────────────────────────────────
            ConfigCategory swap = builder.getOrCreateCategory(Text.literal("Tool Swap"));

            swap.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), cfg.toolSwapEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Automatically switch to the right tool when looking at a matching block.\nBind a key to toggle on/off. Also toggleable via /ssbu toolswap."))
                .setSaveConsumer(v -> cfg.toolSwapEnabled = v)
                .build());

            swap.addEntry(eb.startBooleanToggle(Text.literal("Prevent Swap on Weapon"), cfg.toolSwapPreventOnWeapon)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Do not swap while holding a sword, bow, crossbow, or trident.\nUseful to avoid accidentally swapping off a weapon mid-combat."))
                .setSaveConsumer(v -> cfg.toolSwapPreventOnWeapon = v)
                .build());

            swap.addEntry(eb.startBooleanToggle(Text.literal("Wildcard Matching"), cfg.toolSwapWildcard)
                .setDefaultValue(true)
                .setTooltip(Text.literal("ON = patterns match any substring of the block/item ID or display name.\nOFF = exact match required (namespace optional for minecraft: items)."))
                .setSaveConsumer(v -> cfg.toolSwapWildcard = v)
                .build());

            swap.addEntry(eb.startBooleanToggle(Text.literal("Only When Breaking"), cfg.toolSwapOnlyBreaking)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Only swap while left-click (mining) is held."))
                .setSaveConsumer(v -> cfg.toolSwapOnlyBreaking = v)
                .build());

            swap.addEntry(eb.startBooleanToggle(Text.literal("Only When Crouching"), cfg.toolSwapOnlyCrouching)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Only swap while sneaking (Shift)."))
                .setSaveConsumer(v -> cfg.toolSwapOnlyCrouching = v)
                .build());

            swap.addEntry(eb.startBooleanToggle(Text.literal("Pause Mining on Swap"), cfg.toolSwapPauseMining)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Cancel block-break progress for 1 tick when a tool swap occurs. Prevents accidentally chipping a block with the wrong tool right as you swap."))
                .setSaveConsumer(v -> cfg.toolSwapPauseMining = v)
                .build());

            swap.addEntry(eb.startStrList(Text.literal("Swap Rules"), new ArrayList<>(cfg.toolSwapRules))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("One rule per line:  <block_pattern> <tool_pattern> [area_filter]\n\nBlock aliases:\n  mithril  → gray wool + cyan terracotta + light blue wool\n  titanium → polished diorite\n  gemstone → matches all gemstone blocks by name\n\nExamples:\n  mithril mithril_pickaxe\n  titanium drill\n  gemstone drill\n  mithril mithril_pickaxe mines_of_divan\n\nPatterns are case-insensitive substrings. Underscores = spaces."))
                .setSaveConsumer(v -> cfg.toolSwapRules = new ArrayList<>(v))
                .build());

            // ── Weapon Swap ───────────────────────────────────────────────────
            ConfigCategory weaponSwap = builder.getOrCreateCategory(Text.literal("Weapon Swap"));

            weaponSwap.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), cfg.toolSwapWeaponEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Automatically switch to the matching weapon when looking at a matching entity.\nToggle with the Numpad 0 key (rebindable in Controls)."))
                .setSaveConsumer(v -> cfg.toolSwapWeaponEnabled = v)
                .build());

            weaponSwap.addEntry(eb.startBooleanToggle(Text.literal("Wildcard Matching"), cfg.toolSwapWildcard)
                .setDefaultValue(true)
                .setTooltip(Text.literal("ON = entity and weapon patterns match any substring.\nOFF = exact match required.\nShared with Tool Swap wildcard setting."))
                .setSaveConsumer(v -> cfg.toolSwapWildcard = v)
                .build());

            weaponSwap.addEntry(eb.startBooleanToggle(Text.literal("Only When Breaking"), cfg.toolSwapWeaponOnlyBreaking)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Only swap weapons while left-click (attacking) is held."))
                .setSaveConsumer(v -> cfg.toolSwapWeaponOnlyBreaking = v)
                .build());

            weaponSwap.addEntry(eb.startBooleanToggle(Text.literal("Only When Crouching"), cfg.toolSwapWeaponOnlyCrouching)
                .setDefaultValue(false)
                .setTooltip(Text.literal("Only swap weapons while sneaking (Shift)."))
                .setSaveConsumer(v -> cfg.toolSwapWeaponOnlyCrouching = v)
                .build());

            weaponSwap.addEntry(eb.startBooleanToggle(Text.literal("Attack on Swap"), cfg.weaponSwapAttackOnSwap)
                .setDefaultValue(false)
                .setTooltip(Text.literal("When a weapon swap fires: block the in-progress attack with the old item,\nthen attack once with the new weapon on the next tick."))
                .setSaveConsumer(v -> cfg.weaponSwapAttackOnSwap = v)
                .build());

            weaponSwap.addEntry(eb.startStrList(Text.literal("Weapon Rules"), new ArrayList<>(cfg.toolSwapWeaponRules))
                .setDefaultValue(new ArrayList<>())
                .setTooltip(Text.literal("One rule per line:  <entity_pattern> <weapon_pattern>\n\nEntity aliases:\n  monsters / hostile / hostiles → HostileEntity\n  angry / targeting_me          → mob targeting you\n  mobs / all_mobs / all         → any MobEntity\n  (anything else)               → substring of entity type/display name\n\nExamples:\n  hostile sword\n  targeting_me axe\n  zombie Reaper\n  monsters Reaper_Scythe\n\nPatterns are case-insensitive. Underscores = spaces."))
                .setSaveConsumer(v -> cfg.toolSwapWeaponRules = new ArrayList<>(v))
                .build());

            return builder.build();
        };
    }
}
