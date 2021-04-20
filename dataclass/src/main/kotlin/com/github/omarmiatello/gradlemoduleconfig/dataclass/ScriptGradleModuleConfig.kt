package com.github.omarmiatello.gradlemoduleconfig.dataclass

public data class ScriptGradleModuleConfig(
    val gradleConfig: GradleConfig = GradleConfig("", "", "", "", ""),
    val modules: List<GradleComponent> = emptyList(),
    val logLevel: Int = -1,
    val hasPrompt: Boolean = false,
    val writeOnDisk: Boolean = false,
    val forceNamingConvention: Boolean = false,
) {
    public fun logV(message: Any?) {
        if (logLevel >= 2) println(message)
    }

    public fun logD(message: Any?) {
        if (logLevel >= 1) println(message)
    }

    public fun logI(message: Any?) {
        if (logLevel >= 0) println(message)
    }
}

public data class GradleConfig(
    val packagePrefix: String,
    val templatesDirPath: String,
    val settingsGradleFilename: String,
    val dependenciesFilename: String,
    val dependenciesPrefix: String,
)
