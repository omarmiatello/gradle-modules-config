package com.github.omarmiatello.gradlemoduleconfig.script

public fun askUserBoolean(message: String): Boolean {
    print("$message [Y/N]: ")
    return readLine()?.toLowerCase() in listOf("y", "yes", "ok", "")
}

public fun importantMessage(message: String): String =
    "*".repeat(message.length)
        .let { "$it\n$message\n$it" }
