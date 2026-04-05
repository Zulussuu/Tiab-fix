package com.tiabfix.compat;

import com.tiabfix.client.TiabClientOverlay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class JadeCompat implements IWailaPlugin, IEntityComponentProvider {

    private static final ResourceLocation ENTITY_ACCEL = ResourceLocation.fromNamespaceAndPath("tiabfix",
            "entity_accel");

    @Override
    public void register(IWailaCommonRegistration registration) {
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(this, Entity.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        Entity entity = accessor.getEntity();
        var data = TiabClientOverlay.CLIENT_ENTITIES.get(entity.getId());

        if (data != null && data.timeRate > 1) {
            tooltip.add(Component.literal("Acceleration Rate: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("x" + data.timeRate).withStyle(ChatFormatting.WHITE)));
            int seconds = data.remainingTicks / 20;
            tooltip.add(Component.literal("Acceleration Time Remaining: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(seconds + "s").withStyle(ChatFormatting.WHITE)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ENTITY_ACCEL;
    }
}
