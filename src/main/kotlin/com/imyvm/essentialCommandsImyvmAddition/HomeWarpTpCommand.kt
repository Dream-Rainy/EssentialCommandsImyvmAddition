package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.commands.CommandUtil
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter
import com.fibermc.essentialcommands.text.ECText
import com.fibermc.essentialcommands.text.TextFormatType
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.server.command.ServerCommandSource

@Throws(CommandSyntaxException::class)
fun homeWarpTpCommand(
    context: CommandContext<ServerCommandSource>,
): Int {
    val senderPlayer = context.source.playerOrThrow
    val ecText = ECText.access(senderPlayer)
    val warpName = StringArgumentType.getString(context, "homewarpname")
    val warpNameText = ecText.accent(warpName)
    val loc = HomeWarpManager.getHomeWarp(warpName)
        ?: throw CommandUtil.createSimpleException(
            ecText.getText(
                "cmd.warp.tp.error.not_found",
                TextFormatType.Error,
                warpNameText
            )
        )
    if (!HomeWarpManager.checkPermission(warpName, senderPlayer)) {
        throw CommandUtil.createSimpleException(
            ecText.getText(
                "cmd.warp.tp.error.permission",
                TextFormatType.Error,
                warpNameText
            )
        )
    }

    if (!EconomyUtil.homeWarpCheckMoneyAndCoolDown(loc, warpName, senderPlayer)) {
        throw CommandUtil.createSimpleException(
            ecText.getText(
                "cmd.warp.home.no_enough_money",
                TextFormatType.Error
            )
        )
    }

    // Teleport & chat message
    PlayerTeleporter.requestTeleport(
        senderPlayer,
        loc,
        ecText.getText("cmd.warp.location_name", warpNameText)
    )
    return Command.SINGLE_SUCCESS
}