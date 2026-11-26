package com.tiabfix.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("tiabfix");
    public static final DeferredHolder<Item, UnlimitedTiabItem> UNLIMITED = ITEMS.register("cheaters_time_in_a_bottle", () -> new UnlimitedTiabItem(new Item.Properties().stacksTo(1)));
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "tiabfix");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.tiabfix")).icon(() -> new ItemStack(UNLIMITED.get()))
        .displayItems((parameters, output) -> output.accept(UNLIMITED.get())).build());
    public static void register(IEventBus bus) { ITEMS.register(bus); TABS.register(bus); }
}