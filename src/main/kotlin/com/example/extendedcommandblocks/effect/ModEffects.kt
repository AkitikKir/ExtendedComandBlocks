package com.example.extendedcommandblocks.effect

import com.example.extendedcommandblocks.ExtendedCommandBlocksMod
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModEffects {
    val COMMAND_ACCESS_EFFECT: StatusEffect = Registry.register(
        Registries.STATUS_EFFECT,
        Identifier(ExtendedCommandBlocksMod.MOD_ID, "command_access"),
        object : StatusEffect(StatusEffectCategory.BENEFICIAL, 0x4D8CFF) {}
    )

    val COMMAND_ACCESS_POTION: Potion = Registry.register(
        Registries.POTION,
        Identifier(ExtendedCommandBlocksMod.MOD_ID, "command_access"),
        Potion(StatusEffectInstance(COMMAND_ACCESS_EFFECT, 20 * 60 * 10, 0))
    )

    fun register() {
        // Object init triggers registrations.
    }

    fun hasCommandAccess(stack: ItemStack): Boolean {
        return stack.isOf(net.minecraft.item.Items.POTION)
    }
}
