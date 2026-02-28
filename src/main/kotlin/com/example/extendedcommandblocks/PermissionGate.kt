package com.example.extendedcommandblocks

import com.example.extendedcommandblocks.effect.ModEffects
import net.minecraft.server.network.ServerPlayerEntity

object PermissionGate {
    fun hasCommandBlockAccess(player: ServerPlayerEntity): Boolean {
        return player.abilities.creativeMode ||
            player.isCreativeLevelTwoOp ||
            hasPotionAccess(player)
    }

    fun hasPotionAccess(player: ServerPlayerEntity): Boolean {
        return player.hasStatusEffect(ModEffects.COMMAND_ACCESS_EFFECT)
    }
}
