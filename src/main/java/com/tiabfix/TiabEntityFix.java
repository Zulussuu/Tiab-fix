package com.tiabfix;

import com.tiabfix.command.TiabFixCommands;
import com.tiabfix.config.TiabFixConfig;
import com.tiabfix.core.ModComponents;
import com.tiabfix.network.PacketHandler;
import com.tiabfix.network.SyncDurationPayload;
import com.tiabfix.network.SyncEntityAccelPayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.InterModComms;

@Mod("tiabfix")
public class TiabEntityFix {
    public TiabEntityFix(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, TiabFixConfig.SPEC);
        ModComponents.register(modBus);
        modBus.addListener(this::registerNetworking);
        modBus.addListener(this::sendIMC);
        NeoForge.EVENT_BUS.register(this);
    }

    private void registerNetworking(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        registrar.playToServer(
                SyncDurationPayload.TYPE,
                SyncDurationPayload.STREAM_CODEC,
                PacketHandler::handleSyncDuration);

        registrar.playToClient(
                SyncEntityAccelPayload.TYPE,
                SyncEntityAccelPayload.STREAM_CODEC,
                PacketHandler::handleSyncEntityAccel);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TiabFixCommands.register(event.getDispatcher());
    }

    private void sendIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", com.tiabfix.compat.TOPCompat::new);
    }
}
