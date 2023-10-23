package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.commands.ListCommandFactory
import com.fibermc.essentialcommands.text.ECText
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.EntityArgumentType.getPlayer
import net.minecraft.command.argument.EntityArgumentType.player
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource

fun register(
    dispatcher: CommandDispatcher<ServerCommandSource>
) {
    val builder = literal("homewarp")
        .requires(ServerCommandSource::isExecutedByPlayer)
        .then(literal("tp")
            .then(argument("homewarpname", word())
                .suggests(WarpHomeSuggestion.STRING_SUGGESTIONS_PROVIDER)
                .executes{ context ->
                    homeWarpTpCommand(
                        context
                    )
                }
            )
        )
        .then(literal("list")
            .executes(
                ListCommandFactory.create(
                    ECText.getInstance().getString("cmd.warp.list.start"),
                    "warp tp"
                ) { context: CommandContext<ServerCommandSource> ->
                    HomeWarpManager.getAccessibleWarps(context.source.playerOrThrow)
                }
            )
        )
        .then(argument("homewarpname", word())
            .suggests(WarpHomeSuggestion.HOME_WARP_OWNER)
            .then(literal("set")
                .then(argument("type", integer(0,1))
                    .executes{ context ->
                        homeWarpSetCommand(
                            context,
                            getString(context, "homewarpname"),
                            getInteger(context, "type")
                        )
                    }
                )
            )
            .then(literal("remove")
                .executes{ context ->
                    homeWarpDelCommand(
                        context,
                        getString(context, "homewarpname")
                    )
                }
            )
            .then(literal("mode")
                .executes{ context ->
                    HomeWarpManager.changeHomeWarpMode(
                        getString(context, "homewarpname"),
                        context.source.playerOrThrow
                    )
                    1
                }
            )
            .then(literal("player")
                .then(argument("player", player())
                    .then(literal("add")
                        .executes{ context ->
                            homeWarpAddPlayerCommand(
                                context,
                                getString(context, "homewarpname"),
                                getPlayer(context, "player")
                            )
                        }
                    )
                    .then(literal("remove")
                        .executes{ context ->
                            homeWarpDelPlayerCommand(
                                context,
                                getString(context, "homewarpname"),
                                getPlayer(context, "player")
                            )
                        }
                    )
                )
            )
        )
    dispatcher.register(builder)
}