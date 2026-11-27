import java.util.*;

public class CtorTest {
    public static void main(String[] args) throws Exception {
        String[] modules = {
            "FishingRodFaceModule",
            "DeathCommandModule",
            "AutoLoginModule",
            "PlayerAlertModule",
            "HandsomeSpinModule",
            "InventoryPrinterModule",
            "ChestAuraModule",
            "AutoSlabModule",
            "TreeAuraModule",
            "AutoDigFeetModule",
            "AutoTouchFireModule",
            "AutoTreeChopModule",
            "HoverModule",
            "ElytraArmorSwitchModule",
            "ElytraUnbreakModule",
            "AutoFollowPlayerModule",
            "AutoBlockCenterModule",
            "VanityEspModule",
            "ThreeCCheckModule",
        };
        int ok = 0;
        int fail = 0;
        for (String name : modules) {
            String cls = "com.github.mikumiku.evernight.modules." + name;
            try {
                Object instance = Class.forName(cls).getDeclaredConstructor().newInstance();
                ok++;
                System.out.println("OK   " + name + "  settings=" + ((evernight.client.modules.Module) instance).getSettings().size());
            } catch (Throwable t) {
                fail++;
                Throwable cause = t.getCause() == null ? t : t.getCause();
                System.out.println("FAIL " + name + " -> " + cause);
            }
        }
        System.out.println("ok=" + ok + " fail=" + fail);
    }
}
