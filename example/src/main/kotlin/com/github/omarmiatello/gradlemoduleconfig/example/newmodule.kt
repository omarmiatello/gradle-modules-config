@file:Suppress("ComplexMethod", "TopLevelPropertyNaming", "WildcardImport", "MaxLineLength")

package com.github.omarmiatello.gradlemoduleconfig.example

import com.github.omarmiatello.gradlemoduleconfig.dataclass.App
import com.github.omarmiatello.gradlemoduleconfig.dataclass.GradleConfig
import com.github.omarmiatello.gradlemoduleconfig.dataclass.Group
import com.github.omarmiatello.gradlemoduleconfig.dataclass.LibAndroid
import com.github.omarmiatello.gradlemoduleconfig.dataclass.LibKotlin
import com.github.omarmiatello.gradlemoduleconfig.dataclass.ScriptGradleModuleConfig
import com.github.omarmiatello.gradlemoduleconfig.dataclass.toGradleComponent
import com.github.omarmiatello.gradlemoduleconfig.script.buildModules
import org.docopt.Docopt

const val usage: String = """
This tool create the modules inside the Android project.
Usage:
 newmodule.kt [--app NAME | --lib NAME | --lib-hilt NAME | --lib-java-kotlin NAME] [--quiet | --verbose] [--dry-run] [--no-prompt]

Options:
 -h --help                  Show this guide
 --app NAME                 Create a new app module. NAME could be without colon like 'my-app' or with colon like ':my-feature:app'
 --lib NAME                 Create a new Android lib module. NAME could be without colon like 'my-lib' or with colon like ':my-feature:core'
 --lib-hilt NAME            Create a new Android lib module with dagger. NAME could be without colon like 'my-lib' or with colon like ':my-feature:domain'
 --lib-kotlin NAME          Create a new lib module. NAME could be without colon like 'my-lib' or with colon like ':my-feature:core'
 --quiet                    Print less text
 --verbose                  Print more text
 --dry-run                  Preview of the changes. Do not write on disk.
 --no-prompt                Skip the user confirmation
 
Example:
 kscript newmodule.kt --lib my-feature
 kscript newmodule.kt --app :my-feature:app
 kscript newmodule.kt --lib my-feature:core
"""

fun main(args: Array<String>) {
    if (args.isEmpty()) println("For help use: kscript newmodule.kt --help")

    val config = ScriptGradleModuleConfig(
        gradleConfig = GradleConfig(
            packagePrefix = "com.github.owner.projectname",
            templatesDirPath = "templates",
            settingsGradleFilename = "settings.gradle.kts",
            dependenciesFilename = "buildSrc/src/main/kotlin/Dependencies.kt",
            dependenciesPrefix = "module",
        ),
        modules = listOf(
            Group(
                gradleName = "feature-name",
                modules = listOf(
                    App(
                        gradleName = "app",
                    ),
                    LibAndroid(
                        gradleName = "ui",
                    ),
                    LibKotlin(
                        gradleName = "core",
                    )
                )
            ),
            LibAndroid(
                gradleName = "my-lib",
            )
        ),
        logLevel = 1,
        hasPrompt = true,
        writeOnDisk = true,
        forceNamingConvention = false,
    )

    // start build modules
    config.parseArgs(args).buildModules(
        onContentReplace = { module ->
            mapOf(
                "packagename" to module.packageName,
                "moduleName" to module.moduleNameParts.joinToString("") { it.capitalize() }.decapitalize(),
                "ModuleName" to module.moduleNameParts.joinToString("") { it.capitalize() },
                "module_name" to module.moduleNameParts.joinToString("_")
            )
        },
        onDirReplace = { module ->
            mapOf("packagename" to module.packageName.replace('.', '/'))
        }
    )
}

fun ScriptGradleModuleConfig.parseArgs(args: Array<String>): ScriptGradleModuleConfig {
    val map: MutableMap<String, Any> = Docopt(usage).parse(args.toList())
    logV("Script args: $map")

    fun String.isTrue() = map[this] == true

    fun String.string() = map[this] as? String

    val app = "--app".string()
    val lib = "--lib".string()
    val libHilt = "--lib-hilt".string()
    val libKotlin = "--lib-kotlin".string()

    return copy(
        modules = when {
            app != null -> listOf(app.toGradleComponent { App(it) })
            lib != null -> listOf(lib.toGradleComponent { LibAndroid(it) })
            libHilt != null -> listOf(libHilt.toGradleComponent { LibAndroid(it, templateName = "lib-hilt") })
            libKotlin != null -> listOf(libKotlin.toGradleComponent { LibKotlin(it) })
            else -> modules
        },
        logLevel = when {
            "--quiet".isTrue() -> 0
            "--verbose".isTrue() -> 2
            else -> logLevel
        },
        hasPrompt = when {
            "--no-prompt".isTrue() -> false
            else -> hasPrompt
        },
        writeOnDisk = when {
            "--dry-run".isTrue() -> false
            else -> writeOnDisk
        },
    )
}
