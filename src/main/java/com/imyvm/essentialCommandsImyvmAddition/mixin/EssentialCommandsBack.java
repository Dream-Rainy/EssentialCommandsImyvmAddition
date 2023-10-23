package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.BackCommand;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackCommand.class)
public class EssentialCommandsBack {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lcom/fibermc/essentialcommands/teleportation/PlayerTeleporter;requestTeleport(Lcom/fibermc/essentialcommands/playerdata/PlayerData;Lcom/fibermc/essentialcommands/types/MinecraftLocation;Lnet/minecraft/text/MutableText;)V"))
    private void checkMoneyAndCooldown(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();
        MinecraftLocation loc = playerData.getPreviousLocation();
        EconomyUtil.INSTANCE.homeAndBackCheckBalanceAndCooldown(loc, context.getSource().getPlayerOrThrow(), EconomyUtil.OperationType.BACK);
    }
}
