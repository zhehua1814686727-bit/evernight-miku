package com.github.mikumiku.evernight;

import com.github.mikumiku.evernight.modules.AutoBlockCenterModule;
import com.github.mikumiku.evernight.modules.AutoDigFeetModule;
import com.github.mikumiku.evernight.modules.AutoFollowPlayerModule;
import com.github.mikumiku.evernight.modules.AutoLoginModule;
import com.github.mikumiku.evernight.modules.AutoSlabModule;
import com.github.mikumiku.evernight.modules.AutoTouchFireModule;
import com.github.mikumiku.evernight.modules.AutoTreeChopModule;
import com.github.mikumiku.evernight.modules.ChestAuraModule;
import com.github.mikumiku.evernight.modules.DeathCommandModule;
import com.github.mikumiku.evernight.modules.ElytraArmorSwitchModule;
import com.github.mikumiku.evernight.modules.ElytraUnbreakModule;
import com.github.mikumiku.evernight.modules.FishingRodFaceModule;
import com.github.mikumiku.evernight.modules.HandsomeSpinModule;
import com.github.mikumiku.evernight.modules.HoverModule;
import com.github.mikumiku.evernight.modules.InventoryPrinterModule;
import com.github.mikumiku.evernight.modules.PlayerAlertModule;
import com.github.mikumiku.evernight.modules.ThreeCCheckModule;
import com.github.mikumiku.evernight.modules.TreeAuraModule;
import com.github.mikumiku.evernight.modules.VanityEspModule;
import com.github.mikumiku.evernight.modules.hud.PotCountHudModule;
import evernight.addon.AddonContext;
import evernight.client.modules.Module;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Central registration table for every ported module.
 *
 * <p>Only modules that add behaviour the client does not already provide are registered here:
 * everything that duplicated an Evernight built-in (AutoCrystal, Aura, Velocity, Scaffold, Nuker,
 * NoFall, Phase, ElytraFly, Trident, KeyPearl, AutoLog, HoleFill, ...) was dropped, see
 * {@code README.md} for the list and the matching built-in.
 */
public final class MikuModules {
    private MikuModules() {
    }

    private static final List<Supplier<? extends Module>> FACTORIES = new ArrayList<>();

    static {
        // Miku
        FACTORIES.add(FishingRodFaceModule::new);
        FACTORIES.add(DeathCommandModule::new);
        FACTORIES.add(AutoLoginModule::new);
        FACTORIES.add(PlayerAlertModule::new);
        FACTORIES.add(HandsomeSpinModule::new);
        FACTORIES.add(InventoryPrinterModule::new);
        FACTORIES.add(ChestAuraModule::new);
        // Miku 建设
        FACTORIES.add(AutoSlabModule::new);
        FACTORIES.add(TreeAuraModule::new);
        FACTORIES.add(AutoDigFeetModule::new);
        FACTORIES.add(AutoTouchFireModule::new);
        FACTORIES.add(AutoTreeChopModule::new);
        // Miku 特调
        FACTORIES.add(HoverModule::new);
        FACTORIES.add(ElytraArmorSwitchModule::new);
        FACTORIES.add(ElytraUnbreakModule::new);
        FACTORIES.add(AutoFollowPlayerModule::new);
        FACTORIES.add(AutoBlockCenterModule::new);
        FACTORIES.add(VanityEspModule::new);
        FACTORIES.add(ThreeCCheckModule::new);
        // HUD
        FACTORIES.add(PotCountHudModule::new);
    }

    public static void register(AddonContext context) {
        for (Supplier<? extends Module> factory : FACTORIES) {
            try {
                context.registerModule(factory.get());
            } catch (Throwable throwable) {
                context.logger().error("Failed to register Miku module", throwable);
            }
        }
    }

    public static int count() {
        return FACTORIES.size();
    }
}
