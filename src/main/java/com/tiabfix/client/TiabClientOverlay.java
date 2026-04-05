package com.tiabfix.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = "tiabfix", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TiabClientOverlay {

    public static final Map<Integer, EntityAccelData> CLIENT_ENTITIES = new ConcurrentHashMap<>();

    public static class EntityAccelData {
        public int timeRate;
        public int remainingTicks;

        public EntityAccelData(int rate, int ticks) {
            this.timeRate = rate;
            this.remainingTicks = ticks;
        }
    }

    @SubscribeEvent
    public static void onClientTick(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide)
            return;
        CLIENT_ENTITIES.values().forEach(data -> {
            if (data.remainingTicks > 0)
                data.remainingTicks--;
        });
        CLIENT_ENTITIES.entrySet()
                .removeIf(entry -> entry.getValue().remainingTicks <= 0 || entry.getValue().timeRate <= 0);
    }
}
