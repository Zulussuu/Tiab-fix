package com.tiabfix.compat;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

public class AE2Compat {

    private static Boolean ae2Loaded = null;

    public static boolean isAE2Loaded() {
        if (ae2Loaded == null) {
            ae2Loaded = ModList.get().isLoaded("ae2");
        }
        return ae2Loaded;
    }

    public static boolean tryTickAE2BlockEntity(BlockEntity blockEntity) {
        if (!isAE2Loaded()) {
            return false;
        }
        return AE2TickHandler.tryTick(blockEntity);
    }
}
