package com.imyvm.essentialCommandsImyvmAddition.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.teleportation.QueuedTeleport;
import com.fibermc.essentialcommands.teleportation.TeleportManager;
import com.fibermc.essentialcommands.text.TextFormatType;
import dev.jpcode.eccore.util.TimeUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TeleportManager.class)
public class EssentialCommandsSendDelay {
    @Inject(method = "queueTeleport(Lcom/fibermc/essentialcommands/teleportation/QueuedTeleport;)V", at = @At(value = "INVOKE", target = "Lcom/fibermc/essentialcommands/playerdata/PlayerData;sendMessage(Ljava/lang/String;[Lnet/minecraft/text/Text;)V", ordinal = 1), cancellable = true)
    private void sendDelay(QueuedTeleport queuedTeleport, CallbackInfo ci) {
        ci.cancel();
        var playerData = queuedTeleport.getPlayerData();
        var playerAccess = ((ServerPlayerEntityAccess) playerData.getPlayer());
        playerData.sendMessage(
                "teleport.queued",
                queuedTeleport.getDestName().setStyle(playerAccess.ec$getProfile().getStyle(TextFormatType.Accent)),
                playerAccess.ec$getEcText().accent(String.format("%.1f", TimeUtil.ticksToSeconds(queuedTeleport.getTicksRemaining())))
        );
    }
}
