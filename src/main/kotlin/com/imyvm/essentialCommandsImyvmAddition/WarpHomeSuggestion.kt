package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion
import com.fibermc.essentialcommands.types.WarpLocation
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource


object WarpHomeSuggestion {
    //Brigader Suggestions
    val STRING_SUGGESTIONS_PROVIDER = ListSuggestion.ofContext { ctx: CommandContext<ServerCommandSource> ->
        HomeWarpManager
            .getAccessibleWarps(ctx.source.playerOrThrow)
            .map { obj: MutableMap.MutableEntry<String, WarpLocation> -> obj.value.name }
            .toList()
    }
    val HOME_WARP_OWNER = ListSuggestion.ofContext { ctx: CommandContext<ServerCommandSource> ->
        HomeWarpManager
            .getOwnerWarps(ctx.source.playerOrThrow)
            .map { obj: WarpLocation -> obj.name }
            .toList()
    }
}
