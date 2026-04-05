package com.tiabfix.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncEntityAccelPayload(int entityId, int timeRate, int remainingTicks) implements CustomPacketPayload {
    public static final Type<SyncEntityAccelPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("tiabfix", "sync_entity_accel"));

    public static final StreamCodec<FriendlyByteBuf, SyncEntityAccelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEntityAccelPayload::entityId,
            ByteBufCodecs.VAR_INT, SyncEntityAccelPayload::timeRate,
            ByteBufCodecs.VAR_INT, SyncEntityAccelPayload::remainingTicks,
            SyncEntityAccelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
