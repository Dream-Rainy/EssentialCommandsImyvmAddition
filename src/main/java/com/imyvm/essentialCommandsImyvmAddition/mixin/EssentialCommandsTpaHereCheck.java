package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.commands.TeleportAskHereCommand;
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TeleportAskHereCommand.class)
public class EssentialCommandsTpaHereCheck {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lcom/fibermc/essentialcommands/playerdata/PlayerData;sendMessage(Ljava/lang/String;[Lnet/minecraft/text/Text;)V"), cancellable = true)
    private void checkBalanceAndCooldown(CommandContext<ServerCommandSource> context, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        EconomyUtil.INSTANCE.tpaCheckBalanceAndCooldown(context, cir);
    }
}
