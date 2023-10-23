package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.EssentialCommands.LOGGER
import dev.jpcode.eccore.config.Config
import dev.jpcode.eccore.config.ConfigOption
import dev.jpcode.eccore.config.Option
import java.nio.file.Path
import java.time.ZoneId


class EssentialEconomyConfig(savePath: Path?, displayName: String?, documentationLink: String?) :
    Config<EssentialEconomyConfig?>(savePath, displayName, documentationLink) {
    @ConfigOption
    @JvmField
    val HOME_SET_PRICE = Option(
        "home_set_price", "x*10*2^x"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val HOME_TP_PRICE = Option(
        "home_tp_price", "IF(x <= 2 ,0 , 5*2^(x-2))"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val HOME_TP_COOLDOWN = Option(
        "home_tp_cooldown", "IF(x <= 2 ,0,IF(x >= 5,240,30*2^(x-2)))"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val HOME_TP_DELAY = Option(
        "home_tp_delay", "IF(x <= 2,3,6)"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val BACK_PRICE = Option(
        "back_price", "IF(x <= 2,0,5*2^(x-2))"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val TPA_PRICE = Option(
        "tpa_price", "IF(x <= 2,0,7.5*2^(x-2))"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val TPA_COOLDOWN = Option(
        "tpa_cooldown", "IF(x <= 2,0,IF(x >= 5,360,45*2^(x-2)))"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val TPA_DELAY = Option(
        "tpa_delay", "IF(x <= 2,3,9)"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val TIME_ZONE = Option(
        "timezone", ZoneId.systemDefault()
    ) { zoneId: String? -> ZoneId.of(zoneId) }

    @ConfigOption
    @JvmField
    val WARP_TP_PRICE = Option(
        "warp_tp_price", "30"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val WARP_TP_COOLDOWN = Option(
        "warp_tp_cooldown", "1200"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val WARP_TP_DELAY = Option(
        "warp_tp_delay", "6"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val WARP_HOME_PRICE = Option(
        "warp_home_price", "0"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val WARP_HOME_COOLDOWN = Option(
        "warp_home_cooldown", "0"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val WARP_HOME_DELAY = Option(
        "warp_home_delay", "3"
    ) { obj: String -> obj }

    @ConfigOption
    @JvmField
    val WARP_SET_HOME_PRICE = Option(
        "warp_set_home_price", "150"
    ) { obj: String -> obj }

    companion object {
        fun <T> getValueSafe(option: Option<T>, defaultValue: T): T {
            try {
                return option.getValue()
            } catch (ex: Exception) {
                // Someone was getting an error with eccore/config/Option not being found when Option.getValue() was called
                // from within ServerPlayerEntityMixin. I can't reproduce, but /shrug
                // We're actually catching a ClassNotFoundException due to mixin weirdness, I think...
                LOGGER.error(ex)
            }
            return defaultValue
        }
    }
}
