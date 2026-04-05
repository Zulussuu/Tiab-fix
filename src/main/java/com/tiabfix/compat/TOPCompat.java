package com.tiabfix.compat;

import com.tiabfix.client.TiabClientOverlay;
import mcjty.theoneprobe.api.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;

import java.util.function.Function;

public class TOPCompat implements IProbeInfoEntityProvider, Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe probe) {
        probe.registerEntityProvider(this);
        return null;
    }

    @Override
    public String getID() {
        return "tiabfix:entity_accel";
    }

    @Override
    public void addProbeEntityInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level, Entity entity,
            IProbeHitEntityData data) {
        var accelData = TiabClientOverlay.CLIENT_ENTITIES.get(entity.getId());
        if (accelData != null && accelData.timeRate > 1) {
            probeInfo.text(Component.literal("Acceleration Rate: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("x" + accelData.timeRate).withStyle(ChatFormatting.WHITE)));

            int seconds = accelData.remainingTicks / 20;
            probeInfo.text(Component.literal("Acceleration Time Remaining: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(seconds + "s").withStyle(ChatFormatting.WHITE)));
        }
    }
}
