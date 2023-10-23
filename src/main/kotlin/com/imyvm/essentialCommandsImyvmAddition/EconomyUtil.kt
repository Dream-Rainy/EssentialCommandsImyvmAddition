package com.imyvm.essentialCommandsImyvmAddition

import com.ezylang.evalex.EvaluationException
import com.ezylang.evalex.Expression
import com.fibermc.essentialcommands.text.ECText
import com.fibermc.essentialcommands.text.TextFormatType
import com.fibermc.essentialcommands.types.MinecraftLocation
import com.imyvm.economy.EconomyMod
import com.imyvm.essentialCommandsImyvmAddition.EconomyUtil.OperationType.*
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain.ECONOMY_CONFIG
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain.LOGGER
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain.NO_ENOUGH_MONEY
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain.NO_SUCH_PLAYER
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain.ON_COOLDOWN
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.time.LocalDateTime
import java.util.UUID


object EconomyUtil {
    data class PlayerData (
        var tpaCount: Int,
        var tpaCooldown: LocalDateTime,
        var homeUseCount: Int,
        var homeCooldown: LocalDateTime,
        var backCount: Int,
        var warpCount: Int,
        var warpCooldown: LocalDateTime,
    )

    data class TeleportData (
        val operationType: OperationType,
        val playerEntity: ServerPlayerEntity,
        val balanceShouldTake: Long
    )

    data class TeleportKey (
        val location: Vec3d,
        val playerUUID: UUID
    )

    enum class OperationType {
        TPA,HOME,BACK,WARP_TP,NONE
    }

    private val teleportDataMap = mutableMapOf<TeleportKey, TeleportData>()
    private val playerDataMap = mutableMapOf<UUID, PlayerData>()
    private val EVALUATION_EXCEPTION = SimpleCommandExceptionType(
        ECText.getInstance().getText(
            "ecomy.cmd.error.evaluation_exception",
            TextFormatType.Error
        )
    )
    private val NO_SUCH_OPERATION = SimpleCommandExceptionType(
        ECText.getInstance().getText(
            "ecomy.cmd.error.no_such_operation",
            TextFormatType.Error
        )
    )
    fun calculate(expression: Expression):Long {
        try {
            return expression.evaluate().numberValue.longValueExact()
        } catch (e: EvaluationException) {
            LOGGER.error(e.message)
            e.printStackTrace()
            throw EVALUATION_EXCEPTION.create()
        }
    }

    fun mapClear() {
        playerDataMap.clear()
        teleportDataMap.clear()
        println("Cleared")
    }

    fun teleportDataMapDelete(key: TeleportKey) {
        teleportDataMap.remove(key)
    }

    fun getOperationMoneyShouldTake(playerEntity: ServerPlayerEntity, location: MinecraftLocation): TeleportData {
        val key = TeleportKey(location.pos(), playerEntity.uuid)
        return teleportDataMap[key] ?: TeleportData(NONE, playerEntity,0)
    }

    fun playerCommandCountAdd(playerEntity: ServerPlayerEntity, operationType: OperationType) {
        when (operationType) {
            TPA -> playerDataMap[playerEntity.uuid]!!.tpaCount += 1
            HOME -> playerDataMap[playerEntity.uuid]!!.homeUseCount += 1
            BACK -> playerDataMap[playerEntity.uuid]!!.backCount += 1
            WARP_TP -> playerDataMap[playerEntity.uuid]!!.warpCount += 1
            else -> NONE
        }
    }

    fun getPlayerCommandCount(playerEntity: ServerPlayerEntity, operationType: OperationType) :Int {
        if (!playerDataMap.containsKey(playerEntity.uuid)) {
            playerDataMap[playerEntity.uuid] =
                PlayerData(
                    1, LocalDateTime.now(),
                    1,LocalDateTime.now(),
                    1,1, LocalDateTime.now()
                )
            return 1
        }
        return when (operationType) {
            TPA -> playerDataMap[playerEntity.uuid]?.tpaCount ?: throw NullPointerException()
            HOME -> playerDataMap[playerEntity.uuid]?.homeUseCount ?: throw NullPointerException()
            BACK -> playerDataMap[playerEntity.uuid]?.backCount ?: throw NullPointerException()
            WARP_TP -> playerDataMap[playerEntity.uuid]?.warpCount ?: throw NullPointerException()
            NONE -> 0
        }
    }

    private fun getPlayerCommandCooldown(playerEntity: ServerPlayerEntity, operationType: OperationType): LocalDateTime {
        if (!playerDataMap.containsKey(playerEntity.uuid)) {
            playerDataMap[playerEntity.uuid] =
                PlayerData(
                    1, LocalDateTime.now(),
                    1,LocalDateTime.now(),
                    1,1, LocalDateTime.now()
                )
            return LocalDateTime.MIN
        }
        return when (operationType) {
            TPA -> playerDataMap[playerEntity.uuid]?.tpaCooldown ?: throw NullPointerException()
            HOME -> playerDataMap[playerEntity.uuid]?.homeCooldown ?: throw NullPointerException()
            WARP_TP -> playerDataMap[playerEntity.uuid]?.warpCooldown ?: throw NullPointerException()
            else -> LocalDateTime.MIN
        }
    }

    fun updatePlayerCommandCooldown(playerEntity: ServerPlayerEntity, operationType: OperationType) {
        when (operationType) {
            HOME -> {
                val expression = ECONOMY_CONFIG.HOME_TP_COOLDOWN.with(
                    "x",
                    getPlayerCommandCount(playerEntity, operationType)
                )
                val cooldown = calculate(expression)
                playerDataMap[playerEntity.uuid]!!.homeCooldown = LocalDateTime.now().plusSeconds(cooldown)
            }
            TPA -> {
                val expression = ECONOMY_CONFIG.TPA_COOLDOWN.with(
                    "x",
                    getPlayerCommandCount(playerEntity, operationType)
                )
                val cooldown = calculate(expression)
                playerDataMap[playerEntity.uuid]!!.tpaCooldown = LocalDateTime.now().plusSeconds(cooldown)
            }
            WARP_TP -> {
                val expression = ECONOMY_CONFIG.HOME_TP_COOLDOWN.with(
                    "x",
                    getPlayerCommandCount(playerEntity, operationType)
                )
                val cooldown = calculate(expression)
                playerDataMap[playerEntity.uuid]!!.warpCooldown = LocalDateTime.now().plusSeconds(cooldown)
            }
            else -> throw NO_SUCH_OPERATION.create()
        }
    }

    @Throws(CommandSyntaxException::class)
    fun tpaCheckBalanceAndCooldown(context: CommandContext<ServerCommandSource>, cir: CallbackInfoReturnable<Int?>) {
        val playerEntity = context.source.player ?: throw NO_SUCH_PLAYER.create()
        val playerEntityEconomyData = EconomyMod.data.getOrCreate(playerEntity)
        val balance = playerEntityEconomyData.money
        if (LocalDateTime.now().isBefore(getPlayerCommandCooldown(playerEntity, TPA))) {
            cir.setReturnValue(0)
            throw ON_COOLDOWN.create()
        }
        val moneyShouldTake = calculate(
            ECONOMY_CONFIG.TPA_PRICE.with(
                "x",
                getPlayerCommandCount(playerEntity, TPA)
            )
        )
        if (balance < moneyShouldTake) {
            cir.setReturnValue(0)
            throw NO_ENOUGH_MONEY.create()
        }
        val key = TeleportKey(playerEntity.pos, playerEntity.uuid)
        val data = TeleportData(TPA, playerEntity, moneyShouldTake)
        teleportDataMap[key] = data
    }

    @Throws(CommandSyntaxException::class)
    fun homeAndBackCheckBalanceAndCooldown(location: MinecraftLocation, playerEntity: ServerPlayerEntity, operationType: OperationType) {
        val playerEntityEconomyData = EconomyMod.data.getOrCreate(playerEntity)
        val balance = playerEntityEconomyData.money
        if (LocalDateTime.now().isBefore(getPlayerCommandCooldown(playerEntity, operationType))) {
            throw ON_COOLDOWN.create()
        }
        val expression = when (operationType) {
            HOME -> ECONOMY_CONFIG.HOME_TP_PRICE
            BACK -> ECONOMY_CONFIG.BACK_PRICE
            else -> throw NO_SUCH_OPERATION.create()
        }
        val moneyShouldTake = calculate(
            expression.with(
                "x",
                getPlayerCommandCount(playerEntity, operationType)
            )
        )
        if (balance < moneyShouldTake) {
            throw NO_ENOUGH_MONEY.create()
        }
        val key = TeleportKey(location.pos(), playerEntity.uuid)
        val data = TeleportData(operationType, playerEntity, moneyShouldTake)
        teleportDataMap[key] = data
        if (operationType == HOME) {
            updatePlayerCommandCooldown(playerEntity, HOME)
        }
    }

    @Throws(CommandSyntaxException::class)
    fun homeWarpCheckMoneyAndCoolDown(location: MinecraftLocation, warpName: String, player: ServerPlayerEntity): Boolean {
        if (!HomeWarpManager.getFreePermission(warpName, player)) {
            val playerEntityEconomyData = EconomyMod.data.getOrCreate(player)
            val balance = playerEntityEconomyData.money
            if (LocalDateTime.now().isBefore(getPlayerCommandCooldown(player, WARP_TP))) {
                throw ON_COOLDOWN.create()
            }
            val moneyShouldTake = calculate(
                ECONOMY_CONFIG.WARP_TP_PRICE.with(
                    "x",
                    getPlayerCommandCount(player, WARP_TP)
                )
            )
            if (balance < moneyShouldTake) {
                return false
            }
            val key = TeleportKey(location.pos(), player.uuid)
            val data = TeleportData(WARP_TP, player, moneyShouldTake)
            teleportDataMap[key] = data
            return true
        }
        return false
    }
}