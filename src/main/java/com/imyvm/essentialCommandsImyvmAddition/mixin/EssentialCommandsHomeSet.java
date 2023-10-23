package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.HomeSetCommand;
import com.fibermc.essentialcommands.text.ECText;
import com.imyvm.economy.EconomyMod;
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil;
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HomeSetCommand.class)
public class EssentialCommandsHomeSet {
    @Inject(method = "exec", at = @At("HEAD"), remap = false)
    private static void checkMoney(CommandContext<ServerCommandSource> context, String homeName, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        var serverPlayerEntity = context.getSource().getPlayerOrThrow();
        var playerEconomyData = EconomyMod.data.getOrCreate(serverPlayerEntity);
        var playerData = ((ServerPlayerEntityAccess) serverPlayerEntity).ec$getPlayerData();
        var balanceShouldTake = EconomyUtil.INSTANCE.calculate(
                EssentialCommandsImyvmAdditionMain.INSTANCE.getECONOMY_CONFIG().getHOME_SET_PRICE().with(
                        "x", playerData.getHomeEntries().size()
                ));
        if (playerEconomyData.getMoney() < balanceShouldTake) {
            throw EssentialCommandsImyvmAdditionMain.INSTANCE.getNO_ENOUGH_MONEY().create();
        }
        playerEconomyData.addMoney(-balanceShouldTake);
        serverPlayerEntity.sendMessage(ECText.access(serverPlayerEntity).getText("ecomy.info.take_money", Text.of(Double.toString(balanceShouldTake))));
    }
}
