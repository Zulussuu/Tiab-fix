package com.tiabfix.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record DurationComponent(int durationSeconds) {
    public static final int[] DURATIONS = { 30, 60, 120, 240 };

    public static final Codec<DurationComponent> CODEC = Codec.INT.xmap(
            DurationComponent::new,
            DurationComponent::durationSeconds);

    public static final StreamCodec<ByteBuf, DurationComponent> STREAM_CODEC = ByteBufCodecs.INT
            .map(DurationComponent::new, DurationComponent::durationSeconds);

    public DurationComponent cycle() {
        int currentIndex = -1;
        for (int i = 0; i < DURATIONS.length; i++) {
            if (DURATIONS[i] == durationSeconds) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % DURATIONS.length;
        return new DurationComponent(DURATIONS[nextIndex]);
    }

    public int getTicks() {
        return durationSeconds * 20;
    }
}
