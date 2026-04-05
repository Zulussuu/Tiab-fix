package com.tiabfix;

import com.tiabfix.config.TiabFixConfig;
import com.tiabfix.core.DurationComponent;
import com.tiabfix.core.ModComponents;
import com.tiabfix.network.SyncDurationPayload;
import com.tiabfix.network.SyncEntityAccelPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;
import org.mangorage.tiab.common.api.impl.ITiabItem;
import org.mangorage.tiab.common.core.StoredTimeComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@net.neoforged.fml.common.EventBusSubscriber(modid = "tiabfix", bus = net.neoforged.fml.common.EventBusSubscriber.Bus.GAME)
public class TiabInteractionHandler {

    public static final Map<UUID, EntityAccelData> ACCELERATED_ENTITIES = new HashMap<>();

    public static class EntityAccelData {
        public int timeRate;
        public int remainingTicks;

        public EntityAccelData(int rate, int ticks) {
            this.timeRate = rate;
            this.remainingTicks = ticks;
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        if (!event.getEntity().isCrouching())
            return;
        if (!TiabFixConfig.enableDurationCycling)
            return;
        handleDurationCycle(event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getEntity().isCrouching())
            return;
        if (!TiabFixConfig.enableDurationCycling)
            return;
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND)
            return;
        handleDurationCycle(event.getEntity(), event.getItemStack());
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleInteraction(event, event.getTarget(), "EntityInteract");
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleInteraction(event, event.getTarget(), "EntityInteractSpecific");
    }

    private static void handleInteraction(PlayerInteractEvent event, Entity target, String eventName) {
        if (!TiabFixConfig.enableEntityAcceleration) {
            return;
        }

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        boolean isTiab = stack.getItem() instanceof ITiabItem;

        if (!isTiab)
            return;
        if (player.level().isClientSide())
            return;
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;

        accelerateEntity(player, stack, target);
        if (event instanceof PlayerInteractEvent.EntityInteract c) {
            c.setCanceled(true);
        } else if (event instanceof PlayerInteractEvent.EntityInteractSpecific c) {
            c.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        var iterator = ACCELERATED_ENTITIES.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            UUID entityUUID = entry.getKey();
            EntityAccelData data = entry.getValue();

            Entity entity = null;
            for (var level : event.getServer().getAllLevels()) {
                entity = ((net.minecraft.server.level.ServerLevel) level).getEntity(entityUUID);
                if (entity != null)
                    break;
            }

            if (entity == null || entity.isRemoved()) {
                iterator.remove();
                continue;
            }

            PacketDistributor.sendToAllPlayers(
                    new SyncEntityAccelPayload(entity.getId(), data.timeRate, data.remainingTicks));

            for (int i = 0; i < data.timeRate; i++) {
                if (entity instanceof AgeableMob ageable) {
                    int age = ageable.getAge();
                    if (age < 0) {
                        ageable.setAge(age + 1);
                    } else if (age > 0) {
                        ageable.setAge(age - 1);
                    }
                } else {
                    entity.tick();
                }
            }

            data.remainingTicks--;
            if (data.remainingTicks <= 0) {
                PacketDistributor.sendToAllPlayers(new SyncEntityAccelPayload(entity.getId(), 0, 0));
                iterator.remove();
            }
        }
    }

    private static void handleDurationCycle(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof ITiabItem))
            return;
        if (!isUnlimitedTiab(stack))
            return;

        DurationComponent current = stack.getOrDefault(ModComponents.DURATION.get(), new DurationComponent(30));
        DurationComponent next = current.cycle();
        stack.set(ModComponents.DURATION.get(), next);

        if (player.level().isClientSide) {
            PacketDistributor.sendToServer(new SyncDurationPayload(next.durationSeconds()));
            player.displayClientMessage(
                    Component.literal("Duration: ").withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(next.durationSeconds() + "s").withStyle(ChatFormatting.YELLOW)),
                    true);
        }

    }

    private static void accelerateEntity(Player player, ItemStack stack, Entity target) {
        if (player.level().isClientSide())
            return;

        var cfg = ICommonTimeInABottleAPI.COMMON_API.get().getConfig();
        UUID targetId = target.getUUID();

        EntityAccelData existing = ACCELERATED_ENTITIES.get(targetId);

        int nextRate;
        int energyRequired;
        boolean isCreativeMode = player.isCreative();

        DurationComponent durationComp = stack.getOrDefault(ModComponents.DURATION.get(), new DurationComponent(30));
        int totalTicks = durationComp.getTicks();

        if (existing != null) {
            int currentRate = existing.timeRate;
            if (currentRate >= 256) {
                return;
            }
            nextRate = currentRate * 2;
            energyRequired = 20 * 30 * nextRate;

            if (!canUse(stack, isCreativeMode, energyRequired)) {
                return;
            }

            existing.timeRate = nextRate;
            int remaining = existing.remainingTicks;
            int added = (totalTicks - remaining) / 2;
            existing.remainingTicks += added;
        } else {
            nextRate = 1;
            energyRequired = 20 * 30;

            if (!canUse(stack, isCreativeMode, energyRequired)) {
                return;
            }

            existing = new EntityAccelData(nextRate, totalTicks);
            ACCELERATED_ENTITIES.put(targetId, existing);
        }

        consumeTime(player, stack, energyRequired);

        PacketDistributor.sendToAllPlayers(
                new SyncEntityAccelPayload(target.getId(), existing.timeRate, existing.remainingTicks));

        player.displayClientMessage(
                Component.literal("Entity accelerated: x" + nextRate).withStyle(ChatFormatting.GREEN),
                true);
    }

    private static boolean canUse(ItemStack stack, boolean isCreativeMode, int energyRequired) {
        if (isCreativeMode)
            return true;
        if (isUnlimitedTiab(stack))
            return true;

        var comp = ICommonTimeInABottleAPI.COMMON_API.get().getRegistration().getStoredTime();
        StoredTimeComponent stored = (StoredTimeComponent) stack.getOrDefault(comp, new StoredTimeComponent(0, 0));
        return stored.stored() >= energyRequired;
    }

    private static void consumeTime(Player player, ItemStack stack, int cost) {
        if (player.isCreative())
            return;
        if (isUnlimitedTiab(stack))
            return;

        var comp = ICommonTimeInABottleAPI.COMMON_API.get().getRegistration().getStoredTime();
        StoredTimeComponent current = (StoredTimeComponent) stack.getOrDefault(comp, new StoredTimeComponent(0, 0));
        stack.set(comp, new StoredTimeComponent(Math.max(0, current.stored() - cost), current.total()));
    }

    private static boolean isUnlimitedTiab(ItemStack stack) {
        var comp = ICommonTimeInABottleAPI.COMMON_API.get().getRegistration().getStoredTime();
        StoredTimeComponent stored = (StoredTimeComponent) stack.getOrDefault(comp, new StoredTimeComponent(0, 0));
        return stored.stored() >= Integer.MAX_VALUE - 1000;
    }
}
