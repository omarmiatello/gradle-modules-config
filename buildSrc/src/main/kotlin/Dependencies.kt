import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

fun String.isStableVersion(): Boolean {
    val upperCase = toUpperCase(java.util.Locale.ROOT)
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { it in upperCase }
    return stableKeyword || Regex("^[0-9,.v-]+(-r)?$").matches(this)
}

fun String.isNotStableVersion() = !isStableVersion()

object Sdk {
    const val min = 21
    const val target = 30
    const val compile = 30
}

object Version {
    const val java = "11"
    const val kotlin = "1.5.0"
}

object Lib {
    const val docopt = "com.offbytwo:docopt:0.6.0.20150202"
    const val kotlinxSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.0"

    const val testJunit = "junit:junit:4.13.2"
}

val DependencyHandler.moduleDataclass get() = project(":dataclass")
val DependencyHandler.moduleScript get() = project(":script")
val DependencyHandler.moduleDependenciesUpdate get() = project(":dependencies-update")
