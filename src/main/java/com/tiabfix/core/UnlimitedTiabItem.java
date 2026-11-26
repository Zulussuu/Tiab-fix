package com.tiabfix.core;

import com.tiabfix.TiabInteractionHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.mangorage.tiab.common.items.TiabItem;
import java.util.List;

public class UnlimitedTiabItem extends TiabItem {
    public UnlimitedTiabItem(Item.Properties properties) { super(properties); }
    @Override public void tickBottle(ItemStack stack, int ticks) {}
    @Override public void tickBottle(ItemStack stack) {}
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Stored Time: Unlimited").withStyle(ChatFormatting.GREEN));
        int duration = stack.getOrDefault(ModComponents.DURATION.get(), new DurationComponent(30)).durationSeconds();
        tooltip.add(Component.literal("Duration: " + duration + "s").withStyle(ChatFormatting.YELLOW));
    }
}