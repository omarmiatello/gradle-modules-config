# gradle-modules-config

[![GitHub license](https://img.shields.io/github/license/omarmiatello/gradle-modules-config)](LICENSE)
[![](https://img.shields.io/maven-central/v/com.github.omarmiatello.gradle-modules-config/script)](https://search.maven.org/search?q=g:com.github.omarmiatello.gradle-modules-config)
![Pre Merge Checks](https://github.com/omarmiatello/gradle-modules-config/workflows/Pre%20Merge%20Checks/badge.svg)
![Language](https://img.shields.io/github/languages/top/omarmiatello/gradle-modules-config?color=blue&logo=kotlin)


Define a configuration and this script will take care of creating the gradle modules

Note: Could be used with Ktor

## Setup

Add this in your `build.gradle.ktx` file:
```kotlin
implementation("com.github.omarmiatello.gradle-modules-config:script:1.0.1")
```

## How to use

#### Edit online your configuration!

Try it on Kotlin Playground https://pl.kotl.in/9oXsIoDNS or [our samples here](https://omarmiatello.github.io/gradle-modules-config/config_online.html)

<iframe src="https://pl.kotl.in/bEJDWFCw6?from=17&to=48&"></iframe>

#### kscript example

```kotlin
#!/usr/bin/env kscript
@file:DependsOn("com.github.omarmiatello.gradle-modules-config:script:1.0.1")
@file:DependsOn("com.offbytwo:docopt:0.6.0.20150202")

/**
 * ### Run script
 * > kscript newmodule.kt
 *
 * ### Edit script
 * This will open IntelliJ IDEA with a minimalistic project containing just your script and a generated gradle.build file
 * > kscript --idea newmodule.kt
 */
import com.github.omarmiatello.gradlemoduleconfig.dataclass.*
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
```


## License

```
MIT License

Copyright (c) 2021 Omar Miatello

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
