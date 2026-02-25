package com.example.extendedcommandblocks.command

import com.example.extendedcommandblocks.config.CommandWhitelist
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
                        val added = CommandWhitelist.add(raw)
                        context.source.sendFeedback({ Text.literal("Добавлена команда: /$added") }, true)
                        1
                    })
        )

        dispatcher.register(
            literal("commanddel")
                .requires { it.hasPermissionLevel(4) }
                .then(argument("command", StringArgumentType.word())
                    .executes { context ->
                        val raw = StringArgumentType.getString(context, "command")
                        val removed = CommandWhitelist.remove(raw)
                        context.source.sendFeedback({ Text.literal("Удалена команда: /$removed") }, true)
                        1
                    })
        )

        dispatcher.register(
            literal("commandlist")
                .requires { it.hasPermissionLevel(4) }
                .executes { context ->
                    val entries = CommandWhitelist.list().sorted()
                    val message = if (entries.isEmpty()) {
                        "Whitelist пуст"
                    } else {
                        "Whitelist: ${entries.joinToString(", ") { "/$it" }}"
                    }
                    context.source.sendFeedback({ Text.literal(message) }, false)
                    1
                }
        )
    }
}
