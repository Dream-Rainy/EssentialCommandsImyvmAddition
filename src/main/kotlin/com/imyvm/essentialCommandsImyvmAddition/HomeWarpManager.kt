package com.imyvm.essentialCommandsImyvmAddition

import com.fibermc.essentialcommands.playerdata.PlayerData
import com.fibermc.essentialcommands.text.ECText
import com.fibermc.essentialcommands.text.TextFormatType
import com.fibermc.essentialcommands.types.MinecraftLocation
import com.fibermc.essentialcommands.types.WarpLocation
import com.fibermc.essentialcommands.types.WarpStorage
import com.imyvm.essentialCommandsImyvmAddition.EssentialCommandsImyvmAdditionMain.NO_SUCH_PLAYER
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.WorldSavePath
import net.minecraft.world.PersistentState
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import java.util.stream.Stream

/**
* 若flag为0，则此时为白名单模式，blacklist默认为空；
* 若flag为1，则此时为黑名单模式，joinlist中的玩家可以免费传送，blacklist中的玩家不允许传送，
* 其余玩家均可传送，但需要花费游戏币，有冷却时间。
** */
data class ParsedData(val flag: Int, val owner: String, val joinlist: MutableList<String>, val blacklist: MutableList<String>)

object HomeWarpManager: PersistentState() {
    private val homeWarps: WarpStorage = WarpStorage()
    private var saveDir: Path? = null
    private var worldDataFile: File? = null
    private val HOME_WARP_KEYS = "homewarps"

    private val NO_SUCH_HOME_WARP = SimpleCommandExceptionType(
        ECText.getInstance().getText(
            "ecomy.cmd.error.no_such_homewarp",
            TextFormatType.Error
        )
    )

    private val HOME_WARP_STRING_CORRUPTION = SimpleCommandExceptionType(
        ECText.getInstance().getText(
            "ecomy.cmd.error.homewarp_string_corruption",
            TextFormatType.Error
        )
    )

    private val HOME_WARP_PLAYER_ALREADY_EXISTS = SimpleCommandExceptionType(
        ECText.getInstance().getText(
            "ecomy.cmd.error.homewarp_player_already_exists",
            TextFormatType.Error,
        )
    )

    fun onServerStart(server: MinecraftServer) {
        this.saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("essentialcommandsimyvmaddition")
        try {
            saveDir?.let { Files.createDirectories(it) }
        } catch (e:IOException) {
            e.printStackTrace()
        }

        this.worldDataFile = saveDir?.resolve("world_data.dat")?.toFile()

        try {
            val fileExisted = !worldDataFile!!.createNewFile()
            if (fileExisted && worldDataFile!!.length() > 0) {
                this.fromNbt(NbtIo.readCompressed(worldDataFile).getCompound("data"))
            } else {
                this.markDirty()
                this.save()
            }
        } catch (e: IOException) {
            EssentialCommandsImyvmAdditionMain.LOGGER.error(
                String.format(
                    "An unexpected error occoured while loading the Essential Commands World Data file (Path: '%s')",
                    worldDataFile!!.path
                ))
            e.printStackTrace()
        }
    }

    private fun fromNbt(tag: NbtCompound) {
        val warpsNbt = tag.getCompound(this.HOME_WARP_KEYS)
        homeWarps.loadNbt(warpsNbt)
        warpsLoadEvent.invoker().accept(homeWarps)
    }

    private val warpsLoadEvent: Event<Consumer<WarpStorage?>> = EventFactory.createArrayBacked(
        Consumer::class.java
    ) { listeners: Array<Consumer<WarpStorage?>> ->
        Consumer { warps: WarpStorage? ->
            for (event in listeners) {
                event.accept(warps)
            }
        }
    }

    private fun save() {
        EssentialCommandsImyvmAdditionMain.LOGGER.info("Saving world_data.dat (HomeWarps)...")
        super.save(worldDataFile)
        EssentialCommandsImyvmAdditionMain.LOGGER.info("world_data.dat saved.")
    }

    override fun writeNbt(nbt: NbtCompound): NbtCompound {
        // Warps to NBT
        val warpsNbt = NbtCompound()
        homeWarps.writeNbt(warpsNbt)
        nbt.put(this.HOME_WARP_KEYS, warpsNbt)
        return nbt
    }

    @Throws(CommandSyntaxException::class)
    fun setHomeWarp(warpName: String?, location: MinecraftLocation?, owner: ServerPlayerEntity, type: Int) {
        homeWarps.putCommand(
            warpName, WarpLocation(
                location,
                "${type};${owner.uuidAsString};;",
                warpName,
            )
        )
        markDirty()
        this.save()
    }

    @Throws(CommandSyntaxException::class)
    fun addToHomeWarp(warpName: String, senderPlayerEntity: ServerPlayerEntity, playerNeedAdd: ServerPlayerEntity) {
        val warpLocation = homeWarps[warpName] ?: throw NO_SUCH_HOME_WARP.create()
        val oldPermissionString = warpLocation.permissionString
        val data = parseString(oldPermissionString)
        if (data.owner != senderPlayerEntity.uuidAsString) {
            throw NO_SUCH_HOME_WARP.create()
        }
        if (!data.joinlist.contains(playerNeedAdd.uuidAsString)) {
            data.joinlist.add(playerNeedAdd.uuidAsString)
            val newPermissionString = composeString(data)
            changeHomeWarpPermissiongString(warpName, newPermissionString)
            markDirty()
            this.save()
        } else {
            val message = StringReader(playerNeedAdd.name.string)
            throw HOME_WARP_PLAYER_ALREADY_EXISTS.createWithContext(message)
        }
    }

    @Throws(CommandSyntaxException::class)
    fun delHomeWarp(warpName: String, senderPlayerEntity: ServerPlayerEntity): Boolean {
        val warpLocation = homeWarps[warpName] ?: throw NO_SUCH_HOME_WARP.create()
        val oldPermissionString = warpLocation.permissionString
        val data = parseString(oldPermissionString)
        if (data.owner == senderPlayerEntity.uuidAsString) {
            val prevValue: WarpLocation? = homeWarps.remove(warpName)
            markDirty()
            this.save()
            return prevValue != null
        } else {
               throw NO_SUCH_HOME_WARP.create()
        }
    }

    @Throws(CommandSyntaxException::class)
    fun delFromHomeWarp(warpName: String, senderPlayerEntity: ServerPlayerEntity, playerNeedRemove: ServerPlayerEntity) {
        val warpLocation = homeWarps[warpName] ?: throw NO_SUCH_HOME_WARP.create()
        val oldPermissionString = warpLocation.permissionString
        val data = parseString(oldPermissionString)
        val uuid = playerNeedRemove.uuidAsString
        if (data.owner != senderPlayerEntity.uuidAsString) {
            throw NO_SUCH_HOME_WARP.create()
        }
        if (data.flag == 1) {                                                       //黑名单模式
            if (data.joinlist.contains(uuid) && !data.blacklist.contains(uuid)) {   //在白名单，且不在黑名单
                data.joinlist.remove(uuid)
                data.blacklist.add(uuid)
            } else if (!data.blacklist.contains(uuid)) {                            //不在白名单，且不在黑名单
                data.blacklist.add(uuid)
            } else {                                                                //在黑名单
                throw HOME_WARP_PLAYER_ALREADY_EXISTS.create()
            }
        } else {                                                                    //白名单模式
            if (data.joinlist.contains(uuid)) {                                     //在白名单
                data.joinlist.remove(uuid)
            } else {                                                                //不在白名单内
                throw NO_SUCH_PLAYER.create()
            }
        }
        val newPermissionString = composeString(data)
        changeHomeWarpPermissiongString(warpName, newPermissionString)
    }

    fun getHomeWarp(warpName: String?): WarpLocation? {
        return homeWarps[warpName]
    }

    @Throws(CommandSyntaxException::class)
    fun checkPermission(warpName: String, player: ServerPlayerEntity): Boolean {
        val warpLocation = homeWarps[warpName]
        val permissionString = warpLocation!!.permissionString
        val data = parseString(permissionString)
        if (data.flag == 0 && (player.uuidAsString == data.owner || data.joinlist.contains(player.uuidAsString))) return true
        if (data.flag == 1 && !data.blacklist.contains(player.uuidAsString)) return true
        return false
    }

    fun getHomeWarpNames(): List<String> {
        return this.homeWarps.keys.stream().toList()
    }

    fun getAccessibleWarps(player: ServerPlayerEntity): List<MutableMap.MutableEntry<String, WarpLocation>> {
//        val warpsStream: Stream<WarpLocation> = this.homeWarps.values.stream()
//        return warpsStream.filter { loc: WarpLocation ->
//            checkPermission(loc.name, player)
//        }
        return this.homeWarps.entries.filter { loc: MutableMap.MutableEntry<String, WarpLocation> ->
            checkPermission(loc.value.name, player)
        }
    }

    fun getOwnerWarps(player: ServerPlayerEntity): Stream<WarpLocation> {
        val warpStream = this.homeWarps.values.stream()
        return warpStream.filter { loc: WarpLocation ->
            val permissionString = loc.permissionString
            val data = parseString(permissionString)
            data.owner == player.entityName
        }
    }

    fun changeHomeWarpMode(warpName: String, senderPlayerEntity: ServerPlayerEntity) {
        val warpLocation = homeWarps[warpName] ?: throw NO_SUCH_HOME_WARP.create()
        val senderPlayerData = PlayerData.access(senderPlayerEntity)
        val oldPermissionString = warpLocation.permissionString
        val data = parseString(oldPermissionString)
        if (data.owner != senderPlayerEntity.uuidAsString) {
            throw NO_SUCH_HOME_WARP.create()
        }
        changeHomeWarpPermissiongString(warpName, composeString(ParsedData(1 - data.flag, data.owner, data.joinlist, data.blacklist)))
        senderPlayerData.sendCommandFeedback("cmd.warp.home.mode.change")
    }

    private fun changeHomeWarpPermissiongString(warpName: String, newPermissionString: String){
        val warpLocation = homeWarps[warpName]
        val warpLocationNBT = warpLocation!!.asNbt()
        warpLocationNBT.remove("permissionString")
        warpLocationNBT.putString("permissionString", newPermissionString)
        val newWarpLocation = WarpLocation.fromNbt(warpLocationNBT, warpLocation.name)
        homeWarps.remove(warpName)
        homeWarps.putCommand(
            warpName, newWarpLocation
        )
    }

    fun parseListSection(section: String): MutableList<String> {
        val parts = section.split(",")
        return if (parts.size == 1 && parts[0].isEmpty()) mutableListOf() else parts.toMutableList()
    }

    private fun parseString(string: String): ParsedData {
        val parts = string.split(";")
        val flag = parts[0].toIntOrNull()
            ?: throw HOME_WARP_STRING_CORRUPTION.create()
        val owner = parts[1]
        val joinlist = parseListSection(parts[2])
        val blacklist = parseListSection(parts[3])
        return ParsedData(flag, owner, joinlist, blacklist)
    }

    private fun composeString(data: ParsedData): String {
        val sb = StringBuilder()
        sb.append(data.flag).append(";")
        sb.append(data.owner).append(";")
        sb.append(data.joinlist.joinToString(","))
        if (data.flag == 1) {
            sb.append(";")
            sb.append(data.blacklist.joinToString(","))
        }
        return sb.toString()
    }

    fun getFreePermission(warpName: String, player: ServerPlayerEntity): Boolean {
        val warpLocation = homeWarps[warpName]
        val permissionString = warpLocation!!.permissionString
        val data = parseString(permissionString)
        val uuid = player.uuidAsString
        return data.owner == uuid || data.joinlist.contains(uuid)
    }
}