package com.example.extendedcommandblocks.config

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.ConcurrentSkipListSet

object CommandWhitelist {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val allowedRoots = ConcurrentSkipListSet<String>()
    private val filePath: Path = FabricLoader.getInstance().configDir.resolve("extendedcommandblocks-whitelist.json")

    fun load() {
        if (!Files.exists(filePath)) {
            save()
            return
        }

        try {
            Files.newBufferedReader(filePath).use { reader ->
                val type = object : TypeToken<MutableSet<String>>() {}.type
                val loaded: MutableSet<String> = gson.fromJson(reader, type) ?: mutableSetOf()
                allowedRoots.clear()
                loaded.map(::normalizeRoot).filter(String::isNotBlank).forEach(allowedRoots::add)
            }
        } catch (ex: Exception) {
            allowedRoots.clear()
            save()
        }
    }

    fun save() {
        Files.createDirectories(filePath.parent)
        Files.newBufferedWriter(filePath).use { writer ->
            gson.toJson(allowedRoots, writer)
        }
    }

    fun add(command: String): String {
        val normalized = normalizeRoot(command)
        require(normalized.isNotBlank()) { "Команда не может быть пустой" }
        allowedRoots.add(normalized)
        save()
        return normalized
    }

    fun remove(command: String): String {
        val normalized = normalizeRoot(command)
        require(normalized.isNotBlank()) { "Команда не может быть пустой" }
        if (!allowedRoots.remove(normalized)) {
            throw IllegalArgumentException("Команда '$normalized' не найдена в whitelist")
        }
        save()
        return normalized
    }

    fun list(): Set<String> = allowedRoots.toSet()

    fun checkCommand(rawCommand: String): Boolean {
        val root = extractRoot(rawCommand) ?: return false
        return allowedRoots.contains(root)
    }

    fun extractRoot(rawCommand: String): String? {
        val trimmed = rawCommand.trim().removePrefix("/")
        if (trimmed.isBlank()) return null
        return normalizeRoot(trimmed.substringBefore(' '))
    }

    private fun normalizeRoot(command: String): String {
        return command.trim().removePrefix("/").lowercase(Locale.ROOT)
    }
}
