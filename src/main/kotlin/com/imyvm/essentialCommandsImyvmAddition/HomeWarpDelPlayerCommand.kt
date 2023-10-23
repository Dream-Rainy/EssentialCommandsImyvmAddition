package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.playerdata.PlayerData
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity

@Throws(CommandSyntaxException::class)
fun homeWarpDelPlayerCommand(
    context: CommandContext<ServerCommandSource>,
    homeWarpName: String,
    playerNeedRemove: ServerPlayerEntity
): Int {
    val senderPlayer = context.source.playerOrThrow
    val senderPlayerData = PlayerData.access(senderPlayer)
    try {
        HomeWarpManager.delFromHomeWarp(homeWarpName, senderPlayer, playerNeedRemove)
        senderPlayerData.sendCommandFeedback("cmd.warp.home.player.del.feedback", playerNeedRemove.name)
    } catch (e: CommandSyntaxException) {
        senderPlayerData.sendCommandError(e.message)
    }
    return Command.SINGLE_SUCCESS
}