package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.QueuedTeleport;
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil;
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain;
import dev.jpcode.eccore.util.TimeUtil;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QueuedTeleport.class)
public class EssentialCommandsSetTpaDelay {
    @Shadow(remap = false)
    private int ticksRemaining;
    @Inject(method = "<init>(Lcom/fibermc/essentialcommands/playerdata/PlayerData;Lnet/minecraft/text/Text;)V", at = @At("RETURN"))
    private void setDelay(PlayerData playerData, Text destName, CallbackInfo ci) {
        var expression = EssentialCommandsImyvmAdditionMain.INSTANCE.getECONOMY_CONFIG().getTPA_DELAY();
        var type = EconomyUtil.OperationType.TPA;
        var delay = EconomyUtil.INSTANCE.calculate(
                expression.with(
                        "x",EconomyUtil.INSTANCE.getPlayerCommandCount(
                                playerData.getPlayer(), type
                        )
                )
        );
        this.ticksRemaining = (int) delay * TimeUtil.TPS;
        EconomyUtil.INSTANCE.updatePlayerCommandCooldown(playerData.getPlayer(), EconomyUtil.OperationType.TPA);
    }
}
