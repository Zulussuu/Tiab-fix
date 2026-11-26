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

                ItemStack tiabStack = new ItemStack(com.tiabfix.core.ModItems.UNLIMITED.get());
                player.getInventory().add(tiabStack);
                source.sendSuccess(() -> Component.literal("Cheater's Time in a Bottle added to your inventory!"),
                                true);

                return 1;
        }
}
