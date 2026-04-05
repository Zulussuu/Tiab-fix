package com.tiabfix.mixin;

import com.tiabfix.compat.AE2Compat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;
import org.mangorage.tiab.common.entities.TimeAcceleratorEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(TimeAcceleratorEntity.class)
public abstract class TimeAcceleratorEntityMixin extends Entity {

    @Shadow
    public abstract int getTimeRate();

    @Shadow
    public abstract int getRemainingTime();

    @Shadow
    public abstract void setRemainingTime(int remainingTime);

    @Shadow
    public abstract BlockPos getBlockPos();

    @Shadow
    public abstract void setTimeRate(int rate);

    public TimeAcceleratorEntityMixin() {
        super(null, null);
    }

    private void unlockEnergyInput(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            unlockEnergyFields(be, 0);
        }

        for (Direction dir : Direction.values()) {
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, dir);
            if (storage != null) {
                unlockEnergyFields(storage, 0);
            }
        }
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
        if (storage != null) {
            unlockEnergyFields(storage, 0);
        }
    }

    private void unlockEnergyFields(Object target, int depth) {
        if (target == null || depth > 5)
            return;
        try {
            Class<?> clazz = target.getClass();
            while (clazz != null && !clazz.getName().startsWith("java.")
                    && !clazz.getName().startsWith("net.minecraft.")) {
                for (Field field : clazz.getDeclaredFields()) {
                    try {
                        field.setAccessible(true);
                        String name = field.getName().toLowerCase();

                        if (isEnergyInputField(name)) {
                            if (field.getType() == int.class) {
                                field.setInt(target, Integer.MAX_VALUE);
                            } else if (field.getType() == long.class) {
                                field.setLong(target, Long.MAX_VALUE);
                            }
                        }

                        if (isEnergyStorageField(name) && !field.getType().isPrimitive()
                                && !field.getType().isArray()) {
                            Object nested = field.get(target);
                            if (nested != null && !nested.getClass().getName().startsWith("java.")) {
                                unlockEnergyFields(nested, depth + 1);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isEnergyInputField(String name) {
        return name.contains("maxreceive") || name.contains("max_receive")
                || name.contains("maxinput") || name.contains("max_input")
                || name.contains("maxtransfer") || name.contains("max_transfer")
                || name.contains("transferrate") || name.contains("transfer_rate")
                || name.contains("inputrate") || name.contains("input_rate")
                || name.contains("maxin") || name.contains("receiverate")
                || name.contains("maxextract") || name.contains("max_extract")
                || name.contains("maxusage") || name.contains("currentmaxusage")
                || name.contains("chargeRate") || name.contains("chargerate")
                || name.contains("maxcharge") || name.contains("insertrate")
                || name.contains("maxinsert") || name.contains("transfercap")
                || (name.contains("max") && name.contains("io"));
    }

    private boolean isEnergyStorageField(String name) {
        return name.contains("energy") || name.contains("power")
                || name.contains("storage") || name.contains("battery")
                || name.contains("capacitor") || name.contains("container")
                || name.contains("handler") || name.contains("cell")
                || name.contains("buffer");
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void onTick(CallbackInfo ci) {
        if (level().isClientSide())
            return;
        ServerLevel level = (ServerLevel) level();

        BlockPos pos = getBlockPos();
        if (pos == null) {
            this.remove(RemovalReason.KILLED);
            ci.cancel();
            return;
        }

        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(ICommonTimeInABottleAPI.COMMON_API.get().getTagKey())) {
            this.remove(RemovalReason.KILLED);
            setRemainingTime(0);
            setTimeRate(1);
            ci.cancel();
            return;
        }

        BlockEntity targetBlockEntity = level.getBlockEntity(pos);
        BlockEntityTicker<BlockEntity> targetTicker = null;
        if (targetBlockEntity != null) {
            targetTicker = (BlockEntityTicker<BlockEntity>) targetBlockEntity.getBlockState().getTicker(level,
                    (BlockEntityType<BlockEntity>) targetBlockEntity.getType());
        }

        unlockEnergyInput(level, pos);

        for (int i = 0; i < getTimeRate(); i++) {
            blockState = level.getBlockState(pos);

            if (targetBlockEntity != null && AE2Compat.tryTickAE2BlockEntity(targetBlockEntity)) {
            } else if (targetTicker != null) {
                targetTicker.tick(level, pos, blockState, targetBlockEntity);
            } else if (blockState.isRandomlyTicking()) {
                blockState.randomTick(level, pos, level.getRandom());
            }
        }

        setRemainingTime(getRemainingTime() - 1);
        if (getRemainingTime() <= 0) {
            this.remove(RemovalReason.KILLED);
        }

        ci.cancel();
    }
}
