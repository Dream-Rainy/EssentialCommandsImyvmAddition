package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.playerdata.PlayerData
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

@Throws(CommandSyntaxException::class)
fun homeWarpAddPlayerCommand(
    context: CommandContext<ServerCommandSource>,
    homeWarpName: String,
    playerNeedAdd: ServerPlayerEntity
): Int {
    val senderPlayer = context.source.playerOrThrow
    val senderPlayerData = PlayerData.access(senderPlayer)
    try {
        HomeWarpManager.addToHomeWarp(homeWarpName, senderPlayer, playerNeedAdd)
        senderPlayerData.sendCommandFeedback("cmd.warp.home.player.add.feedback", playerNeedAdd.name)
    } catch (e: CommandSyntaxException) {
        senderPlayerData.sendCommandError(e.message)
    }
    return Command.SINGLE_SUCCESS
}