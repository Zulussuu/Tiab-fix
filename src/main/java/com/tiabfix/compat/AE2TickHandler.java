package com.tiabfix.compat;

import net.minecraft.world.level.block.entity.BlockEntity;

public class AE2TickHandler {
    public static boolean tryTick(BlockEntity be) {
        try {
            Class<?> tickable = Class.forName("appeng.api.networking.ticking.IGridTickable");
            if (!tickable.isInstance(be)) return false;
            Object managed = be.getClass().getMethod("getMainNode").invoke(be);
            Object node = Class.forName("appeng.api.networking.IManagedGridNode").getMethod("getNode").invoke(managed);
            if (node == null) return true;
            tickable.getMethod("tickingRequest", Class.forName("appeng.api.networking.IGridNode"), int.class)
                    .invoke(be, node, 1);
            return true;
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }
}