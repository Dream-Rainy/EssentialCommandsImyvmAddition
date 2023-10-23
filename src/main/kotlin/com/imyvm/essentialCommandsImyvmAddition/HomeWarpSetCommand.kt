package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.playerdata.PlayerData
import com.fibermc.essentialcommands.text.ECText
import com.fibermc.essentialcommands.types.MinecraftLocation
import com.imyvm.economy.EconomyMod
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.server.command.ServerCommandSource

@Throws(CommandSyntaxException::class)
fun homeWarpSetCommand(
    context: CommandContext<ServerCommandSource>,
    homeWarpName: String,
    type: Int
): Int {
    val senderPlayer = context.source.playerOrThrow
    val senderPlayerData = PlayerData.access(senderPlayer)
    val senderPlayerEconomyData = EconomyMod.data.getOrCreate(senderPlayer)
    val warpNameText = ECText.access(senderPlayer).accent(homeWarpName)
    if (senderPlayerEconomyData.money < EconomyUtil.calculate(EssentialCommandsImyvmAdditionMain.ECONOMY_CONFIG.WARP_SET_HOME_PRICE)) {
        senderPlayerData.sendCommandError("cmd.warp.home.no_enough_money")
        return 0
    }
    try {
        HomeWarpManager.setHomeWarp(homeWarpName, MinecraftLocation(senderPlayer), senderPlayer, type)
        senderPlayerData.sendCommandFeedback("cmd.warp.home.set.feedback", warpNameText)
    } catch (e: CommandSyntaxException) {
        senderPlayerData.sendCommandError("cmd.warp.home.set.exists", warpNameText)
    }
    return Command.SINGLE_SUCCESS
}