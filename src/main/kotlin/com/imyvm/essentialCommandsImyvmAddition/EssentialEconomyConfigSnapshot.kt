package com.imyvm.essentialCommandsImyvmAddition

import com.ezylang.evalex.Expression

import java.time.ZoneId


class EssentialEconomyConfigSnapshot(essentialEconomyConfig: EssentialEconomyConfig) {
    val HOME_TP_PRICE: Expression
    val HOME_TP_COOLDOWN: Expression
    val HOME_SET_PRICE: Expression
    val HOME_TP_DELAY: Expression
    val BACK_PRICE: Expression
    val TPA_PRICE: Expression
    val TPA_COOLDOWN: Expression
    val TPA_DELAY: Expression
    val TIME_ZONE: ZoneId
    val WARP_TP_PRICE: Expression
    val WARP_TP_COOLDOWN: Expression
    val WARP_TP_DELAY: Expression
    val WARP_HOME_PRICE: Expression
    val WARP_HOME_COOLDOWN: Expression
    val WARP_HOME_DELAY: Expression
    val WARP_SET_HOME_PRICE: Expression

    init {
        HOME_SET_PRICE = Expression(essentialEconomyConfig.HOME_SET_PRICE.getValue())
        HOME_TP_PRICE = Expression(essentialEconomyConfig.HOME_TP_PRICE.getValue())
        HOME_TP_COOLDOWN = Expression(essentialEconomyConfig.HOME_TP_COOLDOWN.getValue())
        HOME_TP_DELAY = Expression(essentialEconomyConfig.HOME_TP_DELAY.getValue())
        BACK_PRICE = Expression(essentialEconomyConfig.BACK_PRICE.getValue())
        TPA_PRICE = Expression(essentialEconomyConfig.TPA_PRICE.getValue())
        TPA_COOLDOWN = Expression(essentialEconomyConfig.TPA_COOLDOWN.getValue())
        TPA_DELAY = Expression(essentialEconomyConfig.TPA_DELAY.getValue())
        TIME_ZONE = essentialEconomyConfig.TIME_ZONE.getValue()
        WARP_TP_PRICE = Expression(essentialEconomyConfig.WARP_TP_PRICE.getValue())
        WARP_TP_COOLDOWN = Expression(essentialEconomyConfig.WARP_TP_COOLDOWN.getValue())
        WARP_TP_DELAY = Expression(essentialEconomyConfig.WARP_TP_DELAY.getValue())
        WARP_HOME_PRICE = Expression(essentialEconomyConfig.WARP_HOME_PRICE.getValue())
        WARP_HOME_COOLDOWN = Expression(essentialEconomyConfig.WARP_HOME_COOLDOWN.getValue())
        WARP_HOME_DELAY = Expression(essentialEconomyConfig.WARP_HOME_DELAY.getValue())
        WARP_SET_HOME_PRICE = Expression(essentialEconomyConfig.WARP_SET_HOME_PRICE.getValue())
    }

    companion object {
        fun create(config: EssentialEconomyConfig): EssentialEconomyConfigSnapshot {
            return EssentialEconomyConfigSnapshot(config)
        }
    }
}
