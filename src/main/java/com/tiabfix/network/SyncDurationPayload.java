package com.tiabfix.network;

import com.tiabfix.core.DurationComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncDurationPayload(int durationSeconds) implements CustomPacketPayload {
    public static final Type<SyncDurationPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("tiabfix", "sync_duration"));

    public static final StreamCodec<FriendlyByteBuf, SyncDurationPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncDurationPayload::durationSeconds,
            SyncDurationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
