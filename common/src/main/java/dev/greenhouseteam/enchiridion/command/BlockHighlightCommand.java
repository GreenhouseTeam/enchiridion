package dev.greenhouseteam.enchiridion.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.greenhouseteam.enchiridion.Enchiridion;
import dev.greenhouseteam.enchiridion.network.clientbound.SetOutlinedBlocksClientboundPacket;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockHighlightCommand {

    private static int executeCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!source.isPlayer()) {
            return 0;
        }
        ServerPlayer player = source.getPlayerOrException();
        Block block = BlockStateArgument.getBlock(context, "block").getState().getBlock();
        var range = IntegerArgumentType.getInteger(context, "range");
        var color = block.defaultMapColor().calculateRGBColor(MapColor.Brightness.HIGH);
        int duration = IntegerArgumentType.getInteger(context, "duration");
        Iterator<BlockPos> blocks = BlockPos.withinManhattan(
                player.blockPosition(),
                range,
                range,
                range
        ).iterator();
        List<BlockPos> blocksList = new ArrayList<>();
        while (blocks.hasNext()) {
            BlockPos pos = blocks.next();
            BlockState state = player.level().getBlockState(pos);
            if (state.getBlock().equals(block)) {
                blocksList.add(new BlockPos(pos)); // To deal with mutable BlockPos
            }
        }

        Enchiridion.getHelper().sendClientbound(player, new SetOutlinedBlocksClientboundPacket(
                blocksList,
                color,
                duration

        ));


        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register(
                Commands.literal("blockhighlight")
                        .then(
                                Commands.argument("block", BlockStateArgument.block(commandBuildContext))
                                        .then(
                                                Commands.argument("range", IntegerArgumentType.integer(1))
                                                        .then(
                                                                Commands.argument("duration", IntegerArgumentType.integer(1))
                                                                        .executes(BlockHighlightCommand::executeCommand)
                                                        )
                                        )
                        )
        );

    }
}
