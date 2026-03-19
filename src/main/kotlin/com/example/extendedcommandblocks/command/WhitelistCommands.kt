package com.example.extendedcommandblocks.command

import com.example.extendedcommandblocks.config.CommandWhitelist
import com.example.extendedcommandblocks.network.ModNetworking
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
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
    }
}
