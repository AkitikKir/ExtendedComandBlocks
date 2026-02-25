package com.example.extendedcommandblocks

import com.example.extendedcommandblocks.command.WhitelistCommands
import com.example.extendedcommandblocks.config.CommandWhitelist
import com.example.extendedcommandblocks.effect.ModEffects
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistry
import net.minecraft.item.Items
import net.minecraft.potion.Potions
import net.minecraft.recipe.Ingredient
import org.slf4j.LoggerFactory

class ExtendedCommandBlocksMod : ModInitializer {
    companion object {
        const val MOD_ID = "extendedcommandblocks"
        private val LOGGER = LoggerFactory.getLogger(MOD_ID)
    }

    override fun onInitialize() {
        ModEffects.register()
        registerBrewingRecipes()
        CommandWhitelist.load()

        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            WhitelistCommands.register(dispatcher)
        })

        LOGGER.info("Extended Command Blocks initialized")
    }

    private fun registerBrewingRecipes() {
        FabricBrewingRecipeRegistry.registerPotionRecipe(
            Potions.AWKWARD,
            Ingredient.ofItems(Items.ECHO_SHARD),
            ModEffects.COMMAND_ACCESS_POTION
        )
    }
}
