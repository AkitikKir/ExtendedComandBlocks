package com.example.extendedcommandblocks.vanish

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import java.util.UUID

object VanishManager {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val filePath: Path = FabricLoader.getInstance().configDir.resolve("extendedcommandblocks-vanish.json")
    private val vanishedPlayers: MutableSet<UUID> = Collections.synchronizedSet(mutableSetOf())

    fun load() {
        if (!Files.exists(filePath)) {
            save()
            return
        }

        try {
            Files.newBufferedReader(filePath).use { reader ->
                val type = object : TypeToken<MutableSet<String>>() {}.type
                val loaded: MutableSet<String> = gson.fromJson(reader, type) ?: mutableSetOf()
                vanishedPlayers.clear()
                loaded.mapNotNull(::parseUuidOrNull).forEach(vanishedPlayers::add)
            }
        } catch (_: Exception) {
            vanishedPlayers.clear()
            save()
        }
    }

    private fun save() {
        Files.createDirectories(filePath.parent)
        Files.newBufferedWriter(filePath).use { writer ->
            gson.toJson(vanishedPlayers.map(UUID::toString).sorted(), writer)
        }
    }

    fun registerServer() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register { _, sender, _ ->
            if (!isVanished(sender)) {
                return@register true
            }

            sender.sendMessage(Text.translatable("message.extendedcommandblocks.vanish_chat_blocked"), false)
            false
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            val joiningPlayer = handler.player
            if (isVanished(joiningPlayer)) {
                joiningPlayer.isInvisible = true
                hideFromAll(server, joiningPlayer)
            }
            hideVanishedFrom(joiningPlayer, server)
        }

    }

    fun isVanished(player: ServerPlayerEntity): Boolean {
        return vanishedPlayers.contains(player.uuid)
    }

    fun setVanished(server: MinecraftServer, target: ServerPlayerEntity, vanished: Boolean): Boolean {
        val changed = if (vanished) {
            vanishedPlayers.add(target.uuid)
        } else {
            vanishedPlayers.remove(target.uuid)
        }

        if (!changed) {
            return false
        }

        save()
        target.isInvisible = vanished
        if (vanished) {
            hideFromAll(server, target)
        } else {
            showToAll(server, target)
        }
        return true
    }

    private fun hideVanishedFrom(observer: ServerPlayerEntity, server: MinecraftServer) {
        val uuids = vanishedPlayers.toList()
        if (uuids.isEmpty()) {
            return
        }

        uuids.forEach { uuid ->
            val hidden = server.playerManager.getPlayer(uuid) ?: return@forEach
            if (hidden == observer) {
                return@forEach
            }
            observer.networkHandler.sendPacket(PlayerRemoveS2CPacket(listOf(hidden.uuid)))
        }
    }

    private fun hideFromAll(server: MinecraftServer, target: ServerPlayerEntity) {
        server.playerManager.playerList.forEach { observer ->
            if (observer == target) {
                return@forEach
            }
            observer.networkHandler.sendPacket(PlayerRemoveS2CPacket(listOf(target.uuid)))
        }
    }

    private fun showToAll(server: MinecraftServer, target: ServerPlayerEntity) {
        server.playerManager.playerList.forEach { observer ->
            if (observer == target) {
                return@forEach
            }
            observer.networkHandler.sendPacket(PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, target))
        }
    }

    private fun parseUuidOrNull(raw: String): UUID? {
        return try {
            UUID.fromString(raw)
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
