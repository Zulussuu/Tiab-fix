package com.tiabfix.network;

import com.tiabfix.client.TiabClientOverlay;
import com.tiabfix.core.DurationComponent;
import com.tiabfix.core.ModComponents;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mangorage.tiab.common.api.impl.ITiabItem;

public class PacketHandler {

    public static void handleSyncDuration(final SyncDurationPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            var stack = player.getMainHandItem();
            if (stack.getItem() instanceof ITiabItem) {
                stack.set(ModComponents.DURATION.get(), new DurationComponent(payload.durationSeconds()));
            }
        });
    }

    public static void handleSyncEntityAccel(final SyncEntityAccelPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.timeRate() <= 0 || payload.remainingTicks() <= 0) {
                TiabClientOverlay.CLIENT_ENTITIES.remove(payload.entityId());
            } else {
                TiabClientOverlay.CLIENT_ENTITIES.put(payload.entityId(),
                        new TiabClientOverlay.EntityAccelData(payload.timeRate(), payload.remainingTicks()));
            }
        });
    }
}
