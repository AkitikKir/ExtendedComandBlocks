package com.example.extendedcommandblocks.command

import com.example.extendedcommandblocks.config.CommandWhitelist
import com.example.extendedcommandblocks.network.ModNetworking
import com.example.extendedcommandblocks.vanish.VanishManager
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object WhitelistCommands {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            literal("commandadd")
                .requires { it.hasPermissionLevel(4) }
                .then(argument("command", StringArgumentType.word())
                    .executes { context ->
                        val raw = StringArgumentType.getString(context, "command")
                        try {
                            val added = CommandWhitelist.add(raw)
                            ModNetworking.syncWhitelistToAll(context.source.server)
                            context.source.sendFeedback(
                                { Text.translatable("message.extendedcommandblocks.command_added", "/$added") },
                                true
                            )
                        } catch (ex: IllegalArgumentException) {
                            context.source.sendError(Text.literal(ex.message ?: "Invalid command"))
                            return@executes 0
                        }
                        1
                    })
        )

        dispatcher.register(
            literal("commanddel")
                .requires { it.hasPermissionLevel(4) }
                .then(argument("command", StringArgumentType.word())
                    .executes { context ->
                        val raw = StringArgumentType.getString(context, "command")
                        try {
                            val removed = CommandWhitelist.remove(raw)
                            ModNetworking.syncWhitelistToAll(context.source.server)
                            context.source.sendFeedback(
                                { Text.translatable("message.extendedcommandblocks.command_removed", "/$removed") },
                                true
                            )
                        } catch (ex: IllegalArgumentException) {
                            context.source.sendError(Text.literal(ex.message ?: "Invalid command"))
                            return@executes 0
                        }
                        1
                    })
        )

        dispatcher.register(
            literal("commandlist")
                .requires { it.hasPermissionLevel(4) }
                .executes { context ->
                    val entries = CommandWhitelist.list().sorted()
                    val text = if (entries.isEmpty()) {
                        Text.translatable("message.extendedcommandblocks.whitelist_empty")
                    } else {
                        Text.translatable(
                            "message.extendedcommandblocks.whitelist",
                            entries.joinToString(", ") { "/$it" }
                        )
                    }
                    context.source.sendFeedback({ text }, false)
                    1
                }
        )

        dispatcher.register(
            literal("vanish")
                .requires { it.hasPermissionLevel(4) }
                .then(argument("target", EntityArgumentType.player())
                    .executes { context ->
                        val target = EntityArgumentType.getPlayer(context, "target")
                        val becomeVanished = !VanishManager.isVanished(target)
                        setVanishState(context.source, target, becomeVanished)
                    }
                    .then(argument("enabled", BoolArgumentType.bool())
                        .executes { context ->
                            val target = EntityArgumentType.getPlayer(context, "target")
                            val enabled = BoolArgumentType.getBool(context, "enabled")
                            setVanishState(context.source, target, enabled)
                        }))
        )
    }

    private fun setVanishState(
        source: ServerCommandSource,
        target: net.minecraft.server.network.ServerPlayerEntity,
        enabled: Boolean
    ): Int {
        val changed = VanishManager.setVanished(source.server, target, enabled)
        if (!changed) {
            source.sendError(
                Text.translatable(
                    if (enabled) {
                        "message.extendedcommandblocks.vanish_already_enabled"
                    } else {
                        "message.extendedcommandblocks.vanish_already_disabled"
                    },
                    target.displayName
                )
            )
            return 0
        }

        source.sendFeedback(
            {
                Text.translatable(
                    if (enabled) {
                        "message.extendedcommandblocks.vanish_enabled"
                    } else {
                        "message.extendedcommandblocks.vanish_disabled"
                    },
                    target.displayName
                )
            },
            true
        )
        target.sendMessage(
            Text.translatable(
                if (enabled) {
                    "message.extendedcommandblocks.vanish_self_enabled"
                } else {
                    "message.extendedcommandblocks.vanish_self_disabled"
                }
            ),
            false
        )
        return 1
    }
}
