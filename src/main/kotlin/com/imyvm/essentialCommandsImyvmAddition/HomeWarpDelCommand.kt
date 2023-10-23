package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.playerdata.PlayerData
import com.fibermc.essentialcommands.text.ECText
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.server.command.ServerCommandSource

@Throws(CommandSyntaxException::class)
fun homeWarpDelCommand(
    context: CommandContext<ServerCommandSource>,
    homeWarpName: String
): Int {
    val senderPlayer = context.source.playerOrThrow
    val senderPlayerData = PlayerData.access(senderPlayer)
    val warpNameText = ECText.access(senderPlayer).accent(homeWarpName)
    try {
        HomeWarpManager.delHomeWarp(homeWarpName, senderPlayer)
        senderPlayerData.sendCommandFeedback("cmd.warp.home.del.feedback", warpNameText)
    } catch (e: CommandSyntaxException) {
        senderPlayerData.sendCommandError(e.message)
    }
    return Command.SINGLE_SUCCESS
}