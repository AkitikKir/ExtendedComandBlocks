package com.example.extendedcommandblocks.network

object ClientWhitelistState {
    @Volatile
    private var allowedCommands: List<String> = emptyList()
    @Volatile
    private var deniedMessage: String? = null
    @Volatile
    private var deniedMessageUntilMs: Long = 0L

    fun updateWhitelist(commands: List<String>) {
        allowedCommands = commands.sorted()
    }

    fun getAllowedCommands(): List<String> = allowedCommands

    fun setDeniedMessage(message: String) {
        deniedMessage = message
        deniedMessageUntilMs = System.currentTimeMillis() + 5000L
    }

    fun currentDeniedMessage(): String? {
        val now = System.currentTimeMillis()
        return if (now <= deniedMessageUntilMs) deniedMessage else null
    }
}
