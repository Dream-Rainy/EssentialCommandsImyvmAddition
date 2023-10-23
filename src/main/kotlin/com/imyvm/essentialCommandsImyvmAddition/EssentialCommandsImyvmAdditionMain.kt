package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.text.ECText
import com.fibermc.essentialcommands.text.TextFormatType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.*

object EssentialCommandsImyvmAdditionMain : ModInitializer {
    val LOGGER: Logger = LoggerFactory.getLogger("essential-commands-imyvm-addition")
	private val BACKING_ECONOMY_CONFIG = EssentialEconomyConfig(
		Path.of("./config/EssentialEconomy.properties"),
		"Essential Economy Config",
		""
	)

	var ECONOMY_CONFIG = EssentialEconomyConfigSnapshot.create(BACKING_ECONOMY_CONFIG)
	val NO_ENOUGH_MONEY = SimpleCommandExceptionType(
		ECText.getInstance().getText(
			"ecomy.cmd.error.no_enough_money",
			TextFormatType.Error
		)
	)

	val NO_SUCH_PLAYER = SimpleCommandExceptionType(
		ECText.getInstance().getText(
			"ecomy.cmd.error.no_such_player",
			TextFormatType.Error
		)
	)

	val ON_COOLDOWN = SimpleCommandExceptionType(
		ECText.getInstance().getText(
			"ecomy.cmd.error.on_cooldown",
			TextFormatType.Error
		)
	)

	override fun onInitialize() {
		CommandRegistrationCallback.EVENT.register {dispatcher, _, _ ->
			register(dispatcher)
		}
		BACKING_ECONOMY_CONFIG.registerLoadHandler { backingConfig ->
			ECONOMY_CONFIG = backingConfig?.let { EssentialEconomyConfigSnapshot.create(it) }!!
		}
		BACKING_ECONOMY_CONFIG.loadOrCreateProperties()
		val task = object : TimerTask() {
			override fun run() {
				EconomyUtil.mapClear()
			}
		}
		val timezone = TimeZone.getTimeZone(ECONOMY_CONFIG.TIME_ZONE)
		val calendar = Calendar.getInstance(timezone).apply {
			time = Date()
			add(Calendar.DAY_OF_MONTH, 1)
			set(Calendar.HOUR_OF_DAY, 0)
			set(Calendar.MINUTE, 0)
			set(Calendar.SECOND, 0)
			set(Calendar.MILLISECOND, 0)
		}
		val timer = Timer()
		timer.scheduleAtFixedRate(task, calendar.time, 24 * 60 * 60 * 1000)
		ServerLifecycleEvents.SERVER_STARTING.register { server ->
			HomeWarpManager.onServerStart(server)
		}
		LOGGER.info("Essential Commands Imyvm Addition initialized")
	}
}