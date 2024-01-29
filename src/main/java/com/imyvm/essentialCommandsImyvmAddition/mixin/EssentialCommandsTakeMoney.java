package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.imyvm.economy.EconomyMod;
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil;
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerTeleporter.class)
public abstract class EssentialCommandsTakeMoney {
    @Inject(method = "execTeleport", at = @At("HEAD"), cancellable = true, remap = false)
    private static void takeMoney(ServerPlayerEntity playerEntity, MinecraftLocation dest, MutableText destName, CallbackInfo ci) throws CommandSyntaxException {
        var teleportData = EconomyUtil.INSTANCE.getOperationMoneyShouldTake(playerEntity, dest);
        var playerEntityEconomyData = EconomyMod.data.getOrCreate(teleportData.getPlayerEntity());
        var moneyShouldTake = teleportData.getBalanceShouldTake() * 100;
        if (playerEntityEconomyData.getMoney() < moneyShouldTake) {
            ci.cancel();
            EconomyUtil.INSTANCE.teleportDataMapDelete(new EconomyUtil.TeleportKey(dest.pos(), playerEntity.getUuid()));
            throw EssentialCommandsImyvmAdditionMain.INSTANCE.getNO_ENOUGH_MONEY().create();
        }
        playerEntityEconomyData.addMoney(-moneyShouldTake);
        EconomyUtil.INSTANCE.playerCommandCountAdd(playerEntity, teleportData.getOperationType());
        EconomyUtil.INSTANCE.teleportDataMapDelete(new EconomyUtil.TeleportKey(dest.pos(), playerEntity.getUuid()));
        playerEntity.sendMessage(ECText.access(playerEntity).getText("ecomy.info.take_money", Text.of(Long.toString(moneyShouldTake / 100))));
    }
}
