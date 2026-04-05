package com.tiabfix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.tiabfix.core.DurationComponent;
import com.tiabfix.core.ModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.mangorage.tiab.common.api.ICommonTimeInABottleAPI;
import org.mangorage.tiab.common.core.StoredTimeComponent;

import java.util.List;

public class TiabFixCommands {

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(
                                Commands.literal("tiabfix")
                                                .requires(source -> source.hasPermission(2))
                                                .then(Commands.literal("unlimited")
                                                                .executes(TiabFixCommands::giveUnlimitedTiab)));
        }

        private static int giveUnlimitedTiab(CommandContext<CommandSourceStack> context) {
                CommandSourceStack source = context.getSource();
                ServerPlayer player = source.getPlayer();

                if (player == null) {
                        source.sendFailure(Component.literal("This command can only be used by a player"));
                        return 0;
                }

                var registration = ICommonTimeInABottleAPI.COMMON_API.get().getRegistration();
                ItemStack tiabStack = new ItemStack((net.minecraft.world.item.Item) registration.getTiabItem());

                tiabStack.set(registration.getStoredTime(),
                                new StoredTimeComponent(Integer.MAX_VALUE, Integer.MAX_VALUE));
                tiabStack.set(ModComponents.DURATION.get(), new DurationComponent(30));

                tiabStack.set(DataComponents.CUSTOM_NAME,
                                Component.literal("Cheater's Time in a Bottle").withStyle(ChatFormatting.LIGHT_PURPLE,
                                                ChatFormatting.ITALIC));

                ItemLore lore = new ItemLore(
                                List.of(
                                                Component.literal("⚡ ").withStyle(ChatFormatting.GOLD)
                                                                .append(Component.literal("UNLIMITED TIME").withStyle(
                                                                                ChatFormatting.YELLOW,
                                                                                ChatFormatting.BOLD))
                                                                .append(Component.literal(" ⚡")
                                                                                .withStyle(ChatFormatting.GOLD)),
                                                Component.literal(""),
                                                Component.literal("∞ Stored Time").withStyle(ChatFormatting.AQUA),
                                                Component.literal("∞ Total Time Gathered")
                                                                .withStyle(ChatFormatting.GRAY),
                                                Component.literal(""),
                                                Component.literal("Current Duration: 30s")
                                                                .withStyle(ChatFormatting.GREEN)),
                                List.of(
                                                Component.literal("⚡ ").withStyle(ChatFormatting.GOLD)
                                                                .append(Component.literal("UNLIMITED TIME").withStyle(
                                                                                ChatFormatting.YELLOW,
                                                                                ChatFormatting.BOLD))
                                                                .append(Component.literal(" ⚡")
                                                                                .withStyle(ChatFormatting.GOLD)),
                                                Component.literal(""),
                                                Component.literal("∞ Stored Time").withStyle(ChatFormatting.AQUA),
                                                Component.literal("∞ Total Time Gathered")
                                                                .withStyle(ChatFormatting.GRAY),
                                                Component.literal(""),
                                                Component.literal("Current Duration: 30s")
                                                                .withStyle(ChatFormatting.GREEN),
                                                Component.literal(""),
                                                Component.literal("Shift + Left Click to change duration").withStyle(
                                                                ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
                tiabStack.set(DataComponents.LORE, lore);

                player.getInventory().add(tiabStack);
                source.sendSuccess(() -> Component.literal("Cheater's Time in a Bottle added to your inventory!"),
                                true);

                return 1;
        }
}
