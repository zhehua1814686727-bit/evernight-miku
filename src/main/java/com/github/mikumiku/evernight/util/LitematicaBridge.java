package com.github.mikumiku.evernight.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;

/**
 * Minimal reflective bridge to Litematica.
 *
 * <p>The addon must keep working when Litematica is not installed, so no Litematica type may appear
 * in a field, signature or static initialiser that the client loads unconditionally. Everything goes
 * through {@link Class#forName} here and only after {@link #isAvailable()} reported the mod.
 */
public final class LitematicaBridge {
    private static final String HANDLER = "fi.dy.masa.litematica.world.SchematicWorldHandler";

    private static boolean resolved;
    private static Method getSchematicWorld;
    private static Method getBlockState;
    private static boolean failed;

    private LitematicaBridge() {
    }

    public static boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("litematica");
    }

    private static void resolve() {
        if (LitematicaBridge.resolved) {
            return;
        }
        LitematicaBridge.resolved = true;
        try {
            Class<?> handler = Class.forName(HANDLER);
            LitematicaBridge.getSchematicWorld = handler.getMethod("getSchematicWorld");
        } catch (Throwable throwable) {
            LitematicaBridge.failed = true;
        }
    }

    /** @return the schematic world of the loaded placement, or {@code null} when none is loaded */
    public static Object schematicWorld() {
        LitematicaBridge.resolve();
        if (LitematicaBridge.failed || LitematicaBridge.getSchematicWorld == null) {
            return null;
        }
        try {
            return LitematicaBridge.getSchematicWorld.invoke(null);
        } catch (Throwable throwable) {
            return null;
        }
    }

    /** Reads the schematic block state at {@code position}; air when unavailable. */
    public static BlockState stateAt(Object schematicWorld, BlockPos position) {
        if (schematicWorld == null) {
            return Blocks.AIR.defaultBlockState();
        }
        try {
            if (LitematicaBridge.getBlockState == null) {
                LitematicaBridge.getBlockState = schematicWorld.getClass()
                        .getMethod("getBlockState", BlockPos.class);
            }
            Object state = LitematicaBridge.getBlockState.invoke(schematicWorld, position);
            return state instanceof BlockState blockState ? blockState : Blocks.AIR.defaultBlockState();
        } catch (Throwable throwable) {
            return Blocks.AIR.defaultBlockState();
        }
    }
}
