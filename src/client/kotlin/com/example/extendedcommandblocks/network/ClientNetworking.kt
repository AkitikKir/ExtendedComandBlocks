package com.example.extendedcommandblocks.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ClientNetworking {
    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.WHITELIST_SYNC_PACKET_ID) { client, _, buf, _ ->
            val size = buf.readVarInt()
            val commands = mutableListOf<String>()
            repeat(size) {
                commands.add(buf.readString())
            }
            client.execute {
                ClientWhitelistState.updateWhitelist(commands)
            }
        }

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.ACCESS_DENIED_PACKET_ID) { client, _, buf, _ ->
            buf.readBlockPos()
            val message = buf.readString()
            client.execute {
                ClientWhitelistState.setDeniedMessage(message)
            }
        }
    }
}
