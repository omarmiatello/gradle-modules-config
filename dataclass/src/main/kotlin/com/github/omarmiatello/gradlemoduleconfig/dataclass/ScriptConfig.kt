package com.github.omarmiatello.gradlemoduleconfig.dataclass

public data class ScriptConfig(
    val gradleConfig: GradleConfig,
    val modules: List<GradleComponent>,
    val logLevel: Int,
    val hasPrompt: Boolean,
    val writeOnDisk: Boolean,
    val forceNamingConvention: Boolean,
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
