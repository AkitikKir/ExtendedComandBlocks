package com.example.extendedcommandblocks.network

import com.example.extendedcommandblocks.ExtendedCommandBlocksMod
import com.example.extendedcommandblocks.config.CommandWhitelist
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

object ModNetworking {
    val WHITELIST_SYNC_PACKET_ID: Identifier =
        Identifier(ExtendedCommandBlocksMod.MOD_ID, "whitelist_sync")
    val ACCESS_DENIED_PACKET_ID: Identifier =
        Identifier(ExtendedCommandBlocksMod.MOD_ID, "access_denied")

    fun registerServer() {
        ServerPlayConnectionEvents.JOIN.register(ServerPlayConnectionEvents.Join { handler, _, _ ->
            sendWhitelistSync(handler.player)
        })
    }

    fun syncWhitelistToAll(server: MinecraftServer) {
        server.playerManager.playerList.forEach(::sendWhitelistSync)
    }

    fun sendAccessDenied(player: ServerPlayerEntity, pos: BlockPos, message: String) {
        val buf = PacketByteBufs.create()
        buf.writeBlockPos(pos)
        buf.writeString(message)
        ServerPlayNetworking.send(player, ACCESS_DENIED_PACKET_ID, buf)
    }

    private fun sendWhitelistSync(player: ServerPlayerEntity) {
        val entries = CommandWhitelist.list().sorted()
        val buf = PacketByteBufs.create()
        buf.writeVarInt(entries.size)
        entries.forEach(buf::writeString)
        ServerPlayNetworking.send(player, WHITELIST_SYNC_PACKET_ID, buf)
    }
}
