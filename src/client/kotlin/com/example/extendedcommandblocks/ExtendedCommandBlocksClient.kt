package com.example.extendedcommandblocks

import com.example.extendedcommandblocks.network.ClientNetworking
import net.fabricmc.api.ClientModInitializer

class ExtendedCommandBlocksClient : ClientModInitializer {
    override fun onInitializeClient() {
        ClientNetworking.register()
    }
}
