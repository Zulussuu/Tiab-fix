package com.tiabfix.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.lang.reflect.Method;

@Mixin(targets = "appeng.block.AEBaseEntityBlock", remap = false)
public class AE2BaseBlockMixin {

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return (lvl, pos, blockState, be) -> {
            try {
                Class<?> gridTickableClass = Class.forName("appeng.api.networking.ticking.IGridTickable");
                if (!gridTickableClass.isInstance(be)) {
                    return;
                }

                Method getMainNodeMethod = be.getClass().getMethod("getMainNode");
                Object mainNode = getMainNodeMethod.invoke(be);
                if (mainNode == null) {
                    return;
                }

                Method getNodeMethod = mainNode.getClass().getMethod("getNode");
                Object node = getNodeMethod.invoke(mainNode);
                if (node == null) {
                    return;
                }

                Method tickingRequestMethod = be.getClass().getMethod("tickingRequest",
                        Class.forName("appeng.api.networking.IGridNode"), int.class);
                tickingRequestMethod.invoke(be, node, 1);
            } catch (Exception ignored) {
            }
        };
    }
}
