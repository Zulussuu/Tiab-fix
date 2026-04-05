package com.tiabfix.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = "tiabfix", bus = EventBusSubscriber.Bus.MOD)
public class TiabFixConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_CROP_ACCELERATION = BUILDER
            .comment("Enable crop/block acceleration feature")
            .define("enableCropAcceleration", true);

    private static final ModConfigSpec.BooleanValue ENABLE_ENTITY_ACCELERATION = BUILDER
            .comment("Enable entity acceleration feature (speeds up growth, breeding cooldown)")
            .define("enableEntityAcceleration", true);

    private static final ModConfigSpec.BooleanValue ENABLE_DURATION_CYCLING = BUILDER
            .comment("Enable left-click duration cycling for unlimited TIAB")
            .define("enableDurationCycling", true);

    private static final ModConfigSpec.BooleanValue SHOW_ENTITY_OVERLAY = BUILDER
            .comment("Show acceleration rate overlay above accelerated entities")
            .define("showEntityOverlay", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableCropAcceleration = true;
    public static boolean enableEntityAcceleration = true;
    public static boolean enableDurationCycling = true;
    public static boolean showEntityOverlay = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableCropAcceleration = ENABLE_CROP_ACCELERATION.get();
        enableEntityAcceleration = ENABLE_ENTITY_ACCELERATION.get();
        enableDurationCycling = ENABLE_DURATION_CYCLING.get();
        showEntityOverlay = SHOW_ENTITY_OVERLAY.get();
    }
}
