package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.commands.HomeCommand;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.teleportation.QueuedLocationTeleport;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil;
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jpcode.eccore.util.TimeUtil;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HomeCommand.class)
public class EssentialCommandsHome {
    @Inject(method = "exec(Lcom/fibermc/essentialcommands/playerdata/PlayerData;Lcom/fibermc/essentialcommands/playerdata/PlayerData;Ljava/lang/String;)I", at = @At("HEAD"), cancellable = true, remap = false)
    private static void checkMoneyAndCooldown(PlayerData senderPlayerData, PlayerData targetPlayerData, String homeName, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        var playerEntity = senderPlayerData.getPlayer();
        var ecText = ECText.access(playerEntity);
        var homeNameText = ecText.getText(
                "cmd.home.location_name",
                TextFormatType.Default,
                ecText.accent(homeName)
        );
        MinecraftLocation loc = targetPlayerData.getHomeLocation(homeName);
        if (loc == null) {
            Message msg = ecText.getText(
                    "cmd.home.tp.error.not_found",
                    TextFormatType.Error,
                    Text.literal(homeName));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
        EconomyUtil.INSTANCE.homeAndBackCheckBalanceAndCooldown(loc, playerEntity, EconomyUtil.OperationType.HOME);
        var delay = EconomyUtil.INSTANCE.calculate(
                EssentialCommandsImyvmAdditionMain.INSTANCE.getECONOMY_CONFIG().getHOME_TP_DELAY().with(
                        "x",
                        EconomyUtil.INSTANCE.getPlayerCommandCount(playerEntity, EconomyUtil.OperationType.HOME)
                )
        ) * TimeUtil.TPS;
        var queuedTeleport = new QueuedLocationTeleport(senderPlayerData, loc, homeNameText, (int) delay);
        PlayerTeleporter.requestTeleport(queuedTeleport);
        cir.setReturnValue(1);
    }
}
