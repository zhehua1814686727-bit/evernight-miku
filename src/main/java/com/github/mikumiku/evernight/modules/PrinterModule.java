package com.github.mikumiku.evernight.modules;

import com.github.mikumiku.evernight.MikuCategories;
import com.github.mikumiku.evernight.MikuModule;
import com.github.mikumiku.evernight.util.LitematicaBridge;
import com.github.mikumiku.evernight.util.MikuUtils;
import evernight.client.events.core.EventHandler;
import evernight.client.events.ui.MinecraftTickEvent;
import evernight.client.settings.core.Setting;
import evernight.client.settings.types.BooleanSetting;
import evernight.client.settings.types.NumberSetting;
import evernight.client.utils.inventory.InventoryUtils;
import evernight.client.utils.world.BlockInteractionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Prints the Litematica placement that is currently loaded around the player. Ported from
 * {@code BestPrinter}; the schematic comes from Litematica's schematic world, so no file parsing is
 * involved and the placement origin/rotation is whatever Litematica has configured.
 *
 * <p>Litematica is reached through {@link LitematicaBridge} so the addon still loads (and this
 * module simply refuses to run) when the mod is absent.
 */
public final class PrinterModule extends MikuModule {
    private final Setting<Double> range;
    private final Setting<Integer> blocksPerTick;
    private final Setting<Integer> delayTicks;
    private final Setting<Integer> scanInterval;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> dirtForGrass;
    private final Setting<Boolean> debug;

    private int delayCounter;
    private int scanCooldown;
    private boolean warnedMissing;

    public PrinterModule() {
        super("Printer", "Rebuilds the Litematica placement loaded around you.",
                MikuCategories.BUILD, "mikuprinter", "printer");
        this.range = this.add(new NumberSetting<>("Range", 4.5, 1.0, 6.0));
        this.blocksPerTick = this.add(new NumberSetting<>("BlocksPerTick", 2, 1, 9));
        this.delayTicks = this.add(new NumberSetting<>("DelayTicks", 0, 0, 10));
        this.scanInterval = this.add(new NumberSetting<>("ScanInterval", 10, 1, 40));
        this.rotate = this.add(new BooleanSetting("Rotate", true));
        this.dirtForGrass = this.add(new BooleanSetting("DirtForGrass", true));
        this.debug = this.add(new BooleanSetting("Debug", false));
    }

    @Override
    public void onEnable() {
        this.delayCounter = 0;
        this.scanCooldown = 0;
        this.warnedMissing = false;
        if (!LitematicaBridge.isAvailable()) {
            this.warn("需要安装 Litematica 并加载投影");
            this.disable();
        }
    }

    @EventHandler
    private void onTick(MinecraftTickEvent event) {
        if (!this.inGame()) {
            return;
        }
        if (!LitematicaBridge.isAvailable()) {
            if (!this.warnedMissing) {
                this.warnedMissing = true;
                this.warn("需要安装 Litematica 并加载投影");
            }
            this.disable();
            return;
        }
        Object schematicWorld = LitematicaBridge.schematicWorld();
        if (schematicWorld == null) {
            return;
        }
        if (this.delayCounter > 0) {
            this.delayCounter--;
            return;
        }
        if (this.scanCooldown > 0) {
            this.scanCooldown--;
            return;
        }
        this.scanCooldown = this.scanInterval.getValue();

        int placed = 0;
        double maxDistance = this.range.getValue() * this.range.getValue();
        BlockPos origin = mc().player.blockPosition();
        int radius = (int) Math.ceil(this.range.getValue());
        outer:
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (placed >= this.blocksPerTick.getValue()) {
                        break outer;
                    }
                    BlockPos position = origin.offset(x, y, z);
                    if (mc().player.position().distanceToSqr(position.getCenter()) > maxDistance) {
                        continue;
                    }
                    BlockState target = LitematicaBridge.stateAt(schematicWorld, position);
                    if (target.isAir()) {
                        continue;
                    }
                    if (mc().level.getBlockState(position).is(target.getBlock())) {
                        continue;
                    }
                    if (!MikuUtils.canReplace(position)) {
                        continue;
                    }
                    if (this.place(position, target)) {
                        placed++;
                    }
                }
            }
        }
        if (placed > 0 && this.delayTicks.getValue() > 0) {
            this.delayCounter = this.delayTicks.getValue();
        }
    }

    private boolean place(BlockPos position, BlockState target) {
        Block block = target.getBlock();
        InventoryUtils.SwapSlot slot = InventoryUtils.findSwapSlot(
                stack -> this.matchesBlock(stack, block), true);
        if (!slot.isFound()) {
            if (this.debug.getValue()) {
                this.info("Missing item for " + block.getName().getString());
            }
            return false;
        }
        Direction support = BlockInteractionUtils.findSupportDirection(position);
        if (support == null) {
            return false;
        }
        int previousSlot = mc().player.getInventory().getSelectedSlot();
        InventoryUtils.swapTo(slot);
        try {
            if (this.rotate.getValue()) {
                MikuUtils.rotateTo(position);
            }
            return BlockInteractionUtils.placeBlock(position, null, support, false, InteractionHand.MAIN_HAND);
        } finally {
            InventoryUtils.swapBack(previousSlot, slot);
        }
    }

    private boolean matchesBlock(ItemStack stack, Block block) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        if (blockItem.getBlock() == block) {
            return true;
        }
        if (!this.dirtForGrass.getValue()) {
            return false;
        }
        Block held = blockItem.getBlock();
        if (block == Blocks.GRASS_BLOCK || block == Blocks.MYCELIUM || block == Blocks.PODZOL) {
            return held == Blocks.DIRT;
        }
        return false;
    }
}
