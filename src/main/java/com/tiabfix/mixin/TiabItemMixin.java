package com.tiabfix.mixin;

import com.tiabfix.config.TiabFixConfig;
import com.tiabfix.core.DurationComponent;
import com.tiabfix.core.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;
import org.mangorage.tiab.common.api.impl.ITimeAcceleratorEntity;
import org.mangorage.tiab.common.core.StoredTimeComponent;
import org.mangorage.tiab.common.items.TiabItem;
import org.mangorage.tiab.common.misc.CommonHelper;
import org.mangorage.tiab.common.misc.CommonSoundHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(TiabItem.class)
public abstract class TiabItemMixin {

    @Shadow
    public abstract int getEnergyCost(int timeRate);

    @Shadow
    public abstract boolean canUse(ItemStack stack, boolean isCreativeMode, int energyRequired);

    private int getCustomDuration(ItemStack stack) {
        DurationComponent duration = stack.get(ModComponents.DURATION.get());
        if (duration != null) {
            return duration.getTicks();
        }
        var cfg = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        return cfg.TICKS_CONST() * cfg.EACH_USE_DURATION();
    }

    @Overwrite(remap = false)
    public InteractionResult useOn(UseOnContext context) {
        if (!TiabFixConfig.enableCropAcceleration) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        var cfg = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();

        if (level.isClientSide) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState blockState = level.getBlockState(pos);
        BlockEntity targetTE = level.getBlockEntity(pos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        Optional<? extends ITimeAcceleratorEntity> existingEntity = ICommonTimeInABottleAPI.COMMON_API.get()
                .getEntities(level, new AABB(pos))
                .stream()
                .filter(e -> e.getBlockPos() != null && e.getBlockPos().equals(pos))
                .findFirst();

        boolean hasExistingAccelerator = existingEntity.isPresent();

        if (!hasExistingAccelerator) {
            if (blockState.is(ICommonTimeInABottleAPI.COMMON_API.get().getTagKey())) {
                return InteractionResult.FAIL;
            }
        }

        int nextRate = 1;
        int duration = getCustomDuration(stack);
        int energyRequired = getEnergyCost(nextRate);
        boolean isCreativeMode = player != null && player.isCreative();

        if (hasExistingAccelerator) {
            ITimeAcceleratorEntity entityTA = existingEntity.get();
            int currentRate = entityTA.getTimeRate();
            int maxRate = (int) Math.pow(2, cfg.MAX_RATE_MULTI() - 1);

            if (currentRate >= maxRate) {
                return InteractionResult.SUCCESS;
            }

            nextRate = currentRate * 2;
            energyRequired = getEnergyCost(nextRate);

            if (!canUse(stack, isCreativeMode, energyRequired)) {
                return InteractionResult.SUCCESS;
            }

            entityTA.setTimeRate(nextRate);
            int timeAdded = (duration - entityTA.getRemainingTime()) / 2;
            entityTA.setRemainingTime(entityTA.getRemainingTime() + timeAdded);
        } else {
            if (!canUse(stack, isCreativeMode, energyRequired)) {
                return InteractionResult.SUCCESS;
            }

            ITimeAcceleratorEntity entityTA = ICommonTimeInABottleAPI.COMMON_API.get()
                    .createEntity((ServerLevel) level);
            entityTA.setBlockPos(pos);
            entityTA.setRemainingTime(duration);
            level.addFreshEntity(entityTA.asEntity());
        }

        if (!isCreativeMode) {
            final int required = energyRequired;
            CommonHelper.modify(stack, ICommonTimeInABottleAPI.COMMON_API.get().getRegistration().getStoredTime(),
                    new StoredTimeComponent(0, 0), old -> {
                        var newStoredTime = Math.min(old.stored() - required, cfg.MAX_STORED_TIME());
                        return new StoredTimeComponent(newStoredTime, old.total());
                    });
        }

        CommonSoundHelper.playSound(level, pos, nextRate);

        return InteractionResult.SUCCESS;
    }
}
